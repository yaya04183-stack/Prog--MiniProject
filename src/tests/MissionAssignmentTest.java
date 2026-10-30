
package tests;

import java.util.List;
import java.util.Map;
import java.util.Set;

import agents.Agent;
import colony.Colony;
import enums.EmergencyType;
import enums.Mission_Emergency_States;
import missions.Mission;

public class MissionAssignmentTest {

    static class TestAgent implements Agent {

        private boolean available = true;
        private Mission mission;
        private final boolean shouldFail;

        TestAgent(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public boolean canPerform(Mission mission) {
            return mission != null && available;
        }

        @Override
        public boolean isAvailable() {
            return available;
        }

        @Override
        public void makeAvailable() {
            available = true;
            mission = null;
        }

        @Override
        public void setAvailability(boolean availability) {
            this.available = availability;
        }

        @Override
        public String getCurrentLocation() {
            return "Test Location";
        }

        @Override
        public void setCurrentLocation(String location) {}

        @Override
        public boolean canHandleEmergency(EmergencyType type) {
            return false;
        }

        @Override
        public void assignMission(Mission mission) {
            if (shouldFail) {
                throw new RuntimeException("Simulated assignment failure");
            }

            this.mission = mission;
            this.available = false;
        }
    }

    private static Mission createMission() {
        return new Mission(5, null, Map.of(), Set.of()) {};
    }

    private static boolean testRollback() {

        Colony colony = new Colony();
        Mission mission = createMission();

        TestAgent agent1 = new TestAgent(false);
        TestAgent agent2 = new TestAgent(true);

        boolean result = colony.submitMission(
                mission, List.of(agent1, agent2)
        );

        return !result
                && agent1.isAvailable()
                && agent2.isAvailable()
                && mission.getAgents().isEmpty()
                && mission.getState() == Mission_Emergency_States.FAILED
                && colony.getActiveMission() == null;
    }

    private static boolean testSuccessfulLifecycle() {

        Colony colony = new Colony();
        Mission mission = createMission();
        TestAgent agent = new TestAgent(false);

        boolean submitted = colony.submitMission(
                mission, List.of(agent)
        );

        boolean started = colony.startActiveMission();

        boolean correctStart =
                mission.getState() == Mission_Emergency_States.INPROGRESS
                && !agent.isAvailable();

        boolean finished = colony.finishActiveMission(true);

        return submitted
                && started
                && correctStart
                && finished
                && mission.getState() == Mission_Emergency_States.COMPLETED
                && agent.isAvailable()
                && mission.getAgents().isEmpty()
                && colony.getActiveMission() == null;
    }

    private static boolean testFailedLifecycle() {

        Colony colony = new Colony();
        Mission mission = createMission();
        TestAgent agent = new TestAgent(false);

        boolean submitted = colony.submitMission(
                mission, List.of(agent)
        );

        boolean started = colony.startActiveMission();
        boolean finished = colony.finishActiveMission(false);

        return submitted
                && started
                && finished
                && mission.getState() == Mission_Emergency_States.FAILED
                && agent.isAvailable()
                && mission.getAgents().isEmpty()
                && colony.getActiveMission() == null;
    }

    private static boolean testFailActiveMission() {

        Colony colony = new Colony();
        Mission mission = createMission();
        TestAgent agent = new TestAgent(false);

        boolean submitted = colony.submitMission(
                mission, List.of(agent)
        );

        boolean started = colony.startActiveMission();
        boolean failed = colony.failActiveMission("Test failure");

        return submitted
                && started
                && failed
                && mission.getState() == Mission_Emergency_States.FAILED
                && "Test failure".equals(mission.getFailureReason())
                && agent.isAvailable()
                && mission.getAgents().isEmpty()
                && colony.getActiveMission() == null;
    }

    public static void main(String[] args) {

        System.out.println("Rollback Test: "
                + (testRollback() ? "PASSED" : "FAILED"));

        System.out.println("Successful Lifecycle Test: "
                + (testSuccessfulLifecycle() ? "PASSED" : "FAILED"));

        System.out.println("Failed Lifecycle Test: "
                + (testFailedLifecycle() ? "PASSED" : "FAILED"));

        System.out.println("Fail Active Mission Test: "
                + (testFailActiveMission() ? "PASSED" : "FAILED"));
    }
}
