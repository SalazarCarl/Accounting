import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

// LoginApp.java: the main entry point for the AccuTrack application.
// It holds the window and the "navigation" between screens.
// Uses CardLayout: parang stack of cards, only the top one is visible.
public class LoginApp {

    private static final CardLayout cards = new CardLayout();
    private static final JPanel root = new JPanel(cards);
    private static final Map<String, Theme.Screen> screens = new HashMap<>();

    // screens that other files need
    static LoginScreen login;
    static DashboardScreen dashboard;
    static String currentUser = "";

    // show a screen by its name: "login", "signup", "forgot", "dashboard"
    static void show(String name) {
        Theme.Screen s = screens.get(name);
        if (s != null) s.onShow();      // clear the fields first
        cards.show(root, name);
    }

    private static void register(String name, Theme.Screen screen) {
        screens.put(name, screen);
        root.add(screen, name);
    }

    // create all the screens and put them in the stack
    static JPanel buildRoot() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception e) { }

        login = new LoginScreen();
        dashboard = new DashboardScreen();
        register("login", login);
        register("signup", new SignUpScreen());
        register("forgot", new ForgotPasswordScreen());
        // SLOT FOR DASHBOARD: the dashboard content is plugged in DashboardScreen.java
        register("dashboard", dashboard);
        cards.show(root, "login");      // login is the first screen shown
        return root;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("AccuTrack");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(buildRoot());
            f.setMinimumSize(new Dimension(820, 600));
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
