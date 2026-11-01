package facilities;

import agents.Agent;
import enums.FacilityType;
import enums.OperationalStatus;
import enums.Resources;
import java.util.List;
import java.util.Map;

public class Laboratory extends Facilities
{
    private boolean onFire = false;

    // Initializes a Laboratory instance.
    // Initializes a Laboratory instance.
    public Laboratory(String location, double capacity, OperationalStatus status,
                      Map<Resources, Double> resources, List<Equipment> equipment,
                      List<Agent> occupants)
    {
        super(FacilityType.LABORATORY, location, capacity, status, resources, equipment, occupants);
    }

    // Getters

    // Checks whether on fire.
    public boolean isOnFire() { return onFire; }

    // Handles catch fire.
    public void catchFire()
    {
        onFire = true;
        setOperationalStatus(OperationalStatus.UNAVAILABLE);
    }

    // Handles extinguish fire.
    public void extinguishFire()
    {
        onFire = false;
        setOperationalStatus(OperationalStatus.DAMAGED);
    }

    // Handles repair.
    public void repair()
    {
        setOperationalStatus(OperationalStatus.OPERATIONAL);
    }
}
