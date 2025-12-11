package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
// Import thư viện file chooser
import javax.swing.filechooser.FileNameExtensionFilter;

// Import thư viện Apache POI để đọc Excel
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // Cho file .xlsx

// Import thư viện IO
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// Import các thư viện Java cơ bản
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

@SuppressWarnings("serial") // Thêm để tắt cảnh báo serialVersionUID
public class AddExamUI extends JDialog {
    
    // private static final long serialVersionUID = 1L; // (Đây là cách 2 để tắt cảnh báo)

    private final AdminUI parent;
    private final JTextField txtExamName;
    private final JSpinner spnTimeLimit; // Ô nhập thời gian
    private final JPanel questionsPanel;
    private final ArrayList<QuestionPanel> questionForms = new ArrayList<>();

    public AddExamUI(AdminUI parent) {
        super(parent, "Tạo bài thi mới", true);
        this.parent = parent;

        setSize(900, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // --- Panel Header ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        headerPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        
        headerPanel.add(new JLabel("Tên bài thi:"));
        txtExamName = new JTextField(30);
        txtExamName.setFont(Theme.getFont(16f));
        headerPanel.add(txtExamName);
        
        headerPanel.add(new JLabel("Thời gian (phút):"));
        spnTimeLimit = new JSpinner(new SpinnerNumberModel(30, 1, 120, 1)); 
        headerPanel.add(spnTimeLimit);
        
        ModernButton btnImport = new ModernButton("Import Excel...", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        btnImport.setFont(Theme.getFont(14f));
        btnImport.addActionListener(e -> importFromExcel());
        headerPanel.add(btnImport);
        
        add(headerPanel, BorderLayout.NORTH);

        questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(questionsPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        ModernButton btnCancel = new ModernButton("Hủy", Theme.BTN_DANGER, Theme.BTN_DANGER_HOVER, Theme.BTN_DANGER);
        ModernButton btnAddQuestion = new ModernButton("Thêm câu hỏi", Theme.BTN_SECONDARY, Theme.BTN_SECONDARY_HOVER, Theme.BTN_SECONDARY);
        ModernButton btnSaveExam = new ModernButton("Lưu bài thi", Theme.BTN_PRIMARY, Theme.BTN_PRIMARY_HOVER, Theme.BTN_PRIMARY);

        btnCancel.addActionListener(e -> dispose());
        btnAddQuestion.addActionListener(e -> addNewQuestionPanel());
        btnSaveExam.addActionListener(e -> saveExam());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnAddQuestion);
        buttonPanel.add(btnSaveExam);
        add(buttonPanel, BorderLayout.SOUTH);
        
        addNewQuestionPanel();
    }
    
    private void importFromExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file câu hỏi Excel (.xlsx)");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Import file sẽ xóa tất cả câu hỏi hiện tại (nếu có).\nBạn có muốn tiếp tục?",
                "Xác nhận Import", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            
            questionForms.clear();
            questionsPanel.removeAll();
            int importedCount = 0;
            
            try (FileInputStream fis = new FileInputStream(selectedFile);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                Sheet sheet = workbook.getSheetAt(0); 
                
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) { 
                        continue; 
                    }

                    // Khai báo biến ra ngoài để dùng trong catch
                    String q = "", a = "", b = "", c = "", d = "", correctStr = "";
                    int currentRowNum = row.getRowNum() + 1; // Số hàng thực tế (bắt đầu từ 1)
                    
                    try {
                        q = getStringFromCell(row.getCell(0)); // Cột A
                        a = getStringFromCell(row.getCell(1)); // Cột B
                        b = getStringFromCell(row.getCell(2)); // Cột C
                        c = getStringFromCell(row.getCell(3)); // Cột D
                        d = getStringFromCell(row.getCell(4)); // Cột E
                        correctStr = getStringFromCell(row.getCell(5)); // Cột F

                        // Kiểm tra nếu hàng rỗng thì bỏ qua
                        if (q.isEmpty() && a.isEmpty() && b.isEmpty() && c.isEmpty() && d.isEmpty()) {
                            continue;
                        }
                        
                        int correct = Integer.parseInt(correctStr.trim());
                        if (correct < 1 || correct > 4) throw new NumberFormatException("Đáp án phải từ 1-4");
                        
                        addNewQuestionPanel(q, a, b, c, d, correct);
                        importedCount++;

                    } catch (NumberFormatException nfe) {
                        // --- THAY ĐỔI: TẠO THÔNG BÁO LỖI DEBUG ---
                        // Hiển thị chính xác những gì code đã đọc
                        String debugMessage = String.format(
                            "Lỗi tại Hàng %d (trong file Excel):\n\n" +
                            "Code đã đọc được:\n" +
                            " - Cột A (Câu hỏi): %s\n" +
                            " - Cột B (Đáp án A): %s\n" +
                            " - Cột C (Đáp án B): %s\n" +
                            " - Cột D (Đáp án C): %s\n" +
                            " - Cột E (Đáp án D): %s\n" +
                            " - Cột F (Đáp án đúng): '%s'\n\n" +
                            "Nguyên nhân: Không thể chuyển '%s' thành số (1-4).\n" +
                            "Câu hỏi này sẽ bị bỏ qua.",
                            currentRowNum, q, a, b, c, d, correctStr, correctStr
                        );
                        
                        JOptionPane.showMessageDialog(this, debugMessage, "Lỗi định dạng chi tiết", JOptionPane.ERROR_MESSAGE);
                        // --- KẾT THÚC THAY ĐỔI ---
                        
                    } catch (Exception e) {
                         JOptionPane.showMessageDialog(this, "Lỗi xử lý hàng " + currentRowNum + ": " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Import thành công " + importedCount + " câu hỏi.", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + e.getMessage(), "Lỗi File", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                 e.printStackTrace();
                 JOptionPane.showMessageDialog(this, "Lỗi xử lý file Excel: " + e.getMessage(), "Lỗi chung", JOptionPane.ERROR_MESSAGE);
            }
            
            questionsPanel.revalidate();
            questionsPanel.repaint();
        }
    }

    /**
     * Hàm hỗ trợ để đọc giá trị từ Cell an toàn (tránh lỗi CellType)
     */
    private String getStringFromCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (cell.getNumericCellValue() == (int) cell.getNumericCellValue()) {
                    return String.valueOf((int) cell.getNumericCellValue());
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    try {
                         if (cell.getNumericCellValue() == (int) cell.getNumericCellValue()) {
                            return String.valueOf((int) cell.getNumericCellValue());
                         } else {
                            return String.valueOf(cell.getNumericCellValue());
                         }
                    } catch (Exception ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }
    
    /**
     * (Overload) Thêm một QuestionPanel mới VỚI dữ liệu cho trước (dùng cho import)
     */
    private void addNewQuestionPanel(String q, String a, String b, String c, String d, int correct) {
        QuestionPanel qp = new QuestionPanel(this, questionForms.size() + 1);
        qp.setData(q, a, b, c, d, correct);
        questionForms.add(qp);
        questionsPanel.add(qp);
    }

    /**
     * Thêm một QuestionPanel mới RỖNG (dùng cho nút "Thêm câu hỏi")
     */
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
        int timeLimit = (int) spnTimeLimit.getValue();

        try (Connection conn = Server.getConnection()) {
            String examSql = "INSERT INTO exams (exam_name, time_limit_minutes) VALUES (?, ?)";
            PreparedStatement examStmt = conn.prepareStatement(examSql, Statement.RETURN_GENERATED_KEYS);
            examStmt.setString(1, examName);
            examStmt.setInt(2, timeLimit); 
            examStmt.executeUpdate();

            ResultSet generatedKeys = examStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int examId = generatedKeys.getInt(1);
                String questionSql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement questionStmt = conn.prepareStatement(questionSql);
                
                for (QuestionPanel qp : questionForms) {
                    if (qp.getQuestionText().trim().isEmpty()) {
                        continue; 
                    }
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

    /**
     * Lớp nội bộ (inner class) đại diện cho form nhập 1 câu hỏi
     */
    private static class QuestionPanel extends JPanel {
        private JTextField txtQuestion, txtOptA, txtOptB, txtOptC, txtOptD;
        private JSpinner spnCorrect;
        private TitledBorder titledBorder;

        QuestionPanel(AddExamUI parentUI, int number) {
            titledBorder = BorderFactory.createTitledBorder("Câu hỏi " + number);
            
            // --- SỬA LỖI: Dùng setTitleFont() ---
            titledBorder.setTitleFont(Theme.getBoldFont(14f)); 
            titledBorder.setTitleColor(Theme.TEXT_ACCENT);
            
            setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 5, 5, 5), // Khoảng cách bên ngoài
                titledBorder // Viền tiêu đề
            ));
            
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0; 

            JButton btnDelete = new JButton("Xóa");
            btnDelete.setBackground(Theme.BTN_DANGER);
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setFont(Theme.getFont(12f));
            btnDelete.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(parentUI, "Bạn có chắc muốn xóa câu hỏi này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    parentUI.removeQuestionPanel(this);
                }
            });

            // Hàng 1: Câu hỏi và Nút Xóa
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1; add(new JLabel("Câu hỏi:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.8; txtQuestion = new JTextField(40); add(txtQuestion, gbc);
            gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.1; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST; add(btnDelete, gbc);
            
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.CENTER;

            // Hàng 2: Đáp án A, B
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1; add(new JLabel("Đáp án A:"), gbc);
            gbc.gridx = 1; gbc.weightx = 0.4; txtOptA = new JTextField(20); add(txtOptA, gbc);
            gbc.gridx = 2; gbc.weightx = 0.1; add(new JLabel("Đáp án B:"), gbc);
            gbc.gridx = 3; gbc.weightx = 0.4; txtOptB = new JTextField(20); add(txtOptB, gbc);

            // Hàng 3: Đáp án C, D
            gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.1; add(new JLabel("Đáp án C:"), gbc);
            gbc.gridx = 1; gbc.weightx = 0.4; txtOptC = new JTextField(20); add(txtOptC, gbc);
            gbc.gridx = 2; gbc.weightx = 0.1; add(new JLabel("Đáp án D:"), gbc);
            gbc.gridx = 3; gbc.weightx = 0.4; txtOptD = new JTextField(20); add(txtOptD, gbc);

            // Hàng 4: Đáp án đúng
            gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.1; add(new JLabel("Đáp án đúng (1-4):"), gbc);
            gbc.gridx = 1; gbc.weightx = 0.4; spnCorrect = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1)); add(spnCorrect, gbc);
        }
        
        public void setData(String q, String a, String b, String c, String d, int correct) {
            txtQuestion.setText(q);
            txtOptA.setText(a);
            txtOptB.setText(b);
            txtOptC.setText(c);
            txtOptD.setText(d);
            spnCorrect.setValue(correct);
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