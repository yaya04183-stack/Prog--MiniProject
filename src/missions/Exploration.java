package missions;
import java.util.Map;
import java.util.Set;
import enums.Resources;
import enums.Capability;
import facilities.Facilities;



public class Exploration extends Mission
{
    private String region;
    public Exploration(int neededtimesunits,Facilities facility,Map<Resources,Double> requiredResources,Set<Capability> requiredCapabilities,String region)
    {
        super(neededtimesunits,facility,requiredResources,requiredCapabilities);
        if(region==null ||region.isBlank())
        {
            throw new IllegalArgumentException("Invalid Region");
        }
        else this.region = region;
    }
    public void setRegion(String region)
    {
        if(region==null||region.isBlank())
            throw new IllegalArgumentException("Invalid Region");
        else this.region = region;
    }
    public String getRegion()
    {
        return this.region;
    }
}
