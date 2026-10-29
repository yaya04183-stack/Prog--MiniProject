package facilities;

import agents.Agent;
import enums.FacilityType;
import enums.OperationalStatus;
import enums.Resources;
import java.util.List;
import java.util.Map;

public class CommunicationStation extends Facilities
{
    public CommunicationStation(String location, double capacity, OperationalStatus status,
                                Map<Resources, Double> resources, StringBuilder equipment,
                                List<Agent> occupants)
    {
        super(FacilityType.COMMUNICATION_STATION, location, capacity, status, resources, equipment, occupants);
    }

    // Rescue missions and drones will ask this
    public boolean isCommunicationAvailable()
    {
        return getOperationalStatus().isOperational();
    }
}
