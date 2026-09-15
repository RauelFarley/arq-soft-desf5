package br.com.rauel.productcatalog.controller;

import br.com.rauel.productcatalog.dto.*;
import br.com.rauel.productcatalog.service.ServicoProduto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Produtos", description = "Cadastro e consulta de produtos")
public class ControladorProduto {
    private final ServicoProduto servico;

    public ControladorProduto(ServicoProduto servico) { this.servico = servico; }

    @PostMapping
    @Operation(summary = "Criar produto")
    @ApiResponse(responseCode = "201", description = "Produto criado; Location contém a URL do recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<RespostaProduto> criar(@Valid @RequestBody RequisicaoProduto requisicao) {
        RespostaProduto resposta = servico.criar(requisicao);
        var localizacao = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(localizacao).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos")
    public List<RespostaProduto> listarTodos() { return servico.listarTodos(); }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar produto pelo ID")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public RespostaProduto buscarPorId(@PathVariable @Positive(message = "O ID deve ser positivo") Long id) {
        return servico.buscarPorId(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar por parte do nome, sem diferenciar maiúsculas e minúsculas")
    @ApiResponse(responseCode = "200", description = "Lista de produtos encontrados")
    @ApiResponse(responseCode = "400", description = "Nome ausente, vazio ou maior que 150 caracteres")
    public List<RespostaProduto> buscarPorNome(@RequestParam("name")
            @NotBlank(message = "O nome da busca é obrigatório")
            @Size(max = 150, message = "O nome da busca deve ter no máximo 150 caracteres") String nome) {
        return servico.buscarPorNome(nome);
    }

    @GetMapping("/count")
    @Operation(summary = "Contar produtos")
    public RespostaContagem contar() { return new RespostaContagem(servico.contar()); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os campos editáveis do produto")
    @ApiResponse(responseCode = "200", description = "Produto atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public RespostaProduto atualizar(@PathVariable @Positive(message = "O ID deve ser positivo") Long id,
            @Valid @RequestBody RequisicaoProduto requisicao) {
        return servico.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir produto")
    @ApiResponse(responseCode = "204", description = "Produto excluído")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ResponseEntity<Void> excluir(@PathVariable @Positive(message = "O ID deve ser positivo") Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
