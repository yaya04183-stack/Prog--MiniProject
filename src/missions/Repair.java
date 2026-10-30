package missions;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


import enums.Mission_Emergency_States;
import enums.Resources;
import enums.Capability;
import agents.Agent;
import facilities.Facilities;
import enums.OperationalStatus;
import facilities.Equipment;
import enums.Skills;

public class Repair extends Mission
{
    private Object repairObject;

    public Repair(int needtimesunits,Facilities facility,Map<Resources,Double> requiredResources,Set<Capability> requiredCapabilities,Object repairObject)
    {
        super(needtimesunits,facility,requiredResources,requiredCapabilities);
        if(!(repairObject instanceof Equipment)
            && !(repairObject instanceof Facilities))
            {
            throw new IllegalArgumentException("Invalid Repair Object");
            }
        else if(repairObject instanceof Facilities && ((Facilities)repairObject).getOperationalStatus() != OperationalStatus.DAMAGED)
        {
            throw new IllegalArgumentException("Facility is not damaged");
        }   
        if (repairObject instanceof Equipment
        && ((Equipment) repairObject).getOperationalStatus()
                != OperationalStatus.DAMAGED) {
            throw new IllegalArgumentException("Equipment is not damaged");
        }
        this.repairObject = repairObject;
        }
    public void setObject(Object repairObject)
        {
        if(!(repairObject instanceof Equipment) && !(repairObject instanceof Facilities))
        {
            throw new IllegalArgumentException("Invalid Repair Object");
        }
        else if(repairObject instanceof Facilities && ((Facilities)repairObject).getOperationalStatus() == OperationalStatus.DESTROYED)
        {
            throw new IllegalArgumentException("Facility is destroyed");
        }  
        else if(repairObject instanceof Facilities && ((Facilities)repairObject).getOperationalStatus() == OperationalStatus.UNAVAILABLE)
        {
            throw new IllegalArgumentException("Facility is un available");
        }
        else if(repairObject instanceof Facilities && ((Facilities)repairObject).getOperationalStatus() == OperationalStatus.OPERATIONAL)
        {
            throw new IllegalArgumentException("Facility is already under operational");   
        } 
        
        

        else if(repairObject instanceof Equipment && ((Equipment)repairObject).getOperationalStatus() == OperationalStatus.DESTROYED)
        {
            throw new IllegalArgumentException("Facility is destroyed");
        }  
        else if(repairObject instanceof Equipment && ((Equipment)repairObject).getOperationalStatus() == OperationalStatus.UNAVAILABLE)
        {
            throw new IllegalArgumentException("Facility is un available");
        }
        else if(repairObject instanceof Equipment && ((Equipment)repairObject).getOperationalStatus() == OperationalStatus.OPERATIONAL)
        {
            throw new IllegalArgumentException("Facility is already under operational");
        } 
        
        this.repairObject = repairObject;
    }    
    public Object getObject(){return repairObject;}

    public void performRepair()
    {
        if(repairObject instanceof Facilities)
            ((Facilities) repairObject).setOperationalStatus(OperationalStatus.OPERATIONAL);
        else if(repairObject instanceof Equipment)
            ((Equipment) repairObject).setOperationalStatus(OperationalStatus.OPERATIONAL);
    }

    @Override
    protected void onSuccessfulCompletion() {
        performRepair();
    }

    @Override
    public boolean hasValidTeamComposition(List<Agent> candidateAgents) {
        return candidateAgents != null
                && candidateAgents.stream().anyMatch(agent -> agent != null
                        && agent.hasSkill(Skills.MECHANICAL))
                && candidateAgents.stream().anyMatch(agent -> agent != null
                        && agent.hasCapability(Capability.EQUIPMENT_REPAIR));
    }

}
    
