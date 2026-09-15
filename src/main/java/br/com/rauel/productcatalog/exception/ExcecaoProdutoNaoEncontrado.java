package br.com.rauel.productcatalog.exception;

public class ExcecaoProdutoNaoEncontrado extends RuntimeException {
    public ExcecaoProdutoNaoEncontrado(Long id) {
        super("Produto com ID " + id + " não encontrado");
    }
}
