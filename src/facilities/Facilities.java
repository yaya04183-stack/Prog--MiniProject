package facilities;

import agents.Agent;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import resources.Resources;

public  abstract class Facilities
{
    private final FacilityType type;
    private String  location;
    private double capacity;
    //private String operationalStatus;
    private OperationalStatus operationalStatus;
    private Map<Resources,Double> resources;
    private StringBuilder equipment;
    private List<Agent> occupants;


    public Facilities(FacilityType type,String location,double capacity,OperationalStatus  operationalStatus,Map<Resources,Double> resources,StringBuilder equipment,  List<Agent> occupants)
    {   
        if (type == null)
            throw new IllegalArgumentException("Invalid Facility Type");
        this.type = type;
        // location
        if(location==null || location.isBlank())
            throw new IllegalArgumentException("Invalid Location");
        else this.location = location;
        // capacity
        if(capacity < 0)
            throw new IllegalArgumentException("Invalid Capacity");
        else this.capacity = capacity;
        // operationalStatus
        if (operationalStatus == null)
        throw new IllegalArgumentException("Invalid Operational Status");
        this.operationalStatus = operationalStatus;
        // resources
        if(resources==null)
            throw new IllegalArgumentException("Invalid resources");
        else this.resources = resources;
        // equipment
        if(equipment==null || equipment.isEmpty())
            throw new IllegalArgumentException("Invalid Equipment");
        else this.equipment = equipment;
        if (occupants == null)
            {throw new IllegalArgumentException("Invalid Occupants");}
        else this.occupants = new ArrayList<>(occupants);
    }


    public String getLocation(){return this.location;}
    public double getCapacity(){return this.capacity;}
    public OperationalStatus getOperationalStatus(){return this.operationalStatus;}
    public Map<Resources,Double> getResources(){return this.resources;}
    public StringBuilder getEquipment(){return this.equipment;}
    public List<Agent> getOccupants() {return new ArrayList<>(this.occupants);}


    public void  setOperationalStatus(OperationalStatus operationalStatus)
    {
        if (operationalStatus == null)
            throw new IllegalArgumentException("Invalid Operational Status");
        else this.operationalStatus = operationalStatus;
    }


    public void setEquipment(StringBuilder equipment)
    {
        if(equipment==null || equipment.isEmpty())
            throw new IllegalArgumentException("Invalid Equipment");
        else this.equipment = equipment;
    }


    public void setResources(Map<Resources,Double> resources)
    {
        if(resources==null)
            throw new IllegalArgumentException("Invalid Resources");
        else this.resources = resources;
    }



    public void setOccupants(List<Agent> occupants)
    {    
        if (occupants == null)
            throw new IllegalArgumentException("Invalid Occupants");
        else
            this.occupants = new ArrayList<>(occupants);
}
//##########################

// in Facilities: reading how much of a resource this facility holds
public double getResourceAmount(Resources r)
{
    return resources.getOrDefault(r, 0.0);
}
protected double deductResource(Resources r, double amount)
    {
        if (r == null || amount < 0)
            throw new IllegalArgumentException("Invalid deduction");
        double actual = Math.min(getResourceAmount(r), amount);
        resources.put(r, getResourceAmount(r) - actual);
        return actual;
    }
protected void addResource(Resources r, double amount)
    {
        if (r == null || amount < 0)
            throw new IllegalArgumentException("Invalid amount");
        resources.put(r, getResourceAmount(r) + amount);
    }

}