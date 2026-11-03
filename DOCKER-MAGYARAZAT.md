# Docker: amit a ZH-hoz használni kell tudni

A tanár szerint Dockerből legfeljebb a használat lehet a ZH-ban. Ez az anyag ezért a kész környezet elindítására és olvasására koncentrál.

**Fontos:** az órai Docker-anyag nincs meg a mappáidban, ezért ez általános összefoglaló, és lehet, hogy az óra mást emelt ki. Hasonlítsd össze az órán látott parancsokkal.

## Mire jó?

Tegyük fel, hogy az alkalmazásodnak PostgreSQL kell. Docker nélkül telepítened és be is kellene állítanod. Dockerrel egy paranccsal elindul egy kész, előre beállított adatbázis, és egy másik paranccsal el is tűnik.

## Fogalmak

| Fogalom | Mit jelent | Hasonlat Javából |
|---|---|---|
| **Image** | Kész, csak olvasható csomag, ebből indul a konténer | Osztály |
| **Container** | Egy image futó példánya | Objektum |
| **Docker Hub** | Innen tölti le az image-eket | Maven Central |
| **Port mapping** | A gépedről elérhetővé teszi a konténer portját | |
| **Volume** | A konténeren kívül megmaradó adat | |
| **Compose** | Egy vagy több konténer leírása fájlban | |

## Elindítás előtt

**A Docker Desktopnak futnia kell.** Windowson indítsd el, és várd meg, amíg kész. Ha nem fut, ezt a hibát kapod, nálam is ez jött:

```text
failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine;
check if the path is correct and if the daemon is running
```

Ez nem a projekted hibája, csak el kell indítani a programot.

## A legfontosabb parancsok

Compose-zal, a `compose.yaml` mappájában kiadva:

```bash
docker compose up -d
```

Elindítja a fájlban leírt konténereket. A `-d` miatt a háttérben futnak, és visszakapod a terminált. Első alkalommal letölti az image-et is, ehhez internet kell.

```bash
docker ps
```

Listázza a futó konténereket. Itt ellenőrizd, hogy elindult-e, és melyik porton van. A `docker ps -a` a leállítottakat is mutatja.

```bash
docker compose logs
```

Megmutatja a konténer naplóját. Ha valami nem indul el, először ezt nézd meg. A `-f` kapcsolóval folyamatosan követi.

```bash
docker compose stop
```

Leállítja a konténereket, de megtartja őket.

```bash
docker compose down
```

Leállítja és törli a konténereket. A volume-ban lévő adat megmarad.

```bash
docker compose down -v
```

A volume-ot is törli, tehát **az adatbázis tartalma elvész**.

Compose nélkül, egy konténerre:

```bash
docker run -d --name prog5-db -p 5433:5432 -e POSTGRES_PASSWORD=titok postgres:17
```

| Rész | Jelentés |
|---|---|
| `-d` | Háttérben fut |
| `--name prog5-db` | Ezen a néven hivatkozol rá |
| `-p 5433:5432` | `gép:konténer` port |
| `-e NÉV=érték` | Környezeti változó, ezzel állítod be a konténert |
| `postgres:17` | `image:verzió` |

További hasznos parancsok:

```bash
docker stop prog5-db
```

```bash
docker rm prog5-db
```

```bash
docker images
```

```bash
docker exec -it prog5-db psql -U postgres
```

Az utolsó a konténeren belül indít parancsot, itt az adatbázis konzolját.

## Compose fájl olvasása

Ha a ZH-n kapsz ilyet, ezekre van szükséged belőle:

```yaml
services:
  db:                                 # a szolgáltatás neve
    image: postgres:17                # miből induljon
    ports:
      - "5433:5432"                   # gép portja : konténer portja
    environment:                      # beállítások
      POSTGRES_DB: filmtar            # adatbázis neve
      POSTGRES_USER: prog5            # felhasználó
      POSTGRES_PASSWORD: titok        # jelszó
    volumes:
      - db-data:/var/lib/postgresql/data   # itt marad meg az adat

volumes:
  db-data:
```

