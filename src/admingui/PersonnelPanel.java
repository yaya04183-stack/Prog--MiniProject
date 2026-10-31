package admingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import enums.Skills;
import personnel.Engineer;
import personnel.MedicalOfficer;
import personnel.Personnel;
import personnel.Scientist;

/** Lists all personnel and lets the admin add new ones or edit an existing one. */
final class PersonnelPanel extends JPanel implements Refreshable {

    private final SimulationContext context;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Name", "ID", "Type", "Health", "Location", "Skills", "Fatigue", "Available", "Mission" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Personnel> currentRows = List.of();

    PersonnelPanel(SimulationContext context) {
        super(new BorderLayout(6, 6));
        this.context = context;
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setBackground(Theme.BACKGROUND);
        Theme.zebraStripe(table);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(Theme.BACKGROUND);
        JButton addButton = new JButton("Add Personnel...");
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
            JOptionPane.showMessageDialog(this, "Select a person first.");
            return;
        }
        Personnel person = currentRows.get(row);
        person.makeAvailable();
        context.pushEffect("FREED", person.getCurrentLocation(), Theme.SKY);
        context.log("Admin forced " + person.getName() + " back to available.");
        context.notifyChanged();
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this), "Add Personnel",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        Theme.styleDialog(dialog);

        JComboBox<String> typeCombo = new JComboBox<>(new String[] { "Engineer", "Scientist", "MedicalOfficer" });
        JTextField nameField = new JTextField();
        JSpinner idSpinner = new JSpinner(new SpinnerNumberModel(nextSuggestedId(), 1, 1_000_000, 1));
        JTextField healthField = new JTextField("Healthy");
        JComboBox<String> locationCombo = new JComboBox<>(locationOptions());
        locationCombo.setEditable(true);
        JSpinner fatigueSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
        JCheckBox availableBox = new JCheckBox("Available", true);

        JPanel skillsPanel = new JPanel(new GridLayout(0, 1));
        java.util.Map<Skills, JCheckBox> skillBoxes = new java.util.EnumMap<>(Skills.class);
        for (Skills skill : Skills.values()) {
            JCheckBox box = new JCheckBox(skill.name());
            skillBoxes.put(skill, box);
            skillsPanel.add(box);
        }
        skillBoxes.get(Skills.MECHANICAL).setSelected(true);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Type:"));
        form.add(typeCombo);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("ID:"));
        form.add(idSpinner);
        form.add(new JLabel("Health status:"));
        form.add(healthField);
        form.add(new JLabel("Location:"));
        form.add(locationCombo);
        form.add(new JLabel("Fatigue level:"));
        form.add(fatigueSpinner);
        form.add(new JLabel("Availability:"));
        form.add(availableBox);
        form.add(new JLabel("Skills:"));
        form.add(skillsPanel);

        dialog.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton create = new JButton("Create");
        JButton cancel = new JButton("Cancel");
        footer.add(create);
        footer.add(cancel);
        dialog.add(footer, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dialog.dispose());
        create.addActionListener(e -> {
            try {
                Set<Skills> selected = EnumSet.noneOf(Skills.class);
                for (var entry : skillBoxes.entrySet()) {
                    if (entry.getValue().isSelected()) {
                        selected.add(entry.getKey());
                    }
                }
                String name = nameField.getText().trim();
                int id = (Integer) idSpinner.getValue();
                String health = healthField.getText().trim();
                String location = ((String) locationCombo.getEditor().getItem()).trim();
                double fatigue = (Double) fatigueSpinner.getValue();
                boolean available = availableBox.isSelected();
                String type = (String) typeCombo.getSelectedItem();

                Personnel person = switch (type) {
                    case "Engineer" -> new Engineer(name, id, health, location, selected, fatigue, available);
                    case "Scientist" -> new Scientist(name, id, health, location, selected, fatigue, available);
                    default -> new MedicalOfficer(name, id, health, location, selected, fatigue, available);
                };

                context.colony().addPersonnel(person);
                context.pushEffect("NEW CREW", location, Theme.LEAF);
                context.log("Added " + type + " " + name + " (#" + id + ") at " + location + ".");
                context.notifyChanged();
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Cannot add personnel", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openEditDialog(Personnel person) {
        JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this),
                "Edit " + person.getName(), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        Theme.styleDialog(dialog);

        JTextField healthField = new JTextField(person.getHealthStatus());
        JTextField locationField = new JTextField(person.getCurrentLocation());
        JSpinner fatigueSpinner = new JSpinner(new SpinnerNumberModel(person.getFatigueLevel(), 0.0, 100.0, 1.0));
        JCheckBox availableBox = new JCheckBox("Available", person.getAvailability());

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Health status:"));
        form.add(healthField);
        form.add(new JLabel("Current location:"));
        form.add(locationField);
        form.add(new JLabel("Fatigue level:"));
        form.add(fatigueSpinner);
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
                person.setHealthStatus(healthField.getText().trim());
                person.setCurrentLocation(locationField.getText().trim());
                person.setFatigueLevel((Double) fatigueSpinner.getValue());
                person.setAvailability(availableBox.isSelected());
                context.log("Updated personnel record for " + person.getName() + ".");
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

    private int nextSuggestedId() {
        int max = 100;
        for (Personnel person : context.colony().getPersonnel()) {
            max = Math.max(max, person.getId());
        }
        return max + 1;
    }

    @Override
    public void refresh() {
        int selectedRow = table.getSelectedRow();
        Personnel previouslySelected = selectedRow >= 0 && selectedRow < currentRows.size()
                ? currentRows.get(selectedRow) : null;

        currentRows = context.colony().getPersonnel();
        model.setRowCount(0);
        for (Personnel person : currentRows) {
            Agent agent = person;
            model.addRow(new Object[] {
                    person.getName(),
                    person.getId(),
                    person.getClass().getSimpleName(),
                    person.getHealthStatus(),
                    person.getCurrentLocation(),
                    person.getSkills(),
                    String.format("%.1f", person.getFatigueLevel()),
                    agent.isAvailable() ? "Yes" : "Busy",
                    person.getMission() == null ? "-" : person.getMission().getClass().getSimpleName()
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
