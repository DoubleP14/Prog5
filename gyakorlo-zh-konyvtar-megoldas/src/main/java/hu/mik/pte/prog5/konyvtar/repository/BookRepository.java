package hu.mik.pte.prog5.konyvtar.repository;

import hu.mik.pte.prog5.konyvtar.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    List<Book> findAll();

    Optional<Book> findByUid(String uid);

    Book save(Book book);

    void deleteByUid(String uid);
}
