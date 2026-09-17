package com.example.readingshelf.service;

import com.example.readingshelf.generated.model.Book;
import com.example.readingshelf.generated.model.CreateBookRequest;
import com.example.readingshelf.generated.model.ReadingStatus;
import com.example.readingshelf.generated.model.UpdateBookRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public List<Book> listBooks(ReadingStatus status) {
        return books.values().stream()
            .filter(book -> status == null || book.getStatus() == status)
            .sorted(Comparator.comparing(Book::getId))
            .toList();
    }

    public Optional<Book> getBookById(Long id) {
        return Optional.ofNullable(books.get(id));
    }

    public Optional<Book> updateBook(Long id, UpdateBookRequest request) {
        Book updatedBook = books.computeIfPresent(
            id,
            (existingId, existingBook) -> new Book(
                existingId,
                request.getTitle(),
                request.getAuthor(),
                request.getStatus()
            )
        );

        return Optional.ofNullable(updatedBook);
    }

    public boolean deleteBook(Long id) {
        return books.remove(id) != null;
    }
}
