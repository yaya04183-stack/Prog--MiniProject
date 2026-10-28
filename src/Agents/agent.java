package agents; 

import missions.Mission;
import emergencies.EmergencyType;

public interface Agent {

    boolean canPerform(Mission mission);

    boolean isAvailable();

    void setAvailability(boolean availability);

    String getCurrentLocation();

    void setCurrentLocation(String currentLocation);

    boolean canHandleEmergency(EmergencyType emergencyType);
}
