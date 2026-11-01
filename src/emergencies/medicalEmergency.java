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

    // Getters

    // Returns patient.
    public Personnel getPatient()
    {
        return patient;
    }

    // Setters

    // Sets patient.
    public void setPatient(Personnel patient)
    {
        if (patient == null)
        {
            throw new IllegalArgumentException("Invalid patient");
        }
        this.patient = patient;
    }

    // Checks s operational facility.
    @Override
    public boolean requiresOperationalFacility() {
        return true;
    }

    // Applies response.
    @Override
    public boolean applyResponse() {
        this.patient.setHealthStatus("Stable");
        return true;
    }
}
