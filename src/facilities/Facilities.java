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


    // Initializes a Facilities instance.
    // Initializes a Facilities instance.
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



    // Validates resources.
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










// in Facilities: reading how much of a resource this facility holds
// Deducts resource.
public double deductResource(Resources r, double amount)
    {
        if (r == null || amount < 0 || !Double.isFinite(amount))
            throw new IllegalArgumentException("Invalid deduction");
        double actual = Math.min(getResourceAmount(r), amount);
        resources.put(r, getResourceAmount(r) - actual);
        return actual;
    }
// Adds resource.
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
    // Getters

    // Returns location.
    public String getLocation(){return this.location;}
    // Returns capacity.
    public double getCapacity(){return this.capacity;}
    // Returns operational status.
    public OperationalStatus getOperationalStatus(){return this.operationalStatus;}
    // Returns resources.
    public Map<Resources,Double> getResources(){return new HashMap<>(this.resources);}
    // Returns equipment.
    public List<Equipment> getEquipment(){return new ArrayList<>(this.equipment);}
    // Returns occupants.
    public List<Agent> getOccupants() {return new ArrayList<>(this.occupants);}
// Returns resource amount.
public double getResourceAmount(Resources r)
{
    return resources.getOrDefault(r, 0.0);
}

    // Setters

    // Sets operational status.
    public void  setOperationalStatus(OperationalStatus operationalStatus)
    {
        if (operationalStatus == null)
            throw new IllegalArgumentException("Invalid Operational Status");
        else this.operationalStatus = operationalStatus;
    }
    // Sets equipment.
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
    // Sets resources.
    public void setResources(Map<Resources,Double> resources)
    {
        validateResources(resources);
        this.resources = new HashMap<>(resources);
    }
    // Sets occupants.
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

}
