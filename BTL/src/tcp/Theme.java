package tcp;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;

public class Theme {
    public static final Color PRIMARY_BACKGROUND = new Color(0x46178F);
    public static final Color SECONDARY_BACKGROUND = new Color(0x3B1A69);
    public static final Color PANEL_BACKGROUND = new Color(0xFFFFFF);
    public static final Color TEXT_LIGHT = Color.WHITE;
    public static final Color TEXT_DARK = new Color(0x333333);
    public static final Color TEXT_ACCENT = new Color(0x46178F);
    public static final Color BTN_PRIMARY = new Color(0x24C277);
    public static final Color BTN_PRIMARY_HOVER = new Color(0x20AB6A);
    public static final Color BTN_SECONDARY = new Color(0xFFC700);
    public static final Color BTN_SECONDARY_HOVER = new Color(0xE6B300);
    public static final Color BTN_DANGER = new Color(0xE74C3C);
    public static final Color BTN_DANGER_HOVER = new Color(0xC0392B);
    public static final Color INPUT_BACKGROUND = new Color(0xF2F2F2);
    public static final Color BORDER_COLOR = new Color(0xCCCCCC);

    public static Font getFont(float size) {
        try {
            InputStream is = Theme.class.getResourceAsStream("/fonts/Poppins-Regular.ttf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(size);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("SansSerif", Font.PLAIN, (int) size);
        }
    }
    
    public static Font getBoldFont(float size) {
         try {
            InputStream is = Theme.class.getResourceAsStream("/fonts/Poppins-Bold.ttf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(size);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("SansSerif", Font.BOLD, (int) size);
        }
    }
}