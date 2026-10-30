package personnel;

import java.util.*;
import enums.Skills;
import missions.Mission;
import missions.Repair;
import missions.Rescue;

public class Engineer extends Personnel
{   
    public Engineer(String name,int id,String healthStatus,String currentLocation,Set<Skills> skills,double fatigueLevel,boolean availability,Mission mission)
    {
        super(name,id,healthStatus,currentLocation,skills,fatigueLevel,availability,null);
        if(mission != null)
            if(!canPerform(mission))
            {
                throw new IllegalArgumentException("Engineer Cannot Make that Mission");
            }
            else super.setMission(mission);
    }
    
    
    @Override 
    public boolean canPerform(Mission mission)
    {   
        if(!super.canPerform(mission))
            return false;
        
        return (((mission instanceof Repair 
                && super.getSkills().contains(Skills.MECHANICAL)))
                || (mission instanceof Rescue 
                && super.getSkills().contains(Skills.COMMUNICATION)));
    }
}
