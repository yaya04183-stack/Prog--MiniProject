package admingui;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import colony.Colony;
import marsenvironment.MarsEnvironment;
import missions.Mission;

/**
 * Holds the live Colony/MarsEnvironment the admin console is pointed at and
 * broadcasts change/log events to every panel. No simulation behavior lives
 * here - it only forwards calls into the existing colony/missions/emergencies
 * classes and keeps the GUI panels in sync with them.
 */
final class SimulationContext {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_LOG_ENTRIES = 600;

    private Colony colony;
    private MarsEnvironment environment;
    private final List<Mission> missionDrafts = new ArrayList<>();
    private final List<Runnable> changeListeners = new ArrayList<>();
    private final List<java.util.function.Consumer<String>> logListeners = new ArrayList<>();
    private final List<java.util.function.Consumer<Object>> emergencyListeners = new ArrayList<>();
    private final LinkedList<String> logEntries = new LinkedList<>();

    SimulationContext(Colony colony, MarsEnvironment environment) {
        this.colony = colony;
        this.environment = environment;
    }

    Colony colony() {
        return colony;
    }

    MarsEnvironment environment() {
        return environment;
    }

    void replaceColony(Colony newColony, MarsEnvironment newEnvironment) {
        this.colony = newColony;
        this.environment = newEnvironment;
        this.missionDrafts.clear();
        notifyChanged();
    }

    void addMissionDraft(Mission mission) {
        missionDrafts.add(mission);
        notifyChanged();
    }

    void removeMissionDraft(Mission mission) {
        missionDrafts.remove(mission);
        notifyChanged();
    }

    List<Mission> missionDrafts() {
        return new ArrayList<>(missionDrafts);
    }

    void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    void notifyChanged() {
        for (Runnable listener : new ArrayList<>(changeListeners)) {
            listener.run();
        }
    }

    void addLogListener(java.util.function.Consumer<String> listener) {
        logListeners.add(listener);
    }

    void log(String message) {
        String line = "[" + LocalTime.now().format(TIME_FORMAT) + "] " + message;
        logEntries.addFirst(line);
        while (logEntries.size() > MAX_LOG_ENTRIES) {
            logEntries.removeLast();
        }
        for (java.util.function.Consumer<String> listener : new ArrayList<>(logListeners)) {
            listener.accept(line);
        }
    }

    List<String> logEntries() {
        return new ArrayList<>(logEntries);
    }

    void addEmergencyListener(java.util.function.Consumer<Object> listener) {
        emergencyListeners.add(listener);
    }

    void notifyEmergencyTriggered(Object location) {
        for (java.util.function.Consumer<Object> listener : new ArrayList<>(emergencyListeners)) {
            listener.accept(location);
        }
    }

    record Effect(String text, Object location, Color color, long startNanos) { }

    private final List<java.util.function.Consumer<Effect>> effectListeners = new ArrayList<>();

    void addEffectListener(java.util.function.Consumer<Effect> listener) {
        effectListeners.add(listener);
    }

    void pushEffect(String text, Object location, Color color) {
        Effect effect = new Effect(text, location, color, System.nanoTime());
        for (java.util.function.Consumer<Effect> listener : new ArrayList<>(effectListeners)) {
            listener.accept(effect);
        }
    }
}
