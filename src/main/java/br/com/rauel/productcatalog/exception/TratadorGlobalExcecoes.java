package br.com.rauel.productcatalog.exception;

import br.com.rauel.productcatalog.dto.RespostaErro;
import br.com.rauel.productcatalog.dto.RespostaErroCampo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class TratadorGlobalExcecoes extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ExcecaoProdutoNaoEncontrado.class)
    public ResponseEntity<Object> tratarProdutoNaoEncontrado(ExcecaoProdutoNaoEncontrado excecao, WebRequest requisicao) {
        return erro(HttpStatus.NOT_FOUND, excecao.getMessage(), List.of(), new HttpHeaders(), requisicao);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException excecao,
            HttpHeaders cabecalhos, HttpStatusCode status, WebRequest requisicao) {
        var campos = excecao.getBindingResult().getFieldErrors().stream()
                .map(campo -> new RespostaErroCampo(campo.getField(), campo.getDefaultMessage())).toList();
        return erro(status, "Dados de entrada inválidos", campos, cabecalhos, requisicao);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException excecao,
            HttpHeaders cabecalhos, HttpStatusCode status, WebRequest requisicao) {
        var campos = excecao.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream().map(item ->
                        new RespostaErroCampo(resultado.getMethodParameter().getParameterName(), item.getDefaultMessage())))
                .toList();
        return erro(status, "Parâmetros inválidos", campos, cabecalhos, requisicao);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception excecao, Object corpo,
            HttpHeaders cabecalhos, HttpStatusCode status, WebRequest requisicao) {
        String mensagem = switch (status.value()) {
            case 400 -> "Requisição inválida: verifique o JSON e os parâmetros informados";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método HTTP não permitido";
            case 406 -> "Formato de resposta não suportado";
            case 415 -> "Tipo de conteúdo não suportado";
            default -> "Não foi possível processar a requisição";
        };
        return erro(status, mensagem, List.of(), cabecalhos, requisicao);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> tratarErroInesperado(Exception excecao, WebRequest requisicao) {
        logger.error("Erro inesperado ao processar requisição", excecao);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a requisição",
                List.of(), new HttpHeaders(), requisicao);
    }

    private ResponseEntity<Object> erro(HttpStatusCode status, String mensagem, List<RespostaErroCampo> campos,
            HttpHeaders cabecalhos, WebRequest requisicao) {
        String caminho = ((ServletWebRequest) requisicao).getRequest().getRequestURI();
        var resposta = new RespostaErro(LocalDateTime.now(), status.value(),
                HttpStatus.valueOf(status.value()).getReasonPhrase(), mensagem, caminho, campos);
        return new ResponseEntity<>(resposta, cabecalhos, status);
    }
}
