import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

// FileUserStorage.java: saves accounts in users.txt (current storage, for the demo).
// Format: one account per line -> username|password
// NOTE: plain text ang password dinhi. Demo only, not for a real app.
public class FileUserStorage implements UserStorage {

    private static final Path FILE = Paths.get("users.txt");   // created automatically

    // read all accounts. Walay file = walay accounts pa, dili mo-crash
    private List<String[]> readAll() {
        List<String[]> users = new ArrayList<>();
        if (!Files.exists(FILE)) return users;
        try {
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                String[] p = line.split("\\|", -1);   // split by the | symbol
                if (p.length == 2) users.add(p);      // skip broken lines
            }
        } catch (IOException e) {
            System.out.println("Can't read users.txt: " + e.getMessage());
        }
        return users;
    }

    // save all accounts back to the file. Return true kung successful
    private boolean writeAll(List<String[]> users) {
        List<String> lines = new ArrayList<>();
        for (String[] u : users) lines.add(u[0] + "|" + u[1]);
        try {
            Files.write(FILE, lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            System.out.println("Can't save users.txt: " + e.getMessage());
            return false;
        }
    }

    @Override public boolean usernameExists(String username) {
        for (String[] u : readAll()) if (u[0].equalsIgnoreCase(username)) return true;
        return false;
    }

    @Override public boolean addUser(String username, String password) {
        List<String[]> users = readAll();
        users.add(new String[]{username, password});
        return writeAll(users);
    }

    @Override public String checkLogin(String username, String password) {
        for (String[] u : readAll()) {
            if (u[0].equalsIgnoreCase(username) && u[1].equals(password)) return u[0];
        }
        return null;
    }

    @Override public boolean updatePassword(String username, String newPassword) {
        List<String[]> users = readAll();
        boolean found = false;
        for (String[] u : users) {
            if (u[0].equalsIgnoreCase(username)) { u[1] = newPassword; found = true; }
        }
        return found && writeAll(users);
    }
}
