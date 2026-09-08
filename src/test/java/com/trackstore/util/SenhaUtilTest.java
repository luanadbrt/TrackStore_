package com.trackstore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SenhaUtil - hash e verificação de senha")
class SenhaUtilTest {

    @Test
    @DisplayName("aceita a senha correta e rejeita a errada")
    void verificaSenha() {
        String salt = SenhaUtil.gerarSalt();
        String hash = SenhaUtil.gerarHash("minhaSenha123", salt);

        assertTrue(SenhaUtil.verificar("minhaSenha123", salt, hash));
        assertFalse(SenhaUtil.verificar("minhasenha123", salt, hash)); // maiúscula importa
        assertFalse(SenhaUtil.verificar("outraSenha", salt, hash));
    }

    @Test
    @DisplayName("a mesma senha gera hashes diferentes com salts diferentes")
    void saltTornaHashUnico() {
        String senha = "senhaIgual";
        String hash1 = SenhaUtil.gerarHash(senha, SenhaUtil.gerarSalt());
        String hash2 = SenhaUtil.gerarHash(senha, SenhaUtil.gerarSalt());

        // é isso que impede descobrir, olhando o banco, que dois usuários usam a
        // mesma senha — e inviabiliza tabelas de hash pré-calculadas
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("o hash não contém a senha em texto puro")
    void naoGuardaSenhaEmTextoPuro() {
        String salt = SenhaUtil.gerarSalt();
        String hash = SenhaUtil.gerarHash("segredo", salt);

        assertFalse(hash.contains("segredo"));
        assertNotEquals("segredo", hash);
    }

    @Test
    @DisplayName("não quebra com argumentos nulos")
    void toleraNulos() {
        String salt = SenhaUtil.gerarSalt();
        String hash = SenhaUtil.gerarHash("x", salt);

        assertFalse(SenhaUtil.verificar(null, salt, hash));
        assertFalse(SenhaUtil.verificar("x", null, hash));
        assertFalse(SenhaUtil.verificar("x", salt, null));
    }
}
