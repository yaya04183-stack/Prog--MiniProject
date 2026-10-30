package missions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import enums.Resources;
import enums.Mission_Emergency_States;
import enums.Capability;
import agents.Agent;
import facilities.Facilities;
import facilities.Laboratory;
import agents.Agent;
import enums.Skills;
import enums.OperationalStatus;

public class Research extends Mission
{
    private String topic;
    private boolean experimentCompleted;
    public Research(int needtimeunits,Facilities facility,Map<Resources,Double> requiredResources,Set<Capability> requiredCapabilities,String topic)
    {
        super(needtimeunits,facility,requiredResources,requiredCapabilities);
        if(topic==null || topic.isBlank())
            throw new IllegalArgumentException("Invalid Topic Of Research");
        else this.topic = topic;  
        
        if(!(facility instanceof Laboratory)){
            throw new IllegalArgumentException(
                "Research mission requires a Labratory"
            );
        }
    }
    public String getTopic(){return this.topic;}
    public boolean isExperimentCompleted() { return experimentCompleted; }
    public void setTopic(String topic)
    {
        if(topic==null || topic.isBlank())
            throw new IllegalArgumentException("Invalid Topic Of Research");
        else this.topic = topic;
    }

    @Override
    public boolean hasValidTeamComposition(List<Agent> candidateAgents) {
        return candidateAgents != null
                && candidateAgents.stream().anyMatch(agent -> agent != null
                        && agent.hasSkill(Skills.SCIENTIFIC))
                && hasOperationalLaboratoryEquipment();
    }

    private boolean hasOperationalLaboratoryEquipment() {
        if (!(getFacility() instanceof Laboratory laboratory)
                || laboratory.getOperationalStatus() != OperationalStatus.OPERATIONAL) {
            return false;
        }
        return laboratory.getEquipment().stream()
                .anyMatch(equipment -> equipment.getOperationalStatus()
                        == OperationalStatus.OPERATIONAL
                        && equipment.getCategory() == facilities.Equipment.Category.SCIENTIFIC);
    }

    @Override
    protected void onSuccessfulCompletion() {
        experimentCompleted = true;
    }
}
