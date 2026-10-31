import javax.swing.*;
import java.awt.*;

// LoginScreen.java: the login screen
public class LoginScreen extends Theme.Screen {

    final Theme.PillField user = new Theme.PillField("Enter your username");
    final Theme.PillPassword pass = new Theme.PillPassword("Enter your password");
    final JCheckBox remember = new JCheckBox("Remember me");
    final JLabel msg = Theme.msgLabel();
    final Theme.PillButton loginBtn = new Theme.PillButton("Log In");

    public LoginScreen() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        Theme.addRow(form, Theme.label("Welcome back", Theme.serif(Font.PLAIN, 30), Theme.OLIVE_DEEP, SwingConstants.CENTER), 0);
        Theme.addRow(form, Theme.label("Please enter your credentials to log in.", Theme.sans(Font.PLAIN, 14), Theme.OLIVE, SwingConstants.CENTER), 8);
        Theme.addRow(form, Theme.fieldLabel("Username\nUsername only"), 26);
        Theme.addRow(form, user, 6);
        Theme.addRow(form, Theme.fieldLabel("Password"), 16);
        Theme.addRow(form, pass, 6);

        // "Remember me" on the left, "Forgot password?" on the right
        remember.setOpaque(false);
        remember.setFocusPainted(false);
        remember.setFont(Theme.sans(Font.PLAIN, 13));
        remember.setForeground(Theme.OLIVE_DEEP);
        JLabel forgot = Theme.link("Forgot password?", SwingConstants.RIGHT, () -> LoginApp.show("forgot"));
        forgot.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        JPanel options = new JPanel(new BorderLayout());
        options.setOpaque(false);
        options.add(remember, BorderLayout.WEST);
        options.add(forgot, BorderLayout.EAST);
        Theme.addRow(form, options, 10);

        Theme.addRow(form, msg, 6);
        Theme.addRow(form, loginBtn, 6);
        Theme.addRow(form, Theme.footer("Don\u2019t have an account?", "Sign up", () -> LoginApp.show("signup")), 18);

        add(Theme.authLayout(form));

        loginBtn.addActionListener(e -> doLogin());
        user.addActionListener(e -> doLogin());   // Enter key
        pass.addActionListener(e -> doLogin());   // Enter key
    }

    // clear the fields every time we go back to login
    @Override void onShow() {
        user.setText("");
        pass.setText("");
        remember.setSelected(false);
        Theme.say(msg, "", false);
    }

    // ===== the login: check all the errors first, then check the account =====
    private void doLogin() {
        String id = user.getText().trim();
        String p = new String(pass.getPassword());

        if (id.isEmpty()) { Theme.say(msg, "Please enter your username.", false); return; }
        if (p.isEmpty())  { Theme.say(msg, "Please enter your password.", false); return; }
        if (id.contains("@")) {
            Theme.say(msg, "Email login is not supported. Use username.", false);
            return;
        }

        String name = UserStore.checkLogin(id, p);
        if (name == null) {
            // don't say kung username or password ang mali (for security)
            Theme.say(msg, "Wrong username or password.", false);
            return;
        }
        LoginApp.dashboard.setUser(name);
        LoginApp.show("dashboard");
    }

    // used by other screens to show a message here (example: "Account created!")
    void say(String text, boolean good) { Theme.say(msg, text, good); }
}
