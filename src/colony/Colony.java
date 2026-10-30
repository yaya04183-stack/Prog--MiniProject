package colony;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import enums.Resources;
import enums.OperationalStatus;
import enums.Mission_Emergency_States;

import facilities.Facilities;
import missions.Mission;
import personnel.Personnel;
import roboticunit.RoboticUnit;
import agents.Agent;
import emergencies.Emergency;


import java.util.HashSet;
import java.util.Set;

public class Colony {

    private List<Personnel> personnel;
    private List<Facilities> facilities;
    private List<RoboticUnit> roboticUnits;
    private List<Mission> missions;
    private List<Emergency> emergencies;
    private Mission activeMission;

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
            this.facilities.add(facility);
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
                mission.unassign(agent);
            }

            this.activeMission = null;
            this.missions.remove(mission);

            mission.rejectMission("Mission assignment failed: " + e.getMessage());
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
        this.activeMission.startMission();
        return true;
    }

    public void advanceTimeUnit() {
        if (this.activeMission != null) {
            this.activeMission.advanceTimeUnit();
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
                || state == Mission_Emergency_States.FAILED) {
            this.activeMission = null;
        }
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
    List<Agent> responders = getAvailableResponders(emergency);
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
