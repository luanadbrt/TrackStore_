package com.trackstore.view;

import com.trackstore.service.UsuarioService;
import com.trackstore.util.ValidacaoException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TelaLogin extends JFrame {

    private JTextField txtEmail;
    private JPasswordField txtSenha;
    private final UsuarioService usuarioService = new UsuarioService();

    public TelaLogin() {
        setTitle("Track Store - Acesso");
        setSize(980, 610);
        setMinimumSize(new Dimension(850, 540));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        montarTela();
    }

    private void montarTela() {
        JPanel raiz = new JPanel(new GridLayout(1, 2));
        raiz.setBackground(UI.FUNDO);

        JPanel visual = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                ImageIcon icon = UI.imagemIgreja(getWidth(), getHeight());
                if (icon != null) {
                    g2.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                    g2.setColor(new Color(35, 18, 58, 155));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, UI.ROXO_ESCURO, getWidth(), getHeight(), UI.AZUL);
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
        visual.setBorder(new EmptyBorder(50, 55, 50, 55));

        JPanel textoVisual = new JPanel();
        textoVisual.setOpaque(false);
        textoVisual.setLayout(new BoxLayout(textoVisual, BoxLayout.Y_AXIS));
        JLabel cruz = new JLabel("✝");
        cruz.setForeground(Color.WHITE);
        cruz.setFont(new Font("Serif", Font.PLAIN, 60));
        cruz.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel titulo = new JLabel("TRACK STORE");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 34));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitulo = new JLabel("Gestão de estoque e vendas");
        subtitulo.setForeground(new Color(240, 236, 247));
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel frase = new JLabel("Organização para servir melhor.");
        frase.setForeground(new Color(225, 220, 235));
        frase.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        frase.setAlignmentX(Component.LEFT_ALIGNMENT);

        textoVisual.add(cruz);
        textoVisual.add(Box.createVerticalStrut(8));
        textoVisual.add(titulo);
        textoVisual.add(Box.createVerticalStrut(8));
        textoVisual.add(subtitulo);
        textoVisual.add(Box.createVerticalStrut(18));
        textoVisual.add(frase);
        visual.add(textoVisual, BorderLayout.SOUTH);

        JPanel direita = new JPanel(new GridBagLayout());
        direita.setBackground(UI.FUNDO);
        JPanel card = UI.painelArredondado(24);
        card.setBorder(new EmptyBorder(35, 38, 35, 38));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(390, 420));

        JLabel marca = new JLabel("✦  TRACK STORE");
        marca.setFont(new Font("Segoe UI", Font.BOLD, 14));
        marca.setForeground(UI.ROXO);
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(marca);
        card.add(Box.createVerticalStrut(14));

        JLabel tituloLogin = new JLabel("Bem-vindo!");
        tituloLogin.setFont(new Font("Segoe UI", Font.BOLD, 27));
        tituloLogin.setForeground(UI.TEXTO);
        tituloLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel apoio = new JLabel("Entre para acessar o sistema.");
        apoio.setForeground(UI.TEXTO_SECUNDARIO);
        apoio.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(tituloLogin);
        card.add(Box.createVerticalStrut(5));
        card.add(apoio);
        card.add(Box.createVerticalStrut(28));

        card.add(label("E-mail"));
        txtEmail = new JTextField();
        UI.estilizarCampo(txtEmail);
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        card.add(txtEmail);
        card.add(Box.createVerticalStrut(17));

        card.add(label("Senha"));
        txtSenha = new JPasswordField();
        UI.estilizarCampo(txtSenha);
        txtSenha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        card.add(txtSenha);
        card.add(Box.createVerticalStrut(23));

        JButton btnEntrar = UI.botao("ENTRAR  →", UI.ROXO);
        btnEntrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        card.add(btnEntrar);
        card.add(Box.createVerticalStrut(12));

        JButton btnCadastrar = UI.botao("Ainda não tenho cadastro");
        btnCadastrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCadastrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(btnCadastrar);

        btnEntrar.addActionListener(e -> autenticar());
        btnCadastrar.addActionListener(e -> abrirCadastro());
        getRootPane().setDefaultButton(btnEntrar);

        direita.add(card);
        raiz.add(visual);
        raiz.add(direita);
        add(raiz);
    }

    private JLabel label(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UI.TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void abrirCadastro() {
        TelaCadastroUsuario dialogo = TelaCadastroUsuario.paraAutocadastro(this);
        dialogo.setVisible(true);
        if (dialogo.isSalvou()) {
            txtEmail.setText(dialogo.getEmailCadastrado());
            txtSenha.requestFocusInWindow();
        }
    }

    private void autenticar() {
        String email = txtEmail.getText().trim();
        String senha = new String(txtSenha.getPassword());
        try {
            UsuarioService.Login login = usuarioService.autenticar(email, senha);
            switch (login.resultado()) {
                case USUARIO_INEXISTENTE -> UI.erro(this, "O usuário não existe no sistema.");
                case SENHA_INCORRETA -> UI.erro(this, "Senha incorreta.");
                case OK -> {
                    new TelaPrincipal(login.usuario()).setVisible(true);
                    dispose();
                }
            }
        } catch (ValidacaoException ex) {
            UI.aviso(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
        } finally {
            txtSenha.setText("");
        }
    }
}
