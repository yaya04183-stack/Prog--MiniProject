package agents;

import enums.EmergencyType;
import enums.Capability;
import enums.Skills;
import missions.Mission;

public interface Agent {

    // Checks whether it can perform.
    boolean canPerform(Mission mission);

    // Checks whether it has enough battery for.
    default boolean hasEnoughBatteryFor(Mission mission) {
        return mission != null;
    }

    // Consumes battery for time unit.
    default boolean consumeBatteryForTimeUnit() {
        return true;
    }

    // Checks whether it has skill.
    default boolean hasSkill(Skills skill) {
        return false;
    }

    // Checks whether it has capability.
    default boolean hasCapability(Capability capability) {
        return false;
    }

    // Checks whether available.
    boolean isAvailable();

    // Makes available.
    void makeAvailable();

    // Sets availability.
    void setAvailability(boolean availability);

    // Returns current location.
    String getCurrentLocation();

    // Assigns mission.
    void  assignMission(Mission mission);

    // Sets current location.
    void setCurrentLocation(String currentLocation);

    // Checks whether it can handle emergency.
    boolean canHandleEmergency(EmergencyType emergencyType);
}
