package personnel;
public abstract class Personnel 
{
    private String name;
    private int id;
    private String healthStatus;
    private String currentLocation;
    private String skills;    
    private double fatigueLevel;
    private boolean availability;
    
    public Personnel(String name,int id,String healthStatus,String currentLocation,String skills,double fatigueLevel,boolean availability)
    {   
        // name
        if(name==null || name.isBlank())
            throw new IllegalArgumentException("Name Cannot be null or empty");
        else this.name = name;
        // id
        if(id <= 0)
            throw new IllegalArgumentException("Id Connot be negative or zero");
        else this.id = id;
        // healthStatus
        if(healthStatus==null || healthStatus.isBlank())
            throw new IllegalArgumentException("Health Status Connot be null or empty");
        else this.healthStatus = healthStatus;
        // currentLocations
        if(currentLocation==null || currentLocation.isBlank())
            throw new IllegalArgumentException("Current Locations Connot be null or emppty");
        else this.currentLocation = currentLocation;
        // skills
        if(skills==null || skills.isBlank())
            throw new IllegalArgumentException("skills Cannot be null or empty");    
        else this.skills = skills;
        //fatigueLevel
        if(fatigueLevel<0 || fatigueLevel>100)
            throw new IllegalArgumentException("Fatigue Level Cannot be negative of greater than 100");
        else this.fatigueLevel = fatigueLevel;
        this.availability = availability;
        }


    public String getName(){
        return name;
    }
    public int getId(){
        return id;
    }
    public String getCurrentLocation(){
        return  currentLocation ;
    }
    public String getSkills(){
        return  skills;
    }

    public double getFatigueLevel(){
        return fatigueLevel;
    }
    
    public boolean isAvailability(){
        return availability;
    }

    public String getHealthStatus() {
    return healthStatus;
    }
    


    public void setHealthStatus(String healthStatus){
    if(healthStatus==null || healthStatus.isBlank())
        throw new IllegalArgumentException("Health Status Connot be null or empty");
    else this.healthStatus = healthStatus;

    }

    public void setCurrentLocation(String currentLocation){
        if(currentLocation==null || currentLocation.isBlank())
            throw new IllegalArgumentException("Current Locations Connot be null or emppty");
        else this.currentLocation = currentLocation;
    }
    
    public void setFatigueLevel(double fatigueLevel){
        if(fatigueLevel<0 || fatigueLevel>100)
            throw new IllegalArgumentException("Fatigue Level Cannot be negative of greater than 100");
        else this.fatigueLevel = fatigueLevel;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }
}