package admingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import agents.Agent;
import enums.Capability;
import enums.OperationalStatus;
import facilities.Equipment;
import roboticunit.Drone;
import roboticunit.ExplorationRover;
import roboticunit.RepairRobot;
import roboticunit.RoboticUnit;

/** Lists all robotic units and lets the admin add new ones or edit an existing one. */
final class RoboticUnitsPanel extends JPanel implements Refreshable {

    private final SimulationContext context;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Type", "Battery", "Health", "Location", "Available", "Capabilities", "Equipment", "Mission" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<RoboticUnit> currentRows = List.of();

    RoboticUnitsPanel(SimulationContext context) {
        super(new BorderLayout(6, 6));
        this.context = context;
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setBackground(Theme.BACKGROUND);
        Theme.zebraStripe(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(Theme.BACKGROUND);
        JButton addButton = new JButton("Add Robotic Unit...");
        Theme.styleAccentButton(addButton, Theme.TEAL);
        addButton.addActionListener(e -> openAddDialog());
        JButton makeAvailableButton = new JButton("Force Make Available");
        Theme.styleAccentButton(makeAvailableButton, Theme.SKY);
        makeAvailableButton.addActionListener(e -> forceMakeAvailable());
        buttons.add(addButton);
        buttons.add(makeAvailableButton);
        buttons.add(new JLabel("  (double-click a row to edit)"));
        add(buttons, BorderLayout.SOUTH);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < currentRows.size()) {
                        openEditDialog(currentRows.get(row));
                    }
                }
            }
        });
    }

    private void forceMakeAvailable() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentRows.size()) {
            JOptionPane.showMessageDialog(this, "Select a robotic unit first.");
            return;
        }
        RoboticUnit unit = currentRows.get(row);
        unit.makeAvailable();
        context.pushEffect("FREED", unit.getCurrentLocation(), Theme.SKY);
        context.log("Admin forced a " + unit.getClass().getSimpleName() + " back to available.");
        context.notifyChanged();
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this), "Add Robotic Unit",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        Theme.styleDialog(dialog);

        JComboBox<String> typeCombo = new JComboBox<>(new String[] { "ExplorationRover", "RepairRobot", "Drone" });
        JSpinner batterySpinner = new JSpinner(new SpinnerNumberModel(100.0, 0.0, 100.0, 1.0));
        JTextField healthField = new JTextField("Operational");
        JComboBox<String> locationCombo = new JComboBox<>(locationOptions());
        locationCombo.setEditable(true);
        JCheckBox availableBox = new JCheckBox("Available", true);
        JCheckBox addSurveyKit = new JCheckBox("Attach an operational 'Survey Kit' equipment item", true);

        JPanel capabilitiesPanel = new JPanel(new GridLayout(0, 1));
        java.util.Map<Capability, JCheckBox> capabilityBoxes = new java.util.EnumMap<>(Capability.class);
        for (Capability capability : Capability.values()) {
            JCheckBox box = new JCheckBox(capability.name());
            capabilityBoxes.put(capability, box);
            capabilitiesPanel.add(box);
        }
        capabilityBoxes.get(Capability.SURFACE_TRAVEL).setSelected(true);
        capabilityBoxes.get(Capability.EQUIPMENT_CARRYING).setSelected(true);
        capabilityBoxes.get(Capability.EXPLORATION).setSelected(true);
        JLabel capabilitiesLabel = new JLabel("Capabilities (ExplorationRover only):");

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Type:"));
        form.add(typeCombo);
        form.add(new JLabel("Battery level (0-100):"));
        form.add(batterySpinner);
        form.add(new JLabel("Health status:"));
        form.add(healthField);
        form.add(new JLabel("Location:"));
        form.add(locationCombo);
        form.add(new JLabel("Availability:"));
        form.add(availableBox);
        form.add(new JLabel("Survey kit:"));
        form.add(addSurveyKit);
        form.add(capabilitiesLabel);
        form.add(capabilitiesPanel);

        Runnable syncCapabilityVisibility = () -> {
            boolean isRover = "ExplorationRover".equals(typeCombo.getSelectedItem());
            capabilitiesPanel.setVisible(isRover);
            capabilitiesLabel.setVisible(isRover);
            addSurveyKit.setVisible(isRover);
        };
        typeCombo.addActionListener(e -> syncCapabilityVisibility.run());
        syncCapabilityVisibility.run();

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton create = new JButton("Create");
        JButton cancel = new JButton("Cancel");
        footer.add(create);
        footer.add(cancel);
        dialog.add(footer, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dialog.dispose());
        create.addActionListener(e -> {
            try {
                double battery = (Double) batterySpinner.getValue();
                String health = healthField.getText().trim();
                String location = ((String) locationCombo.getEditor().getItem()).trim();
                boolean available = availableBox.isSelected();
                String type = (String) typeCombo.getSelectedItem();

                RoboticUnit unit;
                switch (type) {
                    case "Drone" -> unit = new Drone(battery, health, location, available);
                    case "RepairRobot" -> unit = new RepairRobot(battery, health, location, available);
                    default -> {
                        Set<Capability> selected = EnumSet.noneOf(Capability.class);
                        for (var entry : capabilityBoxes.entrySet()) {
                            if (entry.getValue().isSelected()) {
                                selected.add(entry.getKey());
                            }
                        }
                        ExplorationRover rover = new ExplorationRover(battery, health, location, available, selected);
                        if (addSurveyKit.isSelected()) {
                            rover.setEquipment(List.of(new Equipment("Survey Kit", OperationalStatus.OPERATIONAL)));
                        }
                        unit = rover;
                    }
                }

                context.colony().addRobot(unit);
                context.pushEffect("DEPLOYED", location, Theme.LEAF);
                context.log("Added " + type + " at " + location + ".");
                context.notifyChanged();
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Cannot add robotic unit", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openEditDialog(RoboticUnit unit) {
        JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this),
                "Edit " + unit.getClass().getSimpleName(), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        Theme.styleDialog(dialog);

        JSpinner batterySpinner = new JSpinner(new SpinnerNumberModel(unit.getBatteryLevel(), 0.0, 100.0, 1.0));
        JTextField healthField = new JTextField(unit.getHealthStatus());
        JTextField locationField = new JTextField(unit.getCurrentLocation());
        JCheckBox availableBox = new JCheckBox("Available", unit.getAvailability());

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Battery level:"));
        form.add(batterySpinner);
        form.add(new JLabel("Health status:"));
        form.add(healthField);
        form.add(new JLabel("Current location:"));
        form.add(locationField);
        form.add(new JLabel("Availability:"));
        form.add(availableBox);
        dialog.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        footer.add(save);
        footer.add(cancel);
        dialog.add(footer, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            try {
                unit.setBatteryLevel((Double) batterySpinner.getValue());
                unit.setHealthStatus(healthField.getText().trim());
                unit.setCurrentLocation(locationField.getText().trim());
                unit.setAvailability(availableBox.isSelected());
                context.log("Updated robotic unit record (" + unit.getClass().getSimpleName() + ").");
                context.notifyChanged();
                dialog.dispose();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Cannot update", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String[] locationOptions() {
        Set<String> locations = new java.util.LinkedHashSet<>();
        for (var facility : context.colony().getFacilities()) {
            locations.add(facility.getLocation());
        }
        locations.add("Martian Surface");
        return locations.toArray(new String[0]);
    }

    @Override
    public void refresh() {
        int selectedRow = table.getSelectedRow();
        RoboticUnit previouslySelected = selectedRow >= 0 && selectedRow < currentRows.size()
                ? currentRows.get(selectedRow) : null;

        currentRows = context.colony().getRoboticUnits();
        model.setRowCount(0);
        for (RoboticUnit unit : currentRows) {
            Agent agent = unit;
            model.addRow(new Object[] {
                    unit.getClass().getSimpleName(),
                    String.format("%.1f", unit.getBatteryLevel()),
                    unit.getHealthStatus(),
                    unit.getCurrentLocation(),
                    agent.isAvailable() ? "Yes" : "Busy",
                    unit.getCapabilities(),
                    unit.getEquipment().size(),
                    unit.getMission() == null ? "-" : unit.getMission().getClass().getSimpleName()
            });
        }

        if (previouslySelected != null) {
            int restoredIndex = currentRows.indexOf(previouslySelected);
            if (restoredIndex >= 0) {
                table.setRowSelectionInterval(restoredIndex, restoredIndex);
            }
        }
    }
}
