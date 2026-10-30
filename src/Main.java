import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import agents.Agent;
import colony.Colony;
import emergencies.OxygenLeak;
import enums.Capability;
import enums.EnviromentStates;
import enums.Mission_Emergency_States;
import enums.OperationalStatus;
import enums.Resources;
import enums.Skills;
import facilities.Equipment;
import facilities.Habitat;
import marsenvironment.MarsEnvironment;
import missions.Exploration;
import missions.Mission;
import personnel.Engineer;
import personnel.Scientist;
import roboticunit.ExplorationRover;
import roboticunit.RepairRobot;
import simulation.Simulation;

/** Runs the five demonstration scenarios required by the project PDF. */
public class Main {

    public static void main(String[] args) {
        demonstrateSuccessfulExploration();
        demonstrateMissionRejection();
        demonstrateResourceShortage();
        demonstrateOxygenLeakResponse();
        demonstrateDustStormEffect();
    }

    private static void demonstrateSuccessfulExploration() {
        printHeading("Scenario 1: Successful exploration");
        Simulation.resettime();

        Colony colony = new Colony();
        Habitat habitat = createHabitat("Habitat-A", 10.0, 2.0);
        colony.addFacility(habitat);
        List<Agent> team = addExplorationTeam(colony, "Habitat-A", 101);
        Exploration mission = createExplorationMission(habitat, 2.0);

        printMissionSnapshot("Before submission", mission, colony, team);
        boolean accepted = colony.submitMission(mission, team);
        System.out.println("Submission accepted: " + accepted);
        boolean started = accepted && colony.startActiveMission();
        System.out.println("Mission started: " + started);

        if (started) {
            Simulation.advanceTime(colony, mission.getNeededTimesUnits());
        }

        printMissionSnapshot("After execution", mission, colony, team);
        Simulation.showtime();
    }

    private static void demonstrateMissionRejection() {
        printHeading("Scenario 2: Mission rejected for an unsuitable agent");

        Colony colony = new Colony();
        Habitat habitat = createHabitat("Habitat-B", 10.0, 2.0);
        colony.addFacility(habitat);

        Engineer unsuitableAgent = new Engineer(
                "Alex", 201, "Healthy", "Habitat-B",
                EnumSet.of(Skills.MECHANICAL), 0.0, true);
        colony.addPersonnel(unsuitableAgent);

        Mission mission = createExplorationMission(habitat, 1.0);
        boolean accepted = colony.submitMission(mission, List.of(unsuitableAgent));

        printMissionOutcome(mission, colony, accepted);
        System.out.println("Agent available after rejection: "
                + unsuitableAgent.isAvailable());
    }

    private static void demonstrateResourceShortage() {
        printHeading("Scenario 3: Mission rejected because resources are insufficient");

        Colony colony = new Colony();
        Habitat habitat = createHabitat("Habitat-C", 1.0, 2.0);
        colony.addFacility(habitat);
        List<Agent> team = addExplorationTeam(colony, "Habitat-C", 301);
        Mission mission = createExplorationMission(habitat, 5.0);
        double oxygenBefore = habitat.getResourceAmount(Resources.OXYGEN);

        boolean accepted = colony.submitMission(mission, team);

        printMissionOutcome(mission, colony, accepted);
        System.out.println("Oxygen before attempt: " + oxygenBefore);
        System.out.println("Oxygen after rejection: "
                + habitat.getResourceAmount(Resources.OXYGEN));
    }

    private static void demonstrateOxygenLeakResponse() {
        printHeading("Scenario 4: Oxygen leak emergency response");

        Colony colony = new Colony();
        Habitat habitat = createHabitat("Habitat-D", 10.0, 3.0);
        colony.addFacility(habitat);

        Engineer engineer = new Engineer(
                "Sam", 401, "Healthy", "Habitat-D",
                EnumSet.of(Skills.MECHANICAL), 0.0, true);
        RepairRobot repairRobot = new RepairRobot(
                100.0, "Operational", "Habitat-D", true);
        colony.addPersonnel(engineer);
        colony.addRobot(repairRobot);

        OxygenLeak emergency = new OxygenLeak(habitat, 2.0);
        double oxygenBefore = habitat.getResourceAmount(Resources.OXYGEN);
        double sparePartsBefore = habitat.getResourceAmount(Resources.SPARE_PARTS);
        boolean handled = colony.handleEmergency(emergency);

        System.out.println("Emergency handled: " + handled);
        System.out.println("Emergency status: " + emergency.getStatus());
        System.out.println("Leak repaired: " + emergency.isRepaired());
        System.out.println("Oxygen before/after: " + oxygenBefore + " / "
                + habitat.getResourceAmount(Resources.OXYGEN));
        System.out.println("Spare parts before/after: " + sparePartsBefore + " / "
                + habitat.getResourceAmount(Resources.SPARE_PARTS));
        System.out.println("Responders available afterward: "
                + engineer.isAvailable() + ", " + repairRobot.isAvailable());
    }

