package src.main.java.ecojuego;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import src.main.java.ecojuego.logic.UserProfile;
import src.main.java.ecojuego.ui.EcoGameFrame;
import src.main.java.ecojuego.ui.EcoLoginDialog;

public final class EcoJuegoApp {

    private EcoJuegoApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            installLookAndFeel();
            startSession();
        });
    }

    private static void startSession() {
        EcoLoginDialog dialog = new EcoLoginDialog(null);
        UserProfile profile = dialog.showDialog();
        if (profile == null) {
            System.exit(0);
            return;
        }
        final EcoGameFrame[] frameHolder = new EcoGameFrame[1];
        Runnable logoutAction = () -> SwingUtilities.invokeLater(() -> {
            if (frameHolder[0] != null) {
                frameHolder[0].dispose();
            }
            startSession();
        });
        EcoGameFrame frame = new EcoGameFrame(profile, logoutAction);
        frameHolder[0] = frame;
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void installLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException ignored) {
        }
    }
}
