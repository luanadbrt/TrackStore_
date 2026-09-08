package com.trackstore.util;

/**
 * Exceção usada para regras de negócio (campos vazios, dados duplicados,
 * estoque insuficiente, etc.).
 *
 * A mensagem já vem pronta para ser exibida diretamente ao usuário — por isso as
 * telas nunca precisam decidir o texto: elas só mostram getMessage().
 */
public class ValidacaoException extends Exception {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
