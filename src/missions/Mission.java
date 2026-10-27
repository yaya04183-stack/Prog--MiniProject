package missions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import resources.Resources;

public abstract class Mission 
{
    private List<Agent> agents;
    private int neededtimesunits;
    private Facility facility;
    private Missionstate state;
    private Map<Resources,Double> requiredResources;
    
    public Mission(List<Agent> agents,int neededtimesunits,Facility facility,Map<Resources, Double> requiredResources){
    
    if(requiredResources == null)
    {
        throw new IllegalArgumentException("Invalid required resources");
    }
    else
    {

    for(Map.Entry<Resources, Double> entry : requiredResources.entrySet())
        {
            if(entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0)
            {
                throw new IllegalArgumentException("Invalid required resources");
            }
        }
        this.requiredResources = new HashMap<>(requiredResources);
    }

    if(agents==null)
            throw new IllegalArgumentException("Invalid agent");
    else this.agents = new ArrayList<>(agents);

    
    if(neededtimesunits <= 0)
            throw new IllegalArgumentException("Invalid timeUnits");
    else this.neededtimesunits = neededtimesunits;
    

    state = null;
    this.facility = facility;
        
    }
    
    public List<Agent> getAgents(){return new ArrayList<>(this.agents);}
    public int getNeededTimesUnits(){return this.neededtimesunits;}
    public Missionstate getState(){return this.state;}
    public Facility getFacility(){return this.facility;}
    
    public Map<Resources,Double> getRequiredResources(){return new HashMap<>(this.requiredResources);}  



    public void setFacility(Facility facility)
    {
        this.facility = facility;
    }

    public void setAgents(List<Agent> agents)
    {
    if(agents==null)
            throw new IllegalArgumentException("Invalid agent");
    else this.agents = new ArrayList<>(agents);
    }


    public void setNeededTimeUnits(int neededtimesunits)
    {
        if(neededtimesunits <= 0)
            throw new IllegalArgumentException("Invalid timeUnits");
        else this.neededtimesunits = neededtimesunits;
    }

    public void setState(Missionstate state) 
    {
        if (state == null)
            throw new IllegalArgumentException("Invalid State");
        this.state = state;
    }


    public void assign(Agent agent)
    {
        if(agent == null)
        {
            throw new IllegalArgumentException("Invalid Agent");
        }
        if(this.agents.contains(agent)){
            throw new IllegalArgumentException("Agent Already assigned");
        }
        
        state = Missionstate.ASSIGNED;
        this.agents.add(agent);
        
    }
    
    public boolean checkresources()
    {
        return 
    }
}







