import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.SwingConstants;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminDashboard extends JFrame {
    private final int adminId;
    private final String adminName;
    private JPanel content;
    private CardLayout cards;

    public AdminDashboard(int adminId, String adminName) {
        this.adminId=adminId; this.adminName=adminName;
        setTitle("RapidIQ \u2013 Admin");
        setSize(1200,760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        cards = new CardLayout();
        content = new JPanel(cards);
        // Add placeholder panels with names - replaced fresh when clicked
        JPanel ph1=new JPanel(); ph1.setName("home");      content.add(ph1,"home");
        JPanel ph2=new JPanel(); ph2.setName("topics");    content.add(ph2,"topics");
        JPanel ph3=new JPanel(); ph3.setName("questions"); content.add(ph3,"questions");
        JPanel ph4=new JPanel(); ph4.setName("perf");      content.add(ph4,"perf");
        JPanel ph5=new JPanel(); ph5.setName("analytics"); content.add(ph5,"analytics");
        add(content, BorderLayout.CENTER);
        replaceCard("home");
        setVisible(true);
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────
    private final List<JButton> sidebtns = new ArrayList<>();
    private JPanel buildSidebar() {
        JPanel s = new JPanel(); s.setLayout(new BoxLayout(s,BoxLayout.Y_AXIS));
        s.setBackground(AppConstants.SIDEBAR_BG); s.setPreferredSize(new Dimension(210,0));
        s.setBorder(new EmptyBorder(20,0,20,0));

        JLabel logo = new JLabel("RapidIQ"); logo.setFont(new Font("Georgia",Font.BOLD,22));
        logo.setForeground(AppConstants.GREEN); logo.setAlignmentX(0.5f);
        JLabel roleLbl = new JLabel("ADMIN PORTAL"); roleLbl.setFont(new Font("Arial",Font.BOLD,10));
        roleLbl.setForeground(new Color(150,200,160)); roleLbl.setAlignmentX(0.5f);
        s.add(logo); s.add(Box.createVerticalStrut(4)); s.add(roleLbl);
        s.add(Box.createVerticalStrut(18));
        JSeparator sep=new JSeparator(); sep.setForeground(new Color(50,60,70)); sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        s.add(sep); s.add(Box.createVerticalStrut(10));

        addSideBtn(s,"🏠  Home",            "home");
        addSideBtn(s,"📚  Topics & Levels",  "topics");
        addSideBtn(s,"❓  Questions",         "questions");
        addSideBtn(s,"📊  Performance",       "perf");
        addSideBtn(s,"📈  Analytics",         "analytics");

        s.add(Box.createVerticalGlue());
        JLabel ul=new JLabel("👤 "+adminName); ul.setFont(AppConstants.F_SMALL);
        ul.setForeground(new Color(180,190,200)); ul.setAlignmentX(0.5f); s.add(ul);
        s.add(Box.createVerticalStrut(8));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(AppConstants.F_SMALL); logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(AppConstants.ACCENT_RED); logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false); logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(0.5f); logoutBtn.setMaximumSize(new Dimension(150,32));
        logoutBtn.addActionListener(e->{ new LoginPage(); dispose(); });
        s.add(logoutBtn);
        return s;
    }

    private void addSideBtn(JPanel s, String label, String card) {
        JButton b = new JButton(label); b.setFont(AppConstants.F_BODY);
        b.setForeground(new Color(180,190,205)); b.setBackground(AppConstants.SIDEBAR_BG);
        b.setBorderPainted(false); b.setFocusPainted(false); b.setContentAreaFilled(true);
        b.setHorizontalAlignment(SwingConstants.LEFT); b.setBorder(new EmptyBorder(11,22,11,10));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE,46)); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e->{ 
            highlight(b);
            replaceCard(card);
        });
        sidebtns.add(b); s.add(b); s.add(Box.createVerticalStrut(2));
    }

    private void highlight(JButton active) {
        for(JButton b:sidebtns){ b.setBackground(AppConstants.SIDEBAR_BG); b.setForeground(new Color(180,190,205)); }
        active.setBackground(new Color(45,60,72)); active.setForeground(Color.WHITE);
    }

    private void replaceCard(String card) {
        // Find and remove existing component with this card name
        for(java.awt.Component comp : content.getComponents()) {
            if(card.equals(comp.getName())) {
                content.remove(comp);
                break;
            }
        }
        // Build fresh panel
        JPanel newPanel;
        switch(card) {
            case "home":      newPanel = buildHome(); break;
            case "topics":    newPanel = new ManageTopicsPanel(this); break;
            case "questions": newPanel = new ManageQuestionsPanel(this); break;
            case "perf":      newPanel = buildPerfPanel(); break;
            case "analytics": newPanel = buildAnalyticsPanel(); break;
            default: return;
        }
        newPanel.setName(card);
        content.add(newPanel, card);
        cards.show(content, card);
        content.revalidate();
        content.repaint();
    }

    // ── Home ─────────────────────────────────────────────────────────────────
    private JPanel buildHome() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(AppConstants.BG); p.setBorder(new EmptyBorder(28,28,28,28));
        JLabel hdr=new JLabel("Welcome, "+adminName+"!"); hdr.setFont(AppConstants.F_TITLE); hdr.setForeground(AppConstants.TEXT_DARK);
        hdr.setBorder(new EmptyBorder(0,0,24,0)); p.add(hdr,BorderLayout.NORTH);
        JPanel cards=new JPanel(new GridLayout(1,4,18,0)); cards.setOpaque(false);
        try(Connection c=AppConstants.getConn()){
            cards.add(statCard("\uD83D\uDC65 Users",    q(c,"SELECT COUNT(*) FROM users WHERE role='user'"), AppConstants.ACCENT_BLUE));
            cards.add(statCard("\uD83D\uDCDA Topics",   q(c,"SELECT COUNT(*) FROM topics"),                  AppConstants.GREEN));
            cards.add(statCard("\u2753 Questions", q(c,"SELECT COUNT(*) FROM questions"),               AppConstants.ACCENT_GOLD));
            cards.add(statCard("\uD83D\uDCDD Attempts", q(c,"SELECT COUNT(*) FROM quiz_attempts"),            new Color(150,80,200)));
        } catch(SQLException ex){ cards.add(new JLabel("DB Error")); }
        p.add(cards,BorderLayout.CENTER);
        return p;
    }

    private JPanel statCard(String label, int val, Color color) {
        JPanel c=new JPanel(new BorderLayout()); c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220),1,true), new EmptyBorder(22,22,22,22)));
        JLabel vl=new JLabel(String.valueOf(val)); vl.setFont(new Font("Arial",Font.BOLD,38)); vl.setForeground(color);
        JLabel ll=new JLabel(label); ll.setFont(AppConstants.F_BODY); ll.setForeground(Color.GRAY);
        c.add(vl,BorderLayout.CENTER); c.add(ll,BorderLayout.SOUTH);
        return c;
    }

    private int q(Connection c,String sql) throws SQLException {
        ResultSet rs=c.createStatement().executeQuery(sql); return rs.next()?rs.getInt(1):0;
    }

    // ── Performance panel ─────────────────────────────────────────────────────
    private JPanel buildPerfPanel() {
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(AppConstants.BG); p.setBorder(new EmptyBorder(20,20,20,20));
        JLabel hdr=new JLabel("User Performance"); hdr.setFont(AppConstants.F_H1); hdr.setForeground(AppConstants.TEXT_DARK); hdr.setBorder(new EmptyBorder(0,0,14,0));
        p.add(hdr,BorderLayout.NORTH);
        DefaultTableModel m=new DefaultTableModel(new String[]{"Username","Topic","Level","Score","Total","Percentage","Time"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        try(Connection c=AppConstants.getConn()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT u.username,t.topic_name,l.level_name,qa.score,qa.total_questions,qa.percentage,qa.attempt_time " +
                "FROM quiz_attempts qa JOIN users u ON qa.user_id=u.id JOIN topics t ON qa.topic_id=t.topic_id JOIN levels l ON t.level_id=l.level_id ORDER BY qa.attempt_time DESC");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),
                rs.getInt(4),rs.getInt(5),String.format("%.1f%%",rs.getFloat(6)),rs.getTimestamp(7)});
        }catch(SQLException ex){m.addRow(new Object[]{"DB Error","","","","","",""});}
        JTable t=new JTable(m); styleTable(t); t.setAutoCreateRowSorter(true);
        p.add(new JScrollPane(t),BorderLayout.CENTER);
        return p;
    }

    // ── Analytics panel ───────────────────────────────────────────────────────
    private JPanel buildAnalyticsPanel() {
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(AppConstants.BG); p.setBorder(new EmptyBorder(20,20,20,20));
        JLabel hdr=new JLabel("Analytics"); hdr.setFont(AppConstants.F_H1); hdr.setForeground(AppConstants.TEXT_DARK); hdr.setBorder(new EmptyBorder(0,0,14,0));
        p.add(hdr,BorderLayout.NORTH);
        JPanel grid=new JPanel(new GridLayout(2,2,16,16)); grid.setOpaque(false);
        grid.add(monthlyChart()); grid.add(topUsersTable()); grid.add(topicAttemptsChart()); grid.add(levelPassRate());
        p.add(grid,BorderLayout.CENTER);
        return p;
    }

    private JPanel monthlyChart() {
        int[] months=new int[6]; String[] labels=new String[6];
        try(Connection c=AppConstants.getConn()){
            Calendar cal=Calendar.getInstance();
            for(int i=5;i>=0;i--){
                Calendar m=(Calendar)cal.clone(); m.add(Calendar.MONTH,-i); int idx=5-i;
                labels[idx]=new java.text.SimpleDateFormat("MMM yy").format(m.getTime());
                PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM quiz_attempts WHERE YEAR(attempt_time)=? AND MONTH(attempt_time)=?");
                ps.setInt(1,m.get(Calendar.YEAR)); ps.setInt(2,m.get(Calendar.MONTH)+1);
                ResultSet rs=ps.executeQuery(); if(rs.next()) months[idx]=rs.getInt(1);
            }
        }catch(Exception ignored){}
        return new BarChartPanel("Quiz Attempts – Last 6 Months", labels, months, AppConstants.GREEN);
    }

    private JPanel topicsChart(int[] vals, String[] lbls) {
        return new BarChartPanel("Topic Attempts", lbls, vals, AppConstants.ACCENT_BLUE);
    }

    private JPanel topicAttemptsChart() {
        List<String> lbls=new ArrayList<>(); List<Integer> vals=new ArrayList<>();
        try(Connection c=AppConstants.getConn()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT t.topic_name,COUNT(*) as cnt FROM quiz_attempts qa JOIN topics t ON qa.topic_id=t.topic_id GROUP BY qa.topic_id ORDER BY cnt DESC LIMIT 6");
            while(rs.next()){lbls.add(rs.getString(1)); vals.add(rs.getInt(2));}
        }catch(Exception ignored){}
        int[] v=vals.stream().mapToInt(Integer::intValue).toArray();
        return new BarChartPanel("Attempts per Topic", lbls.toArray(new String[0]), v, AppConstants.ACCENT_BLUE);
    }

    private JPanel levelPassRate() {
        List<String> lbls=new ArrayList<>(); List<Integer> vals=new ArrayList<>();
        try(Connection c=AppConstants.getConn()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT l.level_name, ROUND(AVG(qa.percentage)) as avg_pct FROM quiz_attempts qa JOIN topics t ON qa.topic_id=t.topic_id JOIN levels l ON t.level_id=l.level_id GROUP BY l.level_id ORDER BY l.level_id");
            while(rs.next()){lbls.add(rs.getString(1)); vals.add(rs.getInt(2));}
        }catch(Exception ignored){}
        int[] v=vals.stream().mapToInt(Integer::intValue).toArray();
        return new BarChartPanel("Avg Score by Level (%)", lbls.toArray(new String[0]), v, AppConstants.ACCENT_GOLD);
    }

    private JPanel topUsersTable() {
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220),1,true),new EmptyBorder(12,12,12,12)));
        JLabel t=new JLabel("\uD83C\uDFC6 Top 5 Users"); t.setFont(AppConstants.F_H2); t.setBorder(new EmptyBorder(0,0,8,0)); p.add(t,BorderLayout.NORTH);
        DefaultTableModel m=new DefaultTableModel(new String[]{"#","Username","Total Score","Attempts"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=AppConstants.getConn()){
            // Sum only the BEST raw score (correct answers) per topic per user.
            // 1 correct answer = 1 point, max 5 points per topic.
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT u.username, SUM(best.max_score) AS ts, COUNT(DISTINCT best.topic_id) AS cnt " +
                "FROM users u " +
                "JOIN ( " +
                "    SELECT user_id, topic_id, MAX(score) AS max_score " +
                "    FROM quiz_attempts " +
                "    GROUP BY user_id, topic_id " +
                ") best ON best.user_id = u.id " +
                "GROUP BY u.id " +
                "ORDER BY ts DESC " +
                "LIMIT 5");
            int rk=1; while(rs.next()) m.addRow(new Object[]{rk++,rs.getString(1),rs.getInt(2),rs.getInt(3)});
        }catch(SQLException ignored){}
        JTable tbl=new JTable(m); styleTable(tbl); p.add(new JScrollPane(tbl),BorderLayout.CENTER);
        return p;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(34);
        t.setFont(AppConstants.F_BODY);
        t.setForeground(Color.BLACK);
        t.setBackground(Color.WHITE);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(220, 225, 235));
        t.setSelectionBackground(new Color(25, 120, 70));
        t.setSelectionForeground(Color.WHITE);
        t.getTableHeader().setPreferredSize(new Dimension(0, 42));
        t.getTableHeader().setBackground(new Color(30, 42, 58));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        t.getTableHeader().setOpaque(true);
        // Per-column header renderer
        for (int i = 0; i < t.getColumnModel().getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setHeaderRenderer(new javax.swing.table.TableCellRenderer() {
                @Override public java.awt.Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                    JLabel lbl = new JLabel(value == null ? "" : value.toString());
                    lbl.setFont(new Font("Arial", Font.BOLD, 13));
                    lbl.setForeground(Color.WHITE);
                    lbl.setBackground(new Color(30, 42, 58));
                    lbl.setOpaque(true);
                    lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
                    lbl.setHorizontalAlignment(JLabel.LEFT);
                    return lbl;
                }
            });
        }
        // Row renderer
        t.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (isSelected) {
                    setBackground(new Color(25, 120, 70));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 247, 252));
                    setForeground(Color.BLACK);
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        });
    }
}

