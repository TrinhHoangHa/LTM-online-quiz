package tcp;

import javax.swing.ImageIcon;
import java.awt.*;

public class ImagePanel extends RoundedPanel {
	private static final long serialVersionUID = 1L;
    private Image stickerImage;

    public ImagePanel(LayoutManager layout, int radius, Color color, String imagePath) {
        super(layout, radius, color);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            this.stickerImage = icon.getImage();
        } catch (Exception e) {
            System.err.println("Không thể tải hình ảnh trang trí: " + imagePath);
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (stickerImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            
            // THAY ĐỔI: Độ mờ của ảnh. 1.0f là rõ 100%.
            // Bạn có thể thay đổi giá trị này, ví dụ 0.5f để mờ 50%.
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            int x = (getWidth() - stickerImage.getWidth(this)) / 2;
            int y = (getHeight() - stickerImage.getHeight(this)) / 2;

            g2d.drawImage(stickerImage, x, y, this);

            g2d.dispose();
        }
    }
}