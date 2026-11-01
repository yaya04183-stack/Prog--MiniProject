package emergencies;

import agents.Agent;
import enums.EmergencyStatus;
import enums.EmergencyType;
import enums.Resources;
import facilities.Facilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Emergency {

    private EmergencyType type;
    private EmergencyStatus status = EmergencyStatus.PENDING;
    private String reason = "";
    private String failureReason = "";
    private Object location;
    private List<Agent> agents;
    private Map<Resources, Double> requiredResources = new HashMap<>();
    private boolean occurrenceRegistered;

    // Initializes a Emergency instance.
    // Initializes a Emergency instance.
    /** Retains the original constructor used by the in-progress design. */
    public Emergency(Object location, List<Agent> agents) {
        this(null, location, agents);
    }

    // Initializes a Emergency instance.
    // Initializes a Emergency instance.
    public Emergency(EmergencyType type, Object location) {
        this(type, location, new ArrayList<>());
        if (type == null) {
            throw new IllegalArgumentException("Invalid Emergency Type");
        }
    }

    // Initializes a Emergency instance.
    // Initializes a Emergency instance.
    private Emergency(EmergencyType type, Object location, List<Agent> agents) {
        if (location == null || (!(location instanceof Facilities)
                && (!(location instanceof String) || ((String) location).isBlank()))) {
            throw new IllegalArgumentException("Invalid Location");
        }
        if (agents == null || agents.stream().anyMatch(agent -> agent == null)) {
            throw new IllegalArgumentException("Invalid Agents");
        }
        this.type = type;
        this.location = location;
        this.agents = new ArrayList<>(agents);
    }

    // Getters

    // Returns type.
    public EmergencyType getType() {
        return this.type;
    }

    // Returns status.
    public EmergencyStatus getStatus() {
        return this.status;
    }

    // Returns reason.
    public String getReason() {
        return this.reason;
    }

    // Returns failure reason.
    public String getFailureReason() {
        return this.failureReason;
    }

    // Returns location.
    public Object getLocation() {
        return this.location;
    }

    // Returns agents.
    public List<Agent> getAgents() {
        return new ArrayList<>(this.agents);
    }

    // Returns required resources.
    public Map<Resources, Double> getRequiredResources() {
        return new HashMap<>(this.requiredResources);
    }

    // Setters

    // Sets location.
    public void setLocation(Object location) {
        if (location == null || (!(location instanceof Facilities)
                && (!(location instanceof String) || ((String) location).isBlank()))) {
            throw new IllegalArgumentException("Invalid Location");
        }
        this.location = location;
    }

    // Sets agents.
    public void setAgents(List<Agent> agents) {
        if (agents == null || agents.stream().anyMatch(agent -> agent == null)) {
            throw new IllegalArgumentException("Invalid Agents");
        }
        this.agents = new ArrayList<>(agents);
    }

    // Sets reason.
    public void setReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Invalid Reason");
        }
        this.reason = reason;
        this.failureReason = reason;
    }

    // Sets required resources.
    public void setRequiredResources(Map<Resources, Double> requiredResources) {
        if (requiredResources == null) {
            throw new IllegalArgumentException("Invalid emergency resources");
        }
        for (Map.Entry<Resources, Double> entry : requiredResources.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null
                    || !Double.isFinite(entry.getValue()) || entry.getValue() < 0) {
                throw new IllegalArgumentException("Invalid emergency resources");
            }
        }
        this.requiredResources = new HashMap<>(requiredResources);
    }

    // Registers occurrence.
    public final void registerOccurrence() {
        if (!this.occurrenceRegistered) {
            this.occurrenceRegistered = true;
            onOccurrence();
        }
    }

    // Handles occurrence.
    protected void onOccurrence() { }

    // Handles unresolved.
    protected void onUnresolved() { }

    // Checks whether it has required responders.
    public boolean hasRequiredResponders(List<Agent> responders) {
        return this.type != null && responders != null && responders.stream()
                .anyMatch(agent -> agent != null && agent.canHandleEmergency(this.type));
    }

    // Checks s operational facility.
    public boolean requiresOperationalFacility() {
        return false;
    }

    // Applies response.
    public abstract boolean applyResponse();

    // Marks resolved.
    public void markResolved() {
        this.status = EmergencyStatus.RESOLVED;
        this.reason = "";
        this.failureReason = "";
    }

    // Marks unresolved.
    public void markUnresolved(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason Cannot be NULL Or Empty String");
        }
        boolean firstUnresolvedResult = this.status != EmergencyStatus.UNRESOLVED;
        this.status = EmergencyStatus.UNRESOLVED;
        this.reason = reason;
        this.failureReason = reason;
        if (firstUnresolvedResult) {
            onUnresolved();
        }
    }
}
