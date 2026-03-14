import java.awt.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class TopicChooserDialog extends JDialog {

    private final int userId;
    private final String username;
    private final JFrame owner;
    private final UserDashboard.LevelInfo level;

    public int    pendingTopicId   = -1;
    public String pendingTopicName = null;
    public String pendingLevelName = null;

    private static final Color[] ACCENT = { new Color(25,120,70), new Color(195,90,10), new Color(100,30,160) };
    private static final Color[] LITE   = { new Color(200,240,218), new Color(255,222,180), new Color(220,200,255) };
    private static final String[] LEVEL_LABELS = { "BEGINNER", "INTERMEDIATE", "ADVANCED" };

    public TopicChooserDialog(JFrame owner, int userId, String username, UserDashboard.LevelInfo level) {
        super(owner, level.name + " - Select Topic", true);
        this.owner = owner; this.userId = userId;
        this.username = username; this.level = level;
        setSize(860, 620);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(owner);
        setResizable(true);
        buildUI();
        setVisible(true);
    }

    private int levelIndex() {
        String n = level.name.toLowerCase();
        if (n.contains("inter")) return 1;
        if (n.contains("adv"))   return 2;
        return 0;
    }

    private void buildUI() {
        int idx      = levelIndex();
        Color accent = ACCENT[idx];
        Color lite   = LITE[idx];

        List<TopicRow> topics = loadTopics();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 252));

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(accent);
        header.setBorder(new EmptyBorder(22, 30, 22, 24));

        JPanel hLeft = new JPanel();
        hLeft.setLayout(new BoxLayout(hLeft, BoxLayout.Y_AXIS));
        hLeft.setOpaque(false);

        JLabel levelTag = new JLabel(LEVEL_LABELS[idx]);
        levelTag.setFont(new Font("Arial", Font.BOLD, 11));
        levelTag.setForeground(new Color(255,255,255,180));
        levelTag.setAlignmentX(0);

        JLabel title = new JLabel("Choose a Topic");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(0);
        title.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel sub = new JLabel("5 questions  |  " + AppConstants.timerForLevel(level.name) + " seconds per question");
        sub.setFont(new Font("Arial", Font.PLAIN, 13));
        sub.setForeground(new Color(255,255,255,180));
        sub.setAlignmentX(0);
        sub.setBorder(new EmptyBorder(6, 0, 0, 0));

        long passed = topics.stream().filter(t -> t.bestPct >= level.requiredPct && t.bestPct >= 0).count();
        double pct  = topics.isEmpty() ? 0 : (double)passed / topics.size();

        JPanel progRow = new JPanel(new BorderLayout(10, 0));
        progRow.setOpaque(false);
        progRow.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);

        JLabel progLbl = new JLabel(passed + " / " + topics.size()
            + " topics passed  |  Required: >=" + (int)level.requiredPct + "% per topic");
        progLbl.setFont(new Font("Arial", Font.BOLD, 12));
        progLbl.setForeground(new Color(255,255,255,210));
        progLbl.setAlignmentX(0);

        JPanel miniBarWrap = new JPanel(new BorderLayout(0,4));
        miniBarWrap.setOpaque(false);
        miniBarWrap.setBorder(new EmptyBorder(5,0,0,0));

        JPanel miniBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,60));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),6,6));
                if (pct > 0) {
                    g2.setColor(Color.WHITE);
                    g2.fill(new RoundRectangle2D.Float(0,0,(float)(getWidth()*pct),getHeight(),6,6));
                }
                g2.dispose();
            }
        };
        miniBar.setOpaque(false);
        miniBar.setPreferredSize(new Dimension(0, 8));
        miniBarWrap.add(miniBar, BorderLayout.CENTER);

        statsPanel.add(progLbl);
        statsPanel.add(miniBarWrap);
        progRow.add(statsPanel, BorderLayout.CENTER);

        hLeft.add(levelTag); hLeft.add(title); hLeft.add(sub); hLeft.add(progRow);

        JButton closeBtn = new JButton("X");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setForeground(new Color(255,255,255,180));
        closeBtn.setContentAreaFilled(false); closeBtn.setBorderPainted(false); closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        header.add(hLeft, BorderLayout.CENTER);
        header.add(closeBtn, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Content ──────────────────────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(245, 247, 252));
        content.setBorder(new EmptyBorder(20, 22, 10, 22));

        if (topics.isEmpty()) {
            content.add(buildEmptyPanel(accent), BorderLayout.CENTER);
        } else {
            int cols = topics.size() == 1 ? 1 : 2;
            int rows = (int) Math.ceil(topics.size() / (double) cols);
            JPanel grid = new JPanel(new GridLayout(rows, cols, 16, 16));
            grid.setOpaque(false);
            for (TopicRow t : topics) grid.add(buildTopicCard(t, accent, lite));
            if (topics.size() % 2 != 0 && cols == 2) grid.add(emptyCard());

            JScrollPane sc = new JScrollPane(grid);
            sc.setBorder(null); sc.setOpaque(false); sc.getViewport().setOpaque(false);
            sc.getVerticalScrollBar().setUnitIncrement(16);
            content.add(sc, BorderLayout.CENTER);
        }

        root.add(content, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0, new Color(220,224,232)),
            new EmptyBorder(13, 24, 13, 24)));

        JLabel hint = new JLabel("Select any topic to start  |  Timer resets for each question");
        hint.setFont(new Font("Arial", Font.PLAIN, 12));
        hint.setForeground(new Color(140,150,170));

        JButton closeBtn2 = new JButton("Close") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(160,30,30) : new Color(205,45,45));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        closeBtn2.setForeground(Color.WHITE); closeBtn2.setFont(new Font("Arial",Font.BOLD,13));
        closeBtn2.setContentAreaFilled(false); closeBtn2.setBorderPainted(false); closeBtn2.setFocusPainted(false);
        closeBtn2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn2.setPreferredSize(new Dimension(100,38));
        closeBtn2.addActionListener(e -> dispose());

        footer.add(hint, BorderLayout.WEST);
        footer.add(closeBtn2, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildTopicCard(TopicRow t, Color accent, Color lite) {
        boolean attempted = t.bestPct >= 0;
        boolean passed    = attempted && t.bestPct >= level.requiredPct;

        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,10));
                g2.fill(new RoundRectangle2D.Float(4,4,getWidth()-4,getHeight()-4,16,16));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-4,getHeight()-4,16,16));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-4,8,4,4));
                if (passed) {
                    g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),10));
                    g2.fillRect(0, 8, getWidth()-4, getHeight()-12);
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 18, 18));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel nameLbl = new JLabel(t.name);
        nameLbl.setFont(new Font("Arial", Font.BOLD, 16));
        nameLbl.setForeground(new Color(22, 30, 50));
        nameLbl.setAlignmentX(0);
        top.add(nameLbl);
        top.add(Box.createVerticalStrut(10));
        top.add(buildBadge(t.bestPct, passed, accent));
        card.add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel attLbl = new JLabel(attempted
            ? t.attempts + " attempt" + (t.attempts > 1 ? "s" : "")
            : "Not yet attempted");
        attLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        attLbl.setForeground(new Color(150, 158, 175));

        JButton startBtn = new JButton("Start") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? accent.darker() : accent);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 13));
        startBtn.setContentAreaFilled(false); startBtn.setBorderPainted(false); startBtn.setFocusPainted(false);
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.setPreferredSize(new Dimension(100, 36));
        startBtn.addActionListener(e -> {
            pendingTopicId   = t.id;
            pendingTopicName = t.name;
            pendingLevelName = level.name;
            dispose();
        });

        bottom.add(attLbl, BorderLayout.WEST);
        bottom.add(startBtn, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildBadge(double pct, boolean passed, Color accent) {
        boolean attempted = pct >= 0;
        Color bg = passed    ? new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),30)
                 : attempted ? new Color(255,236,180)
                 : new Color(234,236,242);
        Color fg = passed    ? accent
                 : attempted ? new Color(160,110,0)
                 : new Color(130,140,165);
        String txt = passed    ? String.format("[PASS]  Best: %.0f%%", pct)
                   : attempted ? String.format("[retry] Best: %.0f%%", pct)
                   : "Not attempted yet";
        JPanel b = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setOpaque(false); b.setLayout(new BoxLayout(b,BoxLayout.X_AXIS));
        b.setBorder(new EmptyBorder(5,12,5,12));
        b.setMaximumSize(new Dimension(280, 28)); b.setAlignmentX(0);
        JLabel l = new JLabel(txt); l.setFont(new Font("Arial",Font.BOLD,12)); l.setForeground(fg);
        b.add(l); return b;
    }

    private JPanel buildEmptyPanel(Color accent) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setOpaque(false);
        p.add(Box.createVerticalGlue());
        JLabel msg = new JLabel("No topics added yet");
        msg.setFont(new Font("Arial",Font.BOLD,18)); msg.setForeground(new Color(80,90,110)); msg.setAlignmentX(0.5f);
        JLabel sub = new JLabel("Login as Admin to add topics for this level.");
        sub.setFont(new Font("Arial",Font.PLAIN,13)); sub.setForeground(new Color(130,140,160)); sub.setAlignmentX(0.5f);
        sub.setBorder(new EmptyBorder(8,0,0,0));
        p.add(msg); p.add(sub); p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel emptyCard() { JPanel p = new JPanel(); p.setOpaque(false); return p; }

    private List<TopicRow> loadTopics() {
        List<TopicRow> list = new ArrayList<>();
        try (Connection c = AppConstants.getConn()) {
            PreparedStatement ps = c.prepareStatement(
                "SELECT topic_id, topic_name FROM topics WHERE level_id=? ORDER BY topic_name");
            ps.setInt(1, level.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int tid = rs.getInt(1); String tname = rs.getString(2);
                PreparedStatement ps2 = c.prepareStatement(
                    "SELECT MAX(percentage), COUNT(*) FROM quiz_attempts WHERE user_id=? AND topic_id=?");
                ps2.setInt(1, userId); ps2.setInt(2, tid);
                ResultSet r2 = ps2.executeQuery();
                double best = -1; int att = 0;
                if (r2.next()) { best = r2.getDouble(1); att = r2.getInt(2); if (r2.wasNull()) best = -1; }
                list.add(new TopicRow(tid, tname, best, att));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    static class TopicRow {
        int id, attempts; String name; double bestPct;
        TopicRow(int id, String n, double b, int a) { this.id=id; name=n; bestPct=b; attempts=a; }
    }
}