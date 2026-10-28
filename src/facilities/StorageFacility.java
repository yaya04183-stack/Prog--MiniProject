package facilities;

import java.util.List;
import java.util.Map;
import resources.Resources;

public class StorageFacility extends Facilities
{
    public StorageFacility(String location, double capacity, OperationalStatus status,
                           Map<Resources, Double> resources, StringBuilder equipment,
                           List<Agent> occupants)
    {
        super(FacilityType.STORAGE, location, capacity, status, resources, equipment, occupants);
    }

    public double getTotalStored()
    {
        double total = 0;
        for (double amount : getResources().values())
            total += amount;
        return total;
    }

    // Returns false if full or not operational
    public boolean store(Resources r, double amount)
    {
        if (!getOperationalStatus().isOperational()) return false;
        if (getTotalStored() + amount > getCapacity()) return false;
        addResource(r, amount);
        return true;
    }
}