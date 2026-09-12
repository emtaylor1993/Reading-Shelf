package com.example.readingshelf.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example.readingshelf.generated.api.BooksApi;
import com.example.readingshelf.generated.model.Book;
import com.example.readingshelf.generated.model.CreateBookRequest;
import com.example.readingshelf.service.BookService;

@RestController 
public class BookController implements BooksApi {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Override 
    public ResponseEntity<Book> createBook(CreateBookRequest createBookRequest) {
        Book book = bookService.createBook(createBookRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }
}
