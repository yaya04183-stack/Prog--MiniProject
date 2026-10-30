package emergencies;

import enums.EmergencyType;
import facilities.Facilities;


public class Fire extends Emergency {

    private Facilities affected_facility;


    public Fire(Facilities affected_facility){
        super(EmergencyType.FIRE);
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
