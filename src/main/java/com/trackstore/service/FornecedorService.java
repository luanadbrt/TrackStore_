package com.trackstore.service;

import com.trackstore.dao.FornecedorDAO;
import com.trackstore.dao.HibernateUtil;
import com.trackstore.model.Fornecedor;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import java.util.List;

/** CRUD de fornecedor. Antes não existia: só dava para cadastrar por INSERT no MySQL. */
public class FornecedorService {

    private final FornecedorDAO fornecedorDAO = new FornecedorDAO();

    public Fornecedor cadastrar(String nome) throws ValidacaoException {
        validarNome(nome);

        return HibernateUtil.emTransacao(session -> {
            if (fornecedorDAO.buscarPorNomeExato(session, nome) != null) {
                throw new ValidacaoException(Mensagens.NOME_FORNECEDOR_DUPLICADO);
            }
            Fornecedor fornecedor = new Fornecedor(nome.trim());
            fornecedorDAO.salvar(session, fornecedor);
            return fornecedor;
        });
    }

    public Fornecedor atualizar(int id, String nome) throws ValidacaoException {
        validarNome(nome);

        return HibernateUtil.emTransacao(session -> {
            Fornecedor fornecedor = fornecedorDAO.buscarPorId(session, id);
            if (fornecedor == null) {
                throw new ValidacaoException(Mensagens.FORNECEDOR_NAO_ENCONTRADO);
            }
            Fornecedor existente = fornecedorDAO.buscarPorNomeExato(session, nome);
            if (existente != null && existente.getId() != id) {
                throw new ValidacaoException(Mensagens.NOME_FORNECEDOR_DUPLICADO);
            }
            fornecedor.setNome(nome.trim());
            return fornecedor;
        });
    }

    /** Só exclui fornecedor sem histórico — apagar quebraria a FK de reposicoes. */
    public void excluir(int id) throws ValidacaoException {
        HibernateUtil.emTransacao(session -> {
            Fornecedor fornecedor = fornecedorDAO.buscarPorId(session, id);
            if (fornecedor == null) {
                throw new ValidacaoException(Mensagens.FORNECEDOR_NAO_ENCONTRADO);
            }
            if (fornecedorDAO.contarReposicoes(session, id) > 0) {
                throw new ValidacaoException(Mensagens.FORNECEDOR_COM_REPOSICOES);
            }
            fornecedorDAO.remover(session, fornecedor);
        });
    }

    public List<Fornecedor> listarTodos() {
        return HibernateUtil.consultando(fornecedorDAO::listarTodos);
    }

    private void validarNome(String nome) throws ValidacaoException {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException(Mensagens.CAMPOS_OBRIGATORIOS);
        }
    }
}
