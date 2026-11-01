package facilities;

import agents.Agent;
import enums.FacilityType;
import enums.OperationalStatus;
import enums.Resources;
import java.util.List;
import java.util.Map;

public class PowerStation extends Facilities
{
    private final double outputPerTimeUnit;

    // Initializes a PowerStation instance.
    // Initializes a PowerStation instance.
    public PowerStation(String location, double capacity, OperationalStatus status,
                        Map<Resources, Double> resources, List<Equipment> equipment,
                        List<Agent> occupants, double outputPerTimeUnit)
    {
        super(FacilityType.POWER_STATION, location, capacity, status, resources, equipment, occupants);
        if (outputPerTimeUnit < 0 || !Double.isFinite(outputPerTimeUnit))
            throw new IllegalArgumentException("Invalid output");
        this.outputPerTimeUnit = outputPerTimeUnit;
    }

    // The simulation calls this every time unit
    // Generates power.
    public void generatePower()
    {
        if (getOperationalStatus().isOperational())
            addResource(Resources.BATTERY_POWER, outputPerTimeUnit);
    }
}
