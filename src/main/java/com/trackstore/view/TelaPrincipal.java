package com.trackstore.view;

import com.trackstore.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TelaPrincipal extends JFrame {

    private final Usuario usuarioLogado;

    public TelaPrincipal(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        setTitle("Track Store - Painel Principal");
        setSize(1080, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        montarTela();
    }

    private void montarTela() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(UI.FUNDO);

        JPanel menu = new JPanel();
        menu.setBackground(UI.ROXO_ESCURO);
        menu.setBorder(new EmptyBorder(28, 18, 22, 18));
        menu.setPreferredSize(new Dimension(235, 0));
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("✝  TRACK STORE");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(logo);
        JLabel logoSub = new JLabel("  gestão para servir melhor");
        logoSub.setForeground(new Color(205, 194, 221));
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(logoSub);
        menu.add(Box.createVerticalStrut(35));

        adicionarMenu(menu, "⌂   Início", e -> {});
        adicionarMenu(menu, "▣   Loja de Produtos", e -> new TelaLojaProdutos(usuarioLogado).setVisible(true));

        if (usuarioLogado.isAdmin()) {
            adicionarMenu(menu, "□   Produtos", e -> new TelaGestaoProdutos().setVisible(true));
            adicionarMenu(menu, "▤   Estoque", e -> new TelaEstoque().setVisible(true));
            adicionarMenu(menu, "↻   Reposições", e -> new TelaHistoricoReposicao().setVisible(true));
            adicionarMenu(menu, "♧   Fornecedores", e -> new TelaFornecedores().setVisible(true));
            adicionarMenu(menu, "♙   Usuários", e -> new TelaGestaoUsuarios(usuarioLogado).setVisible(true));
        }

        menu.add(Box.createVerticalGlue());
        JButton sair = menuBotao("↪   Sair");
        sair.addActionListener(e -> {
            dispose();
            new TelaLogin().setVisible(true);
        });
        menu.add(sair);

        JPanel conteudo = new JPanel(new BorderLayout(0, 20));
        conteudo.setBackground(UI.FUNDO);
        conteudo.setBorder(new EmptyBorder(30, 34, 30, 34));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        JPanel saudacao = UI.cabecalho("Olá, " + usuarioLogado.getNome() + "! 👋",
                "Acompanhe o Track Store de forma simples e organizada.");
        topo.add(saudacao, BorderLayout.WEST);
        JLabel perfil = new JLabel("PERFIL  •  " + usuarioLogado.getTipo());
        perfil.setOpaque(true);
        perfil.setBackground(new Color(235, 227, 245));
        perfil.setForeground(UI.ROXO_ESCURO);
        perfil.setBorder(new EmptyBorder(8, 12, 8, 12));
        topo.add(perfil, BorderLayout.EAST);
        conteudo.add(topo, BorderLayout.NORTH);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 0));
        cards.setOpaque(false);
        cards.add(UI.card("Acesso rápido", "Loja", "▣"));
        cards.add(UI.card("Controle", "Estoque", "□"));
        cards.add(UI.card("Usuário", usuarioLogado.isAdmin() ? "Administrador" : "Cliente", "♙"));
        centro.add(cards);
        centro.add(Box.createVerticalStrut(22));

        JPanel boasVindas = UI.painelArredondado(22);
        boasVindas.setBorder(new EmptyBorder(20, 22, 20, 22));
        JPanel imagem = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon icon = UI.imagemIgreja(getWidth(), getHeight());
                if (icon != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 18, 18));
                    g2.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                    g2.setColor(new Color(52, 34, 78, 75));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            }
        };
        imagem.setPreferredSize(new Dimension(230, 145));
        JLabel cruz = new JLabel("✝", SwingConstants.CENTER);
        cruz.setForeground(Color.WHITE);
        cruz.setFont(new Font("Serif", Font.PLAIN, 48));
        imagem.add(cruz, BorderLayout.CENTER);
        boasVindas.add(imagem, BorderLayout.WEST);

        JPanel texto = new JPanel();
        texto.setOpaque(false);
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Track Store");
        t.setFont(new Font("Segoe UI", Font.BOLD, 25));
        t.setForeground(UI.ROXO_ESCURO);
        JLabel s = new JLabel("Sistema de estoque e vendas da igreja");
        s.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        s.setForeground(UI.TEXTO_SECUNDARIO);
        JLabel dica = new JLabel("Use o menu ao lado para navegar pelas funções do sistema.");
        dica.setForeground(UI.TEXTO_SECUNDARIO);
        texto.add(t);
        texto.add(Box.createVerticalStrut(5));
        texto.add(s);
        texto.add(Box.createVerticalStrut(14));
        texto.add(dica);
        boasVindas.add(texto, BorderLayout.CENTER);
        centro.add(boasVindas);
        conteudo.add(centro, BorderLayout.CENTER);

        raiz.add(menu, BorderLayout.WEST);
        raiz.add(conteudo, BorderLayout.CENTER);
        add(raiz);
    }

    private void adicionarMenu(JPanel menu, String texto, java.awt.event.ActionListener acao) {
        JButton b = menuBotao(texto);
        b.addActionListener(acao);
        menu.add(b);
        menu.add(Box.createVerticalStrut(6));
    }

    private JButton menuBotao(String texto) {
        JButton b = new JButton(texto);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setForeground(new Color(241, 237, 247));
        b.setBackground(UI.ROXO);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(new EmptyBorder(9, 12, 9, 8));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addChangeListener(e -> b.setBackground(b.getModel().isRollover() ? UI.ROXO_CLARO : UI.ROXO));
        return b;
    }
}
