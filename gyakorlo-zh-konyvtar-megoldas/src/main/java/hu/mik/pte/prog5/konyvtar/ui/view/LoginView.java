package hu.mik.pte.prog5.konyvtar.ui.view;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")     // 2-1: /login, layout nelkul
@AnonymousAllowed   // bejelentkezes nelkul is elerheto
public class LoginView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        this.loginForm.setAction("login");
        this.loginForm.setForgotPasswordButtonVisible(false);
        this.setSizeFull();
        this.setAlignItems(Alignment.CENTER);
        this.setJustifyContentMode(JustifyContentMode.CENTER);
        this.add(this.loginForm);
    }

    // 2-1: rossz jelszo utan a Spring a /login?error cimre kuld vissza
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            this.loginForm.setError(true);
        }
    }

    // 7: lokalizalt cim
    @Override
    public String getPageTitle() {
        return this.getTranslation("title.login");
    }
}