    private static void demonstrateDustStormEffect() {
        printHeading("Scenario 5: Dust storm affects exploration");

        Colony colony = new Colony();
        colony.setMarsEnvironment(new MarsEnvironment(EnviromentStates.DUST_STORM));
        Habitat habitat = createHabitat("Habitat-E", 10.0, 2.0);
        colony.addFacility(habitat);
        List<Agent> team = addExplorationTeam(colony, "Habitat-E", 501);
        Mission mission = createExplorationMission(habitat, 1.0);

        boolean accepted = colony.submitMission(mission, team);

        printMissionOutcome(mission, colony, accepted);
        System.out.println("Environment: " + EnviromentStates.DUST_STORM);
    }

    private static Habitat createHabitat(
            String location, double oxygen, double spareParts) {
        return new Habitat(
                location,
                50.0,
                OperationalStatus.OPERATIONAL,
                Map.of(Resources.OXYGEN, oxygen, Resources.SPARE_PARTS, spareParts),
                List.of(),
                List.of());
    }

    private static List<Agent> addExplorationTeam(
            Colony colony, String location, int firstId) {
        Scientist scientist = new Scientist(
                "Scientist-" + firstId, firstId, "Healthy", location,
                EnumSet.of(Skills.SCIENTIFIC), 0.0, true);
        ExplorationRover rover = new ExplorationRover(
                100.0,
                "Operational",
                location,
                true,
                EnumSet.of(
                        Capability.SURFACE_TRAVEL,
                        Capability.EQUIPMENT_CARRYING,
                        Capability.EXPLORATION));
        rover.setEquipment(List.of(
                new Equipment("Survey Kit", OperationalStatus.OPERATIONAL)));

        colony.addPersonnel(scientist);
        colony.addRobot(rover);
        return List.of(scientist, rover);
    }

    private static Exploration createExplorationMission(
            Habitat habitat, double requiredOxygen) {
        Set<Capability> roverCapabilities = EnumSet.of(
                Capability.SURFACE_TRAVEL,
                Capability.EQUIPMENT_CARRYING,
                Capability.EXPLORATION);
        return new Exploration(
                2,
                habitat,
                Map.of(Resources.OXYGEN, requiredOxygen),
                roverCapabilities,
                "North Ridge");
    }

    private static void printMissionSnapshot(
            String label, Mission mission, Colony colony, List<Agent> team) {
        System.out.println(label + ":");
        System.out.println("  Mission state: " + mission.getState());
        System.out.println("  Active mission: " + (colony.getActiveMission() == mission));
        System.out.println("  Colony oxygen: "
                + colony.getColonyResourceTotal().getOrDefault(Resources.OXYGEN, 0.0));
        System.out.println("  All agents available: "
                + team.stream().allMatch(Agent::isAvailable));
        if (mission.getFailureReason() != null) {
            System.out.println("  Failure reason: " + mission.getFailureReason());
        }
    }

    private static void printMissionOutcome(
            Mission mission, Colony colony, boolean accepted) {
        System.out.println("Submission accepted: " + accepted);
        System.out.println("Mission state: " + mission.getState());
        System.out.println("Failure reason: " + mission.getFailureReason());
        System.out.println("Mission active: " + (colony.getActiveMission() == mission));
        if (!accepted && mission.getState() != Mission_Emergency_States.FAILED) {
            System.out.println("Unexpected result: rejected mission is not marked FAILED.");
        }
    }

    private static void printHeading(String heading) {
        System.out.println();
        System.out.println("=== " + heading + " ===");
    }
}
