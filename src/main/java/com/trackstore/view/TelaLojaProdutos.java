package com.trackstore.view;

import com.trackstore.model.Produto;
import com.trackstore.model.Usuario;
import com.trackstore.service.Carrinho;
import com.trackstore.service.PedidoService;
import com.trackstore.service.ProdutoService;
import com.trackstore.util.Formato;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TelaLojaProdutos extends JFrame {

    private JTextField txtBusca;
    private JComboBox<Produto> comboProdutos;
    private JLabel lblSaldo;
    private JSpinner spnQuantidade;
    private JTable tabelaCarrinho;
    private DefaultTableModel modeloCarrinho;
    private JLabel lblTotalGeral;

    private final ProdutoService produtoService = new ProdutoService();
    private final PedidoService pedidoService = new PedidoService();
    private final Carrinho carrinho = new Carrinho();
    private final Usuario usuarioLogado;

    public TelaLojaProdutos(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        setTitle("Track Store - Loja de Produtos");
        setSize(620, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        montarTela();
        buscarProdutos();
    }

    private void montarTela() {
        JPanel painelTopo = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        painelTopo.add(new JLabel("Buscar produto:"), gbc);
        txtBusca = new JTextField(15);
        gbc.gridx = 1; gbc.weightx = 1;
        painelTopo.add(txtBusca, gbc);
        JButton btnBuscar = new JButton("Buscar");
        gbc.gridx = 2; gbc.weightx = 0;
        painelTopo.add(btnBuscar, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painelTopo.add(new JLabel("Produto:"), gbc);
        comboProdutos = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1;
        painelTopo.add(comboProdutos, gbc);
        gbc.gridwidth = 1; gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy = 2;
        painelTopo.add(new JLabel("Disponível:"), gbc);
        lblSaldo = new JLabel("-");
        lblSaldo.setFont(lblSaldo.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 1; gbc.gridwidth = 2;
        painelTopo.add(lblSaldo, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 3;
        painelTopo.add(new JLabel("Quantidade:"), gbc);
        // JSpinner com mínimo 1: quantidade negativa ou zero deixa de ser digitável
        spnQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 999_999, 1));
        gbc.gridx = 1;
        painelTopo.add(spnQuantidade, gbc);
        JButton btnAdicionar = new JButton("Adicionar ao carrinho");
        gbc.gridx = 2;
        painelTopo.add(btnAdicionar, gbc);

        modeloCarrinho = new DefaultTableModel(
                new Object[]{"Produto", "Qtd", "Preço unit.", "Total"}, 0);
        tabelaCarrinho = UI.tabelaSomenteLeitura(modeloCarrinho);

        JPanel painelInferior = new JPanel(new BorderLayout());
        lblTotalGeral = new JLabel("Total: " + Formato.moeda(java.math.BigDecimal.ZERO),
                SwingConstants.RIGHT);
        lblTotalGeral.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTotalGeral.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));

        JButton btnRemover = new JButton("Remover item");
        JButton btnConfirmarCompra = new JButton("Confirmar compra");

        painelInferior.add(lblTotalGeral, BorderLayout.NORTH);
        painelInferior.add(UI.painelBotoes(btnRemover, btnConfirmarCompra), BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(painelTopo, BorderLayout.NORTH);
        add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscarProdutos());
        btnAdicionar.addActionListener(e -> adicionarAoCarrinho());
        btnRemover.addActionListener(e -> removerDoCarrinho());
        btnConfirmarCompra.addActionListener(e -> confirmarCompra());
        comboProdutos.addActionListener(e -> atualizarSaldoExibido());
        UI.aplicarTema(this);
    }

    private void buscarProdutos() {
        String termo = txtBusca.getText().trim();
        UI.carregar(this,
                () -> produtoService.listarDisponiveis(termo),
                lista -> {
                    comboProdutos.removeAllItems();
                    for (Produto p : lista) {
                        comboProdutos.addItem(p);
                    }
                    atualizarSaldoExibido();
                    if (lista.isEmpty()) {
                        lblSaldo.setText("nenhum produto disponível");
                    }
                });
    }

    private void atualizarSaldoExibido() {
        Produto selecionado = (Produto) comboProdutos.getSelectedItem();
        if (selecionado == null) {
            lblSaldo.setText("-");
            return;
        }
        int restante = selecionado.getQuantidade() - carrinho.quantidadeDe(selecionado.getId());
        lblSaldo.setText(String.format("%d unidade(s) — %s",
                Math.max(restante, 0), Formato.moeda(selecionado.getPreco())));
    }

    private void adicionarAoCarrinho() {
        Produto selecionado = (Produto) comboProdutos.getSelectedItem();
        if (selecionado == null) {
            UI.aviso(this, Mensagens.SELECIONE_PRODUTO);
            return;
        }

        int quantidade = (Integer) spnQuantidade.getValue();

        // Relê o saldo do banco: o produto do combo foi carregado quando a tela
        // abriu e outro usuário pode ter comprado ou reposto desde então.
        Produto atual;
        try {
            atual = produtoService.buscarPorId(selecionado.getId());
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
            return;
        }

        if (atual == null || !atual.isAtivo()) {
            UI.aviso(this, Mensagens.PRODUTO_INATIVO);
            buscarProdutos();
            return;
        }

        // Soma o que JÁ está no carrinho: sem isso, adicionar 6 e depois mais 6 de um
        // produto com 10 em estoque passava, porque cada adição era validada sozinha.
        int jaNoCarrinho = carrinho.quantidadeDe(atual.getId());
        if (jaNoCarrinho + quantidade > atual.getQuantidade()) {
            UI.aviso(this, Mensagens.estoqueInsuficiente(atual.getNome(),
                    Math.max(atual.getQuantidade() - jaNoCarrinho, 0)));
            return;
        }

        carrinho.adicionar(atual, quantidade);
        atualizarTabelaCarrinho();
        spnQuantidade.setValue(1);
    }

    private void removerDoCarrinho() {
        int linha = tabelaCarrinho.getSelectedRow();
        if (linha < 0) {
            UI.aviso(this, Mensagens.SELECIONE_ITEM_CARRINHO);
            return;
        }
        carrinho.remover(linha);
        atualizarTabelaCarrinho();
    }

    private void atualizarTabelaCarrinho() {
        modeloCarrinho.setRowCount(0);
        for (Carrinho.Item item : carrinho.getItens()) {
            modeloCarrinho.addRow(new Object[]{
                    item.getNome(),
                    item.getQuantidade(),
                    Formato.moeda(item.getPrecoUnitario()),
                    Formato.moeda(item.getTotal())
            });
        }
        lblTotalGeral.setText("Total: " + Formato.moeda(carrinho.getTotal()));
        atualizarSaldoExibido();
    }

    /**
     * Confirmação ÚNICA, porém detalhada.
     *
     * A versão anterior abria dois JOptionPane em sequência ("Confirmar compra?" e
     * depois "Tem certeza?"), o que só treina o usuário a clicar Sim duas vezes sem
     * ler. Um diálogo com o resumo completo da compra informa mais e continua sendo
     * uma barreira deliberada contra o clique acidental.
     */
    private void confirmarCompra() {
        if (carrinho.isVazio()) {
            UI.aviso(this, Mensagens.CARRINHO_VAZIO);
            return;
        }

        StringBuilder resumo = new StringBuilder("<html><b>Confirme sua compra:</b><br><br><table>");
        for (Carrinho.Item item : carrinho.getItens()) {
            resumo.append(String.format("<tr><td>%dx</td><td>%s</td><td align='right'>%s</td></tr>",
                    item.getQuantidade(), item.getNome(), Formato.moeda(item.getTotal())));
        }
        resumo.append("</table><br><b>Total: ")
                .append(Formato.moeda(carrinho.getTotal()))
                .append("</b><br><br>Esta ação não pode ser desfeita.</html>");

        if (!UI.confirmar(this, "Confirmar compra", new JLabel(resumo.toString()))) {
            return;
        }

        try {
            pedidoService.finalizar(usuarioLogado, carrinho);
            UI.sucesso(this, Mensagens.COMPRA_REALIZADA);
            carrinho.limpar();
            atualizarTabelaCarrinho();
            buscarProdutos();

        } catch (ValidacaoException ex) {
            // Ex.: outro usuário levou a última unidade entre o carrinho e o "Confirmar".
            UI.aviso(this, ex.getMessage());
            buscarProdutos();
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
        }
    }
}
