package admingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import emergencies.Emergency;
import emergencies.Fire;
import emergencies.medicalEmergency;
import emergencies.OxygenLeak;
import emergencies.RoverBreakdown;
import facilities.Facilities;
import facilities.Habitat;
import facilities.Laboratory;
import personnel.Personnel;
import roboticunit.ExplorationRover;
import roboticunit.RoboticUnit;

/** Lets the admin trigger an emergency and see how the colony responded. */
final class EmergenciesPanel extends JPanel implements Refreshable {

    private final SimulationContext context;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Type", "Location", "Status", "Reason" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    EmergenciesPanel(SimulationContext context) {
        super(new BorderLayout(6, 6));
        this.context = context;
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setBackground(Theme.BACKGROUND);
        Theme.zebraStripe(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(Theme.BACKGROUND);
        JButton trigger = new JButton("New Emergency...");
        Theme.styleAccentButton(trigger, Theme.AMBER);
        trigger.addActionListener(e -> openTriggerDialog());
        buttons.add(trigger);
        add(buttons, BorderLayout.SOUTH);
    }

    private void openTriggerDialog() {
        JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this), "New Emergency",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        Theme.styleDialog(dialog);

        JComboBox<String> typeCombo = new JComboBox<>(new String[] { "Fire", "OxygenLeak", "MedicalEmergency", "RoverBreakdown" });

        JComboBox<Laboratory> labCombo = new JComboBox<>();
        for (Facilities f : context.colony().getFacilities()) {
            if (f instanceof Laboratory lab) {
                labCombo.addItem(lab);
            }
        }
        JComboBox<Habitat> habitatCombo = new JComboBox<>();
        for (Facilities f : context.colony().getFacilities()) {
            if (f instanceof Habitat habitat) {
                habitatCombo.addItem(habitat);
            }
        }
        JComboBox<Personnel> personCombo = new JComboBox<>();
        for (Personnel p : context.colony().getPersonnel()) {
            personCombo.addItem(p);
        }
        JComboBox<ExplorationRover> roverCombo = new JComboBox<>();
        for (RoboticUnit unit : context.colony().getRoboticUnits()) {
            if (unit instanceof ExplorationRover rover) {
                roverCombo.addItem(rover);
            }
        }
        JSpinner oxygenLossSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 100.0, 0.5));

        labCombo.setRenderer(labelRenderer(f -> ((Laboratory) f).getLocation()));
        habitatCombo.setRenderer(labelRenderer(f -> ((Habitat) f).getLocation()));
        personCombo.setRenderer(labelRenderer(p -> ((Personnel) p).getName() + " @ " + ((Personnel) p).getCurrentLocation()));
        roverCombo.setRenderer(labelRenderer(r -> "ExplorationRover @ " + ((ExplorationRover) r).getCurrentLocation()));

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Emergency type:"));
        form.add(typeCombo);

        JLabel labLabel = new JLabel("Laboratory affected:");
        JLabel habitatLabel = new JLabel("Habitat affected:");
        JLabel oxygenLabel = new JLabel("Oxygen loss amount:");
        JLabel personLabel = new JLabel("Patient:");
        JLabel roverLabel = new JLabel("Rover affected:");

        form.add(labLabel);
        form.add(labCombo);
        form.add(habitatLabel);
        form.add(habitatCombo);
        form.add(oxygenLabel);
        form.add(oxygenLossSpinner);
        form.add(personLabel);
        form.add(personCombo);
        form.add(roverLabel);
        form.add(roverCombo);

        dialog.add(form, BorderLayout.CENTER);

        Runnable syncVisibility = () -> {
            String type = (String) typeCombo.getSelectedItem();
            boolean isFire = "Fire".equals(type);
            boolean isLeak = "OxygenLeak".equals(type);
            boolean isMedical = "MedicalEmergency".equals(type);
            boolean isRover = "RoverBreakdown".equals(type);
            labLabel.setVisible(isFire);
            labCombo.setVisible(isFire);
            habitatLabel.setVisible(isLeak);
            habitatCombo.setVisible(isLeak);
            oxygenLabel.setVisible(isLeak);
            oxygenLossSpinner.setVisible(isLeak);
            personLabel.setVisible(isMedical);
            personCombo.setVisible(isMedical);
            roverLabel.setVisible(isRover);
            roverCombo.setVisible(isRover);
        };
        typeCombo.addActionListener(e -> syncVisibility.run());
        syncVisibility.run();

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton triggerButton = new JButton("Trigger");
        JButton cancel = new JButton("Cancel");
        footer.add(triggerButton);
        footer.add(cancel);
        dialog.add(footer, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dialog.dispose());
        triggerButton.addActionListener(e -> {
            try {
                String type = (String) typeCombo.getSelectedItem();
                Emergency emergency = switch (type) {
                    case "Fire" -> {
                        Laboratory lab = (Laboratory) labCombo.getSelectedItem();
                        if (lab == null) {
                            throw new IllegalArgumentException("No laboratory available for a fire.");
                        }
                        yield new Fire(lab);
                    }
                    case "OxygenLeak" -> {
                        Habitat habitat = (Habitat) habitatCombo.getSelectedItem();
                        if (habitat == null) {
                            throw new IllegalArgumentException("No habitat available for an oxygen leak.");
                        }
                        yield new OxygenLeak(habitat, (Double) oxygenLossSpinner.getValue());
                    }
                    case "MedicalEmergency" -> {
                        Personnel patient = (Personnel) personCombo.getSelectedItem();
                        if (patient == null) {
                            throw new IllegalArgumentException("No personnel available to be the patient.");
                        }
                        yield new medicalEmergency(patient);
                    }
                    default -> {
                        ExplorationRover rover = (ExplorationRover) roverCombo.getSelectedItem();
                        if (rover == null) {
                            throw new IllegalArgumentException("No exploration rover available to break down.");
                        }
                        yield new RoverBreakdown(rover);
                    }
                };

                boolean handled = context.colony().handleEmergency(emergency);
                context.notifyEmergencyTriggered(emergency.getLocation());
                context.pushEffect(emergencyBanner(type), emergency.getLocation(),
                        handled ? Theme.LEAF : Theme.ROSE);
                context.log(type + " triggered: " + (handled ? "HANDLED" : "NOT HANDLED")
                        + " | status=" + emergency.getStatus()
                        + (emergency.getFailureReason() != null && !emergency.getFailureReason().isBlank()
                                ? " | " + emergency.getFailureReason() : ""));
                context.notifyChanged();
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Cannot trigger emergency", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String emergencyBanner(String type) {
        return switch (type) {
            case "Fire" -> "FIRE!";
            case "OxygenLeak" -> "OXYGEN LEAK!";
            case "MedicalEmergency" -> "MEDICAL!";
            default -> "ROVER DOWN!";
        };
    }

    @SuppressWarnings("unchecked")
    private <T> javax.swing.ListCellRenderer<T> labelRenderer(java.util.function.Function<Object, String> labelFn) {
        return (list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "" : labelFn.apply(value));
            label.setOpaque(true);
            label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return label;
        };
    }

    @Override
    public void refresh() {
        List<Emergency> emergencies = context.colony().getEmergencies();
        model.setRowCount(0);
        for (Emergency emergency : emergencies) {
            Object location = emergency.getLocation();
            String locationText = location instanceof Facilities facility ? facility.getLocation() : String.valueOf(location);
            model.addRow(new Object[] {
                    emergency.getClass().getSimpleName(),
                    locationText,
                    emergency.getStatus(),
                    emergency.getReason().isBlank() ? "-" : emergency.getReason()
            });
        }
    }
}
