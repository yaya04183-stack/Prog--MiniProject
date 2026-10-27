package roboticunit;


public class RoboticUnit 
{
    private double batteryLevel;
    private String healthStatus;
    private String currentLocation;
    private boolean availability;


    public RoboticUnit(double batteryLevel,String healthStatus,String currentLocation , boolean availability)
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
    }

    public double getBatteryLevel(){return this.batteryLevel;}
    public String getHealthStatus(){return this.healthStatus;}
    public String getCurrentLocation(){return this.currentLocation;}
    public boolean getAvailability(){return this.availability;}

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
    
}
    

