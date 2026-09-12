package com.example.readingshelf.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.readingshelf.generated.model.Book;
import com.example.readingshelf.generated.model.CreateBookRequest;
import com.example.readingshelf.generated.model.ReadingStatus;
import com.example.readingshelf.service.BookService;

@WebMvcTest(BookController.class)
public class BookControllerTest {
    
    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean 
    private BookService bookService;

    @Test
    void createsBookAndReturns201() throws Exception {
        Book createdBook = new Book(42L, "Green Eggs and Ham", "Dr. Seuss", ReadingStatus.TO_READ);
        when(bookService.createBook(any(CreateBookRequest.class))).thenReturn(createdBook);
        mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "title": "Green Eggs and Ham",
                        "author": "Dr. Seuss",
                        "status": "TO_READ"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.title").value("Green Eggs and Ham"))
            .andExpect(jsonPath("$.author").value("Dr. Seuss"))
            .andExpect(jsonPath("$.status").value("TO_READ"));
        
        ArgumentCaptor<CreateBookRequest> captor = ArgumentCaptor.forClass(CreateBookRequest.class);
        verify(bookService).createBook(captor.capture());
        CreateBookRequest receivedRequest = captor.getValue();
        assertEquals("Green Eggs and Ham", receivedRequest.getTitle());
        assertEquals("Dr. Seuss", receivedRequest.getAuthor());
        assertEquals(ReadingStatus.TO_READ, receivedRequest.getStatus());
    }

    @Test
    void rejectsEmptyTitleWithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "title": "",
                        "author": "Dr. Seuss",
                        "status": "TO_READ"
                    }
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void rejectsMissingTitleWithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "author": "Dr. Seuss",
                        "status": "TO_READ"
                    }
                    """))
            .andExpect(status().isBadRequest());
        
        verifyNoInteractions(bookService);
    }
}
