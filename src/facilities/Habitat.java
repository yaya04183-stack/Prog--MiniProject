package facilities;

import agents.Agent;
import enums.FacilityType;
import enums.OperationalStatus;
import enums.Resources;
import java.util.List;
import java.util.Map;

public class Habitat extends Facilities
{
    // Composition: created INSIDE the habitat, never passed in
    private final LifeSupportSystem lifeSupport;

    public Habitat(String location, double capacity, OperationalStatus status,
                Map<Resources, Double> resources, List<Equipment> equipment,
                List<Agent> occupants)
    {
        super(FacilityType.HABITAT, location, capacity, status, resources, equipment, occupants);
        this.lifeSupport = new LifeSupportSystem();
    }

    public LifeSupportSystem getLifeSupport() { return lifeSupport; }

    // Oxygen leak: removes oxygen from THIS habitat only (never below zero)
    public double applyOxygenLoss(double amount)
    {
        return deductResource(Resources.OXYGEN, amount);
    }
}
