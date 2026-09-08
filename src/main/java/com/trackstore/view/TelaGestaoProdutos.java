package com.trackstore.view;

import com.trackstore.model.Produto;
import com.trackstore.service.ProdutoService;
import com.trackstore.util.Formato;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestão de produtos: listar, editar, excluir e reativar.
 *
 * Tela nova — antes só existia a inserção, e nenhuma das telas permitia alterar
 * ou remover um produto já cadastrado (o CRUD parava no "C" e no "R").
 */
public class TelaGestaoProdutos extends JFrame {

    private JTextField txtFiltroCodigo;
    private JTextField txtFiltroNome;
    private JCheckBox chkSomenteAtivos;
    private DefaultTableModel modelo;
    private JTable tabela;

    private final ProdutoService produtoService = new ProdutoService();
    private final List<Produto> produtosExibidos = new ArrayList<>();

    public TelaGestaoProdutos() {
        setTitle("Track Store - Gestão de Produtos");
        setSize(760, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        montarTela();
        carregarTabela();
    }

    private void montarTela() {
        // ---- filtros ----
        JPanel painelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        painelFiltro.add(new JLabel("Código:"));
        txtFiltroCodigo = new JTextField(8);
        painelFiltro.add(txtFiltroCodigo);

        painelFiltro.add(new JLabel("Nome:"));
        txtFiltroNome = new JTextField(14);
        painelFiltro.add(txtFiltroNome);

        chkSomenteAtivos = new JCheckBox("Somente ativos", true);
        painelFiltro.add(chkSomenteAtivos);

        JButton btnFiltrar = new JButton("Filtrar");
        painelFiltro.add(btnFiltrar);

        // ---- tabela ----
        modelo = new DefaultTableModel(
                new Object[]{"Código", "Nome", "Quantidade", "Preço", "Situação"}, 0);
        tabela = UI.tabelaSomenteLeitura(modelo);

        // ---- ações ----
        JButton btnNovo = new JButton("Novo");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnReativar = new JButton("Reativar");

        setLayout(new BorderLayout());
        add(painelFiltro, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(UI.painelBotoes(btnNovo, btnEditar, btnExcluir, btnReativar), BorderLayout.SOUTH);

        btnFiltrar.addActionListener(e -> carregarTabela());
        chkSomenteAtivos.addActionListener(e -> carregarTabela());
        btnNovo.addActionListener(e -> novo());
        btnEditar.addActionListener(e -> editar());
        btnExcluir.addActionListener(e -> excluir());
        btnReativar.addActionListener(e -> reativar());
        getRootPane().setDefaultButton(btnFiltrar);
        UI.aplicarTema(this);
    }

    private void carregarTabela() {
        String filtroCodigo = txtFiltroCodigo.getText();
        String filtroNome = txtFiltroNome.getText();
        boolean somenteAtivos = chkSomenteAtivos.isSelected();

        UI.carregar(this,
                () -> produtoService.filtrar(filtroCodigo, filtroNome, somenteAtivos),
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
                                p.getSituacao()
                        });
                    }
                });
    }

    /** Produto da linha selecionada, ou null (avisando o usuário). */
    private Produto selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || linha >= produtosExibidos.size()) {
            UI.aviso(this, Mensagens.SELECIONE_PRODUTO);
            return null;
        }
        return produtosExibidos.get(tabela.convertRowIndexToModel(linha));
    }

    private void novo() {
        TelaCadastroProduto dialogo = TelaCadastroProduto.paraNovo(this);
        dialogo.setVisible(true);
        if (dialogo.isSalvou()) {
            carregarTabela();
        }
    }

    private void editar() {
        Produto produto = selecionado();
        if (produto == null) {
            return;
        }
        TelaCadastroProduto dialogo = TelaCadastroProduto.paraEdicao(this, produto);
        dialogo.setVisible(true);
        if (dialogo.isSalvou()) {
            carregarTabela();
        }
    }

    private void excluir() {
        Produto produto = selecionado();
        if (produto == null) {
            return;
        }

        boolean confirmado = UI.confirmar(this, "Confirmar exclusão",
                "Excluir o produto \"" + produto.getNome() + "\"?\n\n"
                        + "Se ele já tiver vendas ou reposições registradas, será apenas\n"
                        + "INATIVADO, para preservar o histórico.");
        if (!confirmado) {
            return;
        }

        try {
            boolean apagouDeVez = produtoService.excluir(produto.getId());
            UI.sucesso(this, apagouDeVez
                    ? "Produto excluído com sucesso."
                    : Mensagens.PRODUTO_INATIVADO);
            carregarTabela();
        } catch (ValidacaoException ex) {
            UI.aviso(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
        }
    }

    private void reativar() {
        Produto produto = selecionado();
        if (produto == null) {
            return;
        }
        try {
            produtoService.reativar(produto.getId());
            UI.sucesso(this, Mensagens.PRODUTO_REATIVADO);
            carregarTabela();
        } catch (ValidacaoException ex) {
            UI.aviso(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
        }
    }
}
