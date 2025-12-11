package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddQuestionUI extends JDialog {
	private static final long serialVersionUID = 1L;
    private final EditExamUI parentUI;
    private final int examId;

    private final JTextField txtQuestion;
    private final JTextField txtOptA, txtOptB, txtOptC, txtOptD;
    private final JSpinner spnCorrect;

    public AddQuestionUI(EditExamUI parent, int examId) {
        super(parent, "Thêm câu hỏi mới", true);
        this.parentUI = parent;
        this.examId = examId;

        setSize(800, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Panel chứa các trường nhập liệu
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtQuestion = new JTextField(40);
        txtOptA = new JTextField(20);
        txtOptB = new JTextField(20);
        txtOptC = new JTextField(20);
        txtOptD = new JTextField(20);
        spnCorrect = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));

        // Sắp xếp các thành phần
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Câu hỏi:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(txtQuestion, gbc);
        gbc.gridwidth = 1; // Reset gridwidth
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Đáp án A:"), gbc);
        gbc.gridx = 1; formPanel.add(txtOptA, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Đáp án B:"), gbc);
        gbc.gridx = 3; formPanel.add(txtOptB, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Đáp án C:"), gbc);
        gbc.gridx = 1; formPanel.add(txtOptC, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Đáp án D:"), gbc);
        gbc.gridx = 3; formPanel.add(txtOptD, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Đáp án đúng (1-4):"), gbc);
        gbc.gridx = 1; formPanel.add(spnCorrect, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Panel chứa các nút bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        ModernButton btnCancel = new ModernButton("Hủy", Theme.BTN_DANGER, Theme.BTN_DANGER_HOVER, Theme.BTN_DANGER);
        ModernButton btnSaveChanges = new ModernButton("Lưu câu hỏi", Theme.BTN_PRIMARY, Theme.BTN_PRIMARY_HOVER, Theme.BTN_PRIMARY);
        
        btnSaveChanges.addActionListener(e -> saveQuestion());
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSaveChanges);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveQuestion() {
        // Lấy dữ liệu từ các trường nhập liệu
        String questionText = txtQuestion.getText();
        String optA = txtOptA.getText();
        String optB = txtOptB.getText();
        String optC = txtOptC.getText();
        String optD = txtOptD.getText();
        int correctOpt = (int) spnCorrect.getValue();

        // Kiểm tra dữ liệu đầu vào
        if (questionText.trim().isEmpty() || optA.trim().isEmpty() || optB.trim().isEmpty() || optC.trim().isEmpty() || optD.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin cho câu hỏi và các đáp án!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Lưu vào cơ sở dữ liệu
        try (Connection conn = Server.getConnection()){
            String sql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.examId);
            stmt.setString(2, questionText);
            stmt.setString(3, optA);
            stmt.setString(4, optB);
            stmt.setString(5, optC);
            stmt.setString(6, optD);
            stmt.setInt(7, correctOpt);
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Thêm câu hỏi thành công!");
            parentUI.loadQuestions(); // Tải lại bảng câu hỏi ở cửa sổ cha
            this.dispose(); // Đóng cửa sổ hiện tại

        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm câu hỏi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}