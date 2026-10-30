package colony;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.IdentityHashMap;

import enums.Resources;
import enums.OperationalStatus;
import enums.Mission_Emergency_States;

import facilities.Facilities;
import missions.Mission;
import personnel.Personnel;
import roboticunit.RoboticUnit;
import agents.Agent;
import emergencies.Emergency;
import marsenvironment.MarsEnvironment;


import java.util.HashSet;
import java.util.Set;

public class Colony {

    private List<Personnel> personnel;
    private List<Facilities> facilities;
    private List<RoboticUnit> roboticUnits;
    private List<Mission> missions;
    private List<Emergency> emergencies;
    private Mission activeMission;
    private MarsEnvironment marsEnvironment;
    private final Set<Mission> resourcesPreparedMissions =
            Collections.newSetFromMap(new IdentityHashMap<>());

    public Colony() 
    {
        personnel = new ArrayList<>();
        facilities = new ArrayList<>();
        roboticUnits = new ArrayList<>();
        missions = new ArrayList<>();
        emergencies = new ArrayList<>();
        
        activeMission = null;
    }

    public void addPersonnel(Personnel person)
    {
        if(person==null){
            throw new IllegalArgumentException("The Person caoont be null or empty");
        }
        else {this.personnel.add(person);}
    }

    public void addFacility(Facilities facility)
    {
        if(facility==null)
        {
            throw new IllegalArgumentException("The Facility cannot be null or empty");
        }
        else 
        {
            if (!this.facilities.contains(facility)) {
                this.facilities.add(facility);
            }
        }
    }

    public void addRobot(RoboticUnit robot){
        if(robot == null) {
            throw new IllegalArgumentException("Robot cannot be null or empty");
        }
        else{
            this.roboticUnits.add(robot);
        }
            
    }


    public boolean submitMission(Mission mission,List<Agent> agents){
        if(mission == null){
            return false;
        }
        if(this.missions.contains(mission)){
            return false;
        }
        if(mission.isAssociatedWithAnotherColony(this)){
            return false;
        }
        if (mission.getState() != null){
            this.missions.add(mission);
            return false;
        }
        if(agents == null || agents.isEmpty()){
            mission.rejectMission(("No agents provided"));
            this.missions.add(mission);
            return false;
        }
        if(activeMission != null){
            mission.rejectMission("Another mission is already active");
            this.missions.add(mission);
            return  false;
        }
        Set<Agent> uniqueAgents = new HashSet<>();
        
        for(Agent agent : agents){
            if(agent == null){
                mission.rejectMission(("Invalid agent : null"));
                this.missions.add(mission);
                return false;
            }
            if(!uniqueAgents.add(agent)){
                mission.rejectMission("Duplicate agent assigned");
                this.missions.add(mission);
                return false;
            }
            if(!agent.isAvailable()){
                mission.rejectMission(("Agent is unavailable"));
                this.missions.add(mission);
                return false;
            }
            if(!agent.canPerform(mission)){
                mission.rejectMission(("Agent lacks required capabilities"));
                this.missions.add(mission);
                return false;
            }
            if(mission.getAgents().contains((agent))){
                mission.rejectMission("Agent is already assigned to this mission");
                this.missions.add(mission);
                return false;
            }
        }

        if (!mission.hasValidTeamComposition(agents)) {
            mission.rejectMission("Mission team does not meet its requirements");
            this.missions.add(mission);
            return false;
        }

        if (this.marsEnvironment != null) {
            String restriction = this.marsEnvironment.getMissionRestrictionReason(mission, agents);
            if (restriction != null) {
                mission.rejectMission(restriction);
                this.missions.add(mission);
                return false;
            }
        }

        if (!mission.checkresources(getColonyResourceTotal())) {
            mission.rejectMission("Insufficient resources for mission");
            this.missions.add(mission);
            return false;
        }

        mission.setColony(this);

        List<Agent> assignedAgents = new ArrayList<>();
        this.missions.add(mission);
        this.activeMission=mission;

        try{
            for(Agent agent : agents){
                mission.assign(agent);
                assignedAgents.add((agent));
            }
        }catch(RuntimeException e){
            for (Agent agent : assignedAgents){
                if (mission.getAgents().contains(agent)) {
                    mission.unassign(agent);
                }
            }

            this.activeMission = null;
            this.missions.remove(mission);

            if (mission.getState() == null) {
                mission.rejectMission("Mission assignment failed: " + e.getMessage());
            }
            this.missions.add(mission);

            return false;
        }
        return true;
    } 
    
    public boolean finishActiveMission(boolean successful){
        if(this.activeMission == null){
            return false;
        }
        this.activeMission.finishMission(successful);

        this.activeMission=null;

        return true;
    }


