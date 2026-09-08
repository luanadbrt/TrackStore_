package com.trackstore.view;

import com.trackstore.model.Produto;
import com.trackstore.model.Reposicao;
import com.trackstore.service.ProdutoService;
import com.trackstore.service.ReposicaoService;
import com.trackstore.util.Formato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Histórico de reposições de estoque.
 *
 * Tela nova. A tabela "reposicoes" já era gravada desde a primeira versão, mas
 * NUNCA era lida por nenhuma parte do sistema: o histórico existia só no banco,
 * invisível para o usuário.
 */
public class TelaHistoricoReposicao extends JFrame {

    private static final DateTimeFormatter ENTRADA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JComboBox<Object> comboProduto;
    private JTextField txtInicio;
    private JTextField txtFim;
    private JLabel lblResumo;
    private DefaultTableModel modelo;

    private final ReposicaoService reposicaoService = new ReposicaoService();
    private final ProdutoService produtoService = new ProdutoService();

    public TelaHistoricoReposicao() {
        setTitle("Track Store - Histórico de Reposições");
        setSize(760, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        montarTela();
        carregarProdutos();
        carregarTabela();
    }

    private void montarTela() {
        JPanel painelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));

        painelFiltro.add(new JLabel("Produto:"));
        comboProduto = new JComboBox<>();
        comboProduto.addItem("(todos)");
        comboProduto.setPreferredSize(new Dimension(220, 24));
        painelFiltro.add(comboProduto);

        painelFiltro.add(new JLabel("De:"));
        txtInicio = new JTextField(9);
        txtInicio.setToolTipText("dd/MM/aaaa");
        painelFiltro.add(txtInicio);

        painelFiltro.add(new JLabel("Até:"));
        txtFim = new JTextField(9);
        txtFim.setToolTipText("dd/MM/aaaa");
        painelFiltro.add(txtFim);

        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnLimpar = new JButton("Limpar");
        painelFiltro.add(btnFiltrar);
        painelFiltro.add(btnLimpar);

        modelo = new DefaultTableModel(
                new Object[]{"Data", "Produto", "Fornecedor", "Quantidade"}, 0);
        JTable tabela = UI.tabelaSomenteLeitura(modelo);

        lblResumo = new JLabel(" ");
        lblResumo.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        setLayout(new BorderLayout());
        add(painelFiltro, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(lblResumo, BorderLayout.SOUTH);

        btnFiltrar.addActionListener(e -> carregarTabela());
        btnLimpar.addActionListener(e -> {
            comboProduto.setSelectedIndex(0);
            txtInicio.setText("");
            txtFim.setText("");
            carregarTabela();
        });
        getRootPane().setDefaultButton(btnFiltrar);
        UI.aplicarTema(this);
    }

    private void carregarProdutos() {
        UI.carregar(this, produtoService::listarTodos, lista -> {
            for (Produto p : lista) {
                comboProduto.addItem(p);
            }
        });
    }

    private void carregarTabela() {
        Integer produtoId = comboProduto.getSelectedItem() instanceof Produto p ? p.getId() : null;

        LocalDateTime inicio = lerData(txtInicio.getText(), true);
        LocalDateTime fim = lerData(txtFim.getText(), false);

        UI.carregar(this,
                () -> reposicaoService.filtrar(produtoId, inicio, fim),
                lista -> {
                    modelo.setRowCount(0);
                    int totalUnidades = 0;
                    for (Reposicao r : lista) {
                        modelo.addRow(new Object[]{
                                Formato.dataHora(r.getDataReposicao()),
                                r.getProduto().getNome(),
                                r.getFornecedor().getNome(),
                                r.getQuantidade()
                        });
                        totalUnidades += r.getQuantidade();
                    }
                    lblResumo.setText(String.format(
                            "%d reposição(ões) — %d unidade(s) no total.", lista.size(), totalUnidades));
                });
    }

    /** Converte dd/MM/aaaa; data inválida ou vazia simplesmente não filtra. */
    private LocalDateTime lerData(String texto, boolean inicioDoDia) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            LocalDate data = LocalDate.parse(texto.trim(), ENTRADA);
            return inicioDoDia ? data.atStartOfDay() : data.atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
