package missions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import enums.Missionstate;
import enums.Resources;
import enums.Capability;
import agents.Agent;
import facilities.Facilities;

public abstract class Mission {

    private List<Agent> agents;
    private int neededtimesunits;
    private Facilities facility;
    private Missionstate state;
    private Map<Resources, Double> requiredResources;
    private Set<Capability> requiredCapabilities;

    public Mission(
            List<Agent> agents,
            int neededtimesunits,
            Facilities facility,
            Map<Resources, Double> requiredResources,
            Set<Capability> requiredCapabilities) {

        if (requiredResources == null) {
            throw new IllegalArgumentException(
                    "Invalid required resources");
        }

        for (Map.Entry<Resources, Double> entry
                : requiredResources.entrySet()) {

            if (entry.getKey() == null
                    || entry.getValue() == null
                    || entry.getValue() <= 0) {

                throw new IllegalArgumentException(
                        "Invalid required resources");
            }
        }

        this.requiredResources = new HashMap<>(requiredResources);

        if (requiredCapabilities == null
                || requiredCapabilities.contains(null)) {

            throw new IllegalArgumentException(
                    "Invalid required capabilities");
        }

        this.requiredCapabilities =
                new HashSet<>(requiredCapabilities);

        if (agents == null) {
            throw new IllegalArgumentException("Invalid agents");
        }

        this.agents = new ArrayList<>(agents);

        if (neededtimesunits <= 0) {
            throw new IllegalArgumentException("Invalid time units");
        }

        this.neededtimesunits = neededtimesunits;
        this.state = null;
        this.facility = facility;
    }

    // Getters

    public List<Agent> getAgents() {
        return new ArrayList<>(this.agents);
    }

    public int getNeededTimesUnits() {
        return this.neededtimesunits;
    }

    public Missionstate getState() {
        return this.state;
    }

    public Facilities getFacility() {
        return this.facility;
    }

    public Map<Resources, Double> getRequiredResources() {
        return new HashMap<>(this.requiredResources);
    }

    public Set<Capability> getRequiredCapabilities() {
        return new HashSet<>(this.requiredCapabilities);
    }

    // Setters

    public void setFacility(Facilities facility) {
        this.facility = facility;
    }

    public void setAgents(List<Agent> agents) {
        if (agents == null) {
            throw new IllegalArgumentException("Invalid agents");
        }

        this.agents = new ArrayList<>(agents);
    }

    public void setNeededTimeUnits(int neededtimesunits) {
        if (neededtimesunits <= 0) {
            throw new IllegalArgumentException("Invalid time units");
        }

        this.neededtimesunits = neededtimesunits;
    }

    public void setState(Missionstate state) {
        if (state == null) {
            throw new IllegalArgumentException("Invalid state");
        }

        this.state = state;
    }

    public void setRequiredCapabilities(
            Set<Capability> capabilities) {

        if (capabilities == null || capabilities.contains(null)) {
            throw new IllegalArgumentException(
                    "Invalid required capabilities");
        }

        this.requiredCapabilities =
                new HashSet<>(capabilities);
    }

    // Assign an agent

    public void assign(Agent agent) {
        if (agent == null) {
            throw new IllegalArgumentException("Invalid agent");
        }

        if (this.agents.contains(agent)) {
            throw new IllegalArgumentException(
                    "Agent already assigned");
        }

        this.agents.add(agent);
        this.state = Missionstate.ASSIGNED;
    }

    // Check required resources against available resources

    public boolean checkresources(
            Map<Resources, Double> availableResources) {

        if (availableResources == null) {
            return false;
        }

        for (Map.Entry<Resources, Double> entry
                : this.requiredResources.entrySet()) {

            Double available =
                    availableResources.get(entry.getKey());

            if (available == null
                    || available < entry.getValue()) {
                return false;
            }
        }

        return true;
    }
}
