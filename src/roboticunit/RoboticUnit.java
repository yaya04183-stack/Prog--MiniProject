package roboticunit;

import missions.Mission;
import enums.Capability;
import agents.Agent;
import enums.EmergencyType;
import facilities.Equipment;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;


public abstract class RoboticUnit implements Agent {

    private double batteryLevel;
    private String healthStatus;
    private String currentLocation;
    private boolean availability;
    private Set<Capability> capabilities;
    private Mission mission;
    private List<Equipment> equipment = new ArrayList<>();

    private static final double BATTERY_COST_PER_TIME_UNIT = 1.0;

    public RoboticUnit(
            double batteryLevel,
            String healthStatus,
            String currentLocation,
            boolean availability,
            Set<Capability> capabilities) {

        if (batteryLevel < 0 || batteryLevel > 100||!Double.isFinite(batteryLevel)) {
            throw new IllegalArgumentException("Invalid Battery Level");
        }
        this.batteryLevel = batteryLevel;

        if (healthStatus == null || healthStatus.isBlank()) {
            throw new IllegalArgumentException("Invalid Status");
        }
        this.healthStatus = healthStatus;

        if (currentLocation == null || currentLocation.isBlank()) {
            throw new IllegalArgumentException("Invalid Current Location");
        }
        this.currentLocation = currentLocation;

        this.availability = availability;

        setCapabilities(capabilities);
    }

    // Getters

    public double getBatteryLevel() {
        return this.batteryLevel;
    }

    public String getHealthStatus() {
        return this.healthStatus;
    }

    public String getCurrentLocation() {
        return this.currentLocation;
    }

    public boolean getAvailability() {
        return this.availability;
    }

    public Set<Capability> getCapabilities() {
        return new HashSet<>(this.capabilities);
    }

    public List<Equipment> getEquipment() {
        return new ArrayList<>(this.equipment);
    }

    public boolean hasOperationalEquipment() {
        return this.equipment.stream().anyMatch(item ->
                item.getOperationalStatus() == enums.OperationalStatus.OPERATIONAL);
    }

    @Override
    public boolean hasCapability(Capability capability) {
        return capability != null && this.capabilities.contains(capability);
    }

    public Mission getMission(){
        return this.mission;
    }

    // Setters

    public void setBatteryLevel(double batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100 || !Double.isFinite(batteryLevel)) {
            throw new IllegalArgumentException("Invalid Battery Level");
        }

        this.batteryLevel = batteryLevel;
    }

    public void setHealthStatus(String healthStatus) {
        if (healthStatus == null || healthStatus.isBlank()) {
            throw new IllegalArgumentException("Invalid Status");
        }

        this.healthStatus = healthStatus;
    }

    public void setCurrentLocation(String currentLocation) {
        if (currentLocation == null || currentLocation.isBlank()) {
            throw new IllegalArgumentException("Invalid Current Location");
        }

        this.currentLocation = currentLocation;
    }

    public void setAvailability(boolean availability) {
        if (this.mission != null && availability) {
            throw new IllegalStateException("Robot is still assigned to a mission");
        }
        this.availability = availability;
    }

    public void setCapabilities(Set<Capability> capabilities) {
        if (capabilities == null
                || capabilities.isEmpty()) {
            throw new IllegalArgumentException("Invalid Capabilities");
        }
        for (Capability capability : capabilities) {
        if (capability == null) {
            throw new IllegalArgumentException("Invalid Capabilities");
        }
    }
        this.capabilities = new HashSet<>(capabilities);
    }

    public void setEquipment(List<Equipment> equipment) {
        if (equipment == null || equipment.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException("Invalid Equipment");
        }
        this.equipment = new ArrayList<>(equipment);
    }

    // Agent methods

    @Override
    public void makeAvailable() {
        this.mission = null;
        setAvailability(true);
    }

    @Override
    public boolean isAvailable() {
        return getAvailability();
    }

    @Override
    public boolean canPerform(Mission mission) {
        return mission != null
                && isAvailable()
                && hasEnoughBatteryFor(mission)
                && getCapabilities().containsAll(
                        mission.getRequiredCapabilities()
                );
    }

    @Override
    public boolean hasEnoughBatteryFor(Mission mission) {
        return mission != null
                && this.batteryLevel >= mission.getNeededTimesUnits()
                        * BATTERY_COST_PER_TIME_UNIT;
    }

    @Override
    public boolean consumeBatteryForTimeUnit() {
        if (this.batteryLevel < BATTERY_COST_PER_TIME_UNIT) {
            return false;
        }
        this.batteryLevel -= BATTERY_COST_PER_TIME_UNIT;
        return true;
    }

    @Override
    public boolean canHandleEmergency(EmergencyType emergencyType) {
        if (emergencyType == null) {
            return false;
        }

        switch (emergencyType) {
            case OXYGEN_LEAK:
            case ROVER_BREAKDOWN:
                return isAvailable()
                        && this.batteryLevel > 0
                        && capabilities.contains(Capability.EQUIPMENT_REPAIR);
            default:
                return false;
        }
    }

    @Override 
    public void assignMission(Mission mission){
        if(mission == null){
            throw new IllegalArgumentException("Invalid Mission");
        }
        if(!mission.isAssigningAgent(this)){
            mission.assign(this);
            return;
        }
        if(!canPerform(mission)){
            throw new IllegalArgumentException("Cannot perform mission");
        }
        this.mission = mission;
        setAvailability(false);
    }
}
