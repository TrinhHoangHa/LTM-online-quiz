package tcp;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {
	private static final long serialVersionUID = 1L;
    private Color normalColor;
    private Color hoverColor;
    private Color selectedColor;

    private boolean isSelected = false;
    private boolean interactionsEnabled = true;

    public ModernButton(String text, Color normal, Color hover, Color selected) {
        super(text);
        this.normalColor = normal;
        this.hoverColor = hover;
        this.selectedColor = selected;

        setFont(Theme.getBoldFont(16f));
        setForeground(Color.WHITE);
        setBackground(normalColor);
        setBorder(new EmptyBorder(15, 25, 15, 25));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (interactionsEnabled) {
                    setBackground(hoverColor);
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (interactionsEnabled) {
                    if (isSelected) {
                        setBackground(selectedColor);
                    } else {
                        setBackground(normalColor);
                    }
                    repaint();
                }
            }
        });
    }
    
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        if (selected) {
            setBackground(selectedColor);
        } else {
            setBackground(normalColor);
        }
        repaint();
    }

    public void setInteractionsEnabled(boolean enabled) {
        this.interactionsEnabled = enabled;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        
        super.paintComponent(g);
    }
}