# ZH Vaadin – végigvezetés a 0-ról

> A kiinduló projekt: `zh-vaadin/zh-vaadin` (érintetlen, gyakorolj rajta!)
> A kész megoldás: `zh-vaadin-megoldas` (ez a mappa)

---

## 0. Az alapok – mi mire való?

| Fogalom | Mire jó |
|---|---|
| **Maven** (`pom.xml`) | Függőségkezelő + build eszköz. A `pom.xml`-be írod, milyen könyvtárak kellenek (Spring, Vaadin), a Maven letölti őket. `mvn clean package` = mindent töröl és újrafordít → `target/*.jar`. **Ha ez nem fut le hibátlanul, elégtelen!** |
| **Spring Boot** | Keretrendszer, ami elindít egy webszervert (Tomcat) és **bean**-eket kezel. |
| **Bean** | Egy objektum, amit a Spring hoz létre és tárol. `@Service`, `@Repository`, `@Configuration`, `@Bean` jelöli. |
| **Dependency Injection (DI)** | Nem te írod, hogy `new PetServiceImpl()`, hanem a konstruktorban kéred (`public ListView(PetService petService)`), és a Spring beadja. **Mindig az interface-t kérd** (`PetService`, nem `PetServiceImpl`)! |
| **Spring Security** | Bejelentkezés + jogosultságok. Szerepek (role): `ADMIN`, `REGISTER`, `CAT`... A Spring ezeket `ROLE_ADMIN` formában tárolja. |
| **Vaadin** | UI Java-ban. Nincs HTML: `new Button(...)`, `new Grid<>()`, `add(...)`. A view-k osztályok, a `@Route` adja meg az URL-t. |
| **I18n** | Minden felirat a `src/main/resources/vaadin-i18n/translations.properties`-ből jön: `getTranslation("kulcs")`. **Soha ne írj be szöveget közvetlenül!** |

### Rétegek (a projekt felépítése)
```
entity/       Pet, PetSpecies         -> adatmodell
repository/   PetRepository (interface) + 2 implementáció ("db", "mock")  -> adatelérés
service/      PetService (interface) + PetServiceImpl -> üzleti logika, jogosultság
ui/           MainLayout (fejléc), view/ (LoginView, ListView, RegisterView) -> felület
config/       SecurityConfig          -> bejelentkezés, felhasználók
```
A hívási lánc: **View → Service → Repository**. A view soha nem hívja közvetlenül a repository-t.

### Vaadin vs Thymeleaf
A Vaadint javaslom: minden Java, nincs HTML sablon és nincs controller/model/`th:` attribútum. A ZH kiinduló projektje is Vaadin.

---

## 0.1 Új projekt generálása – https://start.spring.io/

> **A ZH-n ez nem kell!** Ott megkapod a kész kiinduló projektet (`pom.xml`, entitások, repository-k, translations.properties már benne vannak). Erre akkor van szükség, ha gyakorlásként a nulláról akarsz projektet csinálni, vagy a 2. ZH-n (Docker) üres projekttel kezdesz.

A start.spring.io-n kattintgatod össze a projekt vázát: a `pom.xml`-t, a mappaszerkezetet, a main osztályt és a Maven wrappert (`mvnw`). Így ezeket nem kell kézzel megírni.

**Bal oldal (beállítások):**

| Mező | Mit válassz | Miért |
|---|---|---|
| Project | **Maven** | Ezt tanuljuk (a Gradle a másik build eszköz) |
| Language | **Java** | |
| Spring Boot | **4.1.x** (pl. 4.1.2). Ne `SNAPSHOT`/`RC`/`M` legyen! | A tárgy a 4.1.1-et használja, a 4.1.x mind ugyanaz. A SNAPSHOT/RC fejlesztői változat, instabil. |
| Group | `hu.mik.pte.prog5` | „Cég/szervezet” azonosító, ebből lesz a csomagnév eleje |
| Artifact | pl. `zh-vaadin` | A projekt neve, ebből lesz a `.jar` neve |
| Name | (automatikusan = Artifact) | |
| Package name | pl. `hu.mik.pte.prog5.zh1vaadin` | Ide kerül a main osztály. Kötőjel nem lehet benne, ezért a `zh-vaadin`-ból alapból `zh_vaadin` lesz, átírhatod. |
| Packaging | **Jar** | A Spring Boot beépített Tomcattel fut, nem kell war |
| Configuration | **Properties** | `application.properties` (a YAML is jó, csak más a szintaxis) |
| Java | **25** | A tárgy Java 25-öt használ. Ennek egyeznie kell a gépeden lévő JDK-val! |

