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

public class Repair extends Mission
{
    private Object repairObject;

    public Repair(int needtimesunits,Facilities facility,Map<Resources,Double> requiredResources,Set<Capability> requiredCapabilities,Object repairObject)
    {
        super(needtimesunits,facility,requiredResources,requiredCapabilities);
        if(!(repairObject instanceof StringBuilder)
            && !(repairObject instanceof Facilities))
            {
            throw new IllegalArgumentException("Invalid Repair Object");
            }
        else if(repairObject instanceof Facilities && ((Facilities)repairObject).getOperationalStatus() != OperationalStatus.DAMAGED)
        {
            throw new IllegalArgumentException("Facility is not damaged");
        }     
        this.repairObject = repairObject;
    }
    public void setObject(Object repairObject)
    {
        if(!(repairObject instanceof StringBuilder)
            && !(repairObject instanceof Facilities))
            {
            throw new IllegalArgumentException("Invalid Repair Object");
            }
        else if(repairObject instanceof Facilities && ((Facilities)repairObject).getOperationalStatus() != OperationalStatus.DAMAGED)
        {
            throw new IllegalArgumentException("Facility is not damaged");
        }  
        this.repairObject = repairObject;
    }    
    public Object getObject(){return repairObject;}
    public void performRepair()
    {
        if(repairObject instanceof Facilities)
            ((Facilities) repairObject).setOperationalStatus(OperationalStatus.OPERATIONAL);
        else if(repairObject instanceof StringBuilder)
            System.out.println("Equipment is done");
    }

}
    