package com.esen.bookstore.service;

import com.esen.bookstore.model.Book;
import com.esen.bookstore.model.Bookstore;
import com.esen.bookstore.repository.BookRepository;
import com.esen.bookstore.repository.BookstoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    private final BookstoreRepository bookstoreRepository;

    private final BookstoreService bookstoreService;

    public void removeBookFromInventories(Book book) {
        bookstoreRepository.findAll()
                .forEach(bookstore -> {
                    bookstore.getInventory().remove(book);
                    bookstoreRepository.save(bookstore);
                });
    }

    public void save(Book book) {
        bookRepository.save(book);
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Cannot find book with id " + id);
        }

        var book = bookRepository.findById(id).get();

        bookstoreService.removeBookFromInventories(book);

        bookRepository.deleteById(id);
    }

    public Book update(Long id, String title, String author, String publisher, Double price) {
        if (Stream.of(title, author, publisher, price).allMatch(Objects::isNull)) {
            throw new IllegalArgumentException("At least one input is required");
        }

        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Cannot find book with id " + id);
        }

        var book = bookRepository.findById(id).get();

        if (title != null) { book.setTitle(title); }
        if (author != null) { book.setAuthor(author); }
        if (publisher != null) { book.setPublisher(publisher); }
        if (price != null) { book.setPrice(price); }

        return bookRepository.save(book);
    }

    public Map<String, Double> findPrices(Long id)
    {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Cannot find book with id " + id);
        }

        var book = bookRepository.findById(id).get();
        var bookstores = bookstoreService.findAll();


        Map<String, Double> price = new HashMap<String, Double>();

        for (Bookstore bookstore : bookstores)
        {
            price.put(bookstore.getLocation(), bookstore.getPriceModifier() * book.getPrice());
        }

        return price;
    }

    public List<Book> findByAuthorOrTitleOrPublisher(String author, String title, String publisher)
    {
        return bookRepository.findAll().stream()
                .filter(book -> {
                    if (title!=null) {
                        return book.getTitle().equals(title);
                    }
                    if (author!=null) {
                        return book.getAuthor().equals(author);
                    }
                    if (publisher!=null) {
                        return book.getPublisher().equals(publisher);
                    }
                    return true;
                })
                .toList();
    }
}