**Jobb oldal → ADD DEPENDENCIES** (Ctrl+B), ezeket add hozzá:
- **Vaadin** (Web) – a felület
- **Spring Security** (Security) – bejelentkezés, jogosultságok
- (opcionális) **Validation** – ha `@NotBlank`, `@NotNull` annotációkkal validálnál (`BeanValidationBinder`)
- (opcionális) **Lombok** – `@Getter`/`@Setter`/`@RequiredArgsConstructor` (a tanár Gyak6-os példájában volt)
- Thymeleaf-es változathoz Vaadin helyett: **Spring Web** + **Thymeleaf** (+ Spring Security)

> A **Spring Web**-et Vaadin mellé nem kell külön felvenni, a Vaadin starter behúzza.

**Utána:**
1. **GENERATE** (Ctrl+Enter) → letölt egy `.zip`-et.
2. Csomagold ki (pl. `C:\Patrik\Prog5\zh-vaadin`).
3. IntelliJ: **File → Open** → válaszd ki a mappát (vagy benne a `pom.xml`-t) → **Open as Project** → **Trust Project**.
4. Várd meg, amíg a Maven mindent letölt (jobb alul a folyamatjelző).

**Mit kapsz?** A generált `pom.xml`-ben a Vaadin kiválasztásakor automatikusan benne van:
- `vaadin.version` property + `vaadin-bom` (a Vaadin csomagok verzióit egyezteti),
- `vaadin-spring-boot-starter` + `vaadin-dev` (a dev módhoz kell, Vaadin 25 óta külön függőség),
- `vaadin-maven-plugin` (a frontend buildhez).

Csak a saját osztályaidat kell megírnod (entity, repository, service, config, ui).

**A generált mappaszerkezet:**
```
pom.xml                      -> függőségek
mvnw, mvnw.cmd, .mvn/        -> Maven wrapper: Maven telepítése nélkül is fut (./mvnw clean package)
src/main/java/.../ZhVaadinApplication.java   -> @SpringBootApplication + main(): innen indul
src/main/resources/application.properties    -> beállítások (port stb.)
src/test/java/...            -> tesztek
```
A saját osztályaid **a main osztály csomagjába vagy alá** kerüljenek (`...zh1vaadin.service`, `...zh1vaadin.ui` stb.). A Spring csak ott keres beaneket (component scan)! Ha máshova teszed, a `@Service` „nem létezik” a Spring számára.

A fordításokhoz hozd létre kézzel a `src/main/resources/vaadin-i18n/translations.properties` fájlt. A Vaadin pontosan ezen a néven keresi.

---

## 0.2 IntelliJ + Java 25 beállítás, futtatás

1. **Java 25 SDK:** File → Project Structure → Project → SDK → ha nincs 25: **Download JDK… → Version 25, Eclipse Temurin**. A Language level is legyen 25.
2. **Maven reload:** jobb oldali **Maven** panel → 🔄 (Reload All Maven Projects). Ezt mindig csináld meg, ha a `pom.xml` változott.
3. **Fordítás (a ZH feltétele!):** Maven panel → `zh-vaadin` → Lifecycle → dupla klikk a **clean**-re, majd a **package**-re. Az alján `BUILD SUCCESS` kell legyen.
   Parancssorból: `./mvnw clean package` (Windows CMD-ben: `mvnw.cmd clean package`).
   > A `clean` + `install` is jó ellenőrzésnek: az `install` lefuttatja a `package`-et is (fordítás + tesztek + jar), utána még bemásolja a jart a helyi Maven tárolóba (`~/.m2`). Ha ez `BUILD SUCCESS`, akkor a `package` is az. A feladat a `package`-et nevezi meg, ezért a beadás előtt azt futtasd.
   > **Ne kapcsold ki a teszteket** (`-DskipTests`, vagy a Maven panel „Skip Tests” gombja), mert a tanár `mvn clean package` parancsa lefuttatja őket.
4. **Indítás:** nyisd meg a `ZhVaadinApplication`-t → zöld ▶ a `main` mellett. Az első indulás lassú (1-2 perc), mert a Vaadin letölti a Node-ot és buildeli a frontendet. Utána: http://localhost:8083
5. **Dev profillal indítás:** fent a Run Configuration legördülő → Edit Configurations… → **Active profiles: `dev`** (ha nem látod: Modify options → Add VM options → `-Dspring.profiles.active=dev`) → ekkor a 8084-es porton fut.
6. **Leállítás:** piros ■ gomb. Ha elfelejted, a következő indításnál „Port 8083 was already in use” lesz.

