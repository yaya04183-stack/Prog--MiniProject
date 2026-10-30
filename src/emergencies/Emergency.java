package emergencies;

import enums.EmergencyStatus;
import enums.EmergencyType;
import agents.Agent;

import java.util.ArrayList;
import java.util.List;
import facilities.Facilities;

public abstract class Emergency
{
    private final EmergencyType type;
    private EmergencyStatus status = EmergencyStatus.PENDING;
    private String failureReason = "";
    private Object location;
    private List<Agent> agents;

    public Emergency(EmergencyType type,Object location)
    {
        if (type == null)
            throw new IllegalArgumentException("Invalid Emergency Type");
        this.type = type;

        if(location==null || (!(location instanceof Facilities) && !(location instanceof String && location.equals("Martian Surface"))))
            throw new IllegalArgumentException("Invalid Location");
        else this.location = location;

        agents = new ArrayList<>();
    }

    public EmergencyType getType() {return type;}
    public EmergencyStatus getStatus() {return status;}
    public String getFailureReason() {return failureReason;}
    public List<Agent> getAgents(){return new ArrayList<>(agents);}
    public Object getLocation(){return location;}

    public void setAgents(List<Agent> agents)
    {
        if(agents==null)
            throw new IllegalArgumentException("Invalid Agents");
        else this.agents = new ArrayList<>(agents);
    }
    public abstract boolean hasRequiredResponders(List<Agent> responders);

    public void markResolved()
    {
        status = EmergencyStatus.RESOLVED;
        failureReason = "";
    }

    public void markUnresolved(String reason)
    {   
        if(reason==null || reason.isBlank())
            throw new IllegalArgumentException("Reason Cannot be NULL Or Empty String");
        else 
        {
        status = EmergencyStatus.UNRESOLVED;
        failureReason = reason;
        }
    }
}
