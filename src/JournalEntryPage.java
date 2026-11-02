import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class JournalEntryPage extends JPanel {

    private static final Color CORNSILK = new Color(254, 250, 224);
    private static final Color DARK_OLIVE = new Color(96, 108, 56);
    private static final Color WARM_IVORY = new Color(255, 239, 205);
    private static final Color HOVER_OLIVE = new Color(115, 130, 72);

    private JTextField dateField;
    private JTextField debitAccountField;
    private JTextField creditAccountField;
    private JTextField amountField;
    private JTextField descField;
    private JLabel projectTitleLabel;
    private String currentProjectTitle = "Untitled Entry";

    private JTable previewTable;
    private DefaultTableModel previewModel;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd yyyy");

    public JournalEntryPage(Runnable onBackClick, Runnable onViewJournal, Runnable onViewLedger) {
        setLayout(new BorderLayout());
        setBackground(CORNSILK);

        // Header matching theme
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(DARK_OLIVE);
        header.setBorder(new EmptyBorder(12, 26, 12, 26));

        projectTitleLabel = new JLabel("Project: " + currentProjectTitle);
        projectTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 21));
        projectTitleLabel.setForeground(CORNSILK);

        JButton backButton = createLightButton("🏠");
        backButton.setPreferredSize(new Dimension(50, 39));
        backButton.addActionListener(e -> onBackClick.run());

        JButton viewButton = new JButton("View Full Journal");
        viewButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        viewButton.setBackground(HOVER_OLIVE);
        viewButton.setForeground(Color.WHITE);
        viewButton.setFocusPainted(false);
        viewButton.setBorderPainted(false);
        viewButton.setOpaque(true);
        viewButton.setBorder(new EmptyBorder(10, 15, 10, 15));
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewButton.addActionListener(e -> onViewJournal.run());

        JButton ledgerButton = new JButton("View Ledger");
        ledgerButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        ledgerButton.setBackground(HOVER_OLIVE);
        ledgerButton.setForeground(Color.WHITE);
        ledgerButton.setFocusPainted(false);
        ledgerButton.setBorderPainted(false);
        ledgerButton.setOpaque(true);
        ledgerButton.setBorder(new EmptyBorder(10, 15, 10, 15));
        ledgerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ledgerButton.addActionListener(e -> onViewLedger.run());

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerLeft.setOpaque(false);
        headerLeft.add(backButton);
        headerLeft.add(projectTitleLabel);
        header.add(headerLeft, BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);
        headerRight.add(viewButton);
        headerRight.add(ledgerButton);
        header.add(headerRight, BorderLayout.EAST);

        // Main Body Layout (Clean Form on Left / Top, Compact Guide Table on Right)
        JPanel body = new JPanel(new BorderLayout(20, 20));
        body.setBackground(CORNSILK);
        body.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Form Card (Styled with WARM_IVORY and RoundedPanel)
        RoundedPanel formCard = new RoundedPanel(22, WARM_IVORY);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(25, 30, 25, 30));
        formCard.setPreferredSize(new Dimension(420, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        dateField = new JTextField(18);
        debitAccountField = new JTextField(18);
        creditAccountField = new JTextField(18);
        amountField = new JTextField("0.00", 18);
        descField = new JTextField(18);

        dateField.setText(java.time.LocalDate.now().toString());

        addFormRow(formCard, gbc, 0, "Date (YYYY-MM-DD):", dateField);
        addFormRow(formCard, gbc, 1, "Debit Account:", debitAccountField);
        addFormRow(formCard, gbc, 2, "Credit Account:", creditAccountField);
        addFormRow(formCard, gbc, 3, "Amount:", amountField);
        addFormRow(formCard, gbc, 4, "Description (#...):", descField);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 10, 10, 10);

        JButton saveButton = createActionButton("Add Entry");
        saveButton.addActionListener(e -> saveJournalEntry());

        JButton doneButton = createLightButton("Done / Dashboard");
        doneButton.addActionListener(e -> onBackClick.run());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(saveButton);
        buttonPanel.add(doneButton);
        formCard.add(buttonPanel, gbc);

        // Compact Guide Table Panel (Shows Date, Description, Amount)
        RoundedPanel guidePanel = new RoundedPanel(22, WARM_IVORY);
        guidePanel.setLayout(new BorderLayout());
        guidePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel guideTitle = new JLabel("Project Entries Summary");
        guideTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        guideTitle.setForeground(DARK_OLIVE);
        guideTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        guidePanel.add(guideTitle, BorderLayout.NORTH);

        String[] columns = {"Date", "Description", "Amount"};
        previewModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        previewTable = new JTable(previewModel);
        previewTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        previewTable.setRowHeight(24);
        previewTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        previewTable.getTableHeader().setBackground(DARK_OLIVE);
        previewTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        guidePanel.add(scrollPane, BorderLayout.CENTER);

        body.add(formCard, BorderLayout.WEST);
        body.add(guidePanel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public void startNewProject(String title) {
        this.currentProjectTitle = title;
        projectTitleLabel.setText("Project: " + title);
        dateField.setText(java.time.LocalDate.now().toString());
        debitAccountField.setText("");
        creditAccountField.setText("");
        amountField.setText("0.00");
        descField.setText("");
        loadPreviewEntries();
    }

    public String getCurrentProjectTitle() {
        return currentProjectTitle;
    }

    public void loadExistingProject(String title) {
        this.currentProjectTitle = title;
        projectTitleLabel.setText("Project: " + title);
        loadPreviewEntries();
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(DARK_OLIVE);
        panel.add(label, gbc);

        gbc.gridx = 1;
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(field, gbc);
    }

    private void saveJournalEntry() {
        String date = dateField.getText().trim();
        String debitAcc = debitAccountField.getText().trim();
        String creditAcc = creditAccountField.getText().trim();
        String amountStr = amountField.getText().trim();
        String description = descField.getText().trim();

        if (date.isEmpty() || debitAcc.isEmpty() || creditAcc.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String query = "INSERT INTO journal_entries (title, entry_date, debit_account, credit_account, description, amount, user_id) VALUES (?, ?, ?, ?, ?, ?, (SELECT user_id FROM users WHERE username = ?))";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, currentProjectTitle);
                pstmt.setString(2, date);
                pstmt.setString(3, debitAcc);
                pstmt.setString(4, creditAcc);
                pstmt.setString(5, description);
                pstmt.setDouble(6, amount);
                pstmt.setString(7, LoginApp.currentUser);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Entry added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                debitAccountField.setText("");
                creditAccountField.setText("");
                amountField.setText("0.00");
                descField.setText("");

                loadPreviewEntries();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPreviewEntries() {
        if (previewModel == null) return;
        previewModel.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT entry_date, description, amount FROM journal_entries j JOIN users u ON j.user_id = u.user_id WHERE j.title = ? AND u.username = ? ORDER BY entry_id ASC")) {

            pstmt.setString(1, currentProjectTitle);
            pstmt.setString(2, LoginApp.currentUser);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("entry_date");
                    String formattedDate = sqlDate != null ? sqlDate.toLocalDate().format(DISPLAY_FORMAT) : "";
                    String desc = rs.getString("description");
                    if (desc == null || desc.isEmpty()) desc = "Journal Entry";
                    if (!desc.startsWith("#")) desc = "#" + desc;

                    previewModel.addRow(new Object[]{
                        formattedDate,
                        desc,
                        String.format("PHP %.2f", rs.getDouble("amount"))
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not load preview entries: " + e.getMessage());
        }
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(CORNSILK);
        button.setBackground(HOVER_OLIVE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createLightButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(DARK_OLIVE);
        button.setBackground(CORNSILK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(9, 12, 9, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 60, 30, 18));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 6, radius, radius);
                g2.setColor(backgroundColor);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 7, radius, radius);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
