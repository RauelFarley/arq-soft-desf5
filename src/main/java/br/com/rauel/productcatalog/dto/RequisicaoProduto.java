package br.com.rauel.productcatalog.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RequisicaoProduto(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres") String nome,
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres") String descricao,
        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0", inclusive = false, message = "O preço deve ser maior que zero")
        @Digits(integer = 13, fraction = 2, message = "O preço deve ter até 13 dígitos inteiros e 2 decimais") BigDecimal preco,
        @NotNull(message = "A quantidade em estoque é obrigatória")
        @PositiveOrZero(message = "A quantidade em estoque não pode ser negativa") Integer quantidadeEstoque,
        @NotNull(message = "O estado ativo é obrigatório") Boolean ativo) {
    public RequisicaoProduto {
        nome = nome == null ? null : nome.strip();
    }
}
