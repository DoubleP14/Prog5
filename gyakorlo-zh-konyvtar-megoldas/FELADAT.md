# Gyakorló zárthelyi dolgozat – Könyvtár (bővített)

> Ez nem a tanár feladata, hanem egy gyakorlásra írt, a `zh1.pdf` és az órai Gyak6-os példa mintájára készült feladat. Nehezebb és hosszabb, mint a két korábbi ZH-leírás. A 120 perces időkeret csak javaslat, az igazi ZH időtartamát nem ismerem.

## Bevezetés
A mellékelt projektet egészítse ki/javítsa ki a megadott instrukciók alapján. Az induló állapotban minden szükséges függőség és üzenet megtalálható. A felület kinézete nem számít, csak a működés. A dependency injection során, ahol lehetséges, az interface-re hivatkozzon. A felületeken megjelenő szövegeket mindenhol I18n segítségével a `translations` fájlokból rakja fel!

**Ha nem fordul a kód (`mvn clean package`), vagy nem indul el az alkalmazás, az automatikusan elégtelent jelent!**

## Feladatok

### 1. Port beállítás
Állítsa be a konfigurációban, hogy az alkalmazás alapértelmezetten a **8091**-es, `local` profil esetén pedig a **8092**-es porton fusson.

### 2. Ki-/bejelentkezés

#### 2-1 Bejelentkező képernyő
Készítse el a `LoginView` osztályt a Vaadin beépített bejelentkező komponensével. Állítsa be, hogy a bejelentkezés ezen a képernyőn keresztül történjen. Az oldal a `/login` útvonalon legyen. Hiba esetén kerüljön jelölésre, hogy hiba történt!

#### 2-2 Bejelentkezés
A `SecurityConfig` osztályban rögzített **mind a négy** felhasználó jelenleg nem tud bejelentkezni. Javítsa ki a konfigurációt! A jelszó mindenkinél `password`. Több hiba is van az osztályban.

### 3. BookService
Implementálja a `BookService` interface-t, az implementált metódusok a `BookRepository` megfelelő metódusait hívják. A `BookRepository`-nak két implementációja van, `sql` és `memory` néven, ezek közül az utóbbit használja. Az importok között nem szerepelhet a `MemoryBookRepository`.

A `listGenres` a `BookGenre` enum összes értékét adja vissza.

### 4. Header

#### 4-1 Fejléc
Implementálja a `MainLayout` osztályt. A fejlécben szerepeljen a bejelentkezett felhasználó neve, egy menü az 5-ös és 6-os feladat képernyőihez, illetve egy kijelentkezés gomb.

#### 4-2 Nyelvváltó
A fejlécben legyen egy `ComboBox`, amellyel magyar és angol nyelv között lehet váltani. A nyelvek a saját nevükkel jelenjenek meg. A kiválasztott érték az aktuális nyelv legyen. Váltáskor az egész felület az új nyelven jelenjen meg.

### 5. Listázás

#### 5-1 BookListView
Készítse el a `BookListView` képernyőt `Grid` használatával. Az oszlopok sorrendje kötelezően:

| Azonosító | Cím | Szerző | Kiadás éve | Műfaj | Műveletek |
|---|---|---|---|---|---|

- A műfaj a lefordított nevével jelenjen meg (pl. `krimi`, nem `CRIME`).
- Az Azonosító oszlop csak `ADMIN` jogosultságú felhasználónak látszódjon (`SecurityService.getRolesAsString`).
- Ha nincs megjeleníthető könyv, a táblázat a `book.list.empty` szöveget mutassa.
- A táblázat fölött legyen egy „Új könyv” gomb, amely a 6-os feladat képernyőjére navigál.

#### 5-2 BookListView routing
A képernyő a `/books` útvonalon legyen elérhető, a `MainLayout`-ba foglalva. A bejelentkezés után is ez jelenjen meg.

### 6. Könyv felvétele és szerkesztése

#### 6-1 Validálás annotációkkal
Lássa el a `Book` osztály mezőit validációs annotációkkal. A hibaüzenet a `ValidationMessages` fájlok `book.required` kulcsa legyen:

