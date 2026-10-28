package facilities;

import agents.Agent;
import java.util.List;
import java.util.Map;
import resources.Resources;

public class PowerStation extends Facilities
{
    private final double outputPerTimeUnit;

    public PowerStation(String location, double capacity, OperationalStatus status,
                        Map<Resources, Double> resources, StringBuilder equipment,
                        List<Agent> occupants, double outputPerTimeUnit)
    {
        super(FacilityType.POWER_STATION, location, capacity, status, resources, equipment, occupants);
        if (outputPerTimeUnit < 0)
            throw new IllegalArgumentException("Invalid output");
        this.outputPerTimeUnit = outputPerTimeUnit;
    }

    // The simulation calls this every time unit
    public void generatePower()
    {
        if (getOperationalStatus().isOperational())
            addResource(Resources.BATTERY_POWER, outputPerTimeUnit);
    }
}