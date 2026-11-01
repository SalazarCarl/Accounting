import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

// LedgerPage.java: per-account T-Account Ledger matching the reference image.
public class LedgerPage extends JPanel {

    private static final Color CORNSILK = new Color(254, 250, 224);
    private static final Color DARK_OLIVE = new Color(96, 108, 56);
    private static final Color WARM_IVORY = new Color(255, 239, 205);

    private String currentProjectTitle;
    private JComboBox<String> accountSelector;
    private JPanel tAccountsContainer;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public LedgerPage(Runnable onBackClick) {
        setLayout(new BorderLayout());
        this.setBackground(CORNSILK);

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(DARK_OLIVE);
        header.setBorder(new EmptyBorder(12, 26, 12, 26));

        JLabel heading = new JLabel("General Ledger (T-Accounts)");
        heading.setFont(new Font("SansSerif", Font.BOLD, 21));
        heading.setForeground(CORNSILK);

        JButton backButton = createLightButton("←  Back to Dashboard");
        backButton.addActionListener(e -> onBackClick.run());

        header.add(heading, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        // Main area with account selector toolbar
        JPanel body = new JPanel(new BorderLayout(0, 15));
        body.setBackground(CORNSILK);
        body.setBorder(new EmptyBorder(25, 40, 25, 40));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        toolbar.setOpaque(false);
        JLabel selectLabel = new JLabel("Select Account:");
        selectLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        selectLabel.setForeground(DARK_OLIVE);

        accountSelector = new JComboBox<>();
        accountSelector.setFont(new Font("SansSerif", Font.PLAIN, 14));
        accountSelector.setPreferredSize(new Dimension(220, 32));
        accountSelector.addActionListener(e -> loadSelectedAccountLedger());

        toolbar.add(selectLabel);
        toolbar.add(accountSelector);
        body.add(toolbar, BorderLayout.NORTH);

        // Container for T-account cards
        tAccountsContainer = new JPanel(new BorderLayout());
        tAccountsContainer.setOpaque(false);
        body.add(tAccountsContainer, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public void loadLatestProject() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT j.title FROM journal_entries j JOIN users u ON j.user_id = u.user_id "
                     + "WHERE u.username = ? ORDER BY j.entry_id DESC LIMIT 1")) {
            ps.setString(1, LoginApp.currentUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    loadProject(rs.getString("title"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not load latest project: " + e.getMessage());
        }
    }

    public void loadProject(String title) {
        this.currentProjectTitle = title;
        refreshAccountList();
    }

    private void refreshAccountList() {
        accountSelector.removeActionListener(accountSelector.getActionListeners()[0]);
        accountSelector.removeAllItems();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT debit_account FROM journal_entries j JOIN users u ON j.user_id = u.user_id "
                     + "WHERE j.title = ? AND u.username = ? "
                     + "UNION "
                     + "SELECT DISTINCT credit_account FROM journal_entries j JOIN users u ON j.user_id = u.user_id "
                     + "WHERE j.title = ? AND u.username = ? "
                     + "ORDER BY 1")) {
            if (conn == null) return;
            ps.setString(1, currentProjectTitle);
            ps.setString(2, LoginApp.currentUser);
            ps.setString(3, currentProjectTitle);
            ps.setString(4, LoginApp.currentUser);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String acc = rs.getString(1);
                    if (acc != null && !acc.isEmpty()) {
                        accountSelector.addItem(acc);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not load account list: " + e.getMessage());
        }

        accountSelector.addActionListener(e -> loadSelectedAccountLedger());
        if (accountSelector.getItemCount() > 0) {
            accountSelector.setSelectedIndex(0);
        } else {
            tAccountsContainer.removeAll();
            tAccountsContainer.revalidate();
            tAccountsContainer.repaint();
        }
    }

    private void loadSelectedAccountLedger() {
        String accountName = (String) accountSelector.getSelectedItem();
        if (accountName == null) return;

        tAccountsContainer.removeAll();

        RoundedPanel sheetPanel = new RoundedPanel(22, WARM_IVORY);
        sheetPanel.setLayout(new BorderLayout());
        sheetPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Upper-left corner account title
        JPanel titleCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleCard.setOpaque(false);
        titleCard.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel accountTitle = new JLabel(accountName);
        accountTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        accountTitle.setForeground(DARK_OLIVE);
        titleCard.add(accountTitle);
        sheetPanel.add(titleCard, BorderLayout.NORTH);

        // T-Account table: Date | Explanation | Debit | Date | Explanation | Credit
        String[] columns = {"Date", "Explanation", "Debit", "Date", "Explanation", "Credit"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.setGridColor(new Color(205, 200, 180));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setOpaque(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);

        // Fetch entries where this account is either debit or credit
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT entry_date, debit_account, credit_account, description, amount "
                     + "FROM journal_entries j JOIN users u ON j.user_id = u.user_id "
                     + "WHERE j.title = ? AND u.username = ? AND (debit_account = ? OR credit_account = ?) "
                     + "ORDER BY entry_date ASC, entry_id ASC")) {
            if (conn != null) {
                ps.setString(1, currentProjectTitle);
                ps.setString(2, LoginApp.currentUser);
                ps.setString(3, accountName);
                ps.setString(4, accountName);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        java.sql.Date sqlDate = rs.getDate("entry_date");
                        String formattedDate = sqlDate != null ? sqlDate.toLocalDate().format(DISPLAY_FORMAT) : "";
                        String debitAcc = rs.getString("debit_account");
                        String creditAcc = rs.getString("credit_account");
                        String desc = rs.getString("description");
                        if (desc == null || desc.isEmpty()) desc = "Journal Entry";
                        double amount = rs.getDouble("amount");
                        String amtStr = String.format("%.2f", amount);

                        if (accountName.equalsIgnoreCase(debitAcc)) {
                            // Debit entry: put on left side, description as explanation
                            model.addRow(new Object[]{formattedDate, desc, amtStr, "", "", ""});
                        } else if (accountName.equalsIgnoreCase(creditAcc)) {
                            // Credit entry: put on right side, description as explanation
                            model.addRow(new Object[]{"", "", "", formattedDate, desc, amtStr});
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not load account ledger: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setOpaque(true);
        sheetPanel.add(scrollPane, BorderLayout.CENTER);

        tAccountsContainer.add(sheetPanel, BorderLayout.CENTER);
        tAccountsContainer.revalidate();
        tAccountsContainer.repaint();
    }

    private JButton createLightButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(DARK_OLIVE);
        b.setBackground(CORNSILK);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(9, 12, 9, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        RoundedPanel(int r, Color c) { this.radius = r; this.bg = c; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(60, 60, 30, 18));
            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 6, radius, radius);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 7, radius, radius);
            g2.dispose();
        }
    }
}