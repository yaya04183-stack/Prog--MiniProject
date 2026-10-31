package admingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import colony.Colony;
import enums.EnviromentStates;
import enums.Mission_Emergency_States;
import enums.Resources;
import missions.Mission;
import simulation.Simulation;

/** Top summary strip: colony-wide resources, environment control, active-mission + time controls. */
final class DashboardPanel extends JPanel implements Refreshable {

    private final SimulationContext context;
    private final Map<Resources, JProgressBar> resourceBars = new EnumMap<>(Resources.class);
    private final JComboBox<EnviromentStates> environmentCombo = new JComboBox<>(EnviromentStates.values());
    private final JLabel activeMissionLabel = new JLabel("Active mission: none");
    private final JLabel simTimeLabel = new JLabel("Sim time: 0");
    private final JCheckBox autoRunCheckBox = new JCheckBox("Auto-advance active mission (1 unit/sec)");
    private boolean updatingCombo;
    private final Timer autoRunTimer;

    DashboardPanel(SimulationContext context) {
        super(new BorderLayout(10, 6));
        this.context = context;
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        setBackground(Theme.BACKGROUND);

        add(buildResourcePanel(), BorderLayout.CENTER);
        add(buildControlPanel(), BorderLayout.EAST);

        environmentCombo.addActionListener(e -> {
            if (updatingCombo) {
                return;
            }
            EnviromentStates selected = (EnviromentStates) environmentCombo.getSelectedItem();
            if (selected != null) {
                context.environment().SetEnvironmentState(selected);
                context.log("Mars environment set to " + selected);
                context.notifyChanged();
            }
        });

        autoRunTimer = new Timer(1000, e -> {
            Mission active = context.colony().getActiveMission();
            if (active != null && active.getState() == Mission_Emergency_States.INPROGRESS) {
                Simulation.advanceTime(context.colony(), 1);
                context.log("Auto-advance: +1 time unit (mission " + active.getClass().getSimpleName() + ")");
                context.notifyChanged();
            }
        });
        autoRunCheckBox.addActionListener(e -> {
            if (autoRunCheckBox.isSelected()) {
                autoRunTimer.start();
            } else {
                autoRunTimer.stop();
            }
        });
    }

    private static final java.util.Map<Resources, java.awt.Color> RESOURCE_COLORS = java.util.Map.of(
            Resources.OXYGEN, Theme.SKY,
            Resources.WATER, Theme.TEAL,
            Resources.FOOD, Theme.LEAF,
            Resources.FUEL, Theme.CORAL,
            Resources.BATTERY_POWER, Theme.AMBER,
            Resources.MEDICAL_SUPPLIES, Theme.ROSE,
            Resources.SPARE_PARTS, Theme.LAVENDER);

    private JPanel buildResourcePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 8, 4));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createTitledBorder("Colony Resources (operational facilities only)"));
        for (Resources resource : Resources.values()) {
            JPanel cell = new JPanel(new BorderLayout(4, 0));
            cell.setBackground(Theme.BACKGROUND);
            JLabel label = new JLabel(resource.name());
            label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
            label.setForeground(Theme.INK);
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setStringPainted(true);
            bar.setForeground(RESOURCE_COLORS.getOrDefault(resource, Theme.TEAL));
            resourceBars.put(resource, bar);
            cell.add(label, BorderLayout.NORTH);
            cell.add(bar, BorderLayout.CENTER);
            panel.add(cell);
        }
        return panel;
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createTitledBorder("Environment / Time / Active Mission"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;

        panel.add(new JLabel("Mars environment:"), c);
        c.gridx = 1;
        panel.add(environmentCombo, c);

        c.gridx = 0;
        c.gridy++;
        panel.add(simTimeLabel, c);
        c.gridx = 1;
        JButton advance1 = new JButton("Advance +1");
        Theme.styleAccentButton(advance1, Theme.TEAL);
        advance1.addActionListener(e -> advanceTime(1));
        panel.add(advance1, c);

        c.gridx = 0;
        c.gridy++;
        JButton advance5 = new JButton("Advance +5");
        Theme.styleAccentButton(advance5, Theme.SKY);
        advance5.addActionListener(e -> advanceTime(5));
        panel.add(advance5, c);
        c.gridx = 1;
        autoRunCheckBox.setBackground(Theme.BACKGROUND);
        panel.add(autoRunCheckBox, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        activeMissionLabel.setFont(activeMissionLabel.getFont().deriveFont(Font.BOLD));
        activeMissionLabel.setForeground(Theme.SKY);
        panel.add(activeMissionLabel, c);

        return panel;
    }

    private void advanceTime(int units) {
        Mission active = context.colony().getActiveMission();
        if (active == null || active.getState() != Mission_Emergency_States.INPROGRESS) {
            context.log("Advance time ignored: no mission currently in progress.");
            context.notifyChanged();
            return;
        }
        Simulation.advanceTime(context.colony(), units);
        context.log("Advanced simulation by " + units + " time unit(s).");
        context.notifyChanged();
    }

    @Override
    public void refresh() {
        Colony colony = context.colony();
        Map<Resources, Double> totals = colony.getColonyResourceTotal();
        for (Map.Entry<Resources, JProgressBar> entry : resourceBars.entrySet()) {
            double amount = totals.getOrDefault(entry.getKey(), 0.0);
            int clamped = (int) Math.max(0, Math.min(100, amount));
            entry.getValue().setValue(clamped);
            entry.getValue().setString(String.format("%.1f", amount));
        }

        updatingCombo = true;
        environmentCombo.setSelectedItem(context.environment().GetEnvironmentState());
        updatingCombo = false;

        simTimeLabel.setText("Sim time: " + Simulation.time);

        Mission active = colony.getActiveMission();
        if (active == null) {
            activeMissionLabel.setText("Active mission: none");
        } else {
            String failure = active.getFailureReason();
            activeMissionLabel.setText(String.format("Active mission: %s | state=%s | %d/%d units%s",
                    active.getClass().getSimpleName(), active.getState(),
                    active.getElapsedTimeUnits(), active.getNeededTimesUnits(),
                    failure != null ? " | " + failure : ""));
        }
    }

    Component asComponent() {
        return this;
    }
}