    public boolean startActiveMission(){
        if(this.activeMission == null){
            return false;
        }
        Mission mission = this.activeMission;
        mission.startMission();
        return mission.getState() == Mission_Emergency_States.INPROGRESS;
    }

    public void advanceTimeUnit() {
        if (this.activeMission == null) {
            return;
        }
        if (this.marsEnvironment != null) {
            String failureReason = this.marsEnvironment
                    .getActiveMissionFailureReason(this.activeMission);
            if (failureReason != null) {
                failActiveMission(failureReason);
                return;
            }
        }
        this.activeMission.advanceTimeUnit();
    }

    /** Prepares mission resources atomically, once per mission. */
    public boolean prepareMissionStart(Mission mission) {
        if (mission == null || this.activeMission != mission
                || mission.getState() != Mission_Emergency_States.ASSIGNED) {
            return false;
        }
        if (this.resourcesPreparedMissions.contains(mission)) {
            return true;
        }

        Map<Resources, Double> remaining = mission.getRequiredResources();
        Map<Facilities, Map<Resources, Double>> deductionPlan = new LinkedHashMap<>();
        Set<Facilities> visitedFacilities = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Facilities facility : this.facilities) {
            if (facility == null || !visitedFacilities.add(facility)
                    || facility.getOperationalStatus() != OperationalStatus.OPERATIONAL) {
                continue;
            }
            for (Resources resource : new ArrayList<>(remaining.keySet())) {
                double needed = remaining.get(resource);
                if (needed <= 0) {
                    continue;
                }
                double allocated = Math.min(needed, facility.getResourceAmount(resource));
                if (allocated > 0) {
                    deductionPlan.computeIfAbsent(facility, key -> new LinkedHashMap<>())
                            .put(resource, allocated);
                    remaining.put(resource, needed - allocated);
                }
            }
        }

        if (remaining.values().stream().anyMatch(amount -> amount > 0)) {
            return false;
        }

        Map<Facilities, Map<Resources, Double>> originalResources = new LinkedHashMap<>();
        for (Facilities facility : deductionPlan.keySet()) {
            originalResources.put(facility, facility.getResources());
        }
        try {
            for (Map.Entry<Facilities, Map<Resources, Double>> facilityEntry
                    : deductionPlan.entrySet()) {
                for (Map.Entry<Resources, Double> resourceEntry
                        : facilityEntry.getValue().entrySet()) {
                    double deducted = facilityEntry.getKey().deductResource(
                            resourceEntry.getKey(), resourceEntry.getValue());
                    if (deducted != resourceEntry.getValue()) {
                        restoreResources(originalResources);
                        return false;
                    }
                }
            }
        } catch (RuntimeException deductionFailure) {
            restoreResources(originalResources);
            return false;
        }

        this.resourcesPreparedMissions.add(mission);
        return true;
    }

    private void restoreResources(Map<Facilities, Map<Resources, Double>> originalResources) {
        for (Map.Entry<Facilities, Map<Resources, Double>> entry : originalResources.entrySet()) {
            entry.getKey().setResources(entry.getValue());
        }
    }

    public Map<Resources, Double> getColonyResourceTotal()
    {
        Map<Resources,Double> totals = new HashMap<>();
        
        for(Facilities facility : facilities)
        {
            if(facility == null || facility.getOperationalStatus() != OperationalStatus.OPERATIONAL)
                continue;
            for(Map.Entry<Resources,Double> entry : facility.getResources().entrySet())
            {
                totals.merge(entry.getKey(),entry.getValue(),Double::sum);
            }    
        }
        return totals;
    }

    public boolean failActiveMission(String reason) {
    if (this.activeMission == null) {
        return false;
    }

    this.activeMission.failMission(reason);
    this.activeMission = null;

    return true;
    }

    public void clearActiveMissionIfTerminal(Mission mission) {
        if (mission == null || this.activeMission != mission) {
            return;
        }

        Mission_Emergency_States state = mission.getState();
        if (state == Mission_Emergency_States.COMPLETED
                || state == Mission_Emergency_States.FAILED
                || (state == null && mission.getAgents().isEmpty())) {
            this.activeMission = null;
        }
    }

    public void setMarsEnvironment(MarsEnvironment marsEnvironment) {
        if (marsEnvironment == null) {
            throw new IllegalArgumentException("Mars environment cannot be null");
        }
        this.marsEnvironment = marsEnvironment;
    }

private List<Agent> getAvailableResponders(Emergency emergency)
{     
    List<Agent> responders = new ArrayList<>();
    if(emergency==null)
        throw new IllegalArgumentException("Emergency Cannot be NULL");
    for(Personnel person : this.personnel)
    {
        if(person.isAvailable() && person.canHandleEmergency(emergency.getType()))
            responders.add(person);
    }
    for(RoboticUnit robot : this.roboticUnits)
    {
        if(robot.isAvailable() && robot.canHandleEmergency(emergency.getType()))
            responders.add(robot);
    }
    return responders;
}