**Honnan tudod, mit kell beírni az alkalmazásba?** Mindent ebből a fájlból olvasol ki:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/filmtar
spring.datasource.username=prog5
spring.datasource.password=titok
```

| Érték | Honnan jön |
|---|---|
| `localhost` | Az alkalmazásod a gépeden fut, nem konténerben |
| `5433` | A `ports` sor **bal** oldala |
| `filmtar` | `POSTGRES_DB` |
| `prog5`, `titok` | `POSTGRES_USER`, `POSTGRES_PASSWORD` |

Ehhez az alkalmazásban adatbázis-függőségek is kellenének. A Gyak2–6 projektekben nincs adatbázis, ezért ha ilyen lesz a ZH-ban, valószínűleg a kiinduló projekt tartalmazza majd.

**Csak ha az alkalmazás is konténerben fut:** akkor a cím `db:5432`. A szolgáltatás neve lesz a gépnév, és a jobb oldali port számít.

## A port a leggyakoribb félreértés

```text
"5433:5432"
  │     │
  │     └─ a konténeren belüli port, a program itt figyel
  └─────── a te gépeden lévő port, ide csatlakozol
```

Ha már fut valami a gépeden az 5432-es porton, a bal oldali számot kell átírni. A jobb oldalit nem, mert a konténerben a program továbbra is azon figyel.

## Tipikus hibák

| Hibaüzenet vagy tünet | Ok | Megoldás |
|---|---|---|
| `failed to connect to the docker API` | Nem fut a Docker Desktop | Indítsd el |
| `port is already allocated` | Foglalt a gép portja | Írd át a bal oldali számot, vagy állítsd le a másik programot |
| `no configuration file provided` | Rossz mappában vagy | Lépj a `compose.yaml` mappájába |
| `container name ... is already in use` | Már van ilyen nevű konténer | `docker rm` a régire |
| `Connection refused` az alkalmazásból | Nem fut a konténer, vagy rossz a port | `docker ps`, majd a bal oldali port |
| `password authentication failed` | Rossz adatok, vagy régi volume maradt | Nézd meg a compose fájlt, szükség esetén `down -v` |
| A konténer azonnal leáll | Hibás beállítás | `docker compose logs`, vagy `docker ps -a` |

Azért jöhet elő a `password authentication failed`, mert az adatbázis a jelszót csak az első induláskor állítja be. Ha később átírod a compose fájlt, a régi volume a régi jelszót tartja meg.

## Spring Boot kiegészítés

Ha a `pom.xml`-ben szerepel a `spring-boot-docker-compose` függőség, és a projekt gyökerében van `compose.yaml`, akkor a Spring Boot indításkor maga elindítja a konténereket, és beállítja a kapcsolatot. Ilyenkor a `docker compose up` parancs és a `spring.datasource` sorok sem feltétlenül kellenek, de a Docker Desktopnak ekkor is futnia kell.

## Csak felismerés szintjén: Dockerfile

Ha a tanár szava szerint csak a használat kell, ilyet valószínűleg nem kell írnod, de érdemes felismerni. Ebből készül a saját image:

```dockerfile
FROM eclipse-temurin:25-jre
COPY target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

| Sor | Jelentés |
|---|---|
| `FROM` | Melyik image-ből indul |
| `COPY` | Fájl bemásolása az image-be |
| `EXPOSE` | Melyik porton figyel a program |
| `ENTRYPOINT` | Mit futtat induláskor |

## Gyors recept a ZH-ra

1. Indítsd el a Docker Desktopot, még a feladat előtt. **Induljon el már a ZH elején, mert nem azonnal lesz kész.**
2. Lépj a `compose.yaml` mappájába.
3. Futtasd a `docker compose up -d` parancsot.
4. A `docker ps` paranccsal nézd meg, hogy fut-e.
5. A compose fájlból írd át a portot, a felhasználót és a jelszót az alkalmazás beállításaiba.
6. Hibánál nézd meg a `docker compose logs` kimenetét.
7. A végén futtasd a `docker compose down` parancsot.

## Mit ellenőriztem ebből?

- **Compose példa:** a `docker compose config` elfogadta, tehát a fájl formailag helyes.
- **Parancsok:** nem futtattam őket, mert a gépeden nem futott a Docker Desktop, és nem akartam elindítani vagy image-et letölteni. A parancsok általános Docker-ismereten alapulnak, ezért érdemes egyszer kipróbálnod.
- **`spring.datasource` sorok:** valódi adatbázissal nem próbáltam ki őket.
- **Első indulás:** az image letöltéséhez internet kell, ezért a ZH előtt egyszer futtasd le, hogy a gépeden legyen.
