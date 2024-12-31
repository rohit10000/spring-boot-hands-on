package com.example.springbootapplication.controller;

import com.example.springbootapplication.entity.Book;
import com.example.springbootapplication.repository.BooksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class BooksController {

    @Autowired
    private BooksRepository repository;

    @PostMapping("/create")
    public String newBook(@RequestBody Book newBook) {
        repository.save(newBook);
        return "Added";
    }

    @GetMapping("/get/{id}")
    public Book findOne(@PathVariable Long id) {
        return repository.findById(id);
    }

}
