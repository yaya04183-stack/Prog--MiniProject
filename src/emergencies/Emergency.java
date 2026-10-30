package emergencies;

import enums.EmergencyStatus;
import enums.EmergencyType;
import agents.Agent;
import java.util.List;

public abstract class Emergency
{
    private final EmergencyType type;
    private EmergencyStatus status = EmergencyStatus.PENDING;
    private String failureReason = "";

    public Emergency(EmergencyType type)
    {
        if (type == null)
            throw new IllegalArgumentException("Invalid Emergency Type");
        this.type = type;
    }

    public EmergencyType getType() { return type; }
    public EmergencyStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }

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
