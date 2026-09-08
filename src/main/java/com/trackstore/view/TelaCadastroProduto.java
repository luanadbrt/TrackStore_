package com.trackstore.view;

import com.trackstore.model.Produto;
import com.trackstore.service.ProdutoService;
import com.trackstore.util.Formato;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Cadastro e edição de produto (mesmo formulário para os dois casos).
 *
 * Antes esta tela só inseria e ainda repetia, na mão, validações que o DAO já
 * fazia — inclusive a mensagem de campo vazio, duplicada nos dois lugares.
 * Agora ela só converte texto em número e delega a regra ao ProdutoService.
 */
public class TelaCadastroProduto extends JDialog {

    private JTextField txtCodigo;
    private JTextField txtNome;
    private JTextField txtQuantidade;
    private JTextField txtPreco;

    private final ProdutoService produtoService = new ProdutoService();
    private final Produto produtoEmEdicao;
    private boolean salvou;

    private TelaCadastroProduto(Window pai, Produto produtoEmEdicao) {
        super(pai, ModalityType.APPLICATION_MODAL);
        this.produtoEmEdicao = produtoEmEdicao;

        setTitle(produtoEmEdicao == null
                ? "Track Store - Cadastro de Produtos"
                : "Track Store - Editar Produto");
        setSize(420, 280);
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        montarTela();
        preencherCampos();
    }

    public static TelaCadastroProduto paraNovo(Window pai) {
        return new TelaCadastroProduto(pai, null);
    }

    public static TelaCadastroProduto paraEdicao(Window pai, Produto produto) {
        return new TelaCadastroProduto(pai, produto);
    }

    private void montarTela() {
        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int linha = 0;

        gbc.gridx = 0; gbc.gridy = linha;
        painel.add(new JLabel("Código do produto:"), gbc);
        txtCodigo = new JTextField(20);
        gbc.gridx = 1;
        painel.add(txtCodigo, gbc);

        linha++;
        gbc.gridx = 0; gbc.gridy = linha;
        painel.add(new JLabel("Nome do produto:"), gbc);
        txtNome = new JTextField(20);
        gbc.gridx = 1;
        painel.add(txtNome, gbc);

        linha++;
        gbc.gridx = 0; gbc.gridy = linha;
        painel.add(new JLabel("Quantidade (unidades):"), gbc);
        txtQuantidade = new JTextField(20);
        gbc.gridx = 1;
        painel.add(txtQuantidade, gbc);

        linha++;
        gbc.gridx = 0; gbc.gridy = linha;
        painel.add(new JLabel("Preço (R$):"), gbc);
        txtPreco = new JTextField(20);
        gbc.gridx = 1;
        painel.add(txtPreco, gbc);

        linha++;
        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar = new JButton("Cancelar");
        gbc.gridx = 0; gbc.gridy = linha; gbc.gridwidth = 2;
        painel.add(UI.painelBotoes(btnConfirmar, btnCancelar), gbc);

        btnConfirmar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnConfirmar);

        add(painel);
        UI.aplicarTema(this);
    }

    private void preencherCampos() {
        if (produtoEmEdicao != null) {
            txtCodigo.setText(String.valueOf(produtoEmEdicao.getCodigo()));
            txtNome.setText(produtoEmEdicao.getNome());
            txtQuantidade.setText(String.valueOf(produtoEmEdicao.getQuantidade()));
            txtPreco.setText(produtoEmEdicao.getPreco().toPlainString().replace('.', ','));
        }
    }

    private void salvar() {
        try {
            String textoCodigo = txtCodigo.getText().trim();
            String nome = txtNome.getText().trim();
            String textoQuantidade = txtQuantidade.getText().trim();
            String textoPreco = txtPreco.getText().trim();

            if (textoCodigo.isEmpty() || nome.isEmpty()
                    || textoQuantidade.isEmpty() || textoPreco.isEmpty()) {
                throw new ValidacaoException(Mensagens.CAMPOS_OBRIGATORIOS);
            }

            int codigo = Formato.paraInteiro(textoCodigo, Mensagens.CODIGO_INVALIDO);
            int quantidade = Formato.paraInteiro(textoQuantidade, Mensagens.QUANTIDADE_INVALIDA);
            BigDecimal preco = Formato.paraDecimal(textoPreco);

            if (produtoEmEdicao == null) {
                produtoService.cadastrar(codigo, nome, quantidade, preco);
            } else {
                produtoService.atualizar(produtoEmEdicao.getId(), codigo, nome, quantidade, preco);
            }

            salvou = true;
            UI.sucesso(this, Mensagens.PRODUTO_SALVO);
            dispose();

        } catch (ValidacaoException ex) {
            UI.aviso(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
        }
    }

    public boolean isSalvou() {
        return salvou;
    }
}
