package tcp;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {
    private JTextField txtUser = new JTextField(15);
    private JPasswordField txtPass = new JPasswordField(15);
    private JButton btnLogin = new JButton("Đăng nhập");

    public Client() {
        setTitle("Đăng nhập");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("Tên đăng nhập:"));
        add(txtUser);
        add(new JLabel("Mật khẩu:"));
        add(txtPass);
        add(btnLogin);

        btnLogin.addActionListener(e -> login());

        setVisible(true);
    }

    private void login() {
        try (Socket socket = new Socket("127.0.0.1", 5300);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            out.println("LOGIN;" + user + ";" + pass);

            String response = in.readLine();
            if ("SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
                this.dispose();
                new QuizUI(user);
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}
