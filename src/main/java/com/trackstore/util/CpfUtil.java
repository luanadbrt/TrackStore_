package com.trackstore.util;

/**
 * Validação de CPF pelos dígitos verificadores.
 *
 * Contar 11 caracteres não basta: "12345678901" tem 11 dígitos e não é um CPF.
 * Os dois últimos dígitos são calculados a partir dos nove primeiros, então dá
 * para saber se o número é válido sem consultar nada.
 */
public final class CpfUtil {

    /** Quantidade de dígitos exigida. A especificação original do trabalho pedia 9. */
    public static final int DIGITOS = 11;

    private CpfUtil() {
    }

    /** Remove pontos, traços e espaços — o usuário pode digitar 000.000.000-00. */
    public static String normalizar(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("[^0-9]", "");
    }

    public static boolean valido(String cpf) {
        String n = normalizar(cpf);

        if (n.length() != DIGITOS) {
            return false;
        }

        // 00000000000, 11111111111, ... passam no cálculo dos dígitos, mas não existem
        if (n.chars().distinct().count() == 1) {
            return false;
        }

        return digitoVerificador(n, 9) == (n.charAt(9) - '0')
                && digitoVerificador(n, 10) == (n.charAt(10) - '0');
    }

    /**
     * Calcula o dígito da posição informada: soma ponderada dos dígitos anteriores,
     * com pesos decrescentes, e o resto da divisão por 11.
     */
    private static int digitoVerificador(String cpf, int posicao) {
        int soma = 0;
        int peso = posicao + 1;
        for (int i = 0; i < posicao; i++) {
            soma += (cpf.charAt(i) - '0') * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    /** Formata para exibição: 12345678909 -> 123.456.789-09 */
    public static String formatar(String cpf) {
        String n = normalizar(cpf);
        if (n.length() != DIGITOS) {
            return n;
        }
        return n.substring(0, 3) + "." + n.substring(3, 6) + "." + n.substring(6, 9) + "-" + n.substring(9);
    }
}
