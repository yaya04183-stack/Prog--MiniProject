package roboticunit;
import enums.Capability;
import java.util.Set;
import java.util.EnumSet;

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
    capabilities
    );
}




}
