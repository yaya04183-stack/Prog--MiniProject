package personnel;
import enums.Skills;
import java.util.*;
import missions.Mission;
import missions.Rescue;
import emergencies.MedicalEmergency;
import enums.EmergencyType;

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
    public boolean canPerform(Mission  mission)
    {   
        if(!super.canPerform(mission))
            return false;
        
            return ((mission instanceof Rescue && super.getSkills().contains(Skills.COMMUNICATION)));
    
    }
    public boolean canPerform(MedicalEmergency emergency)
    {   
        return emergency != null
        && super.canHandleEmergency(EmergencyType.MEDICAL);
        }
}
