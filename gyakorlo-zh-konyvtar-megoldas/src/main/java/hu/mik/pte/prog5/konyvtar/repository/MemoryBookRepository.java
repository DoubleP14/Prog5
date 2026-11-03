package hu.mik.pte.prog5.konyvtar.repository;

import hu.mik.pte.prog5.konyvtar.entity.Book;
import hu.mik.pte.prog5.konyvtar.entity.BookGenre;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository("memory")
public class MemoryBookRepository implements BookRepository {

    private final List<Book> books = new ArrayList<>(List.of(
            new Book(UUID.randomUUID().toString(), "Egri csillagok", "Gárdonyi Géza", 1899, BookGenre.NOVEL),
            new Book(UUID.randomUUID().toString(), "Tíz kicsi indián", "Agatha Christie", 1939, BookGenre.CRIME),
            new Book(UUID.randomUUID().toString(), "Dűne", "Frank Herbert", 1965, BookGenre.SCIFI),
            new Book(UUID.randomUUID().toString(), "Sapiens", "Yuval Noah Harari", 2011, BookGenre.HISTORY),
            new Book(UUID.randomUUID().toString(), "Toldi", "Arany János", 1847, BookGenre.POETRY)
    ));

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(this.books);
    }

    @Override
    public Optional<Book> findByUid(String uid) {
        return this.books.stream().filter(b -> b.getUid().equals(uid)).findFirst();
    }

    @Override
    public Book save(Book book) {
        if (book.getUid() == null) {
            book.setUid(UUID.randomUUID().toString());
        } else {
            this.books.removeIf(b -> b.getUid().equals(book.getUid()));
        }
        this.books.add(book);
        return book;
    }

    @Override
    public void deleteByUid(String uid) {
        this.books.removeIf(b -> b.getUid().equals(uid));
    }
}
