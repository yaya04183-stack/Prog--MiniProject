package roboticunit;
import missions.Mission;
import enums.Capability;

import java.util.Set;
import java.util.HashSet;

public abstract class RoboticUnit implements Agent 
{
    private double batteryLevel;
    private String healthStatus;
    private String currentLocation;
    private boolean availability;
    private Set<Capability> capabilities;


    public RoboticUnit(double batteryLevel,String healthStatus,String currentLocation , boolean availability,Set<Capability> capabilities)
    {
    if(batteryLevel < 0 || batteryLevel>100) 
        throw new IllegalArgumentException("Invalid Battery Level");
    else 
        this.batteryLevel = batteryLevel;
    
    if(healthStatus == null || healthStatus.isBlank()) 
        throw new IllegalArgumentException("Invalid Status");
    else 
        this.healthStatus = healthStatus;
    
    if(currentLocation == null || currentLocation.isBlank()) 
        throw new IllegalArgumentException("Invalid Current Location");
    else 
        this.currentLocation = currentLocation;

    this.availability = availability;

    if(capabilities == null || capabilities.isEmpty() || capabilities.contains(null)) 
        throw new IllegalArgumentException("Invalid Capability");
    else 
        this.capabilities = new HashSet<>(capabilities);
    }

    public double getBatteryLevel(){return this.batteryLevel;}
    public String getHealthStatus(){return this.healthStatus;}
    public String getCurrentLocation(){return this.currentLocation;}
    public boolean getAvailability(){return this.availability;}
    public Set<Capability> getCapabilities(){return new HashSet<>(this.capabilities);}

    public void setBatteryLevel(double batteryLevel)
    {
        if(batteryLevel<0 || batteryLevel>100) throw new IllegalArgumentException("Invalid Battery Level");
        else this.batteryLevel = batteryLevel;
    }
    public void setHealthStatus(String healthStatus)
    {
        if(healthStatus == null || healthStatus.isBlank()) throw new IllegalArgumentException("Invalid Status");
        else this.healthStatus = healthStatus;
    }
    public void setCurrentLocation(String currentLocation)
    {
        if(currentLocation == null || currentLocation.isBlank()) throw new IllegalArgumentException("Invalid Current Location");
        else this.currentLocation = currentLocation;
    }
    public void setAvailability(boolean availability)
    {
        this.availability = availability;
    }

    public void setCapability(Set<Capability> capabilities)
    {
        if(capabilities == null || capabilities.isEmpty() || capabilities.contains(null) ) throw new IllegalArgumentException("Invalid capabilities");
        else this.capabilities = new HashSet<>(capabilities);
    }

    public void makeAvailable(){
        setAvailability(true);
    }

    public  boolean isAvailable(){
        return getAvailability();
    }
    


}


