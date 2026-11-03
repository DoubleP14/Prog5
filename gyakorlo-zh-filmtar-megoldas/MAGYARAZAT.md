# Filmtár: magyarázat (Spring MVC + Thymeleaf)

A feladatok a [FELADAT.md](FELADAT.md) számozását követik, a kódban ugyanezek a számok szerepelnek kommentben.

Belépés: `user` / `user`, `admin` / `admin`. Az alkalmazás címe: http://localhost:8095

## Mi a különbség a Vaadinhoz képest?

Vaadinban Java osztály írja le a képernyőt. Itt három külön rész dolgozik együtt:

```text
böngésző ── GET /movies ──> Controller ── adatok a Modelbe ──> "movies/list"
                                                                  │
böngésző <────────── kész HTML ──────── templates/movies/list.html ┘
```

| Feladat | Vaadin | MVC + Thymeleaf |
|---|---|---|
| Oldal címe | `@Route("movies")` | `@GetMapping("/movies")` |
| Képernyő | `VerticalLayout` osztály | `templates/*.html` |
| Adat átadása | `grid.setItems(...)` | `model.addAttribute(...)` |
| Űrlap kötése | `Binder` | `th:object` + `th:field` |
| Validálás | `binder.writeBeanIfValid` | `@Valid` + `BindingResult` |
| Hibaüzenet | a Binder kiírja | `th:errors` |
| Ki látja | `setVisible(...)` | `sec:authorize` |
| Fordítás | `getTranslation("kulcs")` | `#{kulcs}` |
| Oldal újratöltése | nem kell | minden kérés új oldal, ezért kell a `redirect:` |

## Sorrend a ZH-n

1. `application.yaml`
2. `DataService` és a `MovieService`
3. `SecurityConfig`, `AppUserDetailsService`, login
4. `layout.html`
5. lista
6. űrlap
7. törlés
8. nyelv
9. regisztráció
10. admin és hibaoldal

Minden lépés után indítsd el és nézd meg. Először a saját filmek nélkül is futnia kell a belépésnek.

---

## Projekt létrehozása

Ha nulláról kellene, a start.spring.io oldalon a függőségek: Spring Web, Thymeleaf, Spring Security, Validation, Lombok. Utána vesd össze a `pom.xml`-t az itteni megoldáséval.

**Lombok Java 25-tel:** a kiinduló `pom`-ban ez be van állítva. Nélküle nálam ez lett a `mvnw clean package` eredménye:

```text
cannot find symbol: method setId(java.lang.Long)
```

A hiba oka, hogy a fordító nem futtatta a Lombokot, így nem jöttek létre a getterek és setterek. A javítás a `maven-compiler-plugin` `annotationProcessorPaths` része a `pom.xml`-ben. A Gyak5 `pom`-jában ez nincs benne, ezért ott is figyelj rá.

## 1. Konfiguráció

```yaml
spring:
  profiles:
    active: mock
server:
  port: 8095
```

Yaml-ban a behúzás a szerkezet része, ezért két szóközzel beljebb kell írni. Ugyanez properties-ben `spring.profiles.active=mock` és `server.port=8095`.

## 2. Adatok

### 2-1. Profil

```java
@Service
@Profile("mock")
public class InMemoryMockDataService implements DataService { ... }
```

A `@Profile("mock")` azt jelenti, hogy a bean csak akkor jön létre, ha ez a profil aktív. A `MovieService` csak a `DataService` interfészt ismeri, így profilváltáskor a kódján nem kell változtatni.

| Helyzet | Eredmény |
|---|---|
| Egy profil aktív | Egy `DataService` bean van, minden működik |
| Egyik sem aktív | Nincs bean, az alkalmazás el sem indul |
| Mindkettő aktív | Két bean van, a Spring nem tud választani |

