package simulation;

public class Simulation {
    public static int time =0;

    public static void advancetime()
    {
        time++;
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
