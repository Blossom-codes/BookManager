package com.balancee.BookManager.controller;

import com.balancee.BookManager.dto.book.BookRequest;
import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.book.BookResponse;
import com.balancee.BookManager.dto.user.UserInfo;
import com.balancee.BookManager.entity.Book;
import com.balancee.BookManager.service.BookService;
import com.balancee.BookManager.service.UserService;
import com.balancee.BookManager.utils.LocaleHandler;
import com.balancee.BookManager.utils.LoggingUtils;
import com.balancee.BookManager.utils.ResponseCodes;
import com.balancee.BookManager.utils.Roles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Validated
public class BookController extends BaseController {

    private final UserService userService;
    private final BookService bookService;

    @Operation(
            summary = "Save a new book",
            description = "Save a book in the database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Save was successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/save")
    public ResponseEntity<?> saveBook(HttpServletRequest request, HttpServletResponse response, @RequestBody @Valid BookRequest bookRequest) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setResponseCode(ResponseCodes.ERROR);
        responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.ERROR));

        UserInfo userInfo = new UserInfo();
        try {
            LoggingUtils.DebugInfo("Saving a new book - Title: " + bookRequest.getTitle());

            userInfo = this.validateToken(request);
            if (userInfo == null || userInfo.getAuthorities().equalsIgnoreCase(Roles.ROLE_USER.name())) {
                LoggingUtils.DebugInfo("An error occurred: unauthorized access");
                responseDto.setResponseCode(ResponseCodes.USER_NOT_AUTHORIZED);
                responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.USER_NOT_AUTHORIZED));
                return ResponseEntity.badRequest().body(responseDto);
            }


            responseDto = bookService.save(bookRequest);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            LoggingUtils.DebugInfo("Error during saving of book: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }
    }

    @Operation(
            summary = "Get all books",
            description = "Fetch books from the database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetch was successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getBooks")
    public ResponseEntity<?> getBook(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                     @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                     @RequestParam(name = "title", required = false, defaultValue = "") String title,
                                     @RequestParam(name = "author", required = false, defaultValue = "") String author,
                                     @RequestParam(name = "available", required = false, defaultValue = "") Boolean available

                                     ) {
        Page<BookResponse> bookList = null;
        try {
            bookList = bookService.getAllBooks(page, size, title, author,available);
            return ResponseEntity.ok(bookList);
        } catch (Exception e) {
            LoggingUtils.DebugInfo("Error occurred during fetching of books: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(bookList);
        }
    }
    @Operation(
            summary = "Update a book",
            description = "Update details of a book"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update was successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBook(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID id, @RequestBody @Valid BookRequest bookRequest) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setResponseCode(ResponseCodes.ERROR);
        responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.ERROR));

        UserInfo userInfo = new UserInfo();
        try {
            LoggingUtils.DebugInfo("Updating a book - id: "+id +"-"+ bookRequest.getTitle());

            userInfo = this.validateToken(request);
            if (userInfo == null || userInfo.getAuthorities().equalsIgnoreCase(Roles.ROLE_USER.name())) {
                LoggingUtils.DebugInfo("An error occurred: unauthorized access");
                responseDto.setResponseCode(ResponseCodes.USER_NOT_AUTHORIZED);
                responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.USER_NOT_AUTHORIZED));
                return ResponseEntity.badRequest().body(responseDto);
            }


            responseDto = bookService.update(id,bookRequest);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            LoggingUtils.DebugInfo("Error occurred during updating the book: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }
    }

    @Operation(
            summary = "Delete a book",
            description = "Update details of a book"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete was successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBook(HttpServletRequest request, HttpServletResponse response, @PathVariable UUID id) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setResponseCode(ResponseCodes.ERROR);
        responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.ERROR));

        UserInfo userInfo = new UserInfo();
        try {
            LoggingUtils.DebugInfo("Deleting a book - id: "+id);

            userInfo = this.validateToken(request);
            if (userInfo == null || userInfo.getAuthorities().equalsIgnoreCase(Roles.ROLE_USER.name())) {
                LoggingUtils.DebugInfo("An error occurred: unauthorized access");
                responseDto.setResponseCode(ResponseCodes.USER_NOT_AUTHORIZED);
                responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.USER_NOT_AUTHORIZED));
                return ResponseEntity.badRequest().body(responseDto);
            }


            responseDto = bookService.delete(id);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            LoggingUtils.DebugInfo("Error occurred during updating the book: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }
    }
}
