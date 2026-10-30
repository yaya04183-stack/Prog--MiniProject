package missions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import enums.Mission_Emergency_States;
import enums.Resources;
import enums.Capability;
import agents.Agent;
import facilities.Facilities;
import enums.OperationalStatus;

public class Rescue extends Mission
{
    public Rescue(
                    int needtimesunits,
                    Facilities facility,
                    Map<Resources, Double> requiredResources,
                    Set<Capability> requiredCapabilities)
    {
        super(needtimesunits,facility,requiredResources,requiredCapabilities);
    }

}
