package personnel;

import mission.Mission;

public class MedicalOfficer {

(String name,int id,String healthStatus,String currentLocation,String skills,double fatigueLevel,boolean availability){

    super(name, id, healthStatus, currentLocation, skills, fatigueLevel, availability);

    this.mission = mission;
}

public getMission() {
    return mission; 

}

public void setMission(Mission mission) {
    this.mission = mission;
}

}