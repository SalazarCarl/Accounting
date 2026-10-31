import javax.swing.*;
import java.awt.*;

// DashboardScreen.java: SLOT FOR THE DASHBOARD.
// After a successful login, this launches the AccuTrack Accounting app.
public class DashboardScreen extends Theme.Screen {

    private boolean launched = false;

    public DashboardScreen() {
        setLayout(new BorderLayout());
        add(buildContent(""));
    }

    // called after a successful login. We rebuild the content so it knows the username
    void setUser(String username) {
        LoginApp.currentUser = username;
        if (!launched) {
            launched = true;
            // Launch the Accounting app on the Event Dispatch Thread, then close login window
            final String name = username;
            SwingUtilities.invokeLater(() -> {
                try {
                    new homepage(name); // opens AccuTrack dashboard
                } catch (Exception e) {
                    e.printStackTrace();
                    // Fallback: show error in login window
                    SwingUtilities.invokeLater(() -> {
                        JFrame loginFrame = (JFrame) SwingUtilities.windowForComponent(DashboardScreen.this);
                        if (loginFrame != null) loginFrame.dispose();
                        JOptionPane.showMessageDialog(null,
                            "Could not launch AccuTrack: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    });
                    return;
                }
                JFrame loginFrame = (JFrame) SwingUtilities.windowForComponent(DashboardScreen.this);
                if (loginFrame != null) loginFrame.dispose();
            });
        }
        // Keep a minimal placeholder so the login window doesn't look broken
        removeAll();
        add(buildContent(username));
        revalidate();
        repaint();
    }

    private JComponent buildContent(String username) {
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);
        String hello = username.isEmpty() ? "Welcome!" : "Welcome, " + username + "!";
        Theme.addRow(col, Theme.label("Dashboard", Theme.serif(Font.PLAIN, 34), Theme.OLIVE_DEEP, SwingConstants.CENTER), 0);
        Theme.addRow(col, Theme.label(hello, Theme.serif(Font.PLAIN, 24), Theme.OLIVE, SwingConstants.CENTER), 12);
        Theme.addRow(col, Theme.label("Launching AccuTrack...", Theme.sans(Font.PLAIN, 14), Theme.OLIVE, SwingConstants.CENTER), 8);

        Theme.PillButton logout = new Theme.PillButton("Log out");
        logout.addActionListener(e -> LoginApp.show("login"));   // back to login
        Theme.addRow(col, logout, 30);

        return Theme.plainCard(col);
    }
}
