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

    // Initializes a RoboticUnit instance.
    // Initializes a RoboticUnit instance.
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



    // Checks whether it has operational equipment.
    public boolean hasOperationalEquipment() {
        return this.equipment.stream().anyMatch(item ->
                item.getOperationalStatus() == enums.OperationalStatus.OPERATIONAL);
    }

    // Checks whether it has capability.
    @Override
    public boolean hasCapability(Capability capability) {
        return capability != null && this.capabilities.contains(capability);
    }




    // Agent methods

    // Makes available.
    @Override
    public void makeAvailable() {
        this.mission = null;
        setAvailability(true);
    }


    // Checks whether it can perform.
    @Override
    public boolean canPerform(Mission mission) {
        return mission != null
                && isAvailable()
                && hasEnoughBatteryFor(mission)
                && getCapabilities().containsAll(
                        mission.getRequiredCapabilities()
                );
    }

    // Checks whether it has enough battery for.
    @Override
    public boolean hasEnoughBatteryFor(Mission mission) {
        return mission != null
                && this.batteryLevel >= mission.getNeededTimesUnits()
                        * BATTERY_COST_PER_TIME_UNIT;
    }

    // Consumes battery for time unit.
    @Override
    public boolean consumeBatteryForTimeUnit() {
        if (this.batteryLevel < BATTERY_COST_PER_TIME_UNIT) {
            return false;
        }
        this.batteryLevel -= BATTERY_COST_PER_TIME_UNIT;
        return true;
    }

    // Checks whether it can handle emergency.
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

    // Assigns mission.
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
    // Getters

    // Returns battery level.
    public double getBatteryLevel() {
        return this.batteryLevel;
    }
    // Returns health status.
    public String getHealthStatus() {
        return this.healthStatus;
    }
    // Returns current location.
    public String getCurrentLocation() {
        return this.currentLocation;
    }
    // Returns availability.
    public boolean getAvailability() {
        return this.availability;
    }
    // Returns capabilities.
    public Set<Capability> getCapabilities() {
        return new HashSet<>(this.capabilities);
    }
    // Returns equipment.
    public List<Equipment> getEquipment() {
        return new ArrayList<>(this.equipment);
    }
    // Returns mission.
    public Mission getMission(){
        return this.mission;
    }
    // Checks whether available.
    @Override
    public boolean isAvailable() {
        return getAvailability();
    }

    // Setters

    // Sets battery level.
    public void setBatteryLevel(double batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100 || !Double.isFinite(batteryLevel)) {
            throw new IllegalArgumentException("Invalid Battery Level");
        }

        this.batteryLevel = batteryLevel;
    }
    // Sets health status.
    public void setHealthStatus(String healthStatus) {
        if (healthStatus == null || healthStatus.isBlank()) {
            throw new IllegalArgumentException("Invalid Status");
        }

        this.healthStatus = healthStatus;
    }
    // Sets current location.
    public void setCurrentLocation(String currentLocation) {
        if (currentLocation == null || currentLocation.isBlank()) {
            throw new IllegalArgumentException("Invalid Current Location");
        }

        this.currentLocation = currentLocation;
    }
    // Sets availability.
    public void setAvailability(boolean availability) {
        if (this.mission != null && availability) {
            throw new IllegalStateException("Robot is still assigned to a mission");
        }
        this.availability = availability;
    }
    // Sets capabilities.
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
    // Sets equipment.
    public void setEquipment(List<Equipment> equipment) {
        if (equipment == null || equipment.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException("Invalid Equipment");
        }
        this.equipment = new ArrayList<>(equipment);
    }

}
