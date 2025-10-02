package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class AddExamUI extends JDialog {
    private final AdminUI parent;
    private final JTextField txtExamName;
    private final JPanel questionsPanel; // Panel chính chứa các form câu hỏi
    private final ArrayList<QuestionPanel> questionForms = new ArrayList<>();

    public AddExamUI(AdminUI parent) {
        super(parent, "Tạo bài thi mới", true);
        this.parent = parent;

        setSize(800, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // --- Panel Header ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        headerPanel.add(new JLabel("Tên bài thi:"));
        txtExamName = new JTextField(40);
        txtExamName.setFont(Theme.getFont(16f));
        headerPanel.add(txtExamName);
        add(headerPanel, BorderLayout.NORTH);

        // --- Panel chứa các câu hỏi ---
        questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(questionsPanel);
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel nút chức năng ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        // THÊM MỚI: Nút Hủy
        ModernButton btnCancel = new ModernButton("Hủy", Theme.BTN_DANGER, Theme.BTN_DANGER_HOVER, Theme.BTN_DANGER);
        ModernButton btnAddQuestion = new ModernButton("Thêm câu hỏi", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        ModernButton btnSaveExam = new ModernButton("Lưu bài thi", Theme.BTN_PRIMARY, Theme.BTN_PRIMARY_HOVER, Theme.BTN_PRIMARY);

        // THÊM MỚI: Sự kiện cho nút Hủy
        btnCancel.addActionListener(e -> dispose());
        btnAddQuestion.addActionListener(e -> addNewQuestionPanel());
        btnSaveExam.addActionListener(e -> saveExam());

        // THÊM MỚI: Thêm nút Hủy vào panel
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnAddQuestion);
        buttonPanel.add(btnSaveExam);
        add(buttonPanel, BorderLayout.SOUTH);
        
        addNewQuestionPanel(); // Thêm sẵn 1 câu hỏi
    }
    
    private void addNewQuestionPanel() {
        QuestionPanel qp = new QuestionPanel(this, questionForms.size() + 1);
        questionForms.add(qp);
        questionsPanel.add(qp);
        questionsPanel.revalidate();
        questionsPanel.repaint();
    }
    
    public void removeQuestionPanel(QuestionPanel panelToRemove) {
        questionForms.remove(panelToRemove);
        questionsPanel.remove(panelToRemove);
        
        renumberQuestionPanels();
        
        questionsPanel.revalidate();
        questionsPanel.repaint();
    }
    
    private void renumberQuestionPanels() {
        for (int i = 0; i < questionForms.size(); i++) {
            questionForms.get(i).setQuestionNumber(i + 1);
        }
    }
    
    private void saveExam() {
        String examName = txtExamName.getText();
        if (examName == null || examName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên bài thi!");
            return;
        }

        try (Connection conn = Server.getConnection()) {
            String examSql = "INSERT INTO exams (exam_name) VALUES (?)";
            PreparedStatement examStmt = conn.prepareStatement(examSql, Statement.RETURN_GENERATED_KEYS);
            examStmt.setString(1, examName);
            examStmt.executeUpdate();

            ResultSet generatedKeys = examStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int examId = generatedKeys.getInt(1);

                String questionSql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement questionStmt = conn.prepareStatement(questionSql);
                
                for (QuestionPanel qp : questionForms) {
                    questionStmt.setInt(1, examId);
                    questionStmt.setString(2, qp.getQuestionText());
                    questionStmt.setString(3, qp.getOptionA());
                    questionStmt.setString(4, qp.getOptionB());
                    questionStmt.setString(5, qp.getOptionC());
                    questionStmt.setString(6, qp.getOptionD());
                    questionStmt.setInt(7, qp.getCorrectOption());
                    questionStmt.addBatch();
                }
                questionStmt.executeBatch();
                
                JOptionPane.showMessageDialog(this, "Thêm bài thi thành công!");
                parent.loadExams();
                this.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu bài thi!");
        }
    }

    // Lớp nội bộ để tạo form cho 1 câu hỏi
    private static class QuestionPanel extends JPanel {
        private JTextField txtQuestion, txtOptA, txtOptB, txtOptC, txtOptD;
        private JSpinner spnCorrect;
        private TitledBorder titledBorder;

        QuestionPanel(AddExamUI parentUI, int number) {
            titledBorder = BorderFactory.createTitledBorder("Câu hỏi " + number);
            setBorder(titledBorder);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JButton btnDelete = new JButton("Xóa câu");
            btnDelete.setBackground(Theme.BTN_DANGER);
            btnDelete.setForeground(Color.WHITE);
            btnDelete.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(parentUI, "Bạn có chắc muốn xóa câu hỏi này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    parentUI.removeQuestionPanel(this);
                }
            });

            gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Câu hỏi:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 2; txtQuestion = new JTextField(40); add(txtQuestion, gbc);
            
            gbc.gridx = 3; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST; add(btnDelete, gbc);
            gbc.anchor = GridBagConstraints.CENTER;

            gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Đáp án A:"), gbc);
            gbc.gridx = 1; txtOptA = new JTextField(20); add(txtOptA, gbc);
            gbc.gridx = 2; add(new JLabel("Đáp án B:"), gbc);
            gbc.gridx = 3; txtOptB = new JTextField(20); add(txtOptB, gbc);

            gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Đáp án C:"), gbc);
            gbc.gridx = 1; txtOptC = new JTextField(20); add(txtOptC, gbc);
            gbc.gridx = 2; add(new JLabel("Đáp án D:"), gbc);
            gbc.gridx = 3; txtOptD = new JTextField(20); add(txtOptD, gbc);

            gbc.gridx = 0; gbc.gridy = 3; add(new JLabel("Đáp án đúng (1-4):"), gbc);
            gbc.gridx = 1; spnCorrect = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1)); add(spnCorrect, gbc);
        }
        
        public void setQuestionNumber(int number) {
            titledBorder.setTitle("Câu hỏi " + number);
        }

        public String getQuestionText() { return txtQuestion.getText(); }
        public String getOptionA() { return txtOptA.getText(); }
        public String getOptionB() { return txtOptB.getText(); }
        public String getOptionC() { return txtOptC.getText(); }
        public String getOptionD() { return txtOptD.getText(); }
        public int getCorrectOption() { return (int) spnCorrect.getValue(); }
    }
}