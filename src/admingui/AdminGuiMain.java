package admingui;

import javax.swing.SwingUtilities;

/** Entry point for the admin-facing colony management GUI. */
public final class AdminGuiMain {

    private AdminGuiMain() {
    }

    public static void main(String[] args) {
        Theme.installLookAndFeel();
        SwingUtilities.invokeLater(() -> new AdminConsole().setVisible(true));
    }
}
