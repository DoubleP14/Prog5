package hu.mik.pte.prog5.zh1vaadin.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import hu.mik.pte.prog5.zh1vaadin.ui.view.ListView;
import hu.mik.pte.prog5.zh1vaadin.ui.view.RegisterView;
import jakarta.annotation.security.PermitAll;

@PermitAll
public class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;

    public MainLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        // 4: felhasznalonev
        Span username = new Span(this.getTranslation("header.username") + " "
                + this.authenticationContext.getPrincipalName().orElse(""));

        // 4: menu
        RouterLink list = new RouterLink(this.getTranslation("header.menu.pet.list"), ListView.class);
        RouterLink register = new RouterLink(this.getTranslation("header.menu.pet.register"), RegisterView.class);
        // 8-2: a regisztracios menupont csak REGISTER joggal latszik
        register.setVisible(this.authenticationContext.hasRole("REGISTER"));

        // 4: kijelentkezes
        Button logout = new Button(this.getTranslation("header.menu.logout"),
                e -> this.authenticationContext.logout());

        HorizontalLayout header = new HorizontalLayout(username, list, register, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.getStyle().set("padding", "0 var(--lumo-space-m)");

        this.addToNavbar(header);
    }
}
