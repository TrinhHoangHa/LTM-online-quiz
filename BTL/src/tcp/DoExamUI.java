package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

class Question {
    int id;
    String question;
    String[] options = new String[4];
    int correctOption;
}

public class DoExamUI extends JFrame {
    private final int examId;
    private final String username;

    private final ArrayList<Question> questions = new ArrayList<>();
    private final int[] userAnswers;

    private final JPanel mainQuestionsPanel;
    private final ArrayList<JPanel> questionPanelList = new ArrayList<>();
    private final ArrayList<ModernButton[]> answerButtonsList = new ArrayList<>();
    private final ArrayList<CircleButton> questionButtons = new ArrayList<>();

    private final JLabel scoreLabel;
    private final ModernButton submitBtn;

    public DoExamUI(int examId, String username) {
        this.examId = examId;
        this.username = username;

        setTitle("Làm bài kiểm tra");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loadQuestions();
        this.userAnswers = new int[questions.size()];

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.PRIMARY_BACKGROUND);
        setContentPane(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel examTitle = new JLabel("Bài kiểm tra #" + examId, SwingConstants.LEFT);
        examTitle.setFont(Theme.getBoldFont(24f));
        examTitle.setForeground(Theme.TEXT_LIGHT);
        headerPanel.add(examTitle, BorderLayout.WEST);
        scoreLabel = new JLabel("", SwingConstants.RIGHT);
        scoreLabel.setFont(Theme.getBoldFont(24f));
        scoreLabel.setForeground(Theme.BTN_SECONDARY);
        headerPanel.add(scoreLabel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        sidebar.setBackground(Theme.SECONDARY_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(120, 0));
        mainPanel.add(new JScrollPane(sidebar), BorderLayout.WEST);

        mainQuestionsPanel = new JPanel();
        mainQuestionsPanel.setLayout(new BoxLayout(mainQuestionsPanel, BoxLayout.Y_AXIS));
        mainQuestionsPanel.setBackground(Color.DARK_GRAY);

        JScrollPane scrollPane = new JScrollPane(mainQuestionsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        buildQuestionPanels(sidebar);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        submitBtn = new ModernButton("Nộp bài", Theme.BTN_PRIMARY, Theme.BTN_PRIMARY_HOVER, Theme.BTN_PRIMARY);
        submitBtn.addActionListener(e -> submitExam());
        bottomPanel.add(submitBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadQuestions() {
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
                q.correctOption = rs.getInt("correct_option");
                questions.add(q);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildQuestionPanels(JPanel sidebar) {
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);

            RoundedPanel questionCard = new RoundedPanel(new BorderLayout(10, 20), 20, Theme.PANEL_BACKGROUND);
            questionCard.setBorder(new EmptyBorder(40, 40, 40, 40));
            questionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));

            JLabel qLabel = new JLabel(String.format("<html><body style='width: 550px;'>Câu %d: %s</body></html>", i + 1, q.question));
            qLabel.setFont(Theme.getBoldFont(22f));
            qLabel.setForeground(Theme.TEXT_DARK);
            questionCard.add(qLabel, BorderLayout.NORTH);

            JPanel answersPanel = new JPanel(new GridLayout(2, 2, 20, 20));
            answersPanel.setOpaque(false);
            ModernButton[] answerButtons = new ModernButton[4];

            for (int j = 0; j < 4; j++) {
                answerButtons[j] = new ModernButton("<html>" + q.options[j] + "</html>", Theme.PRIMARY_BACKGROUND, Theme.SECONDARY_BACKGROUND, Theme.BTN_SECONDARY);
                answerButtons[j].setFont(Theme.getFont(16f));
                final int questionIndex = i;
                final int optionIndex = j + 1;
                answerButtons[j].addActionListener(e -> selectAnswer(questionIndex, optionIndex));
                answersPanel.add(answerButtons[j]);
            }
            answerButtonsList.add(answerButtons);
            questionCard.add(answersPanel, BorderLayout.CENTER);

            mainQuestionsPanel.add(questionCard);
            mainQuestionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            questionPanelList.add(questionCard);

            CircleButton circleBtn = new CircleButton(String.valueOf(i + 1));
            final int indexToScroll = i;
            circleBtn.addActionListener(e -> {
                Rectangle rect = questionPanelList.get(indexToScroll).getBounds();
                mainQuestionsPanel.scrollRectToVisible(rect);
            });
            sidebar.add(circleBtn);
            questionButtons.add(circleBtn);
        }
    }

    private void selectAnswer(int questionIndex, int optionIndex) {
        userAnswers[questionIndex] = optionIndex;

        ModernButton[] buttons = answerButtonsList.get(questionIndex);
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setSelected((i + 1) == optionIndex);
        }

        questionButtons.get(questionIndex).setAnswered(true);
    }

    private void submitExam() {
        for (int i = 0; i < userAnswers.length; i++) {
            if (userAnswers[i] == 0) {
                JOptionPane.showMessageDialog(this, "Bạn chưa trả lời câu " + (i + 1) + "!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                Rectangle rect = questionPanelList.get(i).getBounds();
                mainQuestionsPanel.scrollRectToVisible(rect);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn nộp bài không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (userAnswers[i] == questions.get(i).correctOption) {
                score++;
            }

            ModernButton[] buttons = answerButtonsList.get(i);
            for (int j = 0; j < buttons.length; j++) {
                int currentOption = j + 1;
                buttons[j].setInteractionsEnabled(false);
                if (currentOption == questions.get(i).correctOption) {
                    buttons[j].setBackground(Theme.BTN_PRIMARY);
                } else if (currentOption == userAnswers[i]) {
                    buttons[j].setBackground(Theme.BTN_DANGER);
                } else {
                    buttons[j].setBackground(Theme.PRIMARY_BACKGROUND);
                }
                buttons[j].setEnabled(false);
            }
        }
        scoreLabel.setText("Điểm: " + score + " / " + questions.size());

        try (Connection conn = Server.getConnection()) {
            String sql = "INSERT INTO exam_history (username, exam_id, score, total_questions, taken_at) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, examId);
            stmt.setInt(3, score);
            stmt.setInt(4, questions.size());
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        submitBtn.setText("Quay lại");
        submitBtn.removeActionListener(submitBtn.getActionListeners()[0]);
        submitBtn.addActionListener(e -> {
            new QuizUI(username);
            dispose();
        });
    }

    private static class CircleButton extends JButton {
        private boolean answered = false;
        public CircleButton(String text) {
            super(text);
            setFont(Theme.getBoldFont(16f));
            setForeground(Theme.TEXT_DARK);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(50, 50));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        public void setAnswered(boolean answered) {
            this.answered = answered;
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (answered) {
                g2.setColor(Theme.BTN_SECONDARY);
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(Theme.BORDER_COLOR);
            g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}