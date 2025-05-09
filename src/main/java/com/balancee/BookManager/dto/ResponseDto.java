package com.balancee.BookManager.dto;

import lombok.Data;

@Data
public class ResponseDto {
    private String responseCode;
    private String responseMessage;
    private Object errorMessage;
    private Object info;
}
