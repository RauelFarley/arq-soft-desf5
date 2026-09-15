package br.com.rauel.productcatalog.service;

import br.com.rauel.productcatalog.dto.RequisicaoProduto;
import br.com.rauel.productcatalog.exception.ExcecaoProdutoNaoEncontrado;
import br.com.rauel.productcatalog.model.Produto;
import br.com.rauel.productcatalog.repository.RepositorioProduto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TesteServicoProduto {
    @Mock private RepositorioProduto repositorio;
    @InjectMocks private ServicoProduto servico;
    private Produto produto;
    private RequisicaoProduto requisicao;
    private final LocalDateTime criadoEm = LocalDateTime.of(2026, 1, 1, 10, 0);

    @BeforeEach
    void preparar() {
        requisicao = new RequisicaoProduto("Teclado", "USB", new BigDecimal("99.90"), 10, true);
        produto = new Produto("Teclado", "USB", new BigDecimal("99.90"), 10, true);
        ReflectionTestUtils.setField(produto, "id", 1L);
        ReflectionTestUtils.setField(produto, "criadoEm", criadoEm);
        ReflectionTestUtils.setField(produto, "atualizadoEm", criadoEm);
    }

    @Test
    void criaEConverteProduto() {
        when(repositorio.saveAndFlush(any(Produto.class))).thenReturn(produto);
        var resposta = servico.criar(requisicao);
        var capturador = ArgumentCaptor.forClass(Produto.class);
        verify(repositorio).saveAndFlush(capturador.capture());
        assertThat(capturador.getValue()).extracting(Produto::obterNome, Produto::obterDescricao, Produto::obterPreco,
                Produto::obterQuantidadeEstoque, Produto::obterAtivo)
                .containsExactly("Teclado", "USB", new BigDecimal("99.90"), 10, true);
        assertThat(capturador.getValue().obterId()).isNull();
        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.criadoEm()).isEqualTo(criadoEm);
    }

    @Test
    void listaProdutos() {
        when(repositorio.findAll()).thenReturn(List.of(produto));
        assertThat(servico.listarTodos()).singleElement().satisfies(item -> assertThat(item.nome()).isEqualTo("Teclado"));
    }

    @Test
    void retornaListaVazia() {
        when(repositorio.findAll()).thenReturn(List.of());
        assertThat(servico.listarTodos()).isEmpty();
    }

    @Test
    void buscaIdExistente() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(produto));
        assertThat(servico.buscarPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void rejeitaIdInexistente() {
        when(repositorio.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servico.buscarPorId(9L)).isInstanceOf(ExcecaoProdutoNaoEncontrado.class);
    }

    @Test
    void buscaPorNome() {
        when(repositorio.buscarPorNome("TEC")).thenReturn(List.of(produto));
        assertThat(servico.buscarPorNome(" TEC ")).hasSize(1);
        verify(repositorio).buscarPorNome("TEC");
    }

    @Test
    void contaProdutos() {
        when(repositorio.count()).thenReturn(7L);
        assertThat(servico.contar()).isEqualTo(7L);
    }

    @Test
    void atualizaCamposEditaveisEPreservaIdentidade() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(produto));
        when(repositorio.saveAndFlush(produto)).thenReturn(produto);
        var atualizado = servico.atualizar(1L, new RequisicaoProduto("Mouse", null, new BigDecimal("25.50"), 0, false));
        assertThat(atualizado.id()).isEqualTo(1L);
        assertThat(atualizado.criadoEm()).isEqualTo(criadoEm);
        assertThat(atualizado.nome()).isEqualTo("Mouse");
        assertThat(atualizado.descricao()).isNull();
        assertThat(atualizado.preco()).isEqualByComparingTo("25.50");
        assertThat(atualizado.quantidadeEstoque()).isZero();
        assertThat(atualizado.ativo()).isFalse();
    }

    @Test
    void rejeitaAtualizacaoDeProdutoInexistente() {
        when(repositorio.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servico.atualizar(9L, requisicao)).isInstanceOf(ExcecaoProdutoNaoEncontrado.class);
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void excluiProdutoExistente() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(produto));
        servico.excluir(1L);
        verify(repositorio).delete(produto);
    }

    @Test
    void rejeitaExclusaoDeProdutoInexistente() {
        when(repositorio.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servico.excluir(9L)).isInstanceOf(ExcecaoProdutoNaoEncontrado.class);
        verify(repositorio, never()).delete(any());
    }
}
