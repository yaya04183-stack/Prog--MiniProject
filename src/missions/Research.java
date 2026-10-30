package missions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import enums.Resources;
import enums.Mission_Emergency_States;
import enums.Capability;
import agents.Agent;
import facilities.Facilities;
import facilities.Laboratory;

public class Research extends Mission
{
    private String topic;
    public Research(int needtimeunits,Facilities facility,Map<Resources,Double> requiredResources,Set<Capability> requiredCapabilities,String topic)
    {
        super(needtimeunits,facility,requiredResources,requiredCapabilities);
        if(topic==null || topic.isBlank())
            throw new IllegalArgumentException("Invalid Topic Of Research");
        else this.topic = topic;  
        
        if(!(facility instanceof Laboratory)){
            throw new IllegalArgumentException(
                "Research mission requires a Labratory"
            );
        }
    }
    public String getTopic(){return this.topic;}
    public void setTopic(String topic)
    {
        if(topic==null || topic.isBlank())
            throw new IllegalArgumentException("Invalid Topic Of Research");
        else this.topic = topic;
    }
}
