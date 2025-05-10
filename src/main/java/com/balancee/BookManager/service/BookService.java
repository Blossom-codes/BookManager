package com.balancee.BookManager.service;

import com.balancee.BookManager.dto.book.BookRequest;
import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.book.BookResponse;
import com.balancee.BookManager.dto.user.UserInfo;
import com.balancee.BookManager.entity.Book;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookService {

    ResponseDto save(BookRequest request);
    Page<BookResponse> getAllBooks(int page, int size, String title, String author, Boolean available);
}
