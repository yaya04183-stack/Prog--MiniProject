package personnel;

import java.util.Set;

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
                throw new IllegalArgumentException("Scintist Cannot Make That Mission");
            }
            else super.setMission(mission);
        
    }
    @Override 
    public void setMission(Mission mission)
    {
        if(canPerform(mission))
            super.setMission(mission);
        else throw new IllegalArgumentException("Scintist Cannot Make That Mission");
    }
    public void makeAvailable()
    {
        setAvailability(true);
    }
    public boolean canPerform(Mission mission)
    {
        if((mission!=null)
        && ((mission instanceof Research && super.getSkills().contains(Skills.SCIENTIFIC)) 
        ||  (mission instanceof Exploration && super.getSkills().contains(Skills.SCIENTIFIC))
        ||   (mission instanceof Rescue && super.getSkills().contains(Skills.COMMUNICATION)))
        && super.isAvailability())
        return true;
        else return false;
    }

}
