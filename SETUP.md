# AccuTrack — Setup Guide

This app stores its data in a **local MySQL database**. The connection is hardcoded in `src/DatabaseConnection.java`:

```
jdbc:mysql://localhost:3306/accutrack_db
user: root
pass: <see DatabaseConnection.java>
```

The MySQL password is stored in the source file `src/DatabaseConnection.java` (constant `PASSWORD`). Before the app will run, the machine needs **MySQL installed and running locally**, the `accutrack_db` database created, and the schema loaded.

---

## 1. Install MySQL (if not already)

- **Windows:** Download MySQL Installer from https://dev.mysql.com/downloads/mysql/ and install the "Server" component. It runs as a Windows service named `MySQL80` (or similar). Make sure it is **started**.
- **Mac:** `brew install mysql` then `brew services start mysql`.
- **Linux:** `sudo apt install mysql-server` then `sudo service mysql start`.

Verify it is running:

```bash
mysqladmin -u root -p status
```

## 2. Create the database and load the schema

The repo includes `schema.sql`. Run it, using the password from `src/DatabaseConnection.java`:

```bash
mysql -u root -p < schema.sql
```

This creates the `accutrack_db` database and the `users`, `accounts`, `journal_entries`, and `worksheet_data` tables.

## 3. Install Java (if not already)

Requires **Java 17+**. Check:

```bash
java -version
javac -version
```

If missing, install from https://adoptium.net/ (OpenJDK).

## 4. Compile

The MySQL JDBC driver is already in `lib/mysql-connector-j-26.10.0.jar`.

```bash
javac -cp "lib/*" -d build src/*.java
```

## 5. Run

```bash
java -cp "build:lib/*" homepage
```

The login window will open. The default admin account is `Administrator`.

---

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| `Connection failed: Access denied for user 'root'@'localhost'` | MySQL password does not match the `PASSWORD` constant in `src/DatabaseConnection.java` — reset it in MySQL, then update that constant and recompile |
| `Connection failed: Unknown database 'accutrack_db'` | Schema not loaded — run step 2 |
| `Connection failed: Communications link failure` / `Cannot connect to MySQL server` | MySQL service is not running — start it (step 1) |
| `ClassNotFound: com.mysql.cj.jdbc.Driver` | JDBC jar missing or not on classpath — make sure `lib/mysql-connector-j-*.jar` is present and the `-cp` includes `lib/*` |
| `java -version` works but `javac` does not | Java JDK (not just JRE) is not on PATH — install the JDK, not the JRE |

## Notes

- All data is **user-scoped**: journal entries are tied to `users.user_id`, so each user only sees their own projects.
- If you change the MySQL password, remember to edit `src/DatabaseConnection.java` and recompile.