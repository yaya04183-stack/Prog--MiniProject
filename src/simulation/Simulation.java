package simulation;

import colony.Colony;

public class Simulation {
    public static int time =0;

    public static void advancetime()
    {
        time++;
    }

    public static void advanceTime(Colony colony) {
        advanceTime(colony, 1);
    }

    public static void advanceTime(Colony colony, int timeUnits) {
        if (colony == null) {
            throw new IllegalArgumentException("Colony cannot be null");
        }
        if (timeUnits <= 0) {
            throw new IllegalArgumentException("Time units must be positive");
        }

        for (int i = 0; i < timeUnits; i++) {
            time++;
            colony.advanceTimeUnit();
        }
    }

    public static void advancetime(Colony colony) {
        advanceTime(colony, 1);
    }

    public static void resettime()
    {
        time = 0;
    }
    public static void showtime()
    {
        System.out.println("Current time: " + time);
    }
}
