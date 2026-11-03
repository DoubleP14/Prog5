# Gyakorló ZH (Könyvtár, bővített) – megoldás feladatonként

> A feladat: [FELADAT.md](FELADAT.md). A kiinduló projekt: `../gyakorlo-zh-konyvtar`, a kész kód: ez a mappa.
> A kódban `// 5-1` jellegű kommentek mutatják, melyik sor melyik feladathoz tartozik. Itt a döntések okát írom le.

**Új fájlok a kiinduló projekthez képest:** `application-local.properties`, `BookServiceImpl`, `SecurityServiceImpl`.
**Módosított fájlok:** `application.properties`, `SecurityConfig`, `Book`, `MainLayout`, `LoginView`, `BookListView`, `BookFormView`.
**Nem kellett módosítani:** a repository-k, a `BookService`, a `SecurityService`, a fordítások, a `ValidationMessages` fájlok és a `pom.xml`.

**Ami új a korábbi ZH-khoz képest:** [4-2 nyelvváltó](#4-2-nyelvváltó), [5-1 üres lista és gomb](#5-1-booklistview), [6-1 annotációk](#6-1-validálás-annotációkkal), [6-2 BeanValidationBinder](#6-2-bookformview), [6-3 szerkesztés](#6-3-szerkesztés), [8-3 ConfirmDialog](#8-3-könyv-törlése).

---

## 1. Port beállítás
- `application.properties`: `server.port=8091`
- **Új fájl** `application-local.properties`: `server.port=8092`

A profil neve a fájlnévből jön: `application-<profil>.properties`. Ha aktív a `local` profil, ez felülírja az alap értéket.

**Ellenőrzés:** a konzolban a `Tomcat started on port ...` sor.

---

## 2. Ki-/bejelentkezés

### 2-1 Bejelentkező képernyő
**`SecurityConfig`:** a `securityFilterChain` bean mondja meg, hogy a `LoginView` a bejelentkező oldal.

**`LoginView`:**
- `@Route("login")`: nincs `layout`, mert belépés előtt nincs kinek fejlécet mutatni.
- `@AnonymousAllowed`: enélkül a belépő oldalt sem érné el senki.
- `setAction("login")`: a Spring Security-nek küldi az adatokat.
- A `beforeEnter` a `/login?error` címet figyeli, és `setError(true)` jelzi a hibát.

### 2-2 Bejelentkezés: a négy hiba
A `SecurityConfig` szándékosan hibás volt:

| # | Hiba | Javítás | Tünet |
|---|---|---|---|
| 1 | Nincs `@Configuration` | Oda kell írni | A Spring be sem olvassa az osztályt |
| 2 | Nincs `@Bean` a metóduson | Oda kell írni | A felhasználók nem jutnak el a Springhez |
| 3 | Az `editor` jelszavából hiányzik a `{noop}` | `{noop}password` | Az `editor` nem tud belépni |
| 4 | A managernek csak `admin, reader` van átadva | `admin, editor, reader, scifan` | Az `editor` és a `scifan` nem létezik |

> A 3. és a 4. hiba fordításkor nem jelez, és az app is elindul. Csak akkor derül ki, ha **mind a négy felhasználóval** belépsz.

---

## 3. BookService
**Új fájl:** `BookServiceImpl`, `@Service` annotációval.

- `@Qualifier("memory")`: két `BookRepository` bean van, ezzel választasz név szerint. Így nem kell importálni a `MemoryBookRepository`-t.
- `listGenres`: `Arrays.asList(BookGenre.values())`.

---

## 4. Header

### 4-1 Fejléc
A `MainLayout` a konstruktorban kéri az `AuthenticationContext`-et, ebből jön a név (`getPrincipalName`) és a kijelentkezés (`logout`). A menü `RouterLink`-ekből áll, az `addToNavbar` teszi a fejlécbe. A `@PermitAll` a layoutra is rákerült.

### 4-2 Nyelvváltó
```java
ComboBox<Locale> language = new ComboBox<>();
language.setPlaceholder(this.getTranslation("header.language"));
language.setItems(Locale.of("hu"), Locale.ENGLISH);
language.setItemLabelGenerator(l -> l.getDisplayLanguage(l));
language.setValue(Locale.of(UI.getCurrent().getLocale().getLanguage()));
language.addValueChangeListener(e -> {
    if (e.getValue() != null) {
        VaadinSession.getCurrent().setLocale(e.getValue());
        UI.getCurrent().getPage().reload();
    }
});
```
- **`getDisplayLanguage(l)`:** minden nyelv a saját nevén jelenik meg: `magyar`, `English`.
- **`Locale.of(... .getLanguage())`:** a böngésző nyelve lehet `hu_HU`, a listában viszont `hu` van. Ezért csak a nyelv részét használom, így biztosan megegyezik a lista egyik elemével.
- **`VaadinSession...setLocale`:** megjegyzi a nyelvet a munkamenetre.
- **`reload()`:** a feliratok a konstruktorokban állnak össze, ezért újra kell építeni az oldalt.
- **A `null` ellenőrzés:** ha valaki törli a mező tartalmát, ne fusson hibára.
- **A fordításfájlok:** a `translations_hu.properties` és a `translations_en.properties` is kell. A Vaadin a nyelvet ezek közül választja.
- **Kijelentkezéskor** a munkamenet megszűnik, így a nyelv visszaáll a böngészőére.

---

## 5. Listázás

### 5-1 BookListView
- `new Grid<>(Book.class, false)`: a `false` miatt nem generál magától oszlopokat, így a sorrendet te adod meg.
- A műfaj fordítása: `"book.genre." + b.getGenre().name()` → `book.genre.CRIME` → „krimi”.
- `SecurityService` kell az Azonosító oszlophoz. Interface-ből nem lesz bean, ezért van az üres `SecurityServiceImpl`.
- **Üres lista:** `grid.setEmptyStateText(getTranslation("book.list.empty"))`.
- **Új könyv gomb:** `UI.getCurrent().navigate(BookFormView.class)`, és `setVisible(editor)`.

### 5-2 Routing
- `@Route(value = "books", layout = MainLayout.class)`
- `@RouteAlias(value = "", layout = MainLayout.class)`: belépés után a Vaadin a `/` címre visz.

---

## 6. Könyv felvétele és szerkesztése

### 6-1 Validálás annotációkkal
```java
@NotBlank(message = "{book.required}")
private String title;

@NotNull(message = "{book.required}")
private Integer publishYear;
```
- **`@NotBlank`** Stringre való: nem lehet `null`, üres vagy csak szóköz. A `@NotNull` önmagában átengedné az üres szöveget, mert a `TextField` üresen `""` értéket ad.
- **`{book.required}`:** a kapcsos zárójel azt jelenti, hogy a szöveg a `ValidationMessages` fájlokból jön, nyelv szerint. Ez külön fájl, nem a `translations`.
- A `pom.xml`-ben kell hozzá a `spring-boot-starter-validation`. Itt a kiinduló projektben benne van, de **a `zh-vaadin` `pom.xml`-jében nincs**.

### 6-2 BookFormView
A `BeanValidationBinder` az annotációkat is figyelembe veszi, ezért itt nem kell `asRequired`:
```java
private final BeanValidationBinder<Book> binder = new BeanValidationBinder<>(Book.class);

this.binder.forField(title).bind("title");
this.binder.forField(publishYear)
        .withValidator(y -> y == null || (y >= 1450 && y <= 2100), this.getTranslation("validation.year"))
        .bind("publishYear");
```
- **A `bind("title")` a mező nevét várja** Stringként. Ha elírod, nem a fordításkor derül ki, hanem futáskor.
- **Csapda: a `y == null ||` rész.** A validátor üres mezőre is lefut. Enélkül `null`-ból akarna számot olvasni, és hibára futna. A `null` esetet a `@NotNull` kezeli.
- **Kötelező jelölés:** az `IntegerField` és a `RadioButtonGroup` megkapta a jelölést, a `@NotBlank` mezők nem. A hibaüzenet ettől még megjelenik. Ha a jelölés is kell, írd oda: `title.setRequiredIndicatorVisible(true)`.
- **Mentés:** a `writeBeanIfValid` `false` értéknél nem ír a `book`-ba, ilyenkor jön a `Notification`.

### 6-3 Szerkesztés
Az űrlap ugyanaz, csak a `book` honnan jön:
```java
@Override
public void beforeEnter(BeforeEnterEvent event) {
    this.book = event.getRouteParameters().get("uid")
            .flatMap(this.bookService::findByUid)
            .orElseGet(Book::new);
    this.binder.readBean(this.book);
}
```
- **`getRouteParameters().get("uid")`:** az útvonal `:uid` részét adja. Új könyvnél üres.
- **`flatMap`:** a `findByUid` maga is `Optional`-t ad vissza, így nem lesz `Optional<Optional<Book>>`.
- **`orElseGet(Book::new)`:** ha nincs uid vagy nincs ilyen könyv, új, üres könyv lesz.
- **`readBean`:** a könyv adatait beírja a mezőkbe. Új könyvnél üres mezőket jelent.
- **Miért nem jön létre új könyv?** Az `uid` a `book` objektumban marad, a repository `save` metódusa pedig a régit kicseréli. A módosított könyv ezért a lista végére kerül, ez a kapott repository működése.
- **A gomb a listában:** `UI.getCurrent().navigate("books/" + book.getUid() + "/edit")`.

> A `beforeEnter` után áll be a `book`, ezért kell a konstruktor helyett itt betölteni. A konstruktor még nem látja az útvonal paramétereit.

### 6-4 Routing
```java
@Route(value = "books/new", layout = MainLayout.class)
@RouteAlias(value = "books/:uid/edit", layout = MainLayout.class)
```

---

## 7. Képernyők címe
Mindhárom view `implements HasDynamicTitle`. A `BookFormView` címe függ attól, hogy új könyvről van-e szó:
```java
return this.getTranslation(this.book == null || this.book.getUid() == null
        ? "title.book.new" : "title.book.edit");
```

---

## 8. Jogosultság

### 8-1
`@EnableMethodSecurity` a `SecurityConfig`-on. Nélküle a többi annotáció hatástalan, és ezt a fordító nem jelzi.

### 8-2 Könyv felvétele és szerkesztése
| Hol | Mivel |
|---|---|
| `save`, `findByUid`, `listGenres` | `@PreAuthorize("hasRole('EDITOR')")` |
| Menüpont | `create.setVisible(authenticationContext.hasRole("EDITOR"))` |
| Új könyv gomb, szerkesztés gomb | `setVisible(this.editor)` |
| `BookFormView` (mindkét útvonal) | `@RolesAllowed("EDITOR")` |

### 8-3 Könyv törlése
A Műveletek oszlop soronként egy `HorizontalLayout`-ot ad, benne a két gombbal:
```java
var actionsColumn = this.grid.addComponentColumn(this::createActions);
actionsColumn.setVisible(this.admin || this.editor);
```
A törlés először csak kérdez:
```java
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
```
- **`getTranslation(kulcs, book.getTitle())`:** a szövegben lévő `{0}` helyére kerül a cím.
- **`setCancelable(true)`:** enélkül nincs mégse gomb.
- **Csapda: a `dialog.open()`.** Ha kimarad, nem hibázik, csak nem történik semmi.
- A törlés a megerősítés eseményében fut, nem a gomb megnyomásakor.
- A `refresh()` azért kell, mert a `Grid` magától nem tudja, hogy a lista megváltozott.
- A `delete` metódust a `@PreAuthorize("hasRole('ADMIN')")` védi, a gomb elrejtése csak a felületet érinti.

### 8-4 Könyvek szűrése (bónusz)
```java
@PostFilter("@securityService.getRolesAsString().contains(filterObject.genre.name())")
```
A `genre` enum, a szerepek String-ek, ezért kell a `.name()`. A `findAll` másolatot ad vissza, így a szűrés nem törli a könyveket az eredeti listából.

---

## Ellenőrzés

A bővített, végleges változaton:

| Feladat | Teszt | Eredmény |
|---|---|---|
| Fordítás | `clean package` tesztekkel, Java 25, a kiinduló projekt másolatán is | ✅ |
| 1 | Indulás profil nélkül a 8091-es porton | ✅ |
| 2-2 | Belépés: `admin`, `editor`, `reader` | ✅ |
| 4-1 | Név, menü, kijelentkezés | ✅ |
| 4-2 | Magyar → angol: a felület, a cím és a hibaüzenetek is angolok | ✅ |
| 5-1 | `admin`: oszlopok sorrendje, Azonosító, gombok | ✅ |
| 5-1 | Üres lista felirata (`reader`, a regény törlése után) | ✅ |
| 6-1, 6-2 | Üres űrlap: minden mező hibás + figyelmeztetés | ✅ |
| 6-1 | Csak szóköz a címben: hibás | ✅ |
| 6-2 | 1000-es évszám: „Az év 1450 és 2100 között lehet!” | ✅ |
| 6-2 | Mégse gomb: vissza a listára | ✅ |
| 6-2 | `editor` új könyvet ment | ✅ |
| 6-3, 6-4 | Szerkesztés: kitöltött űrlap, a mentés után nincs új könyv | ✅ |
| 7 | Címek, új és szerkesztés külön címmel | ✅ |
| 8-2 | `editor`: szerkesztés gomb, nincs törlés és Azonosító | ✅ |
| 8-2 | `reader`: nincs gomb és menüpont, a `/books/new` tiltott | ✅ |
| 8-3 | Törlés: a mégse megtartja, a megerősítés törli | ✅ |
| 8-4 | `editor`: regény, krimi; `reader`: regény | ✅ |

**Csak a bővítés előtti változaton néztem:**
- a `local` profilt (8092), a `scifan` belépését és a rossz jelszó hibajelzését. Ezek kódja nem változott.

**Nem próbáltam ki:**
- a `delete` és a `findByUid` tiltását jogosultság nélkül, mert a felületen nincs hozzá gomb;
- az `editor` szerkesztését (az `admin` szerkesztett, az `editor` új könyvet mentett);
- a `/books/<rossz azonosító>/edit` címet. A kód szerint ilyenkor üres, új könyv űrlapja nyílik meg.

**Egy szépséghiba:** keskeny ablakban a Műveletek oszlop a táblázat szélén túlnyúlik, és a törlés gomb levágva látszik. A feladat szerint a kinézet nem számít, ezért nem javítottam.