public boolean handleEmergency(Emergency emergency)
{
    if(emergency==null)
        throw new IllegalArgumentException("Emergency Cannot be NULL");
    if(this.emergencies.contains(emergency))
        return false;
    emergencies.add(emergency);

    emergency.registerOccurrence();

    List<Agent> responders = getAvailableResponders(emergency);
    if(!emergency.hasRequiredResponders(responders))
    {
        emergency.markUnresolved("Required Responders Is Not Available");
        return false;
    }

    if (emergency.requiresOperationalFacility() && !hasOperationalFacilityAt(emergency)) {
        emergency.markUnresolved("No operational facility is available at the emergency location");
        return false;
    }

    if (!consumeEmergencyResources(emergency.getRequiredResources())) {
        emergency.markUnresolved("Insufficient resources for emergency response");
        return false;
    }

    Object location = emergency.getLocation();
    String responseLocation = location instanceof Facilities facility
            ? facility.getLocation() : (String) location;
    List<Agent> dispatched = new ArrayList<>();
    try {
        for (Agent responder : responders) {
            responder.setAvailability(false);
            dispatched.add(responder);
            responder.setCurrentLocation(responseLocation);
        }
        emergency.setAgents(dispatched);
        if (!emergency.applyResponse()) {
            emergency.markUnresolved("Emergency response could not be completed");
            return false;
        }
        emergency.markResolved();
        return true;
    } catch (RuntimeException responseFailure) {
        emergency.markUnresolved("Emergency response failed: " + responseFailure.getMessage());
        return false;
    } finally {
        for (Agent responder : dispatched) {
            responder.makeAvailable();
        }
    }
}

private boolean hasOperationalFacilityAt(Emergency emergency) {
    Object location = emergency.getLocation();
    if (location instanceof Facilities target) {
        return this.facilities.contains(target)
                && target.getOperationalStatus() == OperationalStatus.OPERATIONAL;
    }
    String targetLocation = (String) location;
    return this.facilities.stream().anyMatch(facility -> facility != null
            && facility.getOperationalStatus() == OperationalStatus.OPERATIONAL
            && facility.getLocation().equals(targetLocation));
}

private boolean consumeEmergencyResources(Map<Resources, Double> requiredResources) {
    Map<Resources, Double> remaining = new LinkedHashMap<>(requiredResources);
    Map<Facilities, Map<Resources, Double>> deductionPlan = new LinkedHashMap<>();
    Set<Facilities> visitedFacilities = Collections.newSetFromMap(new IdentityHashMap<>());

    for (Facilities facility : this.facilities) {
        if (facility == null || !visitedFacilities.add(facility)
                || facility.getOperationalStatus() != OperationalStatus.OPERATIONAL) {
            continue;
        }
        for (Resources resource : new ArrayList<>(remaining.keySet())) {
            double needed = remaining.get(resource);
            if (needed <= 0) {
                continue;
            }
            double allocated = Math.min(needed, facility.getResourceAmount(resource));
            if (allocated > 0) {
                deductionPlan.computeIfAbsent(facility, key -> new LinkedHashMap<>())
                        .put(resource, allocated);
                remaining.put(resource, needed - allocated);
            }
        }
    }

    if (remaining.values().stream().anyMatch(amount -> amount > 0)) {
        return false;
    }

    Map<Facilities, Map<Resources, Double>> originalResources = new LinkedHashMap<>();
    for (Facilities facility : deductionPlan.keySet()) {
        originalResources.put(facility, facility.getResources());
    }
    try {
        for (Map.Entry<Facilities, Map<Resources, Double>> facilityEntry
                : deductionPlan.entrySet()) {
            for (Map.Entry<Resources, Double> resourceEntry
                    : facilityEntry.getValue().entrySet()) {
                double deducted = facilityEntry.getKey().deductResource(
                        resourceEntry.getKey(), resourceEntry.getValue());
                if (deducted != resourceEntry.getValue()) {
                    restoreResources(originalResources);
                    return false;
                }
            }
        }
    } catch (RuntimeException deductionFailure) {
        restoreResources(originalResources);
        return false;
    }
    return true;
}

public List<Emergency> getEmergencies(){
    return  new ArrayList<>(this.emergencies);
}

public List<Personnel> getPersonnel() {
    return new ArrayList<>(this.personnel);
}
public List<Facilities> getFacilities() {
    return new ArrayList<>(this.facilities);
}
public List<RoboticUnit> getRoboticUnits() {
    return new ArrayList<>(this.roboticUnits);
}
public List<Mission> getMissions() {
    return new ArrayList<>(this.missions);
}
public Mission getActiveMission() {
    return this.activeMission;
}
}
