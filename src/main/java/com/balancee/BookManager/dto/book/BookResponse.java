package com.balancee.BookManager.dto.book;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class BookResponse {
    private UUID id;
    private String title;
    private String author;
    private String publishedDate;
    private Boolean available;
}
