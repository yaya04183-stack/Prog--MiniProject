package missions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import enums.Mission_Emergency_States;
import enums.Resources;
import enums.Capability;
import agents.Agent;
import facilities.Facilities;

public abstract class Mission {

    private List<Agent> agents;
    private int neededtimesunits;
    private Facilities facility;
    private Mission_Emergency_States state;
    private Map<Resources,Double> requiredResources;
    private Set<Capability> requiredCapabilities;
    private String failureReason;

    public Mission(
            int neededtimesunits,
            Facilities facility,
            Map<Resources, Double> requiredResources,
            Set<Capability> requiredCapabilities) {

        if (requiredResources == null) {
            throw new IllegalArgumentException(
                    "Invalid required resources");
        }

        for (Map.Entry<Resources, Double> entry
                : requiredResources.entrySet()) {

            if (entry.getKey() == null
                    || entry.getValue() == null
                    || entry.getValue() <= 0) {

                throw new IllegalArgumentException(
                        "Invalid required resources");
            }
        }

        this.requiredResources = new HashMap<>(requiredResources);

        if (requiredCapabilities == null
                || requiredCapabilities.contains(null)) {

            throw new IllegalArgumentException(
                    "Invalid required capabilities");
        }

        this.requiredCapabilities =
                new HashSet<>(requiredCapabilities);

        this.agents = new ArrayList<>();

        if (neededtimesunits <= 0) {
            throw new IllegalArgumentException("Invalid time units");
        }

        this.neededtimesunits = neededtimesunits;
        this.state = null;
        this.facility = facility;
    }

    // Getters

    public List<Agent> getAgents() {
        return new ArrayList<>(this.agents);
    }

    public int getNeededTimesUnits() {
        return this.neededtimesunits;
    }

    public Mission_Emergency_States getState() {
        return this.state;
    }

    public Facilities getFacility() {
        return this.facility;
    }

    public Map<Resources, Double> getRequiredResources() {
        return new HashMap<>(this.requiredResources);
    }

    public Set<Capability> getRequiredCapabilities() {
        return new HashSet<>(this.requiredCapabilities);
    }

    public String getFailureReason(){
        return this.failureReason;
    }

    // Setters

    public void setFacility(Facilities facility) {
        this.facility = facility;
    }


    public void setNeededTimeUnits(int neededtimesunits) {
        if (neededtimesunits <= 0) {
            throw new IllegalArgumentException("Invalid time units");
        }

        this.neededtimesunits = neededtimesunits;
    }

    public void setState(Mission_Emergency_States state) {
        if (state == null) {
            throw new IllegalArgumentException("Invalid state");
        }

        this.state = state;
    }

    public void setRequiredCapabilities(
            Set<Capability> capabilities) {

        if (capabilities == null || capabilities.contains(null)) {
            throw new IllegalArgumentException(
                    "Invalid required capabilities");
        }

        this.requiredCapabilities =
                new HashSet<>(capabilities);
    }

    public void setFailureReason(String failureReason){
        if(failureReason == null || failureReason.isBlank()){
            throw new IllegalArgumentException("Invalid failure reason");
        }
        this.failureReason = failureReason;
    }

    public void failMission(String reason){
        if(reason == null || reason.isBlank()){
            throw new IllegalArgumentException("Invalid failure reason");
        }
        this.state = Mission_Emergency_States.FAILED;
        this.failureReason = reason;

        for (Agent agent : this.agents){
            agent.makeAvailable();
        }
    }
    public void rejectMission(String reason) {
    if (reason == null || reason.isBlank()) {
        throw new IllegalArgumentException("Invalid rejection reason");
    }

    this.state = Mission_Emergency_States.FAILED;
    this.failureReason = reason;
}



    // Assign an agent

    public void assign(Agent agent) {
        if (agent == null) {
            throw new IllegalArgumentException("Invalid agent");
        }

        if(this.agents.contains(agent)){
            throw new IllegalArgumentException("Agent already assigned");
        }

        if(!agent.isAvailable()){
            throw new IllegalArgumentException("Agent is not available");
        }

        if(!agent.canPerform(this)){
            throw new IllegalArgumentException("Agent cannot perform this mission");
        }

        this.agents.add(agent);
        agent.setAvailability(false);
        this.state = Mission_Emergency_States.ASSIGNED;
    }

    // Check required resources against available resources

    public boolean checkresources(
            Map<Resources, Double> availableResources) {

        if (availableResources == null) {
            return false;
        }

        for (Map.Entry<Resources, Double> entry
                : this.requiredResources.entrySet()) {

            Double available =
                    availableResources.get(entry.getKey());

            if (available == null
                    || available < entry.getValue()) {
                return false;
            }
        }
        return true;
    }


    // After the end of the mission we must free the Agent

    public void finishMission(boolean successful){
        if(this.state != Mission_Emergency_States.INPROGRESS){
            throw new IllegalStateException("Mission is not in progress");
        }
        if(successful){
            this.state = Mission_Emergency_States.COMPLETED;
        }
        else{
            this.state = Mission_Emergency_States.FAILED;
        }
        for(Agent agent : this.agents){
            agent.makeAvailable();
        }
    }


    // Change the status of the mission
    public void startMission() {

    if (this.state != Mission_Emergency_States.ASSIGNED) {
        throw new IllegalStateException(
                "Mission is not assigned");
    }

    this.state = Mission_Emergency_States.INPROGRESS;
}

}