---

## 1. Port beállítás
**Fájl:** `src/main/resources/application.properties`
```properties
server.port=8083
```
**Új fájl:** `src/main/resources/application-dev.properties`
```properties
server.port=8084
```
**Miért?** A Spring mindig betölti az `application.properties`-t. Ha aktív a `dev` profil, utána betölti az `application-<profil>.properties`-t is, és az felülírja az ugyanolyan kulcsokat.
Kipróbálás: IntelliJ-ben a Run Configuration → Active profiles: `dev`, vagy `--spring.profiles.active=dev`.

---

## 2-2. Bejelentkezés javítása (SecurityConfig)
A kiinduló kódban **3 hiba** volt, ezeket kellett megtalálni:
1. Nem volt `@Configuration` az osztályon → a Spring be sem olvasta.
2. Nem volt `@Bean` a `userDetailsManager()` metóduson → a felhasználók nem kerültek be a Springbe.
3. `return new InMemoryUserDetailsManager();` **üres** volt → `new InMemoryUserDetailsManager(admin, dog, cat, uncommon)`.

Ide kerül még:
- `@EnableWebSecurity` – webes security bekapcsolása
- `@EnableMethodSecurity` – **8-1 feladat**, ettől működik a `@PreAuthorize` és a `@PostFilter`

> `{noop}password` = a jelszó nincs titkosítva (no operation encoder). Teszt usereknél ez rendben van.

> **Deprecated figyelmeztetés:** a kiinduló kódban lévő `@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)` Vaadin 25 óta elavult (`deprecated ... marked for removal`). Ez csak **figyelmeztetés**, a kód ettől még lefordul és fut, tehát nem bukó hiba. Vaadin 25-ben már nem kell ez a sor, ezért a megoldásból kivettem (az importjaival együtt), és így is működik minden jogosultság.

## 2-1. LoginView
A SecurityConfig-ba:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> vaadin.loginView(LoginView.class));
    return http.build();
}
```
Ezzel közlöd: „a bejelentkező oldal a LoginView, nem a Spring alap oldala”. Egyúttal minden view alapból zárt lesz, kivéve ha annotációval (`@PermitAll`, `@AnonymousAllowed`, `@RolesAllowed`) megnyitod.

A LoginView:
- `@Route("login")` → `/login` (**nem** a MainLayout-ban, mert ott még nincs user)
- `@AnonymousAllowed` → belépés nélkül is elérhető (különben végtelen átirányítás lenne)
- `extends VerticalLayout` → egy Vaadin komponens, amibe `add()`-dal pakolhatsz
- `LoginForm` = a Vaadin beépített belépő komponense; `setAction("login")` → a `/login`-ra POST-ol, ezt a Spring dolgozza fel
- Hibakezelés: rossz jelszónál a Spring a `/login?error` címre küld vissza. A `beforeEnter` (a `BeforeEnterObserver` része, minden belépés előtt lefut) ezt nézi, és `loginForm.setError(true)` jeleníti meg a hibát.

### Opcionális: a belépő űrlap lefordítása (`LoginI18n`)
> **Külön egyik PDF sem kéri**, és a kész megoldásban sincs benne. A `LoginForm` feliratai („Log in”, „Incorrect username or password”) alapból angolok, a kiinduló `translations.properties`-ben nincs is hozzájuk kulcs, és a tanár Gyak6-os példája sem fordítja őket. Csak a bevezető általános mondata („mindenhol I18n”) érinti, ezért akkor írd meg, ha marad időd a végén.

A `LoginView`-ba az import:
```java
import com.vaadin.flow.component.login.LoginI18n;
```
A konstruktorba, a `setForgotPasswordButtonVisible(false)` sor után:
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
A `translations.properties`-be:
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
Egy ideiglenes másolaton kipróbálva: `BUILD SUCCESS`, a cím, a mezőnevek, a gomb és a rossz jelszó hibaüzenete magyarul jelenik meg. A két `login.error.username` / `login.error.password` szöveg (üresen hagyott mező) lefordul, de futás közben nem lett megnézve.

---

## 3. PetService implementáció
**Új fájl:** `service/PetServiceImpl.java`
```java
@Service
public class PetServiceImpl implements PetService {
    private final PetRepository petRepository;

