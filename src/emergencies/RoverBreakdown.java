package emergencies;

import enums.EmergencyType;
import roboticunit.ExplorationRover;
import agents.Agent;
import java.util.List;


public class RoverBreakdown  extends Emergency {

    private ExplorationRover affected_facility;

    // Initializes a RoverBreakdown instance.
    // Initializes a RoverBreakdown instance.
    public RoverBreakdown(ExplorationRover affected_facility){
        super(EmergencyType.ROVER_BREAKDOWN, "Martian Surface");
        setAffected_facility(affected_facility);
    }

    // Getters

    // Returns affected facility.
    public ExplorationRover getAffected_facility() {
        return affected_facility;
    }

    // Setters

    // Sets affected facility.
    public void setAffected_facility(ExplorationRover affected_facility) {
        if(affected_facility==null){
            throw new IllegalArgumentException("Invalid affected facility");
        }
        this.affected_facility = affected_facility;
    }

    // Handles occurrence.
    @Override
    protected void onOccurrence() {
        if (this.affected_facility.getMission() != null) {
            this.affected_facility.getMission().failMission("Rover breakdown");
        }
        this.affected_facility.setHealthStatus("DAMAGED");
        this.affected_facility.setAvailability(false);
    }

    // Applies response.
    @Override
    public boolean applyResponse() {
        return true;
    }

    // Checks whether it has required responders.
    @Override
    public boolean hasRequiredResponders(List<Agent> responders) {
        return super.hasRequiredResponders(responders);
    }
}
