package br.com.rauel.productcatalog;

import br.com.rauel.productcatalog.dto.RequisicaoProduto;
import br.com.rauel.productcatalog.repository.RepositorioProduto;
import br.com.rauel.productcatalog.service.ServicoProduto;
import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:producttestdb")
@Transactional
class TesteAplicacaoCatalogoProdutos {
    private final ServicoProduto servico;
    private final RepositorioProduto repositorio;
    private final Flyway flyway;
    private final EntityManager gerenciadorEntidades;

    @Autowired
    TesteAplicacaoCatalogoProdutos(ServicoProduto servico, RepositorioProduto repositorio, Flyway flyway,
            EntityManager gerenciadorEntidades) {
        this.servico = servico;
        this.repositorio = repositorio;
        this.flyway = flyway;
        this.gerenciadorEntidades = gerenciadorEntidades;
    }

    @Test
    void carregaContextoComMigracaoAplicada() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(repositorio.count()).isZero();
    }

    @Test
    void atualizaDataMesmoSemAlteracaoDosCampos() {
        var requisicao = new RequisicaoProduto("Teclado", null, new BigDecimal("99.90"), 10, true);
        var criado = servico.criar(requisicao);
        gerenciadorEntidades.clear();
        var atualizado = servico.atualizar(criado.id(), requisicao);
        assertThat(atualizado.criadoEm()).isEqualTo(criado.criadoEm());
        assertThat(atualizado.atualizadoEm()).isAfter(criado.atualizadoEm());
    }

    @Test
    void persisteBuscaAtualizaEExcluiComBancoReal() {
        var criado = servico.criar(new RequisicaoProduto("Teclado Mecânico", null, new BigDecimal("99.90"), 10, true));
        assertThat(criado.id()).isPositive();
        assertThat(criado.criadoEm()).isNotNull();
        assertThat(criado.atualizadoEm()).isEqualTo(criado.criadoEm());
        gerenciadorEntidades.clear();
        var recarregado = servico.buscarPorId(criado.id());
        assertThat(recarregado.criadoEm()).isEqualTo(criado.criadoEm());
        assertThat(recarregado.atualizadoEm()).isEqualTo(criado.atualizadoEm());
        assertThat(servico.buscarPorNome("CLADO")).singleElement().satisfies(item -> assertThat(item.id()).isEqualTo(criado.id()));
        assertThat(servico.buscarPorNome("inexistente")).isEmpty();
        assertThat(servico.contar()).isEqualTo(1);
        var atualizado = servico.atualizar(criado.id(), new RequisicaoProduto("Mouse", "Sem fio", new BigDecimal("15.50"), 0, false));
        assertThat(atualizado.id()).isEqualTo(criado.id());
        assertThat(atualizado.criadoEm()).isEqualTo(criado.criadoEm());
        assertThat(atualizado.atualizadoEm()).isAfter(criado.atualizadoEm());
        assertThat(servico.buscarPorId(criado.id()).nome()).isEqualTo("Mouse");
        servico.excluir(criado.id());
        repositorio.flush();
        assertThat(servico.listarTodos()).isEmpty();
    }
}
