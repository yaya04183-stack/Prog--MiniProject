
package facilities;

public class CommunicationStation extends Facilities {

    public CommunicationStation(
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
