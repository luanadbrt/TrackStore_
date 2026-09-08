package com.trackstore.dao;

import com.trackstore.model.Pedido;
import org.hibernate.Session;

import java.util.List;

/** Acesso a dados de Pedido. A regra de baixa de estoque está no PedidoService. */
public class PedidoDAO {

    public void salvar(Session session, Pedido pedido) {
        session.persist(pedido);
    }

    public Pedido buscarPorId(Session session, int id) {
        return session.get(Pedido.class, id);
    }

    public List<Pedido> listarPorUsuario(Session session, int usuarioId) {
        return session.createQuery(
                        "FROM Pedido WHERE usuario.id = :id ORDER BY dataPedido DESC", Pedido.class)
                .setParameter("id", usuarioId)
                .list();
    }

    public List<Pedido> listarTodos(Session session) {
        return session.createQuery("FROM Pedido ORDER BY dataPedido DESC", Pedido.class).list();
    }
}
