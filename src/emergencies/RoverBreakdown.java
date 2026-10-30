package emergencies;

import java.text.BreakIterator;

import enums.EmergencyType;
import facilities.Facilities;
import roboticunit.ExplorationRover;


public class RoverBreakdown  extends Emergency {

    private ExplorationRover affected_facility;

    public RoverBreakdown(ExplorationRover affected_facility){
        super(EmergencyType.ROVER_BREAKDOWN);
        setAffected_facility(affected_facility);
    }

    public ExplorationRover getAffected_facility() {
        return affected_facility;
    }

    public void setAffected_facility(ExplorationRover affected_facility) {
        if(affected_facility==null){
            throw new IllegalArgumentException("Invalid affected facility");
        }
        this.affected_facility = affected_facility;
    }
}
