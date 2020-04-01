package com.example.library_fullstack.data;

import com.example.library_fullstack.entity.LibraryBook;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface LibraryBookRepository extends CrudRepository<LibraryBook,Integer> {
    List<LibraryBook> findAll();

    //Search for a book or books with at least a keyword and then a list of books will be shown
    List<LibraryBook> findByTitleContainsIgnoreCase(String bookName);
}
