package emergencies;

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

    protected void markResolved()
    {
        status = EmergencyStatus.RESOLVED;
    }

    protected void markUnresolved(String reason)
    {
        status = EmergencyStatus.UNRESOLVED;
        failureReason = reason;
    }
}