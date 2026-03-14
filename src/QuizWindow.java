import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class QuizWindow extends JFrame {
    private final int userId;
    private final String username;
    private final int topicId;
    private final String topicName;
    private final String levelName;
    private final JFrame owner;

    private List<Question> questions = new ArrayList<>();
    private int current = 0;
    private int score   = 0;
    private int timeLeft;
    private javax.swing.Timer countdownTimer;

    // UI components
    private JLabel   qNumLbl, qTextLbl, timerLbl;
    private JButton[] optBtns = new JButton[4];
    private JButton  checkBtn, nextBtn;
    private JPanel   optPanel;
    private int selectedOpt = -1;
    private boolean answered = false;
    private JPanel timerPanel;

    public QuizWindow(JFrame owner, int userId, String username,
                      int topicId, String topicName, String levelName) {
        super(topicName + " Quiz");
        this.owner=owner; this.userId=userId; this.username=username;
        this.topicId=topicId; this.topicName=topicName; this.levelName=levelName;
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loadQuestions();
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(null,"No questions found for this topic.\nPlease add questions via Admin panel.","No Questions",JOptionPane.WARNING_MESSAGE);
            return;
        }
        buildUI();
        showQuestion();
        setVisible(true);
    }

    private void loadQuestions() {
        try (Connection c = AppConstants.getConn()) {
            PreparedStatement ps = c.prepareStatement(
                "SELECT question_id,question,option1,option2,option3,option4,correct_option FROM questions WHERE topic_id=?");
            ps.setInt(1, topicId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) questions.add(new Question(
                rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getString(6),rs.getInt(7)));
            Collections.shuffle(questions);
        } catch (SQLException ignored) {}
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppConstants.BG);

        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(12,0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(14,24,14,24));

        qNumLbl = new JLabel("Question 1 / " + questions.size());
        qNumLbl.setFont(AppConstants.F_H2); qNumLbl.setForeground(AppConstants.TEXT_GRAY);

        JLabel topicLbl = new JLabel(topicName + "  [" + levelName + "]");
        topicLbl.setFont(new Font("Arial",Font.BOLD,15)); topicLbl.setForeground(AppConstants.TEXT_DARK);

        timerPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                float ratio = (float)timeLeft/AppConstants.timerForLevel(levelName);
                Color c = ratio>0.5f?AppConstants.GREEN:ratio>0.25f?AppConstants.ACCENT_GOLD:AppConstants.ACCENT_RED;
                g2.setColor(new Color(230,230,230)); g2.fillOval(0,0,48,48);
                g2.setColor(c); g2.fillArc(0,0,48,48,90,(int)(-360*ratio));
                g2.setColor(Color.WHITE); g2.fillOval(8,8,32,32);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        timerPanel.setOpaque(false); timerPanel.setPreferredSize(new Dimension(48,48));
        timerPanel.setLayout(new GridBagLayout());

        timerLbl = new JLabel("30");
        timerLbl.setFont(new Font("Arial",Font.BOLD,14)); timerLbl.setForeground(AppConstants.TEXT_DARK);
        timerPanel.add(timerLbl);

        topBar.add(topicLbl, BorderLayout.WEST);
        topBar.add(qNumLbl,  BorderLayout.CENTER);
        topBar.add(timerPanel, BorderLayout.EAST);
        root.add(topBar, BorderLayout.NORTH);

        // ── Center: question + options ────────────────────────────────────────
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(AppConstants.BG);
        center.setBorder(new EmptyBorder(24,32,16,32));

        qTextLbl = new JLabel("", SwingConstants.LEFT);
        qTextLbl.setFont(new Font("Arial",Font.BOLD,17));
        qTextLbl.setForeground(AppConstants.TEXT_DARK);
        qTextLbl.setAlignmentX(0);
        qTextLbl.setBorder(new EmptyBorder(0,0,20,0));
        center.add(qTextLbl);

        optPanel = new JPanel(new GridLayout(2,2,14,14));
        optPanel.setOpaque(false);
        optPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,180));
        optPanel.setAlignmentX(0);

        for (int i=0;i<4;i++) {
            final int idx=i;
            optBtns[i] = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = (Color)getClientProperty("bg");
                    if(bg==null) bg = getModel().isRollover()?new Color(235,240,250):Color.WHITE;
                    g2.setColor(bg); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            optBtns[i].setFont(AppConstants.F_BODY);
            optBtns[i].setForeground(AppConstants.TEXT_DARK);
            optBtns[i].setContentAreaFilled(false);
            optBtns[i].setBorderPainted(true);
            optBtns[i].setBorder(BorderFactory.createLineBorder(new Color(210,215,225),1,true));
            optBtns[i].setFocusPainted(false);
            optBtns[i].setHorizontalAlignment(SwingConstants.LEFT);
            optBtns[i].setBorder(new EmptyBorder(12,16,12,16));
            optBtns[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            optBtns[i].addActionListener(e -> selectOption(idx));
            optPanel.add(optBtns[i]);
        }
        center.add(optPanel);
        root.add(center, BorderLayout.CENTER);

        // ── Bottom bar ────────────────────────────────────────────────────────
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        bot.setBackground(Color.WHITE);
        bot.setBorder(new EmptyBorder(14, 32, 18, 32));

        checkBtn = AppConstants.greenBtn("Check Answer");
        checkBtn.setPreferredSize(new Dimension(160, 42));
        checkBtn.addActionListener(e -> checkAnswer());

        nextBtn = AppConstants.blueBtn("Next \u2192");
        nextBtn.setPreferredSize(new Dimension(160, 42));
        nextBtn.setEnabled(true);
        nextBtn.addActionListener(e -> nextQuestion());

        bot.add(checkBtn);
        bot.add(nextBtn);
        root.add(bot, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void showQuestion() {
        if (current >= questions.size()) { endQuiz(); return; }
        Question q = questions.get(current);
        answered = false; selectedOpt = -1;

        qNumLbl.setText("Question " + (current+1) + " / " + questions.size());
        qTextLbl.setText("<html><body style='width:640px'>" + q.text + "</body></html>");
        optBtns[0].setText("A. " + q.opt1);
        optBtns[1].setText("B. " + q.opt2);
        optBtns[2].setText("C. " + q.opt3);
        optBtns[3].setText("D. " + q.opt4);

        for (JButton b : optBtns) {
            b.putClientProperty("bg", null);
            b.setForeground(AppConstants.TEXT_DARK);
            b.setBorder(new EmptyBorder(12,16,12,16));
            b.setEnabled(true);
            b.repaint();
        }

        // Both buttons always visible
        checkBtn.setText("Submit");
        checkBtn.setEnabled(true);
        checkBtn.setVisible(true);
        nextBtn.setText(current+1 < questions.size() ? "Next \u2192" : "Finish");
        nextBtn.setEnabled(true);

        // Start timer
        if (countdownTimer != null) countdownTimer.stop();
        timeLeft = AppConstants.timerForLevel(levelName);
        timerLbl.setText(String.valueOf(timeLeft));
        timerPanel.repaint();

        countdownTimer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            timerLbl.setText(String.valueOf(Math.max(0, timeLeft)));
            timerPanel.repaint();
            if (timeLeft <= 0) {
                countdownTimer.stop();
                if (!answered) autoNext();
            }
        });
        countdownTimer.start();
    }

    private void selectOption(int idx) {
        if (answered) return;
        selectedOpt = idx;
        for (int i=0;i<4;i++) {
            optBtns[i].putClientProperty("bg", i==idx ? new Color(220,235,255) : null);
            optBtns[i].setBorder(i==idx ?
                BorderFactory.createLineBorder(AppConstants.ACCENT_BLUE,2,true) :
                new EmptyBorder(12,16,12,16));
            optBtns[i].repaint();
        }
    }

    private void checkAnswer() {
        if (answered) return;
        if (selectedOpt < 0) {
            AppConstants.msg(this,"Please select an option first.","Info",JOptionPane.INFORMATION_MESSAGE); return;
        }
        countdownTimer.stop();
        answered = true;
        revealAnswer(selectedOpt);
    }

    private void autoNext() {
        answered = true;
        revealAnswer(-1); // show correct answer immediately, no user pick
        checkBtn.setText("Time's Up!");
        checkBtn.setEnabled(false);
        nextBtn.setEnabled(true);

        // Show answer for 1.5s then immediately go to next question
        javax.swing.Timer autoAdvance = new javax.swing.Timer(1500, e -> nextQuestion());
        autoAdvance.setRepeats(false);
        autoAdvance.start();

        // If user clicks Next manually, cancel the timer
        ActionListener[] manual = new ActionListener[1];
        manual[0] = e -> {
            autoAdvance.stop();
            nextBtn.removeActionListener(manual[0]);
            nextQuestion();
        };
        nextBtn.addActionListener(manual[0]);
    }

    private void revealAnswer(int chosen) {
        Question q = questions.get(current);
        int correct = q.correct - 1; // 0-based

        for (int i=0;i<4;i++) {
            optBtns[i].setEnabled(false);
            if (i == correct) {
                optBtns[i].putClientProperty("bg", new Color(210,245,220));
                optBtns[i].setForeground(new Color(30,120,60));
                optBtns[i].setBorder(BorderFactory.createLineBorder(AppConstants.GREEN,2,true));
            } else if (i == chosen && i != correct) {
                optBtns[i].putClientProperty("bg", new Color(255,220,220));
                optBtns[i].setForeground(new Color(160,30,30));
                optBtns[i].setBorder(BorderFactory.createLineBorder(AppConstants.ACCENT_RED,2,true));
            }
            optBtns[i].repaint();
        }
        if (chosen == correct) score++;
        checkBtn.setEnabled(false);
        nextBtn.setEnabled(true);
        nextBtn.setText(current+1 < questions.size() ? "Next \u2192" : "Finish");
    }

    private void nextQuestion() {
        current++;
        showQuestion();
    }

    private void endQuiz() {
        countdownTimer.stop();
        int total = questions.size();
        double pct = total==0 ? 0 : (double)score/total*100;

        // Save to DB
        try (Connection c = AppConstants.getConn()) {
            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO quiz_attempts(user_id,topic_id,score,total_questions,percentage) VALUES(?,?,?,?,?)");
            ps.setInt(1,userId); ps.setInt(2,topicId);
            ps.setInt(3,score); ps.setInt(4,total); ps.setDouble(5,pct);
            ps.executeUpdate();
        } catch (SQLException ignored) {}

        // Show result then refresh dashboard
        showResultDialog(score, total, pct);
        dispose();
        // Open fresh dashboard, close old one
        SwingUtilities.invokeLater(() -> {
            if (owner != null) owner.dispose();
            new UserDashboard(userId, username);
        });
    }

    private void showResultDialog(int score, int total, double pct) {
        JDialog dlg = new JDialog(this, "Quiz Result", true);
        dlg.setSize(420,320); dlg.setLocationRelativeTo(this); dlg.setResizable(false);

        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(30,40,30,40));

        JLabel trophy = new JLabel(pct>=80?"\uD83C\uDFC6":pct>=50?"\uD83D\uDC4D":"\uD83D\uDCDA");
        trophy.setFont(new Font("Segoe UI Emoji",Font.PLAIN,48)); trophy.setAlignmentX(0.5f);

        JLabel resultLbl = new JLabel("Score: "+score+" / "+total);
        resultLbl.setFont(new Font("Arial",Font.BOLD,24)); resultLbl.setForeground(AppConstants.TEXT_DARK); resultLbl.setAlignmentX(0.5f);

        JLabel pctLbl = new JLabel(String.format("%.1f%%", pct));
        pctLbl.setFont(new Font("Arial",Font.BOLD,32));
        pctLbl.setForeground(pct>=80?AppConstants.GREEN:pct>=50?AppConstants.ACCENT_GOLD:AppConstants.ACCENT_RED);
        pctLbl.setAlignmentX(0.5f);

        JLabel msgLbl = new JLabel(pct>=80?"Excellent!":pct>=50?"Good job!":"Keep practicing!");
        msgLbl.setFont(AppConstants.F_BODY); msgLbl.setForeground(AppConstants.TEXT_GRAY); msgLbl.setAlignmentX(0.5f);

        JButton ok = AppConstants.greenBtn("Back to Dashboard");
        ok.setMaximumSize(new Dimension(Integer.MAX_VALUE,44)); ok.setAlignmentX(0.5f);
        ok.addActionListener(e -> dlg.dispose());

        p.add(trophy); p.add(Box.createVerticalStrut(12));
        p.add(resultLbl); p.add(Box.createVerticalStrut(6));
        p.add(pctLbl); p.add(Box.createVerticalStrut(4));
        p.add(msgLbl); p.add(Box.createVerticalStrut(20));
        p.add(ok);
        dlg.setContentPane(p); dlg.setVisible(true);
    }

    static class Question {
        int id, correct; String text,opt1,opt2,opt3,opt4;
        Question(int id,String text,String o1,String o2,String o3,String o4,int c){
            this.id=id;this.text=text;opt1=o1;opt2=o2;opt3=o3;opt4=o4;correct=c;}
    }
}