package marsenvironment;

import agents.Agent;
import enums.Capability;
import enums.EnviromentStates;
import enums.Skills;
import java.util.List;
import missions.Exploration;
import missions.Mission;
import missions.Rescue;

public class MarsEnvironment {

    private EnviromentStates environmentState;

    // Initializes a MarsEnvironment instance.
    // Initializes a MarsEnvironment instance.
    public MarsEnvironment(EnviromentStates environmentState){
        SetEnvironmentState(environmentState);
    }




    // Returns mission restriction reason.
    public String getMissionRestrictionReason(Mission mission, List<Agent> agents) {
        if (mission == null || agents == null) {
            return null;
        }

        boolean usesRemoteDrone = false;
        boolean hasCommunicationAgent = false;

        for (Agent agent : agents) {
            if (agent == null) {
                continue;
            }
            usesRemoteDrone |= agent.hasCapability(Capability.AERIAL_RECONNAISSANCE);
            hasCommunicationAgent |= agent.hasSkill(Skills.COMMUNICATION);
        }

        if (environmentState == EnviromentStates.DUST_STORM
                && mission instanceof Exploration
                && hasCapability(agents, Capability.SURFACE_TRAVEL)) {
            return "Dust storm prevents exploration missions from starting";
        }

        if (environmentState == EnviromentStates.COMMUNICATION_DISTRIBUTION) {
            if (usesRemoteDrone) {
                return "Communication disruption prevents remote drone control";
            }
            if (mission instanceof Rescue && hasCommunicationAgent) {
                return "Communication disruption prevents communication-dependent missions";
            }
        }

        return null;
    }

    // Returns active mission failure reason.
    public String getActiveMissionFailureReason(Mission mission) {
        if (mission == null) {
            return null;
        }

        List<Agent> agents = mission.getAgents();
        if (environmentState == EnviromentStates.DUST_STORM
                && hasCapability(agents, Capability.SURFACE_TRAVEL)) {
            return "Dust storm caused the active rover mission to fail";
        }
        if (environmentState == EnviromentStates.COMMUNICATION_DISTRIBUTION) {
            if (hasCapability(agents, Capability.AERIAL_RECONNAISSANCE)) {
                return "Communication disruption prevents remote drone control";
            }
            if (mission instanceof Rescue && hasSkill(agents, Skills.COMMUNICATION)) {
                return "Communication disruption caused the communication-dependent mission to fail";
            }
        }
        return null;
    }

    // Checks whether it has capability.
    private boolean hasCapability(List<Agent> agents, Capability capability) {
        for (Agent agent : agents) {
            if (agent != null && agent.hasCapability(capability)) {
                return true;
            }
        }
        return false;
    }

    // Checks whether it has skill.
    private boolean hasSkill(List<Agent> agents, Skills skill) {
        for (Agent agent : agents) {
            if (agent != null && agent.hasSkill(skill)) {
                return true;
            }
        }
        return false;
    }


    // Getters

    // Handles get environment state.
    public EnviromentStates GetEnvironmentState() {
        return environmentState;
    }
    // Handles get environment state code.
    public int GetEnvironmentStateCode() {
        switch (environmentState) {
            case DUST_STORM:
                return 1;
            case COMMUNICATION_DISTRIBUTION:
                return 2;
            case NORMAL_CONDITION:
                return 3;
            default:
                throw new IllegalArgumentException("Unknown environment state: " + environmentState);
        }
    }

    // Setters

    // Handles set environment state.
    public void SetEnvironmentState(EnviromentStates environmentState) {
        if (environmentState == null) {
            throw new IllegalArgumentException("Environment state cannot be null");
        }
        this.environmentState = environmentState;
    }

}
