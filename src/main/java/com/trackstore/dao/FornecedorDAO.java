package com.trackstore.dao;

import com.trackstore.model.Fornecedor;
import org.hibernate.Session;

import java.util.List;

/**
 * Acesso a dados de Fornecedor.
 *
 * Antes esta classe só tinha listarTodos(): não existia nenhuma forma de cadastrar
 * fornecedor pelo sistema, e o README mandava rodar um INSERT no MySQL na mão.
 */
public class FornecedorDAO {

    public void salvar(Session session, Fornecedor fornecedor) {
        session.persist(fornecedor);
    }

    public Fornecedor atualizar(Session session, Fornecedor fornecedor) {
        return session.merge(fornecedor);
    }

    public void remover(Session session, Fornecedor fornecedor) {
        session.remove(fornecedor);
    }

    public Fornecedor buscarPorId(Session session, int id) {
        return session.get(Fornecedor.class, id);
    }

    public Fornecedor buscarPorNomeExato(Session session, String nome) {
        return session.createQuery("FROM Fornecedor WHERE lower(nome) = :nome", Fornecedor.class)
                .setParameter("nome", nome.trim().toLowerCase())
                .uniqueResultOptional()
                .orElse(null);
    }

    public List<Fornecedor> listarTodos(Session session) {
        return session.createQuery("FROM Fornecedor ORDER BY nome", Fornecedor.class).list();
    }

    public long contarReposicoes(Session session, int fornecedorId) {
        return session.createQuery(
                        "SELECT count(r) FROM Reposicao r WHERE r.fornecedor.id = :id", Long.class)
                .setParameter("id", fornecedorId)
                .getSingleResult();
    }
}
