package com.trackstore.view;

import com.trackstore.model.Usuario;
import com.trackstore.service.UsuarioService;
import com.trackstore.util.CpfUtil;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestão de usuários: listar, criar, editar e excluir. Somente admin.
 *
 * Tela nova. Também substitui o passo manual descrito no README antigo, que mandava
 * promover administradores rodando "UPDATE usuarios SET tipo='ADMIN'" no MySQL.
 */
public class TelaGestaoUsuarios extends JFrame {

    private DefaultTableModel modelo;
    private JTable tabela;

    private final UsuarioService usuarioService = new UsuarioService();
    private final List<Usuario> usuariosExibidos = new ArrayList<>();
    private final Usuario usuarioLogado;

    public TelaGestaoUsuarios(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        setTitle("Track Store - Gestão de Usuários");
        setSize(700, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        montarTela();
        carregarTabela();
    }

    private void montarTela() {
        modelo = new DefaultTableModel(new Object[]{"Nome", "E-mail", "CPF", "Tipo"}, 0);
        tabela = UI.tabelaSomenteLeitura(modelo);

        JButton btnNovo = new JButton("Novo");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnAtualizar = new JButton("Atualizar lista");

        setLayout(new BorderLayout());
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(UI.painelBotoes(btnNovo, btnEditar, btnExcluir, btnAtualizar), BorderLayout.SOUTH);

        btnNovo.addActionListener(e -> novo());
        btnEditar.addActionListener(e -> editar());
        btnExcluir.addActionListener(e -> excluir());
        btnAtualizar.addActionListener(e -> carregarTabela());
        UI.aplicarTema(this);
    }

    private void carregarTabela() {
        UI.carregar(this, usuarioService::listarTodos, lista -> {
            usuariosExibidos.clear();
            usuariosExibidos.addAll(lista);
            modelo.setRowCount(0);
            for (Usuario u : lista) {
                modelo.addRow(new Object[]{
                        u.getNome(),
                        u.getEmail(),
                        CpfUtil.formatar(u.getCpf()),
                        u.getTipo()
                });
            }
        });
    }

    private Usuario selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || linha >= usuariosExibidos.size()) {
            UI.aviso(this, "Selecione um usuário na lista.");
            return null;
        }
        return usuariosExibidos.get(tabela.convertRowIndexToModel(linha));
    }

    private void novo() {
        TelaCadastroUsuario dialogo = TelaCadastroUsuario.paraNovoPeloAdmin(this);
        dialogo.setVisible(true);
        if (dialogo.isSalvou()) {
            carregarTabela();
        }
    }

    private void editar() {
        Usuario usuario = selecionado();
        if (usuario == null) {
            return;
        }
        TelaCadastroUsuario dialogo = TelaCadastroUsuario.paraEdicao(this, usuario);
        dialogo.setVisible(true);
        if (dialogo.isSalvou()) {
            carregarTabela();
        }
    }

    private void excluir() {
        Usuario usuario = selecionado();
        if (usuario == null) {
            return;
        }

        // Sem isso o admin poderia apagar a própria conta e ficar fora do sistema.
        if (usuario.getId() == usuarioLogado.getId()) {
            UI.aviso(this, "Você não pode excluir o usuário com o qual está logado.");
            return;
        }

        if (!UI.confirmar(this, "Confirmar exclusão",
                "Excluir o usuário \"" + usuario.getNome() + "\"?")) {
            return;
        }

        try {
            usuarioService.excluir(usuario.getId());
            UI.sucesso(this, Mensagens.USUARIO_EXCLUIDO);
            carregarTabela();
        } catch (ValidacaoException ex) {
            UI.aviso(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
        }
    }
}
