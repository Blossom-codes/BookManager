package com.balancee.BookManager.controller;

import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.book.BookRequest;
import com.balancee.BookManager.dto.book.BookResponse;
import com.balancee.BookManager.dto.user.UserInfo;
import com.balancee.BookManager.service.BookService;
import com.balancee.BookManager.utils.LocaleHandler;
import com.balancee.BookManager.utils.ResponseCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BookControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    private UserInfo adminUser() {
        UserInfo userInfo = new UserInfo();
        userInfo.setAuthorities("ROLE_ADMIN");
        return userInfo;
    }

    private BookRequest sampleBookRequest() {
        BookRequest request = new BookRequest();
        request.setTitle("Sample Book");
        request.setAuthor("Author");
        request.setAvailable(true);
        request.setPublishedDate("2023-01-01 00:00:00");
        return request;
    }

    @Test
    @DisplayName("saveBook - should return 200 when saved successfully by admin")
    void saveBook_shouldReturnSuccess() {
        // Given
        BookRequest bookRequest = sampleBookRequest();
        ResponseDto mockResponse = new ResponseDto(ResponseCodes.SUCCESS, LocaleHandler.getMessage(ResponseCodes.SUCCESS), null, null);

        BookController controllerSpy = Mockito.spy(bookController);
        doReturn(adminUser()).when(controllerSpy).validateToken(request);
        when(bookService.save(bookRequest)).thenReturn(mockResponse);

        // When
        ResponseEntity<?> responseEntity = controllerSpy.saveBook(request, response, bookRequest);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseDto body = (ResponseDto) responseEntity.getBody();
        assertEquals(ResponseCodes.SUCCESS, body.getResponseCode());
        assertEquals(LocaleHandler.getMessage(ResponseCodes.SUCCESS), body.getResponseMessage());
    }

    @Test
    @DisplayName("updateBook - should return 200 when updated successfully by admin")
    void updateBook_shouldReturnSuccess() {
        // Given
        BookRequest bookRequest = sampleBookRequest();
        ResponseDto mockResponse = new ResponseDto(ResponseCodes.SUCCESS, LocaleHandler.getMessage(ResponseCodes.SUCCESS), null, null);
        UUID bookId = UUID.randomUUID(); // Create and reuse the same UUID

        BookController controllerSpy = Mockito.spy(bookController);
        doReturn(adminUser()).when(controllerSpy).validateToken(request);
        when(bookService.update(bookId, bookRequest)).thenReturn(mockResponse);

        // When
        ResponseEntity<?> responseEntity = controllerSpy.updateBook(request, response, bookId, bookRequest);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseDto body = (ResponseDto) responseEntity.getBody();
        assertEquals(ResponseCodes.SUCCESS, body.getResponseCode());
        assertEquals(LocaleHandler.getMessage(ResponseCodes.SUCCESS), body.getResponseMessage());
    }


    @Test
    @DisplayName("getBook - should return paginated list of books with status 200")
    void getBooks_shouldReturnPaginatedBooks() {
        // Given
        int page = 0;
        int size = 2;
        String title = "Java";
        String author = "John";
        Boolean available = true;

        BookResponse book1 = new BookResponse();
        book1.setTitle("Java 101");
        book1.setAuthor("John");

        BookResponse book2 = new BookResponse();
        book2.setTitle("Advanced Java");
        book2.setAuthor("John");

        List<BookResponse> bookList = List.of(book1, book2);
        Page<BookResponse> mockPage = new PageImpl<>(bookList);

        when(bookService.getAllBooks(page, size, title, author, available)).thenReturn(mockPage);

        // When
        ResponseEntity<?> responseEntity = bookController.getBook(request, response, page, size, title, author, available);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Page<BookResponse> body = (Page<BookResponse>) responseEntity.getBody();
        assertEquals(2, body.getContent().size());
        assertEquals("Java 101", body.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("getBook - filter by title only")
    void getBooks_filterByTitleOnly() {
        String title = "Spring Boot";
        int page = 0, size = 10;

        BookResponse book = new BookResponse();
        book.setTitle("Spring Boot in Action");
        book.setAuthor("Craig Walls");

        Page<BookResponse> mockPage = new PageImpl<>(List.of(book));
        when(bookService.getAllBooks(page, size, title, "", null)).thenReturn(mockPage);

        ResponseEntity<?> responseEntity = bookController.getBook(request, response, page, size, title, "", null);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Page<BookResponse> body = (Page<BookResponse>) responseEntity.getBody();
        assertEquals(1, body.getTotalElements());
        assertEquals("Spring Boot in Action", body.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("getBook - filter by author only")
    void getBooks_filterByAuthorOnly() {
        String author = "Joshua Bloch";
        int page = 0, size = 10;

        BookResponse book = new BookResponse();
        book.setTitle("Effective Java");
        book.setAuthor("Joshua Bloch");

        Page<BookResponse> mockPage = new PageImpl<>(List.of(book));
        when(bookService.getAllBooks(page, size, "", author, null)).thenReturn(mockPage);

        ResponseEntity<?> responseEntity = bookController.getBook(request, response, page, size, "", author, null);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Page<BookResponse> body = (Page<BookResponse>) responseEntity.getBody();
        assertEquals(1, body.getTotalElements());
        assertEquals("Effective Java", body.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("getBook - filter by availability only")
    void getBooks_filterByAvailabilityOnly() {
        Boolean available = true;
        int page = 0, size = 10;

        BookResponse book = new BookResponse();
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setAvailable(true);

        Page<BookResponse> mockPage = new PageImpl<>(List.of(book));
        when(bookService.getAllBooks(page, size, "", "", available)).thenReturn(mockPage);

        ResponseEntity<?> responseEntity = bookController.getBook(request, response, page, size, "", "", available);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Page<BookResponse> body = (Page<BookResponse>) responseEntity.getBody();
        assertEquals(1, body.getTotalElements());
        assertTrue(body.getContent().get(0).getAvailable());
    }

    @Test
    @DisplayName("deleteBook - should return 200 on successful delete")
    void deleteBook_shouldReturnSuccess() {
        // Given
        UUID bookId = UUID.randomUUID();
        ResponseDto mockResponse = new ResponseDto(ResponseCodes.SUCCESS, LocaleHandler.getMessage(ResponseCodes.SUCCESS), null, null);

        BookController controllerSpy = Mockito.spy(bookController);
        doReturn(adminUser()).when(controllerSpy).validateToken(request);
        when(bookService.delete(bookId)).thenReturn(mockResponse);

        // When
        ResponseEntity<?> responseEntity = controllerSpy.deleteBook(request, response, bookId);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseDto body = (ResponseDto) responseEntity.getBody();
        assertEquals(ResponseCodes.SUCCESS, body.getResponseCode());
        assertEquals(LocaleHandler.getMessage(ResponseCodes.SUCCESS), body.getResponseMessage());
    }
}
