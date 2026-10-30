package emergencies;

import enums.EmergencyType;
import facilities.Facilities;
import facilities.Laboratory;
import agents.Agent;
import java.util.List;


public class Fire extends Emergency {

    private Facilities affected_facility;


    public Fire(Facilities affected_facility){
        super(EmergencyType.FIRE, affected_facility);
        setAffected_facility(affected_facility);
    }

    public Facilities getAffected_facility() {
        return affected_facility;
    }

    public void setAffected_facility(Facilities affected_facility) {
        if(!(affected_facility instanceof Laboratory)){
            throw new IllegalArgumentException("Fires must affect a laboratory");
        }
        this.affected_facility = affected_facility;
    }

    @Override
    protected void onOccurrence() {
        ((Laboratory) this.affected_facility).catchFire();
    }

    @Override
    public boolean applyResponse() {
        ((Laboratory) this.affected_facility).extinguishFire();
        return true;
    }

    @Override
    public boolean hasRequiredResponders(List<Agent> responders) {
        return super.hasRequiredResponders(responders);
    }
}
