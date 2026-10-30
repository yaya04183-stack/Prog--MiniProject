package colony;
import java.util.ArrayList;
import java.util.List;

import facilities.Facilities;
import missions.Mission;
import personnel.Personnel;
import roboticunit.RoboticUnit;
import agents.Agent;
import enums.Mission_Emergency_States;

import java.util.HashSet;
import java.util.Set;

public class Colony {

    private List<Personnel> personnel;
    private List<Facilities> facilities;
    private List<RoboticUnit> roboticUnits;
    private List<Mission> missions;
    private Mission activeMission;

    public Colony() {
        personnel = new ArrayList<>();
        facilities = new ArrayList<>();
        roboticUnits = new ArrayList<>();
        missions = new ArrayList<>();
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
        if (mission.getState() != null){
            mission.rejectMission(("Mission has already been initialized"));
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

        for(Agent agent : agents){
            mission.assign(agent);
        }

        this.missions.add(mission);

        this.activeMission=mission;

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
        this.activeMission.startMission();;
        return true;
    }



    public List<Personnel> getPersonnel(){return this.personnel;}
    public List<Facilities> getFacilities(){return this.facilities;}
    public List<RoboticUnit> getRoboticUnits(){return this.roboticUnits;}
    public List<Mission> getMissions(){return this.missions;}
    public Mission getActiveMission(){return this.activeMission;}
}
