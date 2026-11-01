package enums;

public enum OperationalStatus
{
    OPERATIONAL,
    DAMAGED,
    UNAVAILABLE,
    DESTROYED;

    // Used by the colony when summing colony-wide resource totals
    // Getters

    // Checks whether operational.
    public boolean isOperational()
    {
        return this == OPERATIONAL;
    }
}
