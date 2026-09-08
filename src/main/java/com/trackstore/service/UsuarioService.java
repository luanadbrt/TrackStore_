package com.trackstore.service;

import com.trackstore.dao.HibernateUtil;
import com.trackstore.dao.UsuarioDAO;
import com.trackstore.model.Usuario;
import com.trackstore.util.CpfUtil;
import com.trackstore.util.Mensagens;
import com.trackstore.util.SenhaUtil;
import com.trackstore.util.ValidacaoException;

import java.util.List;
import java.util.regex.Pattern;

/** Regras de negócio de usuário: cadastro, autenticação e CRUD. */
public class UsuarioService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$");
    private static final int SENHA_MINIMA = 6;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // ===================== autenticação =====================

    public enum Resultado { OK, USUARIO_INEXISTENTE, SENHA_INCORRETA }

    public record Login(Resultado resultado, Usuario usuario) {
        public boolean autenticado() {
            return resultado == Resultado.OK;
        }
    }

    /**
     * Autentica com UMA única consulta.
     *
     * Antes a tela chamava buscarPorEmail() e, logo depois, autenticar() — que
     * internamente chamava buscarPorEmail() de novo: duas queries e duas sessões
     * abertas para um único login.
     *
     * Observação de segurança: distinguir "usuário não existe" de "senha incorreta"
     * permite descobrir quais e-mails estão cadastrados (enumeração de usuários).
     * Em sistema real usa-se a mensagem única CREDENCIAIS_INVALIDAS. Aqui o Resultado
     * detalhado é devolvido porque a especificação do trabalho pede as duas mensagens
     * separadas — a decisão fica na tela, não aqui.
     */
    public Login autenticar(String email, String senha) throws ValidacaoException {
        if (email == null || email.isBlank() || senha == null || senha.isEmpty()) {
            throw new ValidacaoException(Mensagens.CAMPOS_OBRIGATORIOS);
        }

        Usuario usuario = HibernateUtil.consultando(s -> usuarioDAO.buscarPorEmail(s, email));

        if (usuario == null) {
            return new Login(Resultado.USUARIO_INEXISTENTE, null);
        }
        if (!SenhaUtil.verificar(senha, usuario.getSenhaSalt(), usuario.getSenhaHash())) {
            return new Login(Resultado.SENHA_INCORRETA, null);
        }
        return new Login(Resultado.OK, usuario);
    }

    // ===================== CREATE =====================

    /** Autocadastro pela tela de login. */
    public Usuario cadastrar(String nome, String email, String cpf, String senha)
            throws ValidacaoException {
        return cadastrar(nome, email, cpf, senha, null);
    }

    /**
     * @param tipo tipo desejado; se null, aplica a regra automática:
     *             o PRIMEIRO usuário do sistema vira ADMIN e os demais viram CLIENTE.
     *             Isso elimina o passo manual de rodar
     *             "UPDATE usuarios SET tipo='ADMIN'..." no MySQL após a instalação.
     */
    public Usuario cadastrar(String nome, String email, String cpf, String senha, Usuario.Tipo tipo)
            throws ValidacaoException {

        validarDadosBasicos(nome, email, senha);
        String cpfNormalizado = validarCpf(cpf);
        String emailNormalizado = email.trim().toLowerCase();

        return HibernateUtil.emTransacao(session -> {
            if (usuarioDAO.buscarPorEmail(session, emailNormalizado) != null) {
                throw new ValidacaoException(Mensagens.EMAIL_DUPLICADO);
            }
            if (usuarioDAO.buscarPorCpf(session, cpfNormalizado) != null) {
                throw new ValidacaoException(Mensagens.CPF_DUPLICADO);
            }

            Usuario.Tipo tipoFinal = tipo;
            if (tipoFinal == null) {
                boolean primeiroUsuario = usuarioDAO.contar(session) == 0;
                tipoFinal = primeiroUsuario ? Usuario.Tipo.ADMIN : Usuario.Tipo.CLIENTE;
            }

            String salt = SenhaUtil.gerarSalt();
            String hash = SenhaUtil.gerarHash(senha, salt);

            Usuario usuario = new Usuario(nome.trim(), emailNormalizado, cpfNormalizado,
                    hash, salt, tipoFinal);
            usuarioDAO.salvar(session, usuario);
            return usuario;
        });
    }

    // ===================== UPDATE =====================

    /** Atualiza os dados cadastrais. Passe senhaNova = null para manter a senha atual. */
    public Usuario atualizar(int id, String nome, String email, String cpf,
                             Usuario.Tipo tipo, String senhaNova) throws ValidacaoException {

        if (nome == null || nome.isBlank() || email == null || email.isBlank()) {
            throw new ValidacaoException(Mensagens.CAMPOS_OBRIGATORIOS);
        }
        if (!EMAIL.matcher(email.trim()).matches()) {
            throw new ValidacaoException(Mensagens.EMAIL_INVALIDO);
        }
        if (senhaNova != null && !senhaNova.isEmpty() && senhaNova.length() < SENHA_MINIMA) {
            throw new ValidacaoException(Mensagens.SENHA_CURTA);
        }

        String cpfNormalizado = validarCpf(cpf);
        String emailNormalizado = email.trim().toLowerCase();

        return HibernateUtil.emTransacao(session -> {
            Usuario usuario = usuarioDAO.buscarPorId(session, id);
            if (usuario == null) {
                throw new ValidacaoException(Mensagens.USUARIO_NAO_ENCONTRADO);
            }

            Usuario porEmail = usuarioDAO.buscarPorEmail(session, emailNormalizado);
            if (porEmail != null && porEmail.getId() != id) {
                throw new ValidacaoException(Mensagens.EMAIL_DUPLICADO);
            }
            Usuario porCpf = usuarioDAO.buscarPorCpf(session, cpfNormalizado);
            if (porCpf != null && porCpf.getId() != id) {
                throw new ValidacaoException(Mensagens.CPF_DUPLICADO);
            }

            usuario.setNome(nome.trim());
            usuario.setEmail(emailNormalizado);
            usuario.setCpf(cpfNormalizado);
            if (tipo != null) {
                usuario.setTipo(tipo);
            }
            if (senhaNova != null && !senhaNova.isEmpty()) {
                String salt = SenhaUtil.gerarSalt();
                usuario.setSenhaSalt(salt);
                usuario.setSenhaHash(SenhaUtil.gerarHash(senhaNova, salt));
            }
            return usuario;
        });
    }

    // ===================== DELETE =====================

    public void excluir(int id) throws ValidacaoException {
        HibernateUtil.emTransacao(session -> {
            Usuario usuario = usuarioDAO.buscarPorId(session, id);
            if (usuario == null) {
                throw new ValidacaoException(Mensagens.USUARIO_NAO_ENCONTRADO);
            }
            // pedidos referenciam o usuário: apagar quebraria a FK e o histórico
            if (usuarioDAO.contarPedidos(session, id) > 0) {
                throw new ValidacaoException(Mensagens.USUARIO_COM_PEDIDOS);
            }
            usuarioDAO.remover(session, usuario);
        });
    }

    // ===================== READ =====================

    public List<Usuario> listarTodos() {
        return HibernateUtil.consultando(usuarioDAO::listarTodos);
    }

    public Usuario buscarPorId(int id) {
        return HibernateUtil.consultando(s -> usuarioDAO.buscarPorId(s, id));
    }

    // ===================== validação =====================

    private void validarDadosBasicos(String nome, String email, String senha)
            throws ValidacaoException {
        if (nome == null || nome.isBlank()
                || email == null || email.isBlank()
                || senha == null || senha.isEmpty()) {
            throw new ValidacaoException(Mensagens.CAMPOS_OBRIGATORIOS);
        }
        if (!EMAIL.matcher(email.trim()).matches()) {
            throw new ValidacaoException(Mensagens.EMAIL_INVALIDO);
        }
        if (senha.length() < SENHA_MINIMA) {
            throw new ValidacaoException(Mensagens.SENHA_CURTA);
        }
    }

    private String validarCpf(String cpf) throws ValidacaoException {
        if (cpf == null || cpf.isBlank() || !CpfUtil.valido(cpf)) {
            throw new ValidacaoException(Mensagens.CPF_INVALIDO);
        }
        return CpfUtil.normalizar(cpf);
    }
}
