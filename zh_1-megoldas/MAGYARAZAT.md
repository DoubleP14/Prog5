# ZH_1 (zh_1.pdf) – végigvezetés feladatonként

> A kész megoldás ez a mappa: `zh_1-megoldas`.
> Az alapfogalmak (bean, dependency injection, Maven stb.) a másik útmutatóban vannak: `../zh-vaadin-megoldas/MAGYARAZAT.md`.
> A kódrészletek azt mutatják, ami az adott feladatnál **kell**. Így látszik, melyik sor melyik feladathoz tartozik. A végleges osztályok a projektben vannak.

**Tartalom**
- [0. Előkészítés (amit a PDF kész projektként feltételez)](#0-előkészítés)
- [1. Ki-/bejelentkezés](#1-ki-bejelentkezés)
- [2. PetService](#2-petservice)
- [3. Header](#3-header)
- [4. Listázás](#4-listázás)
- [5. Állat regisztráció](#5-állat-regisztráció)
- [6. Képernyők címe](#6-képernyők-címe)
- [7. Jogosultság](#7-jogosultság)
- [Ellenőrzés](#ellenőrzés)
- [Eltérések a zh1.pdf-hez képest](#eltérések-a-zh1pdf-hez-képest)

**Végső szerkezet**
```
zh_1-megoldas/
├─ pom.xml
└─ src/main/
   ├─ java/hu/mik/pte/prog5/zh1/
   │  ├─ Zh1Application.java
   │  ├─ entity/      Pet, PetSpecies
   │  ├─ repository/  PetRepository, InMemoryPetRepository, DBPetRepository
   │  ├─ service/     PetService, PetServiceImpl, SecurityService, SecurityServiceImpl
   │  ├─ config/      SecurityConfig, MessagesI18NProvider
   │  └─ ui/          MainLayout
   │     └─ view/     LoginView, ListView, RegisterView
   └─ resources/
      ├─ application.properties
      ├─ messages.properties                 <- angol (alapértelmezett)
      └─ messages_hu.properties              <- magyar
```

---

## 0. Előkészítés

> A PDF szerint a ZH-n „mellékelt projektet” kapsz. Ehhez nem volt meg, ezért itt a nulláról készült el. **A ZH-n először nézd meg, mi van már készen a kapott projektben**, és csak a hiányzó részt írd meg.

### 0-1 Projekt generálása
https://start.spring.io/ →
- Project: **Maven** · Language: **Java** · Spring Boot: **4.1.x** (nem SNAPSHOT/RC)
- Group: `hu.mik.pte.prog5` · Artifact: `zh1` · Package name: `hu.mik.pte.prog5.zh1`
- Packaging: **Jar** · Configuration: **Properties** · Java: **25**
- Dependencies: **Vaadin**, **Spring Security**

**GENERATE** → kicsomagolás → IntelliJ: File → Open → mappa → Trust Project. Ellenőrizd: Project Structure → SDK = 25.

> **Minden saját osztály a `hu.mik.pte.prog5.zh1` csomagba vagy az alá kerüljön**, különben a Spring nem találja meg (component scan).

### 0-2 Entitások – `entity/`
```java
public enum PetSpecies {
    CAT, DOG, PARROT, TURTLE, FERRET
}
```
```java
public class Pet {
    private String uid;
    private String name;
    private PetSpecies species;

    public Pet() { }                                 // a regisztrációs űrlap ezzel hoz létre új állatot
    public Pet(String uid, String name, PetSpecies species) { ... }
    // getterek, setterek, equals, hashCode
}
```
> **Tipp:** **Alt+Insert** legenerálja a konstruktort, a gettereket/settereket és az equals/hashCode-ot.

### 0-3 Repository – `repository/`
```java
public interface PetRepository {
    List<Pet> findAll();
    List<Pet> findByName(String name);
    List<Pet> findBySpecies(PetSpecies species);
    Pet save(Pet pet);
}
```
```java
@Repository("mock")
public class InMemoryPetRepository implements PetRepository {
    private final List<Pet> petList;

    public InMemoryPetRepository() {
        petList = new ArrayList<>(List.of(
                new Pet(UUID.randomUUID().toString(), "Béla", PetSpecies.CAT),
                new Pet(UUID.randomUUID().toString(), "Yoshi", PetSpecies.PARROT),
                new Pet(UUID.randomUUID().toString(), "Bobi", PetSpecies.DOG),
                new Pet(UUID.randomUUID().toString(), "Büzös", PetSpecies.FERRET),
                new Pet(UUID.randomUUID().toString(), "Lovag", PetSpecies.TURTLE)));
    }

    @Override
    public List<Pet> findAll() {
        return new ArrayList<>(this.petList);        // MÁSOLAT, lásd 7-3
    }

    @Override
    public Pet save(Pet pet) {
        if (pet.getUid() == null) {                  // új állat -> kap azonosítót
            pet.setUid(UUID.randomUUID().toString());
        } else {                                     // meglévő -> a régit kivesszük
            this.petList.remove(this.petList.stream().filter(p -> p.getUid().equals(pet.getUid())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Pet with id: " + pet.getUid() + " not found!")));
        }
        this.petList.add(pet);
        return pet;
    }

    // findByName, findBySpecies: stream + filter
}
```
A `DBPetRepository` `@Repository("db")`, és minden metódusa `throw new UnsupportedOperationException();`. A PDF szerint ez is a projekt része, és emiatt kell majd a `@Qualifier`.

### 0-4 Service interface-ek – `service/`
```java
public interface PetService {
    List<Pet> listAll();
    Pet save(Pet pet);
    List<PetSpecies> listSpecies();
}
```
```java
public interface SecurityService {
    default List<String> getRolesAsString() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(Objects::toString)
                .map(s -> s.replace("ROLE_", "")).toList();
    }
}
```

### 0-5 Fordítások: `messages` resource bundle
A PDF a **`messages`** bundle-t kéri. A Vaadin magától csak a `vaadin-i18n/translations.properties`-t keresi, ezért itt három dolog kell.

**a)** `src/main/resources/messages.properties` (angol, mint a PDF képein) és `messages_hu.properties` (magyar). A kulcsok mindkettőben ugyanazok:
```properties
## REGISTER
register.submit=Register
register.required=This field is required!
register.invalid=Please fill in every field correctly!
register.success=Pet registered successfully!
## PET
pet.id=Identifier
pet.name=Name
pet.species=Species
pet.species.CAT=cat
pet.species.DOG=dog
pet.species.FERRET=ferret
pet.species.PARROT=parrot
pet.species.TURTLE=turtle
## HEADER
header.menu.pet.list=List pets
header.menu.pet.register=Register pets
header.menu.logout=Logout
header.username=Username:
## TITLE
title.login=Login
title.pet.list=Pet list
title.pet.register=Pet registration
```
> A kulcsok nevét a zh-vaadin mintájára választottam, a `register.required`, `.invalid` és `.success` saját kiegészítés. **A ZH-n a kapott fájl kulcsait használd.** Ha hiányzik egy kulcs, vedd fel mindegyik nyelvi fájlba.

**b)** `application.properties`:
```properties
spring.messages.encoding=UTF-8
spring.messages.fallback-to-system-locale=false
```
Az első az ékezetek miatt kell. A második miatt ismeretlen nyelvnél a `messages.properties` jön, nem a Windows nyelve.

**c)** `config/MessagesI18NProvider.java`: ez köti össze a `getTranslation()`-t a `messages` fájlokkal. A Spring Boot ezekből magától készít `MessageSource` beant.
```java
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
        return this.messageSource.getMessage(key, params, "!" + key + "!", locale);
    }
}
```
> A nyelvet a böngésző nyelve dönti el. Elírt kulcsnál a felületen ez látszik: `!kulcs!`.

---

## 1. Ki-/bejelentkezés
> *„Készítse el a LoginView osztályt [...] a Vaadin beépített bejelentkező komponensét használja! [...] a ‘/login’ útvonalon [...] hiba esetén kerüljön jelölésre.”*

**`ui/view/LoginView.java`**
```java
@Route("login")      // /login, layout nélkül, mert itt még nincs bejelentkezett user
@AnonymousAllowed    // különben a belépő oldalt sem érné el senki
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();  // a beépített komponens

    public LoginView() {
        this.loginForm.setAction("login");                // a Spring Security-nek küldi el az adatokat
        this.loginForm.setForgotPasswordButtonVisible(false);

        this.setSizeFull();
        this.setAlignItems(Alignment.CENTER);
        this.setJustifyContentMode(JustifyContentMode.CENTER);
        this.add(this.loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {     // az oldal megnyitása előtt fut le
        // rossz jelszónál a Spring a /login?error címre küld vissza
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            this.loginForm.setError(true);                // hibajelzés
        }
    }
}
```

**`config/SecurityConfig.java`**: „bejelentkezés ezen a képernyőn keresztül történjen”
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> vaadin.loginView(LoginView.class));
        return http.build();
    }

    @Bean
    public UserDetailsManager userDetailsManager() {
        var admin = User.builder().username("admin").password("{noop}password")
                .roles("ADMIN", "REGISTER", "CAT", "DOG", "PARROT", "TURTLE", "FERRET").build();
        var dog = User.builder().username("dog").password("{noop}password")
                .roles("DOG").build();
        var cat = User.builder().username("cat").password("{noop}password")
                .roles("REGISTER", "DOG").build();
        var uncommon = User.builder().username("uncommon").password("{noop}password")
                .roles("TURTLE", "PARROT", "FERRET").build();
        return new InMemoryUserDetailsManager(admin, dog, cat, uncommon);
    }
}
```
- A PDF nem adja meg a felhasználókat, ezért a zh-vaadin projektből vettem át őket. A ZH-n a kapott projektben valószínűleg megvannak.
- A `VaadinSecurityConfigurer` miatt minden képernyő alapból zárt, amíg annotációval meg nem nyitod. Ezért kell minden view-ra `@AnonymousAllowed`, `@PermitAll` vagy `@RolesAllowed`.
- A kijelentkezés gombja a 3. feladatban készül el.

### Opcionális: a belépő űrlap lefordítása (`LoginI18n`)
> **A PDF külön nem kéri**, és a kész megoldásban sincs benne. A `LoginForm` feliratai alapból angolok, csak a bevezető általános mondata („mindenhol I18n”) érinti őket, ezért akkor írd meg, ha marad időd a végén.

Import: `import com.vaadin.flow.component.login.LoginI18n;`. A konstruktorba, a `setForgotPasswordButtonVisible(false)` sor után:
```java
LoginI18n i18n = LoginI18n.createDefault();                    // az angol alapszövegek
i18n.getForm().setTitle(this.getTranslation("login.title"));
i18n.getForm().setUsername(this.getTranslation("login.username"));
i18n.getForm().setPassword(this.getTranslation("login.password"));
i18n.getForm().setSubmit(this.getTranslation("login.submit"));
i18n.getErrorMessage().setTitle(this.getTranslation("login.error.title"));
i18n.getErrorMessage().setMessage(this.getTranslation("login.error.message"));
i18n.getErrorMessage().setUsername(this.getTranslation("login.error.username"));
i18n.getErrorMessage().setPassword(this.getTranslation("login.error.password"));
this.loginForm.setI18n(i18n);                                  // átadja az űrlapnak
```
A `messages_hu.properties`-be:
```properties
## LOGIN
login.title=Bejelentkezés
login.username=Felhasználónév
login.password=Jelszó
login.submit=Belépés
login.error.title=Hibás felhasználónév vagy jelszó
login.error.message=Ellenőrizd az adatokat, és próbáld újra!
login.error.username=A felhasználónév megadása kötelező!
login.error.password=A jelszó megadása kötelező!
```
A `messages.properties`-be:
```properties
## LOGIN
login.title=Login
login.username=Username
login.password=Password
login.submit=Log in
login.error.title=Incorrect username or password
login.error.message=Check your details and try again!
login.error.username=Username is required!
login.error.password=Password is required!
```
> Ebben a projektben ez nincs kipróbálva. A `zh-vaadin` feladat egy másolatán működött, ott a két `login.error.username` / `login.error.password` szöveg (üresen hagyott mező) futás közben nem lett megnézve.

---

## 2. PetService
> *„Implementálja a PetService interface-t [...] a ‘mock’ [...] használja [...] Az importok között nem szerepelhet az InMemoryPetRepository.”*

**`service/PetServiceImpl.java`**
```java
@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;     // az interface-re hivatkozunk

    public PetServiceImpl(@Qualifier("mock") PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public List<Pet> listAll() {
        return this.petRepository.findAll();
    }

    @Override
    public Pet save(Pet pet) {
        return this.petRepository.save(pet);
    }

    @Override
    public List<PetSpecies> listSpecies() {
        return Arrays.asList(PetSpecies.values());  // az enum összes értéke, dinamikusan
    }
}
```
- **`@Qualifier("mock")`:** két `PetRepository` bean van (`db`, `mock`). Enélkül a Spring nem tudja, melyiket adja, és az app el sem indul.
- **Mindhárom metódus kell**, különben piros az osztály. Az IntelliJ **Alt+Enter → Implement methods** csak üres vázat generál (`return List.of();`, `return null;`), ezeket le kell cserélni. Ez lefordul, ezért a build nem jelzi, ha kimarad.
- A jogosultsági annotációk a 7. feladatnál kerülnek rá.

---

## 3. Header
> *„A fejlécben szerepeljen a bejelentkezett felhasználó neve, egy menü [...] illetve egy kijelentkezés gomb. A menüpontokhoz célszerű a RouterLink komponenst használni.”*

**`ui/MainLayout.java`**
```java
@PermitAll
public class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;

    public MainLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        // felhasználó neve
        Span username = new Span(this.getTranslation("header.username") + " "
                + this.authenticationContext.getPrincipalName().orElse(""));

        // menü
        RouterLink list = new RouterLink(this.getTranslation("header.menu.pet.list"), ListView.class);
        RouterLink register = new RouterLink(this.getTranslation("header.menu.pet.register"), RegisterView.class);

        // kijelentkezés
        Button logout = new Button(this.getTranslation("header.menu.logout"),
                e -> this.authenticationContext.logout());

        HorizontalLayout header = new HorizontalLayout(username, list, register, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "0 var(--lumo-space-m)");

        this.addToNavbar(header);
    }
}
```
- A `RouterLink` oldal-újratöltés nélkül vált képernyőt. Az osztályt adod meg neki, nem az URL-t.
- A `logout()` azért elég, mert a kijelentkezést a `VaadinSecurityConfigurer` beállítja.
- A `ListView` és a `RegisterView` kell a fordításhoz, ezért ez a két osztály már létezzen, ha üres is.

---

## 4. Listázás

### 4-1 ListView
> *„A táblázatban lévő azonosító oszlop csak akkor látszódjon, ha ‘ADMIN’ jogosultságú felhasználó nyitja meg. (SecurityService.getRolesAsString) [...] Grid osztályt használja.”*

A `SecurityService`-t csak úgy lehet injektálni, ha van belőle bean. Az interface-ből nem lesz magától, ezért kell egy üres osztály:

**`service/SecurityServiceImpl.java`**
```java
@Service("securityService")    // a név a 7-3-hoz kell
public class SecurityServiceImpl implements SecurityService {
}
```

**`ui/view/ListView.java`**
```java
@Route(value = "list", layout = MainLayout.class)    // 4-2
@RouteAlias(value = "", layout = MainLayout.class)   // belépés után a "/" címre visz a Vaadin
@PermitAll                                           // bármelyik bejelentkezett user
public class ListView extends VerticalLayout {

    // false: ne generáljon magától oszlopokat
    private final Grid<Pet> grid = new Grid<>(Pet.class, false);

    public ListView(PetService petService, SecurityService securityService) {   // interface-ek
        var idColumn = this.grid.addColumn(Pet::getUid).setHeader(this.getTranslation("pet.id"));
        this.grid.addColumn(Pet::getName).setHeader(this.getTranslation("pet.name"));
        this.grid.addColumn(pet -> this.getTranslation("pet.species." + pet.getSpecies().name()))
                .setHeader(this.getTranslation("pet.species"));

        // azonosító csak ADMIN-nak
        idColumn.setVisible(securityService.getRolesAsString().contains("ADMIN"));

        this.grid.setItems(petService.listAll());

        this.setSizeFull();
        this.add(this.grid);
    }
}
```
A faj oszlopnál a kulcs összeáll: `"pet.species." + "CAT"` → `pet.species.CAT` → „cat” / „macska”.

### 4-2 ListView routing
> *„a /list útvonalon legyen elérhető, a MainLayout-ba foglalva.”*

Ez a `@Route(value = "list", layout = MainLayout.class)` sor. A `value` az URL, a `layout` a keret, amibe a képernyő bekerül. A `@RouteAlias` nem feladat, csak kényelmi kiegészítés.

---

## 5. Állat regisztráció

### 5-1 RegisterView
> *„A faj listát a PetService.listSpecies metódusa adja [...] bindolással [...] minden mező kitöltése kötelező [...] A mentés gomb megnyomásakor ellenőrizze az adatok helyességét. Hiba esetén írjon ki egy figyelmeztető üzenetet.”*

A PDF képén választógombok vannak, ezért **`RadioButtonGroup`** kell, nem `ComboBox`.

**`ui/view/RegisterView.java`**
```java
@Route(value = "register", layout = MainLayout.class)   // 5-2
@PermitAll                                               // a 7-2-nél @RolesAllowed("REGISTER") lesz
public class RegisterView extends VerticalLayout {

    private final PetService petService;
    private final Binder<Pet> binder = new Binder<>(Pet.class);

    private final TextField name = new TextField(this.getTranslation("pet.name"));
    private final RadioButtonGroup<PetSpecies> species = new RadioButtonGroup<>(this.getTranslation("pet.species"));

    public RegisterView(PetService petService) {
        this.petService = petService;

        // a fajok a service-ből, a felirat a fordításból
        this.species.setItems(this.petService.listSpecies());
        this.species.setItemLabelGenerator(s -> this.getTranslation("pet.species." + s.name()));
        this.species.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);   // egymás alá, mint a képen

        // bindolás + kötelező mezők
        this.binder.forField(this.name)
                .asRequired(this.getTranslation("register.required"))
                .bind(Pet::getName, Pet::setName);
        this.binder.forField(this.species)
                .asRequired(this.getTranslation("register.required"))
                .bind(Pet::getSpecies, Pet::setSpecies);

        Button submit = new Button(this.getTranslation("register.submit"), e -> this.save());

        this.add(this.name, this.species, submit);
    }

    private void save() {
        Pet pet = new Pet();
        if (this.binder.writeBeanIfValid(pet)) {        // ellenőrzés a gomb megnyomásakor
            this.petService.save(pet);
            Notification.show(this.getTranslation("register.success"), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(ListView.class);
        } else {                                        // hiba -> figyelmeztető üzenet
            Notification.show(this.getTranslation("register.invalid"), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
```
- **`Binder`:** összeköti a mezőket a `Pet` getterével és setterével.
- **`asRequired`:** kötelezővé teszi a mezőt, és kirakja a jelölést a felirat mellé.
- **`writeBeanIfValid`:** `true`, ha minden mező rendben van, és ekkor beírja az adatokat a `pet`-be. Ha nem, `false` a visszatérési érték.
- **`Notification.show`:** a felugró üzenet. A figyelmeztetést a PDF kéri, a sikeres mentés üzenete saját ráadás, törölhető.

### 5-2 RegisterView routing
> *„a /register útvonalon legyen elérhető, a MainLayout-ba foglalva.”*

Ez a `@Route(value = "register", layout = MainLayout.class)` sor. (A PDF ennek a pontnak a „PetController” címet adta, de a szövege a routingról szól.)

---

## 6. Képernyők címe
> *„Mindegyik képernyőn állítson be lokalizációnak megfelelő címet!”*

Mindhárom view kap egy `implements HasDynamicTitle` részt és egy metódust:
```java
public class LoginView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle { ... }
public class ListView extends VerticalLayout implements HasDynamicTitle { ... }
public class RegisterView extends VerticalLayout implements HasDynamicTitle { ... }
```
```java
@Override
public String getPageTitle() {
    return this.getTranslation("title.login");   // ListView: title.pet.list, RegisterView: title.pet.register
}
```
A `@PageTitle("...")` itt nem jó, mert abba csak fix szöveg írható.

---

## 7. Jogosultság

### 7-1 Global method security
> *„Engedélyezze a global method security-t.”*

Egy sor a `SecurityConfig` fölé:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig { ... }
```
Enélkül a `@PreAuthorize` és a `@PostFilter` nem csinál semmit, és a fordító ezt nem jelzi.

### 7-2 Állat regisztráció
> *„A ‘PetService’ implementációjában [...] csak [...] ‘REGISTER’ jogosultsággal. A menüben lévő regisztrációs gomb is [...] A RegisterView csak akkor legyen látogatható [...]”*

Három helyen kell:

**1. `PetServiceImpl`:**
```java
@Override
@PreAuthorize("hasRole('REGISTER')")
public Pet save(Pet pet) { ... }

@Override
@PreAuthorize("hasRole('REGISTER')")
public List<PetSpecies> listSpecies() { ... }
```
**2. `MainLayout`:**
```java
register.setVisible(this.authenticationContext.hasRole("REGISTER"));
```
**3. `RegisterView`:** a `@PermitAll` helyére:
```java
@RolesAllowed("REGISTER")
```
- A `@PreAuthorize` a metódus futása **előtt** ellenőriz. Ha nincs jog, `AccessDeniedException` keletkezik.
- A `hasRole` magától hozzáteszi a `ROLE_` előtagot, ezért nem kell kiírni.
- A menüpont elrejtése önmagában kevés, mert a címet kézzel is be lehet írni. Ezt a `@RolesAllowed` védi.

### 7-3 Állatok szűrése (bónusz)
> *„[...] macska típusú kisállatot csak akkor adjon vissza, ha a felhasználó rendelkezik ‘CAT’ jogosultsággal [...] Figyeljen oda az objektumok típusára az összehasonlításkor!”*

**`PetServiceImpl`:**
```java
@Override
@PostFilter("@securityService.getRolesAsString().contains(filterObject.species.name())")
public List<Pet> listAll() {
    return this.petRepository.findAll();
}
```
| Rész | Jelentése |
|---|---|
| `@PostFilter` | A metódus **után** kiveszi a listából, amire a feltétel hamis |
| `@securityService` | A `securityService` nevű bean (a 4-1-ben kapta a nevét) |
| `filterObject` | A lista aktuális eleme, egy `Pet` |
| `.species.name()` | Az enum Stringként: `PetSpecies.CAT` → `"CAT"` |

- **A típus a csapda:** a szerepek String-ek, a `species` enum. A `.name()` nélkül a `contains` mindig hamis, és üres a lista.
- **A `findAll` másolatot ad vissza**, mert a `@PostFilter` törli az elemeket a kapott listából. Az eredeti listából nem szabad törölnie, és egy `List.of(...)` nem is lenne módosítható.

---

## Ellenőrzés

1. Maven panel → `clean`, majd `package` → `BUILD SUCCESS`.
2. `Zh1Application` → ▶ → http://localhost:8080
   > Itt nincs port feladat, ezért az alap 8080-as porton indul. Ha foglalt (`Port 8080 was already in use`), tegyél az `application.properties`-be egy `server.port=8085` sort. Teszteléskor nálad is foglalt volt, ezért 8085-ön futott.

Tesztfelhasználók (jelszó: `password`): `admin`, `dog` (DOG), `cat` (REGISTER + DOG), `uncommon` (TURTLE, PARROT, FERRET).

| Feladat | Teszt | Elvárt | Eredmény |
|---|---|---|---|
| Fordítás | `clean package` tesztekkel, Java 25 | `BUILD SUCCESS` | ✅ |
| 1 | `/` belépés nélkül | átirányít a `/login`-ra | ✅ |
| 1 | rossz jelszó | hibaüzenet a login formon | ✅ |
| 3 | admin fejléce | név, két menüpont, kijelentkezés gomb | ✅ |
| 3 | kijelentkezés gomb | vissza a `/login`-ra | ✅ |
| 4 | admin listája | 5 állat, Azonosító oszloppal | ✅ |
| 5 | üres űrlap → Regisztrál | „Kötelező mező!” + figyelmeztető üzenet | ✅ |
| 5 | kitöltött űrlap | átdob a listára, ott az új állat | ✅ |
| 6 | címek | lokalizált cím a böngészőfülön | ✅ |
| 7-2 | uncommon | nincs regisztrációs menü, a `/register` tiltott | ✅ |
| 7-3 | uncommon | csak papagáj, görény, teknős | ✅ |

**Amit így nem próbáltam ki:**
- A `dog` és a `cat` usert ebben a projektben. A zh-vaadin projektben mindkettő jó volt, ugyanezzel a jogosultsági kóddal.
- Az angol feliratokat, mert a böngésző magyar volt.
- Az üzenetek `TOP_CENTER` helyzetét. Ezt a böngészős teszt után állítottam át, utána csak fordítás és automata teszt futott.

---

## Eltérések a zh1.pdf-hez képest

| | zh1.pdf (zh-vaadin) | zh_1.pdf (ez) |
|---|---|---|
| Kiinduló projekt | megvolt | a PDF szerint lesz, de itt nem volt meg |
| Port feladat | 8083 / dev: 8084 | nincs (alap 8080) |
| Fordítások | `vaadin-i18n/translations.properties` | `messages.properties` + saját `I18NProvider` |
| Faj választó | `ComboBox` | `RadioButtonGroup` |
| Hiba mentéskor | csak a mezők pirosak | + `Notification` figyelmeztetés |
| Menü | nincs megkötés | `RouterLink` javasolt |
| Számozás | 1–8 | 1–7 (nincs port feladat) |
