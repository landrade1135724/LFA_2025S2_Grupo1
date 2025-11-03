package app;

import app.gui.AppFrame;

public class GuiMain {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new AppFrame().setVisible(true);
        });
    }
}
