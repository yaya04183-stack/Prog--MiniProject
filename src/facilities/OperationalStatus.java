package facilities;

public enum OperationalStatus
{
    OPERATIONAL,
    DAMAGED,
    UNAVAILABLE,
    DESTROYED;

    // Used by the colony when summing colony-wide resource totals
    public boolean isOperational()
    {
        return this == OPERATIONAL;
    }
}