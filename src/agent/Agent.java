package agent;

import emergencies.EmergencyType;
import missions.Mission;

public interface Agent {

    boolean canPerform(Mission mission);

    boolean isAvailable();

    void makeAvailable();

    void setAvailability(boolean availability);

    String getCurrentLocation();

    void setCurrentLocation(String currentLocation);

    boolean canHandleEmergency(EmergencyType emergencyType);
}