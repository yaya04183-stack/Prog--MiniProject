package roboticunit;
import enums.Capability;
import java.util.EnumSet;

public class Drone extends RoboticUnit{

public Drone(
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
        Capability.AERIAL_RECONNAISSANCE,
        Capability.ACCESS_INACCESSIBLE_AREAS
    )
    );
}


}
