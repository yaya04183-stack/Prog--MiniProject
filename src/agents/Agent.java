package agents;

import enums.EmergencyType;
import enums.Capability;
import enums.Skills;
import missions.Mission;

public interface Agent {

    boolean canPerform(Mission mission);

    default boolean hasEnoughBatteryFor(Mission mission) {
        return mission != null;
    }

    default boolean consumeBatteryForTimeUnit() {
        return true;
    }

    default boolean hasSkill(Skills skill) {
        return false;
    }

    default boolean hasCapability(Capability capability) {
        return false;
    }

    boolean isAvailable();

    void makeAvailable();

    void setAvailability(boolean availability);

    String getCurrentLocation();

    void  assignMission(Mission mission);

    void setCurrentLocation(String currentLocation);

    boolean canHandleEmergency(EmergencyType emergencyType);
}