Másik profillal így indíthatod:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=plain
```

### 2-2. @PostConstruct és a save

A `@PostConstruct` metódus akkor fut le, amikor a bean elkészült és az injektálás megtörtént. A konstruktorban ugyanez is megoldható lenne, de a Gyak3 is így csinálja.

Az `init` nem a `save` metódust hívja, mert a `save` a bejelentkezett felhasználót kéri le, indításkor viszont még senki nincs bejelentkezve.

```java
movie.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
```

Ezzel kérdezed le bárhonnan, ki van bejelentkezve.

## 3. Biztonság

### 3-1. SecurityConfig

A Vaadinos megoldásban a `VaadinSecurityConfigurer` beállított mindent. Itt te mondod meg:

```java
http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/css/**").permitAll()
        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
        .requestMatchers("/login", "/register", "/error").permitAll()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated())
```

**A sorrend számít.** Az első illeszkedő szabály dönt, ezért az `anyRequest()` a végére kerül.

| Sor | Mi történik nélküle |
|---|---|
| `/css/**` | A belépőoldal formázás nélkül jelenik meg |
| `ERROR` dispatcher | A hibaoldal megjelenítése is a belépésre irányíthat |
| `/login` | A belépőoldal saját magára irányít végtelenül |

```java
.formLogin(login -> login
        .loginPage("/login")           // a mi GET /login oldalunk
        .defaultSuccessUrl("/movies")  // ide visz a belépés után
        .failureUrl("/login?error"))   // ide rossz jelszónál
```

A belépés után a cím `/movies?continue` lehet. Ez a Spring Security jelölése, nem hiba.

A `PasswordEncoder` beanre azért van szükség, mert a jelszó nem szövegként, hanem BCrypt hash-ként tárolódik. A Vaadinos ZH `{noop}` megoldása helyett itt ez a szabály.

### Method security

A sima `@EnableMethodSecurity` a `@PreAuthorize`, `@PostAuthorize`, `@PreFilter` és `@PostFilter` annotációt kapcsolja be. A másik kettőhöz külön kapcsoló kell:

| Annotáció | Példa | Bekapcsolás |
|---|---|---|
| `@PreAuthorize` | `@PreAuthorize("hasRole('ADMIN')")` | alapból működik |
| `@Secured` | `@Secured("ROLE_ADMIN")` | `securedEnabled = true` |
| `@RolesAllowed` | `@RolesAllowed("ADMIN")` | `jsr250Enabled = true` |

A `hasRole('ADMIN')` a `ROLE_ADMIN` jogot keresi. A `hasAuthority('EDIT_FORM')` pontosan azt a nevet keresi, előtag nélkül. Az `@Secured` is teljes nevet vár.

### 3-2. AppUserDetailsService

Belépéskor a Spring Security meghívja a `loadUserByUsername` metódust, és a kapott hash-t összeveti a beírt jelszóval. Az ellenőrzést nem te írod meg.

A `.roles("USER")` hozzáteszi a `ROLE_` előtagot, ezért lesz belőle `ROLE_USER`.

A `UserDetailsService` és a `PasswordEncoder` beant nem kell sehol összekötni, a Spring megtalálja őket.

### 3-3. Login oldal

Csak a `GET /login` a miénk. A `POST /login` kérést a Spring Security kezeli, ezért nincs hozzá controller metódus.

```html
<form th:action="@{/login}" method="post">
    <input name="username">
    <input type="password" name="password">
```

Két dolog kötelező:

- **A mezők neve** `username` és `password` legyen.
- **`th:action` kell, nem sima `action`.** A Thymeleaf csak így teszi bele a rejtett CSRF tokent. Token nélkül a POST kérésre 403 a válasz, ezt ki is próbáltam.

```html
<p th:if="${param.error}" th:text="#{login.error}"></p>
```

A `param` az URL paramétereit éri el. A `/login?error` címnél van `error`, ezért jelenik meg a szöveg.

## 4. Fragmentek

A fragment újrahasznosítható HTML-darab, olyan, mint egy metódus.

```html
<!-- Definíció a fragments/layout.html fájlban -->
<head th:fragment="head(title)">
    <title th:text="${title}"></title>
</head>

<!-- Használat -->
<head th:replace="~{fragments/layout :: head(#{title.movie.list})}"></head>
```

| Attribútum | Mit csinál |
|---|---|
| `th:replace` | A saját elemét is lecseréli a fragmentre |
| `th:insert` | Megtartja az elemét, a fragment belekerül |

```html
<html xmlns:th="https://www.thymeleaf.org" xmlns:sec="https://www.thymeleaf.org/extras/spring-security">

<a sec:authorize="hasRole('ADMIN')" ...>       <!-- csak ADMIN-nak jelenik meg -->
<span sec:authentication="name"></span>        <!-- a felhasználónév -->
```

A `sec:` a `thymeleaf-extras-springsecurity6` függőségből jön.

A kijelentkezés POST kérés, ezért `<form>` kell hozzá, nem link.

## 5. Lista

```java
@GetMapping
public String list(Model model) {
    model.addAttribute("movies", this.movieService.list());
    return "movies/list";
}
```

A visszaadott szöveg a sablon neve: `templates/movies/list.html`. A `Model` a controller és a sablon közti csomag. A `movies` névvel a sablonban `${movies}` érhető el.

```html
<tr th:if="${movies.isEmpty()}">
    <td colspan="6" th:text="#{movie.list.empty}"></td>
</tr>
<tr th:each="movie : ${movies}">
    <td sec:authorize="hasRole('ADMIN')" th:text="${movie.id}"></td>
    <td th:text="${movie.title}"></td>
</tr>
```

A `th:each` a Java for-each megfelelője, a `${movie.title}` pedig a `getTitle()` metódust hívja.

**Kifejezések, ezeket érdemes megjegyezni:**

| Jelölés | Jelentés | Példa |
|---|---|---|
| `${...}` | Változó a modellből | `${movie.title}` |
| `*{...}` | A `th:object` mezője | `*{title}` |
| `#{...}` | Fordítás a messages fájlból | `#{movie.title}` |
| `@{...}` | URL | `@{/movies/{id}/edit(id=${movie.id})}` |
| `~{...}` | Fragment | `~{fragments/layout :: header}` |

## 6. Űrlap

### 6-1. Annotációk

```java
@NotBlank(message = "{validation.notBlank}")
private String title;
```

A `{...}` kulcsot a Spring Boot a `messages` fájlokban is megtalálja, ezért elég egy helyen tartani a szövegeket. A hibaüzenet így a kiválasztott nyelven jelenik meg, ezt magyarul és angolul is ellenőriztem.

| Annotáció | Mire való | Példa |
|---|---|---|
| `@NotBlank` | Szöveg, ne legyen üres | cím |
| `@NotNull` | Bármi, ne legyen null | szám mező |
| `@Min`, `@Max` | Számtartomány | év |
| `@Size` | Szöveg hossza | jelszó |

### 6-2. Új film

Az űrlaphoz két metódus kell:

```java
@GetMapping("/new")               // megjeleníti az üres űrlapot
public String newForm(Model model) {
    model.addAttribute("movie", new Movie());
    return "movies/form";
}

@PostMapping                      // feldolgozza a beküldött adatot
public String create(@Valid @ModelAttribute("movie") Movie movie, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
        return "movies/form";
    }
    this.movieService.save(movie);
    return "redirect:/movies";
}
```

- **`new Movie()` a GET-ben:** a `th:object` üres objektum nélkül hibát dobna.
- **`@ModelAttribute`:** a Spring a beküldött mezőkből kitölti a `Movie` objektumot.
- **`@Valid`:** lefuttatja az annotációkban megadott ellenőrzést.
- **`BindingResult`:** itt vannak a hibák. **Közvetlenül a `@Valid` paraméter után kell állnia.** Ha hiányzik, hibás adatnál az űrlap helyett hibaoldal jön. A Gyak3–5 `update` metódusában nincs benne, ott pontosan ez történne.
- **`redirect:`:** mentés után a böngésző új GET kérést küld. Enélkül a frissítés újra elküldené az űrlapot, és duplán mentene.

```html
<form th:object="${movie}" th:action="...">
    <label for="title" th:text="#{movie.title}"></label>
    <input type="text" th:field="*{title}"/>
    <span class="error" th:errors="*{title}"></span>
</form>
```

A `th:field` beállítja a mező `id`, `name` és `value` attribútumát, így a hibás űrlapon a beírt adat megmarad. A `th:errors` kiírja az adott mező hibáját, ha van.

**A mezőfragment:**

```html
<div th:fragment="field(name,label,type)">
    <label th:for="${name}" th:text="${label}"></label>
    <input th:type="${type}" th:field="*{__${name}__}"/>
    <span class="error" th:errors="*{__${name}__}"></span>
</div>
```

A `__${name}__` előfeldolgozás: először behelyettesíti a nevet, így `*{title}` lesz belőle. A fragmentet a `<form>` belsejében kell használni, mert a `*{...}` a `th:object` objektumra vonatkozik.

**Szöveg a szám mezőben:** a `typeMismatch.java.lang.Integer` kulcs adja az üzenetet. Nélküle egy hosszú angol kivételszöveg jelenne meg.

### 6-3. Szerkesztés

```html
<h1 th:text="${movie.id == null} ? #{title.movie.new} : #{title.movie.edit}"></h1>
<form th:action="${movie.id == null} ? @{/movies} : @{/movies/{id}(id=${movie.id})}">
```

Egy sablon kezeli mindkét esetet, a különbség csak az, hogy van-e `id`.

```java
@PostMapping("/{id}")
public String update(@PathVariable Long id, @Valid @ModelAttribute("movie") Movie movie,
                     BindingResult bindingResult) {
    Movie existing = this.movieService.findById(id);
    if (existing == null) {
        return "redirect:/movies";
    }
    movie.setId(id);
    if (bindingResult.hasErrors()) {
        return "movies/form";
    }
    movie.setOwner(existing.getOwner());
    this.movieService.save(movie);
    return "redirect:/movies";
}
```

Miért ilyen a sorrend:

- **`setId` a hibaellenőrzés előtt:** így hibás adatnál is „Film szerkesztése” marad a cím.
- **`setOwner`:** a Spring teljesen új `Movie` objektumot tölt ki az űrlapból. Tulajdonos mező nincs az űrlapon, ezért `null` lenne, és a film eltűnne a tulajdonos listájából.

## 7. Törlés

```html
<form sec:authorize="hasRole('ADMIN')" th:action="@{/movies/{id}/delete(id=${movie.id})}" method="post"
      th:data-confirm="#{movie.delete.confirm}" onsubmit="return confirm(this.dataset.confirm);">
    <button type="submit" class="danger" th:text="#{movie.delete}"></button>
</form>
```

- **POST, nem link:** adatot módosító művelet ne legyen GET, és így a CSRF védelem is érvényes rá.
- **`data-confirm`:** a Thymeleaf biztonsági okból nem enged szöveges kifejezést a `th:onsubmit` attribútumban. Ezért a szöveg `data-` attribútumba kerül, a JavaScript onnan olvassa.
- **Mégse:** a `confirm` ekkor `false`-t ad, és a `return false` megállítja a küldést.

**Miért kell két helyen a védelem?** A `sec:authorize` csak elrejti a gombot. A kérést kézzel így is el lehet küldeni, ezért kell a service metódusra a `@PreAuthorize`. Kipróbáltam: `user` felhasználóval a POST kérésre 403 jött, és a film megmaradt.

## 8. Többnyelvűség

Két rész kell:

- **`LocaleResolver`:** hol tárolódik a nyelv. A `SessionLocaleResolver` a sessionben. A bean metódus neve maradjon `localeResolver`, mert a Spring név szerint keresi.
- **`LocaleChangeInterceptor`:** minden kérésnél figyeli a `lang` paramétert, és átállítja a nyelvet.

```html
<a href="?lang=en">EN</a>
```

A `?` kezdet miatt a link az aktuális oldalra mutat.

A `messages.properties` a magyar alap, a `messages_en.properties` az angol. A Vaadinos `vaadin-i18n/translations` mappára itt nincs szükség, a Spring Boot a gyökérben lévő `messages` fájlokat alapból betölti.

Paraméteres szöveg:

```properties
stats.count=Filmek száma: {0}
```

```html
<p th:text="#{stats.count(${count})}"></p>
```

## 9. Regisztráció

### 9-2. Saját hiba a mezőhöz

Az, hogy foglalt-e a név, annotációval nem ellenőrizhető, ezért a controller teszi hozzá a hibát:

```java
bindingResult.rejectValue("username", "validation.username.exists");
```

Az első paraméter a mező, a második a messages kulcs. A hiba ugyanúgy a `th:errors` helyén jelenik meg. Ezért a saját ellenőrzés a `hasErrors()` hívás előtt legyen.

### 9-3. Automatikus beléptetés

Ez a Gyak4–5 kódja, érdemes érteni a lépéseit:

```java
UserDetails userDetails = this.userDetailsService.loadUserByUsername(registerForm.getUsername());
Authentication auth = UsernamePasswordAuthenticationToken
        .authenticated(userDetails, null, userDetails.getAuthorities());   // 1. a belépett felhasználó
SecurityContext context = SecurityContextHolder.createEmptyContext();
context.setAuthentication(auth);                                           // 2. új kontextusba tesszük
SecurityContextHolder.setContext(context);                                 // 3. érvényes erre a kérésre
this.securityContextRepository.saveContext(context, request, response);    // 4. elmentjük a sessionbe
```

A 4. lépés nélkül a következő kérésnél a felhasználó már nem lenne bejelentkezve.

## 10. Admin oldal és hibaoldal

**10-1.** A `requestMatchers("/admin/**").hasRole("ADMIN")` URL szinten véd, a `@PreAuthorize` metódus szinten. Mindkettő működő megoldás. Ha a feladat URL-t említ, a `SecurityConfig` a jó hely, ha műveletet, akkor a service.

**10-2.** A Spring Boot a hibakód alapján magától megkeresi a `templates/error/403.html` sablont, nem kell hozzá kód.

Ha a hibát `curl` paranccsal kéred le, JSON választ kapsz. Böngészőben a saját oldal jelenik meg, mert a böngésző HTML-t kér.

## 11. Bónusz

### 11-1. @PostFilter

```java
@PostFilter("filterObject.owner == authentication.name or hasRole('ADMIN')")
public Collection<Movie> list() {
    return new ArrayList<>(this.movies.values());
}
```

Lefut a metódus, majd a keretrendszer eltávolítja azokat az elemeket, amelyekre a feltétel hamis. **Módosítható lista kell neki.** `List.of()` eredményével kivételt kapnál, ezért van `new ArrayList<>`.

### 11-2. @PostAuthorize

```java
@PostAuthorize("returnObject == null or returnObject.owner == authentication.name or hasRole('ADMIN')")
public Movie findById(Long id) { ... }
```

Lefut a metódus, és az eredmény alapján dönt. Ha a feltétel hamis, 403 lesz a válasz. Az `update` is a `findById` metódust hívja, ezért idegen film POST kéréssel sem módosítható. Ezt is kipróbáltam.

| Annotáció | Mikor dönt | Mire jó |
|---|---|---|
| `@PreAuthorize` | A metódus előtt | Ki hívhatja |
| `@PostAuthorize` | A metódus után | Megkaphatja-e az eredményt |
| `@PreFilter` | A metódus előtt | A bemeneti lista szűrése |
| `@PostFilter` | A metódus után | A kimeneti lista szűrése |

### 11-3. AOP

```java
@Around("execution(public * hu.mik.pte.prog5.filmtar.service.MovieService.*(..))")
public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    Object result = joinPoint.proceed();   // itt fut le az eredeti metódus
    log.info("[AOP] {} ({} ms)", joinPoint.getSignature().toShortString(), System.currentTimeMillis() - start);
    return result;
}
```

A kifejezés részei: `public` láthatóság, `*` bármilyen visszatérési típus, `MovieService.*` minden metódus, `(..)` bármilyen paraméter.

**Két hiba, amit könnyű elkövetni:**

- A `proceed()` nélkül az eredeti metódus le sem fut.
- A `return result` nélkül a hívó `null` értéket kap.

**Önhívás:** az AOP és a method security proxyn keresztül működik. Ha a `MovieService` egyik metódusa a sajátját hívja, ez a proxyt kikerüli, így a `@PreAuthorize` és az aspect sem fut le rá.

## Tipikus hibák

| Tünet | Valószínű ok |
|---|---|
| `cannot find symbol` getter/setter | Lombok nincs bekötve a `pom.xml`-ben |
| Nem indul, nincs `DataService` bean | Nincs aktív profil |
| 403 POST kérésnél, helyes jogokkal | `action` szerepel `th:action` helyett |
| Hibás űrlapnál hibaoldal jön | Hiányzik a `BindingResult`, vagy nem a `@Valid` után áll |
| `Neither BindingResult nor plain target object` | A GET metódus nem tette a modellbe az objektumot, vagy más a neve |
| `??kulcs_hu??` a felületen | Nincs ilyen kulcs a messages fájlban |
| Formázás nélküli belépőoldal | Hiányzik a `/css/**` `permitAll` |
| `TemplateInputException` | Elírt sablonnév vagy rossz mappa |
| `sec:authorize` nem hat | Hiányzik az extras függőség vagy az `xmlns:sec` |
| Szerkesztés után eltűnik a film | Elveszett a tulajdonos |

## Ellenőrzés

Java 25-tel lefutott a `mvnw clean package` a tesztekkel együtt, és végigpróbáltam a funkciókat HTTP-kérésekkel, majd a fő lépéseket böngészőben is. A `FELADAT.md` ellenőrző listája teljesült, a `plain` profillal együtt.

A kiinduló projekt is fordul, és a tesztje is lefut, de abból a feladatokat nem oldottam meg külön. A megoldás kódja ugyanerre épül.
