package com.balancee.BookManager.exception;

import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.utils.LocaleHandler;
import com.balancee.BookManager.utils.ResponseCodes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ResponseDto response = new ResponseDto();
        response.setResponseCode(ResponseCodes.FAILED);
        response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.ERROR));
        response.setErrorMessage(errors);

        return ResponseEntity.ok(response);
    }

}
