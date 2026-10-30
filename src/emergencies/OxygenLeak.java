package emergencies;

import enums.EmergencyType;
import facilities.Habitat;
import facilities.Facilities;
import agents.Agent;
import personnel.Personnel;
import roboticunit.RoboticUnit;
import java.util.List;
import java.util.Map;
import enums.Resources;


public class OxygenLeak extends Emergency {

    private Facilities affected_facility;
    private final double oxygenLossAmount;
    private boolean repaired;


    public OxygenLeak(Facilities affected_facility){
        this(affected_facility, 1.0);
    }

    public OxygenLeak(Facilities affected_facility, double oxygenLossAmount){
        super(EmergencyType.OXYGEN_LEAK, affected_facility);
        if (!Double.isFinite(oxygenLossAmount) || oxygenLossAmount <= 0) {
            throw new IllegalArgumentException("Invalid oxygen loss amount");
        }
        this.oxygenLossAmount = oxygenLossAmount;
        setAffected_facility(affected_facility);
        setRequiredResources(Map.of(Resources.SPARE_PARTS, 1.0));
    }

    public Facilities getAffected_facility() {
        return affected_facility;
    }

    public void setAffected_facility(Facilities affected_facility) {
        if(!(affected_facility instanceof Habitat)){
            throw new IllegalArgumentException("Oxygen leaks must affect a habitat");
        }
        this.affected_facility = affected_facility;
    }

    @Override
    public boolean hasRequiredResponders(List<Agent> responders) {
        if (responders == null) {
            return false;
        }
        boolean hasHumanRepairer = responders.stream().anyMatch(agent ->
                agent instanceof Personnel && agent.canHandleEmergency(getType()));
        boolean hasRobotRepairer = responders.stream().anyMatch(agent ->
                agent instanceof RoboticUnit && agent.canHandleEmergency(getType()));
        return hasHumanRepairer && hasRobotRepairer;
    }

    public double getOxygenLossAmount() {
        return this.oxygenLossAmount;
    }

    public boolean isRepaired() {
        return this.repaired;
    }

    @Override
    protected void onOccurrence() {
        ((Habitat) this.affected_facility).applyOxygenLoss(this.oxygenLossAmount);
    }

    @Override
    protected void onUnresolved() {
        ((Habitat) this.affected_facility).applyOxygenLoss(this.oxygenLossAmount);
    }

    @Override
    public boolean applyResponse() {
        this.repaired = true;
        return true;
    }
}
