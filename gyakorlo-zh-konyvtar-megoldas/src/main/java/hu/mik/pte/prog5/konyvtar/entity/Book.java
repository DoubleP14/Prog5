package hu.mik.pte.prog5.konyvtar.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class Book {

    private String uid;

    // 6-1: a {book.required} a ValidationMessages fajlokbol jon
    @NotBlank(message = "{book.required}")
    private String title;

    @NotBlank(message = "{book.required}")
    private String author;

    @NotNull(message = "{book.required}")
    private Integer publishYear;

    @NotNull(message = "{book.required}")
    private BookGenre genre;

    public Book() {
    }

    public Book(String uid, String title, String author, Integer publishYear, BookGenre genre) {
        this.uid = uid;
        this.title = title;
        this.author = author;
        this.publishYear = publishYear;
        this.genre = genre;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(Integer publishYear) {
        this.publishYear = publishYear;
    }

    public BookGenre getGenre() {
        return genre;
    }

    public void setGenre(BookGenre genre) {
        this.genre = genre;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(uid, book.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}
