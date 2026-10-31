// UserStorage.java: the "contract" for saving accounts.
// Any storage (text file, database) must have these methods.
// The screens never care kung asa gi-save, basta naa these methods.
public interface UserStorage {

    // naa na ba ni nga username? (ignore uppercase/lowercase)
    boolean usernameExists(String username);

    // add a new account. Return true kung na-save, false kung naay error
    boolean addUser(String username, String password);

    // login: check username and password.
    // Return the username kung husto, return null kung mali
    String checkLogin(String username, String password);

    // change the password of the account with that username. Return true kung successful
    boolean updatePassword(String username, String newPassword);
}
