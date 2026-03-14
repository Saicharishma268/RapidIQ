import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;

public class AppConstants {
    public static final Color GREEN       = new Color(52, 160, 100);
    public static final Color DARK_GREEN  = new Color(34, 139, 75);
    public static final Color LIGHT_GREEN = new Color(220, 245, 230);
    public static final Color BG          = new Color(248, 249, 252);
    public static final Color SIDEBAR_BG  = new Color(25, 35, 45);
    public static final Color CARD_WHITE  = Color.WHITE;
    public static final Color TEXT_DARK   = new Color(15, 15, 15);
    public static final Color TEXT_GRAY   = new Color(120, 130, 145);
    public static final Color ACCENT_RED  = new Color(220, 60, 60);
    public static final Color ACCENT_BLUE = new Color(52, 120, 200);
    public static final Color ACCENT_GOLD = new Color(230, 170, 20);

    public static final Font F_TITLE  = new Font("Arial", Font.BOLD, 26);
    public static final Font F_H1     = new Font("Arial", Font.BOLD, 20);
    public static final Font F_H2     = new Font("Arial", Font.BOLD, 15);
    public static final Font F_BODY   = new Font("Arial", Font.PLAIN, 14);
    public static final Font F_SMALL  = new Font("Arial", Font.PLAIN, 12);
    public static final Font F_BTN    = new Font("Arial", Font.BOLD,  14);

    public static final String DB_URL  = "jdbc:mysql://localhost:3306/rapidiq";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "root";

    public static Connection getConn() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static boolean loadDriver(Component p) {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); return true; }
        catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(p,"MySQL Driver not found.\nAdd mysql-connector-java.jar","Error",JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static int timerForLevel(String level) {
        if (level == null) return 20;
        switch (level.toLowerCase()) {
            case "beginner":     return 20;
            case "intermediate": return 30;
            case "advanced":     return 40;
            default:             return 30;
        }
    }

    /**
     * Returns the minimum % each individual topic must score to unlock the NEXT level.
     *   Beginner topics each need >= 70%  --> unlocks Intermediate
     *   Intermediate topics each need >= 80% --> unlocks Advanced
     */
    public static double unlockThreshold(String level) {
        if (level == null) return 0;
        switch (level.toLowerCase()) {
            case "beginner":     return 70.0;
            case "intermediate": return 80.0;
            case "advanced":     return 80.0;
            default:             return 70.0;
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────
    public static JButton roundBtn(String text, Color bg, Color hover) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_BTN); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton greenBtn(String t)  { return roundBtn(t, GREEN, DARK_GREEN); }
    public static JButton redBtn(String t)    { return roundBtn(t, ACCENT_RED, new Color(180,40,40)); }
    public static JButton blueBtn(String t)   { return roundBtn(t, ACCENT_BLUE, new Color(30,90,170)); }

    /** White rounded card */
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_WHITE);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setLayout(new BorderLayout());
        return p;
    }

    public static JPanel card(LayoutManager lm) {
        JPanel p = card(); p.setLayout(lm); return p;
    }

    public static JPanel titledField(String title, JTextField f) {
        f.setFont(F_BODY);
        f.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)), title,
            TitledBorder.LEFT, TitledBorder.TOP, F_SMALL, TEXT_GRAY));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(f); return p;
    }

    public static JPanel buildLoginLeft(String title, String sub) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(GREEN);
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(GREEN);
        JLabel t = new JLabel(title); t.setFont(new Font("Georgia",Font.BOLD,34)); t.setForeground(Color.WHITE); t.setAlignmentX(0.5f);
        JLabel s = new JLabel(sub);   s.setFont(new Font("Arial",Font.BOLD,18));   s.setForeground(Color.WHITE); s.setAlignmentX(0.5f);
        inner.add(Box.createVerticalGlue()); inner.add(t); inner.add(Box.createVerticalStrut(16)); inner.add(s); inner.add(Box.createVerticalGlue());
        panel.add(inner); return panel;
    }

    public static void msg(Component p, String m, String t, int type) {
        JOptionPane.showMessageDialog(p, m, t, type);
    }
}