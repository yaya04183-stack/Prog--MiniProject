
package facilities;

public class PowerStation extends Facilities {

    public PowerStation(
            String location,
            double capacity,
            String operationalStatus,
            java.util.Map<resources.Resources, Double> resources,
            StringBuilder equipment,
            java.util.List<Agent> occupants) {

        super(location, capacity, operationalStatus,
              resources, equipment, occupants);
    }
}
