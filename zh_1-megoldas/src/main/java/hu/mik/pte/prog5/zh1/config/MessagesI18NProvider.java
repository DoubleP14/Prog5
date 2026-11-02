package hu.mik.pte.prog5.zh1.config;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

// A feladat szerint a szovegek a "messages" resource bundle-bol jonnek (messages.properties).
// A Spring Boot ebbol automatikusan MessageSource beant csinal; ez az osztaly osszekoti
// a Vaadin getTranslation(...) hivasait ezzel a MessageSource-szal.
@Component
public class MessagesI18NProvider implements I18NProvider {

    private final MessageSource messageSource;

    public MessagesI18NProvider(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(Locale.ENGLISH, Locale.of("hu"));
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        // ha nincs ilyen kulcs, a kulcsot latjuk a feluleten -> konnyu eszrevenni az elirast
        return this.messageSource.getMessage(key, params, "!" + key + "!", locale);
    }
}
