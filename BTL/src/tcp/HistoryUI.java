package tcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HistoryUI extends JFrame {
    public HistoryUI(int examId, String username) {
        setTitle("Lịch sử làm bài của " + username);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.PANEL_BACKGROUND);
        setContentPane(mainPanel);

        JLabel lblTitle = new JLabel("Lịch sử làm bài", SwingConstants.CENTER);
        lblTitle.setFont(Theme.getBoldFont(24f));
        lblTitle.setForeground(Theme.TEXT_ACCENT);
        lblTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Ngày làm", "Điểm", "Tổng số câu"}, 0);
        JTable table = new JTable(model);

        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        loadHistory(examId, username, model);
        setVisible(true);
    }
    
    private void styleTable(JTable table) {
        table.setFont(Theme.getFont(14f));
        table.setRowHeight(35);
        table.setGridColor(Theme.BORDER_COLOR);
        table.getTableHeader().setFont(Theme.getBoldFont(16f));
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.PRIMARY_BACKGROUND);
        header.setForeground(Theme.TEXT_LIGHT);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i < table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void loadHistory(int examId, String username, DefaultTableModel model) {
        try (Connection conn = Server.getConnection()) {
            String sql = "SELECT taken_at, score, total_questions " +
                         "FROM exam_history WHERE username=? AND exam_id=? ORDER BY taken_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, examId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getTimestamp("taken_at"),
                        rs.getInt("score"),
                        rs.getInt("total_questions")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}