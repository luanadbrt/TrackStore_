package com.trackstore.dao;

import com.trackstore.model.Produto;
import jakarta.persistence.LockModeType;
import org.hibernate.Session;

import java.util.List;

/**
 * Acesso a dados de Produto. Só persistência: nenhuma regra de negócio e nenhum
 * controle de transação (quem abre a transação é o service, via HibernateUtil).
 * A Session sempre chega de fora — é isso que permite que a venda valide o
 * estoque e grave o pedido dentro da MESMA transação.
 */
public class ProdutoDAO {

    public void salvar(Session session, Produto produto) {
        session.persist(produto);
    }

    public Produto atualizar(Session session, Produto produto) {
        return session.merge(produto);
    }

    public Produto buscarPorId(Session session, int id) {
        return session.get(Produto.class, id);
    }

    /**
     * Busca o produto travando a linha até o fim da transação (SELECT ... FOR UPDATE).
     *
     * É o que impede a venda concorrente: se dois usuários comprarem o último item
     * ao mesmo tempo, o segundo fica esperando o primeiro terminar e então enxerga
     * o saldo já debitado, em vez de os dois lerem "1 disponível" e ambos venderem.
     */
    public Produto buscarPorIdComLock(Session session, int id) {
        return session.find(Produto.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    public Produto buscarPorCodigo(Session session, int codigo) {
        return session.createQuery("FROM Produto WHERE codigo = :codigo", Produto.class)
                .setParameter("codigo", codigo)
                .uniqueResultOptional()
                .orElse(null);
    }

    public Produto buscarPorNomeExato(Session session, String nome) {
        return session.createQuery("FROM Produto WHERE lower(nome) = :nome", Produto.class)
                .setParameter("nome", nome.trim().toLowerCase())
                .uniqueResultOptional()
                .orElse(null);
    }

    public List<Produto> listarTodos(Session session) {
        return session.createQuery("FROM Produto ORDER BY nome", Produto.class).list();
    }

    /** Produtos que a loja pode vender: ativos e com saldo. */
    public List<Produto> listarDisponiveis(Session session, String trechoNome) {
        String hql = "FROM Produto WHERE situacao = com.trackstore.model.Produto$Situacao.ATIVO"
                + " AND quantidade > 0";
        if (trechoNome != null && !trechoNome.isBlank()) {
            hql += " AND lower(nome) LIKE :trecho";
        }
        hql += " ORDER BY nome";

        var query = session.createQuery(hql, Produto.class);
        if (trechoNome != null && !trechoNome.isBlank()) {
            query.setParameter("trecho", "%" + trechoNome.trim().toLowerCase() + "%");
        }
        return query.list();
    }

    /**
     * Filtro da tela de Estoque. Os dois critérios são opcionais e ambos são
     * aplicados NO BANCO — a versão anterior trazia a tabela inteira para a memória
     * e filtrava o código com removeIf em Java.
     */
    public List<Produto> filtrar(Session session, String filtroCodigo, String filtroNome,
                                 boolean somenteAtivos) {
        StringBuilder hql = new StringBuilder("FROM Produto WHERE 1 = 1");

        boolean temCodigo = filtroCodigo != null && !filtroCodigo.isBlank();
        boolean temNome = filtroNome != null && !filtroNome.isBlank();

        if (temCodigo) {
            hql.append(" AND cast(codigo as String) LIKE :codigo");
        }
        if (temNome) {
            hql.append(" AND lower(nome) LIKE :nome");
        }
        if (somenteAtivos) {
            hql.append(" AND situacao = com.trackstore.model.Produto$Situacao.ATIVO");
        }
        hql.append(" ORDER BY nome");

        var query = session.createQuery(hql.toString(), Produto.class);
        if (temCodigo) {
            query.setParameter("codigo", "%" + filtroCodigo.trim() + "%");
        }
        if (temNome) {
            query.setParameter("nome", "%" + filtroNome.trim().toLowerCase() + "%");
        }
        return query.list();
    }

    /** Quantos itens de pedido referenciam este produto (decide se pode ser apagado de vez). */
    public long contarVendas(Session session, int produtoId) {
        return session.createQuery(
                        "SELECT count(i) FROM ItemPedido i WHERE i.produto.id = :id", Long.class)
                .setParameter("id", produtoId)
                .getSingleResult();
    }

    public long contarReposicoes(Session session, int produtoId) {
        return session.createQuery(
                        "SELECT count(r) FROM Reposicao r WHERE r.produto.id = :id", Long.class)
                .setParameter("id", produtoId)
                .getSingleResult();
    }

    public void remover(Session session, Produto produto) {
        session.remove(produto);
    }
}
