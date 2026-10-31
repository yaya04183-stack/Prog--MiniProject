package gui;

import java.awt.Color;
import java.awt.Font;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import colony.Colony;
import enums.Capability;
import enums.OperationalStatus;
import enums.Resources;
import enums.Skills;
import enums.EnviromentStates;
import facilities.CommunicationStation;
import facilities.Equipment;
import facilities.Habitat;
import facilities.Laboratory;
import facilities.PowerStation;
import facilities.StorageFacility;
import personnel.Engineer;
import personnel.MedicalOfficer;
import personnel.Scientist;
import marsenvironment.MarsEnvironment;
import roboticunit.Drone;
import roboticunit.ExplorationRover;
import roboticunit.RepairRobot;

/** Entry point for the isolated MARS-OPS administration dashboard. */
public final class Main {

    private Main() { }

    public static void main(String[] args) {
        setSystemLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            Colony colony = createDemoColony();
            MarsEnvironment environment = new MarsEnvironment(
                    EnviromentStates.NORMAL_CONDITION);
            colony.setMarsEnvironment(environment);
            AppSession session = new AppSession(colony, environment);
            session.record("Session started", "Demo colony", "READY",
                    "The example colony includes three damaged items for Repair mission practice. Changes are not saved after closing.");
            new AdminDashboard(session).setVisible(true);
        });
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Color background = new Color(232, 226, 212);
            Color surface = new Color(249, 246, 235);
            Color input = new Color(238, 232, 219);
            Color text = new Color(39, 51, 44);
            Color accent = new Color(174, 83, 54);
            Color selection = new Color(232, 216, 196);
            UIManager.put("Panel.background", background);
            UIManager.put("OptionPane.background", background);
            UIManager.put("OptionPane.messageForeground", text);
            UIManager.put("TextField.background", surface);
            UIManager.put("TextField.foreground", text);
            UIManager.put("TextField.caretForeground", text);
            UIManager.put("TextField.selectionBackground", selection);
            UIManager.put("TextField.selectionForeground", text);
            UIManager.put("TextArea.background", input);
            UIManager.put("TextArea.foreground", text);
            UIManager.put("TextArea.caretForeground", text);
            UIManager.put("ComboBox.background", surface);
            UIManager.put("ComboBox.foreground", text);
            UIManager.put("ComboBox.selectionBackground", selection);
            UIManager.put("ComboBox.selectionForeground", text);
            UIManager.put("CheckBox.background", surface);
            UIManager.put("CheckBox.foreground", text);
            UIManager.put("List.background", surface);
            UIManager.put("List.foreground", text);
            UIManager.put("List.selectionBackground", selection);
            UIManager.put("List.selectionForeground", text);
            UIManager.put("Table.background", surface);
            UIManager.put("Table.foreground", text);
            UIManager.put("Table.selectionBackground", selection);
            UIManager.put("Table.selectionForeground", text);
            UIManager.put("Button.background", input);
            UIManager.put("Button.foreground", text);
            UIManager.put("Focus.color", accent);
            Font uiFont = new Font("Segoe UI", Font.PLAIN, 14);
            for (String key : new String[]{"Label.font", "Button.font", "TextField.font",
                    "ComboBox.font", "List.font", "Table.font", "TableHeader.font",
                    "CheckBox.font", "OptionPane.messageFont", "OptionPane.buttonFont"}) {
                UIManager.put(key, uiFont);
            }
        } catch (Exception ignored) {
            // Swing's built-in look and feel remains available as a fallback.
        }
    }

    private static Colony createDemoColony() {
        Colony colony = new Colony();

        Engineer engineer = new Engineer(
                "Mira Chen", 1001, "Healthy", "Habitat-01",
                EnumSet.of(Skills.MECHANICAL, Skills.FIRE_RESPONSE), 8.0, true);
        Scientist scientist = new Scientist(
                "Noah Patel", 1002, "Healthy", "Habitat-01",
                EnumSet.of(Skills.SCIENTIFIC, Skills.COMMUNICATION), 3.0, true);
        MedicalOfficer medic = new MedicalOfficer(
                "Leila Hassan", 1003, "Healthy", "Habitat-01",
                EnumSet.of(Skills.MEDICAL), 5.0, true);

        Equipment roverKit = new Equipment(
                "Survey Kit", OperationalStatus.OPERATIONAL);
        ExplorationRover rover = new ExplorationRover(
                92.0, "Operational", "Habitat-01", true,
                EnumSet.of(Capability.SURFACE_TRAVEL,
                        Capability.EQUIPMENT_CARRYING,
                        Capability.EXPLORATION));
        rover.setEquipment(List.of(roverKit));

        RepairRobot repairRobot = new RepairRobot(
                88.0, "Operational", "Habitat-01", true);
        Drone drone = new Drone(76.0, "Operational", "Habitat-01", true);

        Equipment labEquipment = new Equipment(
                "Spectrometer", OperationalStatus.OPERATIONAL,
                Equipment.Category.SCIENTIFIC);
        Equipment repairDemoEquipment = new Equipment(
                "Habitat circulation pump", OperationalStatus.DAMAGED);
        Equipment oxygenScrubber = new Equipment(
                "Oxygen scrubber", OperationalStatus.DAMAGED);
        Equipment thermalRegulator = new Equipment(
                "Thermal regulator", OperationalStatus.DAMAGED);
        Habitat habitat = new Habitat(
                "Habitat-01", 30.0, OperationalStatus.OPERATIONAL,
                Map.of(Resources.OXYGEN, 120.0,
                        Resources.WATER, 80.0,
                        Resources.FOOD, 65.0,
                        Resources.SPARE_PARTS, 12.0,
                        Resources.MEDICAL_SUPPLIES, 8.0),
                List.of(repairDemoEquipment, oxygenScrubber, thermalRegulator),
                List.of(engineer, scientist, medic));
        Laboratory laboratory = new Laboratory(
                "Lab-01", 12.0, OperationalStatus.OPERATIONAL,
                Map.of(Resources.BATTERY_POWER, 30.0,
                        Resources.SPARE_PARTS, 4.0),
                List.of(labEquipment), List.of(scientist));
        CommunicationStation communications = new CommunicationStation(
                "Comms-01", 4.0, OperationalStatus.OPERATIONAL,
                Map.of(Resources.BATTERY_POWER, 10.0), List.of(), List.of());
        StorageFacility storage = new StorageFacility(
                "Storage-01", 100.0, OperationalStatus.OPERATIONAL,
                Map.of(Resources.FUEL, 25.0, Resources.WATER, 20.0),
                List.of(), List.of());
        PowerStation power = new PowerStation(
                "Power-01", 100.0, OperationalStatus.OPERATIONAL,
                Map.of(Resources.BATTERY_POWER, 50.0), List.of(), List.of(), 5.0);

        colony.addPersonnel(engineer);
        colony.addPersonnel(scientist);
        colony.addPersonnel(medic);
        colony.addRobot(rover);
        colony.addRobot(repairRobot);
        colony.addRobot(drone);
        colony.addFacility(habitat);
        colony.addFacility(laboratory);
        colony.addFacility(communications);
        colony.addFacility(storage);
        colony.addFacility(power);
        return colony;
    }
}
