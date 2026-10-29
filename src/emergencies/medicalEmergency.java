package emergencies;

import enums.EmergencyType;
import personnel.Personnel;

public class medicalEmergency extends Emergency
{
    private Personnel patient;

    public medicalEmergency(Personnel patient)
    {
        super(EmergencyType.MEDICAL);
        if (patient == null)
        {
            throw new IllegalArgumentException("Invalid patient");
        }
        this.patient = patient;
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
}
