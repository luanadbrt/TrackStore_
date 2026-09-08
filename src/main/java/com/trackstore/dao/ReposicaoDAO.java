package com.trackstore.dao;

import com.trackstore.model.Reposicao;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acesso a dados de Reposicao.
 *
 * Esta classe não existia: a reposição era gravada dentro do ProdutoDAO e nunca
 * era lida por ninguém — a tabela acumulava histórico invisível para o usuário.
 * Agora o histórico é consultável (veja TelaHistoricoReposicao).
 */
public class ReposicaoDAO {

    public void salvar(Session session, Reposicao reposicao) {
        session.persist(reposicao);
    }

    public Reposicao buscarPorId(Session session, int id) {
        return session.get(Reposicao.class, id);
    }

    public List<Reposicao> listarTodas(Session session) {
        return session.createQuery(
                "FROM Reposicao ORDER BY dataReposicao DESC", Reposicao.class).list();
    }

    public List<Reposicao> listarPorProduto(Session session, int produtoId) {
        return session.createQuery(
                        "FROM Reposicao WHERE produto.id = :id ORDER BY dataReposicao DESC",
                        Reposicao.class)
                .setParameter("id", produtoId)
                .list();
    }

    /** Filtro do histórico: produto e período, ambos opcionais. */
    public List<Reposicao> filtrar(Session session, Integer produtoId,
                                   LocalDateTime inicio, LocalDateTime fim) {
        StringBuilder hql = new StringBuilder("FROM Reposicao WHERE 1 = 1");
        if (produtoId != null) {
            hql.append(" AND produto.id = :produtoId");
        }
        if (inicio != null) {
            hql.append(" AND dataReposicao >= :inicio");
        }
        if (fim != null) {
            hql.append(" AND dataReposicao <= :fim");
        }
        hql.append(" ORDER BY dataReposicao DESC");

        var query = session.createQuery(hql.toString(), Reposicao.class);
        if (produtoId != null) {
            query.setParameter("produtoId", produtoId);
        }
        if (inicio != null) {
            query.setParameter("inicio", inicio);
        }
        if (fim != null) {
            query.setParameter("fim", fim);
        }
        return query.list();
    }

    public long contarPorProduto(Session session, int produtoId) {
        return session.createQuery(
                        "SELECT count(r) FROM Reposicao r WHERE r.produto.id = :id", Long.class)
                .setParameter("id", produtoId)
                .getSingleResult();
    }
}
