package hu.mik.pte.prog5.konyvtar.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import hu.mik.pte.prog5.konyvtar.entity.Book;
import hu.mik.pte.prog5.konyvtar.entity.BookGenre;
import hu.mik.pte.prog5.konyvtar.service.BookService;
import hu.mik.pte.prog5.konyvtar.ui.MainLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "books/new", layout = MainLayout.class)             // 6-4: uj konyv
@RouteAlias(value = "books/:uid/edit", layout = MainLayout.class)  // 6-4: szerkesztes
@RolesAllowed("EDITOR")                                            // 8-2
public class BookFormView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

    private final BookService bookService;
    // 6-2: a Book annotacioit is figyelembe veszi
    private final BeanValidationBinder<Book> binder = new BeanValidationBinder<>(Book.class);

    private Book book;

    public BookFormView(BookService bookService) {
        this.bookService = bookService;

        // 6-2: mezok
        TextField title = new TextField(this.getTranslation("book.title"));
        TextField author = new TextField(this.getTranslation("book.author"));
        IntegerField publishYear = new IntegerField(this.getTranslation("book.publishYear"));
        RadioButtonGroup<BookGenre> genre = new RadioButtonGroup<>(this.getTranslation("book.genre"));
        genre.setItems(this.bookService.listGenres());
        genre.setItemLabelGenerator(g -> this.getTranslation("book.genre." + g.name()));

        // 6-2: bindolas property nev alapjan. A null ertekre a validator igazat ad, azt a @NotNull kezeli.
        this.binder.forField(title).bind("title");
        this.binder.forField(author).bind("author");
        this.binder.forField(publishYear)
                .withValidator(y -> y == null || (y >= 1450 && y <= 2100), this.getTranslation("validation.year"))
                .bind("publishYear");
        this.binder.forField(genre).bind("genre");

        Button save = new Button(this.getTranslation("book.save"), e -> this.save());
        Button cancel = new Button(this.getTranslation("book.cancel"),
                e -> UI.getCurrent().navigate(BookListView.class));

        this.add(title, author, publishYear, genre, new HorizontalLayout(save, cancel));
    }

    // 6-3: ha van uid az utvonalban, betoltjuk a konyvet, kulonben uj konyv
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.book = event.getRouteParameters().get("uid")
                .flatMap(this.bookService::findByUid)
                .orElseGet(Book::new);
        this.binder.readBean(this.book);
    }

    // 6-2: helyes adatnal mentes + lista, hibasnal figyelmeztetes
    private void save() {
        if (this.binder.writeBeanIfValid(this.book)) {
            this.bookService.save(this.book);
            UI.getCurrent().navigate(BookListView.class);
        } else {
            Notification.show(this.getTranslation("validation.form"));
        }
    }

    // 7: a cim attol fugg, hogy uj konyv vagy szerkesztes
    @Override
    public String getPageTitle() {
        return this.getTranslation(this.book == null || this.book.getUid() == null
                ? "title.book.new" : "title.book.edit");
    }
}
