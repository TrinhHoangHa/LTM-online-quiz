package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditQuestionUI extends JDialog {
	private static final long serialVersionUID = 1L;
    private final EditExamUI parentUI;
    private final int questionId;

    private final JTextField txtQuestion;
    private final JTextField txtOptA, txtOptB, txtOptC, txtOptD;
    private final JSpinner spnCorrect;

    public EditQuestionUI(EditExamUI parent, int questionId) {
        super(parent, "Chỉnh sửa câu hỏi", true);
        this.parentUI = parent;
        this.questionId = questionId;

        setSize(800, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

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

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Câu hỏi:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(txtQuestion, gbc);
        gbc.gridwidth = 1;
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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        ModernButton btnSaveChanges = new ModernButton("Lưu thay đổi", Theme.BTN_PRIMARY, Theme.BTN_PRIMARY_HOVER, Theme.BTN_PRIMARY);
        btnSaveChanges.addActionListener(e -> saveChanges());
        buttonPanel.add(btnSaveChanges);
        add(buttonPanel, BorderLayout.SOUTH);

        loadQuestionData();
    }

    private void loadQuestionData() {
        try (Connection conn = Server.getConnection()) {
            String sql = "SELECT * FROM questions WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                txtQuestion.setText(rs.getString("question_text"));
                txtOptA.setText(rs.getString("option_a"));
                txtOptB.setText(rs.getString("option_b"));
                txtOptC.setText(rs.getString("option_c"));
                txtOptD.setText(rs.getString("option_d"));
                spnCorrect.setValue(rs.getInt("correct_option"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu câu hỏi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveChanges() {
        String newText = txtQuestion.getText();
        String newA = txtOptA.getText();
        String newB = txtOptB.getText();
        String newC = txtOptC.getText();
        String newD = txtOptD.getText();
        int newCorrect = (int) spnCorrect.getValue();

        try (Connection conn = Server.getConnection()) {
            String sql = "UPDATE questions SET question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_option=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newText);
            stmt.setString(2, newA);
            stmt.setString(3, newB);
            stmt.setString(4, newC);
            stmt.setString(5, newD);
            stmt.setInt(6, newCorrect);
            stmt.setInt(7, this.questionId);
            
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Cập nhật câu hỏi thành công!");
            parentUI.loadQuestions();
            this.dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật câu hỏi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}