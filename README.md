# 🎲 Table Online

O platformă web interactivă pentru jocul de table (Backgammon) în timp real. Proiectul utilizează **Java Spring Boot** pentru backend și **WebSockets** pentru a asigura o sincronizare instantanee a mutărilor și a stării jocului între utilizatori.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)

## 📄 Documentație Proiect
Puteți consulta documentația tehnică detaliată în format PDF aici: [documentatie.pdf](./doc/documentatie.pdf)

---

## 🚀 Caracteristici Principale

* **Multiplayer Real-time**: Joc sincronizat prin protocolul WebSocket custom, eliminând necesitatea reîncărcării paginii.
* **Sistem de Lobby**: Permite crearea de camere noi sau alăturarea în camere existente (cu suport pentru parole).
* **Autentificare și Profil**: Sistem complet de Signup/Login cu management de sesiune (`HttpSession`).
* **Dashboard Utilizator**: Vizualizarea statisticilor personale și a istoricului cronologic al meciurilor jucate.
* **Validare pe Server**: Toate aruncările de zaruri și mutările sunt validate pe backend pentru a asigura integritatea regulilor de joc.

---

## 🛠️ Arhitectură și Tehnologii

### Backend
* **Framework:** Spring Boot (MVC, Data JPA).
* **Real-time:** Infrastructură custom bazată pe `TextWebSocketHandler` și interfața `Broadcaster`.
* **Bază de date:** SQL (MySQL/PostgreSQL) gestionat prin Spring Data JPA.
* **Librării:** Lombok, Jackson (pentru procesare JSON).

### Frontend
* **Template Engine:** Thymeleaf pentru randare dinamică.
* **Client WebSocket:** JavaScript nativ integrat cu fluxul de mesaje al serverului.
* **Styling:** CSS3 modern pentru interfața de joc și dashboard.

---

## 📊 Design Tehnic

Sistemul este proiectat pe o arhitectură modulară, punând accent pe decuplarea logicii de business de infrastructura de comunicație și pe securizarea sesiunilor de utilizator.

### 1. Arhitectura WebSocket & Comunicare Real-time
Infrastructura de rețea este gestionată de o ierarhie de clase condusă de `BaseWebSocketHandler`. Aceasta administrează obiectele de tip `Channel` (camere de joc) și `Client` (sesiuni individuale).
* **Broadcasting:** Serverul utilizează o interfață `Broadcaster` pentru a trimite actualizări fie unicast (către un singur jucător), fie broadcast (către ambii participanți dintr-o cameră).
* **Decuplare:** Mesajele recepționate prin socket sunt delegate către `GameManager` sau `LobbyManager`, care implementează `BaseWebSocketListener`. Această abordare asigură independența motorului de joc față de protocolul de transport.

### 2. Specificația Protocolului (Mesaje JSON)
Comunicarea este standardizată printr-o structură generică de tip `Response<T>`, formată din câmpurile `type` (identificatorul acțiunii) și `payload` (datele efective).
* **Requests (Client):** `create_room`, `join_room`, `roll_request`, `move`.
* **Responses (Server):** `state` (starea tablei), `invalid_move` (erori de validare), `timer`, `game_end`.

### 3. Modelul de Date și Persistență
Sistemul utilizează o bază de date relațională (MySQL/PostgreSQL) gestionată prin **Spring Data JPA**. Arhitectura bazei de date este centrată pe următoarele entități:
* **Player:** Stochează informațiile de profil, credențialele și statisticile generale.
* **Match:** Înregistrează detaliile fiecărei partide finalizate (scor, jucători, câștigător, data).
* **Ranking:** Tabelă dedicată pentru agregarea performanțelor, utilizată pentru afișarea clasamentului în timp real.

### 4. Sistemul de Autentificare și Sesiuni
Securitatea accesului este gestionată prin `AuthController` și mecanismele native Spring:
* **Autentificare:** Procesul de login verifică credențialele în baza de date și, în caz de succes, stochează obiectul utilizator într-o sesiune `HttpSession`.
* **Managementul Sesiunii:** Sesiunea HTTP este utilizată pentru a proteja rutele de lobby și joc. La conectarea prin WebSocket, `clientId` este extras din query parameters și validat pentru a asocia corect conexiunea socket cu identitatea utilizatorului din baza de date.
* **Înregistrare:** Implementată prin pattern-ul `Builder`, asigurând validarea unicității numelui de utilizator înainte de persistență.
---

## 📂 Structura Pachetelor

```text
com.example.proiectis
├── controller   # Handlere pentru rutele Web și Autentificare
├── dto          # Data Transfer Objects pentru comunicarea între straturi
├── entity       # Entități JPA (Match, Player, Ranking)
├── game         # Logica de table și managerii de sesiune (GameManager, LobbyManager)
├── repository   # Interfețe pentru persistența datelor
├── service      # Servicii de business (MatchService, PlayerService)
└── websocket    # Managementul conexiunilor și broadcast-ul mesajelor
```

## ⚙️ Instalare și Rulare

### Precerințe
* **Java 17** sau o versiune mai nouă
* **Maven 3.6** sau o versiune mai nouă
* Un server **MySQL** instalat și activ

### Pași pentru rulare locală

1. **Clonați repository-ul:**
```bash
git clone [https://github.com/username/joc-table-online.git](https://github.com/username/joc-table-online.git)
cd joc-table-online
```
2. **Configurați baza de date: Creați o bază de date (ex. arena_table) și actualizați fișierul** ```src/main/resources/application.properties```

3. **Compilați și rulați proiectul:**
```
mvn clean install
mvn spring-boot:run
```

4. **Accesare aplicație: Deschideți browser-ul la adresa**: http://localhost:8080


## 👥 Echipa

**Cîrneală Darius — Arhitectură WebSocket & Game Engine**

**Botărel Patrik — Database Design & Persistență (JPA)**

**Mărginean Alexandru — Autentificare, Session Management & UI**

<a href="https://github.com/darusc/proiect-is/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=darusc/proiect-is" />
</a>