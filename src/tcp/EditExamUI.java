package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditExamUI extends JDialog {
	private static final long serialVersionUID = 1L;
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

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        lblTitle = new JLabel("Quản lý bài thi: " + exam.name(), SwingConstants.LEFT);
        lblTitle.setFont(Theme.getBoldFont(20f));
        headerPanel.add(lblTitle);
        
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        ModernButton btnRenameExam = new ModernButton("Đổi tên", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        ModernButton btnSetTime = new ModernButton("Đặt thời gian", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        btnRenameExam.addActionListener(e -> renameExam());
        btnSetTime.addActionListener(e -> setTimeLimit());
        headerButtons.add(btnRenameExam);
        headerButtons.add(btnSetTime);
        
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(headerPanel, BorderLayout.WEST);
        headerContainer.add(headerButtons, BorderLayout.EAST);
        
        add(headerContainer, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Nội dung câu hỏi", "A", "B", "C", "D", "Đáp án đúng (1-4)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        questionsTable = new JTable(tableModel);
        
        // --- THÊM MỚI: Áp dụng màu xen kẽ cho bảng câu hỏi ---
        questionsTable.setRowHeight(30);
        questionsTable.setFont(Theme.getFont(14f));
        questionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer zebraRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : Theme.INPUT_BACKGROUND);
                }
                return c;
            }
        };

        for (int i = 0; i < questionsTable.getColumnCount(); i++) {
            questionsTable.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
        }
        // --- KẾT THÚC THÊM MỚI ---
        
        questionsTable.getColumnModel().getColumn(0).setMinWidth(0);
        questionsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        
        add(new JScrollPane(questionsTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        ModernButton btnAddQuestion = new ModernButton("Thêm câu mới", Theme.BTN_PRIMARY, Theme.BTN_PRIMARY_HOVER, Theme.BTN_PRIMARY);
        ModernButton btnEditQuestion = new ModernButton("Sửa câu đã chọn", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        ModernButton btnDeleteQuestion = new ModernButton("Xóa câu đã chọn", Theme.BTN_DANGER, Theme.BTN_DANGER_HOVER, Theme.BTN_DANGER);
        ModernButton btnClose = new ModernButton("Thoát", Theme.PRIMARY_BACKGROUND, Theme.SECONDARY_BACKGROUND, Theme.PRIMARY_BACKGROUND);
        
        btnAddQuestion.addActionListener(e -> addQuestion());
        btnEditQuestion.addActionListener(e -> editQuestion());
        btnDeleteQuestion.addActionListener(e -> deleteQuestion());
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnAddQuestion);
        buttonPanel.add(btnEditQuestion);
        buttonPanel.add(btnDeleteQuestion);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnClose);
        
        add(buttonPanel, BorderLayout.SOUTH);

        loadQuestions();
    }
    
    private void setTimeLimit() {
        int currentTime = 30;
        try (Connection conn = Server.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT time_limit_minutes FROM exams WHERE id = ?");
            stmt.setInt(1, exam.id());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentTime = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String newTimeStr = JOptionPane.showInputDialog(this, "Nhập thời gian làm bài mới (phút):", currentTime);
        if (newTimeStr != null) {
            try {
                int newTime = Integer.parseInt(newTimeStr);
                if (newTime < 1) {
                    JOptionPane.showMessageDialog(this, "Thời gian phải là số dương!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try (Connection conn = Server.getConnection()) {
                    String sql = "UPDATE exams SET time_limit_minutes = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, newTime);
                    stmt.setInt(2, exam.id());
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Cập nhật thời gian thành công!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập một số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadQuestions() {
        tableModel.setRowCount(0);
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
                parent.loadExams();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void addQuestion() {
        AddQuestionUI addDialog = new AddQuestionUI(this, this.exam.id());
        addDialog.setVisible(true);
    }

    private void editQuestion() {
        int selectedRow = questionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để sửa!");
            return;
        }

        int questionId = (int) tableModel.getValueAt(selectedRow, 0);
        EditQuestionUI editDialog = new EditQuestionUI(this, questionId);
        editDialog.setVisible(true);
    }

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
                loadQuestions();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}