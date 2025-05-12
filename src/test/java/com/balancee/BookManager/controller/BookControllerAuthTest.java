package com.balancee.BookManager.controller;


import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.book.BookRequest;
import com.balancee.BookManager.dto.user.UserInfo;
import com.balancee.BookManager.service.BookService;
import com.balancee.BookManager.utils.LocaleHandler;
import com.balancee.BookManager.utils.ResponseCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerAuthTest {

    @Mock
    private BookService bookService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Spy
    private LocaleHandler localeHandler; // if needed

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private BaseController baseController;

    @InjectMocks
    private BookController bookController;

    private final String bookJson = """
        {
            "title": "Unauthorized Book",
            "author": "Anonymous",
            "available": true,
            "publishedDate": "2023-01-01 00:00:00"
        }
    """;

    @Test
    @DisplayName("POST /books/save - No Authorization header should return 403")
    void postBooks_NoAuthorizationHeader_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/books/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /books/save - Malformed Bearer token should return 403")
    void postBooks_MalformedBearerToken_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/books/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer")
                        .content(bookJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /books/save - Invalid JWT should return 403")
    void postBooks_InvalidJwtToken_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/books/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .content(bookJson))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("POST /books/save - as users, should return 403")
    void saveBook_asUser_shouldReturn403() {
        // Given
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle("Unauthorized Book");
        bookRequest.setAuthor("Anonymous");
        bookRequest.setAvailable(true);
        bookRequest.setPublishedDate("2023-01-01 00:00:00");

        // Fake non-admin user
        UserInfo userInfo = new UserInfo();
        userInfo.setAuthorities("ROLE_USER");

        // When validateToken is called, return the mocked user
        BookController controllerSpy = Mockito.spy(bookController);
        doReturn(userInfo).when(controllerSpy).validateToken(request);

        // When
        ResponseEntity<?> responseEntity = controllerSpy.saveBook(request, response, bookRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        ResponseDto body = (ResponseDto) responseEntity.getBody();
        assertEquals(ResponseCodes.USER_NOT_AUTHORIZED, body.getResponseCode());
    }

    @Test
    @DisplayName("POST /books/save - as Admin, should return 200")
    void saveBook_asAdmin_shouldReturn200() {
        // Given
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle("Authorized Book");
        bookRequest.setAuthor("Admin");
        bookRequest.setAvailable(true);
        bookRequest.setPublishedDate("2023-01-01 00:00:00");

        // Fake admin user
        UserInfo userInfo = new UserInfo();
        userInfo.setAuthorities("ROLE_ADMIN");

        // Mock the service response
        ResponseDto mockedResponseDto = new ResponseDto();
        mockedResponseDto.setResponseCode(ResponseCodes.SUCCESS);
        mockedResponseDto.setResponseMessage("Book saved successfully");


        // When validateToken is called, return the mocked admin
        BookController controllerSpy = Mockito.spy(bookController);
        doReturn(userInfo).when(controllerSpy).validateToken(request);

        // Mock the bookService to return the mocked ResponseDto
        when(bookService.save(bookRequest)).thenReturn(mockedResponseDto);

        // When
        ResponseEntity<?> responseEntity = controllerSpy.saveBook(request, response, bookRequest);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseDto body = (ResponseDto) responseEntity.getBody();
        assertEquals(ResponseCodes.SUCCESS, body.getResponseCode());
        assertEquals("Book saved successfully", body.getResponseMessage());
    }


}
