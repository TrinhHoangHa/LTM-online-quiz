package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;

    public Client() {
        setTitle("Quiz Online");
        setSize(400, 600);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // THAY ĐỔI: Sử dụng DecoratedPanel mới, truyền vào đường dẫn của sticker
        DecoratedPanel mainPanel = new DecoratedPanel(new BorderLayout(), 30, Theme.PRIMARY_BACKGROUND, Theme.SECONDARY_BACKGROUND, "/images/black-cat.png");
        mainPanel.setBorder(new EmptyBorder(20, 30, 30, 30));
        setContentPane(mainPanel);

        // --- Panel Logo ---
        // Vẫn cần setOpaque(false) để nền gradient của mainPanel được nhìn thấy
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        try {
            ImageIcon logoIcon = new ImageIcon(Client.class.getResource("/images/png.png"));
            Image scaledImage = logoIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(scaledImage));
            logoPanel.add(lblLogo);
        } catch (Exception e) {
            JLabel lblTitle = new JLabel("Quiz Time!", SwingConstants.CENTER);
            lblTitle.setFont(Theme.getBoldFont(40f));
            lblTitle.setForeground(Theme.TEXT_LIGHT);
            logoPanel.add(lblTitle);
        }
        mainPanel.add(logoPanel, BorderLayout.NORTH);

        // --- Panel Form ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false); // Vẫn cần trong suốt
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblUser = createLabel("Tên đăng nhập", "/images/user.png");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(lblUser, gbc);
        
        txtUser = createTextField();
        gbc.gridy = 1;
        formPanel.add(txtUser, gbc);

        JLabel lblPass = createLabel("Mật khẩu", "/images/padlock.png");
        gbc.gridy = 2;
        formPanel.add(lblPass, gbc);
        
        txtPass = createPasswordField();
        gbc.gridy = 3;
        formPanel.add(txtPass, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // --- Panel Nút Bấm ---
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        btnPanel.setOpaque(false); // Vẫn cần trong suốt

        ModernButton btnLogin = new ModernButton("Đăng nhập", Theme.BTN_PRIMARY, Theme.BTN_PRIMARY_HOVER, Theme.BTN_PRIMARY);
        btnPanel.add(btnLogin);

        ModernButton btnExit = new ModernButton("Thoát", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        btnPanel.add(btnExit);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> login());
        btnExit.addActionListener(e -> System.exit(0));

        setVisible(true);
    }
    
    // ... (Các hàm còn lại của Client.java giữ nguyên không thay đổi) ...
    private JLabel createLabel(String text, String iconPath) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.getFont(16f));
        label.setForeground(Theme.TEXT_LIGHT);
        try {
            ImageIcon icon = new ImageIcon(Client.class.getResource(iconPath));
            Image scaledImage = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImage));
            label.setIconTextGap(10);
        } catch (Exception e) {
            System.err.println("Không tìm thấy icon: " + iconPath);
        }
        return label;
    }
    private JTextField createTextField() {
        JTextField textField = new JTextField(20);
        textField.setFont(Theme.getFont(16f));
        textField.setBackground(Theme.INPUT_BACKGROUND);
        textField.setForeground(Theme.TEXT_DARK);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
            new EmptyBorder(10, 15, 10, 15)
        ));
        return textField;
    }
    private JPasswordField createPasswordField() {
        JPasswordField pwField = new JPasswordField(20);
        pwField.setFont(Theme.getFont(16f));
        pwField.setBackground(Theme.INPUT_BACKGROUND);
        pwField.setForeground(Theme.TEXT_DARK);
        pwField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
            new EmptyBorder(10, 15, 10, 15)
        ));
        return pwField;
    }
    private void login() {
        try (Socket socket = new Socket("127.0.0.1", 5300);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            out.println("LOGIN;" + user + ";" + pass);
            String response = in.readLine();
            if ("SUCCESS".equals(response)) {
                boolean isAdmin = "admin".equalsIgnoreCase(user);
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
                this.dispose();
                if (isAdmin) {
                    new AdminUI(); 
                } else {
                    new QuizUI(user); 
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến máy chủ!", "Lỗi mạng", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatIntelliJLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(Client::new);
    }
}