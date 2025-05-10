package com.balancee.BookManager.dto.book;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
    @NotBlank(message = "Title is required, Please enter one")
    private String title;
    @NotBlank(message = "Author is required, Please enter one.")
    private String author;
    private String publishedDate;
    private Boolean available;

}
