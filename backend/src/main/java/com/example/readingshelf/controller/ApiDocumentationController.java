package com.example.readingshelf.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
public class ApiDocumentationController {
    
    @GetMapping(value = "/reading-shelf-api.yaml", produces = "application/yaml")
    public ResponseEntity<Resource> getApiSpecification() {
        Resource specification = new ClassPathResource("yaml/reading-shelf-api.yaml");
        return ResponseEntity.ok(specification);
    }
}
