package com.trackstore.view;

import com.trackstore.util.Mensagens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Tema visual centralizado do Track Store.
 * Mantém a lógica das telas intacta e concentra cores, botões, tabelas e diálogos.
 */
public final class UI {

    private UI() {}

    public static final Color ROXO = new Color(106, 67, 153);
    public static final Color ROXO_ESCURO = new Color(52, 34, 78);
    public static final Color ROXO_CLARO = new Color(143, 104, 190);
    public static final Color DOURADO = new Color(196, 153, 66);
    public static final Color AZUL = new Color(65, 91, 145);
    public static final Color FUNDO = new Color(247, 245, 250);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXTO = new Color(39, 42, 50);
    public static final Color TEXTO_SECUNDARIO = new Color(103, 108, 120);
    public static final Color BORDA = new Color(225, 227, 234);
    public static final Color VERDE = new Color(38, 142, 92);
    public static final Color VERMELHO = new Color(196, 63, 73);
    public static final Color AMARELO = new Color(205, 145, 25);

    public static final Font FONTE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 22);

    /** Imagem de referência encontrada no Unsplash; há fallback caso não exista internet. */
    private static final String IMAGEM_IGREJA =
            "https://images.unsplash.com/photo-1577648911623-4e87ce711adb?auto=format&fit=crop&fm=jpg&q=82&w=1600";

    public static void prepararLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Label.font", FONTE);
        UIManager.put("Button.font", FONTE);
        UIManager.put("TextField.font", FONTE);
        UIManager.put("PasswordField.font", FONTE);
        UIManager.put("ComboBox.font", FONTE);
        UIManager.put("Table.font", FONTE);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
    }

    public static void aplicarTema(Container raiz) {
        raiz.setBackground(FUNDO);
        estilizarRecursivo(raiz);
    }

    private static void estilizarRecursivo(Component c) {
        if (c instanceof JPanel p) {
            if (p.getBackground().equals(UIManager.getColor("Panel.background")) || p.getBackground() == null) {
                p.setBackground(FUNDO);
            }
        }
        if (c instanceof JLabel l) {
            l.setForeground(TEXTO);
        }
        if (c instanceof JTextField f) {
            estilizarCampo(f);
        }
        if (c instanceof JPasswordField f) {
            estilizarCampo(f);
        }
        if (c instanceof JComboBox<?> combo) {
            combo.setBorder(BorderFactory.createLineBorder(BORDA));
            combo.setBackground(Color.WHITE);
        }
        if (c instanceof JSpinner spinner) {
            spinner.setBorder(BorderFactory.createLineBorder(BORDA));
            spinner.setBackground(Color.WHITE);
        }
        if (c instanceof JButton b && !(b instanceof RoundedButton)) {
            estilizarBotao(b, ROXO);
        }
        if (c instanceof JTable t) {
            estilizarTabela(t);
        }
        if (c instanceof Container container) {
            for (Component filho : container.getComponents()) {
                estilizarRecursivo(filho);
            }
        }
    }

    public static void estilizarCampo(JTextField campo) {
        campo.setBackground(Color.WHITE);
        campo.setForeground(TEXTO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                new EmptyBorder(8, 11, 8, 11)));
        campo.setCaretColor(ROXO);
    }

    public static JButton botao(String texto) {
        return botao(texto, ROXO);
    }

    public static JButton botao(String texto, Color cor) {
        RoundedButton b = new RoundedButton(texto);
        estilizarBotao(b, cor);
        return b;
    }

    public static void estilizarBotao(JButton b, Color cor) {
        b.setBackground(cor);
        if (b instanceof RoundedButton rb) rb.corBase = cor;
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        // JButton comum precisa ser opaco para a cor roxa aparecer.
        // RoundedButton continua fazendo seu próprio desenho arredondado.
        b.setOpaque(!(b instanceof RoundedButton));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(10, 18, 10, 18));
        if (b.getFont() != null) b.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    public static JPanel painelBotoes(JButton... botoes) {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 9));
        painel.setOpaque(false);
        for (JButton botao : botoes) {
            if (!(botao instanceof RoundedButton)) estilizarBotao(botao, ROXO);
            painel.add(botao);
        }
        return painel;
    }

    public static JTable tabelaSomenteLeitura(DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        estilizarTabela(tabela);
        return tabela;
    }

    public static void estilizarTabela(JTable tabela) {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(32);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(BORDA);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.setFillsViewportHeight(true);
        tabela.setBackground(Color.WHITE);
        tabela.setForeground(TEXTO);
        tabela.setSelectionBackground(new Color(235, 227, 245));
        tabela.setSelectionForeground(TEXTO);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 34));
        tabela.getTableHeader().setBackground(ROXO_ESCURO);
        tabela.getTableHeader().setForeground(Color.WHITE);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabela.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 248, 252));
                c.setForeground(TEXTO);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    public static JPanel painelArredondado(int raio) {
        return new RoundedPanel(raio, CARD);
    }

    public static JPanel card(String titulo, String valor, String icone) {
        JPanel card = new RoundedPanel(18, CARD);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setLayout(new BorderLayout(10, 4));
        JLabel ic = new JLabel(icone, SwingConstants.CENTER);
        ic.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        ic.setForeground(ROXO);
        ic.setPreferredSize(new Dimension(45, 45));
        card.add(ic, BorderLayout.WEST);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel lTitulo = new JLabel(titulo);
        lTitulo.setForeground(TEXTO_SECUNDARIO);
        lTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel lValor = new JLabel(valor);
        lValor.setForeground(TEXTO);
        lValor.setFont(new Font("Segoe UI", Font.BOLD, 20));
        textos.add(lTitulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(lValor);
        card.add(textos, BorderLayout.CENTER);
        return card;
    }

    public static JPanel cabecalho(String titulo, String subtitulo) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(titulo);
        t.setFont(FONTE_TITULO);
        t.setForeground(TEXTO);
        JLabel s = new JLabel(subtitulo);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setForeground(TEXTO_SECUNDARIO);
        p.add(t);
        p.add(Box.createVerticalStrut(3));
        p.add(s);
        return p;
    }

    public static ImageIcon imagemIgreja(int largura, int altura) {
        try {
            ImageIcon original = new ImageIcon(new URL(IMAGEM_IGREJA));
            Image imagem = original.getImage();
            return new ImageIcon(imagem.getScaledInstance(largura, altura, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return imagemLocal(largura, altura);
        }
    }

    private static ImageIcon imagemLocal(int largura, int altura) {
        try {
            URL url = UI.class.getResource("/church-fallback.png");
            if (url == null) return null;
            ImageIcon original = new ImageIcon(url);
            return new ImageIcon(original.getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }

    public static void erro(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void erroBanco(Component pai, Throwable causa) {
        JOptionPane.showMessageDialog(pai, Mensagens.ERRO_BANCO + causa.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void aviso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public static void sucesso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component pai, String titulo, Object conteudo) {
        return JOptionPane.showConfirmDialog(pai, conteudo, titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static <T> void carregar(Component pai, Supplier<T> consulta, Consumer<T> aoConcluir) {
        Window janela = SwingUtilities.getWindowAncestor(pai);
        if (janela != null) janela.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() { return consulta.get(); }
            @Override protected void done() {
                if (janela != null) janela.setCursor(Cursor.getDefaultCursor());
                try { aoConcluir.accept(get()); }
                catch (Exception e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    erroBanco(pai, causa);
                }
            }
        }.execute();
    }

    /** Botão com cantos arredondados e efeito de hover. */
    private static class RoundedButton extends JButton {
        private Color corBase = ROXO;
        RoundedButton(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color cor = getModel().isRollover() ? corBase.brighter() : getBackground();
            g2.setColor(cor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int raio;
        private final Color cor;
        RoundedPanel(int raio, Color cor) { this.raio = raio; this.cor = cor; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, raio, raio));
            g2.setColor(BORDA);
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, raio, raio));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
