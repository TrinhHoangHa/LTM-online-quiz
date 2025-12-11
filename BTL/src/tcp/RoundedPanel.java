package tcp;

import javax.swing.JPanel;
import java.awt.*;

public class RoundedPanel extends JPanel {
	private static final long serialVersionUID = 1L;
    private Color startColor;
    private Color endColor;
    private int cornerRadius;

    public RoundedPanel(LayoutManager layout, int radius, Color start, Color end) {
        super(layout);
        this.cornerRadius = radius;
        this.startColor = start;
        this.endColor = end;
        setOpaque(false);
    }
    
     public RoundedPanel(LayoutManager layout, int radius, Color color) {
        this(layout, radius, color, color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, startColor, 0, height, endColor);
        g2d.setPaint(gp);
        g2d.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
    }
}