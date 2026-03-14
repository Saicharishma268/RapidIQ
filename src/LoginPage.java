import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;

public class LoginPage extends JFrame {
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JLabel         registerLabel;

    public LoginPage() {
        setTitle("RapidIQ - Login");
        setSize(1090, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        JPanel main = new JPanel(new GridLayout(1,2));
        main.add(AppConstants.buildLoginLeft("RapidIQ","Welcome Back!"));
        main.add(buildRight());
        setContentPane(main);
        setVisible(true);
    }

    private JPanel buildRight() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(30,50,30,50));

        JLabel title = new JLabel("Login Account");
        title.setFont(new Font("Arial",Font.BOLD,26));
        title.setForeground(AppConstants.GREEN);
        title.setAlignmentX(0.5f);
        form.add(title); form.add(Box.createVerticalStrut(28));

        usernameField = new JTextField();
        form.add(AppConstants.titledField("Username", usernameField));
        form.add(Box.createVerticalStrut(14));

        passwordField = new JPasswordField();
        form.add(AppConstants.titledField("Password", passwordField));
        form.add(Box.createVerticalStrut(14));

        roleBox = new JComboBox<>(new String[]{"User","Admin"});
        roleBox.setFont(AppConstants.F_BODY);
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        roleBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleBox.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)),"Role",
            TitledBorder.LEFT,TitledBorder.TOP,AppConstants.F_SMALL,AppConstants.TEXT_GRAY));
        JPanel rw = new JPanel(); rw.setLayout(new BoxLayout(rw,BoxLayout.Y_AXIS));
        rw.setOpaque(false); rw.setMaximumSize(new Dimension(Integer.MAX_VALUE,60));
        rw.setAlignmentX(0.5f); roleBox.setAlignmentX(Component.LEFT_ALIGNMENT); rw.add(roleBox);
        form.add(rw); form.add(Box.createVerticalStrut(22));

        JButton loginBtn = AppConstants.greenBtn("LOGIN");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));
        loginBtn.addActionListener(e -> doLogin());
        form.add(loginBtn); form.add(Box.createVerticalStrut(16));

        registerLabel = new JLabel("<html><a href=''>New User? Register here</a></html>");
        registerLabel.setFont(AppConstants.F_SMALL);
        registerLabel.setForeground(new Color(0,0,200));
        registerLabel.setAlignmentX(0.5f);
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLabel.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){ new SignupPage(); dispose(); }
        });
        form.add(registerLabel);

        roleBox.addActionListener(e ->
            registerLabel.setVisible("User".equals(roleBox.getSelectedItem())));

        right.add(form); return right;
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        String role = (String) roleBox.getSelectedItem(); // "User" or "Admin"
        if (user.isEmpty()||pass.isEmpty()) { AppConstants.msg(this,"Fill in all fields.","Error",JOptionPane.WARNING_MESSAGE); return; }
        if (!AppConstants.loadDriver(this)) return;
        try (Connection c = AppConstants.getConn()) {
            // role stored in DB as lowercase: 'user' or 'admin'
            PreparedStatement ps = c.prepareStatement("SELECT id, role FROM users WHERE username=? AND password=?");
            ps.setString(1,user); ps.setString(2,pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int uid = rs.getInt("id");
                String dbRole = rs.getString("role").toLowerCase().trim(); // actual role from DB
                String selectedRole = role.toLowerCase().trim();           // role from dropdown
                if (!dbRole.equals(selectedRole)) {
                    AppConstants.msg(this,"Wrong role selected for this account.\nPlease select: " + dbRole.substring(0,1).toUpperCase()+dbRole.substring(1),"Wrong Role",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (dbRole.equals("admin")) {
                    AdminDashboard dash = new AdminDashboard(uid, user);
                    dispose();
                } else {
                    UserDashboard dash = new UserDashboard(uid, user);
                    dispose();
                }
            } else AppConstants.msg(this,"Invalid username or password.","Login Failed",JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) { AppConstants.msg(this,"DB Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored){}
        SwingUtilities.invokeLater(LoginPage::new);
    }
}