package missions;
import java.util.Map;
import java.util.List;
import java.util.Set;
import agents.Agent;
import enums.Resources;
import enums.Capability;
import enums.Skills;
import facilities.Facilities;
import roboticunit.RoboticUnit;



public class Exploration extends Mission
{
    private String region;
    public Exploration(int neededtimesunits,Facilities facility,Map<Resources,Double> requiredResources,Set<Capability> requiredCapabilities,String region)
    {
        super(neededtimesunits,facility,requiredResources,requiredCapabilities);
        if(region==null ||region.isBlank())
        {
            throw new IllegalArgumentException("Invalid Region");
        }
        else this.region = region;
    }
    public void setRegion(String region)
    {
        if(region==null||region.isBlank())
            throw new IllegalArgumentException("Invalid Region");
        else this.region = region;
    }
    public String getRegion()
    {
        return this.region;
    }

    @Override
    public boolean hasValidTeamComposition(List<Agent> candidateAgents) {
        return candidateAgents != null
                && candidateAgents.stream().anyMatch(agent -> agent != null
                        && agent.hasSkill(Skills.SCIENTIFIC))
                && candidateAgents.stream().anyMatch(agent -> agent instanceof RoboticUnit rover
                        && rover.hasCapability(Capability.SURFACE_TRAVEL)
                        && rover.hasCapability(Capability.EQUIPMENT_CARRYING)
                        && rover.hasOperationalEquipment());
    }
}
