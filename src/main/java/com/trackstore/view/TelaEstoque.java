package com.trackstore.view;

import com.trackstore.model.Fornecedor;
import com.trackstore.model.Produto;
import com.trackstore.service.FornecedorService;
import com.trackstore.service.ProdutoService;
import com.trackstore.service.ReposicaoService;
import com.trackstore.util.Formato;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Estoque com reposição: lista os produtos e permite repor pelo botão "+" da linha. */
public class TelaEstoque extends JFrame {

    private JTextField txtFiltroCodigo;
    private JTextField txtFiltroNome;
    private JTable tabela;
    private DefaultTableModel modelo;

    private final ProdutoService produtoService = new ProdutoService();
    private final FornecedorService fornecedorService = new FornecedorService();
    private final ReposicaoService reposicaoService = new ReposicaoService();
    private final List<Produto> produtosExibidos = new ArrayList<>();

    public TelaEstoque() {
        setTitle("Track Store - Estoque com Reposição");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        montarTela();
        carregarTabela();
    }

    private void montarTela() {
        JPanel painelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        painelFiltro.add(new JLabel("Código:"));
        txtFiltroCodigo = new JTextField(8);
        painelFiltro.add(txtFiltroCodigo);

        painelFiltro.add(new JLabel("Nome:"));
        txtFiltroNome = new JTextField(14);
        painelFiltro.add(txtFiltroNome);

        JButton btnFiltrar = new JButton("Filtrar");
        painelFiltro.add(btnFiltrar);
        btnFiltrar.addActionListener(e -> carregarTabela());

        modelo = new DefaultTableModel(
                new Object[]{"Código", "Nome", "Saldo disponível", "Preço", "Repor"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // só a coluna do botão
            }
        };
        tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumn("Repor").setCellRenderer(new BotaoRenderer());
        tabela.getColumn("Repor").setCellEditor(new BotaoEditor());
        tabela.getColumn("Repor").setMaxWidth(60);
        tabela.setRowHeight(28);

        JButton btnHistorico = new JButton("Ver histórico de reposições");
        btnHistorico.addActionListener(e -> new TelaHistoricoReposicao().setVisible(true));

        setLayout(new BorderLayout());
        add(painelFiltro, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(UI.painelBotoes(btnHistorico), BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnFiltrar);
        UI.aplicarTema(this);
    }

    private void carregarTabela() {
        String filtroCodigo = txtFiltroCodigo.getText();
        String filtroNome = txtFiltroNome.getText();

        UI.carregar(this,
                () -> produtoService.filtrar(filtroCodigo, filtroNome, true),
                lista -> {
                    produtosExibidos.clear();
                    produtosExibidos.addAll(lista);
                    modelo.setRowCount(0);
                    for (Produto p : lista) {
                        modelo.addRow(new Object[]{
                                p.getCodigo(),
                                p.getNome(),
                                p.getQuantidade(),
                                Formato.moeda(p.getPreco()),
                                "+"
                        });
                    }
                });
    }

    private void abrirReposicao(int linha) {
        if (linha < 0 || linha >= produtosExibidos.size()) {
            return;
        }
        Produto produto = produtosExibidos.get(linha);

        List<Fornecedor> fornecedores;
        try {
            fornecedores = fornecedorService.listarTodos();
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
            return;
        }

        if (fornecedores.isEmpty()) {
            UI.aviso(this, Mensagens.SEM_FORNECEDORES);
            return;
        }

        JComboBox<Fornecedor> comboFornecedor =
                new JComboBox<>(fornecedores.toArray(new Fornecedor[0]));
        // mínimo 1: a versão anterior aceitava qualquer número, e digitar -50 aqui
        // SUBTRAÍA 50 do estoque registrando isso no histórico como uma entrada
        JSpinner spnQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 999_999, 1));

        JPanel painel = new JPanel(new GridLayout(3, 2, 6, 6));
        painel.add(new JLabel("Produto:"));
        painel.add(new JLabel(produto.getNome()));
        painel.add(new JLabel("Fornecedor:"));
        painel.add(comboFornecedor);
        painel.add(new JLabel("Quantidade:"));
        painel.add(spnQuantidade);

        int opcao = JOptionPane.showConfirmDialog(this, painel,
                "Repor estoque", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) {
            return;
        }

        Fornecedor fornecedor = (Fornecedor) comboFornecedor.getSelectedItem();
        if (fornecedor == null) {
            UI.aviso(this, Mensagens.SELECIONE_FORNECEDOR);
            return;
        }

        try {
            reposicaoService.repor(produto.getId(), fornecedor.getId(),
                    (Integer) spnQuantidade.getValue());
            UI.sucesso(this, Mensagens.REPOSICAO_REALIZADA);
            carregarTabela();
        } catch (ValidacaoException ex) {
            UI.aviso(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
        }
    }

    /** Renderiza o botão "+" na coluna Repor. */
    private static class BotaoRenderer extends JButton implements TableCellRenderer {
        BotaoRenderer() {
            setText("+");
            UI.estilizarBotao(this, UI.ROXO);
            setMargin(new Insets(0, 0, 0, 0));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            return this;
        }
    }

    /** Trata o clique do botão "+" na coluna Repor. */
    private class BotaoEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton botao = new JButton("+");
        private int linhaAtual = -1;

        BotaoEditor() {
            UI.estilizarBotao(botao, UI.ROXO);
            botao.setMargin(new Insets(0, 0, 0, 0));
            botao.addActionListener(e -> {
                int linha = linhaAtual;
                fireEditingStopped();
                // invokeLater: deixa a tabela terminar de sair do modo de edição
                // antes do diálogo modal abrir, senão a célula fica "presa" editando
                SwingUtilities.invokeLater(() -> abrirReposicao(linha));
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            linhaAtual = row;
            return botao;
        }

        @Override
        public Object getCellEditorValue() {
            return "+";
        }
    }
}
