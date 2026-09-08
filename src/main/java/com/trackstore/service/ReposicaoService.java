package com.trackstore.service;

import com.trackstore.dao.FornecedorDAO;
import com.trackstore.dao.HibernateUtil;
import com.trackstore.dao.ProdutoDAO;
import com.trackstore.dao.ReposicaoDAO;
import com.trackstore.model.Fornecedor;
import com.trackstore.model.Produto;
import com.trackstore.model.Reposicao;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import java.time.LocalDateTime;
import java.util.List;

/** Regras de reposição de estoque e consulta do histórico. */
public class ReposicaoService {

    private final ReposicaoDAO reposicaoDAO = new ReposicaoDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final FornecedorDAO fornecedorDAO = new FornecedorDAO();

    /**
     * Soma a quantidade ao saldo e registra a reposição, numa única transação.
     *
     * A validação de quantidade positiva aqui não é detalhe: antes o campo aceitava
     * qualquer número, então digitar -50 na tela de reposição SUBTRAÍA 50 do estoque
     * e ainda registrava isso no histórico como se fosse uma entrada.
     */
    public Reposicao repor(int produtoId, int fornecedorId, int quantidade)
            throws ValidacaoException {

        if (quantidade <= 0) {
            throw new ValidacaoException(Mensagens.QUANTIDADE_POSITIVA);
        }

        return HibernateUtil.emTransacao(session -> {
            Produto produto = produtoDAO.buscarPorIdComLock(session, produtoId);
            if (produto == null) {
                throw new ValidacaoException(Mensagens.PRODUTO_NAO_ENCONTRADO);
            }

            Fornecedor fornecedor = fornecedorDAO.buscarPorId(session, fornecedorId);
            if (fornecedor == null) {
                throw new ValidacaoException(Mensagens.FORNECEDOR_NAO_ENCONTRADO);
            }

            produto.setQuantidade(produto.getQuantidade() + quantidade);

            Reposicao reposicao = new Reposicao(produto, fornecedor, quantidade);
            reposicaoDAO.salvar(session, reposicao);
            return reposicao;
        });
    }

    public List<Reposicao> listarTodas() {
        return HibernateUtil.consultando(reposicaoDAO::listarTodas);
    }

    public List<Reposicao> listarPorProduto(int produtoId) {
        return HibernateUtil.consultando(s -> reposicaoDAO.listarPorProduto(s, produtoId));
    }

    public List<Reposicao> filtrar(Integer produtoId, LocalDateTime inicio, LocalDateTime fim) {
        return HibernateUtil.consultando(s -> reposicaoDAO.filtrar(s, produtoId, inicio, fim));
    }
}
