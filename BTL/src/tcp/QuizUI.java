package tcp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class QuizUI extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public QuizUI(String username) {
        setTitle("Xin chào " + username);
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"ID", "Tên bài kiểm tra", "Hành động"}, 0);
        table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };

        loadExams();

        table.getColumn("Hành động").setCellRenderer(new ButtonRenderer());
        table.getColumn("Hành động").setCellEditor(new ButtonEditor(new JCheckBox(), this));

        add(new JScrollPane(table), BorderLayout.CENTER);
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
                model.addRow(new Object[]{id, name, "Làm bài"});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openExam(int examId) {
        new DoExamUI(examId);
    }
}

// ===== Renderer & Editor cho JTable =====
class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
    public ButtonRenderer() { setOpaque(true); }
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}

class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private QuizUI parent;
    private int examId;

    public ButtonEditor(JCheckBox checkBox, QuizUI parent) {
        super(checkBox);
        this.parent = parent;
        button = new JButton();
        button.addActionListener(e -> parent.openExam(examId));
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        examId = (int) table.getValueAt(row, 0);
        button.setText("Làm bài");
        return button;
    }

    public Object getCellEditorValue() { return "Làm bài"; }
}
