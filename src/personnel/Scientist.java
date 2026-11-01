package personnel;

import enums.Skills;
import java.util.*;
import missions.Exploration;
import missions.Mission;
import missions.Rescue;
import missions.Research;

public class Scientist extends Personnel
{  
    // Initializes a Scientist instance.
    // Initializes a Scientist instance.
    public Scientist(String name,int id,String healthStatus,String currentLocation,Set<Skills> skills,double fatigueLevel,boolean availability)
    {
        super(name,id,healthStatus,currentLocation,skills,fatigueLevel,availability,null);
    }
    
    // Checks whether it can perform.
    @Override 
    public boolean canPerform(Mission mission)
    {   
        if(!super.canPerform(mission))
            return false;
        return
        ((mission instanceof Research && super.getSkills().contains(Skills.SCIENTIFIC)) 
        ||(mission instanceof Exploration && super.getSkills().contains(Skills.SCIENTIFIC))
        ||(mission instanceof Rescue && super.getSkills().contains(Skills.COMMUNICATION)));
        
    }

}
