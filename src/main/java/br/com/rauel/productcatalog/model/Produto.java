package br.com.rauel.productcatalog.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "products")
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false, length = 150)
    private String nome;
    @Column(name = "description", length = 500)
    private String descricao;
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal preco;
    @Column(name = "stock_quantity", nullable = false)
    private Integer quantidadeEstoque;
    @Column(name = "active", nullable = false)
    private Boolean ativo;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Produto() {}

    public Produto(String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque, Boolean ativo) {
        atualizar(nome, descricao, preco, quantidadeEstoque, ativo);
    }

    public void atualizar(String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque, Boolean ativo) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.ativo = ativo;
        // Também registra um PUT que repete os valores atuais.
        if (id != null) {
            atualizadoEm = dataHoraAtual();
        }
    }

    @PrePersist
    private void antesDeCriar() {
        criadoEm = dataHoraAtual();
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    private void antesDeAtualizar() {
        atualizadoEm = dataHoraAtual();
    }

    private static LocalDateTime dataHoraAtual() {
        // TIMESTAMP do H2 usa precisão de microssegundos.
        return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }

    public Long obterId() { return id; }
    public String obterNome() { return nome; }
    public String obterDescricao() { return descricao; }
    public BigDecimal obterPreco() { return preco; }
    public Integer obterQuantidadeEstoque() { return quantidadeEstoque; }
    public Boolean obterAtivo() { return ativo; }
    public LocalDateTime obterCriadoEm() { return criadoEm; }
    public LocalDateTime obterAtualizadoEm() { return atualizadoEm; }
}