    public PetServiceImpl(@Qualifier("mock") PetRepository petRepository) { ... }
```
**Miért `@Qualifier`?** A `PetRepository`-nak két implementációja van (`@Repository("db")` és `@Repository("mock")`). Ha csak `PetRepository`-t kérsz, a Spring nem tudja, melyiket adja, és hibával elszáll. A `@Qualifier("mock")` **név szerint** választ, így nem kell importálni az `InMemoryPetRepository`-t (a feladat ezt kifejezetten tiltja).

A metódusok csak továbbhívnak: `listAll()` → `findAll()`, `save()` → `save()`, `listSpecies()` → `Arrays.asList(PetSpecies.values())` (ez „dinamikusan” adja az enum összes értékét).

**SecurityServiceImpl:** a `SecurityService` interface-nek csak `default` metódusa van, de bean nélkül nem lehet injektálni. Ezért kell egy üres osztály: `@Service("securityService") class SecurityServiceImpl implements SecurityService {}`.

---

## 4. MainLayout (fejléc)
`extends AppLayout` – a Vaadin kész oldalváza, amelynek van navbar része: `addToNavbar(...)`.
- Felhasználónév: `AuthenticationContext` (Vaadin bean, konstruktorban kérd) → `getPrincipalName()`
- Menü: `new RouterLink(felirat, ListView.class)`, ez kattintható link a view-ra
- Kijelentkezés: `new Button(felirat, e -> authenticationContext.logout())`
- **8-2:** `register.setVisible(authenticationContext.hasRole("REGISTER"))`
- `@PermitAll` a layouton: bármely belépett user használhatja.

---

## 5. ListView
```java
@Route(value = "list", layout = MainLayout.class)   // 5-2: /list, a MainLayout-ba ágyazva
@PermitAll
public class ListView extends VerticalLayout implements HasDynamicTitle
```
- `new Grid<>(Pet.class, false)`: a `false` azt jelenti, hogy ne generáljon magától oszlopokat. Mi adjuk hozzá őket, **a sorrend számít**: id, név, faj.
- `addColumn(Pet::getUid).setHeader(getTranslation("pet.id"))`
- Faj fordítva: `pet -> getTranslation("pet.species." + pet.getSpecies().name())` → `pet.species.CAT` → „macska”
- Azonosító csak ADMIN-nak: `idColumn.setVisible(securityService.getRolesAsString().contains("ADMIN"))`
- `grid.setItems(petService.listAll())`
- `@RouteAlias("")`: belépés után a Vaadin a `/`-re visz, így ott is a lista jelenik meg (nem kötelező, de kényelmes).

---

## 6. RegisterView
```java
@Route(value = "register", layout = MainLayout.class)  // 6-2
@RolesAllowed("REGISTER")                               // 8-2: csak REGISTER joggal
```
- `ComboBox<PetSpecies>`: `setItems(petService.listSpecies())` adja az elemeket, `setItemLabelGenerator(...)` a megjelenő (lefordított) szöveget.
- **Binder** = kapcsolat a form mezői és a `Pet` objektum között:
  ```java
  binder.forField(name).asRequired(getTranslation("register.required")).bind(Pet::getName, Pet::setName);
  ```
  Az `asRequired` teszi kötelezővé a mezőt (és kirakja a pöttyöt a felirat mellé).
- Mentés: `if (binder.writeBeanIfValid(pet)) { petService.save(pet); navigate(ListView.class); }`. Csak akkor ír a `pet`-be, ha minden mező valid. Ha nem, a mezők pirosak lesznek.
- A `register.required` kulcsot én adtam hozzá a translations.properties-hez (a felirat is I18n kell legyen).

---

## 7. Címek
Minden view `implements HasDynamicTitle`:
```java
@Override public String getPageTitle() { return getTranslation("title.pet.list"); }
```
(Fix szöveghez lenne `@PageTitle("...")` is, de az nem fordítható, ezért kell a `HasDynamicTitle`.)

---

## 8. Jogosultság
- **8-1:** `@EnableMethodSecurity` a SecurityConfig-on.
- **8-2:** a `save` és a `listSpecies` metódusokon `@PreAuthorize("hasRole('REGISTER')")`, a menüpont `setVisible(hasRole("REGISTER"))`, a view-n `@RolesAllowed("REGISTER")`. Három szinten: service, menü, view.
- **8-3 (bónusz):**
  ```java
  @PostFilter("@securityService.getRolesAsString().contains(filterObject.species.name())")
  ```
  - `@PostFilter` = a metódus lefut, és **utána** a Spring kiszűri a visszaadott listából azokat az elemeket, amelyekre a kifejezés `false`.
  - `filterObject` = a lista aktuális eleme (egy `Pet`).
  - `@securityService` = hivatkozás a `securityService` nevű beanre (ezért kapott nevet a `@Service`).
  - **„Figyeljen az objektumok típusára”:** a `species` egy **enum**, a szerepek **String**-ek. Ezért kell a `.name()` (`PetSpecies.CAT` → `"CAT"`). Enélkül a `contains` mindig `false`, és üres lenne a lista.

---

## Ellenőrzés (amit lefuttattam)
| Teszt | Eredmény |
|---|---|
| `mvn clean package` Java 25-tel, tesztekkel együtt | ✅ `BUILD SUCCESS`, 1 teszt sikeres |
| Indulás a 8083-as porton | ✅ (ez még JDK 21-gyel, a Java 25-ös újrateszt a `dev` profillal ment) |
| Indulás `dev` profillal a 8084-es porton, Java 25-tel | ✅ |
| cat user: 1 kutya + regisztrációs menü, új kutya mentése működik | ✅ |
| `/list` belépés nélkül → átirányít a `/login`-ra | ✅ |
| Rossz jelszó → hibaüzenet | ✅ |
| admin: 5 állat + Azonosító oszlop + regisztrációs menü | ✅ |
| Üres form mentése → „Kötelező mező!” mindkét mezőnél | ✅ |
| Új állat mentése → megjelenik a listában | ✅ |
| dog user: csak kutyák, nincs Azonosító, nincs regisztrációs menü | ✅ |
| dog user `/register` → hozzáférés megtagadva | ✅ |

Tesztfelhasználók (jelszó mindenhol `password`): `admin`, `dog`, `cat` (REGISTER+DOG), `uncommon` (TURTLE, PARROT, FERRET).

---

## A zh_1.pdf eltérései (ugyanez a projekt, kis különbségek)
- Nincs port feladat.
- A faj **RadioButtonGroup** (`new RadioButtonGroup<PetSpecies>()`, ugyanígy `setItems` + `setItemLabelGenerator`).
- Hibás mentésnél **figyelmeztető üzenet** kell: `else { Notification.show(getTranslation("...")); }`.
- „messages resource bundle” a fordításokhoz (a kiinduló projektjében lesz megadva).

## Gyakori hibák és jelentésük
| Hiba | Ok / megoldás |
|---|---|
| `release version 25 not supported` / `invalid target release: 25` | Nem Java 25-tel fordítasz → 0.2 / 1. pont (SDK beállítás) |
| `Port 8083 was already in use` | Fut még egy korábbi példány → állítsd le (piros ■), vagy zárd be a futó Java folyamatot |
| `No qualifying bean of type 'PetRepository' ... expected single matching bean but found 2` | Hiányzik a `@Qualifier("mock")` |
| `No qualifying bean of type 'PetService'` / `'SecurityService'` | Nincs implementáció `@Service`-szel, vagy rossz csomagban van (nem a main osztály alatt) |
| Belépés után sem enged be / alap Spring login oldal jön | Hiányzik a `@Configuration`/`@Bean` a SecurityConfig-ból, vagy nincs `loginView(LoginView.class)` |
| `Could not navigate to '...' Reason: Access is denied` | A view-n nincs `@PermitAll` / `@AnonymousAllowed`, vagy nincs meg a `@RolesAllowed` szerep. A REGISTER nélküli usernél **ez a helyes** viselkedés. |
| `Could not navigate to ''` belépés után | Nincs view a `/` útvonalon → `@RouteAlias("")` a ListView-ra |
| `AccessDeniedException` a service-ben | A `@PreAuthorize` működik, csak olyan user hívta, akinek nincs joga |
| Felirat helyett `!{kulcs}!` jelenik meg | Elírtad a kulcsot, vagy nincs benne a `translations.properties`-ben |
| Üres a lista a 8-3 után | Enum vs String összehasonlítás → `.name()` kell |
| `hasRole('ROLE_REGISTER')` nem működik | A `hasRole` magától hozzáteszi a `ROLE_`-t, csak `hasRole('REGISTER')` kell |

## ZH-taktika
1. Először **SecurityConfig** (be tudj lépni), utána **PetServiceImpl**, aztán a view-k.
2. Gyakran fordíts! `mvn clean package` – ha nem fordul, elégtelen.
3. Minden szöveg `getTranslation(...)`.
4. Minden view kap `@Route` + jogosultság annotációt + `HasDynamicTitle`-t.
