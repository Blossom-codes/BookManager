package com.balancee.BookManager.service.impl;

import com.balancee.BookManager.dto.book.BookRequest;
import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.book.BookResponse;
import com.balancee.BookManager.dto.user.UserInfo;
import com.balancee.BookManager.entity.Book;
import com.balancee.BookManager.repository.BookRepository;
import com.balancee.BookManager.service.BookService;
import com.balancee.BookManager.utils.LocaleHandler;
import com.balancee.BookManager.utils.LoggingUtils;
import com.balancee.BookManager.utils.ResponseCodes;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public ResponseDto save(BookRequest request) {
        ResponseDto response = new ResponseDto();
        response.setResponseCode(ResponseCodes.ERROR);
        response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.ERROR));

        try {
            if (request == null) {
                throw new RuntimeException("An error occurred, request to save book is null");
            }
            Book newBook = new Book();
            newBook.setTitle(request.getTitle());
            newBook.setAuthor(request.getAuthor());
            if (request.getPublishedDate() != null && !request.getPublishedDate().isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                newBook.setPublishedDate(LocalDateTime.parse(request.getPublishedDate(), formatter));
            }
            if (request.getAvailable() != null) {
                newBook.setAvailable(request.getAvailable());
            }
            bookRepository.save(newBook);
            response.setResponseCode(ResponseCodes.SUCCESS);
            response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.SUCCESS));
            LoggingUtils.DebugInfo(request.getTitle() + " By " + request.getAuthor() + " Saved Successfully");
        } catch (Exception ex) {
            LoggingUtils.DebugInfo("An exception has occurred: " + ex);
            ex.printStackTrace();
        }

        return response;
    }

    @Override
    public Page<BookResponse> getAllBooks(int page, int size, String title, String author, Boolean available) {
        LoggingUtils.DebugInfo("Fetching paginated books in the library...");

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedDate").descending());

        Page<Book> bookPage = bookRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(title)) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(author)) {
                predicates.add(cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%"));
            }
            if (available != null) {
                predicates.add(cb.equal(root.get("available"), available));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Page<BookResponse> responsePage = bookPage.map(book -> {
            BookResponse br = new BookResponse();
            br.setId(book.getId());
            br.setAuthor(book.getAuthor());
            br.setTitle(book.getTitle());
            br.setAvailable(book.getAvailable());

            if (book.getPublishedDate() != null) {
                br.setPublishedDate(book.getPublishedDate().format(formatter));
            }

            return br;
        });

        return responsePage;
    }

    @Override
    public ResponseDto update(UUID id, BookRequest bookRequest) {
        ResponseDto response = new ResponseDto();
        response.setResponseCode(ResponseCodes.ERROR);
        response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.ERROR));

        try {
            Optional<Book> optionalBook = bookRepository.findById(id);
            if (optionalBook.isEmpty()) {
                LoggingUtils.DebugInfo("Book not found for id: " + id);
                return response;
            }

            Book book = optionalBook.get();
            book.setTitle(bookRequest.getTitle());
            book.setAuthor(bookRequest.getAuthor());

            if (bookRequest.getPublishedDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                book.setPublishedDate(LocalDateTime.parse(bookRequest.getPublishedDate(), formatter));
            }

            if (bookRequest.getAvailable() != null) {
                book.setAvailable(bookRequest.getAvailable());
            }

            bookRepository.save(book);
            response.setResponseCode(ResponseCodes.SUCCESS);
            response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.SUCCESS));
            LoggingUtils.DebugInfo("Book updated successfully: " + id);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public ResponseDto delete(UUID id) {
        ResponseDto response = new ResponseDto();
        response.setResponseCode(ResponseCodes.FAILED);
        response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.FAILED));

        try {
            if (!bookRepository.existsById(id)) {
                LoggingUtils.DebugInfo("Book not found");
                return response;
            }

            bookRepository.deleteById(id);
            response.setResponseCode(ResponseCodes.SUCCESS);
            response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.SUCCESS));

            LoggingUtils.DebugInfo("Book deleted successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

}
