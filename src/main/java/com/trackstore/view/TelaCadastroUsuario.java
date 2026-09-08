package com.trackstore.view;

import com.trackstore.model.Usuario;
import com.trackstore.service.UsuarioService;
import com.trackstore.util.CpfUtil;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import javax.swing.*;
import java.awt.*;

/**
 * Cadastro e edição de usuário.
 *
 * É um JDialog modal (antes era JFrame): impedia que o usuário abrisse a mesma
 * tela várias vezes e ficasse com duas cópias do formulário abertas ao mesmo tempo.
 */
public class TelaCadastroUsuario extends JDialog {

    private JTextField txtNome;
    private JTextField txtEmail;
    private JTextField txtCpf;
    private JPasswordField txtSenha;
    private JComboBox<Usuario.Tipo> comboTipo;

    private final UsuarioService usuarioService = new UsuarioService();

    /** null = novo usuário; preenchido = edição. */
    private final Usuario usuarioEmEdicao;
    private final boolean modoAdmin;

    private boolean salvou;
    private String emailCadastrado;

    private TelaCadastroUsuario(Window pai, Usuario usuarioEmEdicao, boolean modoAdmin) {
        super(pai, ModalityType.APPLICATION_MODAL);
        this.usuarioEmEdicao = usuarioEmEdicao;
        this.modoAdmin = modoAdmin;

        setTitle(usuarioEmEdicao == null
                ? "Track Store - Cadastro de Usuário"
                : "Track Store - Editar Usuário");
        setSize(430, 330);
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        montarTela();
        preencherCampos();
    }

    /** Autocadastro pela tela de login: sem escolha de tipo. */
    public static TelaCadastroUsuario paraAutocadastro(Window pai) {
        return new TelaCadastroUsuario(pai, null, false);
    }

    /** Admin criando um novo usuário, podendo escolher o tipo. */
    public static TelaCadastroUsuario paraNovoPeloAdmin(Window pai) {
        return new TelaCadastroUsuario(pai, null, true);
    }

    public static TelaCadastroUsuario paraEdicao(Window pai, Usuario usuario) {
        return new TelaCadastroUsuario(pai, usuario, true);
    }

    private void montarTela() {
        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int linha = 0;

        gbc.gridx = 0; gbc.gridy = linha;
        painel.add(new JLabel("Nome:"), gbc);
        txtNome = new JTextField(20);
        gbc.gridx = 1;
        painel.add(txtNome, gbc);

        linha++;
        gbc.gridx = 0; gbc.gridy = linha;
        painel.add(new JLabel("E-mail:"), gbc);
        txtEmail = new JTextField(20);
        gbc.gridx = 1;
        painel.add(txtEmail, gbc);

        linha++;
        gbc.gridx = 0; gbc.gridy = linha;
        painel.add(new JLabel("CPF (11 dígitos):"), gbc);
        txtCpf = new JTextField(20);
        gbc.gridx = 1;
        painel.add(txtCpf, gbc);

        linha++;
        gbc.gridx = 0; gbc.gridy = linha;
        painel.add(new JLabel(usuarioEmEdicao == null ? "Senha:" : "Nova senha:"), gbc);
        txtSenha = new JPasswordField(20);
        gbc.gridx = 1;
        painel.add(txtSenha, gbc);

        if (usuarioEmEdicao != null) {
            linha++;
            gbc.gridx = 1; gbc.gridy = linha;
            JLabel dica = new JLabel("Deixe em branco para manter a senha atual.");
            dica.setFont(dica.getFont().deriveFont(Font.ITALIC, 11f));
            painel.add(dica, gbc);
        }

        if (modoAdmin) {
            linha++;
            gbc.gridx = 0; gbc.gridy = linha;
            painel.add(new JLabel("Tipo:"), gbc);
            comboTipo = new JComboBox<>(Usuario.Tipo.values());
            gbc.gridx = 1;
            painel.add(comboTipo, gbc);
        }

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
        if (usuarioEmEdicao != null) {
            txtNome.setText(usuarioEmEdicao.getNome());
            txtEmail.setText(usuarioEmEdicao.getEmail());
            txtCpf.setText(CpfUtil.formatar(usuarioEmEdicao.getCpf()));
            if (comboTipo != null) {
                comboTipo.setSelectedItem(usuarioEmEdicao.getTipo());
            }
        }
    }

    private void salvar() {
        String nome = txtNome.getText();
        String email = txtEmail.getText();
        String cpf = txtCpf.getText();
        String senha = new String(txtSenha.getPassword());
        Usuario.Tipo tipo = comboTipo == null ? null : (Usuario.Tipo) comboTipo.getSelectedItem();

        try {
            if (usuarioEmEdicao == null) {
                usuarioService.cadastrar(nome, email, cpf, senha, tipo);
            } else {
                usuarioService.atualizar(usuarioEmEdicao.getId(), nome, email, cpf, tipo,
                        senha.isEmpty() ? null : senha);
            }

            salvou = true;
            emailCadastrado = email.trim().toLowerCase();
            UI.sucesso(this, Mensagens.USUARIO_SALVO);
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

    public String getEmailCadastrado() {
        return emailCadastrado;
    }
}
