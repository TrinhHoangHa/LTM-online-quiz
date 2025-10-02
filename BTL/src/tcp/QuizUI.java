package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QuizUI extends JFrame {
    private record Exam(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private final String username;
    private JList<Exam> examList;
    private DefaultListModel<Exam> listModel;
    private ModernButton btnDoExam;
    private ModernButton btnViewHistory;

    public QuizUI(String username) {
        this.username = username;
        setTitle("Trắc nghiệm Online - Xin chào " + username);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel backgroundPanel = new JPanel(new BorderLayout());
        backgroundPanel.setBackground(Theme.PRIMARY_BACKGROUND);
        setContentPane(backgroundPanel);

        JLabel lblTitle = new JLabel("Chọn một bài kiểm tra", SwingConstants.CENTER);
        lblTitle.setFont(Theme.getBoldFont(32f));
        lblTitle.setForeground(Theme.TEXT_LIGHT);
        lblTitle.setBorder(new EmptyBorder(20, 0, 20, 0));
        backgroundPanel.add(lblTitle, BorderLayout.NORTH);
        
        RoundedPanel centerPanel = new RoundedPanel(new BorderLayout(10, 10), 20, Theme.PANEL_BACKGROUND);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        backgroundPanel.add(centerPanel, BorderLayout.CENTER);

        listModel = new DefaultListModel<>();
        examList = new JList<>(listModel);
        examList.setFont(Theme.getFont(18f));
        examList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examList.setFixedCellHeight(50);
        examList.setCellRenderer(new ExamListRenderer());
        
        examList.addListSelectionListener(e -> {
            boolean isSelected = !examList.isSelectionEmpty();
            btnDoExam.setEnabled(isSelected);
            btnViewHistory.setEnabled(isSelected);
        });

        JScrollPane scrollPane = new JScrollPane(examList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new BorderLayout(20, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel mainActionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        mainActionsPanel.setOpaque(false);

        btnDoExam = new ModernButton("Làm bài", Theme.BTN_PRIMARY, Theme.BTN_PRIMARY_HOVER, Theme.BTN_PRIMARY);
        btnDoExam.setEnabled(false);
        btnDoExam.addActionListener(e -> openExam());
        mainActionsPanel.add(btnDoExam);

        btnViewHistory = new ModernButton("Xem lịch sử", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        btnViewHistory.setEnabled(false);
        btnViewHistory.addActionListener(e -> openHistory());
        mainActionsPanel.add(btnViewHistory);

        ModernButton btnLogout = new ModernButton("Đăng xuất", Theme.BTN_DANGER, Theme.BTN_DANGER_HOVER, Theme.BTN_DANGER);
        btnLogout.addActionListener(e -> logout());

        actionPanel.add(mainActionsPanel, BorderLayout.CENTER);
        actionPanel.add(btnLogout, BorderLayout.WEST);

        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        loadExams();
        setVisible(true);
    }
    
    private void loadExams() {
        try (Connection conn = Server.getConnection()) {
            String sql = "SELECT * FROM exams";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("exam_name");
                listModel.addElement(new Exam(id, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách bài thi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openExam() {
        Exam selectedExam = examList.getSelectedValue();
        if (selectedExam != null) {
            new DoExamUI(selectedExam.id(), username);
            this.dispose();
        }
    }

    private void openHistory() {
        Exam selectedExam = examList.getSelectedValue();
        if (selectedExam != null) {
            new HistoryUI(selectedExam.id(), username);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất không?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new Client();
        }
    }
    
    static class ExamListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(5, 15, 5, 15));
            if (isSelected) {
                label.setBackground(Theme.PRIMARY_BACKGROUND);
                label.setForeground(Theme.TEXT_LIGHT);
            } else {
                 label.setBackground(index % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                 label.setForeground(Theme.TEXT_DARK);
            }
            return label;
        }
    }
}