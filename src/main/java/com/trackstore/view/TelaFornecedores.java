package com.trackstore.view;

import com.trackstore.model.Fornecedor;
import com.trackstore.service.FornecedorService;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD de fornecedores.
 *
 * Tela nova. Antes não havia nenhuma forma de cadastrar fornecedor pelo sistema:
 * o README instruía a rodar um INSERT direto no MySQL — e sem fornecedor a tela de
 * reposição de estoque simplesmente não funcionava.
 */
public class TelaFornecedores extends JFrame {

    private DefaultTableModel modelo;
    private JTable tabela;

    private final FornecedorService fornecedorService = new FornecedorService();
    private final List<Fornecedor> exibidos = new ArrayList<>();

    public TelaFornecedores() {
        setTitle("Track Store - Fornecedores");
        setSize(520, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        montarTela();
        carregarTabela();
    }

    private void montarTela() {
        modelo = new DefaultTableModel(new Object[]{"Código", "Nome"}, 0);
        tabela = UI.tabelaSomenteLeitura(modelo);
        tabela.getColumnModel().getColumn(0).setMaxWidth(80);

        JButton btnNovo = new JButton("Novo");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");

        setLayout(new BorderLayout());
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(UI.painelBotoes(btnNovo, btnEditar, btnExcluir), BorderLayout.SOUTH);

        btnNovo.addActionListener(e -> novo());
        btnEditar.addActionListener(e -> editar());
        btnExcluir.addActionListener(e -> excluir());
        UI.aplicarTema(this);
    }

    private void carregarTabela() {
        UI.carregar(this, fornecedorService::listarTodos, lista -> {
            exibidos.clear();
            exibidos.addAll(lista);
            modelo.setRowCount(0);
            for (Fornecedor f : lista) {
                modelo.addRow(new Object[]{f.getId(), f.getNome()});
            }
        });
    }

    private Fornecedor selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || linha >= exibidos.size()) {
            UI.aviso(this, Mensagens.SELECIONE_FORNECEDOR);
            return null;
        }
        return exibidos.get(linha);
    }

    private void novo() {
        String nome = JOptionPane.showInputDialog(this, "Nome do fornecedor:",
                "Novo fornecedor", JOptionPane.PLAIN_MESSAGE);
        if (nome == null) {
            return;
        }
        try {
            fornecedorService.cadastrar(nome);
            UI.sucesso(this, Mensagens.FORNECEDOR_SALVO);
            carregarTabela();
        } catch (ValidacaoException ex) {
            UI.aviso(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
        }
    }

    private void editar() {
        Fornecedor fornecedor = selecionado();
        if (fornecedor == null) {
            return;
        }
        String nome = (String) JOptionPane.showInputDialog(this, "Nome do fornecedor:",
                "Editar fornecedor", JOptionPane.PLAIN_MESSAGE, null, null, fornecedor.getNome());
        if (nome == null) {
            return;
        }
        try {
            fornecedorService.atualizar(fornecedor.getId(), nome);
            UI.sucesso(this, Mensagens.FORNECEDOR_SALVO);
            carregarTabela();
        } catch (ValidacaoException ex) {
            UI.aviso(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
        }
    }

    private void excluir() {
        Fornecedor fornecedor = selecionado();
        if (fornecedor == null) {
            return;
        }
        if (!UI.confirmar(this, "Confirmar exclusão",
                "Excluir o fornecedor \"" + fornecedor.getNome() + "\"?")) {
            return;
        }
        try {
            fornecedorService.excluir(fornecedor.getId());
            UI.sucesso(this, Mensagens.FORNECEDOR_EXCLUIDO);
            carregarTabela();
        } catch (ValidacaoException ex) {
            UI.aviso(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UI.erroBanco(this, ex);
        }
    }
}
