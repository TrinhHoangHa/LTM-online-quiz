package tcp;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.*;

public class DecoratedPanel extends JPanel {
    private Color startColor;
    private Color endColor;
    private int cornerRadius;
    private Image stickerImage;

    public DecoratedPanel(LayoutManager layout, int radius, Color start, Color end, String stickerPath) {
        super(layout);
        this.cornerRadius = radius;
        this.startColor = start;
        this.endColor = end;
        setOpaque(false);

        try {
            ImageIcon icon = new ImageIcon(DecoratedPanel.class.getResource(stickerPath));
            this.stickerImage = icon.getImage();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Không thể tải sticker: " + stickerPath);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
        g2d.setPaint(gp);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        
        if (stickerImage != null) {
            int padding = 20;
            int x = getWidth() - stickerImage.getWidth(null) - padding;
            int y = padding;
            g2d.drawImage(stickerImage, x, y, null);
        }

        g2d.dispose();
    }
}