package admingui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Shared dark, high-contrast color palette + styling helpers for the whole console.
 * Deliberately avoids Nimbus: Nimbus derives most widget colors from its own synth
 * style and largely ignores plain UIManager color keys, which is what made text
 * unreadable before. The default Metal-family look and feel honors these keys
 * directly, so overriding them here reliably reaches every component.
 */
final class Theme {

    static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);

    static final Color BACKGROUND = new Color(0x1E, 0x23, 0x29);
    static final Color PANEL = new Color(0x28, 0x2E, 0x36);
    static final Color PANEL_ALT = new Color(0x32, 0x3A, 0x44);
    static final Color INK = new Color(0xED, 0xF1, 0xF5);
    static final Color MUTED_INK = new Color(0xA9, 0xB4, 0xBF);

    static final Color TEAL = new Color(0x3F, 0xBF, 0x9F);
    static final Color CORAL = new Color(0xFF, 0x9B, 0x73);
    static final Color AMBER = new Color(0xF2, 0xC1, 0x4E);
    static final Color SKY = new Color(0x5B, 0xA8, 0xE0);
    static final Color LAVENDER = new Color(0xA9, 0x8C, 0xF0);
    static final Color LEAF = new Color(0x79, 0xCB, 0x7C);
    static final Color ROSE = new Color(0xE8, 0x6A, 0x7F);

    /** Friendly cool blue used for selection highlights everywhere instead of a warm/red tone. */
    static final Color SELECTION = SKY;

    static final Color SURFACE_SAND = new Color(0xE0, 0xB4, 0x72);
    static final Color SURFACE_PRIMARY = new Color(0x2B, 0x58, 0x6E);

    private Theme() {
    }

    static void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignoredLookAndFeelUnavailable) {
            // Falls back to whatever is already active.
        }

        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Panel.foreground", INK);
        UIManager.put("Label.foreground", INK);
        UIManager.put("Label.font", BASE_FONT);

        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("OptionPane.messageForeground", INK);
        UIManager.put("OptionPane.font", BASE_FONT);

        UIManager.put("Button.background", PANEL_ALT);
        UIManager.put("Button.foreground", INK);
        UIManager.put("Button.font", BOLD_FONT);
        UIManager.put("ToggleButton.background", PANEL_ALT);
        UIManager.put("ToggleButton.foreground", INK);
        UIManager.put("CheckBox.background", BACKGROUND);
        UIManager.put("CheckBox.foreground", INK);
        UIManager.put("CheckBox.font", BASE_FONT);
        UIManager.put("RadioButton.background", BACKGROUND);
        UIManager.put("RadioButton.foreground", INK);

        UIManager.put("ComboBox.background", PANEL_ALT);
        UIManager.put("ComboBox.foreground", INK);
        UIManager.put("ComboBox.selectionBackground", SELECTION);
        UIManager.put("ComboBox.selectionForeground", Color.BLACK);
        UIManager.put("ComboBox.font", BASE_FONT);

        UIManager.put("TextField.background", PANEL_ALT);
        UIManager.put("TextField.foreground", INK);
        UIManager.put("TextField.caretForeground", INK);
        UIManager.put("TextField.font", BASE_FONT);
        UIManager.put("TextArea.background", PANEL_ALT);
        UIManager.put("TextArea.foreground", INK);
        UIManager.put("TextArea.font", BASE_FONT);

        UIManager.put("Spinner.background", PANEL_ALT);
        UIManager.put("Spinner.foreground", INK);
        UIManager.put("FormattedTextField.background", PANEL_ALT);
        UIManager.put("FormattedTextField.foreground", INK);

        UIManager.put("Table.background", PANEL);
        UIManager.put("Table.foreground", INK);
        UIManager.put("Table.font", BASE_FONT);
        UIManager.put("Table.gridColor", new Color(0x3D, 0x45, 0x4E));
        UIManager.put("Table.selectionBackground", SELECTION);
        UIManager.put("Table.selectionForeground", Color.BLACK);
        UIManager.put("TableHeader.background", SURFACE_PRIMARY);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TableHeader.font", BOLD_FONT);

        UIManager.put("ScrollPane.background", BACKGROUND);
        UIManager.put("Viewport.background", PANEL);

        UIManager.put("TabbedPane.background", AMBER);
        UIManager.put("TabbedPane.foreground", Color.BLACK);
        UIManager.put("TabbedPane.selected", SURFACE_SAND);
        UIManager.put("TabbedPane.font", BOLD_FONT);
        UIManager.put("TabbedPane.contentAreaColor", BACKGROUND);

        UIManager.put("TitledBorder.titleColor", AMBER);
        UIManager.put("TitledBorder.font", BOLD_FONT);

        UIManager.put("ProgressBar.background", PANEL_ALT);
        UIManager.put("ProgressBar.foreground", TEAL);
        UIManager.put("ProgressBar.selectionBackground", INK);
        UIManager.put("ProgressBar.selectionForeground", INK);
        UIManager.put("ProgressBar.font", BOLD_FONT);

        UIManager.put("List.background", PANEL_ALT);
        UIManager.put("List.foreground", INK);
        UIManager.put("List.selectionBackground", SELECTION);
        UIManager.put("List.selectionForeground", Color.BLACK);

        UIManager.put("SplitPane.background", BACKGROUND);
        UIManager.put("ToolTip.background", PANEL_ALT);
        UIManager.put("ToolTip.foreground", INK);
    }

    static void styleDialog(JDialog dialog) {
        dialog.getContentPane().setBackground(BACKGROUND);
    }

    static void styleAccentButton(JButton button, Color accent) {
        button.setBackground(accent);
        button.setForeground(Color.BLACK);
        button.setFont(BOLD_FONT);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
    }

    static void zebraStripe(JTable table) {
        table.setFont(BASE_FONT);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? PANEL : PANEL_ALT);
                    c.setForeground(INK);
                } else {
                    c.setBackground(SELECTION);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
        table.setRowHeight(24);
        table.setGridColor(new Color(0x3D, 0x45, 0x4E));
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(Color.BLACK);
        table.setBackground(PANEL);
        table.setForeground(INK);
        table.getTableHeader().setBackground(SURFACE_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(BOLD_FONT);
    }

    static Color statusTint(String statusName) {
        return switch (statusName) {
            case "OPERATIONAL" -> LEAF;
            case "DAMAGED" -> AMBER;
            case "UNAVAILABLE" -> MUTED_INK;
            case "DESTROYED" -> ROSE;
            default -> SKY;
        };
    }
}