// ── Bar chart component ──────────────────────────────────────────────────────
class BarChartPanel extends JPanel {
    private final String title; private final String[] labels; private final int[] values; private final Color barColor;
    BarChartPanel(String title, String[] labels, int[] values, Color barColor) {
        this.title=title; this.labels=labels; this.values=values; this.barColor=barColor;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220),1,true),new EmptyBorder(12,12,12,12)));
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g); if(labels==null||labels.length==0) return;
        Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("Arial",Font.BOLD,12)); g2.setColor(AppConstants.TEXT_DARK); g2.drawString(title,10,20);
        int maxVal=1; for(int v:values) if(v>maxVal) maxVal=v;
        int n=labels.length; int pad=30,bw,gap=8,chartH=getHeight()-70,chartW=getWidth()-60;
        bw=Math.max(10,(chartW-(n-1)*gap)/n);
        for(int i=0;i<n;i++){
            int bh=(int)((double)values[i]/maxVal*chartH);
            int x=pad+i*(bw+gap); int y=getHeight()-40-bh;
            g2.setColor(barColor); g2.fillRoundRect(x,y,bw,bh,6,6);
            g2.setFont(new Font("Arial",Font.PLAIN,10)); g2.setColor(AppConstants.TEXT_DARK);
            String lbl=labels[i].length()>7?labels[i].substring(0,6)+"…":labels[i];
            g2.drawString(lbl,x,getHeight()-22);
            g2.setColor(AppConstants.TEXT_GRAY); g2.drawString(String.valueOf(values[i]),x+bw/2-6,y-4);
        }
    }
}

