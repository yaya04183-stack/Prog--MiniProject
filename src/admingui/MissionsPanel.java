package admingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import agents.Agent;
import enums.Capability;
import enums.OperationalStatus;
import enums.Resources;
import facilities.Equipment;
import facilities.Facilities;
import facilities.Laboratory;
import missions.Exploration;
import missions.Mission;
import missions.Repair;
import missions.Rescue;
import missions.Research;

/** Full mission lifecycle: draft, submit with a chosen team, start, advance, finish/fail. */
final class MissionsPanel extends JPanel implements Refreshable {

    private final SimulationContext context;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Type", "Facility", "State", "Progress", "Agents", "Failure Reason" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Mission> currentRows = List.of();

    MissionsPanel(SimulationContext context) {
        super(new BorderLayout(6, 6));
        this.context = context;
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setBackground(Theme.BACKGROUND);
        Theme.zebraStripe(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(Theme.BACKGROUND);
        JButton newMission = new JButton("New Mission...");
        Theme.styleAccentButton(newMission, Theme.TEAL);
        newMission.addActionListener(e -> openNewMissionDialog());
        JButton submit = new JButton("Submit Selected Draft...");
        Theme.styleAccentButton(submit, Theme.SKY);
        submit.addActionListener(e -> submitSelectedDraft());
        JButton removeDraft = new JButton("Remove Selected Draft");
        Theme.styleAccentButton(removeDraft, Theme.MUTED_INK);
        removeDraft.addActionListener(e -> removeSelectedDraft());
        JButton start = new JButton("Start Active Mission");
        Theme.styleAccentButton(start, Theme.LEAF);
        start.addActionListener(e -> startActive());
        JButton finishOk = new JButton("Force Finish (success)");
        Theme.styleAccentButton(finishOk, Theme.LEAF);
        finishOk.addActionListener(e -> finishActive(true));
        JButton finishFail = new JButton("Force Finish (fail)");
        Theme.styleAccentButton(finishFail, Theme.ROSE);
        finishFail.addActionListener(e -> finishActive(false));
        JButton abort = new JButton("Fail Active Mission...");
        Theme.styleAccentButton(abort, Theme.ROSE);
        abort.addActionListener(e -> failActive());

        buttons.add(newMission);
        buttons.add(submit);
        buttons.add(removeDraft);
        buttons.add(start);
        buttons.add(finishOk);
        buttons.add(finishFail);
        buttons.add(abort);
        add(buttons, BorderLayout.SOUTH);
    }

    private void submitSelectedDraft() {
        Mission selected = selectedMission();
        if (selected == null || selected.getState() != null) {
            JOptionPane.showMessageDialog(this, "Select a draft (unsubmitted) mission row first.");
            return;
        }
        openTeamSelectionDialog(selected);
    }

    private void removeSelectedDraft() {
        Mission selected = selectedMission();
        if (selected == null || selected.getState() != null) {
            JOptionPane.showMessageDialog(this, "Select a draft (unsubmitted) mission row first.");
            return;
        }
        context.removeMissionDraft(selected);
        context.log("Discarded a " + selected.getClass().getSimpleName() + " draft.");
    }

    private void startActive() {
        Mission active = context.colony().getActiveMission();
        boolean started = context.colony().startActiveMission();
        context.log(started ? "Active mission started." : "Could not start active mission"
                + (active != null && active.getFailureReason() != null ? ": " + active.getFailureReason() : "."));
        if (active != null) {
            context.pushEffect(started ? "MISSION GO!" : "START FAILED",
                    active.getFacility() != null ? active.getFacility() : "Martian Surface",
                    started ? Theme.LEAF : Theme.ROSE);
        }
        context.notifyChanged();
    }

    private void finishActive(boolean success) {
        Mission active = context.colony().getActiveMission();
        boolean ok = context.colony().finishActiveMission(success);
        context.log(ok ? "Active mission force-finished (" + (success ? "success" : "failure") + ")."
                : "No active mission to finish.");
        if (ok && active != null) {
            context.pushEffect(success ? "SUCCESS!" : "FAILED",
                    active.getFacility() != null ? active.getFacility() : "Martian Surface",
                    success ? Theme.LEAF : Theme.ROSE);
        }
        context.notifyChanged();
    }

    private void failActive() {
        String reason = JOptionPane.showInputDialog(this, "Failure reason:", "Admin override");
        if (reason == null || reason.isBlank()) {
            return;
        }
        Mission active = context.colony().getActiveMission();
        boolean ok = context.colony().failActiveMission(reason);
        context.log(ok ? "Active mission failed by admin: " + reason : "No active mission to fail.");
        if (ok && active != null) {
            context.pushEffect("ABORTED", active.getFacility() != null ? active.getFacility() : "Martian Surface",
                    Theme.ROSE);
        }
        context.notifyChanged();
    }

    private Mission selectedMission() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentRows.size()) {
            return null;
        }
        return currentRows.get(row);
    }

    private void openTeamSelectionDialog(Mission mission) {
        JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this), "Submit Mission - Choose Team",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        Theme.styleDialog(dialog);

        List<Agent> orderedAgents = new ArrayList<>(allAgents());
        orderedAgents.sort(Comparator.comparing((Agent a) -> !agentQualifies(a, mission))
                .thenComparing(a -> !a.isAvailable()));

        DefaultListModel<Agent> listModel = new DefaultListModel<>();
        for (Agent agent : orderedAgents) {
            listModel.addElement(agent);
        }
        JList<Agent> agentList = new JList<>(listModel);
        agentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        agentList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String text = describeAgent(value) + (agentQualifies(value, mission) ? "   ✓ suggested" : "");
            JLabel label = new JLabel(text);
            label.setOpaque(true);
            label.setFont(Theme.BASE_FONT);
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(agentQualifies(value, mission) ? Theme.LEAF
                        : value.isAvailable() ? Theme.MUTED_INK : Theme.ROSE);
            }
            return label;
        });

        JPanel north = new JPanel(new BorderLayout(4, 4));
        north.setBackground(Theme.BACKGROUND);
        JLabel infoLabel = new JLabel("Required capabilities: " + mission.getRequiredCapabilities()
                + "   (green ✓ = available and qualified for this mission)");
        infoLabel.setForeground(Theme.MUTED_INK);
        north.add(infoLabel, BorderLayout.NORTH);
        JButton autoSelect = new JButton("Auto-select Team");
        Theme.styleAccentButton(autoSelect, Theme.TEAL);
        JPanel autoRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        autoRow.setBackground(Theme.BACKGROUND);
        autoRow.add(autoSelect);
        north.add(autoRow, BorderLayout.SOUTH);
        dialog.add(north, BorderLayout.NORTH);
        dialog.add(new JScrollPane(agentList), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submit = new JButton("Submit");
        JButton cancel = new JButton("Cancel");
        Theme.styleAccentButton(submit, Theme.LEAF);
        footer.add(submit);
        footer.add(cancel);
        dialog.add(footer, BorderLayout.SOUTH);

        autoSelect.addActionListener(e -> {
            List<Agent> autoTeam = autoSelectTeam(mission, orderedAgents);
            if (autoTeam.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "No combination of currently available agents satisfies this mission's team "
                                + "requirements yet. Pick manually, or free up/add more agents.",
                        "Auto-select", JOptionPane.WARNING_MESSAGE);
                return;
            }
            agentList.clearSelection();
            int[] indices = autoTeam.stream().mapToInt(orderedAgents::indexOf).toArray();
            agentList.setSelectedIndices(indices);
            context.log("Auto-selected " + autoTeam.size() + " agent(s) for the " + mission.getClass().getSimpleName()
                    + " draft.");
        });

        cancel.addActionListener(e -> dialog.dispose());
        submit.addActionListener(e -> {
            List<Agent> selected = agentList.getSelectedValuesList();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Select at least one agent.");
                return;
            }
            boolean accepted = context.colony().submitMission(mission, new ArrayList<>(selected));
            context.removeMissionDraft(mission);
            context.log("Submitted " + mission.getClass().getSimpleName() + " with " + selected.size()
                    + " agent(s): " + (accepted ? "ACCEPTED" : "REJECTED (" + mission.getFailureReason() + ")"));
            Object missionSpot = mission.getFacility() != null ? mission.getFacility() : "Martian Surface";
            context.pushEffect(accepted ? "MISSION ACCEPTED" : "REJECTED", missionSpot,
                    accepted ? Theme.LEAF : Theme.ROSE);
            context.notifyChanged();
            dialog.dispose();
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private List<Agent> allAgents() {
        List<Agent> agents = new ArrayList<>(context.colony().getPersonnel());
        agents.addAll(context.colony().getRoboticUnits());
        return agents;
    }

    /** An agent is "suggested" when it's free and the mission would actually accept it. */
    private boolean agentQualifies(Agent agent, Mission mission) {
        return agent.isAvailable() && agent.canPerform(mission);
    }

    /**
     * Greedily builds the smallest team of available, qualified agents that satisfies the
     * mission's own team-composition rule. Returns an empty list if no such team exists yet
     * with the agents currently free.
     */
    private List<Agent> autoSelectTeam(Mission mission, List<Agent> candidatePool) {
        List<Agent> qualified = new ArrayList<>();
        for (Agent agent : candidatePool) {
            if (agentQualifies(agent, mission)) {
                qualified.add(agent);
            }
        }
        if (qualified.isEmpty() || !mission.hasValidTeamComposition(qualified)) {
            return List.of();
        }
        List<Agent> team = new ArrayList<>(qualified);
        for (Agent candidate : new ArrayList<>(team)) {
            List<Agent> without = new ArrayList<>(team);
            without.remove(candidate);
            if (mission.hasValidTeamComposition(without)) {
                team = without;
            }
        }
        return team;
    }

    private String describeAgent(Agent agent) {
        String name = agent.getClass().getSimpleName();
        if (agent instanceof personnel.Personnel person) {
            name = person.getName() + " (" + name + ")";
        }
        return String.format("%-28s @ %-18s %s", name, agent.getCurrentLocation(),
                agent.isAvailable() ? "[available]" : "[busy]");
    }

    private void openNewMissionDialog() {
        JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this), "New Mission",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        Theme.styleDialog(dialog);

        JComboBox<String> typeCombo = new JComboBox<>(new String[] { "Exploration", "Repair", "Rescue", "Research" });
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 1000, 1));
        JComboBox<String> facilityCombo = new JComboBox<>();
        JTextField extraTextField = new JTextField("North Ridge");
        JComboBox<String> repairTargetCombo = new JComboBox<>();
        Map<String, Object> repairTargets = new LinkedHashMap<>();
        JButton forceDamageButton = new JButton("Force-damage a target for testing...");
        JLabel extraLabel = new JLabel("Region:");

        Map<Capability, javax.swing.JCheckBox> capabilityBoxes = new java.util.EnumMap<>(Capability.class);
        JPanel capabilitiesPanel = new JPanel(new GridLayout(0, 1));
        for (Capability capability : Capability.values()) {
            javax.swing.JCheckBox box = new javax.swing.JCheckBox(capability.name());
            capabilityBoxes.put(capability, box);
            capabilitiesPanel.add(box);
        }

        Map<Resources, JSpinner> resourceSpinners = new java.util.EnumMap<>(Resources.class);
        JPanel resourcesPanel = new JPanel(new GridLayout(0, 2, 4, 2));
        for (Resources resource : Resources.values()) {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000.0, 1.0));
            resourceSpinners.put(resource, spinner);
            resourcesPanel.add(new JLabel(resource.name()));
            resourcesPanel.add(spinner);
        }

        Runnable refreshFacilityCombo = () -> {
            String currentType = (String) typeCombo.getSelectedItem();
            facilityCombo.removeAllItems();
            facilityCombo.addItem("(none)");
            for (Facilities facility : context.colony().getFacilities()) {
                if ("Research".equals(currentType) && !(facility instanceof Laboratory)) {
                    continue;
                }
                facilityCombo.addItem(facility.getLocation());
            }
        };

        Runnable refreshRepairTargets = () -> {
            repairTargets.clear();
            repairTargetCombo.removeAllItems();
            for (Facilities facility : context.colony().getFacilities()) {
                if (facility.getOperationalStatus() == OperationalStatus.DAMAGED) {
                    String key = "Facility: " + facility.getLocation();
                    repairTargets.put(key, facility);
                    repairTargetCombo.addItem(key);
                }
                for (Equipment equipment : facility.getEquipment()) {
                    if (equipment.getOperationalStatus() == OperationalStatus.DAMAGED) {
                        String key = "Equipment: " + equipment.getName() + " @ " + facility.getLocation();
                        repairTargets.put(key, equipment);
                        repairTargetCombo.addItem(key);
                    }
                }
            }
            for (roboticunit.RoboticUnit unit : context.colony().getRoboticUnits()) {
                for (Equipment equipment : unit.getEquipment()) {
                    if (equipment.getOperationalStatus() == OperationalStatus.DAMAGED) {
                        String key = "Equipment: " + equipment.getName() + " @ " + unit.getClass().getSimpleName();
                        repairTargets.put(key, equipment);
                        repairTargetCombo.addItem(key);
                    }
                }
            }
        };

        Runnable syncVisibility = () -> {
            String type = (String) typeCombo.getSelectedItem();
            boolean isExploration = "Exploration".equals(type);
            boolean isRepair = "Repair".equals(type);
            boolean isResearch = "Research".equals(type);
            extraLabel.setVisible(isExploration || isResearch);
            extraTextField.setVisible(isExploration || isResearch);
            extraLabel.setText(isResearch ? "Research topic:" : "Region:");
            if (isResearch && "North Ridge".equals(extraTextField.getText())) {
                extraTextField.setText("Soil Composition");
            }
            repairTargetCombo.setVisible(isRepair);
            forceDamageButton.setVisible(isRepair);
            refreshFacilityCombo.run();
            if (isRepair) {
                refreshRepairTargets.run();
            }
            for (var entry : capabilityBoxes.entrySet()) {
                entry.getValue().setSelected(false);
            }
            if (isExploration) {
                capabilityBoxes.get(Capability.SURFACE_TRAVEL).setSelected(true);
                capabilityBoxes.get(Capability.EQUIPMENT_CARRYING).setSelected(true);
                capabilityBoxes.get(Capability.EXPLORATION).setSelected(true);
            } else if (isRepair) {
                capabilityBoxes.get(Capability.EQUIPMENT_REPAIR).setSelected(true);
            }
        };
        typeCombo.addActionListener(e -> syncVisibility.run());
        syncVisibility.run();

        forceDamageButton.addActionListener(e -> {
            Map<String, Object> candidates = new LinkedHashMap<>();
            for (Facilities facility : context.colony().getFacilities()) {
                candidates.put("Facility: " + facility.getLocation() + " (" + facility.getOperationalStatus() + ")", facility);
                for (Equipment equipment : facility.getEquipment()) {
                    candidates.put("Equipment: " + equipment.getName() + " @ " + facility.getLocation()
                            + " (" + equipment.getOperationalStatus() + ")", equipment);
                }
            }
            if (candidates.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No facilities/equipment exist yet.");
                return;
            }
            String choice = (String) JOptionPane.showInputDialog(dialog, "Pick an item to mark DAMAGED:",
                    "Force damage", JOptionPane.PLAIN_MESSAGE, null, candidates.keySet().toArray(), null);
            if (choice == null) {
                return;
            }
            Object target = candidates.get(choice);
            if (target instanceof Facilities facility) {
                facility.setOperationalStatus(OperationalStatus.DAMAGED);
            } else if (target instanceof Equipment equipment) {
                equipment.setOperationalStatus(OperationalStatus.DAMAGED);
            }
            context.log("Admin marked '" + choice + "' as DAMAGED for a repair-mission demo.");
            context.notifyChanged();
            refreshRepairTargets.run();
        });

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Mission type:"));
        form.add(typeCombo);
        form.add(new JLabel("Needed time units:"));
        form.add(timeSpinner);
        form.add(new JLabel("Facility:"));
        form.add(facilityCombo);
        form.add(extraLabel);
        form.add(extraTextField);
        form.add(new JLabel("Repair target:"));
        form.add(repairTargetCombo);
        form.add(new JLabel(""));
        form.add(forceDamageButton);

        JPanel detailPanel = new JPanel(new GridLayout(1, 2, 8, 8));
        detailPanel.add(wrapTitled(resourcesPanel, "Required resources"));
        detailPanel.add(wrapTitled(capabilitiesPanel, "Required capabilities"));

        dialog.add(form, BorderLayout.NORTH);
        dialog.add(detailPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton create = new JButton("Create Draft");
        JButton cancel = new JButton("Cancel");
        footer.add(create);
        footer.add(cancel);
        dialog.add(footer, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dialog.dispose());
        create.addActionListener(e -> {
            try {
                String type = (String) typeCombo.getSelectedItem();
                int units = (Integer) timeSpinner.getValue();
                String facilityLocation = (String) facilityCombo.getSelectedItem();
                Facilities facility = "(none)".equals(facilityLocation) ? null
                        : context.colony().getFacilities().stream()
                                .filter(f -> f.getLocation().equals(facilityLocation)).findFirst().orElse(null);

                Map<Resources, Double> resources = new java.util.EnumMap<>(Resources.class);
                for (var entry : resourceSpinners.entrySet()) {
                    double value = (Double) entry.getValue().getValue();
                    if (value > 0) {
                        resources.put(entry.getKey(), value);
                    }
                }
                Set<Capability> capabilities = EnumSet.noneOf(Capability.class);
                for (var entry : capabilityBoxes.entrySet()) {
                    if (entry.getValue().isSelected()) {
                        capabilities.add(entry.getKey());
                    }
                }

                Mission mission = switch (type) {
                    case "Exploration" -> new Exploration(units, facility, resources, capabilities,
                            extraTextField.getText().trim());
                    case "Repair" -> {
                        Object target = repairTargets.get(repairTargetCombo.getSelectedItem());
                        if (target == null) {
                            throw new IllegalArgumentException(
                                    "No DAMAGED facility/equipment selected. Use 'Force-damage a target' first.");
                        }
                        yield new Repair(units, facility, resources, capabilities, target);
                    }
                    case "Rescue" -> new Rescue(units, facility, resources, capabilities);
                    default -> new Research(units, facility, resources, capabilities, extraTextField.getText().trim());
                };

                context.addMissionDraft(mission);
                context.log("Drafted a new " + type + " mission (not yet submitted).");
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Cannot create mission", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel wrapTitled(JPanel inner, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(inner), BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void refresh() {
        Mission previouslySelected = selectedMission();

        currentRows = new ArrayList<>(context.missionDrafts());
        currentRows.addAll(context.colony().getMissions());
        model.setRowCount(0);
        for (Mission mission : currentRows) {
            model.addRow(new Object[] {
                    mission.getClass().getSimpleName(),
                    mission.getFacility() == null ? "-" : mission.getFacility().getLocation(),
                    mission.getState() == null ? "DRAFT" : mission.getState(),
                    mission.getElapsedTimeUnits() + "/" + mission.getNeededTimesUnits(),
                    mission.getAgents().size(),
                    mission.getFailureReason() == null ? "-" : mission.getFailureReason()
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
