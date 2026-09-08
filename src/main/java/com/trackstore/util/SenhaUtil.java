package com.trackstore.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Criptografia de senhas com PBKDF2-HMAC-SHA256, salt aleatório por usuário e
 * 210.000 iterações. Não depende de biblioteca externa: PBKDF2 já vem no JDK.
 *
 * Por que não SHA-256 puro (versão anterior): SHA-256 foi projetado para ser
 * RÁPIDO, e velocidade é exatamente o que ajuda quem tenta quebrar senhas por
 * força bruta — uma GPU testa bilhões por segundo. PBKDF2 é deliberadamente lento
 * e ajustável, encarecendo o ataque na mesma proporção.
 */
public final class SenhaUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 210_000;
    private static final int TAMANHO_HASH_BITS = 256;
    private static final int TAMANHO_SALT_BYTES = 16;

    private SenhaUtil() {
    }

    public static String gerarSalt() {
        byte[] salt = new byte[TAMANHO_SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String gerarHash(String senha, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    senha.toCharArray(),
                    Base64.getDecoder().decode(salt),
                    ITERACOES,
                    TAMANHO_HASH_BITS);
            byte[] hash = SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar hash da senha", e);
        }
    }

    public static boolean verificar(String senhaDigitada, String salt, String hashArmazenado) {
        if (senhaDigitada == null || salt == null || hashArmazenado == null) {
            return false;
        }
        byte[] calculado = Base64.getDecoder().decode(gerarHash(senhaDigitada, salt));
        byte[] armazenado = Base64.getDecoder().decode(hashArmazenado);

        // MessageDigest.isEqual compara em tempo constante: não vaza, pelo tempo de
        // resposta, quantos bytes iniciais do hash estavam corretos.
        return MessageDigest.isEqual(calculado, armazenado);
    }
}
