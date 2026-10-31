import javax.swing.*;
import java.awt.*;

// ForgotPasswordScreen.java: change the password
// NOTE: demo only ni. A real app sends a code to the email first. Here, only the email
// is checked, so anyone who knows the email can change the password.
public class ForgotPasswordScreen extends Theme.Screen {

    final Theme.PillField username = new Theme.PillField("Enter your username");
    final Theme.PillPassword newPass = new Theme.PillPassword("Enter a new password");
    final Theme.PillPassword confirm = new Theme.PillPassword("Type the new password again");
    final JLabel msg = Theme.msgLabel();
    final Theme.PillButton resetBtn = new Theme.PillButton("Reset password");

    public ForgotPasswordScreen() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        Theme.addRow(form, Theme.label("Reset password", Theme.serif(Font.PLAIN, 30), Theme.OLIVE_DEEP, SwingConstants.CENTER), 0);
        Theme.addRow(form, Theme.label("Enter your username and a new password.", Theme.sans(Font.PLAIN, 14), Theme.OLIVE, SwingConstants.CENTER), 6);
        Theme.addRow(form, Theme.fieldLabel("Username"), 22);
        Theme.addRow(form, username, 6);
        Theme.addRow(form, Theme.fieldLabel("New password"), 14);
        Theme.addRow(form, newPass, 6);
        Theme.addRow(form, Theme.fieldLabel("Confirm new password"), 14);
        Theme.addRow(form, confirm, 6);
        Theme.addRow(form, msg, 8);
        Theme.addRow(form, resetBtn, 4);
        Theme.addRow(form, Theme.footer("Remembered it?", "Back to login", () -> LoginApp.show("login")), 16);

        add(Theme.authLayout(form));

        resetBtn.addActionListener(e -> resetPassword());
        confirm.addActionListener(e -> resetPassword());   // Enter key
    }

    @Override void onShow() {
        username.setText("");
        newPass.setText("");
        confirm.setText("");
        Theme.say(msg, "", false);
    }

    private void fail(String text) { Theme.say(msg, text, false); }

    private void resetPassword() {
        String u = username.getText().trim();
        String p = new String(newPass.getPassword());
        String c = new String(confirm.getPassword());

        if (u.isEmpty() || p.isEmpty() || c.isEmpty()) { fail("Please fill in all the fields."); return; }
        if (!UserStore.isValidUsername(u)) { fail("Username: 3-20 letters, numbers or _ only."); return; }
        if (!UserStore.usernameExists(u)) { fail("No account found with that username."); return; }
        if (p.length() < 4)                { fail("Password must be at least 4 characters."); return; }
        if (p.contains("|"))               { fail("Password cannot contain the | symbol."); return; }
        if (!p.equals(c))                  { fail("Passwords do not match."); return; }

        if (!UserStore.updatePassword(u, p)) { fail("Could not update the password."); return; }

        LoginApp.show("login");
        LoginApp.login.say("Password updated! Please log in.", true);
    }
}
