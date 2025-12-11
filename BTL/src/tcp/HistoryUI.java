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
	private static final long serialVersionUID = 1L;
    public HistoryUI(int examId, String username) {
        setTitle("Lịch sử làm bài của " + username);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.PANEL_BACKGROUND);
        setContentPane(mainPanel);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        Icon historyIcon = null;
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/trophy.png"));
            Image scaledImage = originalIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            historyIcon = new ImageIcon(scaledImage);
        } catch (Exception e) {
            System.err.println("Không tìm thấy icon: /images/trophy.png");
        }

        JLabel lblTitle = new JLabel("Lịch sử làm bài");
        lblTitle.setFont(Theme.getBoldFont(24f));
        lblTitle.setForeground(Theme.TEXT_ACCENT);

        if (historyIcon != null) {
            titlePanel.add(new JLabel(historyIcon));
        }
        titlePanel.add(lblTitle);
        if (historyIcon != null) {
            titlePanel.add(new JLabel(historyIcon));
        }
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Ngày làm", "Điểm", "Tổng số câu"}, 0);
        JTable table = new JTable(model);

        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        loadHistory(examId, username, model);
        setVisible(true);
    }
    
    // --- THAY ĐỔI: Cập nhật styleTable để có màu xen kẽ ---
    private void styleTable(JTable table) {
        table.setFont(Theme.getFont(14f));
        table.setRowHeight(35);
        table.setGridColor(Theme.BORDER_COLOR);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.getBoldFont(16f));
        header.setBackground(Theme.PRIMARY_BACKGROUND);
        header.setForeground(Theme.TEXT_LIGHT);
        header.setReorderingAllowed(false);

        // Renderer tùy chỉnh cho màu xen kẽ và căn giữa
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : Theme.INPUT_BACKGROUND);
                }
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        };

        for(int i=0; i < table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
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