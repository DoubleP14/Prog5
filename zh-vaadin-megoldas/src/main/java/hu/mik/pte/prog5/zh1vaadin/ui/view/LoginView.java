package hu.mik.pte.prog5.zh1vaadin.ui.view;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")     // 2-1: /login utvonal, NEM a MainLayout-ban (ott meg nincs bejelentkezett user)
@AnonymousAllowed   // bejelentkezes nelkul is elerheto
public class LoginView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        // a form a /login-ra POST-ol, azt a Spring Security dolgozza fel
        this.loginForm.setAction("login");
        this.loginForm.setForgotPasswordButtonVisible(false);

        this.setSizeFull();
        this.setAlignItems(Alignment.CENTER);
        this.setJustifyContentMode(JustifyContentMode.CENTER);
        this.add(this.loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // sikertelen belepes utan a Spring a /login?error cimre iranyit
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
