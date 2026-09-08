package com.trackstore.service;

import com.trackstore.dao.HibernateUtil;
import com.trackstore.dao.PedidoDAO;
import com.trackstore.dao.ProdutoDAO;
import com.trackstore.dao.UsuarioDAO;
import com.trackstore.model.Pedido;
import com.trackstore.model.Produto;
import com.trackstore.model.Usuario;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import java.util.List;

/** Regras de finalização de compra. */
public class PedidoService {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Finaliza a compra: valida o estoque, dá baixa e grava o pedido — tudo dentro
     * de UMA transação. Se qualquer item falhar, nada é gravado.
     *
     * Três defeitos da versão anterior foram corrigidos aqui:
     *
     *  1. NÃO HAVIA VALIDAÇÃO DE SALDO. O código apenas subtraía, então comprar 500
     *     de um produto com 3 gravava -497 no estoque.
     *
     *  2. O PEDIDO ERA GRAVADO ANTES DA BAIXA e não havia rollback. Uma falha no
     *     meio deixava o pedido salvo sem o estoque debitado.
     *
     *  3. LEITURA SEM TRAVA. Dois usuários comprando o último item ao mesmo tempo
     *     liam o mesmo saldo e ambos passavam. Agora cada produto é lido com
     *     PESSIMISTIC_WRITE (SELECT ... FOR UPDATE): o segundo espera o primeiro
     *     commitar e enxerga o saldo já debitado.
     */
    public Pedido finalizar(Usuario usuarioLogado, Carrinho carrinho) throws ValidacaoException {
        if (carrinho == null || carrinho.isVazio()) {
            throw new ValidacaoException(Mensagens.CARRINHO_VAZIO);
        }

        return HibernateUtil.emTransacao(session -> {
            Usuario usuario = usuarioDAO.buscarPorId(session, usuarioLogado.getId());
            if (usuario == null) {
                throw new ValidacaoException(Mensagens.USUARIO_NAO_ENCONTRADO);
            }

            Pedido pedido = new Pedido(usuario);

            for (Carrinho.Item item : carrinho.getItens()) {
                // trava a linha do produto até o commit desta transação
                Produto produto = produtoDAO.buscarPorIdComLock(session, item.getProdutoId());

                if (produto == null) {
                    throw new ValidacaoException(Mensagens.PRODUTO_NAO_ENCONTRADO);
                }
                if (!produto.isAtivo()) {
                    throw new ValidacaoException(Mensagens.PRODUTO_INATIVO + "\n\n" + produto.getNome());
                }
                if (produto.getQuantidade() < item.getQuantidade()) {
                    throw new ValidacaoException(
                            Mensagens.estoqueInsuficiente(produto.getNome(), produto.getQuantidade()));
                }

                produto.setQuantidade(produto.getQuantidade() - item.getQuantidade());
                pedido.adicionarItem(produto, item.getQuantidade());
            }

            pedidoDAO.salvar(session, pedido);
            return pedido;
        });
    }

    public List<Pedido> listarPorUsuario(int usuarioId) {
        return HibernateUtil.consultando(s -> pedidoDAO.listarPorUsuario(s, usuarioId));
    }

    public List<Pedido> listarTodos() {
        return HibernateUtil.consultando(pedidoDAO::listarTodos);
    }
}
