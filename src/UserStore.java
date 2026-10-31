// UserStore.java: the "librarian". The screens only ask this class (naay ani nga user?
// husto ba ang password?). It passes the question to the real storage.
//
// Connected to the MySQL database (DatabaseUserStorage).
// To fall back to file storage, change the line below to: new FileUserStorage();
public class UserStore {

    // ===== STORAGE SWITCH =====
    // Database is active. Change to new FileUserStorage() for file-based fallback.
    static UserStorage storage = new DatabaseUserStorage();

    // ===== validation (checking the format) =====

    // username: 3 to 20 letters, numbers, or underscore lang
    static boolean isValidUsername(String u) {
        return u.matches("^[A-Za-z0-9_]{3,20}$");
    }

    // ===== questions from the screens (passed to the storage) =====
    static boolean usernameExists(String username) { return storage.usernameExists(username); }
    static boolean addUser(String u, String p) { return storage.addUser(u, p); }
    static String checkLogin(String username, String password) { return storage.checkLogin(username, password); }
    static boolean updatePassword(String username, String newPassword) { return storage.updatePassword(username, newPassword); }
}
