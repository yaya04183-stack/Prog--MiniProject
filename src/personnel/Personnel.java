package personnel;
import agents.Agent;
import emergencies.EmergencyType;
import java.util.*;
import missions.Mission;

public abstract class Personnel 
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
            throw new IllegalArgumentException("Current Locations Connot be null or emppty");
        else this.currentLocation = currentLocation;
        // skills
        if(skills==null || skills.isEmpty())
            throw new IllegalArgumentException("skills Cannot be null or Empty");    
        else this.skills = new HashSet<>(skills);
        //fatigueLevel
        if(fatigueLevel<0 || fatigueLevel>100)
            throw new IllegalArgumentException("Fatigue Level Cannot be negative of greater than 100");
        else this.fatigueLevel = fatigueLevel;
        this.mission = mission;
        this.availability = availability;
        
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

    public double getFatigueLevel(){
        return fatigueLevel;
    }
    
    public boolean isAvailability(){
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
        if(mission==null)
            throw new IllegalArgumentException("Mission Cannot be Null");
        else this.mission = mission;
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
        if(fatigueLevel<0 || fatigueLevel>100)
            throw new IllegalArgumentException("Fatigue Level Cannot be negative of greater than 100");
        else this.fatigueLevel = fatigueLevel;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public void setSkills(Set<Skills> skills)
    {
        if(skills==null || skills.isEmpty())
            throw new IllegalArgumentException("Skills Cannot be Null or IsEmpty");
        else this.skills = new HashSet<>(skills);
    }
}