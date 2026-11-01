package emergencies;

import enums.EmergencyType;
import facilities.Facilities;
import facilities.Laboratory;
import agents.Agent;
import java.util.List;


public class Fire extends Emergency {

    private Facilities affected_facility;


    // Initializes a Fire instance.
    // Initializes a Fire instance.
    public Fire(Facilities affected_facility){
        super(EmergencyType.FIRE, affected_facility);
        setAffected_facility(affected_facility);
    }

    // Getters

    // Returns affected facility.
    public Facilities getAffected_facility() {
        return affected_facility;
    }

    // Setters

    // Sets affected facility.
    public void setAffected_facility(Facilities affected_facility) {
        if(!(affected_facility instanceof Laboratory)){
            throw new IllegalArgumentException("Fires must affect a laboratory");
        }
        this.affected_facility = affected_facility;
    }

    // Handles occurrence.
    @Override
    protected void onOccurrence() {
        ((Laboratory) this.affected_facility).catchFire();
    }

    // Applies response.
    @Override
    public boolean applyResponse() {
        ((Laboratory) this.affected_facility).extinguishFire();
        return true;
    }

    // Checks whether it has required responders.
    @Override
    public boolean hasRequiredResponders(List<Agent> responders) {
        return super.hasRequiredResponders(responders);
    }
}
