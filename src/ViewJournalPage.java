import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class ViewJournalPage extends JPanel {

    private static final Color CORNSILK = new Color(254, 250, 224);
    private static final Color DARK_OLIVE = new Color(96, 108, 56);
    private static final Color WARM_IVORY = new Color(255, 239, 205);

    private String currentProjectTitle;
    private JTable table;
    private DefaultTableModel model;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd yyyy");

    public ViewJournalPage(Runnable onBackClick, Runnable onBackToJournal) {
        setLayout(new BorderLayout());
        setBackground(CORNSILK);

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

        JLabel heading = new JLabel("General Journal Sheet");
        heading.setFont(new Font("SansSerif", Font.BOLD, 21));
        heading.setForeground(CORNSILK);
        header.add(heading, BorderLayout.EAST);

        // Main Area
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(CORNSILK);
        body.setBorder(new EmptyBorder(30, 40, 30, 40));

        RoundedPanel sheetPanel = new RoundedPanel(22, WARM_IVORY);
        sheetPanel.setLayout(new BorderLayout());
        sheetPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] columns = {"DATE", "PARTICULARS", "DR", "CR"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(DARK_OLIVE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setOpaque(true);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setOpaque(true);
        sheetPanel.add(scrollPane, BorderLayout.CENTER);

        body.add(sheetPanel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public void loadLatestProject() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT j.title FROM journal_entries j JOIN users u ON j.user_id = u.user_id WHERE u.username = ? ORDER BY j.entry_id DESC LIMIT 1")) {
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
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT entry_date, debit_account, credit_account, description, amount FROM journal_entries j JOIN users u ON j.user_id = u.user_id WHERE j.title = ? AND u.username = ? ORDER BY entry_date ASC, entry_id ASC")) {
            if (conn == null) return;
            ps.setString(1, title);
            ps.setString(2, LoginApp.currentUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("entry_date");
                    String formattedDate = sqlDate != null ? sqlDate.toLocalDate().format(DISPLAY_FORMAT) : "";
                    String debitAcc = rs.getString("debit_account");
                    String creditAcc = rs.getString("credit_account");
                    String description = rs.getString("description");
                    if (description == null || description.isEmpty()) description = "Journal Entry";
                    if (!description.startsWith("#")) description = "#" + description;
                    String amtStr = String.format("%.2f", rs.getDouble("amount"));

                    // Row 1: Debit Account + DR Amount
                    model.addRow(new Object[]{formattedDate, debitAcc, amtStr, ""});
                    // Row 2: Credit Account (indented) + CR Amount
                    model.addRow(new Object[]{"", "    " + creditAcc, "", amtStr});
                    // Row 3: Description
                    model.addRow(new Object[]{"", description, "", ""});
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not load journal sheet: " + e.getMessage());
        }
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