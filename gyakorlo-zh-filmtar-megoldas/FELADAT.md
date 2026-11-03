# Gyakorló ZH 2: Filmtár (Spring MVC + Thymeleaf)

Saját gyakorló feladat a Gyak2–5 anyagából, nem a tanártól származik. Azt fedi le, amit a Vaadinos gyakorlók nem: controller, Thymeleaf, kézzel írt Spring Security, profilok.

Javasolt idő: 120 perc. A 11. feladat bónusz.

A projekt fordul, de még nem tud semmit. Készen kapod:

- `pom.xml`
- `Movie`, `AppUser`, `DataService`
- `MovieService` (két TODO-val)
- `messages.properties`, `messages_en.properties`
- `style.css`

Minden felirat a `messages` fájlokból jöjjön. A kulcsokat nézd meg ott.

## 1. Konfiguráció

Az `application.yaml`-ban:

- az alkalmazás a **8095**-ös porton fusson
- alapból a **`mock`** profil legyen aktív

## 2. Adatok

**2-1.** Készíts két `DataService` implementációt:

| Osztály | Profil | Mit ad vissza |
|---|---|---|
| `InMemoryMockDataService` | `mock` | legalább 4 filmet, `user` és `admin` tulajdonossal |
| `InMemoryDataService` | `plain` | üres listát |

**2-2.** A `MovieService`-ben:

- injektáld a `DataService`-t
- induláskor töltsd be a kezdő filmeket, mindegyik kapjon azonosítót
- a `save` új filmnél (`id == null`) adjon azonosítót, és a tulajdonos a bejelentkezett felhasználó legyen

## 3. Biztonság

**3-1.** Készíts `SecurityConfig` osztályt:

- kézzel írt `SecurityFilterChain`, a Vaadinos konfigurátor nélkül
- nyilvános: `/css/**`, `/login`, `/register`, `/error`, valamint a `FORWARD` és `ERROR` dispatcher típus
- minden más csak bejelentkezve érhető el
- saját belépőoldal: `/login`, sikeres belépés után `/movies`, hibánál `/login?error`
- kijelentkezés után `/login?logout`
- a HTTP Basic legyen kikapcsolva
- a jelszavakat BCrypt kódolja
- a method security legyen bekapcsolva

**3-2.** Készíts `AppUserDetailsService` osztályt `UserDetailsService` implementációval, `Map`-ben tárolt felhasználókkal:

| Felhasználó | Jelszó | Szerepek |
|---|---|---|
| `user` | `user` | `USER` |
| `admin` | `admin` | `USER`, `ADMIN` |

**3-3.** Készíts `LoginController` osztályt és `login.html` sablont. Az oldal írja ki a hibás belépés és a sikeres kijelentkezés üzenetét, és legyen rajta link a regisztrációra.

## 4. Közös elemek

Készíts `fragments/layout.html` fájlt két fragmenttel, és minden oldal ezeket használja:

- `head(title)`: oldalcím és a CSS betöltése
- `header`:
  - HU és EN link
  - bejelentkezve a Filmek link, a felhasználónév és a Kijelentkezés gomb
  - ADMIN-nak a Statisztika link

## 5. Lista

**5-1.** A `/` cím irányítson a `/movies` címre. A `GET /movies` jelenítse meg a filmeket.

**5-2.** A `movies/list.html` táblázatának oszlopai: Azonosító, Cím, Rendező, Megjelenés éve, Tulajdonos, Műveletek.

- az Azonosító oszlop csak ADMIN-nak látszik
- üres listánál egy sor írja ki a `movie.list.empty` szöveget
- legyen rajta „Új film” gomb
- a Műveletek oszlopban legyen Szerkesztés gomb

Ellenőrzés: a `plain` profillal üres listát kell látnod.

## 6. Űrlap

**6-1.** Validáció a `Movie` osztályban, a `messages` fájl üzeneteivel:

- `title`, `director`: nem lehet üres
- `releaseYear`: kötelező, 1888 és 2100 között

**6-2.** Új film: `GET /movies/new` az űrlap, `POST /movies` a mentés. Hibánál az űrlap jelenjen meg újra a mezők mellett a hibaüzenetekkel, sikernél irányíts a listára. Készíts `fragments/form-fields.html` fragmentet a címke + mező + hiba hármasnak. Legyen Mégse gomb is.

**6-3.** Szerkesztés: `GET /movies/{id}/edit` az űrlap, `POST /movies/{id}` a mentés.

- ugyanazt a `movies/form.html` sablont használd
- a cím és a form `action` attribútuma az alapján változzon, hogy új filmről vagy szerkesztésről van szó
- a tulajdonos szerkesztéskor ne vesszen el
- nem létező azonosítónál irányíts a listára

## 7. Törlés

**7-1.** `POST /movies/{id}/delete`, a listában piros gombbal. Küldés előtt böngészős `confirm` kérdezzen rá, a szöveg a `movie.delete.confirm` kulcsból jöjjön.

**7-2.** A gomb csak ADMIN-nak látszik, **és** a service metódus is csak ADMIN-nak fut le.

## 8. Többnyelvűség

Készíts `WebConfig` osztályt: a nyelv a sessionben maradjon, alapból magyar, és a `?lang=en` / `?lang=hu` paraméter váltsa. A validációs üzenetek is kövessék a nyelvet.

## 9. Regisztráció

**9-1.** `RegisterForm` osztály (`username`, `password`, `confirmPassword`), `register.html` sablon, `GET` és `POST /register`. A felhasználónév nem lehet üres, a jelszó legalább 4 karakter. A `register` metódust és az `exists` metódust az `AppUserDetailsService`-be írd, új felhasználó szerepe `USER`.

**9-2.** Mezőhöz kötött hibaüzenet, ha a felhasználónév foglalt, vagy ha a két jelszó eltér.

**9-3.** Sikeres regisztráció után a felhasználó automatikusan legyen bejelentkezve, és kerüljön a `/movies` oldalra.

## 10. Admin oldal és hibaoldal

**10-1.** `GET /admin/stats` írja ki a filmek számát a `stats.count` kulccsal. A `/admin/**` címek jogosultságát a `SecurityConfig`-ban állítsd be, csak ADMIN.

**10-2.** A 403-as hibára saját, lefordított oldal jelenjen meg.

## 11. Bónusz

**11-1.** A `list()` csak a saját filmeket adja vissza, az ADMIN mindent lát. Service szinten, `@PostFilter`-rel.

**11-2.** Idegen film szerkesztése címből beírva se menjen: a `findById` `@PostAuthorize`-zal utasítsa el. Az ADMIN mindet elérheti.

**11-3.** AOP: minden `MovieService` hívásról kerüljön naplóbejegyzés a metódus nevével és a futásidővel.

## Ellenőrző lista

- [ ] Belépés nélkül minden a `/login` oldalra visz, de a CSS betölt
- [ ] Rossz jelszóra hibaüzenet, kijelentkezés után üzenet
- [ ] `user` nem lát Azonosító oszlopot, Törlés gombot és Statisztika linket
- [ ] `user` a `/admin/stats` címen a saját 403-as oldalt kapja
- [ ] Üres űrlap mentésekor mindhárom mezőnél hiba van, angolul is
- [ ] Szerkesztés után a tulajdonos nem változik
- [ ] Hibás szerkesztésnél a cím továbbra is „Film szerkesztése”
- [ ] A törlés megerősítésénél a Mégse nem töröl
- [ ] Foglalt névvel és eltérő jelszóval a regisztráció hibát ír, helyes adatokkal belép
- [ ] Ékezetes cím helyesen mentődik
