package personnel;
import java.util.*;
import missions.Mission;
import missions.Repair;
import missions.Rescue;

public class Engineer extends Personnel
{   
    public Engineer(String name,int id,String healthStatus,String currentLocation,Set<Skills> skills,double fatigueLevel,boolean availability,Mission mission)
    {
        super(name,id,healthStatus,currentLocation,skills,fatigueLevel,availability,mission);
    }
    
    @Override 
    public void setMission(Mission mission)
    {
        if((mission==null || !(mission instanceof Repair || mission instanceof Rescue)) && super.isAvailability())
        {
            
        }
    }

    

}