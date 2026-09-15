package br.com.rauel.productcatalog.service;

import br.com.rauel.productcatalog.dto.RequisicaoProduto;
import br.com.rauel.productcatalog.dto.RespostaProduto;
import br.com.rauel.productcatalog.exception.ExcecaoProdutoNaoEncontrado;
import br.com.rauel.productcatalog.model.Produto;
import br.com.rauel.productcatalog.repository.RepositorioProduto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ServicoProduto {
    private final RepositorioProduto repositorio;

    public ServicoProduto(RepositorioProduto repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public RespostaProduto criar(RequisicaoProduto requisicao) {
        Produto produto = new Produto(requisicao.nome(), requisicao.descricao(), requisicao.preco(),
                requisicao.quantidadeEstoque(), requisicao.ativo());
        return converterParaResposta(repositorio.saveAndFlush(produto));
    }

    public List<RespostaProduto> listarTodos() {
        return repositorio.findAll().stream().map(this::converterParaResposta).toList();
    }

    public RespostaProduto buscarPorId(Long id) {
        return converterParaResposta(exigirProduto(id));
    }

    public List<RespostaProduto> buscarPorNome(String nome) {
        return repositorio.buscarPorNome(nome.strip()).stream().map(this::converterParaResposta).toList();
    }

    public long contar() {
        return repositorio.count();
    }

    @Transactional
    public RespostaProduto atualizar(Long id, RequisicaoProduto requisicao) {
        Produto produto = exigirProduto(id);
        produto.atualizar(requisicao.nome(), requisicao.descricao(), requisicao.preco(), requisicao.quantidadeEstoque(), requisicao.ativo());
        return converterParaResposta(repositorio.saveAndFlush(produto));
    }

    @Transactional
    public void excluir(Long id) {
        repositorio.delete(exigirProduto(id));
    }

    private Produto exigirProduto(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ExcecaoProdutoNaoEncontrado(id));
    }

    private RespostaProduto converterParaResposta(Produto produto) {
        return new RespostaProduto(produto.obterId(), produto.obterNome(), produto.obterDescricao(), produto.obterPreco(),
                produto.obterQuantidadeEstoque(), produto.obterAtivo(), produto.obterCriadoEm(), produto.obterAtualizadoEm());
    }
}
