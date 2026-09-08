package com.trackstore.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatação de valores para exibição nas telas. */
public final class Formato {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(BR);
    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formato() {
    }

    public static String moeda(BigDecimal valor) {
        return MOEDA.format(valor == null ? BigDecimal.ZERO : valor);
    }

    public static String dataHora(LocalDateTime data) {
        return data == null ? "" : data.format(DATA_HORA);
    }

    /**
     * Converte texto digitado em BigDecimal aceitando vírgula OU ponto como
     * separador decimal — o usuário brasileiro digita "10,50".
     */
    public static BigDecimal paraDecimal(String texto) throws ValidacaoException {
        try {
            return new BigDecimal(texto.trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidacaoException(Mensagens.PRECO_INVALIDO);
        }
    }

    public static int paraInteiro(String texto, String mensagemErro) throws ValidacaoException {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ValidacaoException(mensagemErro);
        }
    }
}
