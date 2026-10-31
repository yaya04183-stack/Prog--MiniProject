package admingui;

/** Implemented by every panel that needs to re-pull data from the SimulationContext. */
interface Refreshable {
    void refresh();
}