| Mező | Szabály |
|---|---|
| `title`, `author` | nem lehet üres vagy csak szóköz |
| `publishYear`, `genre` | nem lehet `null` |

#### 6-2 BookFormView
Készítse el a `BookFormView` képernyőt a következő mezőkkel, `BeanValidationBinder` használatával:

| Mező | Komponens | Plusz szabály |
|---|---|---|
| Cím | `TextField` | |
| Szerző | `TextField` | |
| Kiadás éve | `IntegerField` | 1450 és 2100 között, saját validátorral (`validation.year`) |
| Műfaj | `RadioButtonGroup` | |

A műfajok listáját a `BookService.listGenres` adja, lefordított névvel.

A mentés gomb megnyomásakor ellenőrizze az adatokat:
- ha hibásak, jelenjen meg figyelmeztető üzenet a felületen (`validation.form`);
- ha helyesek, mentse a könyvet, és navigáljon a listára.

A mégse gomb mentés nélkül navigáljon a listára.

#### 6-3 Szerkesztés
A lista Műveletek oszlopában legyen egy szerkesztés gomb. Ez ugyanazt a `BookFormView` képernyőt nyissa meg, a könyv adataival kitöltve. Mentéskor a meglévő könyv módosuljon, ne jöjjön létre új.

#### 6-4 BookFormView routing
A képernyő két útvonalon legyen elérhető, mindkettő a `MainLayout`-ba foglalva:
- új könyv: `/books/new`
- szerkesztés: `/books/<azonosító>/edit`

### 7. Képernyők címe
Mindegyik képernyőn állítson be lokalizációnak megfelelő címet! A `BookFormView` címe attól függjön, hogy új könyvet vesz fel, vagy szerkeszt (`title.book.new`, `title.book.edit`).

### 8. Jogosultság

#### 8-1 Global method security
Engedélyezze a global method security-t.

#### 8-2 Könyv felvétele és szerkesztése
A `BookService` implementációjában érje el, hogy a `save`, a `findByUid` és a `listGenres` metódust csak `EDITOR` jogosultsággal lehessen meghívni. Az „Új könyv” menüpont és gomb, valamint a szerkesztés gomb is csak ilyen felhasználónak jelenjen meg. A `BookFormView` is csak neki legyen látogatható.

#### 8-3 Könyv törlése
A Műveletek oszlopban legyen egy törlés gomb is. Megnyomásakor egy `ConfirmDialog` kérdezze meg, hogy biztosan törli-e a könyvet (a szövegben szerepeljen a könyv címe, `book.delete.confirm.text`), és legyen mégse lehetőség is. Megerősítés után törölje a könyvet, és frissítse a táblázatot. A gomb csak `ADMIN` jogosultságú felhasználónak látszódjon, és a `BookService.delete` metódust is csak ő hívhassa meg.

#### 8-4 Könyvek szűrése – bónusz feladat
A global method security segítségével érje el, hogy a `BookService.listAll()` csak azokat a könyveket adja vissza, amelyek műfajához a felhasználónak joga van. Krimit például csak `CRIME` jogosultsággal adjon vissza. A jogosultságok lekérdezéséhez használja a `SecurityService.getRolesAsString()` metódust. Figyeljen oda az objektumok típusára!

## Tesztfelhasználók
| Felhasználó | Jelszó | Elvárt működés |
|---|---|---|
| `admin` | `password` | 5 könyv, Azonosító oszlop, szerkesztés és törlés gomb, „Új könyv” menü és gomb |
| `editor` | `password` | regény és krimi, szerkesztés gomb, „Új könyv” menü és gomb, nincs Azonosító és törlés |
| `reader` | `password` | csak regény, nincs gomb és „Új könyv” menü, a `/books/new` tiltott |
| `scifan` | `password` | sci-fi, történelem, költészet, nincs gomb és „Új könyv” menü |

## Beadás előtt
1. Maven: `clean`, majd `package` → `BUILD SUCCESS`.
2. Indítás profil nélkül (8091) és `local` profillal (8092).
3. Belépés mind a négy felhasználóval, a fenti táblázat szerint.
4. Nyelvváltás kipróbálása.
