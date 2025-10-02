package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminUI extends JFrame {
    public record Exam(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private JList<Exam> examList;
    private DefaultListModel<Exam> listModel;
    private ModernButton btnEdit, btnDelete, btnViewHistory;

    public AdminUI() {
        setTitle("Admin Panel");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel backgroundPanel = new JPanel(new BorderLayout());
        backgroundPanel.setBackground(Theme.PRIMARY_BACKGROUND);
        setContentPane(backgroundPanel);

        JLabel lblTitle = new JLabel("Quản lý bài kiểm tra", SwingConstants.CENTER);
        lblTitle.setFont(Theme.getBoldFont(32f));
        lblTitle.setForeground(Theme.TEXT_LIGHT);
        lblTitle.setBorder(new EmptyBorder(20, 0, 20, 0));
        backgroundPanel.add(lblTitle, BorderLayout.NORTH);

        RoundedPanel centerPanel = new RoundedPanel(new BorderLayout(20, 0), 20, Theme.PANEL_BACKGROUND);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        backgroundPanel.add(centerPanel, BorderLayout.CENTER);

        listModel = new DefaultListModel<>();
        examList = new JList<>(listModel);
        examList.setFont(Theme.getFont(18f));
        examList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examList.setFixedCellHeight(50);
        examList.setCellRenderer(new QuizUI.ExamListRenderer());

        examList.addListSelectionListener(e -> {
            boolean isSelected = !examList.isSelectionEmpty();
            btnEdit.setEnabled(isSelected);
            btnDelete.setEnabled(isSelected);
            btnViewHistory.setEnabled(isSelected);
        });

        JScrollPane scrollPane = new JScrollPane(examList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(0, 0, 0, 10));

        ModernButton btnAdd = new ModernButton("Thêm bài mới", Theme.BTN_PRIMARY, Theme.BTN_PRIMARY_HOVER, Theme.BTN_PRIMARY);
        btnEdit = new ModernButton("Sửa bài thi", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        btnDelete = new ModernButton("Xóa", Theme.BTN_DANGER, Theme.BTN_DANGER_HOVER, Theme.BTN_DANGER);
        btnViewHistory = new ModernButton("Lịch sử", Theme.PRIMARY_BACKGROUND, Theme.SECONDARY_BACKGROUND, Theme.PRIMARY_BACKGROUND);
        ModernButton btnLogout = new ModernButton("Đăng xuất", Theme.BTN_DANGER, Theme.BTN_DANGER_HOVER, Theme.BTN_DANGER);

        Dimension buttonSize = new Dimension(150, 50);
        btnAdd.setPreferredSize(buttonSize);
        btnEdit.setPreferredSize(buttonSize);
        btnDelete.setPreferredSize(buttonSize);
        btnViewHistory.setPreferredSize(buttonSize);
        btnLogout.setPreferredSize(buttonSize);
        btnAdd.setMaximumSize(buttonSize);
        btnEdit.setMaximumSize(buttonSize);
        btnDelete.setMaximumSize(buttonSize);
        btnViewHistory.setMaximumSize(buttonSize);
        btnLogout.setMaximumSize(buttonSize);
        
        btnAdd.addActionListener(e -> addExam());
        btnEdit.addActionListener(e -> editExam());
        btnDelete.addActionListener(e -> deleteExam());
        btnViewHistory.addActionListener(e -> viewHistory());
        btnLogout.addActionListener(e -> logout());

        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        btnViewHistory.setEnabled(false);

        actionPanel.add(btnAdd);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        actionPanel.add(btnEdit);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        actionPanel.add(btnDelete);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        actionPanel.add(btnViewHistory);
        actionPanel.add(Box.createVerticalGlue());
        actionPanel.add(btnLogout);

        centerPanel.add(actionPanel, BorderLayout.EAST);

        loadExams();
        setVisible(true);
    }
    
    public void loadExams() {
        listModel.clear();
        try (Connection conn = Server.getConnection()) {
            String sql = "SELECT * FROM exams ORDER BY id DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                listModel.addElement(new Exam(rs.getInt("id"), rs.getString("exam_name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addExam() {
        AddExamUI addExamDialog = new AddExamUI(this);
        addExamDialog.setVisible(true);
    }

    private void editExam() {
        Exam selectedExam = examList.getSelectedValue();
        if (selectedExam == null) return;
        
        EditExamUI editExamDialog = new EditExamUI(this, selectedExam);
        editExamDialog.setVisible(true);
    }

    private void deleteExam() {
        Exam selectedExam = examList.getSelectedValue();
        if (selectedExam == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa bài thi '" + selectedExam.name() + "'?\n" +
                "Hành động này sẽ xóa cả câu hỏi và lịch sử liên quan.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Server.getConnection()) {
                conn.prepareStatement("DELETE FROM exam_history WHERE exam_id = " + selectedExam.id()).executeUpdate();
                conn.prepareStatement("DELETE FROM questions WHERE exam_id = " + selectedExam.id()).executeUpdate();
                conn.prepareStatement("DELETE FROM exams WHERE id = " + selectedExam.id()).executeUpdate();
                loadExams();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void viewHistory() {
        Exam selectedExam = examList.getSelectedValue();
        if (selectedExam != null) {
            new AdminHistoryUI(selectedExam.id(), selectedExam.name());
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
}