// ── Manage Topics Panel ───────────────────────────────────────────────────────
class ManageTopicsPanel extends JPanel {
    private final JFrame parent; private JTable table; private DefaultTableModel model;
    private JComboBox<String> levelBox; private JTextField topicField;
    ManageTopicsPanel(JFrame parent) {
        this.parent=parent; setLayout(new BorderLayout()); setBackground(AppConstants.BG); setBorder(new EmptyBorder(20,20,20,20));
        JLabel hdr=new JLabel("Topics & Levels"); hdr.setFont(AppConstants.F_H1); hdr.setForeground(AppConstants.TEXT_DARK); hdr.setBorder(new EmptyBorder(0,0,14,0));
        add(hdr,BorderLayout.NORTH);

        JPanel left=new JPanel(); left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
        left.setBackground(Color.WHITE); left.setPreferredSize(new Dimension(280,0));
        left.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220),1,true),new EmptyBorder(18,18,18,18)));

        JLabel fhdr=new JLabel("Add Topic"); fhdr.setFont(AppConstants.F_H2); fhdr.setAlignmentX(0); left.add(fhdr); left.add(Box.createVerticalStrut(12));
        topicField=new JTextField(); left.add(AppConstants.titledField("Topic Name",topicField)); left.add(Box.createVerticalStrut(10));

        levelBox=new JComboBox<>(); levelBox.setFont(AppConstants.F_BODY);
        levelBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,50)); levelBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        levelBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200,200,200)),"Level",
            TitledBorder.LEFT,TitledBorder.TOP,AppConstants.F_SMALL,AppConstants.TEXT_GRAY));
        loadLevels(); left.add(levelBox); left.add(Box.createVerticalStrut(16));

        JButton addBtn=AppConstants.greenBtn("Add Topic"); addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40)); addBtn.addActionListener(e->addTopic()); left.add(addBtn); left.add(Box.createVerticalStrut(8));
        JButton delBtn=AppConstants.redBtn("Delete Selected"); delBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40)); delBtn.addActionListener(e->deleteTopic()); left.add(delBtn); left.add(Box.createVerticalGlue());

        model=new DefaultTableModel(new String[]{"ID","Topic","Level"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(model); styleTable(table); JScrollPane sc=new JScrollPane(table); sc.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        JPanel center=new JPanel(new BorderLayout()); center.setOpaque(false); center.setBorder(new EmptyBorder(0,14,0,0)); center.add(sc);
        add(left,BorderLayout.WEST); add(center,BorderLayout.CENTER); loadTopics();
    }
    private void loadLevels(){ levelBox.removeAllItems();
        try(Connection c=AppConstants.getConn()){ ResultSet rs=c.createStatement().executeQuery("SELECT level_id,level_name FROM levels ORDER BY level_id"); while(rs.next()) levelBox.addItem(rs.getInt(1)+" - "+rs.getString(2)); }catch(SQLException ex){ AppConstants.msg(parent,"DB: "+ex.getMessage(),"Error",0); }}
    private void loadTopics(){ model.setRowCount(0);
        try(Connection c=AppConstants.getConn()){ ResultSet rs=c.createStatement().executeQuery("SELECT t.topic_id,t.topic_name,l.level_name FROM topics t JOIN levels l ON t.level_id=l.level_id ORDER BY l.level_id,t.topic_name"); while(rs.next()) model.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3)}); }catch(SQLException ex){ AppConstants.msg(parent,"DB: "+ex.getMessage(),"Error",0); }}
    private void addTopic(){ String name=topicField.getText().trim(); if(name.isEmpty()||levelBox.getSelectedItem()==null){AppConstants.msg(parent,"Fill topic name and select level.","Error",JOptionPane.WARNING_MESSAGE);return;}
        int lid=Integer.parseInt(levelBox.getSelectedItem().toString().split(" - ")[0]);
        try(Connection c=AppConstants.getConn()){ PreparedStatement ps=c.prepareStatement("INSERT INTO topics(level_id,topic_name) VALUES(?,?)"); ps.setInt(1,lid); ps.setString(2,name); ps.executeUpdate(); topicField.setText(""); loadTopics(); AppConstants.msg(parent,"Topic added!","Success",JOptionPane.INFORMATION_MESSAGE); }catch(SQLException ex){ AppConstants.msg(parent,"DB: "+ex.getMessage(),"Error",0); }}
    private void deleteTopic(){ int row=table.getSelectedRow(); if(row<0){AppConstants.msg(parent,"Select a topic.","Error",JOptionPane.WARNING_MESSAGE);return;}
        int id=(int)model.getValueAt(row,0); int ok=JOptionPane.showConfirmDialog(parent,"Delete topic and all its questions?","Confirm",JOptionPane.YES_NO_OPTION); if(ok!=JOptionPane.YES_OPTION) return;
        try(Connection c=AppConstants.getConn()){ c.createStatement().executeUpdate("DELETE FROM questions WHERE topic_id="+id); c.createStatement().executeUpdate("DELETE FROM topics WHERE topic_id="+id); loadTopics(); }catch(SQLException ex){ AppConstants.msg(parent,"DB: "+ex.getMessage(),"Error",0); }}
    private void styleTable(JTable t){
        t.setRowHeight(34); t.setFont(AppConstants.F_BODY);
        t.setForeground(Color.BLACK); t.setBackground(Color.WHITE);
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setGridColor(new Color(220,225,235));
        t.setSelectionBackground(new Color(25,120,70));
        t.setSelectionForeground(Color.WHITE);
        t.getTableHeader().setPreferredSize(new Dimension(0,42));
        t.getTableHeader().setBackground(new Color(30,42,58));
        t.getTableHeader().setFont(new Font("Arial",Font.BOLD,13));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setOpaque(true);
        for (int i=0;i<t.getColumnModel().getColumnCount();i++){
            t.getColumnModel().getColumn(i).setHeaderRenderer(new javax.swing.table.TableCellRenderer(){
                @Override public java.awt.Component getTableCellRendererComponent(
                        JTable tbl,Object val,boolean isSel,boolean hasFocus,int row,int col){
                    JLabel lbl=new JLabel(val==null?"":val.toString());
                    lbl.setFont(new Font("Arial",Font.BOLD,13));
                    lbl.setForeground(Color.WHITE);
                    lbl.setBackground(new Color(30,42,58));
                    lbl.setOpaque(true);
                    lbl.setBorder(new EmptyBorder(0,12,0,12));
                    lbl.setHorizontalAlignment(JLabel.LEFT);
                    return lbl;
                }
            });
        }
        t.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer(){
            @Override public java.awt.Component getTableCellRendererComponent(
                    JTable tbl,Object val,boolean isSel,boolean hasFocus,int row,int col){
                super.getTableCellRendererComponent(tbl,val,isSel,hasFocus,row,col);
                if(isSel){setBackground(new Color(25,120,70));setForeground(Color.WHITE);}
                else{setBackground(row%2==0?Color.WHITE:new Color(245,247,252));setForeground(Color.BLACK);}
                setBorder(new EmptyBorder(0,12,0,12)); return this;
            }
        });
    }
}

