package roboticunit;

import missions.Mission;
import enums.Capability;
import agents.Agent;
import enums.EmergencyType;
import java.util.Set;
import java.util.HashSet;


public abstract class RoboticUnit implements Agent {

    private double batteryLevel;
    private String healthStatus;
    private String currentLocation;
    private boolean availability;
    private Set<Capability> capabilities;

    public RoboticUnit(
            double batteryLevel,
            String healthStatus,
            String currentLocation,
            boolean availability,
            Set<Capability> capabilities) {

        if (batteryLevel < 0 || batteryLevel > 100) {
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

    // Setters

    public void setBatteryLevel(double batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100) {
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
        this.availability = availability;
    }

    public void setCapabilities(Set<Capability> capabilities) {
        if (capabilities == null
                || capabilities.isEmpty()
                || capabilities.contains(null)) {
            throw new IllegalArgumentException("Invalid Capabilities");
        }

        this.capabilities = new HashSet<>(capabilities);
    }

    // Agent methods

    @Override
    public void makeAvailable() {
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
                && getBatteryLevel() > 0
                && getCapabilities().containsAll(
                        mission.getRequiredCapabilities()
                );
    }

    @Override
    public boolean canHandleEmergency(EmergencyType emergencyType) {
        if (emergencyType == null) {
            return false;
        }

        switch (emergencyType) {
            case OXYGEN_LEAK:
            case ROVER_BREAKDOWN:
                return capabilities.contains(Capability.EQUIPMENT_REPAIR);
            default:
                return false;
        }
    }
}
