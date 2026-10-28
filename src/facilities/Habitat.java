package facilities;

import java.util.List;
import java.util.Map;
import resources.Resources;

public class Habitat extends Facilities
{
    // Composition: created INSIDE the habitat, never passed in
    private final LifeSupportSystem lifeSupport;

    public Habitat(String location, double capacity, OperationalStatus status,
                   Map<Resources, Double> resources, StringBuilder equipment,
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