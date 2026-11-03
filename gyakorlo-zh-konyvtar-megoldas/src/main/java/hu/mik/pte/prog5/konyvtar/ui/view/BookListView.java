package hu.mik.pte.prog5.konyvtar.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import hu.mik.pte.prog5.konyvtar.entity.Book;
import hu.mik.pte.prog5.konyvtar.service.BookService;
import hu.mik.pte.prog5.konyvtar.service.SecurityService;
import hu.mik.pte.prog5.konyvtar.ui.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "books", layout = MainLayout.class)   // 5-2
@RouteAlias(value = "", layout = MainLayout.class)   // 5-2: belepes utan a "/" cimre visz a Vaadin
@PermitAll
public class BookListView extends VerticalLayout implements HasDynamicTitle {

    private final BookService bookService;
    private final Grid<Book> grid = new Grid<>(Book.class, false);
    private final boolean admin;
    private final boolean editor;

    public BookListView(BookService bookService, SecurityService securityService) {
        this.bookService = bookService;
        List<String> roles = securityService.getRolesAsString();
        this.admin = roles.contains("ADMIN");
        this.editor = roles.contains("EDITOR");

        // 5-1: Uj konyv gomb; 8-2: csak EDITOR
        Button newBook = new Button(this.getTranslation("book.new"),
                e -> UI.getCurrent().navigate(BookFormView.class));
        newBook.setVisible(this.editor);

        // 5-1: az oszlopok sorrendje kotelezo
        var idColumn = this.grid.addColumn(Book::getUid).setHeader(this.getTranslation("book.id"));
        this.grid.addColumn(Book::getTitle).setHeader(this.getTranslation("book.title"));
        this.grid.addColumn(Book::getAuthor).setHeader(this.getTranslation("book.author"));
        this.grid.addColumn(Book::getPublishYear).setHeader(this.getTranslation("book.publishYear"));
        this.grid.addColumn(b -> this.getTranslation("book.genre." + b.getGenre().name()))
                .setHeader(this.getTranslation("book.genre"));
        var actionsColumn = this.grid.addComponentColumn(this::createActions);

        idColumn.setVisible(this.admin);                  // 5-1
        actionsColumn.setVisible(this.admin || this.editor);

        this.grid.setEmptyStateText(this.getTranslation("book.list.empty"));   // 5-1

        this.refresh();
        this.setSizeFull();
        this.add(newBook, this.grid);
    }

    private HorizontalLayout createActions(Book book) {
        // 6-3; 8-2: csak EDITOR
        Button edit = new Button(this.getTranslation("book.edit"),
                e -> UI.getCurrent().navigate("books/" + book.getUid() + "/edit"));
        edit.setVisible(this.editor);

        // 8-3: csak ADMIN
        Button delete = new Button(this.getTranslation("book.delete"), e -> this.confirmDelete(book));
        delete.setVisible(this.admin);

        return new HorizontalLayout(edit, delete);
    }

    // 8-3: megerosites utan torles es frissites
    private void confirmDelete(Book book) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(this.getTranslation("book.delete.confirm.title"));
        dialog.setText(this.getTranslation("book.delete.confirm.text", book.getTitle()));
        dialog.setConfirmText(this.getTranslation("book.delete"));
        dialog.setCancelable(true);
        dialog.setCancelText(this.getTranslation("book.cancel"));
        dialog.addConfirmListener(e -> {
            this.bookService.delete(book.getUid());
            this.refresh();
        });
        dialog.open();
    }

    private void refresh() {
        this.grid.setItems(this.bookService.listAll());
    }

    // 7
    @Override
    public String getPageTitle() {
        return this.getTranslation("title.book.list");
    }
}
