package roboticunit;
import enums.Capability;
import java.util.Set;


import missions.Mission;

public class ExplorationRover extends RoboticUnit{

public ExplorationRover(
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
        Capability.SURFACE_TRAVEL,
        Capability.EQUIPMENT_CARRYING,
        Capability.EXPLORATION
    )
    );
}






}
