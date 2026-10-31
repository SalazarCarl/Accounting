import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

// Theme.java: all the design lives here (colors, fonts, rounded inputs)
// so every screen looks the same. Ayaw usba ni kung dili kinahanglan.
public class Theme {

    // ===== mga kolor pallete ni =====
    static final Color WHITE      = Color.WHITE;
    static final Color CORNSILK   = new Color(0xFEFAE0);
    static final Color IVORY      = new Color(0xFFEFCD);
    static final Color OLIVE      = new Color(0x606C38);
    static final Color OLIVE_DEEP = new Color(0x3A4522);
    static final Color OLIVE_DARK = new Color(0x4A5429);
    static final Color OLIVE_TINT = new Color(0xDCE2C4);
    static final Color ERROR      = new Color(0xB3402A);   // red, for error messages

    static Font serif(int style, int size) { return new Font("Georgia", style, size); }
    static Font sans(int style, int size)  { return new Font("Segoe UI", style, size); }

    // smooth edges para dili jagged ang curves
    static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    // ===== every screen extends this =====
    // onShow() runs every time the screen is shown (para limpyo balik ang fields)
    static abstract class Screen extends JPanel {
        void onShow() { }
    }

    // ===== rounded parts (Swing has no rounded input, so we draw our own) =====

    // draws the white pill behind the text field
    static void paintPill(Graphics g, JComponent c) {
        Graphics2D g2 = smooth(g);
        int w = c.getWidth(), h = c.getHeight();
        g2.setColor(WHITE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
        g2.setColor(c.hasFocus() ? OLIVE : new Color(96, 108, 56, 90));
        g2.setStroke(new BasicStroke(c.hasFocus() ? 2f : 1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
        g2.dispose();
    }

    // Swing has no placeholder, so we draw the hint text kung empty ang field
    static void drawHint(Graphics g, JTextComponent c, String hint, boolean empty) {
        if (!empty) return;
        Graphics2D g2 = smooth(g);
        g2.setFont(c.getFont());
        g2.setColor(new Color(96, 108, 56, 150));
        FontMetrics fm = g2.getFontMetrics();
        int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(hint, c.getInsets().left, y);
        g2.dispose();
    }

    static void styleField(JTextComponent c) {
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(10, 18, 10, 18));
        c.setFont(sans(Font.PLAIN, 14));
        c.setForeground(OLIVE_DEEP);
        c.setCaretColor(OLIVE_DEEP);
        c.setPreferredSize(new Dimension(340, 44));
        c.addFocusListener(new FocusAdapter() {   // redraw para mo-change ang border color
            public void focusGained(FocusEvent e) { c.repaint(); }
            public void focusLost(FocusEvent e)   { c.repaint(); }
        });
    }

    static class PillField extends JTextField {
        private final String hint;
        PillField(String hint) { this.hint = hint; styleField(this); }
        @Override protected void paintComponent(Graphics g) {
            paintPill(g, this);
            super.paintComponent(g);
            drawHint(g, this, hint, getText().isEmpty());
        }
    }

    static class PillPassword extends JPasswordField {
        private final String hint;
        PillPassword(String hint) { this.hint = hint; styleField(this); }
        @Override protected void paintComponent(Graphics g) {
            paintPill(g, this);
            super.paintComponent(g);
            drawHint(g, this, hint, getPassword().length == 0);
        }
    }

    static class PillButton extends JButton {
        PillButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setForeground(CORNSILK);
            setFont(sans(Font.BOLD, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(340, 46));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(getModel().isRollover() ? OLIVE_DARK : OLIVE);   // darker kung naka-hover
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // the "ENTRI" pill sa taas. Palitan ni sa logo later.
    static class Pill extends JLabel {
        Pill(String t) {
            super(t);
            setFont(sans(Font.BOLD, 14));
            setForeground(OLIVE_DEEP);
            setBorder(new EmptyBorder(8, 20, 8, 20));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(OLIVE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===== big panels =====

    // olive gradient sa background
    static class BgPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setPaint(new LinearGradientPaint(0, 0, getWidth(), getHeight(),
                new float[]{0f, 0.45f, 1f},
                new Color[]{new Color(0x8A9A5B), OLIVE, new Color(0x2B3318)}));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    // the white rounded card with two halves inside
    static class Shell extends JPanel {
        Shell() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            g.setColor(WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        @Override public void paint(Graphics g) {
            int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D b = buf.createGraphics();
            super.paint(b);                        // draw everything inside to an image first
            b.dispose();
            Graphics2D g2 = smooth(g);             // then stamp it with rounded corners
            g2.setPaint(new TexturePaint(buf, new Rectangle(0, 0, w, h)));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 48, 48));
            g2.dispose();
        }
    }

    // left half: white to cornsilk to light olive
    static class FormSide extends JPanel {
        FormSide() { setLayout(new BorderLayout()); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setPaint(new LinearGradientPaint(0, 0, getWidth(), getHeight(),
                new float[]{0f, 0.45f, 1f}, new Color[]{WHITE, CORNSILK, OLIVE_TINT}));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    // right half: drawer photo, olive tint, and caption
    static class VisualSide extends JPanel {
        private static BufferedImage img;
        private static boolean tried = false;   // load the image once lang
        VisualSide() {
            setOpaque(false);
            if (!tried) {
                tried = true;
                try {
                    File f = new File("drawer.jpg");   // must be in the same folder where you run the program
                    if (f.exists()) img = ImageIO.read(f);
                } catch (Exception e) { /* no image: plain olive panel lang ang makita */ }
            }
        }
        @Override protected void paintComponent(Graphics g) {
            int m = 14, w = getWidth() - 2 * m, h = getHeight() - 2 * m;
            if (w <= 0 || h <= 0) return;
            BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D b = buf.createGraphics();
            b.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            b.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            b.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            b.setColor(OLIVE);
            b.fillRect(0, 0, w, h);
            if (img != null) {                     // scale the image to fill the panel
                double s = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
                int iw = (int) (img.getWidth() * s), ih = (int) (img.getHeight() * s);
                b.drawImage(img, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
            }
            b.setPaint(new GradientPaint(0, 0, new Color(96, 108, 56, 60), 0, h, new Color(58, 69, 34, 235)));
            b.fillRect(0, 0, w, h);                // the olive tint
            b.setColor(CORNSILK);
            b.setFont(serif(Font.PLAIN, 28));
            b.drawString("Keep every entry", 32, h - 112);
            b.drawString("in order.", 32, h - 74);
            b.setColor(IVORY);
            b.setFont(sans(Font.PLAIN, 14));
            b.drawString("Your ledger and columnar records,", 32, h - 44);
            b.drawString("all in one place.", 32, h - 24);
            b.dispose();
            Graphics2D g2 = smooth(g);
            g2.setPaint(new TexturePaint(buf, new Rectangle(m, m, w, h)));
            g2.fill(new RoundRectangle2D.Float(m, m, w, h, 36, 36));
            g2.dispose();
        }
    }

    // ===== helpers =====

    // add a component below the previous one (parang nag-stack og blocks)
    static void addRow(JPanel p, Component c, int top) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = GridBagConstraints.RELATIVE;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(top, 0, 0, 0);
        p.add(c, gc);
    }

    static JLabel label(String text, Font font, Color color, int align) {
        JLabel l = new JLabel(text, align);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    // small title above the input (Username, Password, etc.)
    static JLabel fieldLabel(String text) {
        JLabel l = label(text, sans(Font.BOLD, 13), OLIVE_DEEP, SwingConstants.LEFT);
        l.setBorder(new EmptyBorder(0, 6, 0, 0));
        return l;
    }

    // clickable link (Forgot password?, Sign up, etc.)
    static JLabel link(String text, int align, Runnable action) {
        JLabel l = label(text, sans(Font.BOLD, 13), OLIVE, align);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { action.run(); }
        });
        return l;
    }

    // "Don't have an account? Sign up" sa ubos
    static JPanel footer(String text, String linkText, Runnable action) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        p.setOpaque(false);
        p.add(label(text, sans(Font.PLAIN, 13), OLIVE_DEEP, SwingConstants.CENTER));
        p.add(link(linkText, SwingConstants.CENTER, action));
        return p;
    }

    // space for error/success message. Fixed size para dili mo-shift ang form
    static JLabel msgLabel() {
        JLabel l = label(" ", sans(Font.PLAIN, 13), ERROR, SwingConstants.CENTER);
        l.setPreferredSize(new Dimension(340, 20));
        return l;
    }

    // show the message: good=true is olive (success), false is red (error)
    static void say(JLabel l, String text, boolean good) {
        l.setForeground(good ? OLIVE : ERROR);
        l.setText(text.isEmpty() ? " " : text);
    }

    // split layout (form on the left, image on the right) for login, sign up, forgot
    static JPanel authLayout(JPanel form) {
        FormSide left = new FormSide();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(36, 48, 0, 48));
        top.add(new Pill("ENTRI"));
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(form);
        left.add(top, BorderLayout.NORTH);
        left.add(center, BorderLayout.CENTER);

        Shell shell = new Shell();
        shell.setLayout(new GridBagLayout());
        shell.setPreferredSize(new Dimension(980, 620));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.gridx = 0; gc.weightx = 1.0; shell.add(left, gc);
        gc.gridx = 1; gc.weightx = 1.1; shell.add(new VisualSide(), gc);
        return wrap(shell);
    }

    // a plain card without image (for the dashboard)
    static JPanel plainCard(JPanel content) {
        FormSide card = new FormSide();
        card.setLayout(new GridBagLayout());
        card.add(content);
        Shell shell = new Shell();
        shell.setLayout(new GridBagLayout());
        shell.setPreferredSize(new Dimension(980, 620));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1; gc.weighty = 1;
        shell.add(card, gc);
        return wrap(shell);
    }

    // put the shell on top of the olive gradient
    private static JPanel wrap(Shell shell) {
        BgPanel bg = new BgPanel();
        bg.setLayout(new GridBagLayout());
        bg.setBorder(new EmptyBorder(24, 24, 24, 24));
        GridBagConstraints all = new GridBagConstraints();
        all.fill = GridBagConstraints.BOTH;
        all.weightx = 1; all.weighty = 1;
        bg.add(shell, all);
        return bg;
    }
}
