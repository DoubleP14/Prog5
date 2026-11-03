package hu.mik.pte.prog5.konyvtar.repository;

import hu.mik.pte.prog5.konyvtar.entity.Book;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("sql")
public class SqlBookRepository implements BookRepository {

    @Override
    public List<Book> findAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Book> findByUid(String uid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Book save(Book book) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteByUid(String uid) {
        throw new UnsupportedOperationException();
    }
}
