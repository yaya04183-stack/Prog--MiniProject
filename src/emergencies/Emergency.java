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

    /** Retains the original constructor used by the in-progress design. */
    public Emergency(Object location, List<Agent> agents) {
        this(null, location, agents);
    }

    public Emergency(EmergencyType type, Object location) {
        this(type, location, new ArrayList<>());
        if (type == null) {
            throw new IllegalArgumentException("Invalid Emergency Type");
        }
    }

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

    public EmergencyType getType() {
        return this.type;
    }

    public EmergencyStatus getStatus() {
        return this.status;
    }

    public String getReason() {
        return this.reason;
    }

    public String getFailureReason() {
        return this.failureReason;
    }

    public Object getLocation() {
        return this.location;
    }

    public List<Agent> getAgents() {
        return new ArrayList<>(this.agents);
    }

    public Map<Resources, Double> getRequiredResources() {
        return new HashMap<>(this.requiredResources);
    }

    public void setLocation(Object location) {
        if (location == null || (!(location instanceof Facilities)
                && (!(location instanceof String) || ((String) location).isBlank()))) {
            throw new IllegalArgumentException("Invalid Location");
        }
        this.location = location;
    }

    public void setAgents(List<Agent> agents) {
        if (agents == null || agents.stream().anyMatch(agent -> agent == null)) {
            throw new IllegalArgumentException("Invalid Agents");
        }
        this.agents = new ArrayList<>(agents);
    }

    public void setReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Invalid Reason");
        }
        this.reason = reason;
        this.failureReason = reason;
    }

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

    public final void registerOccurrence() {
        if (!this.occurrenceRegistered) {
            this.occurrenceRegistered = true;
            onOccurrence();
        }
    }

    protected void onOccurrence() { }

    protected void onUnresolved() { }

    public boolean hasRequiredResponders(List<Agent> responders) {
        return this.type != null && responders != null && responders.stream()
                .anyMatch(agent -> agent != null && agent.canHandleEmergency(this.type));
    }

    public boolean requiresOperationalFacility() {
        return false;
    }

    public abstract boolean applyResponse();

    public void markResolved() {
        this.status = EmergencyStatus.RESOLVED;
        this.reason = "";
        this.failureReason = "";
    }

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
