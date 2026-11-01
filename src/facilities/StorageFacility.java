package facilities;

import agents.Agent;
import enums.FacilityType;
import enums.OperationalStatus;
import enums.Resources;
import java.util.List;
import java.util.Map;

public class StorageFacility extends Facilities
{
    // Initializes a StorageFacility instance.
    // Initializes a StorageFacility instance.
    public StorageFacility(String location, double capacity, OperationalStatus status,
                           Map<Resources, Double> resources, List<Equipment> equipment,
                           List<Agent> occupants)
    {
        super(FacilityType.STORAGE, location, capacity, status, resources, equipment, occupants);
    }

    // Getters

    // Returns total stored.
    public double getTotalStored()
    {
        double total = 0;
        for (double amount : getResources().values())
            total += amount;
        return total;
    }

    // Handles store.
    public boolean store(Resources r, double amount)
    {
        if (!getOperationalStatus().isOperational()) return false;
        if (getTotalStored() + amount > getCapacity()) return false;
        addResource(r, amount);
        return true;
    }
}
