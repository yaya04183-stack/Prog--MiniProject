package admingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import agents.Agent;
import enums.FacilityType;
import enums.OperationalStatus;
import enums.Resources;
import facilities.CommunicationStation;
import facilities.Equipment;
import facilities.Facilities;
import facilities.Habitat;
import facilities.Laboratory;
import facilities.PowerStation;
import facilities.StorageFacility;

/** Lists all facilities and lets the admin add new ones or manage an existing one's status/resources/equipment. */
final class FacilitiesPanel extends JPanel implements Refreshable {

    private final SimulationContext context;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Type", "Location", "Capacity", "Status", "Resources", "Equipment", "Occupants" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Facilities> currentRows = List.of();

    FacilitiesPanel(SimulationContext context) {
        super(new BorderLayout(6, 6));
        this.context = context;
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setBackground(Theme.BACKGROUND);
        Theme.zebraStripe(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(Theme.BACKGROUND);
        JButton addButton = new JButton("Add Facility...");
        Theme.styleAccentButton(addButton, Theme.TEAL);
        addButton.addActionListener(e -> openAddDialog());
        buttons.add(addButton);
        buttons.add(new JLabel("  (double-click a row to manage status/resources/equipment)"));
        add(buttons, BorderLayout.SOUTH);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < currentRows.size()) {
                        openManageDialog(currentRows.get(row));
                    }
                }
            }
        });
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this), "Add Facility",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        Theme.styleDialog(dialog);

        JComboBox<FacilityType> typeCombo = new JComboBox<>(FacilityType.values());
        JTextField locationField = new JTextField("Facility-" + (context.colony().getFacilities().size() + 1));
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(50.0, 0.0, 1_000_000.0, 5.0));
        JComboBox<OperationalStatus> statusCombo = new JComboBox<>(OperationalStatus.values());
        JSpinner outputSpinner = new JSpinner(new SpinnerNumberModel(5.0, 0.0, 10_000.0, 1.0));

        Map<Resources, JSpinner> resourceSpinners = new EnumMap<>(Resources.class);
        JPanel resourcesPanel = new JPanel(new GridLayout(0, 2, 4, 2));
        for (Resources resource : Resources.values()) {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000.0, 1.0));
            resourceSpinners.put(resource, spinner);
            resourcesPanel.add(new JLabel(resource.name()));
            resourcesPanel.add(spinner);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Type:"));
        form.add(typeCombo);
        form.add(new JLabel("Location name:"));
        form.add(locationField);
        form.add(new JLabel("Capacity:"));
        form.add(capacitySpinner);
        form.add(new JLabel("Operational status:"));
        form.add(statusCombo);
        form.add(new JLabel("Power output/unit (Power Station only):"));
        form.add(outputSpinner);

        dialog.add(form, BorderLayout.NORTH);
        dialog.add(new JScrollPane(resourcesPanel), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton create = new JButton("Create");
        JButton cancel = new JButton("Cancel");
        footer.add(create);
        footer.add(cancel);
        dialog.add(footer, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dialog.dispose());
        create.addActionListener(e -> {
            try {
                Map<Resources, Double> resources = new EnumMap<>(Resources.class);
                for (var entry : resourceSpinners.entrySet()) {
                    double value = (Double) entry.getValue().getValue();
                    if (value > 0) {
                        resources.put(entry.getKey(), value);
                    }
                }
                String location = locationField.getText().trim();
                double capacity = (Double) capacitySpinner.getValue();
                OperationalStatus status = (OperationalStatus) statusCombo.getSelectedItem();
                FacilityType type = (FacilityType) typeCombo.getSelectedItem();

                Facilities facility = switch (type) {
                    case HABITAT -> new Habitat(location, capacity, status, resources, List.of(), List.of());
                    case LABORATORY -> new Laboratory(location, capacity, status, resources, List.of(), List.of());
                    case STORAGE -> new StorageFacility(location, capacity, status, resources, List.of(), List.of());
                    case POWER_STATION -> new PowerStation(location, capacity, status, resources, List.of(), List.of(),
                            (Double) outputSpinner.getValue());
                    case COMMUNICATION_STATION -> new CommunicationStation(location, capacity, status, resources,
                            List.of(), List.of());
                };

                context.colony().addFacility(facility);
                context.pushEffect("BUILT", location, Theme.LEAF);
                context.log("Added facility " + type + " at " + location + ".");
                context.notifyChanged();
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Cannot add facility", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openManageDialog(Facilities facility) {
        JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this),
                "Manage " + facility.getLocation(), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        Theme.styleDialog(dialog);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<OperationalStatus> statusCombo = new JComboBox<>(OperationalStatus.values());
        statusCombo.setSelectedItem(facility.getOperationalStatus());
        JButton applyStatus = new JButton("Apply status");
        top.add(new JLabel("Status:"));
        top.add(statusCombo);
        top.add(applyStatus);
        dialog.add(top, BorderLayout.NORTH);

        DefaultTableModel resourceModel = new DefaultTableModel(new Object[] { "Resource", "Amount" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (var entry : facility.getResources().entrySet()) {
            resourceModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }
        JTable resourceTable = new JTable(resourceModel);

        DefaultTableModel equipmentModel = new DefaultTableModel(new Object[] { "Name", "Status", "Category" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Equipment item : facility.getEquipment()) {
            equipmentModel.addRow(new Object[] { item.getName(), item.getOperationalStatus(), item.getCategory() });
        }
        JTable equipmentTable = new JTable(equipmentModel);

        DefaultTableModel occupantModel = new DefaultTableModel(new Object[] { "Occupant", "Current Location" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        List<Agent> occupantRows = new java.util.ArrayList<>();
        refreshOccupantModel(occupantModel, occupantRows, facility);
        JTable occupantTable = new JTable(occupantModel);

        JPanel center = new JPanel(new GridLayout(1, 3, 8, 8));
        center.add(wrapTitled(resourceTable, "Resources"));
        center.add(wrapTitled(equipmentTable, "Equipment"));
        center.add(wrapTitled(occupantTable, "Occupants"));
        dialog.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<Resources> resourceCombo = new JComboBox<>(Resources.values());
        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1_000_000.0, 1.0));
        JButton addResourceButton = new JButton("Add resource");
        JButton deductResourceButton = new JButton("Deduct resource");
        bottom.add(new JLabel("Resource:"));
        bottom.add(resourceCombo);
        bottom.add(amountSpinner);
        bottom.add(addResourceButton);
        bottom.add(deductResourceButton);

        JTextField newEquipmentName = new JTextField(10);
        JComboBox<OperationalStatus> newEquipmentStatus = new JComboBox<>(OperationalStatus.values());
        JComboBox<Equipment.Category> newEquipmentCategory = new JComboBox<>(Equipment.Category.values());
        JButton addEquipmentButton = new JButton("Add equipment");
        bottom.add(newEquipmentName);
        bottom.add(newEquipmentStatus);
        bottom.add(newEquipmentCategory);
        bottom.add(addEquipmentButton);

        JPanel occupantControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<Agent> addOccupantCombo = new JComboBox<>();
        addOccupantCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "" : describeAgent((Agent) value));
            label.setOpaque(true);
            label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return label;
        });
        refreshAddOccupantCombo(addOccupantCombo, occupantRows);
        JButton addOccupantButton = new JButton("Add occupant");
        JButton removeOccupantButton = new JButton("Remove selected occupant");
        JButton syncOccupantsButton = new JButton("Sync from current location");
        Theme.styleAccentButton(addOccupantButton, Theme.TEAL);
        Theme.styleAccentButton(removeOccupantButton, Theme.MUTED_INK);
        Theme.styleAccentButton(syncOccupantsButton, Theme.SKY);
        occupantControls.add(new JLabel("Occupant:"));
        occupantControls.add(addOccupantCombo);
        occupantControls.add(addOccupantButton);
        occupantControls.add(removeOccupantButton);
        occupantControls.add(syncOccupantsButton);

        JPanel southStack = new JPanel();
        southStack.setLayout(new javax.swing.BoxLayout(southStack, javax.swing.BoxLayout.Y_AXIS));
        southStack.add(bottom);
        southStack.add(occupantControls);
        dialog.add(southStack, BorderLayout.SOUTH);

        addOccupantButton.addActionListener(e -> {
            Agent selected = (Agent) addOccupantCombo.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(dialog, "No available agent to add (or all are already occupants).");
                return;
            }
            List<Agent> updated = new java.util.ArrayList<>(facility.getOccupants());
            updated.add(selected);
            facility.setOccupants(updated);
            context.pushEffect("OCCUPANT+", facility, Theme.LEAF);
            context.log("Added an occupant to " + facility.getLocation() + ".");
            context.notifyChanged();
            refreshOccupantModel(occupantModel, occupantRows, facility);
            refreshAddOccupantCombo(addOccupantCombo, occupantRows);
        });

        removeOccupantButton.addActionListener(e -> {
            int row = occupantTable.getSelectedRow();
            if (row < 0 || row >= occupantRows.size()) {
                JOptionPane.showMessageDialog(dialog, "Select an occupant row first.");
                return;
            }
            List<Agent> updated = new java.util.ArrayList<>(facility.getOccupants());
            updated.remove(occupantRows.get(row));
            facility.setOccupants(updated);
            context.pushEffect("OCCUPANT-", facility, Theme.ROSE);
            context.log("Removed an occupant from " + facility.getLocation() + ".");
            context.notifyChanged();
            refreshOccupantModel(occupantModel, occupantRows, facility);
            refreshAddOccupantCombo(addOccupantCombo, occupantRows);
        });

        syncOccupantsButton.addActionListener(e -> {
            List<Agent> present = new java.util.ArrayList<>();
            for (Agent agent : allAgents()) {
                if (facility.getLocation().equals(agent.getCurrentLocation())) {
                    present.add(agent);
                }
            }
            facility.setOccupants(present);
            context.pushEffect("SYNCED", facility, Theme.SKY);
            context.log("Synced occupants of " + facility.getLocation()
                    + " from current locations (" + present.size() + " found).");
            context.notifyChanged();
            refreshOccupantModel(occupantModel, occupantRows, facility);
            refreshAddOccupantCombo(addOccupantCombo, occupantRows);
        });

        applyStatus.addActionListener(e -> {
            try {
                facility.setOperationalStatus((OperationalStatus) statusCombo.getSelectedItem());
                context.pushEffect(String.valueOf(statusCombo.getSelectedItem()), facility,
                        Theme.statusTint(String.valueOf(statusCombo.getSelectedItem())));
                context.log("Set " + facility.getLocation() + " status to " + statusCombo.getSelectedItem() + ".");
                context.notifyChanged();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Cannot apply", JOptionPane.ERROR_MESSAGE);
            }
        });

        addResourceButton.addActionListener(e -> {
            Resources resource = (Resources) resourceCombo.getSelectedItem();
            double amount = (Double) amountSpinner.getValue();
            facility.addResource(resource, amount);
            context.pushEffect("+" + amount + " " + resource, facility, Theme.AMBER);
            context.log("Added " + amount + " " + resource + " to " + facility.getLocation() + ".");
            context.notifyChanged();
            refreshResourceTable(resourceModel, facility);
        });

        deductResourceButton.addActionListener(e -> {
            Resources resource = (Resources) resourceCombo.getSelectedItem();
            double amount = (Double) amountSpinner.getValue();
            double deducted = facility.deductResource(resource, amount);
            context.pushEffect("-" + deducted + " " + resource, facility, Theme.ROSE);
            context.log("Deducted " + deducted + " " + resource + " from " + facility.getLocation() + ".");
            context.notifyChanged();
            refreshResourceTable(resourceModel, facility);
        });

        addEquipmentButton.addActionListener(e -> {
            try {
                Equipment equipment = new Equipment(newEquipmentName.getText().trim(),
                        (OperationalStatus) newEquipmentStatus.getSelectedItem(),
                        (Equipment.Category) newEquipmentCategory.getSelectedItem());
                List<Equipment> updated = new java.util.ArrayList<>(facility.getEquipment());
                updated.add(equipment);
                facility.setEquipment(updated);
                context.log("Added equipment '" + equipment.getName() + "' to " + facility.getLocation() + ".");
                context.notifyChanged();
                equipmentModel.addRow(new Object[] { equipment.getName(), equipment.getOperationalStatus(),
                        equipment.getCategory() });
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Cannot add equipment", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void refreshResourceTable(DefaultTableModel resourceModel, Facilities facility) {
        resourceModel.setRowCount(0);
        for (var entry : facility.getResources().entrySet()) {
            resourceModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }
    }

    private void refreshOccupantModel(DefaultTableModel occupantModel, List<Agent> occupantRows, Facilities facility) {
        occupantModel.setRowCount(0);
        occupantRows.clear();
        for (Agent agent : facility.getOccupants()) {
            occupantRows.add(agent);
            occupantModel.addRow(new Object[] { describeAgent(agent), agent.getCurrentLocation() });
        }
    }

    private void refreshAddOccupantCombo(JComboBox<Agent> combo, List<Agent> currentOccupants) {
        combo.removeAllItems();
        for (Agent agent : allAgents()) {
            if (!currentOccupants.contains(agent)) {
                combo.addItem(agent);
            }
        }
    }

    private List<Agent> allAgents() {
        List<Agent> agents = new java.util.ArrayList<>(context.colony().getPersonnel());
        agents.addAll(context.colony().getRoboticUnits());
        return agents;
    }

    private String describeAgent(Agent agent) {
        if (agent instanceof personnel.Personnel person) {
            return person.getName() + " (" + person.getClass().getSimpleName() + ")";
        }
        return agent.getClass().getSimpleName();
    }

    private JPanel wrapTitled(JTable table, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void refresh() {
        int selectedRow = table.getSelectedRow();
        Facilities previouslySelected = selectedRow >= 0 && selectedRow < currentRows.size()
                ? currentRows.get(selectedRow) : null;

        currentRows = context.colony().getFacilities();
        model.setRowCount(0);
        for (Facilities facility : currentRows) {
            StringBuilder resourcesText = new StringBuilder();
            for (var entry : facility.getResources().entrySet()) {
                if (resourcesText.length() > 0) {
                    resourcesText.append(", ");
                }
                resourcesText.append(entry.getKey()).append("=").append(String.format("%.1f", entry.getValue()));
            }
            model.addRow(new Object[] {
                    facility.getClass().getSimpleName(),
                    facility.getLocation(),
                    facility.getCapacity(),
                    facility.getOperationalStatus(),
                    resourcesText.toString(),
                    facility.getEquipment().size(),
                    facility.getOccupants().size()
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
