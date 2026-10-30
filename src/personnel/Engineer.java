package personnel;

import java.util.*;
import enums.Skills;
import missions.Mission;
import missions.Repair;
import missions.Rescue;

public class Engineer extends Personnel
{   
    public Engineer(String name,int id,String healthStatus,String currentLocation,Set<Skills> skills,double fatigueLevel,boolean availability)
    {
        super(name,id,healthStatus,currentLocation,skills,fatigueLevel,availability,null);
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
