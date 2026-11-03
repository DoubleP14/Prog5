package hu.mik.pte.prog5.konyvtar.service;

import hu.mik.pte.prog5.konyvtar.entity.Book;
import hu.mik.pte.prog5.konyvtar.entity.BookGenre;

import java.util.List;
import java.util.Optional;

public interface BookService {

    List<Book> listAll();

    Optional<Book> findByUid(String uid);

    Book save(Book book);

    void delete(String uid);

    List<BookGenre> listGenres();
}
