package emergencies;

import enums.EmergencyType;
import roboticunit.ExplorationRover;
import agents.Agent;
import java.util.List;


public class RoverBreakdown  extends Emergency {

    private ExplorationRover affected_facility;

    public RoverBreakdown(ExplorationRover affected_facility){
        super(EmergencyType.ROVER_BREAKDOWN, "Martian Surface");
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

    @Override
    protected void onOccurrence() {
        if (this.affected_facility.getMission() != null) {
            this.affected_facility.getMission().failMission("Rover breakdown");
        }
        this.affected_facility.setHealthStatus("DAMAGED");
        this.affected_facility.setAvailability(false);
    }

    @Override
    public boolean applyResponse() {
        return true;
    }

    @Override
    public boolean hasRequiredResponders(List<Agent> responders) {
        return super.hasRequiredResponders(responders);
    }
}
