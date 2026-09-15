package br.com.rauel.productcatalog.controller;

import br.com.rauel.productcatalog.dto.RequisicaoProduto;
import br.com.rauel.productcatalog.dto.RespostaProduto;
import br.com.rauel.productcatalog.exception.ExcecaoProdutoNaoEncontrado;
import br.com.rauel.productcatalog.service.ServicoProduto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ControladorProduto.class)
class TesteControladorProduto {
    private static final String BASE = "/api/v1/products";
    private static final String VALIDO = """
            {"nome":"Teclado","descricao":"USB","preco":99.90,"quantidadeEstoque":10,"ativo":true}
            """;
    private final MockMvc mvc;
    @MockitoBean private ServicoProduto servico;

    @Autowired
    TesteControladorProduto(MockMvc mvc) { this.mvc = mvc; }

    private RespostaProduto resposta() {
        var dataHora = LocalDateTime.of(2026, 1, 1, 10, 0);
        return new RespostaProduto(1L, "Teclado", "USB", new BigDecimal("99.90"), 10, true, dataHora, dataHora);
    }

    @Test
    void criaComCabecalhoLocation() throws Exception {
        when(servico.criar(any(RequisicaoProduto.class))).thenReturn(resposta());
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost" + BASE + "/1"))
                .andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.criadoEm").exists());
    }

    @Test
    void rejeitaCamposInvalidos() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":" ","preco":0,"quantidadeEstoque":-1,"ativo":null}
                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.dataHora").exists())
                .andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.erro").value("Bad Request"))
                .andExpect(jsonPath("$.mensagem").isNotEmpty()).andExpect(jsonPath("$.caminho").value(BASE))
                .andExpect(jsonPath("$.campos[*].campo", hasItems("nome", "preco", "quantidadeEstoque", "ativo")))
                .andExpect(jsonPath("$.trace").doesNotExist());
        verifyNoInteractions(servico);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "1.001", "10000000000000"})
    void rejeitaPrecosForaDaPrecisaoEDosLimites(String preco) throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(VALIDO.replace("99.90", preco)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos[*].campo", hasItem("preco")));
        verifyNoInteractions(servico);
    }

    @Test
    void rejeitaNomesEDescricoesAcimaDosLimites() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(VALIDO.replace("Teclado", "a".repeat(151)).replace("USB", "b".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[*].campo", hasItems("nome", "descricao")));
        verifyNoInteractions(servico);
    }

    @Test
    void validaNomeAposRemoverEspacos() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(VALIDO.replace("Teclado", " a ")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos[*].campo", hasItem("nome")));
        verifyNoInteractions(servico);
    }

    @Test
    void listaProdutos() throws Exception {
        when(servico.listarTodos()).thenReturn(List.of(resposta()));
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Teclado"));
    }

    @Test
    void listaVazia() throws Exception {
        when(servico.listarTodos()).thenReturn(List.of());
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void buscaPorId() throws Exception {
        when(servico.buscarPorId(1L)).thenReturn(resposta());
        mvc.perform(get(BASE + "/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void retornaNaoEncontradoParaIdInexistente() throws Exception {
        when(servico.buscarPorId(9L)).thenThrow(new ExcecaoProdutoNaoEncontrado(9L));
        mvc.perform(get(BASE + "/9")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Produto com ID 9 não encontrado"))
                .andExpect(jsonPath("$.caminho").value(BASE + "/9"));
    }

    @Test
    void buscaPorNome() throws Exception {
        when(servico.buscarPorNome("TEC")).thenReturn(List.of(resposta()));
        mvc.perform(get(BASE + "/search").param("name", "TEC"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejeitaBuscaEmBranco(String nome) throws Exception {
        mvc.perform(get(BASE + "/search").param("name", nome)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("nome"));
        verifyNoInteractions(servico);
    }

    @Test
    void rejeitaBuscaAcimaDoLimite() throws Exception {
        mvc.perform(get(BASE + "/search").param("name", "a".repeat(151))).andExpect(status().isBadRequest());
        verifyNoInteractions(servico);
    }

    @Test
    void rejeitaBuscaAusente() throws Exception {
        mvc.perform(get(BASE + "/search")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0", "-1", "999999999999999999999999"})
    void rejeitaIdInvalido(String id) throws Exception {
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(servico);
    }

    @Test
    void contaProdutos() throws Exception {
        when(servico.contar()).thenReturn(10L);
        mvc.perform(get(BASE + "/count")).andExpect(status().isOk()).andExpect(content().json("{\"quantidade\":10}"));
    }

    @Test
    void atualizaProduto() throws Exception {
        when(servico.atualizar(eq(1L), any(RequisicaoProduto.class))).thenReturn(resposta());
        mvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void rejeitaAtualizacaoInvalida() throws Exception {
        mvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(servico);
    }

    @Test
    void retornaNaoEncontradoNaAtualizacao() throws Exception {
        when(servico.atualizar(eq(9L), any(RequisicaoProduto.class))).thenThrow(new ExcecaoProdutoNaoEncontrado(9L));
        mvc.perform(put(BASE + "/9").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluiProduto() throws Exception {
        mvc.perform(delete(BASE + "/1")).andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(servico).excluir(1L);
    }

    @Test
    void retornaNaoEncontradoNaExclusao() throws Exception {
        doThrow(new ExcecaoProdutoNaoEncontrado(9L)).when(servico).excluir(9L);
        mvc.perform(delete(BASE + "/9")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "null", "{\"id\":1}", "{\"quantidadeEstoque\":1.5}"})
    void rejeitaCorpoMalformadoOuNaoSuportado(String json) throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
        verifyNoInteractions(servico);
    }
}
