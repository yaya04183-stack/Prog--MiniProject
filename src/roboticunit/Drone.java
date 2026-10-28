package roboticunit;
import enums.Capability;
import java.util.Set;


import missions.Mission;

public class Drone extends RoboticUnit{

public Drone(
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
        Capability.AERIAL_RECONNAISSANCE,
        Capability.ACCESS_INACCESSIBLE_AREAS
    ));
}






}
