package com.trackstore.service;

import com.trackstore.BaseTesteBanco;
import com.trackstore.model.Usuario;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UsuarioService - cadastro e autenticação")
class UsuarioServiceTest extends BaseTesteBanco {

    private static final String CPF_A = "52998224725";
    private static final String CPF_B = "11144477735";

    private final UsuarioService usuarioService = new UsuarioService();

    @Test
    @DisplayName("o primeiro usuário do sistema vira ADMIN e os seguintes, CLIENTE")
    void primeiroUsuarioEhAdmin() throws ValidacaoException {
        Usuario primeiro = usuarioService.cadastrar("Primeiro", "a@teste.com", CPF_A, "senha123");
        Usuario segundo = usuarioService.cadastrar("Segundo", "b@teste.com", CPF_B, "senha123");

        assertEquals(Usuario.Tipo.ADMIN, primeiro.getTipo());
        assertEquals(Usuario.Tipo.CLIENTE, segundo.getTipo());
    }

    @Test
    @DisplayName("a senha não é gravada em texto puro")
    void senhaEhCriptografada() throws ValidacaoException {
        Usuario usuario = usuarioService.cadastrar("Fulano", "a@teste.com", CPF_A, "senha123");

        assertNotEquals("senha123", usuario.getSenhaHash());
        assertFalse(usuario.getSenhaHash().contains("senha123"));
        assertNotNull(usuario.getSenhaSalt());
    }

    @Test
    @DisplayName("autentica com a senha correta")
    void autenticaComSenhaCorreta() throws ValidacaoException {
        usuarioService.cadastrar("Fulano", "a@teste.com", CPF_A, "senha123");

        var login = usuarioService.autenticar("a@teste.com", "senha123");

        assertTrue(login.autenticado());
        assertEquals("Fulano", login.usuario().getNome());
    }

    @Test
    @DisplayName("distingue usuário inexistente de senha incorreta")
    void diferenciaOsDoisMotivosDeFalha() throws ValidacaoException {
        usuarioService.cadastrar("Fulano", "a@teste.com", CPF_A, "senha123");

        assertEquals(UsuarioService.Resultado.SENHA_INCORRETA,
                usuarioService.autenticar("a@teste.com", "errada").resultado());
        assertEquals(UsuarioService.Resultado.USUARIO_INEXISTENTE,
                usuarioService.autenticar("naoexiste@teste.com", "senha123").resultado());
    }

    @Test
    @DisplayName("o e-mail não diferencia maiúsculas de minúsculas")
    void emailNaoDiferenciaCaixa() throws ValidacaoException {
        usuarioService.cadastrar("Fulano", "Fulano@Teste.com", CPF_A, "senha123");

        assertTrue(usuarioService.autenticar("fulano@teste.com", "senha123").autenticado());
    }

    @Test
    @DisplayName("recusa e-mail e CPF duplicados")
    void recusaDuplicados() throws ValidacaoException {
        usuarioService.cadastrar("Fulano", "a@teste.com", CPF_A, "senha123");

        ValidacaoException porEmail = assertThrows(ValidacaoException.class,
                () -> usuarioService.cadastrar("Outro", "a@teste.com", CPF_B, "senha123"));
        assertEquals(Mensagens.EMAIL_DUPLICADO, porEmail.getMessage());

        ValidacaoException porCpf = assertThrows(ValidacaoException.class,
                () -> usuarioService.cadastrar("Outro", "b@teste.com", CPF_A, "senha123"));
        assertEquals(Mensagens.CPF_DUPLICADO, porCpf.getMessage());
    }

    @Test
    @DisplayName("recusa CPF inválido, e-mail malformado e senha curta")
    void recusaDadosInvalidos() {
        assertThrows(ValidacaoException.class,
                () -> usuarioService.cadastrar("Fulano", "a@teste.com", "12345678901", "senha123"));
        assertThrows(ValidacaoException.class,
                () -> usuarioService.cadastrar("Fulano", "email-sem-arroba", CPF_A, "senha123"));
        assertThrows(ValidacaoException.class,
                () -> usuarioService.cadastrar("Fulano", "a@teste.com", CPF_A, "123"));
        assertThrows(ValidacaoException.class,
                () -> usuarioService.cadastrar("", "a@teste.com", CPF_A, "senha123"));
    }

    @Test
    @DisplayName("aceita CPF digitado com pontuação e guarda só os dígitos")
    void aceitaCpfComPontuacao() throws ValidacaoException {
        Usuario usuario = usuarioService.cadastrar("Fulano", "a@teste.com",
                "529.982.247-25", "senha123");

        assertEquals(CPF_A, usuario.getCpf());
    }

    @Test
    @DisplayName("atualiza dados e troca a senha")
    void atualizaUsuario() throws ValidacaoException {
        Usuario usuario = usuarioService.cadastrar("Fulano", "a@teste.com", CPF_A, "senha123");

        usuarioService.atualizar(usuario.getId(), "Fulano da Silva", "novo@teste.com",
                CPF_A, Usuario.Tipo.CLIENTE, "novaSenha456");

        assertFalse(usuarioService.autenticar("novo@teste.com", "senha123").autenticado());
        assertTrue(usuarioService.autenticar("novo@teste.com", "novaSenha456").autenticado());
        assertEquals("Fulano da Silva",
                usuarioService.buscarPorId(usuario.getId()).getNome());
    }

    @Test
    @DisplayName("atualizar sem informar nova senha mantém a anterior")
    void atualizaSemTrocarSenha() throws ValidacaoException {
        Usuario usuario = usuarioService.cadastrar("Fulano", "a@teste.com", CPF_A, "senha123");

        usuarioService.atualizar(usuario.getId(), "Fulano", "a@teste.com",
                CPF_A, Usuario.Tipo.CLIENTE, null);

        assertTrue(usuarioService.autenticar("a@teste.com", "senha123").autenticado());
    }

    @Test
    @DisplayName("exclui usuário sem pedidos")
    void excluiUsuario() throws ValidacaoException {
        Usuario usuario = usuarioService.cadastrar("Fulano", "a@teste.com", CPF_A, "senha123");

        usuarioService.excluir(usuario.getId());

        assertNull(usuarioService.buscarPorId(usuario.getId()));
    }
}
