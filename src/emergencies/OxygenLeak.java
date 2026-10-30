package emergencies;

import enums.EmergencyType;
import facilities.Facilities;


public class OxygenLeak extends Emergency {

    private Facilities affected_facility;


    public OxygenLeak(Facilities affected_facility){
        super(EmergencyType.OXYGEN_LEAK);
        setAffected_facility(affected_facility);
    }

    public Facilities getAffected_facility() {
        return affected_facility;
    }

    public void setAffected_facility(Facilities affected_facility) {
        if(affected_facility==null){
            throw new IllegalArgumentException("Invalid affected facility");
        }
        this.affected_facility = affected_facility;
    }
}
