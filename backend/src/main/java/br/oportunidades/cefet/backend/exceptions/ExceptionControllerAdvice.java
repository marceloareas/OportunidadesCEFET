package br.oportunidades.cefet.backend.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;

// exception handler global dos controllers
@RestControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return build(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, WebRequest request) {
        return build(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, WebRequest request) {
        return build(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(SecurityException ex, WebRequest request) {
        return build(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler({ResourceNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        return build(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        return build("Erro interno no servidor.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Garante que todas as exceções tratadas pela superclasse (ResponseEntityExceptionHandler) -
     * como HttpMessageNotReadableException, HttpRequestMethodNotSupportedException,
     * MethodArgumentTypeMismatchException, NoResourceFoundException, MethodArgumentNotValidException etc. -
     * continuem retornando o status correto do framework, mas com o payload padronizado ErrorResponse
     * em vez do ProblemDetail/corpo padrão do Spring.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                               HttpStatusCode statusCode, WebRequest request) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = HttpStatus.valueOf(statusCode.value()).getReasonPhrase();
        }
        String path = request.getDescription(false).replaceFirst("^uri=", "");
        ErrorResponse errorResponse = ErrorResponse.of(message, statusCode.value(), path);
        return ResponseEntity.status(statusCode).headers(headers).body(errorResponse);
    }

    private ResponseEntity<ErrorResponse> build(String message, HttpStatus status, WebRequest request) {
        String path = request.getDescription(false).replaceFirst("^uri=", "");
        return ResponseEntity.status(status).body(ErrorResponse.of(message, status.value(), path));
    }
}
