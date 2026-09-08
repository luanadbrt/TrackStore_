package com.trackstore;

import com.trackstore.dao.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base dos testes que tocam o banco.
 *
 * Aponta o HibernateUtil para o H2 em memória (hibernate-test.cfg.xml) e limpa as
 * tabelas antes de cada teste, para que um teste nunca dependa do outro.
 */
public abstract class BaseTesteBanco {

    @BeforeAll
    static void configurarBancoDeTeste() {
        HibernateUtil.usarConfiguracao("/hibernate-test.cfg.xml");
    }

    @AfterAll
    static void encerrarBancoDeTeste() {
        HibernateUtil.encerrar();
    }

    @BeforeEach
    void limparTabelas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            // ordem importa: filhos antes dos pais, por causa das chaves estrangeiras
            session.createMutationQuery("delete from ItemPedido").executeUpdate();
            session.createMutationQuery("delete from Pedido").executeUpdate();
            session.createMutationQuery("delete from Reposicao").executeUpdate();
            session.createMutationQuery("delete from Produto").executeUpdate();
            session.createMutationQuery("delete from Fornecedor").executeUpdate();
            session.createMutationQuery("delete from Usuario").executeUpdate();
            tx.commit();
        }
    }
}
