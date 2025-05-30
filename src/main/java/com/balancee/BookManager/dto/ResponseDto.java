package com.balancee.BookManager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
    private String responseCode;
    private String responseMessage;
    private Object errorMessage;
    private Object info;
}
