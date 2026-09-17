package com.example.readingshelf;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsAnEmptyShelf() throws Exception {
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void listsCreatedBooksInIdOrder() throws Exception {
        createToReadBook();
        createReadingBook();

        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("Green Eggs and Ham"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].title").value("The Hobbit"));
    }

    @Test
    void filterBooksByStatus() throws Exception {
        createToReadBook();
        createReadingBook();

        mockMvc.perform(get("/api/books").param("status", "READING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("The Hobbit"))
            .andExpect(jsonPath("$[0].status").value("READING"));
    }

    @Test
    void returnsEmptyListWhenNoBooksMatchStatus() throws Exception {
        createToReadBook();
        createReadingBook();

        mockMvc.perform(get("/api/books").param("status", "FINISHED"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void rejectsUnknownStatus() throws Exception {
        mockMvc.perform(get("/api/books").param("status", "UNKNOWN"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void retrievesAnExistingBook() throws Exception {
        createToReadBook();

        mockMvc.perform(get("/api/books/{id}", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Green Eggs and Ham"))
            .andExpect(jsonPath("$.author").value("Dr. Seuss"))
            .andExpect(jsonPath("$.status").value("TO_READ"));
    }

    @Test
    void returns404ForMissingBook() throws Exception {
        mockMvc.perform(get("/api/books/{id}", 999))
            .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidBookIds() throws Exception {
        mockMvc.perform(get("/api/books/{id}", "abc"))
            .andExpect(status().isBadRequest());
        
        mockMvc.perform(get("/api/books/{id}", 0))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updatesBookAndPreservesItsId() throws Exception {
        createToReadBook();

        mockMvc.perform(put("/api/books/{id}", 1)
            .contentType(APPLICATION_JSON)
            .content("""
                {
                    "title": "The Hobbit",
                    "author": "J.R.R. Tolkien",
                    "status": "FINISHED"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("The Hobbit"))
            .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$.status").value("FINISHED"));

        mockMvc.perform(get("/api/books/{id}", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("The Hobbit"))
            .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$.status").value("FINISHED"));

        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void returns404WhenUpdatingMissingBook() throws Exception {
        mockMvc.perform(put("/api/books/{id}", 999)
            .contentType(APPLICATION_JSON)
            .content("""
                {
                    "title": "The Hobbit",
                    "author": "J.R.R. Tolkien",
                    "status": "READING"
                }
                """))
            .andExpect(status().isNotFound());
        
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void rejectsInvalidUpdateWithoutChangingBook() throws Exception {
        createToReadBook();

        mockMvc.perform(put("/api/books/{id}", 1)
            .contentType(APPLICATION_JSON)
            .content("""
                {
                    "title": "",
                    "author": "Changed Author",
                    "status": "FINISHED"
                }
                """))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/books/{id}", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Green Eggs and Ham"))
            .andExpect(jsonPath("$.author").value("Dr. Seuss"))
            .andExpect(jsonPath("$.status").value("TO_READ"));
    }

    @Test
    void deletesBookAndLeavesOtherBooksUnchanged() throws Exception {
        createToReadBook();
        createReadingBook();

        mockMvc.perform(delete("/api/books/{id}", 1))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
        
        mockMvc.perform(get("/api/books/{id}", 1))
            .andExpect(status().isNotFound());
        
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].title").value("The Hobbit"));
        
        mockMvc.perform(delete("/api/books/{id}", 1))
            .andExpect(status().isNotFound());
    }

    @Test
    void returns404WhenDeletingMissingBook() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", 999))
            .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidDeleteIdsWithoutRemovingBooks() throws Exception {
        createToReadBook();

        mockMvc.perform(delete("/api/books/{id}", "abc"))
            .andExpect(status().isBadRequest());
        
        mockMvc.perform(delete("/api/books/{id}", 0))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/books/{id}", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Green Eggs and Ham"));
    }

    private void createToReadBook() throws Exception {
        mockMvc.perform(post("/api/books")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                    "title": "Green Eggs and Ham",
                    "author": "Dr. Seuss",
                    "status": "TO_READ"
                }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    private void createReadingBook() throws Exception {
        mockMvc.perform(post("/api/books")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                    "title": "The Hobbit",
                    "author": "J.R.R. Tolkien",
                    "status": "READING"
                }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2));
    }
}
