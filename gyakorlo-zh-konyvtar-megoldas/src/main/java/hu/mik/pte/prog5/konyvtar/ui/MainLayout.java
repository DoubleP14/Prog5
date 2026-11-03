package hu.mik.pte.prog5.konyvtar.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import hu.mik.pte.prog5.konyvtar.ui.view.BookFormView;
import hu.mik.pte.prog5.konyvtar.ui.view.BookListView;
import jakarta.annotation.security.PermitAll;

import java.util.Locale;

@PermitAll
public class MainLayout extends AppLayout {

    public MainLayout(AuthenticationContext authenticationContext) {
        // 4-1: felhasznalonev
        Span username = new Span(this.getTranslation("header.username") + " "
                + authenticationContext.getPrincipalName().orElse(""));
        // 4-1: menu
        RouterLink list = new RouterLink(this.getTranslation("header.menu.book.list"), BookListView.class);
        RouterLink create = new RouterLink(this.getTranslation("header.menu.book.new"), BookFormView.class);
        // 8-2: csak EDITOR joggal latszik
        create.setVisible(authenticationContext.hasRole("EDITOR"));
        // 4-1: kijelentkezes
        Button logout = new Button(this.getTranslation("header.menu.logout"), e -> authenticationContext.logout());

        // 4-2: nyelvvalto
        ComboBox<Locale> language = new ComboBox<>();
        language.setPlaceholder(this.getTranslation("header.language"));
        language.setItems(Locale.of("hu"), Locale.ENGLISH);
        language.setItemLabelGenerator(l -> l.getDisplayLanguage(l));   // minden nyelv a sajat nevevel
        language.setValue(Locale.of(UI.getCurrent().getLocale().getLanguage()));
        language.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                VaadinSession.getCurrent().setLocale(e.getValue());
                UI.getCurrent().getPage().reload();                     // ujraepul a felulet az uj nyelven
            }
        });

        HorizontalLayout header = new HorizontalLayout(username, list, create, logout, language);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        this.addToNavbar(header);
    }
}
