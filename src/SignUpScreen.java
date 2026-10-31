import javax.swing.*;
import java.awt.*;

// SignUpScreen.java: create a new account
public class SignUpScreen extends Theme.Screen {

    final Theme.PillField user = new Theme.PillField("Choose a username");
    final Theme.PillPassword pass = new Theme.PillPassword("Create a password");
    final Theme.PillPassword confirm = new Theme.PillPassword("Type the password again");
    final JLabel msg = Theme.msgLabel();
    final Theme.PillButton createBtn = new Theme.PillButton("Create account");

    public SignUpScreen() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        Theme.addRow(form, Theme.label("Create account", Theme.serif(Font.PLAIN, 30), Theme.OLIVE_DEEP, SwingConstants.CENTER), 0);
        Theme.addRow(form, Theme.label("Fill in the details to sign up.", Theme.sans(Font.PLAIN, 14), Theme.OLIVE, SwingConstants.CENTER), 6);
        Theme.addRow(form, Theme.fieldLabel("Username"), 14);
        Theme.addRow(form, user, 4);
        Theme.addRow(form, Theme.fieldLabel("Password"), 10);
        Theme.addRow(form, pass, 4);
        Theme.addRow(form, Theme.fieldLabel("Confirm password"), 10);
        Theme.addRow(form, confirm, 4);
        Theme.addRow(form, msg, 6);
        Theme.addRow(form, createBtn, 4);
        Theme.addRow(form, Theme.footer("Already have an account?", "Log in", () -> LoginApp.show("login")), 12);

        add(Theme.authLayout(form));

        createBtn.addActionListener(e -> createAccount());
        confirm.addActionListener(e -> createAccount());   // Enter key
    }

    @Override void onShow() {
        user.setText("");
        pass.setText("");
        confirm.setText("");
        Theme.say(msg, "", false);
    }

    private void fail(String text) { Theme.say(msg, text, false); }

    // ===== check everything first (from the easiest check to the hardest) =====
    private void createAccount() {
        String u = user.getText().trim();
        String p = new String(pass.getPassword());
        String c = new String(confirm.getPassword());

        if (u.isEmpty() || p.isEmpty() || c.isEmpty()) { fail("Please fill in all the fields."); return; }
        if (!UserStore.isValidUsername(u))  { fail("Username: 3-20 letters, numbers or _ only."); return; }
        if (p.length() < 4)                 { fail("Password must be at least 4 characters."); return; }
        if (p.contains("|"))                { fail("Password cannot contain the | symbol."); return; }
        if (!p.equals(c))                   { fail("Passwords do not match."); return; }
        if (UserStore.usernameExists(u))    { fail("That username is already taken."); return; }

        if (!UserStore.addUser(u, p))   { fail("Could not save the account. Try again."); return; }

        // success: go back to login with a message and the username already filled in
        LoginApp.show("login");
        LoginApp.login.user.setText(u);
        LoginApp.login.say("Account created! You can log in now.", true);
    }
}
