package com.colibri.frota.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> aoLancarStatus(ResponseStatusException e) {
        HttpStatusCode codigo = e.getStatusCode();
        String msg = e.getReason() != null
                ? e.getReason()
                : HttpStatus.valueOf(codigo.value()).getReasonPhrase();
        return ResponseEntity.status(codigo).body(new ErroResponse(codigo.value(), msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> aoValidar(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, msg.isBlank() ? "Dados inválidos" : msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> aoLerCorpo(HttpMessageNotReadableException e) {
        return erro(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: confira o JSON, as datas (AAAA-MM-DD) e os valores permitidos");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErroResponse> aoFaltarParte(MissingServletRequestPartException e) {
        return erro(HttpStatus.BAD_REQUEST, "Campo obrigatório ausente: " + e.getRequestPartName());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroResponse> aoExcederTamanho(MaxUploadSizeExceededException e) {
        return erro(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo muito grande (máximo de 10MB)");
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String msg) {
        return ResponseEntity.status(status).body(new ErroResponse(status.value(), msg));
    }
}