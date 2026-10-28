package colony;
import java.util.ArrayList;
import java.util.List;

import facilities.Facilities;
import missions.Mission;
import personnel.Personnel;
import roboticunit.RoboticUnit;

public class Colony {

    private List<Personnel> personnel;
    private List<Facilities> facilities;
    private List<RoboticUnit> roboticUnits;
    private List<Mission> missions;
    private Mission activeMission;

    public Colony() {
        personnel = new ArrayList<>();
        facilities = new ArrayList<>();
        roboticUnits = new ArrayList<>();
        missions = new ArrayList<>();
        activeMission = null;
    }

    public void addPersonnel(Personnel person)
    {
        if(person==null){
            throw new IllegalArgumentException("The Person caoont be null or empty");
        }
        else {this.personnel.add(person);}
    }

    public void addFacility(Facilities facility)
    {
        if(facility==null)
        {
            throw new IllegalArgumentException("The Facility cannot be null or empty");
        }
        else 
        {
            this.facilities.add(facility);
        }
    }

    public void addRobot(RoboticUnit robot){
        if(robot == null) {
            throw new IllegalArgumentException("Robot cannot be null or empty");
        }
        else{
            this.roboticUnits.add(robot);
        }
            
    }
    
    /*
    public boolean sumbitMission(Mission mission){
        Hykon fyha lawo ynf3 n3ml el mission wala la.
    }   
    */

    public List<Personnel> getPersonnel(){return this.personnel;}
    public List<Facilities> getFacilities(){return this.facilities;}
    public List<RoboticUnit> getRoboticUnits(){return this.roboticUnits;}
    public List<Mission> getMissions(){return this.missions;}
    public Mission getActiveMission(){return this.activeMission;}
}
