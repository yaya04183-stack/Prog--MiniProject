package personnel;

import enums.Skills;
import java.util.*;
import missions.Exploration;
import missions.Mission;
import missions.Rescue;
import missions.Research;

public class Scientist extends Personnel
{  
    public Scientist(String name,int id,String healthStatus,String currentLocation,Set<Skills> skills,double fatigueLevel,boolean availability,Mission mission)
    {
        super(name,id,healthStatus,currentLocation,skills,fatigueLevel,availability,null);
        if(mission!=null)
            if(!canPerform(mission))
            {
                throw new IllegalArgumentException("Scientist Cannot Make That Mission");
            }
            else super.setMission(mission);
        
    }
    
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
