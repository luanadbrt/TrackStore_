package com.trackstore.dao;

import com.trackstore.util.ValidacaoException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Cria e mantém uma única SessionFactory para toda a aplicação e centraliza o
 * controle de transações.
 *
 * Duas responsabilidades importantes vivem aqui:
 *
 * 1. CREDENCIAIS FORA DO CÓDIGO. url/usuário/senha não estão no hibernate.cfg.xml
 *    (que é versionado e vai dentro do .jar). São lidos de variáveis de ambiente
 *    ou de db.properties, que fica fora do controle de versão.
 *
 * 2. TRANSAÇÃO COM ROLLBACK GARANTIDO. O método emTransacao() abre a transação,
 *    faz commit no sucesso e rollback em QUALQUER falha. Antes cada DAO repetia
 *    esse bloco na mão — e o PedidoDAO esquecia o rollback, deixando a chance de
 *    gravar um pedido sem dar baixa no estoque.
 */
public final class HibernateUtil {

    private static final String CONFIG_PADRAO = "/hibernate.cfg.xml";

    private static SessionFactory sessionFactory;
    private static String recursoConfig = CONFIG_PADRAO;

    private HibernateUtil() {
    }

    /** Usado pelos testes para apontar para o banco H2 em memória. */
    public static synchronized void usarConfiguracao(String recurso) {
        encerrar();
        recursoConfig = recurso;
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = construir();
        }
        return sessionFactory;
    }

    private static SessionFactory construir() {
        try {
            Configuration cfg = new Configuration().configure(recursoConfig);

            // Em produção a conexão vem de fora; nos testes o próprio cfg já traz o H2.
            if (CONFIG_PADRAO.equals(recursoConfig)) {
                aplicarCredenciais(cfg);
            }

            return cfg.buildSessionFactory();

        } catch (Throwable ex) {
            System.err.println();
            System.err.println("=== Falha ao inicializar o Hibernate ===");
            System.err.println(ex.getMessage());
            System.err.println();
            System.err.println("Causas mais comuns:");
            System.err.println("  * MySQL não está rodando, ou url/usuário/senha errados.");
            System.err.println("    -> confira o db.properties (modelo em db.properties.example)");
            System.err.println("  * O banco existe mas as tabelas não: rode database/script.sql.");
            System.err.println("  * O schema está diferente das entidades (hbm2ddl.auto=validate");
            System.err.println("    acusa isso de propósito) -> rode database/script.sql de novo.");
            System.err.println();
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Ordem de precedência: variáveis de ambiente, depois db.properties.
     * Falha com mensagem explícita se não achar nenhuma das duas — melhor do que
     * tentar conectar com um valor default errado e dar um erro confuso depois.
     */
    private static void aplicarCredenciais(Configuration cfg) {
        Properties arquivo = lerDbProperties();

        String url = valor("TRACKSTORE_DB_URL", arquivo, "db.url");
        String usuario = valor("TRACKSTORE_DB_USER", arquivo, "db.user");
        String senha = valor("TRACKSTORE_DB_PASS", arquivo, "db.password");

        if (url == null || usuario == null || senha == null) {
            throw new IllegalStateException(
                    "Conexão com o banco não configurada.\n"
                            + "Copie db.properties.example para db.properties e preencha db.url, "
                            + "db.user e db.password\n"
                            + "(ou defina TRACKSTORE_DB_URL, TRACKSTORE_DB_USER e TRACKSTORE_DB_PASS).");
        }

        cfg.setProperty("hibernate.connection.url", url);
        cfg.setProperty("hibernate.connection.username", usuario);
        cfg.setProperty("hibernate.connection.password", senha);
    }

    private static String valor(String variavelAmbiente, Properties arquivo, String chave) {
        String doAmbiente = System.getenv(variavelAmbiente);
        if (doAmbiente != null && !doAmbiente.isBlank()) {
            return doAmbiente;
        }
        String doArquivo = arquivo.getProperty(chave);
        return (doArquivo == null || doArquivo.isBlank()) ? null : doArquivo;
    }

    /** Procura db.properties na pasta de execução e, se não achar, no classpath. */
    private static Properties lerDbProperties() {
        Properties props = new Properties();

        Path local = Path.of("db.properties");
        if (Files.isReadable(local)) {
            try (InputStream in = Files.newInputStream(local)) {
                props.load(in);
                return props;
            } catch (IOException e) {
                System.err.println("Não foi possível ler db.properties: " + e.getMessage());
            }
        }

        try (InputStream in = HibernateUtil.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // sem db.properties: as variáveis de ambiente ainda podem resolver
        }
        return props;
    }

    // ===================== controle de transação =====================

    /** Bloco de código que roda dentro de uma transação e devolve um resultado. */
    @FunctionalInterface
    public interface Operacao<T> {
        T executar(Session session) throws ValidacaoException;
    }

    /** Bloco de código transacional que não devolve nada. */
    @FunctionalInterface
    public interface Comando {
        void executar(Session session) throws ValidacaoException;
    }

    /**
     * Executa a operação dentro de uma transação: commit no sucesso, rollback em
     * qualquer exceção — inclusive nas de validação, como estoque insuficiente.
     */
    public static <T> T emTransacao(Operacao<T> operacao) throws ValidacaoException {
        try (Session session = getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T resultado = operacao.executar(session);
                tx.commit();
                return resultado;
            } catch (RuntimeException | ValidacaoException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    public static void emTransacao(Comando comando) throws ValidacaoException {
        emTransacao(session -> {
            comando.executar(session);
            return null;
        });
    }

    /** Consulta somente leitura: abre e fecha a sessão, sem transação de escrita. */
    public static <T> T consultando(java.util.function.Function<Session, T> consulta) {
        try (Session session = getSessionFactory().openSession()) {
            return consulta.apply(session);
        }
    }

    public static synchronized void encerrar() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        sessionFactory = null;
    }
}
