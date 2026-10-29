package personnel;
import enums.Skills;
import java.util.*;
import missions.Mission;
import missions.Rescue;
import emergencies.medicalEmergency;

public class MedicalOfficer extends Personnel
{
    public MedicalOfficer(String name,int id,String healthStatus,String currentLocation,Set<Skills> skills,double fatigLevel,boolean availability,Mission mission)
    {
        super(name,id,healthStatus,currentLocation,skills,fatigLevel,availability,null);
        if(mission!=null)
            if(canPerform(mission))
            {
                super.setMission(mission);
            }
            else throw new IllegalArgumentException("Medical Officier Cannot Make that Mission");
        
    }
    @Override 
    public void setMission(Mission mission)
    {
        if(canPerform(mission))
            super.setMission(mission);
        else throw new IllegalArgumentException("Medical Officier Cannot Make that Mission");

    }
    public void makeAvailable()
    {
        super.setAvailability(true);
    }
    public boolean canPerform(Mission  mission)
    {
        if((mission!=null)
            &&((mission instanceof Rescue && super.getSkills().contains(Skills.COMMUNICATION)))
            &&super.isAvailability())
            return true;
        else return false;
    }
    public boolean canPerform(medicalEmergency emergency)
    {
        if((emergency!=null)
        &&(super.isAvailability())
        &&(super.getSkills().contains(Skills.MEDICAL)))
            return true;
        else return false;
    }
}
