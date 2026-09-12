package com.example.readingshelf.service;

import com.example.readingshelf.generated.model.Book;
import com.example.readingshelf.generated.model.CreateBookRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service 
public class BookService {
    
    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public Book createBook(CreateBookRequest request) {
        long id = nextId.getAndIncrement();
        Book book = new Book(id, request.getTitle(), request.getAuthor(), request.getStatus());
        books.put(id, book);
        return book;
    }
}
