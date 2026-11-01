import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class homepage extends JFrame {

    private static final Color CORNSILK = new Color(254, 250, 224);
    private static final Color DARK_OLIVE = new Color(96, 108, 56);
    private static final Color WARM_IVORY = new Color(255, 239, 205);
    private static final Color HOVER_OLIVE = new Color(115, 130, 72);
    private static final Color TEXT_COLOR = new Color(55, 62, 37);
    private static final Color MUTED_TEXT = new Color(115, 116, 94);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JPanel sidebar = new JPanel();
    private boolean sidebarVisible = false;
    private JPanel projectGrid;

    private String loggedUser;
    public homepage(String username) {
        this.loggedUser = username;
        setTitle("AccuTrack - Dashboard");
        setSize(1100, 700);
        setMinimumSize(new Dimension(800, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(CORNSILK);

        createSidebar(username);
        createPages();

        sidebar.setVisible(false);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void createSidebar(String username) {
        sidebar.setPreferredSize(new Dimension(240, 600));
        sidebar.setBackground(CORNSILK);
        sidebar.setBorder(new EmptyBorder(28, 18, 25, 18));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        profilePanel.setBackground(CORNSILK);
        profilePanel.setMaximumSize(new Dimension(220, 65));
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel photoLabel = new JLabel(createDefaultProfileIcon());
        JLabel userLabel = new JLabel(username);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        userLabel.setForeground(DARK_OLIVE);

        profilePanel.add(photoLabel);
        profilePanel.add(userLabel);

        JButton homeButton = createSidebarButton("⌂", "Dashboard");
        homeButton.addActionListener(e -> showPage("home"));

        sidebar.add(profilePanel);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(homeButton);
        sidebar.add(Box.createVerticalGlue());
    }

    private void createPages() {
        contentPanel.add(createHomePage(), "home");
        contentPanel.add(new JournalEntryPage(() -> {
            refreshProjectGrid();
            showPage("home");
        }, () -> {
            ViewJournalPage vjp = (ViewJournalPage) contentPanel.getComponent(2);
            vjp.loadLatestProject();
            showPage("viewjournal");
        }, () -> {
            LedgerPage lp = (LedgerPage) contentPanel.getComponent(3);
            JournalEntryPage jep = (JournalEntryPage) contentPanel.getComponent(1);
            lp.loadProject(jep.getCurrentProjectTitle());
            showPage("ledger");
        }), "journal");
        contentPanel.add(new ViewJournalPage(() -> showPage("home")), "viewjournal");
        contentPanel.add(new LedgerPage(() -> showPage("home")), "ledger");
    }

    private JPanel createHomePage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(CORNSILK);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_OLIVE);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JButton menuBtn = createMenuButton();
        menuBtn.addActionListener(e -> toggleSidebar());
        header.add(menuBtn, BorderLayout.WEST);

        JButton newBtn = createActionButton("+ New Journal Entry");
        newBtn.addActionListener(e -> showNamingDialog());

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);
        headerRight.add(newBtn);
        header.add(headerRight, BorderLayout.EAST);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(CORNSILK);
        mainArea.setBorder(new EmptyBorder(30, 40, 30, 40));

        projectGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        projectGrid.setBackground(CORNSILK);

        JScrollPane scrollPane = new JScrollPane(projectGrid);
        scrollPane.setBorder(null);
        scrollPane.setBackground(CORNSILK);
        mainArea.add(scrollPane, BorderLayout.CENTER);

        page.add(header, BorderLayout.NORTH);
        page.add(mainArea, BorderLayout.CENTER);

        refreshProjectGrid();
        return page;
    }

    private void showNamingDialog() {
        String title = JOptionPane.showInputDialog(this, "Enter Project Title:");
        if (title != null && !title.isEmpty()) {
            JournalEntryPage page = (JournalEntryPage) contentPanel.getComponent(1);
            page.startNewProject(title);
            showPage("journal");
        }
    }

    public void refreshProjectGrid() {
        projectGrid.removeAll();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT j.title, MIN(j.entry_date) as created, MAX(j.entry_date) as modified FROM journal_entries j JOIN users u ON j.user_id = u.user_id WHERE u.username = ? GROUP BY j.title ORDER BY MAX(j.entry_date) DESC")) {
            ps.setString(1, loggedUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projectGrid.add(createProjectCard(rs.getString("title"), rs.getString("created"), rs.getString("modified")));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        projectGrid.revalidate();
        projectGrid.repaint();
    }

    private JPanel createProjectCard(String title, String created, String modified) {
        RoundedPanel card = new RoundedPanel(15, WARM_IVORY);
        card.setPreferredSize(new Dimension(220, 150));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JournalEntryPage page = (JournalEntryPage) contentPanel.getComponent(1);
                page.loadExistingProject(title);
                showPage("journal");
            }
        });

        JLabel titleLbl = new JLabel("<html><b>" + title + "</b></html>");
        titleLbl.setForeground(DARK_OLIVE);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JPanel details = new JPanel(new GridLayout(2, 1));
        details.setOpaque(false);
        details.add(new JLabel("Created: " + created));
        details.add(new JLabel("Modified: " + modified));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(details, BorderLayout.CENTER);
        return card;
    }

    private JButton createSidebarButton(String symbol, String text) {
        JButton b = new JButton(symbol + "  " + text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(DARK_OLIVE);
        b.setBackground(CORNSILK);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createActionButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBackground(HOVER_OLIVE);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 20, 10, 20));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createMenuButton() {
        JButton b = new JButton("☰");
        b.setFont(new Font("SansSerif", Font.BOLD, 20));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private ImageIcon createDefaultProfileIcon() {
        BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(DARK_OLIVE);
        g2.fillOval(0, 0, 40, 40);
        g2.dispose();
        return new ImageIcon(img);
    }

    private void toggleSidebar() {
        sidebarVisible = !sidebarVisible;
        sidebar.setVisible(sidebarVisible);
        revalidate();
    }

    private void showPage(String page) {
        cardLayout.show(contentPanel, page);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new homepage("Administrator"));
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        RoundedPanel(int r, Color c) { this.radius = r; this.bg = c; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }
}