// ── Manage Questions Panel ────────────────────────────────────────────────────
class ManageQuestionsPanel extends JPanel {
    private final JFrame parent;
    private JComboBox<TopicItem> topicBox;
    private JTextField qField,o1,o2,o3,o4;
    private JComboBox<String> correctBox;
    private JTable table;
    private DefaultTableModel model;

    ManageQuestionsPanel(JFrame parent) {
        this.parent=parent;
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG);
        setBorder(new EmptyBorder(20,20,20,20));

        // Header with refresh button
        JPanel hdrPanel = new JPanel(new BorderLayout());
        hdrPanel.setOpaque(false);
        hdrPanel.setBorder(new EmptyBorder(0,0,14,0));
        JLabel hdr=new JLabel("Manage Questions");
        hdr.setFont(AppConstants.F_H1);
        hdr.setForeground(AppConstants.TEXT_DARK);
        JButton refreshBtn = AppConstants.blueBtn("\u21BB  Refresh Topics");
        refreshBtn.setPreferredSize(new Dimension(160,36));
        refreshBtn.addActionListener(e -> { loadTopics(); loadQ(); });
        hdrPanel.add(hdr, BorderLayout.WEST);
        hdrPanel.add(refreshBtn, BorderLayout.EAST);
        add(hdrPanel, BorderLayout.NORTH);

