package br.com.rauel.productcatalog;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Catálogo de Produtos", version = "1.0.0",
        description = "API REST para gerenciamento de produtos de uma empresa de vendas on-line."))
public class AplicacaoCatalogoProdutos {
    public static void main(String[] argumentos) {
        SpringApplication.run(AplicacaoCatalogoProdutos.class, argumentos);
    }
}
