package facilities;

public class Habitate extends Facilities {

    public Habitate(
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