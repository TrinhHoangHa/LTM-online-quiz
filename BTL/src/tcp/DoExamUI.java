package tcp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;

class Question {
    int id;
    String question;
    String[] options = new String[4];
    int correct; // 1-4
}

public class DoExamUI extends JFrame {
    private ArrayList<Question> questions = new ArrayList<>();
    private ArrayList<JPanel> questionPanels = new ArrayList<>();
    private ArrayList<ButtonGroup> answerGroups = new ArrayList<>();
    private ArrayList<JButton> questionButtons = new ArrayList<>();
    private JPanel questionsPanel;
    private JLabel scoreLabel = new JLabel("", SwingConstants.CENTER);
    private JButton submitBtn;

    public DoExamUI(int examId) {
        setTitle("Làm bài kiểm tra #" + examId);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        loadQuestions(examId);

        // Sidebar tròn bên trái
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(230, 230, 230));
        sidebar.setPreferredSize(new Dimension(80, 0));

        // Panel chứa câu hỏi
        questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBackground(Color.GRAY);

        JScrollPane scrollPane = new JScrollPane(questionsPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Panel chính chứa score + scroll câu hỏi
        JPanel mainPanel = new JPanel(new BorderLayout());
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        scoreLabel.setForeground(new Color(34, 167, 240));
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(scoreLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Tạo UI cho từng câu hỏi
        for (int i = 0; i < questions.size(); i++) {
            int qIndex = i;
            Question q = questions.get(i);

            JPanel qBox = new JPanel();
            qBox.setLayout(new BoxLayout(qBox, BoxLayout.Y_AXIS));
            qBox.setBackground(Color.WHITE);
            qBox.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            qBox.setAlignmentX(Component.CENTER_ALIGNMENT);
            qBox.setMaximumSize(new Dimension(600, 200));
            qBox.setPreferredSize(new Dimension(600, 200));

            JLabel qLabel = new JLabel("Câu " + (i + 1) + ": " + q.question);
            qLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            qLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            qBox.add(qLabel);

            ButtonGroup group = new ButtonGroup();
            for (int j = 0; j < 4; j++) {
                JRadioButton option = new JRadioButton(q.options[j]);
                option.setAlignmentX(Component.LEFT_ALIGNMENT);
                option.setFont(new Font("SansSerif", Font.PLAIN, 13));
                option.setOpaque(true);
                option.setBackground(Color.WHITE);
                group.add(option);
                qBox.add(option);

                option.addActionListener(e -> questionButtons.get(qIndex).setBackground(new Color(173, 216, 230)));
            }

            answerGroups.add(group);
            questionsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            questionsPanel.add(qBox);
            questionPanels.add(qBox);

            // Nút sidebar tròn
            JButton circleBtn = new JButton(String.valueOf(i + 1)) {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(getBackground());
                    g.fillOval(0, 0, getWidth(), getHeight());
                    g.setColor(getForeground());
                    FontMetrics fm = g.getFontMetrics();
                    int textWidth = fm.stringWidth(getText());
                    int textHeight = fm.getAscent();
                    g.drawString(getText(), (getWidth() - textWidth) / 2,
                            (getHeight() + textHeight) / 2 - 2);
                }

                @Override
                protected void paintBorder(Graphics g) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                }
            };
            circleBtn.setFocusPainted(false);
            circleBtn.setContentAreaFilled(false);
            circleBtn.setBorderPainted(false);
            circleBtn.setOpaque(false);
            circleBtn.setForeground(Color.BLACK);
            circleBtn.setBackground(Color.WHITE);
            circleBtn.setPreferredSize(new Dimension(40, 40));
            circleBtn.setMaximumSize(new Dimension(40, 40));
            circleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            circleBtn.addActionListener(e -> {
                Rectangle rect = questionPanels.get(qIndex).getBounds();
                questionsPanel.scrollRectToVisible(rect);
            });

            questionButtons.add(circleBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebar.add(circleBtn);
        }

        // Nút nộp bài
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        submitBtn = new JButton("Nộp bài");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        submitBtn.setBackground(new Color(34, 167, 240));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        submitBtn.addActionListener(e -> submitExam());
        bottomPanel.add(submitBtn);

        add(bottomPanel, BorderLayout.SOUTH);
        add(sidebar, BorderLayout.WEST);

        setVisible(true);
    }

    private void loadQuestions(int examId) {
        try (Connection conn = Server.getConnection()) {
            String sql = "SELECT * FROM questions WHERE exam_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, examId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Question q = new Question();
                q.id = rs.getInt("id");
                q.question = rs.getString("question_text");
                q.options[0] = rs.getString("option_a");
                q.options[1] = rs.getString("option_b");
                q.options[2] = rs.getString("option_c");
                q.options[3] = rs.getString("option_d");
                q.correct = rs.getInt("correct_option"); // 1-4
                questions.add(q);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitExam() {
        // Kiểm tra câu chưa trả lời
        for (int i = 0; i < answerGroups.size(); i++) {
            if (answerGroups.get(i).getSelection() == null) {
                JOptionPane.showMessageDialog(this,
                        "Câu " + (i + 1) + " chưa trả lời.",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        int score = 0;

        // Hiển thị đáp án đúng/sai
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            ButtonGroup group = answerGroups.get(i);
            Enumeration<AbstractButton> buttons = group.getElements();
            int idx = 0;
            while (buttons.hasMoreElements()) {
                JRadioButton btn = (JRadioButton) buttons.nextElement();
                if (idx + 1 == q.correct) {
                    btn.setBackground(new Color(144, 238, 144)); // xanh nhạt = đúng
                }
                if (btn.isSelected() && (idx + 1 != q.correct)) {
                    btn.setBackground(new Color(255, 182, 193)); // đỏ nhạt = sai
                }
                btn.setForeground(Color.BLACK); // chữ màu đen
                idx++;
            }
        
            // Tính điểm
            buttons = group.getElements();
            idx = 0;
            while (buttons.hasMoreElements()) {
                JRadioButton btn = (JRadioButton) buttons.nextElement();
                if (btn.isSelected() && (idx + 1 == q.correct)) score++;
                idx++;
            }
        }

        // Hiển thị điểm
        scoreLabel.setText("Điểm: " + score + " / " + questions.size());

        // Khóa tất cả đáp án
        for (ButtonGroup group : answerGroups) {
            Enumeration<AbstractButton> buttons = group.getElements();
            while (buttons.hasMoreElements()) buttons.nextElement().setEnabled(false);
        }

        // Đổi nút nộp thành "Quay lại"
        submitBtn.setText("Quay lại");
        submitBtn.removeActionListener(submitBtn.getActionListeners()[0]); // bỏ action cũ
        submitBtn.setEnabled(true); // mở lại nút
        submitBtn.addActionListener(e -> dispose()); // bấm đóng cửa sổ
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DoExamUI(1));
    }
}
