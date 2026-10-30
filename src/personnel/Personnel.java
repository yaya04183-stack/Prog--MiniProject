package personnel;

import enums.EmergencyType;
import enums.Skills;
import missions.Mission;
import agents.Agent;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;



public abstract class Personnel implements Agent 
{
    private String name;
    private int id;
    private String healthStatus;
    private String currentLocation;
    private Set<Skills> skills;
    private double fatigueLevel;
    private boolean availability;
    private Mission mission;
    

    public Personnel(String name,int id,String healthStatus,String currentLocation,Set<Skills> skills,double fatigueLevel,boolean availability,Mission mission)
    {   
        // name
        if(name==null || name.isBlank())
            throw new IllegalArgumentException("Name Cannot be null or empty");
        else this.name = name;
        // id
        if(id <= 0)
            throw new IllegalArgumentException("Id Connot be negative or zero");
        else this.id = id;
        // healthStatus
        if(healthStatus==null || healthStatus.isBlank())
            throw new IllegalArgumentException("Health Status Connot be null or empty");
        else this.healthStatus = healthStatus;
        // currentLocations
        if(currentLocation==null || currentLocation.isBlank())
            throw new IllegalArgumentException("Current Locations Connot be null or empty");
        else this.currentLocation = currentLocation;
        // skills
        if(skills==null || skills.isEmpty())
            throw new IllegalArgumentException("skills Cannot be null or Empty");
        for (Skills skill : skills) {
            if (skill == null)
                throw new IllegalArgumentException("Skills Cannot Contain Null");
        }
        this.skills = new HashSet<>(skills);
        //fatigueLevel
        if(fatigueLevel<0 || fatigueLevel>100 || !Double.isFinite(fatigueLevel))
            throw new IllegalArgumentException("Fatigue Level Cannot be negative of greater than 100");
        else this.fatigueLevel = fatigueLevel;
        this.mission = mission;
        this.availability = availability;
        if(mission != null && availability)
            throw new IllegalArgumentException("Person Cannot be available while assigned to a mission");
        }


    public String getName(){
        return name;
    }
    public int getId(){
        return id;
    }
    public String getCurrentLocation(){
        return  currentLocation ;
    }
    public Set<Skills> getSkills(){
        return new HashSet<>(skills);
    }

    @Override
    public boolean hasSkill(Skills skill) {
        return skill != null && this.skills.contains(skill);
    }

    public double getFatigueLevel(){
        return fatigueLevel;
    }
    
    public boolean getAvailability(){
        return availability;
    }

    public String getHealthStatus() {
    return healthStatus;
    }

    public Mission getMission(){
        return this.mission;
    }


    

    public void setMission(Mission mission)
    {
        if (mission == null) {
            throw new IllegalArgumentException("Invalid mission");
        }
        mission.assign(this);
    }
    @Override 
    public void assignMission(Mission mission)
    {
        if (mission == null) {
            throw new IllegalArgumentException("Invalid mission");
        }
        if (!mission.isAssigningAgent(this)) {
            mission.assign(this);
            return;
        }
        if (!canPerform(mission)) {
            throw new IllegalArgumentException("Person Cannot Perform That Mission");
        }
        this.mission = mission;
        this.availability = false;
    }

    public void setHealthStatus(String healthStatus){
    if(healthStatus==null || healthStatus.isBlank())
        throw new IllegalArgumentException("Health Status Connot be null or empty");
    else this.healthStatus = healthStatus;
    }

    public void setCurrentLocation(String currentLocation){
        if(currentLocation==null || currentLocation.isBlank())
            throw new IllegalArgumentException("Current Locations Connot be null or emppty");
        else this.currentLocation = currentLocation;
    }
    
    public void setFatigueLevel(double fatigueLevel){
        if(fatigueLevel<0 || fatigueLevel>100 || !Double.isFinite(fatigueLevel))
            throw new IllegalArgumentException("Fatigue Level Cannot be negative of greater than 100");
        else this.fatigueLevel = fatigueLevel;
    }

    public void setAvailability(boolean availability) 
    {   
        if(mission != null && availability)
            throw new IllegalArgumentException("Person is still assigned to a mission");
        this.availability = availability;
    }

    public void setSkills(Set<Skills> skills)
    {
        if(skills==null || skills.isEmpty())
            throw new IllegalArgumentException("Skills Cannot be Null or IsEmpty");
        for (Skills skill : skills) {
            if (skill == null)
                throw new IllegalArgumentException("Skills Cannot Contain Null");
        }
        this.skills = new HashSet<>(skills);
    }


    // Agent methods

    @Override
    public void makeAvailable() 
    {   
        this.mission = null;
        setAvailability(true);
    }

    @Override
    public boolean isAvailable() {
        return getAvailability();
    }

    @Override
    public boolean canPerform(Mission mission) {
        if(mission != null && getAvailability())
            return true;
        else return false;
    }

    @Override
    public boolean canHandleEmergency(EmergencyType emergencyType) {
        if(!getAvailability())
            return false;
        if (emergencyType == null) 
        {
            return false;
        }
        switch (emergencyType) {
            case OXYGEN_LEAK:
                return skills.contains(Skills.MECHANICAL);
            case FIRE:
                return skills.contains(Skills.FIRE_RESPONSE);
            case ROVER_BREAKDOWN:
                return skills.contains(Skills.MECHANICAL);
            case MEDICAL:
                return skills.contains(Skills.MEDICAL);            
            default:
                return false;
        }
    }
}
