package admingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import emergencies.Emergency;
import enums.EmergencyStatus;
import enums.Mission_Emergency_States;
import missions.Mission;

/**
 * Main window for the administrator-facing MARS-OPS console. It only reads
 * from and calls public methods on the existing domain model (colony,
 * missions, emergencies, personnel, facilities, robotic units) - no
 * simulation logic lives in this GUI layer.
 */
public final class AdminConsole extends JFrame {

    private final SimulationContext context;
    private final ColonyAnimationPanel animationPanel;
    private final List<Refreshable> refreshables = new ArrayList<>();
    private final JTextArea logArea = new JTextArea();

    private final Map<Mission, Mission_Emergency_States> lastMissionStates = new IdentityHashMap<>();
    private final Map<Emergency, EmergencyStatus> lastEmergencyStatuses = new IdentityHashMap<>();

    public AdminConsole() {
        super("MARS-OPS Admin Console");

        SampleColonyFactory.ColonyBundle bundle = SampleColonyFactory.buildColony();
        this.context = new SimulationContext(bundle.colony(), bundle.environment());
        this.animationPanel = new ColonyAnimationPanel(context);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);

        DashboardPanel dashboard = new DashboardPanel(context);
        PersonnelPanel personnelPanel = new PersonnelPanel(context);
        FacilitiesPanel facilitiesPanel = new FacilitiesPanel(context);
        RoboticUnitsPanel roboticUnitsPanel = new RoboticUnitsPanel(context);
        MissionsPanel missionsPanel = new MissionsPanel(context);
        EmergenciesPanel emergenciesPanel = new EmergenciesPanel(context);

        refreshables.add(dashboard);
        refreshables.add(personnelPanel);
        refreshables.add(facilitiesPanel);
        refreshables.add(roboticUnitsPanel);
        refreshables.add(missionsPanel);
        refreshables.add(emergenciesPanel);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.AMBER);
        tabs.setForeground(Color.BLACK);
        tabs.setFont(Theme.BOLD_FONT);
        tabs.addTab("👩‍🔧 Personnel", personnelPanel);
        tabs.addTab("🏠 Facilities", facilitiesPanel);
        tabs.addTab("🤖 Robotic Units", roboticUnitsPanel);
        tabs.addTab("🚀 Missions", missionsPanel);
        tabs.addTab("🚨 Emergencies", emergenciesPanel);

        JPanel centerColumn = new JPanel(new BorderLayout(0, 6));
        centerColumn.setBackground(Theme.BACKGROUND);
        centerColumn.add(dashboard, BorderLayout.NORTH);
        centerColumn.add(tabs, BorderLayout.CENTER);

        logArea.setEditable(false);
        logArea.setRows(8);
        logArea.setBackground(new java.awt.Color(0x24, 0x1B, 0x14));
        logArea.setForeground(new java.awt.Color(0xF4, 0xE3, 0xC1));
        logArea.setCaretColor(Theme.AMBER);
        logArea.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Live Activity Feed"));

        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerColumn, logScroll);
        centerSplit.setResizeWeight(0.8);
        centerSplit.setBackground(Theme.BACKGROUND);

        animationPanel.setMinimumSize(new Dimension(260, 100));
        animationPanel.setPreferredSize(new Dimension(340, 10));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, animationPanel, centerSplit);
        mainSplit.setResizeWeight(0);
        mainSplit.setContinuousLayout(true);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setDividerLocation(340);
        add(mainSplit, BorderLayout.CENTER);
        getContentPane().setBackground(Theme.BACKGROUND);

        context.addChangeListener(this::refreshAll);
        context.addLogListener(line -> SwingUtilities.invokeLater(() -> logArea.insert(line + "\n", 0)));
        context.addEmergencyListener(location -> animationPanel.flashEmergency(location));
        context.addEffectListener(effect -> animationPanel.addEffect(effect));

        Timer animationTimer = new Timer(40, e -> animationPanel.tick());
        animationTimer.start();

        Timer pollTimer = new Timer(700, e -> {
            detectAndLogTransitions();
            refreshAll();
        });
        pollTimer.start();

        context.log("Admin console started with a sample colony loaded.");
        refreshAll();

        setPreferredSize(new Dimension(1440, 880));
        pack();
        mainSplit.setDividerLocation(340);
        setLocationRelativeTo(null);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(Theme.SURFACE_PRIMARY);
        bar.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JLabel title = new JLabel("🪐 MARS-OPS Admin Console");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        title.setForeground(Color.WHITE);
        bar.add(title);
        bar.add(new JLabel("     "));

        JButton newColony = new JButton("New Empty Colony");
        Theme.styleAccentButton(newColony, Theme.LAVENDER);
        newColony.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Replace the current colony with a brand new empty one?", "New Colony",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SampleColonyFactory.ColonyBundle bundle = SampleColonyFactory.buildEmptyColony();
                lastMissionStates.clear();
                lastEmergencyStatuses.clear();
                context.replaceColony(bundle.colony(), bundle.environment());
                simulation.Simulation.resettime();
                context.log("Started a new, empty colony.");
            }
        });

        JButton reloadSample = new JButton("Reload Sample Colony");
        Theme.styleAccentButton(reloadSample, Theme.AMBER);
        reloadSample.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Replace the current colony with a freshly generated sample colony?", "Reload Sample",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SampleColonyFactory.ColonyBundle bundle = SampleColonyFactory.buildColony();
                lastMissionStates.clear();
                lastEmergencyStatuses.clear();
                context.replaceColony(bundle.colony(), bundle.environment());
                simulation.Simulation.resettime();
                context.log("Reloaded a fresh sample colony.");
            }
        });

        bar.add(newColony);
        bar.add(reloadSample);
        return bar;
    }

    private void detectAndLogTransitions() {
        for (Mission mission : context.colony().getMissions()) {
            Mission_Emergency_States previous = lastMissionStates.get(mission);
            Mission_Emergency_States current = mission.getState();
            if (previous != current) {
                lastMissionStates.put(mission, current);
                if (previous != null) {
                    context.log(mission.getClass().getSimpleName() + " mission transitioned "
                            + previous + " -> " + current
                            + (mission.getFailureReason() != null && current == Mission_Emergency_States.FAILED
                                    ? " (" + mission.getFailureReason() + ")" : ""));
                }
            }
        }
        for (Emergency emergency : context.colony().getEmergencies()) {
            EmergencyStatus previous = lastEmergencyStatuses.get(emergency);
            EmergencyStatus current = emergency.getStatus();
            if (previous != current) {
                lastEmergencyStatuses.put(emergency, current);
                if (previous != null) {
                    context.log(emergency.getClass().getSimpleName() + " emergency transitioned "
                            + previous + " -> " + current);
                }
            }
        }
    }

    private void refreshAll() {
        for (Refreshable refreshable : refreshables) {
            refreshable.refresh();
        }
    }
}
