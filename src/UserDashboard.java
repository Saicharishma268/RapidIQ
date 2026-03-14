import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class UserDashboard extends JFrame {
    private final int userId;
    private final String username;

    public UserDashboard(int userId, String username) {
        this.userId   = userId;
        this.username = username;
        setTitle("RapidIQ");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setContentPane(buildMain());
        setVisible(true);
    }

    private JPanel buildMain() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppConstants.BG);
        root.add(buildTopBar(),  BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        return root;
    }

    // ── Top bar ──────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,new Color(220,220,220)),
            new EmptyBorder(12,24,12,24)));

        JLabel logo = new JLabel("RapidIQ");
        logo.setFont(new Font("Georgia",Font.BOLD,22));
        logo.setForeground(AppConstants.TEXT_DARK);
        bar.add(logo, BorderLayout.WEST);

        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConstants.GREEN);
                g2.fillOval(0,0,38,38);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial",Font.BOLD,16));
                FontMetrics fm = g2.getFontMetrics();
                String ch = username.substring(0,1).toUpperCase();
                g2.drawString(ch, (38-fm.stringWidth(ch))/2, (38+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38,38));
        bar.add(avatar, BorderLayout.EAST);
        return bar;
    }

    // ── Main content ─────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(AppConstants.BG);
        content.setBorder(new EmptyBorder(24,24,24,24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0,0,0,0);

        gc.gridx=0; gc.gridy=0; gc.weightx=0.65; gc.weighty=1.0;
        content.add(buildLeftCol(), gc);

        gc.gridx=1; gc.weightx=0.02; gc.weighty=1.0;
        JPanel gap = new JPanel(); gap.setOpaque(false); content.add(gap,gc);

        gc.gridx=2; gc.weightx=0.33; gc.weighty=1.0;
        content.add(buildRightCol(), gc);

        return content;
    }

    // ── LEFT COLUMN ──────────────────────────────────────────────────────────
    private JPanel buildLeftCol() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.add(buildWelcomeCard());
        col.add(Box.createVerticalStrut(16));
        List<LevelInfo> levels = loadLevels();
        for (LevelInfo lv : levels) {
            col.add(buildLevelCard(lv));
            col.add(Box.createVerticalStrut(12));
        }
        return col;
    }

    private JPanel buildWelcomeCard() {
        JPanel card = AppConstants.card(new BorderLayout());
        card.setBorder(new EmptyBorder(22,24,22,24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        card.setMinimumSize(new Dimension(0, 150));
        card.setPreferredSize(new Dimension(0, 150));

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        JLabel welcome = new JLabel("Welcome, " + username + "!");
        welcome.setFont(AppConstants.F_TITLE);
        welcome.setForeground(AppConstants.TEXT_DARK);
        welcome.setAlignmentX(0);

        String curLevel = getCurrentLevel();
        JLabel levelLbl = new JLabel("Current Level: " + curLevel);
        levelLbl.setFont(AppConstants.F_BODY);
        levelLbl.setForeground(AppConstants.TEXT_GRAY);
        levelLbl.setAlignmentX(0);
        levelLbl.setBorder(new EmptyBorder(6,0,0,0));

        topSection.add(welcome);
        topSection.add(levelLbl);
        card.add(topSection, BorderLayout.NORTH);

        double overall = getOverallProgress();
        JPanel progSection = new JPanel();
        progSection.setLayout(new BoxLayout(progSection, BoxLayout.Y_AXIS));
        progSection.setOpaque(false);
        progSection.setBorder(new EmptyBorder(14,0,0,0));

        JPanel progRow = new JPanel(new BorderLayout(12,0));
        progRow.setOpaque(false);
        JLabel progLbl = new JLabel("Overall Progress");
        progLbl.setFont(AppConstants.F_H2); progLbl.setForeground(AppConstants.TEXT_DARK);
        JLabel pct = new JLabel(String.format("%.0f%%", overall));
        pct.setFont(new Font("Arial",Font.BOLD,18)); pct.setForeground(AppConstants.GREEN);
        progRow.add(progLbl, BorderLayout.WEST);
        progRow.add(pct, BorderLayout.EAST);

        ProgressBar pb = new ProgressBar(overall/100.0, AppConstants.GREEN);
        pb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
        pb.setPreferredSize(new Dimension(0, 14));

        progSection.add(progRow);
        progSection.add(Box.createVerticalStrut(8));
        progSection.add(pb);
        card.add(progSection, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildLevelCard(LevelInfo lv) {
        boolean locked = lv.locked;

        int topicsMet = 0, topicsTotal = 0;
        try (Connection c = AppConstants.getConn()) {
            PreparedStatement ts = c.prepareStatement("SELECT topic_id FROM topics WHERE level_id=?");
            ts.setInt(1, lv.id);
            ResultSet topics = ts.executeQuery();
            while (topics.next()) {
                topicsTotal++;
                int tid = topics.getInt(1);
                PreparedStatement ps = c.prepareStatement(
                    "SELECT MAX(percentage) FROM quiz_attempts WHERE user_id=? AND topic_id=?");
                ps.setInt(1, userId); ps.setInt(2, tid);
                ResultSet r = ps.executeQuery();
                if (r.next() && !r.wasNull() && r.getDouble(1) >= lv.requiredPct) topicsMet++;
            }
        } catch (Exception ignored) {}

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = locked ? new Color(235,238,243) : new Color(230,240,255);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16,20,16,20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);

        // Use [LOCK] text instead of lock emoji
        JLabel nameLbl = new JLabel((locked ? "[LOCKED] " : "") + lv.name);
        nameLbl.setFont(AppConstants.F_H1);
        nameLbl.setForeground(locked ? AppConstants.TEXT_GRAY : AppConstants.TEXT_DARK);
        hdr.add(nameLbl, BorderLayout.WEST);

        if (!locked) {
            JButton startBtn = AppConstants.greenBtn("Start Quiz ->");
            startBtn.setFont(new Font("Arial",Font.BOLD,12));
            startBtn.setPreferredSize(new Dimension(120,32));
            startBtn.addActionListener(e -> openTopicChooser(lv));
            hdr.add(startBtn, BorderLayout.EAST);
        }
        card.add(hdr, BorderLayout.NORTH);

        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
        midPanel.setOpaque(false);
        midPanel.setBorder(new EmptyBorder(5,0,5,0));

        JLabel progInfo = new JLabel(String.format(
            "Avg: %.0f%%  -  each topic needs >=%d%% to unlock next level",
            lv.progress, (int)lv.requiredPct));
        progInfo.setFont(AppConstants.F_SMALL);
        progInfo.setForeground(AppConstants.TEXT_GRAY);

        boolean allPassed = (topicsMet == topicsTotal && topicsTotal > 0);
        Color statusColor = allPassed ? AppConstants.GREEN : AppConstants.ACCENT_RED;
        String statusIcon = allPassed ? "[OK] " : "[!] ";
        JLabel topicStatus = new JLabel(statusIcon + topicsMet + " / " + topicsTotal
            + " topics scored >=" + (int)lv.requiredPct + "%"
            + (allPassed ? "  - next level unlocked!" : "  - improve failing topics to unlock"));
        topicStatus.setFont(new Font("Arial", Font.BOLD, 12));
        topicStatus.setForeground(statusColor);

        midPanel.add(progInfo);
        midPanel.add(Box.createVerticalStrut(3));
        midPanel.add(topicStatus);
        card.add(midPanel, BorderLayout.CENTER);

        ProgressBar pb = new ProgressBar(lv.progress/100.0, locked ? new Color(160,170,185) : AppConstants.GREEN);
        pb.setMaximumSize(new Dimension(Integer.MAX_VALUE,16));
        card.add(pb, BorderLayout.SOUTH);

        return card;
    }

    // ── RIGHT COLUMN ─────────────────────────────────────────────────────────
    private JPanel buildRightCol() {
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.BOTH;
        g.gridx   = 0;
        g.weightx = 1.0;
        g.insets  = new Insets(0, 0, 8, 0);

        g.gridy = 0; g.weighty = 0.42; col.add(buildTopicsAttemptedCard(), g);
        g.gridy = 1; g.weighty = 0.13; col.add(buildStatsCard(), g);
        g.gridy = 2; g.weighty = 0.37; col.add(buildLeaderboardCard(), g);
        g.gridy = 3; g.weighty = 0.08; g.insets = new Insets(0,0,0,0);
        col.add(buildLogoutBtn(), g);

        return col;
    }

    private JPanel buildTopicsAttemptedCard() {
        JPanel card = AppConstants.card(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 0, 0, 0));

        JLabel hdr = new JLabel("Topics Attempted");
        hdr.setFont(AppConstants.F_H2); hdr.setForeground(AppConstants.TEXT_DARK);
        hdr.setBorder(new EmptyBorder(0, 20, 10, 20));
        card.add(hdr, BorderLayout.NORTH);

        Color[] tabColors = {
            new Color(34, 139, 87),
            new Color(215, 110, 15),
            new Color(120, 40, 170)
        };
        // No emoji in tab labels
        String[] tabLabels = {"Beginner", "Intermediate", "Advanced"};

        String[] levelNames = {"", "", ""};
        java.util.List<java.util.List<Object[]>> allTopics = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) allTopics.add(new java.util.ArrayList<>());

        try (Connection c = AppConstants.getConn()) {
            ResultSet levels = c.createStatement().executeQuery(
                "SELECT level_id, level_name FROM levels ORDER BY level_id");
            int idx = 0;
            while (levels.next() && idx < 3) {
                int lid = levels.getInt(1);
                levelNames[idx] = levels.getString(2);
                PreparedStatement tps = c.prepareStatement(
                    "SELECT topic_id, topic_name FROM topics WHERE level_id=? ORDER BY topic_name");
                tps.setInt(1, lid);
                ResultSet topics = tps.executeQuery();
                while (topics.next()) {
                    int tid = topics.getInt(1); String tname = topics.getString(2);
                    PreparedStatement ps2 = c.prepareStatement(
                        "SELECT MAX(percentage) FROM quiz_attempts WHERE user_id=? AND topic_id=?");
                    ps2.setInt(1, userId); ps2.setInt(2, tid);
                    ResultSet r2 = ps2.executeQuery();
                    double best = -1;
                    if (r2.next()) { best = r2.getDouble(1); if (r2.wasNull()) best = -1; }
                    allTopics.get(idx).add(new Object[]{tname, best});
                }
                idx++;
            }
        } catch (SQLException ignored) {}

        JPanel tabBar = new JPanel(new GridLayout(1, 3, 0, 0));
        tabBar.setOpaque(false);
        tabBar.setBorder(new EmptyBorder(0, 12, 0, 12));

        CardLayout cl = new CardLayout();
        JPanel contentArea = new JPanel(cl);
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(10, 12, 10, 12));

        JButton[] tabs = new JButton[3];

        for (int i = 0; i < 3; i++) {
            final int fi = i;
            Color col = tabColors[i];
            String name = levelNames[i].isEmpty() ? tabLabels[i] : levelNames[i];
            java.util.List<Object[]> rows = allTopics.get(i);

            JButton tab = new JButton(name) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if ((Boolean) getClientProperty("active")) {
                        g2.setColor(col);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                        setForeground(Color.WHITE);
                    } else {
                        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 25));
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                        g2.setColor(col);
                        g2.setStroke(new BasicStroke(1.2f));
                        g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10));
                        setForeground(col);
                    }
                    g2.dispose(); super.paintComponent(g);
                }
            };
            tab.setFont(new Font("Arial", Font.BOLD, 11));
            tab.setContentAreaFilled(false); tab.setBorderPainted(false); tab.setFocusPainted(false);
            tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
            tab.putClientProperty("active", i == 0);
            tab.setPreferredSize(new Dimension(0, 32));
            tabs[i] = tab;

            JPanel tabContent = new JPanel();
            tabContent.setLayout(new BoxLayout(tabContent, BoxLayout.Y_AXIS));
            tabContent.setOpaque(false);

            if (rows.isEmpty()) {
                JLabel none = new JLabel("No topics added yet");
                none.setFont(AppConstants.F_SMALL);
                none.setForeground(new Color(col.getRed(), col.getGreen(), col.getBlue(), 160));
                none.setAlignmentX(0.5f);
                tabContent.add(Box.createVerticalStrut(8)); tabContent.add(none);
            } else {
                long done = rows.stream().filter(r -> (double)r[1] >= 0).count();
                JLabel summary = new JLabel(done + " of " + rows.size() + " topics attempted");
                summary.setFont(new Font("Arial", Font.PLAIN, 11));
                summary.setForeground(new Color(col.getRed(), col.getGreen(), col.getBlue(), 180));
                summary.setAlignmentX(0);
                tabContent.add(summary);
                tabContent.add(Box.createVerticalStrut(8));

                for (Object[] r : rows) {
                    String tname = (String) r[0]; double pct = (double) r[1];
                    boolean attempted = pct >= 0;

                    JPanel tRow = new JPanel(new BorderLayout(6, 0)) {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(attempted
                                ? new Color(col.getRed(), col.getGreen(), col.getBlue(), 18)
                                : new Color(245,245,250));
                            g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                            g2.dispose(); super.paintComponent(g);
                        }
                    };
                    tRow.setOpaque(false);
                    tRow.setBorder(new EmptyBorder(7, 10, 7, 10));
                    tRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

                    // Use plain text tick/circle instead of emoji
                    JLabel dotLbl = new JLabel(attempted ? "+" : "o");
                    dotLbl.setFont(new Font("Arial", Font.BOLD, 13));
                    dotLbl.setForeground(attempted ? col : new Color(190,193,205));

                    JLabel nameLbl = new JLabel(tname);
                    nameLbl.setFont(AppConstants.F_BODY); nameLbl.setForeground(AppConstants.TEXT_DARK);

                    JPanel left = new JPanel(new BorderLayout(6,0)); left.setOpaque(false);
                    left.add(dotLbl, BorderLayout.WEST); left.add(nameLbl, BorderLayout.CENTER);
                    tRow.add(left, BorderLayout.CENTER);

                    if (attempted) {
                        Color pc = pct>=70 ? col : pct>=40 ? new Color(200,130,0) : new Color(200,55,55);
                        JLabel pl = new JLabel(String.format("%.0f%%", pct));
                        pl.setFont(new Font("Arial", Font.BOLD, 12)); pl.setForeground(pc);
                        tRow.add(pl, BorderLayout.EAST);
                    } else {
                        JLabel dash = new JLabel("-");
                        dash.setFont(AppConstants.F_SMALL); dash.setForeground(new Color(200,200,210));
                        tRow.add(dash, BorderLayout.EAST);
                    }
                    tabContent.add(tRow);
                    tabContent.add(Box.createVerticalStrut(5));
                }
            }

            JScrollPane sc = new JScrollPane(tabContent);
            sc.setBorder(null); sc.setOpaque(false); sc.getViewport().setOpaque(false);
            sc.getVerticalScrollBar().setUnitIncrement(10);
            sc.setMinimumSize(new Dimension(0, 200));
            contentArea.add(sc, String.valueOf(i));

            tab.addActionListener(e -> {
                cl.show(contentArea, String.valueOf(fi));
                for (int j = 0; j < 3; j++) tabs[j].putClientProperty("active", j == fi);
                tabBar.repaint();
            });

            tabBar.add(tab);
        }

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(tabBar, BorderLayout.NORTH);
        center.add(contentArea, BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);

        cl.show(contentArea, "0");
        return card;
    }

    private JPanel buildStatsCard() {
        JPanel card = AppConstants.card(new BorderLayout());
        card.setBorder(new EmptyBorder(18,20,18,20));

        double bestScore = 0;
        int attempts = 0;
        try (Connection c = AppConstants.getConn()) {
            PreparedStatement ps = c.prepareStatement(
                "SELECT MAX(percentage), COUNT(*) FROM quiz_attempts WHERE user_id=?");
            ps.setInt(1,userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { bestScore=rs.getDouble(1); attempts=rs.getInt(2); }
        } catch (SQLException ignored) {}

        JPanel row1 = new JPanel(new BorderLayout()); row1.setOpaque(false);
        JLabel bl = new JLabel("Best Score"); bl.setFont(AppConstants.F_BODY); bl.setForeground(AppConstants.TEXT_GRAY);
        JLabel bv = new JLabel(String.format("%.0f%%",bestScore)); bv.setFont(new Font("Arial",Font.BOLD,18)); bv.setForeground(AppConstants.GREEN);
        row1.add(bl,BorderLayout.WEST); row1.add(bv,BorderLayout.EAST);

        JSeparator sep = new JSeparator(); sep.setForeground(new Color(230,230,230));

        JPanel row2 = new JPanel(new BorderLayout()); row2.setOpaque(false);
        JLabel al = new JLabel("Quizzes Attempted"); al.setFont(AppConstants.F_BODY); al.setForeground(AppConstants.TEXT_GRAY);
        JLabel av = new JLabel(String.valueOf(attempts)); av.setFont(new Font("Arial",Font.BOLD,18)); av.setForeground(AppConstants.TEXT_DARK);
        row2.add(al,BorderLayout.WEST); row2.add(av,BorderLayout.EAST);

        JPanel inner = new JPanel(); inner.setLayout(new BoxLayout(inner,BoxLayout.Y_AXIS)); inner.setOpaque(false);
        inner.add(row1); inner.add(Box.createVerticalStrut(8)); inner.add(sep);
        inner.add(Box.createVerticalStrut(8)); inner.add(row2);
        card.add(inner);
        return card;
    }

    private JPanel buildLeaderboardCard() {
        JPanel card = AppConstants.card(new BorderLayout());
        card.setBorder(new EmptyBorder(16,20,16,20));

        JPanel hdrRow = new JPanel(new BorderLayout());
        hdrRow.setOpaque(false);
        hdrRow.setBorder(new EmptyBorder(0,0,10,0));
        JLabel hdr = new JLabel("Leaderboard");   // no trophy emoji
        hdr.setFont(AppConstants.F_H2); hdr.setForeground(AppConstants.TEXT_DARK);
        JLabel topLbl = new JLabel("Top 5");
        topLbl.setFont(AppConstants.F_SMALL); topLbl.setForeground(AppConstants.TEXT_GRAY);
        hdrRow.add(hdr, BorderLayout.WEST);
        hdrRow.add(topLbl, BorderLayout.EAST);
        card.add(hdrRow, BorderLayout.NORTH);

        java.util.List<int[]>   ids    = new java.util.ArrayList<>();
        java.util.List<String>  names  = new java.util.ArrayList<>();
        java.util.List<Integer> scores = new java.util.ArrayList<>();
        int myRank = -1; int myScore = 0;

        try (Connection c = AppConstants.getConn()) {
            ResultSet rs = c.createStatement().executeQuery(
                "SELECT u.id, u.username, SUM(best.max_score) AS total_points " +
                "FROM users u " +
                "JOIN (SELECT user_id, topic_id, MAX(score) AS max_score " +
                "      FROM quiz_attempts GROUP BY user_id, topic_id) best " +
                "ON best.user_id = u.id " +
                "GROUP BY u.id ORDER BY total_points DESC");
            int rank = 1;
            while (rs.next()) {
                int uid2  = rs.getInt("id");
                int score = rs.getInt("total_points");
                ids.add(new int[]{uid2});
                names.add(rs.getString("username"));
                scores.add(score);
                if (uid2 == userId) { myRank = rank; myScore = score; }
                rank++;
            }
        } catch (SQLException ignored) {}

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        int limit = Math.min(5, ids.size());
        for (int i = 0; i < limit; i++) {
            boolean isMe = ids.get(i)[0] == userId;
            topPanel.add(buildLeaderRow(i+1, names.get(i), scores.get(i), isMe));
            if (i < limit - 1) topPanel.add(Box.createVerticalStrut(4));
        }
        card.add(topPanel, BorderLayout.CENTER);

        if (myRank > 5 && myRank != -1) {
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);
            JSeparator sep = new JSeparator(); sep.setForeground(new Color(200,200,210));
            bottomPanel.add(sep, BorderLayout.NORTH);
            bottomPanel.add(buildLeaderRow(myRank, username, myScore, true), BorderLayout.SOUTH);
            card.add(bottomPanel, BorderLayout.SOUTH);
        } else if (myRank >= 1 && myRank <= 5) {
            JLabel note = new JLabel("You are in the top 5!");
            note.setFont(new Font("Arial", Font.ITALIC, 11));
            note.setForeground(AppConstants.GREEN);
            note.setBorder(new EmptyBorder(6,0,0,0));
            JPanel notePanel = new JPanel(new BorderLayout());
            notePanel.setOpaque(false);
            notePanel.add(note, BorderLayout.WEST);
            card.add(notePanel, BorderLayout.SOUTH);
        }

        return card;
    }

    private JPanel buildLeaderRow(int rank, String name, int score, boolean isMe) {
        JPanel row = new JPanel(new BorderLayout(8,0));

        // Rank prefix — plain text, no medals/emoji
        String rankPrefix;
        if      (rank == 1) rankPrefix = "1st  ";
        else if (rank == 2) rankPrefix = "2nd  ";
        else if (rank == 3) rankPrefix = "3rd  ";
        else                rankPrefix = rank + "th  ";

        if (isMe) {
            row.setOpaque(true);
            row.setBackground(new Color(255, 243, 176));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,190,0), 1, true),
                new EmptyBorder(5,10,5,10)));
        } else {
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(5,10,5,10));
        }
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        Color textColor  = isMe ? new Color(120,80,0) : AppConstants.TEXT_DARK;
        Color scoreColor = isMe ? new Color(120,80,0) : AppConstants.TEXT_GRAY;

        JLabel rl = new JLabel(rankPrefix + name);
        rl.setFont(isMe ? new Font("Arial",Font.BOLD,13) : AppConstants.F_BODY);
        rl.setForeground(textColor);

        JLabel sl = new JLabel(score + " pts");
        sl.setFont(new Font("Arial",Font.BOLD,13));
        sl.setForeground(scoreColor);

        row.add(rl, BorderLayout.WEST);
        row.add(sl, BorderLayout.EAST);
        return row;
    }

    private JPanel buildLogoutBtn() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JButton btn = AppConstants.greenBtn("Logout");
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
        btn.addActionListener(e -> { new LoginPage(); dispose(); });
        p.add(btn);
        return p;
    }

    // ── DATA HELPERS ─────────────────────────────────────────────────────────
    private String getCurrentLevel() {
        try (Connection c = AppConstants.getConn()) {
            ResultSet lv = c.createStatement().executeQuery(
                "SELECT level_id, level_name FROM levels ORDER BY level_id");
            String current = "Beginner";
            while (lv.next()) {
                int lid = lv.getInt(1); String lname = lv.getString(2);
                double pct = getLevelProgress(c, lid);
                double req = AppConstants.unlockThreshold(lname);
                if (pct >= req) current = lname; else break;
            }
            return current;
        } catch (Exception e) { return "Beginner"; }
    }

    private double getOverallProgress() {
        try (Connection c = AppConstants.getConn()) {
            ResultSet ts = c.createStatement().executeQuery("SELECT topic_id FROM topics");
            int total = 0; double totalScore = 0;
            while (ts.next()) {
                total++;
                int tid = ts.getInt(1);
                PreparedStatement ps = c.prepareStatement(
                    "SELECT MAX(percentage) FROM quiz_attempts WHERE user_id=? AND topic_id=?");
                ps.setInt(1, userId); ps.setInt(2, tid);
                ResultSet r = ps.executeQuery();
                if (r.next() && !r.wasNull()) totalScore += r.getDouble(1);
            }
            return total == 0 ? 0 : totalScore / total;
        } catch (Exception e) { return 0; }
    }

    private double getLevelProgress(Connection c, int levelId) throws SQLException {
        PreparedStatement ts = c.prepareStatement(
            "SELECT topic_id FROM topics WHERE level_id=?");
        ts.setInt(1,levelId);
        ResultSet topics = ts.executeQuery();
        int count=0; double totalPct=0;
        while (topics.next()) {
            int tid = topics.getInt(1);
            PreparedStatement ps = c.prepareStatement(
                "SELECT MAX(percentage) FROM quiz_attempts WHERE user_id=? AND topic_id=?");
            ps.setInt(1,userId); ps.setInt(2,tid);
            ResultSet r = ps.executeQuery();
            count++;
            if (r.next()) totalPct += r.getDouble(1);
        }
        return count==0 ? 0 : totalPct/count;
    }

    private List<LevelInfo> loadLevels() {
        List<LevelInfo> list = new ArrayList<>();
        try (Connection c = AppConstants.getConn()) {
            ResultSet lv = c.createStatement().executeQuery(
                "SELECT level_id, level_name FROM levels ORDER BY level_id");
            List<int[]>  lvRows  = new ArrayList<>();
            List<String> lvNames = new ArrayList<>();
            while (lv.next()) {
                lvRows.add(new int[]{lv.getInt(1)});
                lvNames.add(lv.getString(2));
            }
            for (int i = 0; i < lvRows.size(); i++) {
                int    lid   = lvRows.get(i)[0];
                String lname = lvNames.get(i);
                double prog  = getLevelProgress(c, lid);
                boolean locked;
                double  requiredPct;
                if (i == 0) {
                    locked = false; requiredPct = 70.0;
                } else if (i == 1) {
                    locked = !allTopicsMeetThreshold(c, lvRows.get(0)[0], 70.0);
                    requiredPct = 80.0;
                } else {
                    locked = !allTopicsMeetThreshold(c, lvRows.get(1)[0], 80.0);
                    requiredPct = 80.0;
                }
                list.add(new LevelInfo(lid, lname, prog, requiredPct, locked));
            }
        } catch (Exception ignored) {}
        return list;
    }

    private boolean allTopicsMeetThreshold(Connection c, int levelId, double threshold) throws SQLException {
        PreparedStatement ts = c.prepareStatement(
            "SELECT topic_id FROM topics WHERE level_id=?");
        ts.setInt(1, levelId);
        ResultSet topics = ts.executeQuery();
        int count = 0;
        while (topics.next()) {
            count++;
            int tid = topics.getInt(1);
            PreparedStatement ps = c.prepareStatement(
                "SELECT MAX(percentage) FROM quiz_attempts WHERE user_id=? AND topic_id=?");
            ps.setInt(1, userId); ps.setInt(2, tid);
            ResultSet r = ps.executeQuery();
            double best = 0;
            if (r.next()) best = r.getDouble(1);
            if (best < threshold) return false;
        }
        return count > 0;
    }

    private void openTopicChooser(LevelInfo lv) {
        TopicChooserDialog dlg = new TopicChooserDialog(this, userId, username, lv);
        if (dlg.pendingTopicId >= 0) {
            final int tid   = dlg.pendingTopicId;
            final String tn = dlg.pendingTopicName;
            final String ln = dlg.pendingLevelName;
            SwingUtilities.invokeLater(() ->
                new QuizWindow(this, userId, username, tid, tn, ln)
            );
        }
    }

    static class LevelInfo {
        int id; String name; double progress, requiredPct; boolean locked;
        LevelInfo(int id, String name, double progress, double req, boolean locked) {
            this.id=id; this.name=name; this.progress=progress; this.requiredPct=req; this.locked=locked;
        }
    }
}

// ── Custom progress bar ──────────────────────────────────────────────────────
class ProgressBar extends JPanel {
    private final double fraction;
    private final Color color;

    ProgressBar(double fraction, Color color) {
        this.fraction = Math.min(1.0, Math.max(0,fraction));
        this.color = color;
        setOpaque(false);
        setPreferredSize(new Dimension(0, 14));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(new Color(215,220,228));
        g2.fill(new RoundRectangle2D.Float(0,0,w,h,h,h));
        int fw = (int)(w * fraction);
        if (fw > 0) {
            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Float(0,0,fw,h,h,h));
        }
        if (fraction > 0.1) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial",Font.BOLD,10));
            String txt = String.format("%.0f%%", fraction*100);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txt, fw/2 - fm.stringWidth(txt)/2, h/2+fm.getAscent()/2-1);
        }
        g2.dispose();
    }
}