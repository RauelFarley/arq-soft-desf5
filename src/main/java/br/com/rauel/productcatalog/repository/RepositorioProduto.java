package br.com.rauel.productcatalog.repository;

import br.com.rauel.productcatalog.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RepositorioProduto extends JpaRepository<Produto, Long> {
    @Query("select produto from Produto produto where locate(lower(:nome), lower(produto.nome)) > 0")
    List<Produto> buscarPorNome(@Param("nome") String nome);
}
