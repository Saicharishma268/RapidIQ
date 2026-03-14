import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;

public class SignupPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField, confirmField;

    public SignupPage() {
        setTitle("RapidIQ - Sign Up");
        setSize(1090,680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); setResizable(false);
        JPanel main = new JPanel(new GridLayout(1,2));
        main.add(AppConstants.buildLoginLeft("RapidIQ","Join Us Today!"));
        main.add(buildRight());
        setContentPane(main); setVisible(true);
    }

    private JPanel buildRight() {
        JPanel right = new JPanel(new GridBagLayout()); right.setBackground(Color.WHITE);
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(30,50,30,50));

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Arial",Font.BOLD,26)); title.setForeground(AppConstants.GREEN); title.setAlignmentX(0.5f);
        form.add(title); form.add(Box.createVerticalStrut(28));

        usernameField = new JTextField();
        form.add(AppConstants.titledField("Username", usernameField)); form.add(Box.createVerticalStrut(14));
        passwordField = new JPasswordField();
        form.add(AppConstants.titledField("Password", passwordField)); form.add(Box.createVerticalStrut(14));
        confirmField = new JPasswordField();
        form.add(AppConstants.titledField("Confirm Password", confirmField)); form.add(Box.createVerticalStrut(10));

        JLabel note = new JLabel("* Account created with User role");
        note.setFont(new Font("Arial",Font.ITALIC,12)); note.setForeground(Color.GRAY); note.setAlignmentX(0.5f);
        form.add(note); form.add(Box.createVerticalStrut(20));

        JButton btn = AppConstants.greenBtn("SIGN UP");
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,48)); btn.addActionListener(e -> doSignup()); form.add(btn);
        form.add(Box.createVerticalStrut(14));

        JLabel back = new JLabel("<html><a href=''>Already have an account? Login here</a></html>");
        back.setFont(AppConstants.F_SMALL); back.setForeground(new Color(0,0,200)); back.setAlignmentX(0.5f);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter(){ @Override public void mouseClicked(MouseEvent e){ new LoginPage(); dispose(); }});
        form.add(back); right.add(form); return right;
    }

    private void doSignup() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        String conf = new String(confirmField.getPassword()).trim();
        if (user.isEmpty()||pass.isEmpty()||conf.isEmpty()) { AppConstants.msg(this,"All fields required.","Error",JOptionPane.WARNING_MESSAGE); return; }
        if (user.length()<3) { AppConstants.msg(this,"Username ≥ 3 chars.","Error",JOptionPane.WARNING_MESSAGE); return; }
        if (pass.length()<4) { AppConstants.msg(this,"Password ≥ 4 chars.","Error",JOptionPane.WARNING_MESSAGE); return; }
        if (!pass.equals(conf)) { AppConstants.msg(this,"Passwords don't match.","Error",JOptionPane.WARNING_MESSAGE); return; }
        if (!AppConstants.loadDriver(this)) return;
        try (Connection c = AppConstants.getConn()) {
            PreparedStatement chk = c.prepareStatement("SELECT id FROM users WHERE username=?");
            chk.setString(1,user);
            if (chk.executeQuery().next()) { AppConstants.msg(this,"Username taken.","Error",JOptionPane.WARNING_MESSAGE); return; }
            PreparedStatement ins = c.prepareStatement("INSERT INTO users(username,password,role) VALUES(?,?,'user')");
            ins.setString(1,user); ins.setString(2,pass); ins.executeUpdate();
            AppConstants.msg(this,"Account created! You can now login.","Success",JOptionPane.INFORMATION_MESSAGE);
            new LoginPage(); dispose();
        } catch (SQLException ex) { AppConstants.msg(this,"DB Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); }
    }
}