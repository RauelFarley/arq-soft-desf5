package br.com.rauel.productcatalog.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RespostaErro(LocalDateTime dataHora, int status, String erro, String mensagem,
        String caminho, List<RespostaErroCampo> campos) {}
