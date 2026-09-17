package com.example.readingshelf.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example.readingshelf.generated.api.BooksApi;
import com.example.readingshelf.generated.model.Book;
import com.example.readingshelf.generated.model.CreateBookRequest;
import com.example.readingshelf.generated.model.ReadingStatus;
import com.example.readingshelf.generated.model.UpdateBookRequest;
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

    @Override
    public ResponseEntity<List<Book>> listBooks(ReadingStatus status) {
        return ResponseEntity.ok(bookService.listBooks(status));
    }

    @Override
    public ResponseEntity<Book> getBookById(Long id) {
        return bookService.getBookById(id)
            .map(book -> ResponseEntity.ok(book))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Book> updateBook(Long id, UpdateBookRequest updateBookRequest) {
        return bookService.updateBook(id, updateBookRequest)
            .map(book -> ResponseEntity.ok(book))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> deleteBook(Long id) {
        if (bookService.deleteBook(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
