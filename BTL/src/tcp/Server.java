package tcp;

import java.io.*;
import java.net.*;
import java.sql.*;

public class Server {

    // ===== KẾT NỐI SQL =====
    public static Connection getConnection() {
        try {
            String url = "jdbc:mysql://127.0.0.1:3306/quizDB";
            String user = "root";       // thay bằng user MySQL của bạn
            String pass = "08112004";     // thay bằng mật khẩu MySQL của bạn

            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== MAIN SERVER =====
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5300)) {
            System.out.println("Server running on port 5300...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== XỬ LÝ CLIENT =====
    private static void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request = in.readLine(); // ví dụ: LOGIN;user;pass
            String[] parts = request.split(";");
            if (parts[0].equals("LOGIN")) {
                String username = parts[1];
                String password = parts[2];

                if (checkLogin(username, password)) {
                    out.println("SUCCESS");
                } else {
                    out.println("FAIL");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== KIỂM TRA LOGIN =====
    private static boolean checkLogin(String user, String pass) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user);
            stmt.setString(2, pass);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
