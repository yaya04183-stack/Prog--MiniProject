package roboticunit;
import enums.Capability;
import java.util.EnumSet;

public class ExplorationRover extends RoboticUnit{

// Initializes an ExplorationRover with its fixed exploration capabilities.
public ExplorationRover(
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
        Capability.SURFACE_TRAVEL,
        Capability.EQUIPMENT_CARRYING,
        Capability.EXPLORATION
    )
    );
}




}
