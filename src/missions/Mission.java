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
import colony.Colony;
import facilities.Facilities;
import facilities.Laboratory;

public abstract class Mission {

    private List<Agent> agents;
    private int neededtimesunits;
    private int elapsedTimeUnits;
    private Facilities facility;
    private Mission_Emergency_States state;
    private Map<Resources,Double> requiredResources;
    private Set<Capability> requiredCapabilities;
    private String failureReason;
    private Colony colony;
    private Agent assigningAgent;

    public Mission(
            int neededtimesunits,
            Facilities facility,
            Map<Resources, Double> requiredResources,
            Set<Capability> requiredCapabilities) {

        validateRequiredResources(requiredResources);

        this.requiredResources = new HashMap<>(requiredResources);

        if (requiredCapabilities == null) {

            throw new IllegalArgumentException(
                    "Invalid required capabilities");
        }
        for (Capability capability : requiredCapabilities) {
        if (capability == null) {
            throw new IllegalArgumentException("Invalid required capabilities");
        }
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

    private void validateRequiredResources(Map<Resources,Double> requiredResources)
    {
        if(requiredResources == null)
            throw new IllegalArgumentException("Invalid Required Resources");
        for(Map.Entry<Resources,Double> entry : requiredResources.entrySet())
        {
            Resources resource = entry.getKey();
            Double amount = entry.getValue();
            if(resource==null || amount==null ||amount<0 || !Double.isFinite(amount))
                throw new IllegalArgumentException("Invalid Resources");    
        }    

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

    public int getElapsedTimeUnits() {
        return this.elapsedTimeUnits;
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

    public boolean hasValidTeamComposition(List<Agent> candidateAgents) {
        return candidateAgents != null && !candidateAgents.isEmpty();
    }

    public String getFailureReason(){
        return this.failureReason;
    }

    // Setters

    public void setRequiredResources(Map<Resources,Double> requiredResources)
    {
        validateRequiredResources(requiredResources);
        this.requiredResources = new HashMap<>(requiredResources);
    }

    public void setFacility(Facilities facility) {
        if(this instanceof Research){
            if(!(facility instanceof Laboratory)){
                throw new IllegalArgumentException("Research missions require a Laboratory facility");
            }
        }
        this.facility = facility;  }


    public void setNeededTimeUnits(int neededtimesunits) {
        if (neededtimesunits <= 0) {
            throw new IllegalArgumentException("Invalid time units");
        }

        this.neededtimesunits = neededtimesunits;
    }


    public void setRequiredCapabilities(
            Set<Capability> capabilities) {

        if (capabilities == null) {
            throw new IllegalArgumentException(
                    "Invalid required capabilities");
        }
        for (Capability capability : capabilities) {
            if (capability == null) {
                throw new IllegalArgumentException(
                        "Invalid required capabilities");
            }
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

    public void setColony(Colony colony){
        if(colony == null){
            throw new IllegalArgumentException("Invalid colony");
        }
        this.colony = colony;
    }

    public boolean isAssociatedWithAnotherColony(Colony candidate){
        return this.colony != null && this.colony != candidate;
    }

    public boolean isAssigningAgent(Agent agent){
        return this.assigningAgent == agent;
    }

    private void notifyColonyIfTerminal(){
        if (this.colony != null) {
            this.colony.clearActiveMissionIfTerminal(this);
        }
    }

    public void failMission(String reason){
        if(reason == null || reason.isBlank()){
            throw new IllegalArgumentException("Invalid failure reason");
        }
        if(this.state != Mission_Emergency_States.ASSIGNED && 
            this.state != Mission_Emergency_States.INPROGRESS ){
                throw new IllegalStateException("Mission cannot fail in its current state");
            }
        
        this.state = Mission_Emergency_States.FAILED;
        this.failureReason = reason;

        for (Agent agent : this.agents){
            agent.makeAvailable();
        }
        this.agents.clear();
        notifyColonyIfTerminal();
    }
public void rejectMission(String reason) {
    if (reason == null || reason.isBlank()) {
        throw new IllegalArgumentException("Invalid rejection reason");
    }


    if (this.state != null) {
        throw new IllegalStateException("Mission cannot be rejected in its current state");
    }

    this.state = Mission_Emergency_States.FAILED;
    this.failureReason = reason;

    for (Agent agent : this.agents) {
        agent.makeAvailable();
    }

    this.agents.clear();
    notifyColonyIfTerminal();
}


// Assign an agent

public void assign(Agent agent) {
    if (agent == null) {
        throw new IllegalArgumentException("Invalid agent");
    }

    if(this.colony == null || this.colony.getActiveMission() != this){
        throw new IllegalStateException("Mission is not associated with a colony");
    }
    
    if (this.state != null &&
        this.state != Mission_Emergency_States.ASSIGNED) {
        throw new IllegalStateException(
            "Cannot assign agent to mission in its current state"
        );
    }

    if (this.agents.contains(agent)) {
        throw new IllegalArgumentException("Agent already assigned");
    }

    if (!agent.isAvailable()) {
        throw new IllegalArgumentException("Agent is not available");
    }

    if (!agent.canPerform(this)) {
        throw new IllegalArgumentException("Agent cannot perform this mission");
    }

    if (this.assigningAgent != null) {
        throw new IllegalStateException("An agent assignment is already in progress");
    }

    this.assigningAgent = agent;
    try {
        agent.assignMission(this);
    } finally {
        this.assigningAgent = null;
    }

    this.agents.add(agent);
    this.state = Mission_Emergency_States.ASSIGNED;
}



    public void unassign(Agent agent){
        if(agent == null || !this.agents.contains(agent)){
            throw new IllegalArgumentException("Invalid agent");
        }
        if (this.state != Mission_Emergency_States.ASSIGNED) {
        throw new IllegalStateException(
            "Cannot unassign agent unless mission is ASSIGNED"
        );
    }
        this.agents.remove(agent);
        agent.makeAvailable();

        if(this.agents.isEmpty()&& this.state == Mission_Emergency_States.ASSIGNED){
            this.state = null;
        }
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
                    || !Double.isFinite(available)
                    || available < entry.getValue()) {
                return false;
            }
        }
        return true;
    }


    // After the end of the mission we must free the Agent

    protected void onSuccessfulCompletion() {
        // Mission subclasses may apply their successful outcome here.
    }

    public void finishMission(boolean successful){
        if(this.state != Mission_Emergency_States.INPROGRESS){
            throw new IllegalStateException("Mission is not in progress");
        }
        if(successful){
            onSuccessfulCompletion();
            this.state = Mission_Emergency_States.COMPLETED;
        }
        else{
            this.state = Mission_Emergency_States.FAILED;
            this.failureReason = "Mission Execution failed";
        }
        for(Agent agent : this.agents){
            agent.makeAvailable();
        }
        this.agents.clear();
        notifyColonyIfTerminal();
    }


    // Change the status of the mission
    public void startMission() {

    if(colony == null || colony.getActiveMission() != this){
        throw new IllegalStateException("Mission is not associated with a colony");
    }

    if (this.state != Mission_Emergency_States.ASSIGNED) {
        throw new IllegalStateException(
                "Mission is not assigned");
    }

    if (!this.colony.prepareMissionStart(this)) {
        return;
    }

    this.elapsedTimeUnits = 0;
    this.state = Mission_Emergency_States.INPROGRESS;
}

    public void advanceTimeUnit() {
        if (this.state != Mission_Emergency_States.INPROGRESS) {
            return;
        }

        this.elapsedTimeUnits++;
        if (this.elapsedTimeUnits >= this.neededtimesunits) {
            finishMission(true);
        }
    }

}
