package emergencies;

import enums.EmergencyType;
import personnel.Personnel;
import java.util.Map;
import enums.Resources;

public class medicalEmergency extends Emergency
{
    private Personnel patient;

    public medicalEmergency(Personnel patient)
    {
        super(EmergencyType.MEDICAL,
                patient == null ? null : patient.getCurrentLocation());
        if (patient == null)
        {
            throw new IllegalArgumentException("Invalid patient");
        }
        this.patient = patient;
        setRequiredResources(Map.of(Resources.MEDICAL_SUPPLIES, 1.0));
    }

    public Personnel getPatient()
    {
        return patient;
    }

    public void setPatient(Personnel patient)
    {
        if (patient == null)
        {
            throw new IllegalArgumentException("Invalid patient");
        }
        this.patient = patient;
    }

    @Override
    public boolean requiresOperationalFacility() {
        return true;
    }

    @Override
    public boolean applyResponse() {
        this.patient.setHealthStatus("Stable");
        return true;
    }
}
