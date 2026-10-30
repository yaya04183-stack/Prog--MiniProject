package missions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import enums.Mission_Emergency_States;
import enums.Resources;
import enums.Capability;
import agents.Agent;
import facilities.Facilities;
import enums.OperationalStatus;
import enums.Skills;

public class Rescue extends Mission
{
    public Rescue(
                    int needtimesunits,
                    Facilities facility,
                    Map<Resources, Double> requiredResources,
                    Set<Capability> requiredCapabilities)
    {
        super(needtimesunits,facility,requiredResources,requiredCapabilities);
    }

    @Override
    public boolean hasValidTeamComposition(List<Agent> candidateAgents) {
        if (candidateAgents == null) {
            return false;
        }

        boolean hasCommunicationCapablePersonnel = candidateAgents.stream()
                .anyMatch(agent -> agent != null
                        && agent.hasSkill(Skills.COMMUNICATION));
        boolean hasAppropriateVehicle = candidateAgents.stream()
                .anyMatch(agent -> agent != null
                        && (agent.hasCapability(Capability.SURFACE_TRAVEL)
                        || agent.hasCapability(Capability.ACCESS_INACCESSIBLE_AREAS)));

        return hasCommunicationCapablePersonnel && hasAppropriateVehicle;
    }
    

}
