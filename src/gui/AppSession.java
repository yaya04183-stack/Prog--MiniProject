package gui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import agents.Agent;
import colony.Colony;
import emergencies.Emergency;
import facilities.Equipment;
import facilities.Facilities;
import missions.Mission;
import marsenvironment.MarsEnvironment;
import roboticunit.RoboticUnit;

/** Shared references for the GUI; all domain behavior stays in MARS-OPS classes. */
final class AppSession {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Colony colony;
    private MarsEnvironment environment;
    private final List<Mission> missionDrafts = new ArrayList<>();
    private final List<Emergency> emergencyDrafts = new ArrayList<>();
    private final List<ActivityEntry> activity = new ArrayList<>();

    AppSession(Colony colony, MarsEnvironment environment) {
        if (colony == null) {
            throw new IllegalArgumentException("Colony cannot be null");
        }
        if (environment == null) {
            throw new IllegalArgumentException("Mars environment cannot be null");
        }
        this.colony = colony;
        this.environment = environment;
    }

    Colony colony() {
        return colony;
    }

    MarsEnvironment environment() {
        return environment;
    }

    void setEnvironment(MarsEnvironment environment) {
        this.environment = environment;
    }

    List<Agent> agents() {
        List<Agent> result = new ArrayList<>(colony.getPersonnel());
        result.addAll(colony.getRoboticUnits());
        return result;
    }

    List<Mission> missions() {
        List<Mission> result = new ArrayList<>(colony.getMissions());
        for (Mission draft : missionDrafts) {
            if (!result.contains(draft)) {
                result.add(draft);
            }
        }
        return result;
    }

    List<Emergency> emergencies() {
        List<Emergency> result = new ArrayList<>(colony.getEmergencies());
        for (Emergency draft : emergencyDrafts) {
            if (!result.contains(draft)) {
                result.add(draft);
            }
        }
        return result;
    }

    List<Equipment> equipment() {
        List<Equipment> result = new ArrayList<>();
        Set<Equipment> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Facilities facility : colony.getFacilities()) {
            for (Equipment item : facility.getEquipment()) {
                if (seen.add(item)) {
                    result.add(item);
                }
            }
        }
        for (RoboticUnit robot : colony.getRoboticUnits()) {
            for (Equipment item : robot.getEquipment()) {
                if (seen.add(item)) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    void addMissionDraft(Mission mission) {
        missionDrafts.add(mission);
    }

    void removeMissionDraft(Mission mission) {
        missionDrafts.remove(mission);
    }

    List<Mission> missionDrafts() {
        return new ArrayList<>(missionDrafts);
    }

    void addEmergencyDraft(Emergency emergency) {
        emergencyDrafts.add(emergency);
    }

    void removeEmergencyDraft(Emergency emergency) {
        emergencyDrafts.remove(emergency);
    }

    List<ActivityEntry> activity() {
        return new ArrayList<>(activity);
    }

    void record(String operation, String relatedObject, String result, String details) {
        activity.add(0, new ActivityEntry(
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                operation,
                relatedObject,
                result,
                details == null ? "" : details));
    }

    void clearActivity() {
        activity.clear();
    }

    record ActivityEntry(
            String timestamp,
            String operation,
            String relatedObject,
            String result,
            String details) { }
}
