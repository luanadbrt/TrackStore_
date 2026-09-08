package com.trackstore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CpfUtil - validação de CPF pelos dígitos verificadores")
class CpfUtilTest {

    @Test
    @DisplayName("aceita CPFs válidos, com ou sem pontuação")
    void aceitaCpfValido() {
        assertTrue(CpfUtil.valido("52998224725"));
        assertTrue(CpfUtil.valido("529.982.247-25"));
        assertTrue(CpfUtil.valido("111.444.777-35"));
        assertTrue(CpfUtil.valido("12345678909"));
    }

    @Test
    @DisplayName("rejeita número com dígito verificador errado")
    void rejeitaDigitoVerificadorErrado() {
        // tem 11 dígitos e passaria numa validação que só conta caracteres
        assertFalse(CpfUtil.valido("12345678901"));
        assertFalse(CpfUtil.valido("52998224726"));
    }

    @Test
    @DisplayName("rejeita sequências de dígitos repetidos")
    void rejeitaDigitosRepetidos() {
        assertFalse(CpfUtil.valido("00000000000"));
        assertFalse(CpfUtil.valido("11111111111"));
        assertFalse(CpfUtil.valido("99999999999"));
    }

    @Test
    @DisplayName("rejeita quantidade de dígitos diferente de 11")
    void rejeitaTamanhoErrado() {
        assertFalse(CpfUtil.valido("529982247"));   // os 9 dígitos da versão antiga
        assertFalse(CpfUtil.valido("529982247251"));
        assertFalse(CpfUtil.valido(""));
        assertFalse(CpfUtil.valido(null));
    }

    @Test
    @DisplayName("normaliza e formata")
    void normalizaEFormata() {
        assertEquals("52998224725", CpfUtil.normalizar("529.982.247-25"));
        assertEquals("529.982.247-25", CpfUtil.formatar("52998224725"));
    }
}
