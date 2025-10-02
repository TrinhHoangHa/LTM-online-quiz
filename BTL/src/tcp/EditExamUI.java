package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditExamUI extends JDialog {

    private final AdminUI parent;
    private final AdminUI.Exam exam;
    private final DefaultTableModel tableModel;
    private final JTable questionsTable;
    private final JLabel lblTitle;

    public EditExamUI(AdminUI parent, AdminUI.Exam exam) {
        super(parent, "Chỉnh sửa bài thi", true);
        this.parent = parent;
        this.exam = exam;

        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // --- Panel Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        lblTitle = new JLabel("Quản lý bài thi: " + exam.name(), SwingConstants.LEFT);
        lblTitle.setFont(Theme.getBoldFont(20f));
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        
        ModernButton btnRenameExam = new ModernButton("Đổi tên bài thi", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        btnRenameExam.addActionListener(e -> renameExam());
        headerPanel.add(btnRenameExam, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // --- Bảng câu hỏi ---
        String[] columnNames = {"ID", "Nội dung câu hỏi", "A", "B", "C", "D", "Đáp án đúng (1-4)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng
            }
        };
        questionsTable = new JTable(tableModel);
        questionsTable.setRowHeight(30);
        questionsTable.setFont(Theme.getFont(14f));
        questionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Ẩn cột ID
        questionsTable.getColumnModel().getColumn(0).setMinWidth(0);
        questionsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        
        add(new JScrollPane(questionsTable), BorderLayout.CENTER);

        // --- Panel nút chức năng cho câu hỏi ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        ModernButton btnAddQuestion = new ModernButton("Thêm câu mới", Theme.BTN_PRIMARY, Theme.BTN_PRIMARY_HOVER, Theme.BTN_PRIMARY);
        ModernButton btnEditQuestion = new ModernButton("Sửa câu đã chọn", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        ModernButton btnDeleteQuestion = new ModernButton("Xóa câu đã chọn", Theme.BTN_DANGER, Theme.BTN_DANGER_HOVER, Theme.BTN_DANGER);
        ModernButton btnClose = new ModernButton("Thoát", Theme.PRIMARY_BACKGROUND, Theme.SECONDARY_BACKGROUND, Theme.PRIMARY_BACKGROUND);
        
        btnAddQuestion.addActionListener(e -> addQuestion());
        btnEditQuestion.addActionListener(e -> editQuestion());
        btnDeleteQuestion.addActionListener(e -> deleteQuestion()); // Gọi đến phương thức deleteQuestion()
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnAddQuestion);
        buttonPanel.add(btnEditQuestion);
        buttonPanel.add(btnDeleteQuestion);
        buttonPanel.add(Box.createHorizontalStrut(20)); // Khoảng cách
        buttonPanel.add(btnClose);
        
        add(buttonPanel, BorderLayout.SOUTH);

        loadQuestions();
    }

    public void loadQuestions() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        try (Connection conn = Server.getConnection()) {
            String sql = "SELECT * FROM questions WHERE exam_id = ? ORDER BY id";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, exam.id());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("question_text"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getInt("correct_option")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renameExam() {
        String newName = JOptionPane.showInputDialog(this, "Nhập tên mới cho bài thi:", exam.name());
        if (newName != null && !newName.trim().isEmpty()) {
            try (Connection conn = Server.getConnection()) {
                String sql = "UPDATE exams SET exam_name = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newName);
                stmt.setInt(2, exam.id());
                stmt.executeUpdate();
                lblTitle.setText("Quản lý bài thi: " + newName);
                parent.loadExams(); // Cập nhật lại danh sách ở cửa sổ AdminUI
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void addQuestion() {
        String questionText = JOptionPane.showInputDialog(this, "Nội dung câu hỏi:");
        if(questionText == null || questionText.trim().isEmpty()) return;
        String optA = JOptionPane.showInputDialog(this, "Đáp án A:");
        String optB = JOptionPane.showInputDialog(this, "Đáp án B:");
        String optC = JOptionPane.showInputDialog(this, "Đáp án C:");
        String optD = JOptionPane.showInputDialog(this, "Đáp án D:");
        int correctOpt = Integer.parseInt(JOptionPane.showInputDialog(this, "Đáp án đúng (nhập số 1, 2, 3 hoặc 4):"));

        try (Connection conn = Server.getConnection()){
            String sql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, exam.id());
            stmt.setString(2, questionText);
            stmt.setString(3, optA);
            stmt.setString(4, optB);
            stmt.setString(5, optC);
            stmt.setString(6, optD);
            stmt.setInt(7, correctOpt);
            stmt.executeUpdate();
            loadQuestions(); // Tải lại bảng
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void editQuestion() {
        int selectedRow = questionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để sửa!");
            return;
        }

        int questionId = (int) tableModel.getValueAt(selectedRow, 0);
        // Mở cửa sổ EditQuestionUI
        EditQuestionUI editDialog = new EditQuestionUI(this, questionId);
        editDialog.setVisible(true);
    }

    // PHƯƠNG THỨC BỊ THIẾU CÓ LẼ NẰM Ở ĐÂY
    private void deleteQuestion() {
        int selectedRow = questionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để xóa!");
            return;
        }

        int questionId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa câu hỏi này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try(Connection conn = Server.getConnection()) {
                String sql = "DELETE FROM questions WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, questionId);
                stmt.executeUpdate();
                loadQuestions(); // Tải lại bảng
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}