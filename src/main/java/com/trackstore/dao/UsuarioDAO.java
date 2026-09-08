package com.trackstore.dao;

import com.trackstore.model.Usuario;
import org.hibernate.Session;

import java.util.List;

/** Acesso a dados de Usuario. Só persistência — as regras estão no UsuarioService. */
public class UsuarioDAO {

    public void salvar(Session session, Usuario usuario) {
        session.persist(usuario);
    }

    public Usuario atualizar(Session session, Usuario usuario) {
        return session.merge(usuario);
    }

    public void remover(Session session, Usuario usuario) {
        session.remove(usuario);
    }

    public Usuario buscarPorId(Session session, int id) {
        return session.get(Usuario.class, id);
    }

    public Usuario buscarPorEmail(Session session, String email) {
        return session.createQuery("FROM Usuario WHERE lower(email) = :email", Usuario.class)
                .setParameter("email", email.trim().toLowerCase())
                .uniqueResultOptional()
                .orElse(null);
    }

    public Usuario buscarPorCpf(Session session, String cpf) {
        return session.createQuery("FROM Usuario WHERE cpf = :cpf", Usuario.class)
                .setParameter("cpf", cpf.trim())
                .uniqueResultOptional()
                .orElse(null);
    }

    public List<Usuario> listarTodos(Session session) {
        return session.createQuery("FROM Usuario ORDER BY nome", Usuario.class).list();
    }

    public long contar(Session session) {
        return session.createQuery("SELECT count(u) FROM Usuario u", Long.class).getSingleResult();
    }

    public long contarPedidos(Session session, int usuarioId) {
        return session.createQuery(
                        "SELECT count(p) FROM Pedido p WHERE p.usuario.id = :id", Long.class)
                .setParameter("id", usuarioId)
                .getSingleResult();
    }
}
