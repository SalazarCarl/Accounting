import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// LedgerPage.java: all accounts have their own T-Account Ledger cards in a scrollable list.
public class LedgerPage extends JPanel {

    private static final Color CORNSILK = new Color(254, 250, 224);
    private static final Color DARK_OLIVE = new Color(96, 108, 56);
    private static final Color WARM_IVORY = new Color(255, 239, 205);

    private String currentProjectTitle;
    private JPanel tAccountsContainer;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public LedgerPage(Runnable onBackClick, Runnable onBackToJournal) {
        setLayout(new BorderLayout());
        this.setBackground(CORNSILK);

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(DARK_OLIVE);
        header.setBorder(new EmptyBorder(12, 26, 12, 26));

        JButton backButton = createLightButton("🏠");
        backButton.setPreferredSize(new Dimension(50, 39));
        backButton.addActionListener(e -> onBackClick.run());

        JButton journalButton = new JButton("← Journal Entry");
        journalButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        journalButton.setBackground(CORNSILK);
        journalButton.setForeground(DARK_OLIVE);
        journalButton.setFocusPainted(false);
        journalButton.setBorderPainted(false);
        journalButton.setOpaque(true);
        journalButton.setBorder(new EmptyBorder(9, 12, 9, 12));
        journalButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        journalButton.addActionListener(e -> onBackToJournal.run());

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerLeft.setOpaque(false);
        headerLeft.add(backButton);
        headerLeft.add(journalButton);
        header.add(headerLeft, BorderLayout.WEST);

        JLabel heading = new JLabel("General Ledger (T-Accounts)");
        heading.setFont(new Font("SansSerif", Font.BOLD, 21));
        heading.setForeground(CORNSILK);
        header.add(heading, BorderLayout.EAST);

        // Main scrollable area for all T-Account cards
        tAccountsContainer = new JPanel();
        tAccountsContainer.setLayout(new BoxLayout(tAccountsContainer, BoxLayout.Y_AXIS));
        tAccountsContainer.setBackground(CORNSILK);
        tAccountsContainer.setBorder(new EmptyBorder(25, 40, 25, 40));

        JScrollPane scrollPane = new JScrollPane(tAccountsContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CORNSILK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
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
        refreshLedgers();
    }

    private void refreshLedgers() {
        tAccountsContainer.removeAll();

        // 1. Gather all unique accounts for this project & user
        List<String> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT debit_account FROM journal_entries j JOIN users u ON j.user_id = u.user_id "
                     + "WHERE j.title = ? AND u.username = ? "
                     + "UNION "
                     + "SELECT DISTINCT credit_account FROM journal_entries j JOIN users u ON j.user_id = u.user_id "
                     + "WHERE j.title = ? AND u.username = ? "
                     + "ORDER BY 1")) {
            ps.setString(1, currentProjectTitle);
            ps.setString(2, LoginApp.currentUser);
            ps.setString(3, currentProjectTitle);
            ps.setString(4, LoginApp.currentUser);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String acc = rs.getString(1);
                    if (acc != null && !acc.trim().isEmpty()) {
                        accounts.add(acc.trim());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not load account list: " + e.getMessage());
        }

        if (accounts.isEmpty()) {
            JLabel emptyLbl = new JLabel("No journal entries found for this project.");
            emptyLbl.setFont(new Font("SansSerif", Font.PLAIN, 15));
            emptyLbl.setForeground(DARK_OLIVE);
            emptyLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            tAccountsContainer.add(emptyLbl);
        } else {
            for (String accountName : accounts) {
                JPanel card = createAccountLedgerCard(accountName);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                tAccountsContainer.add(card);
                tAccountsContainer.add(Box.createVerticalStrut(25));
            }
        }

        tAccountsContainer.revalidate();
        tAccountsContainer.repaint();
    }

    private JPanel createAccountLedgerCard(String accountName) {
        RoundedPanel sheetPanel = new RoundedPanel(22, WARM_IVORY);
        sheetPanel.setLayout(new BorderLayout());
        sheetPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Upper-left corner account title + table header share one NORTH panel
        // (BorderLayout only shows the last component added to a region, so the
        //  title and header must be grouped together or the header hides the name)
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);

        JPanel titleCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleCard.setOpaque(false);
        titleCard.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel accountTitle = new JLabel(accountName);
        accountTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        accountTitle.setForeground(DARK_OLIVE);
        titleCard.add(accountTitle);
        northPanel.add(titleCard, BorderLayout.NORTH);

        // T-Account table: Date | Explanation | Debit | Date | Explanation | Credit
        String[] columns = {"Date", "Explanation", "Debit", "Date", "Explanation", "Credit"};

        // Collect debit rows and credit rows separately so we can pack them together without blank rows
        List<Object[]> debitRows = new ArrayList<>();
        List<Object[]> creditRows = new ArrayList<>();
        double totalDebit = 0.0;
        double totalCredit = 0.0;

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
                            debitRows.add(new Object[]{formattedDate, desc, amtStr});
                            totalDebit += amount;
                        }
                        if (accountName.equalsIgnoreCase(creditAcc)) {
                            creditRows.add(new Object[]{formattedDate, desc, amtStr});
                            totalCredit += amount;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not load account ledger for " + accountName + ": " + e.getMessage());
        }

        int maxRows = Math.max(debitRows.size(), creditRows.size());
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (int i = 0; i < maxRows; i++) {
            Object dDate = "";
            Object dDesc = "";
            Object dAmt = "";
            if (i < debitRows.size()) {
                dDate = debitRows.get(i)[0];
                dDesc = debitRows.get(i)[1];
                dAmt = debitRows.get(i)[2];
            }

            Object cDate = "";
            Object cDesc = "";
            Object cAmt = "";
            if (i < creditRows.size()) {
                cDate = creditRows.get(i)[0];
                cDesc = creditRows.get(i)[1];
                cAmt = creditRows.get(i)[2];
            }

            model.addRow(new Object[]{dDate, dDesc, dAmt, cDate, cDesc, cAmt});
        }

        // Add TOTAL row
        String totalDebitStr = String.format("%.2f", totalDebit);
        String totalCreditStr = String.format("%.2f", totalCredit);
        model.addRow(new Object[]{"", "TOTAL", totalDebitStr, "", "TOTAL", totalCreditStr});

        // Add BALANCE row if totals are not equal
        double diff = Math.abs(totalDebit - totalCredit);
        if (diff > 0.001) {
            String diffStr = String.format("%.2f", diff);
            if (totalDebit > totalCredit) {
                model.addRow(new Object[]{"", "BALANCE", diffStr, "", "", ""});
            } else {
                model.addRow(new Object[]{"", "", "", "", "BALANCE", diffStr});
            }
        }

        int totalDisplayedRows = maxRows + 1 + (diff > 0.001 ? 1 : 0);

        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.setGridColor(new Color(205, 200, 180));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(DARK_OLIVE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setOpaque(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(85);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(75);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(170);
        table.getColumnModel().getColumn(5).setPreferredWidth(75);

        // Add table with header to the card
        northPanel.add(table.getTableHeader(), BorderLayout.CENTER);
        sheetPanel.add(northPanel, BorderLayout.NORTH);
        sheetPanel.add(table, BorderLayout.CENTER);

        // Set preferred height so all rows and header are fully visible without inner scrollbars
        // northPanel holds the account title (~30px) + the table header (~26px)
        int totalRows = totalDisplayedRows + 1; // +1 for header
        sheetPanel.setPreferredSize(new Dimension(0, 56 + (totalRows * 24)));

        return sheetPanel;
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
