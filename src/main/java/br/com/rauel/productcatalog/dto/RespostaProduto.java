package br.com.rauel.productcatalog.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RespostaProduto(Long id, String nome, String descricao, BigDecimal preco,
        Integer quantidadeEstoque, Boolean ativo, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {}
