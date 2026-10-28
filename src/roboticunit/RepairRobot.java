package roboticunit;

import enums.Capability;
import java.util.EnumSet;

public class RepairRobot extends RoboticUnit {

public RepairRobot(
    double batteryLevel,
    String healthStatus,
    String currentLocation,
    boolean availability
)
{
    super(batteryLevel,
    healthStatus,
    currentLocation,
    availability,
        EnumSet.of(
        Capability.EQUIPMENT_REPAIR,
        Capability.FACILITY_OPERATIONS
    ));
}




}