        JPanel form=new JPanel();
        form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setPreferredSize(new Dimension(310,0));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220),1,true),
            new EmptyBorder(14,14,14,14)));

        JLabel fhdr=new JLabel("Add Question (max 5/topic)");
        fhdr.setFont(AppConstants.F_H2); fhdr.setAlignmentX(0);
        form.add(fhdr); form.add(Box.createVerticalStrut(10));

        // Topic combobox - NO action listener yet, added after model is initialized
        topicBox = new JComboBox<>();
        topicBox.setFont(AppConstants.F_BODY);
        topicBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        topicBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        topicBox.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)),"Topic",
            TitledBorder.LEFT,TitledBorder.TOP,AppConstants.F_SMALL,AppConstants.TEXT_GRAY));
        form.add(topicBox); form.add(Box.createVerticalStrut(8));

        qField=new JTextField(); form.add(AppConstants.titledField("Question",qField)); form.add(Box.createVerticalStrut(5));
        o1=new JTextField(); form.add(AppConstants.titledField("Option 1",o1)); form.add(Box.createVerticalStrut(5));
        o2=new JTextField(); form.add(AppConstants.titledField("Option 2",o2)); form.add(Box.createVerticalStrut(5));
        o3=new JTextField(); form.add(AppConstants.titledField("Option 3",o3)); form.add(Box.createVerticalStrut(5));
        o4=new JTextField(); form.add(AppConstants.titledField("Option 4",o4)); form.add(Box.createVerticalStrut(7));

        correctBox=new JComboBox<>(new String[]{"1","2","3","4"});
        correctBox.setFont(AppConstants.F_BODY);
        correctBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        correctBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        correctBox.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)),"Correct Option",
            TitledBorder.LEFT,TitledBorder.TOP,AppConstants.F_SMALL,AppConstants.TEXT_GRAY));
        form.add(correctBox); form.add(Box.createVerticalStrut(10));

        JButton addBtn=AppConstants.greenBtn("Add Question");
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        addBtn.addActionListener(e->addQ());
        form.add(addBtn); form.add(Box.createVerticalStrut(7));

        JButton delBtn=AppConstants.redBtn("Delete Selected");
        delBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        delBtn.addActionListener(e->delQ());
        form.add(delBtn);

        model=new DefaultTableModel(new String[]{"ID","Question","Opt1","Opt2","Opt3","Opt4","Ans"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table=new JTable(model);
        table.setRowHeight(32); table.setFont(AppConstants.F_BODY);
        table.setForeground(Color.BLACK); table.setBackground(Color.WHITE);
        table.setShowHorizontalLines(true); table.setShowVerticalLines(false);
        table.setGridColor(new Color(220,225,235));
        table.setSelectionBackground(new Color(25,120,70));
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0,42));
        table.getTableHeader().setBackground(new Color(30,42,58));
        table.getTableHeader().setFont(new Font("Arial",Font.BOLD,13));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setOpaque(true);
        for (int i=0;i<model.getColumnCount();i++){
            table.getColumnModel().getColumn(i).setHeaderRenderer(new javax.swing.table.TableCellRenderer(){
                @Override public java.awt.Component getTableCellRendererComponent(
                        JTable tbl,Object val,boolean isSel,boolean hasFocus,int row,int col){
                    JLabel lbl=new JLabel(val==null?"":val.toString());
                    lbl.setFont(new Font("Arial",Font.BOLD,13));
                    lbl.setForeground(Color.WHITE);
                    lbl.setBackground(new Color(30,42,58));
                    lbl.setOpaque(true);
                    lbl.setBorder(new EmptyBorder(0,12,0,12));
                    lbl.setHorizontalAlignment(JLabel.LEFT);
                    return lbl;
                }
            });
        }
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer(){
            @Override public java.awt.Component getTableCellRendererComponent(
                    JTable tbl,Object val,boolean isSel,boolean hasFocus,int row,int col){
                super.getTableCellRendererComponent(tbl,val,isSel,hasFocus,row,col);
                if(isSel){setBackground(new Color(25,120,70));setForeground(Color.WHITE);}
                else{setBackground(row%2==0?Color.WHITE:new Color(245,247,252));setForeground(Color.BLACK);}
                setBorder(new EmptyBorder(0,12,0,12)); return this;
            }
        });

        // NOW safe to add listener and load topics - model is ready
        topicBox.addActionListener(e -> loadQ());
        loadTopics();

        JScrollPane sc=new JScrollPane(table);
        JPanel center=new JPanel(new BorderLayout());
        center.setOpaque(false); center.setBorder(new EmptyBorder(0,14,0,0)); center.add(sc);
        add(form,BorderLayout.WEST); add(center,BorderLayout.CENTER);
    }

    private void loadTopics() {
        topicBox.removeAllItems();
        try(Connection c=AppConstants.getConn()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT t.topic_id,t.topic_name,l.level_name FROM topics t " +
                "JOIN levels l ON t.level_id=l.level_id ORDER BY l.level_id,t.topic_name");
            while(rs.next())
                topicBox.addItem(new TopicItem(rs.getInt(1), rs.getString(2)+" ["+rs.getString(3)+"]"));
        } catch(SQLException ex){ AppConstants.msg(parent,"DB: "+ex.getMessage(),"Error",0); }
    }

    private int getSelectedTopicId() {
        TopicItem item = (TopicItem) topicBox.getSelectedItem();
        return item == null ? -1 : item.id;
    }

    private void loadQ() {
        model.setRowCount(0);
        int tid = getSelectedTopicId();
        if(tid < 0) return;
        try(Connection c=AppConstants.getConn()){
            PreparedStatement ps=c.prepareStatement(
                "SELECT question_id, question, option1, option2, option3, option4, correct_option " +
                "FROM questions WHERE topic_id=?");
            ps.setInt(1,tid); ResultSet rs=ps.executeQuery();
            while(rs.next()) model.addRow(new Object[]{
                rs.getInt("question_id"), rs.getString("question"),
                rs.getString("option1"),  rs.getString("option2"),
                rs.getString("option3"),  rs.getString("option4"),
                rs.getInt("correct_option")});
        } catch(SQLException ex){ AppConstants.msg(parent,"DB: "+ex.getMessage(),"Error",0); }
    }

    private void addQ() {
        int tid = getSelectedTopicId();
        if(tid < 0){ AppConstants.msg(parent,"Select a topic first.","Error",JOptionPane.WARNING_MESSAGE); return; }
        try(Connection c=AppConstants.getConn()){
            PreparedStatement chk=c.prepareStatement("SELECT COUNT(*) FROM questions WHERE topic_id=?");
            chk.setInt(1,tid); ResultSet rc=chk.executeQuery();
            if(rc.next()&&rc.getInt(1)>=5){
                AppConstants.msg(parent,"This topic already has 5 questions (max limit).","Limit",JOptionPane.WARNING_MESSAGE); return;
            }
            String q=qField.getText().trim(), a=o1.getText().trim(), b=o2.getText().trim(),
                   d=o3.getText().trim(), e2=o4.getText().trim();
            if(q.isEmpty()||a.isEmpty()||b.isEmpty()||d.isEmpty()||e2.isEmpty()){
                AppConstants.msg(parent,"Please fill all fields.","Error",JOptionPane.WARNING_MESSAGE); return;
            }
            int cor=Integer.parseInt((String)correctBox.getSelectedItem());
            PreparedStatement ps=c.prepareStatement(
                "INSERT INTO questions(topic_id,question,option1,option2,option3,option4,correct_option) VALUES(?,?,?,?,?,?,?)");
            ps.setInt(1,tid); ps.setString(2,q); ps.setString(3,a); ps.setString(4,b);
            ps.setString(5,d); ps.setString(6,e2); ps.setInt(7,cor);
            ps.executeUpdate();
            qField.setText(""); o1.setText(""); o2.setText(""); o3.setText(""); o4.setText("");
            loadQ();
            AppConstants.msg(parent,"Question added successfully!","Success",JOptionPane.INFORMATION_MESSAGE);
        } catch(SQLException ex){ AppConstants.msg(parent,"DB: "+ex.getMessage(),"Error",0); }
    }

    private void delQ() {
        int row=table.getSelectedRow();
        if(row<0){ AppConstants.msg(parent,"Select a question to delete.","Error",JOptionPane.WARNING_MESSAGE); return; }
        int id=(int)model.getValueAt(row,0);
        try(Connection c=AppConstants.getConn()){
            c.createStatement().executeUpdate("DELETE FROM questions WHERE question_id="+id);
            loadQ();
        } catch(SQLException ex){ AppConstants.msg(parent,"DB: "+ex.getMessage(),"Error",0); }
    }

    // Inner class to hold topic id + display name separately
    static class TopicItem {
        int id; String label;
        TopicItem(int id, String label){ this.id=id; this.label=label; }
        @Override public String toString(){ return label; }
    }
}