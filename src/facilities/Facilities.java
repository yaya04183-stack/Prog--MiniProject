package facilities;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import resources.Resources;

public  abstract class Facilities
{
    private String  location;
    private double capacity;
    private String operationalStatus;
    private Map<Resources,Double> resources;
    private StringBuilder equipment;
    private List<Agent> occupants;

    public Facilities(String location,double capacity,String operationalStatus,Map<Resources,Double> resources,StringBuilder equipment,  List<Agent> occupants)
    {   
        // location
        if(location==null || location.isBlank())
            throw new IllegalArgumentException("Invalid Location");
        else this.location = location;
        // capacity
        if(capacity < 0)
            throw new IllegalArgumentException("Invalid Capacity");
        else this.capacity = capacity;
        // operationalStatus
        if(operationalStatus==null || operationalStatus.isBlank())
            throw new IllegalArgumentException("Invalid Operational Status");
        else this.operationalStatus = operationalStatus;
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
    public String getOperationalStatus(){return this.operationalStatus;}
    public Map<Resources,Double> getResources(){return this.resources;}
    public StringBuilder getEquipment(){return this.equipment;}
    public List<Agent> getOccupants() {return new ArrayList<>(this.occupants);}

    public void  setOperationalStatus(String operationalStatus)
    {
        if(operationalStatus==null || operationalStatus.isBlank())
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

    public void setOccupants(List<Agent> occupants) {
        if (occupants == null)
            throw new IllegalArgumentException("Invalid Occupants");
        else
            this.occupants = new ArrayList<>(occupants);
}
}