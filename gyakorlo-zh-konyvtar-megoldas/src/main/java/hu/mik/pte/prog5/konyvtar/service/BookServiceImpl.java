package hu.mik.pte.prog5.konyvtar.service;

import hu.mik.pte.prog5.konyvtar.entity.Book;
import hu.mik.pte.prog5.konyvtar.entity.BookGenre;
import hu.mik.pte.prog5.konyvtar.repository.BookRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    // 3: interface-re hivatkozunk, a "memory" implementaciot a @Qualifier valasztja ki
    private final BookRepository bookRepository;

    public BookServiceImpl(@Qualifier("memory") BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // 8-4 (bonusz): a genre enum, a szerepek String-ek, ezert kell a .name()
    @Override
    @PostFilter("@securityService.getRolesAsString().contains(filterObject.genre.name())")
    public List<Book> listAll() {
        return this.bookRepository.findAll();
    }

    // 6-3, 8-2: a szerkesztes ezzel tolti be a konyvet
    @Override
    @PreAuthorize("hasRole('EDITOR')")
    public Optional<Book> findByUid(String uid) {
        return this.bookRepository.findByUid(uid);
    }

    // 8-2
    @Override
    @PreAuthorize("hasRole('EDITOR')")
    public Book save(Book book) {
        return this.bookRepository.save(book);
    }

    // 8-3
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String uid) {
        this.bookRepository.deleteByUid(uid);
    }

    // 3: az enum osszes erteke; 8-2: az urlap hasznalja
    @Override
    @PreAuthorize("hasRole('EDITOR')")
    public List<BookGenre> listGenres() {
        return Arrays.asList(BookGenre.values());
    }
}
