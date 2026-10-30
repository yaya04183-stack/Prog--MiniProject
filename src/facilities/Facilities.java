package facilities;

import agents.Agent;
import enums.FacilityType;
import enums.OperationalStatus;
import enums.Resources;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public  abstract class Facilities
{
    private final FacilityType type;
    private String  location;
    private double capacity;
    //private String operationalStatus;
    private OperationalStatus operationalStatus;
    private Map<Resources,Double> resources;
    private List<Equipment> equipment;
    private List<Agent> occupants;


    public Facilities(FacilityType type,String location,double capacity,OperationalStatus  operationalStatus,Map<Resources,Double> resources,List<Equipment> equipment,  List<Agent> occupants)
    {   
        if (type == null)
            throw new IllegalArgumentException("Invalid Facility Type");
        this.type = type;
        // location
        if(location==null || location.isBlank())
            throw new IllegalArgumentException("Invalid Location");
        else this.location = location;
        // capacity
        if(capacity < 0 || !Double.isFinite(capacity))
            throw new IllegalArgumentException("Invalid Capacity");
        else this.capacity = capacity;
        // operationalStatus
        if (operationalStatus == null)
        throw new IllegalArgumentException("Invalid Operational Status");
        this.operationalStatus = operationalStatus;
        // resources
        validateResources(resources);
        this.resources = new HashMap<>(resources);
        // equipment
        if(equipment==null){
            throw new IllegalArgumentException("Invalid Equipment");
        }
        for(Equipment eq : equipment){
            if(eq==null){
                throw new IllegalArgumentException("Invalid Equipment");
            }
        }
        this.equipment = new ArrayList<>(equipment);
        if (occupants == null)
            {throw new IllegalArgumentException("Invalid Occupants");}
        for (Agent agent : occupants){
        {
            if (agent == null)
                throw new IllegalArgumentException("Invalid Occupants");
        }
    }
    this.occupants = new ArrayList<>(occupants);
    }


    public String getLocation(){return this.location;}
    public double getCapacity(){return this.capacity;}
    public OperationalStatus getOperationalStatus(){return this.operationalStatus;}
    public Map<Resources,Double> getResources(){return new HashMap<>(this.resources);}
    public List<Equipment> getEquipment(){return new ArrayList<>(this.equipment);}
    public List<Agent> getOccupants() {return new ArrayList<>(this.occupants);}

    private void validateResources(Map<Resources,Double> resources)
    {
        if(resources==null)
            throw new IllegalArgumentException("Invalid Resources");
        for(Map.Entry<Resources,Double> entry : resources.entrySet())
        {
            Resources resource = entry.getKey();
            Double amount = entry.getValue();
            if(resource==null || amount==null || !Double.isFinite(amount) || amount<0)
            {
                throw new IllegalArgumentException("Invalid Resources Data");
            }
        }
    }

    public void  setOperationalStatus(OperationalStatus operationalStatus)
    {
        if (operationalStatus == null)
            throw new IllegalArgumentException("Invalid Operational Status");
        else this.operationalStatus = operationalStatus;
    }


    public void setEquipment(List<Equipment> equipment)
    {
        if(equipment==null )
            throw new IllegalArgumentException("Invalid Equipment");
        for(Equipment eq : equipment){
            if(eq==null){
                throw new IllegalArgumentException("Invalid Equipment");
            }
        }
        this.equipment = new ArrayList<>(equipment);
    }


    public void setResources(Map<Resources,Double> resources)
    {
        validateResources(resources);
        this.resources = new HashMap<>(resources);
    }



    public void setOccupants(List<Agent> occupants)
    {    
        if (occupants == null)
            throw new IllegalArgumentException("Invalid Occupants");
        for (Agent agent : occupants){
            if (agent == null)
                throw new IllegalArgumentException("Invalid Occupants");
        }
        this.occupants = new ArrayList<>(occupants);
}


// in Facilities: reading how much of a resource this facility holds
public double getResourceAmount(Resources r)
{
    return resources.getOrDefault(r, 0.0);
}
public double deductResource(Resources r, double amount)
    {
        if (r == null || amount < 0 || !Double.isFinite(amount))
            throw new IllegalArgumentException("Invalid deduction");
        double actual = Math.min(getResourceAmount(r), amount);
        resources.put(r, getResourceAmount(r) - actual);
        return actual;
    }
public void addResource(Resources r, double amount)
    {
        if (r == null || amount < 0 || !Double.isFinite(amount))
            throw new IllegalArgumentException("Invalid amount");
        
        double newAmount = getResourceAmount(r) + amount;
        
        if(!Double.isFinite(newAmount)){
            throw  new IllegalArgumentException("Resource amount overFlow");
        }
        resources.put(r, newAmount);
    }
}
