package admingui;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import colony.Colony;
import enums.Capability;
import enums.EnviromentStates;
import enums.OperationalStatus;
import enums.Resources;
import enums.Skills;
import facilities.CommunicationStation;
import facilities.Equipment;
import facilities.Habitat;
import facilities.Laboratory;
import facilities.PowerStation;
import facilities.StorageFacility;
import marsenvironment.MarsEnvironment;
import personnel.Engineer;
import personnel.MedicalOfficer;
import personnel.Scientist;
import roboticunit.Drone;
import roboticunit.ExplorationRover;
import roboticunit.RepairRobot;

/** Builds a ready-to-explore colony so the admin console never opens empty. */
final class SampleColonyFactory {

    private SampleColonyFactory() {
    }

    record ColonyBundle(Colony colony, MarsEnvironment environment) {
    }

    static ColonyBundle buildColony() {
        Colony colony = new Colony();

        Habitat habitat = new Habitat("Habitat-Alpha", 50.0, OperationalStatus.OPERATIONAL,
                Map.of(Resources.OXYGEN, 40.0, Resources.WATER, 30.0, Resources.FOOD, 25.0,
                        Resources.SPARE_PARTS, 6.0, Resources.MEDICAL_SUPPLIES, 5.0),
                List.of(), List.of());
        Laboratory laboratory = new Laboratory("Laboratory-1", 20.0, OperationalStatus.OPERATIONAL,
                Map.of(Resources.BATTERY_POWER, 10.0),
                List.of(new Equipment("Spectrometer", OperationalStatus.OPERATIONAL, Equipment.Category.SCIENTIFIC)),
                List.of());
        PowerStation powerStation = new PowerStation("Power-Station-1", 100.0, OperationalStatus.OPERATIONAL,
                Map.of(Resources.BATTERY_POWER, 50.0), List.of(), List.of(), 5.0);
        StorageFacility storage = new StorageFacility("Storage-1", 200.0, OperationalStatus.OPERATIONAL,
                Map.of(Resources.FUEL, 40.0, Resources.SPARE_PARTS, 10.0), List.of(), List.of());
        CommunicationStation commsStation = new CommunicationStation("Comms-1", 10.0,
                OperationalStatus.OPERATIONAL, Map.of(), List.of(), List.of());

        colony.addFacility(habitat);
        colony.addFacility(laboratory);
        colony.addFacility(powerStation);
        colony.addFacility(storage);
        colony.addFacility(commsStation);

        Engineer yara = new Engineer("Yara Hazem", 101, "Healthy", "Habitat-Alpha",
                EnumSet.of(Skills.MECHANICAL, Skills.COMMUNICATION), 10.0, true);
        Scientist mariam = new Scientist("Mariam Adel", 102, "Healthy", "Habitat-Alpha",
                EnumSet.of(Skills.SCIENTIFIC, Skills.COMMUNICATION), 5.0, true);
        MedicalOfficer karim = new MedicalOfficer("Karim Abdel Rahman", 103, "Healthy", "Habitat-Alpha",
                EnumSet.of(Skills.MEDICAL, Skills.FIRE_RESPONSE), 0.0, true);
        Engineer ahmed = new Engineer("Ahmed Fathy", 104, "Healthy", "Habitat-Alpha",
                EnumSet.of(Skills.MECHANICAL), 8.0, true);
        Scientist nourhan = new Scientist("Nourhan Samir", 105, "Healthy", "Habitat-Alpha",
                EnumSet.of(Skills.SCIENTIFIC), 3.0, true);

        colony.addPersonnel(yara);
        colony.addPersonnel(mariam);
        colony.addPersonnel(karim);
        colony.addPersonnel(ahmed);
        colony.addPersonnel(nourhan);

        ExplorationRover rover = new ExplorationRover(100.0, "Operational", "Habitat-Alpha", true,
                EnumSet.of(Capability.SURFACE_TRAVEL, Capability.EQUIPMENT_CARRYING, Capability.EXPLORATION));
        rover.setEquipment(List.of(new Equipment("Survey Kit", OperationalStatus.OPERATIONAL)));
        RepairRobot repairRobot = new RepairRobot(100.0, "Operational", "Habitat-Alpha", true);
        Drone drone = new Drone(100.0, "Operational", "Habitat-Alpha", true);

        colony.addRobot(rover);
        colony.addRobot(repairRobot);
        colony.addRobot(drone);

        MarsEnvironment environment = new MarsEnvironment(EnviromentStates.NORMAL_CONDITION);
        colony.setMarsEnvironment(environment);

        return new ColonyBundle(colony, environment);
    }

    static ColonyBundle buildEmptyColony() {
        Colony colony = new Colony();
        MarsEnvironment environment = new MarsEnvironment(EnviromentStates.NORMAL_CONDITION);
        colony.setMarsEnvironment(environment);
        return new ColonyBundle(colony, environment);
    }
}
