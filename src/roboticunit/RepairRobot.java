package roboticunit;
import enums.Capability;
import java.util.Set;



public class RepairRobot extends RoboticUnit{

public RepairRobot(
    double batteryLevel,
    String healthStatus,
    String currentLocation,
    boolean availability,
    Set<Capability> capabilities
)
{
    super(batteryLevel,
    healthStatus,
    currentLocation,
    availability,
        EnumSet.of(
        Capability.EQUIPMENT_REPAIR,
        Capability.FACILITY_OPERATIONS,
    ));
}






}
