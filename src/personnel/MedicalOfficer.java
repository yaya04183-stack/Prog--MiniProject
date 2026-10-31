package personnel;
import enums.Skills;
import java.util.*;
import missions.Mission;
import missions.Rescue;
import emergencies.medicalEmergency;
import enums.EmergencyType;

public class MedicalOfficer extends Personnel
{
    public MedicalOfficer(String name,int id,String healthStatus,String currentLocation,Set<Skills> skills,double fatigLevel,boolean availability)
    {
        super(name,id,healthStatus,currentLocation,skills,fatigLevel,availability,null);
    }
    @Override 
    public boolean canPerform(Mission  mission)
    {   
        if(!super.canPerform(mission))
            return false;
        
            return ((mission instanceof Rescue && super.getSkills().contains(Skills.COMMUNICATION)));
    
    }
    public boolean canPerform(medicalEmergency emergency)
    {   
        return emergency != null
        && super.canHandleEmergency(EmergencyType.MEDICAL);
        }
}
