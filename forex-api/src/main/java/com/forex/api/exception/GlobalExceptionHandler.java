package com.forex.api.exception;

import com.forex.api.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ForexApiException.class)
    public ResponseEntity<ErrorResponse> handleForexApiException(ForexApiException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error(ex.getMessage())
                .statusCode(ex.getStatusCode())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getStatusCode()));
    }
    
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientException(HttpClientErrorException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error("Error en la petición a la API externa: " + ex.getMessage())
                .statusCode(ex.getStatusCode().value())
                .build();
        
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }
    
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServerException(HttpServerErrorException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error("Error en el servidor de la API externa: " + ex.getMessage())
                .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccessException(ResourceAccessException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error("No se puede conectar a la API externa. Verifique su conexión a internet.")
                .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error("Error interno del servidor")
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
