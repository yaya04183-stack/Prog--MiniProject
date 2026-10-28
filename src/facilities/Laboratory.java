package facilities;

import agents.Agent;
import java.util.List;
import java.util.Map;
import resources.Resources;

public class Laboratory extends Facilities
{
    private boolean onFire = false;

    public Laboratory(String location, double capacity, OperationalStatus status,
                      Map<Resources, Double> resources, StringBuilder equipment,
                      List<Agent> occupants)
    {
        super(FacilityType.LABORATORY, location, capacity, status, resources, equipment, occupants);
    }

    public boolean isOnFire() { return onFire; }

    public void catchFire()
    {
        onFire = true;
        setOperationalStatus(OperationalStatus.UNAVAILABLE);
    }

    public void extinguishFire()
    {
        onFire = false;
        setOperationalStatus(OperationalStatus.DAMAGED);
    }

    public void repair()
    {
        setOperationalStatus(OperationalStatus.OPERATIONAL);
    }
}