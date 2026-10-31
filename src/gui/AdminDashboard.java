package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import agents.Agent;
import colony.Colony;
import emergencies.Emergency;
import emergencies.Fire;
import emergencies.medicalEmergency;
import emergencies.OxygenLeak;
import emergencies.RoverBreakdown;
import enums.Capability;
import enums.EmergencyStatus;
import enums.EmergencyType;
import enums.EnviromentStates;
import enums.FacilityType;
import enums.Mission_Emergency_States;
import enums.OperationalStatus;
import enums.Resources;
import enums.Skills;
import facilities.CommunicationStation;
import facilities.Equipment;
import facilities.Facilities;
import facilities.Habitat;
import facilities.Laboratory;
import facilities.PowerStation;
import facilities.StorageFacility;
import marsenvironment.MarsEnvironment;
import missions.Exploration;
import missions.Mission;
import missions.Repair;
import missions.Rescue;
import missions.Research;
import personnel.Engineer;
import personnel.MedicalOfficer;
import personnel.Personnel;
import personnel.Scientist;
import roboticunit.Drone;
import roboticunit.ExplorationRover;
import roboticunit.RepairRobot;
import roboticunit.RoboticUnit;
import simulation.Simulation;

/** Swing administration UI backed by the project's existing domain objects. */
final class AdminDashboard extends JFrame {

    private static final Color BACKGROUND = new Color(232, 226, 212);
    private static final Color SIDEBAR = new Color(34, 42, 38);
    private static final Color SIDEBAR_TEXT = new Color(244, 239, 222);
    private static final Color SIDEBAR_MUTED = new Color(190, 188, 168);
    private static final Color CARD = new Color(249, 246, 235);
    private static final Color BORDER = new Color(201, 191, 172);
    private static final Color TEXT = new Color(39, 51, 44);
    private static final Color MUTED = new Color(99, 111, 101);
    private static final Color ACCENT = new Color(174, 83, 54);
    private static final Color GREEN = new Color(69, 105, 78);
    private static final Color RED = new Color(157, 58, 45);
    private static final Color CONTROL = new Color(238, 232, 219);
    private static final Color SELECTED = new Color(232, 216, 196);
    private static final Color TABLE_HEADER = new Color(222, 214, 196);
    private static final Color SUCCESS_TEXT = new Color(39, 78, 47);
    private static final Color SUCCESS_BG = new Color(220, 231, 209);
    private static final Color SUCCESS_BORDER = new Color(79, 119, 74);
    private static final Color WARNING_TEXT = new Color(105, 74, 28);
    private static final Color WARNING_BG = new Color(244, 231, 193);
    private static final Color WARNING_BORDER = new Color(190, 146, 52);
    private static final Color DANGER_TEXT = new Color(130, 57, 44);
    private static final Color DANGER_BG = new Color(242, 221, 210);
    private static final Color DANGER_BORDER = new Color(178, 87, 62);

    private static final String OVERVIEW = "Dashboard";
    private static final String AGENTS = "People & robots";
    private static final String FACILITIES = "Facilities";
    private static final String EMERGENCIES = "Emergencies";
    private static final String MISSIONS = "Missions";
    private static final String COLONY = "Colony status";
    private static final String TESTING = "Testing center";
    private static final String ACTIVITY = "Activity";

    private final AppSession session;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageHost = new JPanel(pageLayout);
    private final JLabel pageTitle = new JLabel(OVERVIEW);
    private final JLabel pageSubtitle = new JLabel("Live view of the in-memory MARS-OPS colony");
    private final JLabel sessionStatus = new JLabel("Demo session · Ready");
    private final Map<String, JButton> navigationButtons = new LinkedHashMap<>();
    private final Map<String, JLabel> metricValues = new LinkedHashMap<>();

    private final DefaultTableModel agentModel = model("Type", "Name / ID", "Availability",
            "Location", "Skills / capabilities", "Assignment", "Details", "Domain object");
    private final DefaultTableModel facilityModel = model("Facility", "Location", "Status",
            "Capacity", "Resources", "Equipment", "Occupants", "Domain object");
    private final DefaultTableModel equipmentModel = model("Name", "Category", "Status",
            "Owner", "Domain object");
    private final DefaultTableModel emergencyModel = model("Type", "Status", "Location",
            "Responders", "Failure reason", "Domain object");
    private final DefaultTableModel missionModel = model("Mission", "State", "Duration",
            "Elapsed", "Agents", "Required resources", "Failure reason", "Domain object");
    private final DefaultTableModel resourceModel = model("Resource", "Available amount");
    private final DefaultTableModel recentActivityModel = model("Time", "Operation", "Result", "Details");
    private final DefaultTableModel colonyAgentModel = model("Type", "Name / ID", "Availability", "Location");
    private final DefaultTableModel activityModel = model("Time", "Operation", "Object", "Result", "Details");

    private final JTable agentTable = table(agentModel, true);
    private final JTable facilityTable = table(facilityModel, true);
    private final JTable equipmentTable = table(equipmentModel, true);
    private final JTable emergencyTable = table(emergencyModel, true);
    private final JTable missionTable = table(missionModel, true);
    private final JTable resourceTable = table(resourceModel, false);
    private final JTable recentActivityTable = table(recentActivityModel, false);
    private final JTable colonyAgentTable = table(colonyAgentModel, false);
    private final JTable activityTable = table(activityModel, false);

    private final JComboBox<String> availabilityFilter =
            new JComboBox<>(new String[]{"All availability", "Available", "Busy"});
    private final JComboBox<String> agentAction = new JComboBox<>(new String[]{
            "Choose action…", "Change availability", "Set current location", "View details"});
    private final JComboBox<String> facilityAction = new JComboBox<>(new String[]{
            "Choose action…", "Change facility status", "Adjust facility resources",
            "Manage facility equipment", "Manage robot equipment", "Manage occupants"});
    private final JComboBox<String> missionAction = new JComboBox<>(new String[]{
            "Choose action…", "Check selected team", "View selected mission", "Unassign team member"});
    private final JTextField agentSearch = new JTextField(14);
    private final DefaultListModel<Agent> teamListModel = new DefaultListModel<>();
    private final JList<Agent> teamList = new JList<>(teamListModel);
    private final JTextField timeUnitsField = new JTextField("1", 4);
    private final JLabel activeMissionSummary = new JLabel("No active mission. Assign a draft to begin.");
    private JButton startMissionButton;
    private JButton advanceMissionButton;
    private JButton completeMissionButton;
    private JButton failMissionButton;
    private final ScenarioLabPanel scenarioLab;

    AdminDashboard(AppSession session) {
        super("MARS-OPS | Administration & Testing");
        this.session = session;
        this.scenarioLab = new ScenarioLabPanel(session, this::refreshAll);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int windowWidth = Math.max(420, Math.min(1480, screen.width - 32));
        int windowHeight = Math.max(480, Math.min(930, screen.height - 64));
        setMinimumSize(new Dimension(Math.min(900, windowWidth), Math.min(620, windowHeight)));
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        setContentPane(buildShell());
        buildPages();
        refreshAll();
        showPage(OVERVIEW);
    }

    private JPanel buildShell() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(22, 26, 14, 26));
        content.add(buildTopBar(), BorderLayout.NORTH);
        pageHost.setOpaque(false);
        content.add(pageHost, BorderLayout.CENTER);
        content.add(buildFooter(), BorderLayout.SOUTH);
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBackground(SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JPanel top = new JPanel(new BorderLayout(0, 3));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(25, 22, 25, 14));
        JLabel brand = new JLabel("MARS-OPS");
        brand.setForeground(SIDEBAR_TEXT);
        brand.setFont(new Font("Georgia", Font.BOLD, 24));
        JLabel tagline = new JLabel("COLONY CONTROL CENTER");
        tagline.setForeground(SIDEBAR_MUTED);
        tagline.setFont(new Font("Consolas", Font.BOLD, 9));
        top.add(brand, BorderLayout.NORTH);
        top.add(tagline, BorderLayout.CENTER);
        sidebar.add(top, BorderLayout.NORTH);

        JPanel nav = new JPanel(new GridLayout(0, 1, 0, 6));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(6, 12, 10, 12));
        List<String> pages = List.of(OVERVIEW, AGENTS, FACILITIES, EMERGENCIES,
                MISSIONS, COLONY, TESTING, ACTIVITY);
        for (int index = 0; index < pages.size(); index++) {
            String page = pages.get(index);
            JButton button = new JButton(page);
            // Use Swing's basic painter so the configured high-contrast colors
            // are honored consistently across Windows look-and-feel themes.
            button.setUI(new BasicButtonUI());
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setText(String.format("%02d   %s", index + 1, page));
            button.setFont(new Font("Consolas", Font.BOLD, 10));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(12, 13, 12, 10));
            button.setForeground(SIDEBAR_MUTED);
            button.setBackground(SIDEBAR);
            button.addActionListener(event -> showPage(page));
            navigationButtons.put(page, button);
            nav.add(button);
        }
        sidebar.add(nav, BorderLayout.CENTER);

        JLabel note = new JLabel("JAVA 17  |  IN-MEMORY SESSION");
        note.setForeground(SIDEBAR_MUTED);
        note.setFont(new Font("Consolas", Font.BOLD, 8));
        note.setBorder(new EmptyBorder(15, 17, 18, 10));
        sidebar.add(note, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        pageTitle.setForeground(TEXT);
        pageTitle.setFont(new Font("Georgia", Font.BOLD, 27));
        pageSubtitle.setForeground(MUTED);
        pageSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        titles.add(pageTitle, BorderLayout.NORTH);
        titles.add(pageSubtitle, BorderLayout.CENTER);
        panel.add(titles, BorderLayout.WEST);

        sessionStatus.setForeground(SUCCESS_TEXT);
        sessionStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sessionStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SUCCESS_BORDER),
                new EmptyBorder(9, 11, 9, 11)));
        sessionStatus.setOpaque(true);
        sessionStatus.setBackground(SUCCESS_BG);
        panel.add(sessionStatus, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 0, 0, 0));
        JLabel left = new JLabel("This is a demo session. Changes are not saved after closing.");
        left.setForeground(MUTED);
        left.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel right = new JLabel("No persistent storage");
        right.setForeground(MUTED);
        right.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void buildPages() {
        addPage(OVERVIEW, buildOverviewPage());
        addPage(AGENTS, buildAgentsPage());
        addPage(FACILITIES, buildFacilitiesPage());
        addPage(EMERGENCIES, buildEmergenciesPage());
        addPage(MISSIONS, buildMissionsPage());
        addPage(COLONY, buildColonyPage());
        addPage(TESTING, scenarioLab);
        addPage(ACTIVITY, buildActivityPage());
    }

    private void addPage(String name, JPanel page) {
        pageHost.add(page, name);
    }

    private void showPage(String page) {
        pageTitle.setText(page);
        pageSubtitle.setText(switch (page) {
            case OVERVIEW -> "A quick picture of crew, facilities, missions, and supplies";
            case AGENTS -> "Add crew and robots, then view or update their status";
            case FACILITIES -> "Manage locations, supplies, equipment, and occupants";
            case EMERGENCIES -> "Record an emergency and choose a response";
            case MISSIONS -> "Create a mission, choose a team, then manage its progress";
            case COLONY -> "View colony resources and current environment";
            case TESTING -> "Choose a test, see what it checks, and run it";
            default -> "Review the actions performed during this session";
        });
        pageLayout.show(pageHost, page);
        navigationButtons.forEach((name, button) -> {
            boolean selected = name.equals(page);
            button.setForeground(selected ? SIDEBAR_TEXT : SIDEBAR_MUTED);
            button.setBackground(selected ? ACCENT : SIDEBAR);
        });
        refreshAll();
    }

    private JPanel buildOverviewPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setOpaque(false);
        JPanel metrics = new JPanel(new GridLayout(2, 3, 12, 12));
        metrics.setOpaque(false);
        for (String key : List.of("Crew & robots", "Available", "Facilities",
                "Emergencies", "Active mission", "Completed missions")) {
            metrics.add(metricCard(key));
        }

        JPanel quickActions = new JPanel(new BorderLayout(0, 7));
        quickActions.setBackground(CARD);
        quickActions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(10, 12, 10, 12)));
        JLabel quickTitle = new JLabel("Common tasks");
        quickTitle.setForeground(TEXT);
        quickTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        quickActions.add(quickTitle, BorderLayout.NORTH);
        JPanel quickButtons = new JPanel(new GridLayout(2, 2, 8, 7));
        quickButtons.setOpaque(false);
        quickButtons.add(button("Add crew member", true, event -> createPersonnel()));
        quickButtons.add(button("Add robot", false, event -> createRobot()));
        quickButtons.add(button("Create mission", false, event -> createMission()));
        quickButtons.add(button("Record emergency", false, event -> createEmergency()));
        quickActions.add(quickButtons, BorderLayout.CENTER);

        JPanel upper = new JPanel(new BorderLayout(0, 12));
        upper.setOpaque(false);
        upper.add(metrics, BorderLayout.NORTH);
        upper.add(quickActions, BorderLayout.CENTER);
        page.add(upper, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 14, 0));
        lower.setOpaque(false);
        lower.add(tableCard("Colony resources", resourceTable));
        lower.add(tableCard("Recent activity", recentActivityTable));
        page.add(lower, BorderLayout.CENTER);
        return page;
    }

    private JPanel metricCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 9));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 16, 15, 14)));
        JLabel name = new JLabel(title.toUpperCase());
        name.setForeground(MUTED);
        name.setFont(new Font("Consolas", Font.BOLD, 9));
        JLabel value = new JLabel("0");
        value.setForeground(TEXT);
        value.setFont(new Font("Georgia", Font.BOLD, 31));
        metricValues.put(title, value);
        Color marker = switch (title) {
            case "Available", "Completed missions" -> GREEN;
            case "Emergencies" -> RED;
            case "Active mission" -> ACCENT;
            default -> new Color(150, 126, 74);
        };
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, marker),
                new EmptyBorder(15, 16, 15, 14)));
        card.add(name, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAgentsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        JPanel actions = toolbar(
                button("Add crew member", true, event -> createPersonnel()),
                button("Add robot", false, event -> createRobot()),
                agentAction,
                button("Apply to selected", false, event -> applyAgentAction()));
        agentAction.setToolTipText("Choose what to do with the selected person or robot");
        availabilityFilter.addActionListener(event -> applyAgentFilter());
        agentSearch.setToolTipText("Search agents");
        agentSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() { applyAgentFilter(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent event) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent event) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent event) { update(); }
        });
        JPanel filters = toolbar(new JLabel("Show:"), availabilityFilter,
                new JLabel("Find a person or robot:"), agentSearch);
        JPanel controls = new JPanel(new BorderLayout(0, 7));
        controls.setOpaque(false);
        controls.add(actions, BorderLayout.NORTH);
        controls.add(filters, BorderLayout.SOUTH);
        page.add(controls, BorderLayout.NORTH);
        page.add(tableCard("Registered personnel and robotic units", agentTable), BorderLayout.CENTER);
        return page;
    }

    private void applyAgentAction() {
        switch (String.valueOf(agentAction.getSelectedItem())) {
            case "Change availability" -> toggleAgentAvailability();
            case "Set current location" -> setAgentLocation();
            case "View details" -> showAgentDetails();
            default -> showInfo("Choose an action, then select a person or robot in the list.");
        }
    }

    private JPanel buildFacilitiesPage() {
        JPanel page = new JPanel(new BorderLayout(0, 11));
        page.setOpaque(false);
        JPanel actions = toolbar(button("Add facility", true, event -> createFacility()),
                button("Add equipment", false, event -> addEquipmentToFacility(
                        selectedObject(facilityTable, facilityModel, Facilities.class))),
                facilityAction, button("Apply to selected", false, event -> applyFacilityAction()));
        facilityTable.setToolTipText("Select a facility first to add equipment to it");
        facilityAction.setToolTipText("Choose an action for the selected facility or equipment");
        JPanel searches = toolbar(new JLabel("Find a facility:"),
                searchField(facilityTable, "Search facilities"),
                new JLabel("Find equipment:"), searchField(equipmentTable, "Search equipment"));
        JPanel controls = new JPanel(new BorderLayout(0, 7));
        controls.setOpaque(false);
        controls.add(actions, BorderLayout.NORTH);
        controls.add(searches, BorderLayout.SOUTH);
        page.add(controls, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tableCard("Facilities", facilityTable),
                tableCard("Equipment registered at facilities and robots", equipmentTable));
        split.setResizeWeight(0.58);
        split.setDividerLocation(360);
        split.setBorder(null);
        split.setOpaque(false);
        page.add(split, BorderLayout.CENTER);
        return page;
    }

    private void applyFacilityAction() {
        switch (String.valueOf(facilityAction.getSelectedItem())) {
            case "Change facility status" -> changeFacilityStatus();
            case "Adjust facility resources" -> adjustFacilityResource();
            case "Manage facility equipment" -> manageEquipment();
            case "Manage robot equipment" -> manageRobotEquipment();
            case "Manage occupants" -> manageOccupants();
            default -> showInfo("Choose an action, then select the matching item in the list.");
        }
    }

    private JPanel buildEmergenciesPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        JPanel toolbar = toolbar(
                button("Create emergency", true, event -> createEmergency()),
                button("Attempt response", false, event -> handleSelectedEmergency()),
                button("Details", false, event -> showEmergencyDetails()));
        toolbar.add(searchField(emergencyTable, "Search emergencies"));
        page.add(toolbar, BorderLayout.NORTH);
        page.add(tableCard("Registered and unsubmitted emergencies", emergencyTable), BorderLayout.CENTER);
        return page;
    }

    private JPanel buildMissionsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);

        JPanel firstRow = toolbar(
                button("New mission", true, event -> createMission()),
                button("Assign selected draft", false, event -> submitMission()),
                missionAction,
                button("Apply", false, event -> applyMissionAction()));
        missionAction.setToolTipText("Choose an action for the selected mission and team");
        firstRow.add(searchField(missionTable, "Find a mission"));

        JPanel advanceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        advanceRow.setOpaque(false);
        advanceRow.add(new JLabel("Advance by time units:"));
        advanceRow.add(timeUnitsField);
        advanceMissionButton = button("Advance", false, event -> advanceTime());
        completeMissionButton = button("Complete mission", false, event -> finishMission());
        failMissionButton = button("Record failure", false, event -> failMission());
        startMissionButton = button("Start mission", true, event -> startActiveMission());
        advanceRow.add(startMissionButton);
        advanceRow.add(advanceMissionButton);
        advanceRow.add(completeMissionButton);
        advanceRow.add(failMissionButton);

        JPanel activeControls = new JPanel(new BorderLayout(8, 5));
        activeControls.setBackground(CARD);
        activeControls.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 12, 8, 12)));
        activeMissionSummary.setForeground(TEXT);
        activeMissionSummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        activeControls.add(activeMissionSummary, BorderLayout.NORTH);
        activeControls.add(advanceRow, BorderLayout.CENTER);
        JPanel controls = new JPanel(new BorderLayout(0, 7));
        controls.setOpaque(false);
        controls.add(firstRow, BorderLayout.NORTH);
        controls.add(activeControls, BorderLayout.CENTER);

        configureAgentList(teamList);
        teamList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        teamList.setVisibleRowCount(8);

        JPanel teamPanel = new JPanel(new BorderLayout(0, 7));
        teamPanel.setBackground(CARD);
        teamPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(11, 12, 11, 12)));
        JLabel teamTitle = new JLabel("Team selection");
        teamTitle.setForeground(TEXT);
        teamTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel hint = new JLabel("Select one or more available people and robots. The colony checks the team when assigned.");
        hint.setForeground(MUTED);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JPanel captions = new JPanel(new BorderLayout(0, 3));
        captions.setOpaque(false);
        captions.add(teamTitle, BorderLayout.NORTH);
        captions.add(hint, BorderLayout.CENTER);
        teamPanel.add(captions, BorderLayout.NORTH);
        teamPanel.add(new JScrollPane(teamList), BorderLayout.CENTER);

        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tableCard("Mission registry and drafts", missionTable), teamPanel);
        right.setResizeWeight(0.63);
        right.setDividerLocation(360);
        right.setBorder(null);
        right.setOpaque(false);

        page.add(controls, BorderLayout.NORTH);
        page.add(right, BorderLayout.CENTER);
        return page;
    }

    private void applyMissionAction() {
        switch (String.valueOf(missionAction.getSelectedItem())) {
            case "Check selected team" -> checkMissionTeam();
            case "View selected mission" -> showMissionDetails();
            case "Unassign team member" -> unassignAgent();
            default -> showInfo("Choose an action first. Select a mission and, if needed, its team.");
        }
    }

    private void refreshMissionControls() {
        Mission active = session.colony().getActiveMission();
        if (active == null) {
            activeMissionSummary.setText("No active mission. Assign a draft to begin.");
            startMissionButton.setEnabled(false);
            advanceMissionButton.setEnabled(false);
            completeMissionButton.setEnabled(false);
            failMissionButton.setEnabled(false);
            return;
        }
        String state = humanize(active.getState() == null ? "Unknown" : active.getState().name());
        activeMissionSummary.setText(humanize(active.getClass().getSimpleName()) + " mission · "
                + state + " · " + active.getElapsedTimeUnits() + "/"
                + active.getNeededTimesUnits() + " time units elapsed");
        boolean assigned = active.getState() == Mission_Emergency_States.ASSIGNED;
        boolean inProgress = active.getState() == Mission_Emergency_States.INPROGRESS;
        startMissionButton.setEnabled(assigned);
        advanceMissionButton.setEnabled(inProgress);
        completeMissionButton.setEnabled(inProgress);
        failMissionButton.setEnabled(inProgress);
    }

    private JPanel buildColonyPage() {
        JPanel page = new JPanel(new BorderLayout(0, 13));
        page.setOpaque(false);
        JPanel controls = toolbar(
                button("Set environment", true, event -> setEnvironment()),
                button("Colony snapshot", false, event -> showColonyDetails()));
        JLabel note = new JLabel("All totals are read from Colony and registered facilities.");
        note.setForeground(MUTED);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        controls.add(note);
        page.add(controls, BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                tableCard("Personnel and robotic units", colonyAgentTable),
                tableCard("Aggregated operational resources", resourceTable));
        split.setResizeWeight(0.58);
        split.setDividerLocation(650);
        split.setBorder(null);
        split.setOpaque(false);
        page.add(split, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildActivityPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        JPanel toolbar = toolbar(button("Clear displayed log", false, event -> clearActivity()));
        toolbar.add(searchField(activityTable, "Filter activity"));
        JLabel hint = new JLabel("Logs only actions performed in this GUI session.");
        hint.setForeground(MUTED);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        toolbar.add(hint);
        page.add(toolbar, BorderLayout.NORTH);
        page.add(tableCard("Activity events", activityTable), BorderLayout.CENTER);
        return page;
    }

    private JPanel toolbar(Component... components) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        toolbar.setOpaque(false);
        for (Component component : components) {
            toolbar.add(component);
        }
        return toolbar;
    }

    private JButton button(String text, boolean primary,
            java.util.function.Consumer<ActionEvent> action) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(primary ? Color.WHITE : TEXT);
        button.setBackground(primary ? ACCENT : CONTROL);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? ACCENT : BORDER),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        button.addActionListener(event -> action.accept(event));
        return button;
    }

    private JTextField searchField(JTable target, String placeholder) {
        JTextField search = new JTextField(15);
        search.setToolTipText(placeholder);
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                if (target.getRowSorter() instanceof TableRowSorter<?> sorter) {
                    String value = search.getText().trim();
                    sorter.setRowFilter(value.isEmpty() ? null
                            : RowFilter.regexFilter("(?i)" + Pattern.quote(value)));
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent event) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent event) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent event) { update(); }
        });
        return search;
    }

    private JPanel tableCard(String title, JTable table) {
        JPanel card = new JPanel(new BorderLayout(0, 9));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(13, 13, 13, 13)));
        JLabel heading = new JLabel(title);
        heading.setForeground(TEXT);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(heading, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD);
        CardLayout dataLayout = new CardLayout();
        JPanel dataArea = new JPanel(dataLayout);
        dataArea.add(scroll, "data");
        JPanel empty = new JPanel(new BorderLayout());
        empty.setBackground(CARD);
        String emptyMessage = switch (title) {
            case "Registered personnel and robotic units" ->
                    "No people or robots yet. Use Add crew member or Add robot to get started.";
            case "Facilities" -> "No facilities have been added yet.";
            case "Emergencies", "Registered and unsubmitted emergencies" ->
                    "No emergencies have been recorded.";
            case "Equipment registered at facilities and robots" -> "No equipment has been added yet.";
            case "Personnel and robotic units" -> "No people or robots have been added yet.";
            case "Mission registry and drafts" -> "No missions yet. Choose New mission to create one.";
            case "Activity events", "Recent activity" -> "Actions from this session will appear here.";
            case "Colony resources", "Aggregated operational resources" ->
                    "No resources are available in the registered facilities yet.";
            default -> "There is nothing to show here yet.";
        };
        JLabel emptyLabel = new JLabel(emptyMessage, SwingConstants.CENTER);
        emptyLabel.setForeground(MUTED);
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        empty.add(emptyLabel, BorderLayout.CENTER);
        dataArea.add(empty, "empty");
        Runnable updateEmptyState = () -> dataLayout.show(dataArea,
                table.getModel().getRowCount() == 0 ? "empty" : "data");
        table.getModel().addTableModelListener(event -> updateEmptyState.run());
        updateEmptyState.run();
        card.add(dataArea, BorderLayout.CENTER);
        return card;
    }

    private void refreshAll() {
        refreshAgents();
        refreshFacilities();
        refreshEquipment();
        refreshEmergencies();
        refreshMissions();
        refreshResources();
        refreshActivity();
        refreshColonyAgents();
        refreshMetrics();
        refreshTeamList();
        refreshMissionControls();
    }

    private void refreshAgents() {
        clear(agentModel);
        for (Agent agent : session.agents()) {
            String type;
            String name;
            String skills;
            String assignment;
            String details;
            if (agent instanceof Personnel person) {
                type = humanize(person.getClass().getSimpleName());
                name = person.getName() + " (#" + person.getId() + ")";
                skills = formatEnums(person.getSkills());
                assignment = person.getMission() == null
                        ? "None" : humanize(person.getMission().getClass().getSimpleName());
                details = "Health " + person.getHealthStatus() + " | Fatigue "
                        + person.getFatigueLevel();
            } else if (agent instanceof RoboticUnit robot) {
                type = humanize(robot.getClass().getSimpleName());
                name = type;
                skills = formatEnums(robot.getCapabilities());
                assignment = robot.getMission() == null
                        ? "None" : humanize(robot.getMission().getClass().getSimpleName());
                details = "Battery " + robot.getBatteryLevel() + " | "
                        + robot.getHealthStatus() + " | Equipment " + robot.getEquipment().size();
            } else {
                type = humanize(agent.getClass().getSimpleName());
                name = type;
                skills = "Domain interface only";
                assignment = "Not exposed by Agent interface";
                details = "";
            }
            agentModel.addRow(new Object[]{type, name,
                    agent.isAvailable() ? "Available" : "Busy",
                    agent.getCurrentLocation(), skills, assignment, details, agent});
        }
        applyAgentFilter();
    }

    private void applyAgentFilter() {
        if (!(agentTable.getRowSorter() instanceof TableRowSorter<?>)) {
            return;
        }
        @SuppressWarnings("unchecked")
        TableRowSorter<DefaultTableModel> sorter =
                (TableRowSorter<DefaultTableModel>) agentTable.getRowSorter();
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        String availability = String.valueOf(availabilityFilter.getSelectedItem());
        if ("Available".equals(availability)) {
            filters.add(RowFilter.regexFilter("^Available$", 2));
        } else if ("Busy".equals(availability)) {
            filters.add(RowFilter.regexFilter("^Busy$", 2));
        }
        String search = agentSearch.getText().trim();
        if (!search.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(search)));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void refreshFacilities() {
        clear(facilityModel);
        for (Facilities facility : session.colony().getFacilities()) {
            facilityModel.addRow(new Object[]{humanize(facility.getClass().getSimpleName()),
                    facility.getLocation(), humanize(facility.getOperationalStatus().name()),
                    facility.getCapacity(), resourceSummary(facility.getResources()),
                    facility.getEquipment().size(), facility.getOccupants().size(), facility});
        }
    }

    private void refreshEquipment() {
        clear(equipmentModel);
        for (Facilities facility : session.colony().getFacilities()) {
            for (Equipment equipment : facility.getEquipment()) {
                equipmentModel.addRow(new Object[]{equipment.getName(), humanize(equipment.getCategory().name()),
                        humanize(equipment.getOperationalStatus().name()), facility.getLocation(), equipment});
            }
        }
        for (RoboticUnit robot : session.colony().getRoboticUnits()) {
            for (Equipment equipment : robot.getEquipment()) {
                equipmentModel.addRow(new Object[]{equipment.getName(), humanize(equipment.getCategory().name()),
                        humanize(equipment.getOperationalStatus().name()),
                        humanize(robot.getClass().getSimpleName()), equipment});
            }
        }
    }

    private void refreshEmergencies() {
        clear(emergencyModel);
        for (Emergency emergency : session.emergencies()) {
            String status = humanize(emergency.getStatus().name());
            if (session.colony().getEmergencies().contains(emergency)
                    && emergency.getStatus().name().equals("PENDING")) {
                status = "Pending · Registered";
            } else if (!session.colony().getEmergencies().contains(emergency)) {
                status = "Draft";
            }
            emergencyModel.addRow(new Object[]{humanize(emergency.getType().name()), status,
                    emergency.getLocation(), emergency.getAgents().size(),
                    emergency.getFailureReason(), emergency});
        }
    }

    private void refreshMissions() {
        clear(missionModel);
        for (Mission mission : session.missions()) {
            String state;
            if (mission.getState() != null) {
                state = humanize(mission.getState().name());
            } else if (session.missionDrafts().contains(mission)) {
                state = "Draft";
            } else {
                state = "Unassigned";
            }
            missionModel.addRow(new Object[]{humanize(mission.getClass().getSimpleName()), state,
                    mission.getNeededTimesUnits(), mission.getElapsedTimeUnits(),
                    mission.getAgents().size(), resourceSummary(mission.getRequiredResources()),
                    mission.getFailureReason(), mission});
        }
    }

    private void refreshResources() {
        clear(resourceModel);
        session.colony().getColonyResourceTotal().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> resourceModel.addRow(new Object[]{
                        entry.getKey(), entry.getValue()}));
    }

    private void refreshActivity() {
        clear(activityModel);
        clear(recentActivityModel);
        List<AppSession.ActivityEntry> events = session.activity();
        for (int i = 0; i < events.size(); i++) {
            AppSession.ActivityEntry entry = events.get(i);
            Object[] row = {entry.timestamp(), entry.operation(), entry.relatedObject(),
                    entry.result(), entry.details()};
            activityModel.addRow(row);
            if (i < 10) {
                recentActivityModel.addRow(new Object[]{entry.timestamp(), entry.operation(),
                        entry.result(), entry.details()});
            }
        }
    }

    private void refreshColonyAgents() {
        clear(colonyAgentModel);
        for (Agent agent : session.agents()) {
            String type = humanize(agent.getClass().getSimpleName());
            String name = agent instanceof Personnel person
                    ? person.getName() + " (#" + person.getId() + ")" : type;
            colonyAgentModel.addRow(new Object[]{type, name,
                    agent.isAvailable() ? "Available" : "Busy", agent.getCurrentLocation()});
        }
    }

    private void refreshMetrics() {
        List<Agent> agents = session.agents();
        List<Mission> missions = session.missions();
        Mission activeMission = session.colony().getActiveMission();
        long openEmergencies = session.emergencies().stream()
                .filter(emergency -> emergency.getStatus() != EmergencyStatus.RESOLVED)
                .count();
        metricValues.get("Crew & robots").setText(String.valueOf(agents.size()));
        metricValues.get("Available").setText(String.valueOf(
                agents.stream().filter(Agent::isAvailable).count()));
        metricValues.get("Facilities").setText(String.valueOf(session.colony().getFacilities().size()));
        metricValues.get("Emergencies").setText(String.valueOf(openEmergencies));
        metricValues.get("Active mission").setText(activeMission == null ? "No" : "Yes");
        metricValues.get("Completed missions").setText(String.valueOf(missions.stream()
                .filter(mission -> mission.getState() == Mission_Emergency_States.COMPLETED).count()));
    }

    private void refreshTeamList() {
        List<Agent> selected = teamList.getSelectedValuesList();
        teamListModel.clear();
        for (Agent agent : session.agents()) {
            teamListModel.addElement(agent);
        }
        for (int i = 0; i < teamListModel.size(); i++) {
            if (selected.contains(teamListModel.get(i))) {
                teamList.addSelectionInterval(i, i);
            }
        }
    }

    private void createPersonnel() {
        JComboBox<String> role = new JComboBox<>(new String[]{"Engineer", "Scientist", "Medical officer"});
        JTextField name = new JTextField("New crew member", 18);
        JSpinner id = new JSpinner(new SpinnerNumberModel(2001, 1, Integer.MAX_VALUE, 1));
        JComboBox<String> health = stringChoices(personnelHealthStatuses(), "Healthy");
        JComboBox<String> location = stringChoices(existingLocations(), "Habitat-01");
        EnumSelectionPanel<Skills> skills = new EnumSelectionPanel<>(Skills.class);
        skills.setSelection(EnumSet.of(Skills.MECHANICAL));
        JSpinner fatigue = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
        role.addActionListener(event -> skills.setSelection(switch (
                String.valueOf(role.getSelectedItem())) {
            case "Scientist" -> EnumSet.of(Skills.SCIENTIFIC);
            case "Medical officer" -> EnumSet.of(Skills.MEDICAL);
            default -> EnumSet.of(Skills.MECHANICAL);
        }));
        JPanel form = form("Job role", role, "Name", name, "Crew ID", id,
                "Health", health, "Current base", location,
                "Skills", skills, "Fatigue level (0–100)", fatigue);
        if (JOptionPane.showConfirmDialog(this,
                boundedFormContent(form, 620, 520), "Add a crew member",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            String selectedRole = String.valueOf(role.getSelectedItem());
            Personnel person;
            Set<Skills> selectedSkills = skills.selectedValues();
            String selectedHealth = String.valueOf(health.getSelectedItem());
            String selectedLocation = String.valueOf(location.getSelectedItem());
            int selectedId = (Integer) id.getValue();
            double fatigueValue = ((Number) fatigue.getValue()).doubleValue();
            if ("Scientist".equals(selectedRole)) {
                person = new Scientist(name.getText(), selectedId,
                        selectedHealth, selectedLocation, selectedSkills,
                        fatigueValue, true);
            } else if ("Medical officer".equals(selectedRole)) {
                person = new MedicalOfficer(name.getText(), selectedId,
                        selectedHealth, selectedLocation, selectedSkills,
                        fatigueValue, true);
            } else {
                person = new Engineer(name.getText(), selectedId,
                        selectedHealth, selectedLocation, selectedSkills,
                        fatigueValue, true);
            }
            session.colony().addPersonnel(person);
            record("Create personnel", person.getName(), "CREATED",
                    humanize(person.getClass().getSimpleName()) + " #" + person.getId());
        } catch (RuntimeException failure) {
            reportFailure("Create personnel", name.getText(), failure);
        }
    }

    private void createRobot() {
        JComboBox<String> type = new JComboBox<>(new String[]{
                "Exploration rover", "Repair robot", "Drone"});
        JSpinner battery = new JSpinner(new SpinnerNumberModel(100.0, 0.0, 100.0, 1.0));
        JComboBox<String> health = stringChoices(robotHealthStatuses(), "Operational");
        JComboBox<String> location = stringChoices(existingLocations(), "Habitat-01");
        EnumSelectionPanel<Capability> capabilities = new EnumSelectionPanel<>(Capability.class);
        capabilities.setSelection(EnumSet.of(Capability.SURFACE_TRAVEL,
                Capability.EQUIPMENT_CARRYING, Capability.EXPLORATION));
        JLabel capabilityHint = new JLabel("Choose capabilities for the rover.");
        capabilityHint.setForeground(MUTED);
        type.addActionListener(event -> {
            String selectedType = String.valueOf(type.getSelectedItem());
            boolean customCapabilities = "Exploration rover".equals(selectedType);
            capabilities.setEnabled(customCapabilities);
            capabilityHint.setText(customCapabilities
                    ? "Choose what this rover can do."
                    : "This robot has preset abilities: " + humanize(fixedRobotCapabilities(selectedType).toString()));
        });
        JPanel form = form("Robot type", type, "Battery level (0–100)", battery,
                "Health", health, "Current base", location,
                "Abilities", capabilities, "", capabilityHint);
        if (JOptionPane.showConfirmDialog(this, boundedFormContent(form, 620, 440), "Add a robot",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            String selectedType = String.valueOf(type.getSelectedItem());
            double batteryLevel = ((Number) battery.getValue()).doubleValue();
            RoboticUnit robot;
            if ("Exploration rover".equals(selectedType)) {
                robot = new ExplorationRover(batteryLevel,
                        String.valueOf(health.getSelectedItem()),
                        String.valueOf(location.getSelectedItem()),
                        true, capabilities.selectedValues());
            } else if ("Repair robot".equals(selectedType)) {
                robot = new RepairRobot(batteryLevel,
                        String.valueOf(health.getSelectedItem()),
                        String.valueOf(location.getSelectedItem()), true);
            } else {
                robot = new Drone(batteryLevel,
                        String.valueOf(health.getSelectedItem()),
                        String.valueOf(location.getSelectedItem()), true);
            }
            session.colony().addRobot(robot);
            record("Add robot", humanize(robot.getClass().getSimpleName()), "CREATED",
                    "Battery " + robot.getBatteryLevel() + "; abilities "
                            + formatEnums(robot.getCapabilities()));
        } catch (RuntimeException failure) {
            reportFailure("Create robotic unit", String.valueOf(type.getSelectedItem()), failure);
        }
    }

    private void toggleAgentAvailability() {
        Agent agent = selectedObject(agentTable, agentModel, Agent.class);
        if (agent == null) {
            showInfo("Select an agent first.");
            return;
        }
        try {
            boolean requested = !agent.isAvailable();
            agent.setAvailability(requested);
            record("Change agent availability", agentLabel(agent),
                    requested ? "AVAILABLE" : "BUSY", "Set through Agent.setAvailability");
        } catch (RuntimeException failure) {
            reportFailure("Change agent availability", agentLabel(agent), failure);
        }
    }

    private void setAgentLocation() {
        Agent agent = selectedObject(agentTable, agentModel, Agent.class);
        if (agent == null) {
            showInfo("Select an agent first.");
            return;
        }
        String location = JOptionPane.showInputDialog(this, "New location",
                agent.getCurrentLocation());
        if (location == null) {
            return;
        }
        try {
            agent.setCurrentLocation(location);
            record("Set agent location", agentLabel(agent), "UPDATED", location);
        } catch (RuntimeException failure) {
            reportFailure("Set agent location", agentLabel(agent), failure);
        }
    }

    private void showAgentDetails() {
        Agent agent = selectedObject(agentTable, agentModel, Agent.class);
        if (agent == null) {
            showInfo("Select an agent first.");
            return;
        }
        String details = "Type: " + humanize(agent.getClass().getSimpleName())
                + "\nAvailable: " + agent.isAvailable()
                + "\nLocation: " + agent.getCurrentLocation()
                + "\nHandles emergency types: ";
        List<String> supportedEmergencies = new ArrayList<>();
        for (EmergencyType type : EmergencyType.values()) {
            if (agent.canHandleEmergency(type)) {
                supportedEmergencies.add(humanize(type.name()));
            }
        }
        details += supportedEmergencies;
        if (agent instanceof Personnel person) {
            details += "\nID: " + person.getId()
                    + "\nHealth: " + person.getHealthStatus()
                    + "\nSkills: " + formatEnums(person.getSkills())
                    + "\nFatigue: " + person.getFatigueLevel()
                    + "\nMission: " + (person.getMission() == null ? "None"
                            : humanize(person.getMission().getClass().getSimpleName()));
        } else if (agent instanceof RoboticUnit robot) {
            details += "\nHealth: " + robot.getHealthStatus()
                    + "\nBattery: " + robot.getBatteryLevel()
                    + "\nAbilities: " + formatEnums(robot.getCapabilities())
                    + "\nEquipment: " + equipmentNames(robot.getEquipment())
                    + "\nMission: " + (robot.getMission() == null ? "None"
                            : humanize(robot.getMission().getClass().getSimpleName()));
        }
        showText("Agent details", details);
    }

    private void createFacility() {
        JComboBox<FacilityType> type = new JComboBox<>(FacilityType.values());
        humanizeEnumChoices(type);
        JTextField location = new JTextField("New-Location", 18);
        JSpinner capacity = new JSpinner(new SpinnerNumberModel(20.0, 0.1, 100000.0, 1.0));
        JComboBox<OperationalStatus> status = new JComboBox<>(OperationalStatus.values());
        humanizeEnumChoices(status);
        Map<Resources, Double> resourceValues = new LinkedHashMap<>(
                Map.of(Resources.OXYGEN, 20.0, Resources.WATER, 10.0));
        JLabel resourceSummaryLabel = new JLabel(resourceSummary(resourceValues));
        JButton chooseResources = button("Choose resources", false, event -> {
            try {
                Map<Resources, Double> selected = editResourceMap(resourceValues,
                        "Initial facility resources");
                if (selected != null) {
                    resourceValues.clear();
                    resourceValues.putAll(selected);
                    resourceSummaryLabel.setText(resourceSummary(resourceValues));
                }
            } catch (RuntimeException failure) {
                reportFailure("Choose facility resources", "Facility draft", failure);
            }
        });
        JPanel resources = new JPanel(new BorderLayout(8, 0));
        resources.setOpaque(false);
        resources.add(chooseResources, BorderLayout.WEST);
        resources.add(resourceSummaryLabel, BorderLayout.CENTER);
        JSpinner powerOutput = new JSpinner(new SpinnerNumberModel(5.0, 0.0, 100000.0, 1.0));
        JPanel form = form("Facility type", type, "Location name", location,
                "Capacity", capacity, "Status", status,
                "Starting supplies", resources,
                "Power output (for power stations)", powerOutput);
        if (JOptionPane.showConfirmDialog(this, boundedFormContent(form, 620, 470), "Add a facility",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            FacilityType selected = (FacilityType) type.getSelectedItem();
            String loc = location.getText();
            double cap = ((Number) capacity.getValue()).doubleValue();
            OperationalStatus operationalStatus = (OperationalStatus) status.getSelectedItem();
            Map<Resources, Double> resourceMap = new LinkedHashMap<>(resourceValues);
            Facilities facility = switch (selected) {
                case HABITAT -> new Habitat(loc, cap, operationalStatus,
                        resourceMap, List.of(), List.of());
                case LABORATORY -> new Laboratory(loc, cap, operationalStatus,
                        resourceMap, List.of(), List.of());
                case STORAGE -> new StorageFacility(loc, cap, operationalStatus,
                        resourceMap, List.of(), List.of());
                case POWER_STATION -> new PowerStation(loc, cap, operationalStatus,
                        resourceMap, List.of(), List.of(),
                        ((Number) powerOutput.getValue()).doubleValue());
                case COMMUNICATION_STATION -> new CommunicationStation(loc, cap,
                        operationalStatus, resourceMap, List.of(), List.of());
            };
            session.colony().addFacility(facility);
            record("Add facility", loc, "CREATED", humanize(facility.getClass().getSimpleName()));
        } catch (RuntimeException failure) {
            reportFailure("Add facility", location.getText(), failure);
        }
    }

    private void changeFacilityStatus() {
        Facilities facility = selectedObject(facilityTable, facilityModel, Facilities.class);
        if (facility == null) {
            showInfo("Select a facility first.");
            return;
        }
        OperationalStatus status = chooseEnum("Facility status", "Choose the new status",
                OperationalStatus.values(), facility.getOperationalStatus());
        if (status == null) {
            return;
        }
        try {
            facility.setOperationalStatus(status);
            record("Change facility status", facility.getLocation(), "UPDATED", status.name());
        } catch (RuntimeException failure) {
            reportFailure("Change facility status", facility.getLocation(), failure);
        }
    }

    private void adjustFacilityResource() {
        Facilities facility = selectedObject(facilityTable, facilityModel, Facilities.class);
        if (facility == null) {
            showInfo("Select a facility first.");
            return;
        }
        JComboBox<Resources> resource = new JComboBox<>(Resources.values());
        humanizeEnumChoices(resource);
        JComboBox<String> operation = new JComboBox<>(new String[]{"Add supplies", "Remove supplies"});
        JSpinner amount = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 100000.0, 1.0));
        JPanel form = form("Supply type", resource, "What should happen?", operation,
                "Amount", amount);
        if (JOptionPane.showConfirmDialog(this, form, "Adjust facility supplies",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        Resources selectedResource = (Resources) resource.getSelectedItem();
        String selectedOperation = String.valueOf(operation.getSelectedItem());
        double amountValue = ((Number) amount.getValue()).doubleValue();
        try {
            if ("Add supplies".equals(selectedOperation)) {
                facility.addResource(selectedResource, amountValue);
                record("Add facility resource", facility.getLocation(), "UPDATED",
                        humanize(selectedResource.name()) + " +" + amountValue);
            } else {
                double deducted = facility.deductResource(selectedResource, amountValue);
                record("Deduct facility resource", facility.getLocation(), "UPDATED",
                        humanize(selectedResource.name()) + " requested " + amountValue
                                + "; actual amount removed " + deducted);
                showFeedback("Supplies updated", "Removed " + deducted + " units of "
                                + humanize(selectedResource.name()) + " from " + facility.getLocation() + ".",
                        "Requested: " + amountValue + "\nActually removed: " + deducted,
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (RuntimeException failure) {
            reportFailure("Adjust facility resource", facility.getLocation(), failure);
        }
    }

    private Map<Resources, Double> editResourceMap(
            Map<Resources, Double> initialValues, String title) {
        DefaultTableModel resources = new DefaultTableModel(
                new Object[]{"Supply", "Amount (leave blank if not needed)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        for (Resources resource : Resources.values()) {
            Double amount = initialValues.get(resource);
            resources.addRow(new Object[]{resource, amount == null ? "" : amount});
        }
        JTable editor = table(resources, false);
        editor.getColumnModel().getColumn(0).setPreferredWidth(170);
        editor.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int column) {
                Component component = super.getTableCellRendererComponent(
                        source, value instanceof Resources resource
                                ? humanize(resource.name()) : value,
                        selected, focused, row, column);
                if (!selected) {
                    component.setBackground(CARD);
                    component.setForeground(TEXT);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return component;
            }
        });
        editor.getColumnModel().getColumn(1).setPreferredWidth(190);
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Choose each supply type and enter the amount needed."),
                BorderLayout.NORTH);
        panel.add(new JScrollPane(editor), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(420, 300));
        int result = JOptionPane.showConfirmDialog(this, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        if (editor.isEditing() && !editor.getCellEditor().stopCellEditing()) {
            throw new IllegalArgumentException("Finish editing the resource amount first");
        }
        Map<Resources, Double> selected = new LinkedHashMap<>();
        for (int row = 0; row < resources.getRowCount(); row++) {
            Object value = resources.getValueAt(row, 1);
            if (value == null || String.valueOf(value).isBlank()) {
                continue;
            }
            selected.put((Resources) resources.getValueAt(row, 0),
                    Double.parseDouble(String.valueOf(value).trim()));
        }
        return selected;
    }

    private void manageEquipment() {
        Facilities facility = selectedObject(facilityTable, facilityModel, Facilities.class);
        if (facility == null) {
            showInfo("Select a facility first.");
            return;
        }
        String[] choices = {"Add equipment", "Change equipment status", "Remove equipment", "Cancel"};
        String choice = (String) JOptionPane.showInputDialog(this,
                "Choose an equipment operation", "Equipment manager",
                JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
        if (choice == null || "Cancel".equals(choice)) {
            return;
        }
        try {
            List<Equipment> equipment = new ArrayList<>(facility.getEquipment());
            if ("Add equipment".equals(choice)) {
                addEquipmentToFacility(facility);
                return;
            }
            Equipment selected = chooseEquipment("Select equipment", equipment);
            if (selected == null) {
                return;
            }
            if ("Change equipment status".equals(choice)) {
                OperationalStatus status = chooseEnum("Equipment status", "Choose the new status",
                        OperationalStatus.values(), selected.getOperationalStatus());
                if (status == null) {
                    return;
                }
                selected.setOperationalStatus(status);
                record("Change equipment status", selected.getName(), "UPDATED", status.name());
            } else {
                if (JOptionPane.showConfirmDialog(this,
                        "Remove " + selected.getName() + " from " + facility.getLocation() + "?",
                        "Confirm removal", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
                equipment.remove(selected);
                facility.setEquipment(equipment);
                record("Remove equipment", selected.getName(), "REMOVED", facility.getLocation());
            }
        } catch (RuntimeException failure) {
            reportFailure("Manage equipment", facility.getLocation(), failure);
        }
    }

    private void addEquipmentToFacility(Facilities facility) {
        if (facility == null) {
            showInfo("Select a facility first, then choose Add equipment.");
            return;
        }
        JTextField name = new JTextField(16);
        JComboBox<Equipment.Category> category = new JComboBox<>(Equipment.Category.values());
        humanizeEnumChoices(category);
        JComboBox<OperationalStatus> status = new JComboBox<>(OperationalStatus.values());
        humanizeEnumChoices(status);
        JPanel form = form("Name", name, "Category", category, "Status", status);
        if (JOptionPane.showConfirmDialog(this, form, "Add equipment to " + facility.getLocation(),
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            List<Equipment> equipment = new ArrayList<>(facility.getEquipment());
            Equipment added = new Equipment(name.getText(),
                    (OperationalStatus) status.getSelectedItem(),
                    (Equipment.Category) category.getSelectedItem());
            equipment.add(added);
            facility.setEquipment(equipment);
            record("Add equipment", added.getName(), "CREATED", facility.getLocation());
        } catch (RuntimeException failure) {
            reportFailure("Add equipment", facility.getLocation(), failure);
        }
    }

    private void manageRobotEquipment() {
        List<RoboticUnit> robots = session.colony().getRoboticUnits();
        RoboticUnit robot = chooseObject("Select a robotic unit", robots,
                value -> value.getClass().getSimpleName() + " | " + value.getCurrentLocation());
        if (robot == null) {
            return;
        }
        String[] choices = {"Add equipment", "Change equipment status", "Remove equipment", "Cancel"};
        String choice = (String) JOptionPane.showInputDialog(this,
                "Choose an equipment operation", "Robot equipment",
                JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
        if (choice == null || "Cancel".equals(choice)) {
            return;
        }
        List<Equipment> equipment = new ArrayList<>(robot.getEquipment());
        try {
            if ("Add equipment".equals(choice)) {
                JTextField name = new JTextField(16);
                JComboBox<Equipment.Category> category = new JComboBox<>(Equipment.Category.values());
                humanizeEnumChoices(category);
                JComboBox<OperationalStatus> status = new JComboBox<>(OperationalStatus.values());
                humanizeEnumChoices(status);
                JPanel form = form("Name", name, "Category", category, "Status", status);
                if (JOptionPane.showConfirmDialog(this, form, "Add robot equipment",
                        JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                    return;
                }
                equipment.add(new Equipment(name.getText(),
                        (OperationalStatus) status.getSelectedItem(),
                        (Equipment.Category) category.getSelectedItem()));
                robot.setEquipment(equipment);
                record("Add robot equipment", name.getText(), "CREATED",
                        robot.getClass().getSimpleName());
                return;
            }
            Equipment selected = chooseEquipment("Select robot equipment", equipment);
            if (selected == null) {
                return;
            }
            if ("Change equipment status".equals(choice)) {
                OperationalStatus status = chooseEnum("Equipment status", "Choose the new status",
                        OperationalStatus.values(), selected.getOperationalStatus());
                if (status == null) {
                    return;
                }
                selected.setOperationalStatus(status);
                record("Change robot equipment status", selected.getName(), "UPDATED", status.name());
            } else {
                if (JOptionPane.showConfirmDialog(this,
                        "Remove " + selected.getName() + " from this robot?",
                        "Confirm removal", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
                equipment.remove(selected);
                robot.setEquipment(equipment);
                record("Remove robot equipment", selected.getName(), "REMOVED",
                        robot.getClass().getSimpleName());
            }
        } catch (RuntimeException failure) {
            reportFailure("Manage robot equipment", robot.getClass().getSimpleName(), failure);
        }
    }

    private Equipment chooseEquipment(String title, List<Equipment> equipment) {
        if (equipment.isEmpty()) {
            showInfo("This facility has no equipment.");
            return null;
        }
        List<Choice<Equipment>> choices = equipment.stream()
                .map(item -> new Choice<>(item.getName() + " | " + item.getCategory()
                        + " | " + item.getOperationalStatus(), item)).toList();
        @SuppressWarnings("unchecked")
        Choice<Equipment> selected = (Choice<Equipment>) JOptionPane.showInputDialog(
                this, title, title, JOptionPane.PLAIN_MESSAGE, null,
                choices.toArray(), choices.get(0));
        return selected == null ? null : selected.value();
    }

    private void manageOccupants() {
        Facilities facility = selectedObject(facilityTable, facilityModel, Facilities.class);
        if (facility == null) {
            showInfo("Select a facility first.");
            return;
        }
        String[] choices = {"Add occupant", "Remove occupant", "Cancel"};
        String choice = (String) JOptionPane.showInputDialog(this,
                "Choose an occupant operation", "Occupant manager",
                JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
        if (choice == null || "Cancel".equals(choice)) {
            return;
        }
        List<Agent> occupants = new ArrayList<>(facility.getOccupants());
        if ("Add occupant".equals(choice)) {
            List<Agent> candidates = session.agents().stream()
                    .filter(agent -> !occupants.contains(agent)).toList();
            Agent selected = chooseAgent("Select an agent to add", candidates);
            if (selected == null) {
                return;
            }
            occupants.add(selected);
            try {
                facility.setOccupants(occupants);
                record("Add facility occupant", agentLabel(selected), "UPDATED", facility.getLocation());
            } catch (RuntimeException failure) {
                reportFailure("Add facility occupant", facility.getLocation(), failure);
            }
        } else {
            Agent selected = chooseAgent("Select an occupant to remove", occupants);
            if (selected == null) {
                return;
            }
            if (JOptionPane.showConfirmDialog(this,
                    "Remove " + agentLabel(selected) + " from " + facility.getLocation() + "?",
                    "Confirm removal", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            occupants.remove(selected);
            try {
                facility.setOccupants(occupants);
                record("Remove facility occupant", agentLabel(selected), "UPDATED", facility.getLocation());
            } catch (RuntimeException failure) {
                reportFailure("Remove facility occupant", facility.getLocation(), failure);
            }
        }
    }

    private Agent chooseAgent(String title, List<Agent> candidates) {
        if (candidates.isEmpty()) {
            showInfo("There are no matching agents to select.");
            return null;
        }
        List<Choice<Agent>> choices = candidates.stream()
                .map(agent -> new Choice<>(agentLabel(agent), agent)).toList();
        @SuppressWarnings("unchecked")
        Choice<Agent> selected = (Choice<Agent>) JOptionPane.showInputDialog(
                this, title, title, JOptionPane.PLAIN_MESSAGE, null,
                choices.toArray(), choices.get(0));
        return selected == null ? null : selected.value();
    }

    private void createEmergency() {
        EmergencyType type = chooseEnum("Record an emergency", "Choose what happened",
                EmergencyType.values(), EmergencyType.OXYGEN_LEAK);
        if (type == null) {
            return;
        }
        try {
            Emergency emergency;
            switch (type) {
                case OXYGEN_LEAK -> {
                    List<Habitat> habitats = session.colony().getFacilities().stream()
                            .filter(Habitat.class::isInstance).map(Habitat.class::cast).toList();
                    Habitat habitat = chooseObject("Affected habitat", habitats,
                            value -> value.getLocation());
                    if (habitat == null) return;
                    JSpinner lossAmount = new JSpinner(
                            new SpinnerNumberModel(1.0, 0.1, 100000.0, 1.0));
                    JPanel form = form("Oxygen loss amount", lossAmount);
                    if (JOptionPane.showConfirmDialog(this, form, "Record oxygen leak",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                        return;
                    }
                    emergency = new OxygenLeak(habitat,
                            ((Number) lossAmount.getValue()).doubleValue());
                }
                case FIRE -> {
                    List<Laboratory> labs = session.colony().getFacilities().stream()
                            .filter(Laboratory.class::isInstance).map(Laboratory.class::cast).toList();
                    Laboratory lab = chooseObject("Affected laboratory", labs,
                            value -> value.getLocation());
                    if (lab == null) return;
                    emergency = new Fire(lab);
                }
                case ROVER_BREAKDOWN -> {
                    List<ExplorationRover> rovers = session.colony().getRoboticUnits().stream()
                            .filter(ExplorationRover.class::isInstance)
                            .map(ExplorationRover.class::cast).toList();
                    ExplorationRover rover = chooseObject("Affected rover", rovers,
                            value -> value.getCurrentLocation() + " | battery " + value.getBatteryLevel());
                    if (rover == null) return;
                    emergency = new RoverBreakdown(rover);
                }
                case MEDICAL -> {
                    List<Personnel> people = session.colony().getPersonnel();
                    Personnel patient = chooseObject("Patient", people,
                            value -> value.getName() + " (#" + value.getId() + ") | " + value.getCurrentLocation());
                    if (patient == null) return;
                    emergency = new medicalEmergency(patient);
                }
                default -> throw new IllegalArgumentException("Unsupported emergency type: " + type);
            }
            session.addEmergencyDraft(emergency);
            record("Create emergency", humanize(type.name()), "CREATED / DRAFT",
                    "Select this emergency and choose Attempt response when ready.");
        } catch (RuntimeException failure) {
            reportFailure("Create emergency", type.name(), failure);
        }
    }

    private <T> T chooseObject(String prompt, List<T> values,
            java.util.function.Function<T, String> label) {
        if (values.isEmpty()) {
            showInfo("No matching project objects are registered in the colony.");
            return null;
        }
        List<Choice<T>> choices = values.stream()
                .map(value -> new Choice<>(label.apply(value), value)).toList();
        @SuppressWarnings("unchecked")
        Choice<T> selected = (Choice<T>) JOptionPane.showInputDialog(
                this, prompt, prompt, JOptionPane.PLAIN_MESSAGE, null,
                choices.toArray(), choices.get(0));
        return selected == null ? null : selected.value();
    }

    private void handleSelectedEmergency() {
        Emergency emergency = selectedObject(emergencyTable, emergencyModel, Emergency.class);
        if (emergency == null) {
            showInfo("Select an emergency first.");
            return;
        }
        if (session.colony().getEmergencies().contains(emergency)) {
            showInfo("This emergency has already been submitted to the colony.");
            return;
        }
        try {
            Map<Resources, Double> resourcesBefore = session.colony().getColonyResourceTotal();
            boolean handled = session.colony().handleEmergency(emergency);
            Map<Resources, Double> resourcesAfter = session.colony().getColonyResourceTotal();
            session.removeEmergencyDraft(emergency);
            String result = handled ? "RESOLVED" : "UNRESOLVED";
            String details = (handled ? "Emergency response returned true" : emergency.getFailureReason())
                    + "; resource totals: " + resourceChanges(resourcesBefore, resourcesAfter);
            record("Handle emergency", humanize(emergency.getType().name()),
                    handled ? "RESOLVED" : "UNRESOLVED", details);
            showFeedback(handled ? "Emergency resolved" : "Emergency still needs attention",
                    handled ? "The response succeeded and responders have been released."
                            : "The colony could not resolve this emergency with the available resources and responders.",
                    "Current status: " + emergency.getStatus() + "\n" + details,
                    handled ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException failure) {
            reportFailure("Handle emergency", String.valueOf(emergency.getType()), failure);
        }
    }

    private void showEmergencyDetails() {
        Emergency emergency = selectedObject(emergencyTable, emergencyModel, Emergency.class);
        if (emergency == null) {
            showInfo("Select an emergency first.");
            return;
        }
        showText("Emergency details", "Type: " + humanize(emergency.getType().name())
                + "\nStatus: " + humanize(emergency.getStatus().name())
                + "\nLocation: " + emergency.getLocation()
                + "\nResponders: " + emergency.getAgents().stream()
                        .map(AdminDashboard::agentLabel).toList()
                + "\nRequired supplies: " + resourceSummary(emergency.getRequiredResources())
                + "\nFailure reason: " + emergency.getFailureReason());
    }

    private void createMission() {
        JComboBox<String> type = new JComboBox<>(
                new String[]{"Exploration", "Repair", "Research", "Rescue"});
        JTextField duration = new JTextField("2", 8);
        JComboBox<Choice<Facilities>> facility = new JComboBox<>();
        populateMissionFacilities(facility, String.valueOf(type.getSelectedItem()));

        Map<Resources, Double> resourceValues = new LinkedHashMap<>();
        JLabel resourceSummaryLabel = new JLabel("No extra supplies");
        JButton chooseResources = button("Choose resources", false, event -> {
            try {
                Map<Resources, Double> selected = editResourceMap(resourceValues,
                        "Mission resource requirements");
                if (selected != null) {
                    resourceValues.clear();
                    resourceValues.putAll(selected);
                    resourceSummaryLabel.setText(resourceValues.isEmpty()
                            ? "No extra supplies" : resourceSummary(resourceValues));
                }
            } catch (RuntimeException failure) {
                reportFailure("Choose mission resources", String.valueOf(type.getSelectedItem()), failure);
            }
        });
        JPanel resourceControls = new JPanel(new BorderLayout(8, 0));
        resourceControls.setOpaque(false);
        resourceControls.add(chooseResources, BorderLayout.WEST);
        resourceControls.add(resourceSummaryLabel, BorderLayout.CENTER);

        EnumSelectionPanel<Capability> capabilities =
                new EnumSelectionPanel<>(Capability.class);
        JTextField explorationRegion = new JTextField("North Ridge", 20);
        JTextField researchTopic = new JTextField("Soil composition", 20);
        JComboBox<Choice<Object>> repairTarget = new JComboBox<>();
        populateRepairTargets(repairTarget);
        JLabel repairTargetHint = new JLabel();
        repairTargetHint.setForeground(MUTED);
        repairTargetHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JPanel repairTargetControls = new JPanel(new BorderLayout(0, 4));
        repairTargetControls.setOpaque(false);
        repairTargetControls.add(repairTarget, BorderLayout.NORTH);
        repairTargetControls.add(repairTargetHint, BorderLayout.CENTER);
        Runnable updateRepairTargetHint = () -> repairTargetHint.setText(
                repairTarget.getItemCount() > 1
                        ? "Choose the damaged item to restore after a successful mission."
                        : "No damaged target exists. Mark equipment or a facility as Damaged first.");
        repairTarget.addActionListener(event -> updateRepairTargetHint.run());
        updateRepairTargetHint.run();

        CardLayout specificLayout = new CardLayout();
        JPanel specificFields = new JPanel(specificLayout);
        specificFields.add(form("Exploration region", explorationRegion), "Exploration");
        specificFields.add(form("Research topic", researchTopic), "Research");
        specificFields.add(form("Damaged item", repairTargetControls), "Repair");
        JLabel rescueNote = new JLabel("Rescue missions need a suitable vehicle and communication-capable crew.");
        rescueNote.setForeground(MUTED);
        specificFields.add(rescueNote, "Rescue");

        JPanel basics = new JPanel(new BorderLayout(0, 8));
        basics.setOpaque(false);
        JLabel basicsText = new JLabel("Step 1 · Choose the mission and how long it should take.");
        basicsText.setForeground(MUTED);
        basics.add(basicsText, BorderLayout.NORTH);
        basics.add(form("Mission type", type, "Duration (time units)", duration), BorderLayout.CENTER);

        JPanel requirements = new JPanel(new BorderLayout(0, 8));
        requirements.setOpaque(false);
        JLabel requirementsText = new JLabel("Step 2 · Choose a facility and any extra requirements.");
        requirementsText.setForeground(MUTED);
        requirements.add(requirementsText, BorderLayout.NORTH);
        requirements.add(form("Facility", facility, "Extra supplies (optional)", resourceControls,
                "Additional abilities (optional)", capabilities,
                "Mission details", specificFields), BorderLayout.CENTER);

        DefaultListModel<Agent> agents = new DefaultListModel<>();
        for (Agent agent : session.agents()) {
            agents.addElement(agent);
        }
        JList<Agent> team = new JList<>(agents);
        configureAgentList(team);
        team.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane teamScroll = new JScrollPane(team);
        teamScroll.setPreferredSize(new Dimension(520, 190));
        JTextArea review = new JTextArea(6, 48);
        review.setEditable(false);
        review.setLineWrap(true);
        review.setWrapStyleWord(true);
        review.setBackground(CONTROL);
        review.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel message = new JLabel(" ");
        message.setForeground(RED);

        JPanel finalStep = new JPanel(new BorderLayout(0, 10));
        finalStep.setOpaque(false);
        JLabel finalStepText = new JLabel(missionTeamHint(
                String.valueOf(type.getSelectedItem())));
        finalStepText.setForeground(MUTED);
        JPanel teamHeading = new JPanel(new BorderLayout(8, 4));
        teamHeading.setOpaque(false);
        teamHeading.add(finalStepText, BorderLayout.CENTER);
        JButton suggestTeam = button("Suggest a valid team", false, event -> {
            try {
                Mission draft = buildMission(type, duration, facility, resourceValues,
                        capabilities, explorationRegion, researchTopic, repairTarget);
                List<Agent> suggestion = findValidMissionTeam(draft);
                if (suggestion.isEmpty()) {
                    message.setText("No available valid team was found. Add or free a suitable person or robot.");
                    return;
                }
                int[] indices = suggestion.stream().mapToInt(agents::indexOf)
                        .filter(index -> index >= 0).toArray();
                team.setSelectedIndices(indices);
                message.setText("Selected " + suggestion.size()
                        + " available team member(s). Colony checks supplies and conditions at submission.");
            } catch (RuntimeException failure) {
                reportFailure("Suggest mission team", String.valueOf(type.getSelectedItem()), failure);
            }
        });
        teamHeading.add(suggestTeam, BorderLayout.EAST);
        finalStep.add(teamHeading, BorderLayout.NORTH);
        finalStep.add(teamScroll, BorderLayout.CENTER);
        finalStep.add(new JScrollPane(review), BorderLayout.SOUTH);

        CardLayout stepLayout = new CardLayout();
        JPanel steps = new JPanel(stepLayout);
        steps.add(boundedFormContent(basics, 620, 260), "0");
        steps.add(boundedFormContent(requirements, 620, 430), "1");
        steps.add(boundedFormContent(finalStep, 620, 430), "2");

        JLabel progress = new JLabel();
        progress.setForeground(TEXT);
        progress.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        header.add(progress, BorderLayout.NORTH);
        header.add(message, BorderLayout.SOUTH);

        JButton back = button("Back", false, event -> { });
        JButton next = button("Continue", true, event -> { });
        JButton saveDraft = button("Save draft", false, event -> { });
        JButton assign = button("Create and assign team", true, event -> { });
        JButton cancel = button("Cancel", false, event -> { });
        JDialog dialog = new JDialog(this, "Create a mission", true);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.add(back);
        footer.add(next);
        footer.add(saveDraft);
        footer.add(assign);
        footer.add(cancel);
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(new EmptyBorder(16, 18, 14, 18));
        content.add(header, BorderLayout.NORTH);
        content.add(steps, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        int[] currentStep = {0};
        Runnable updateStep = () -> {
            String[] titles = {"Step 1 of 3 · Mission", "Step 2 of 3 · Requirements",
                    "Step 3 of 3 · Team and review"};
            progress.setText(titles[currentStep[0]]);
            stepLayout.show(steps, String.valueOf(currentStep[0]));
            back.setEnabled(currentStep[0] > 0);
            next.setVisible(currentStep[0] < 2);
            saveDraft.setVisible(currentStep[0] == 2);
            assign.setVisible(currentStep[0] == 2);
            review.setText(buildMissionReview(type, duration, facility, resourceValues,
                    capabilities, team, explorationRegion, researchTopic, repairTarget));
            dialog.pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setSize(Math.min(dialog.getWidth(), screen.width - 40),
                    Math.min(dialog.getHeight(), screen.height - 80));
            dialog.setLocationRelativeTo(this);
        };
        back.addActionListener(event -> {
            if (currentStep[0] > 0) {
                currentStep[0]--;
                message.setText(" ");
                updateStep.run();
            }
        });
        next.addActionListener(event -> {
            if (currentStep[0] == 0) {
                try {
                    if (Integer.parseInt(duration.getText().trim()) <= 0) {
                        message.setText("Duration must be greater than zero.");
                        return;
                    }
                } catch (NumberFormatException invalid) {
                    message.setText("Enter the duration as a whole number.");
                    return;
                }
            } else if (currentStep[0] == 1) {
                String selectedType = String.valueOf(type.getSelectedItem());
                if ("Exploration".equals(selectedType) && explorationRegion.getText().isBlank()) {
                    message.setText("Enter the exploration region to continue.");
                    return;
                }
                if ("Research".equals(selectedType) && researchTopic.getText().isBlank()) {
                    message.setText("Enter the research topic to continue.");
                    return;
                }
                if ("Repair".equals(selectedType)
                        && (repairTarget.getSelectedItem() == null
                        || ((Choice<?>) repairTarget.getSelectedItem()).value() == null)) {
                    message.setText("No damaged facility or equipment is available to repair.");
                    return;
                }
            }
            currentStep[0] = Math.min(2, currentStep[0] + 1);
            message.setText(" ");
            updateStep.run();
        });
        cancel.addActionListener(event -> dialog.dispose());
        saveDraft.addActionListener(event -> {
            try {
                Mission mission = buildMission(type, duration, facility, resourceValues,
                        capabilities, explorationRegion, researchTopic, repairTarget);
                session.addMissionDraft(mission);
                record("Create mission", humanize(mission.getClass().getSimpleName()),
                        "CREATED / DRAFT", "Draft saved; choose a team and submit it when ready.");
                dialog.dispose();
                showFeedback("Mission draft saved", "The draft is saved for you to assign later.",
                        "Draft mission: " + mission.getClass().getSimpleName(),
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (RuntimeException failure) {
                reportFailure("Save mission draft", String.valueOf(type.getSelectedItem()), failure);
            }
        });
        assign.addActionListener(event -> {
            List<Agent> selectedAgents = team.getSelectedValuesList();
            if (selectedAgents.isEmpty()) {
                message.setText("Select at least one person or robot for the team.");
                return;
            }
            try {
                Mission mission = buildMission(type, duration, facility, resourceValues,
                        capabilities, explorationRegion, researchTopic, repairTarget);
                dialog.dispose();
                submitMissionObject(mission, selectedAgents);
            } catch (RuntimeException failure) {
                reportFailure("Create mission", String.valueOf(type.getSelectedItem()), failure);
            }
        });
        team.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                review.setText(buildMissionReview(type, duration, facility, resourceValues,
                        capabilities, team, explorationRegion, researchTopic, repairTarget));
            }
        });
        type.addActionListener(event -> {
            String selectedType = String.valueOf(type.getSelectedItem());
            populateMissionFacilities(facility, selectedType);
            specificLayout.show(specificFields, selectedType);
            finalStepText.setText(missionTeamHint(selectedType));
            team.clearSelection();
            updateRepairTargetHint.run();
            updateStep.run();
        });
        updateStep.run();
        dialog.setVisible(true);
    }

    private String missionTeamHint(String missionType) {
        return switch (missionType) {
            case "Exploration" -> "Suggested: scientist + equipped exploration rover.";
            case "Repair" -> "Suggested: mechanical crew member + repair robot.";
            case "Research" -> "Suggested: scientist + operational laboratory.";
            case "Rescue" -> "Suggested: communication-skilled person + capable vehicle.";
            default -> "Step 3 · Review the details and choose the whole team.";
        };
    }

    private List<Agent> findValidMissionTeam(Mission mission) {
        List<Agent> availableCandidates = session.agents().stream()
                .filter(Agent::isAvailable)
                .filter(agent -> agent.canPerform(mission))
                .toList();

        for (Agent candidate : availableCandidates) {
            List<Agent> singleMemberTeam = List.of(candidate);
            if (mission.hasValidTeamComposition(singleMemberTeam)) {
                return singleMemberTeam;
            }
        }
        for (int first = 0; first < availableCandidates.size(); first++) {
            for (int second = first + 1; second < availableCandidates.size(); second++) {
                List<Agent> pair = List.of(availableCandidates.get(first),
                        availableCandidates.get(second));
                if (mission.hasValidTeamComposition(pair)) {
                    return pair;
                }
            }
        }
        return List.of();
    }

    private Mission buildMission(
            JComboBox<String> type,
            JTextField duration,
            JComboBox<Choice<Facilities>> facility,
            Map<Resources, Double> resourceValues,
            EnumSelectionPanel<Capability> capabilities,
            JTextField explorationRegion,
            JTextField researchTopic,
            JComboBox<Choice<Object>> repairTarget) {
        int durationUnits = Integer.parseInt(duration.getText().trim());
        Choice<Facilities> facilityChoice = facility.getSelectedIndex() < 0 ? null
                : facility.getItemAt(facility.getSelectedIndex());
        Facilities selectedFacility = facilityChoice == null ? null : facilityChoice.value();
        Map<Resources, Double> selectedResources = new LinkedHashMap<>(resourceValues);
        Set<Capability> selectedCapabilities = capabilities.selectedValues();
        return switch (String.valueOf(type.getSelectedItem())) {
            case "Exploration" -> new Exploration(durationUnits, selectedFacility,
                    selectedResources, selectedCapabilities, explorationRegion.getText().trim());
            case "Research" -> new Research(durationUnits, selectedFacility,
                    selectedResources, selectedCapabilities, researchTopic.getText().trim());
            case "Repair" -> {
                Choice<Object> target = repairTarget.getSelectedIndex() < 0 ? null
                        : repairTarget.getItemAt(repairTarget.getSelectedIndex());
                if (target == null || target.value() == null) {
                    throw new IllegalArgumentException("No damaged repair target is available");
                }
                yield new Repair(durationUnits, selectedFacility, selectedResources,
                        selectedCapabilities, target.value());
            }
            case "Rescue" -> new Rescue(durationUnits, selectedFacility,
                    selectedResources, selectedCapabilities);
            default -> throw new IllegalArgumentException("Choose a mission type");
        };
    }

    private String buildMissionReview(
            JComboBox<String> type,
            JTextField duration,
            JComboBox<Choice<Facilities>> facility,
            Map<Resources, Double> resourceValues,
            EnumSelectionPanel<Capability> capabilities,
            JList<Agent> team,
            JTextField explorationRegion,
            JTextField researchTopic,
            JComboBox<Choice<Object>> repairTarget) {
        Choice<Facilities> selectedFacility = facility.getSelectedIndex() < 0 ? null
                : facility.getItemAt(facility.getSelectedIndex());
        String typeName = String.valueOf(type.getSelectedItem());
        String missionDetail = switch (typeName) {
            case "Exploration" -> "\nRegion: " + explorationRegion.getText().trim();
            case "Research" -> "\nTopic: " + researchTopic.getText().trim();
            case "Repair" -> {
                Choice<Object> target = repairTarget.getSelectedIndex() < 0 ? null
                        : repairTarget.getItemAt(repairTarget.getSelectedIndex());
                yield "\nDamaged item: " + (target == null || target.value() == null
                        ? "Not selected" : repairTargetLabel(target.value()));
            }
            default -> "";
        };
        return "Mission: " + humanize(String.valueOf(type.getSelectedItem()))
                + "\nDuration: " + duration.getText().trim() + " time units"
                + "\nFacility: " + (selectedFacility == null || selectedFacility.value() == null
                        ? "No facility selected" : selectedFacility.value().getLocation())
                + "\nRequired supplies: " + (resourceValues.isEmpty() ? "None" : resourceSummary(resourceValues))
                + "\nRequired abilities: " + (capabilities.selectedValues().isEmpty()
                        ? "None" : humanize(capabilities.selectedValues().toString()))
                + missionDetail
                + "\nSelected team members: " + (team.getSelectedValuesList().isEmpty()
                        ? "None" : team.getSelectedValuesList().stream()
                                .map(AdminDashboard::agentLabel)
                                .reduce((a, b) -> a + ", " + b).orElse("None"));
    }

    private void populateMissionFacilities(
            JComboBox<Choice<Facilities>> combo, String missionType) {
        combo.removeAllItems();
        combo.addItem(new Choice<>("No facility", null));
        boolean research = "Research".equals(missionType);
        for (Facilities candidate : session.colony().getFacilities()) {
            if (!research || candidate instanceof Laboratory) {
                combo.addItem(new Choice<>(candidate.getClass().getSimpleName()
                        + " | " + candidate.getLocation(), candidate));
            }
        }
        if (combo.getItemCount() > 1) {
            combo.setSelectedIndex(1);
        }
    }

    private void populateRepairTargets(JComboBox<Choice<Object>> combo) {
        combo.removeAllItems();
        combo.addItem(new Choice<>("No damaged target available", null));
        for (Object target : repairTargets()) {
            combo.addItem(new Choice<>(repairTargetLabel(target), target));
        }
        if (combo.getItemCount() > 1) {
            combo.setSelectedIndex(1);
        }
    }

    private void submitMissionObject(Mission mission, List<Agent> selectedAgents) {
        try {
            boolean accepted = session.colony().submitMission(mission, selectedAgents);
            boolean registered = session.colony().getMissions().contains(mission);
            if (registered) {
                session.removeMissionDraft(mission);
            }
            String outcome = accepted ? "ACCEPTED" : "REJECTED";
            String reason = mission.getFailureReason();
            record("Submit mission / assign team", mission.getClass().getSimpleName(),
                    outcome, accepted ? "Mission state " + mission.getState() : reason);
            showFeedback(accepted ? "Mission assigned" : "Mission not assigned",
                    accepted ? "The team is assigned. Start the mission when you are ready."
                            : friendlyMissionRejection(reason),
                    accepted ? "Current mission state: " + mission.getState()
                            : "Colony submission result: rejected\nReason: " + reason,
                    accepted ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException failure) {
            reportFailure("Submit mission", mission.getClass().getSimpleName(), failure);
        }
    }

    private String friendlyMissionRejection(String reason) {
        if (reason == null || reason.isBlank()) {
            return "The colony could not accept this mission. Check its team and current status.";
        }
        String message = reason.toLowerCase(java.util.Locale.ROOT);
        if (message.contains("agent") && message.contains("unavailable")) {
            return "At least one selected person or robot is busy. Choose only available team members.";
        }
        if (message.contains("capabilit") || message.contains("skill")) {
            return "The selected team is missing a skill or ability required for this mission.";
        }
        if (message.contains("team") || message.contains("responder")) {
            return "The selected team does not include everyone needed for this mission.";
        }
        if (message.contains("active mission") || message.contains("already active")) {
            return "Another mission is already active. Finish it before assigning a new mission.";
        }
        if (message.contains("resource")) {
            return "The colony does not have enough supplies for this mission.";
        }
        if (message.contains("no agents") || message.contains("empty")) {
            return "Choose at least one available person or robot for the team.";
        }
        return "The colony could not accept this mission. Review the team and mission details.";
    }

    private List<Object> repairTargets() {
        List<Object> targets = new ArrayList<>();
        for (Facilities facility : session.colony().getFacilities()) {
            if (facility.getOperationalStatus() == OperationalStatus.DAMAGED) {
                targets.add(facility);
            }
            for (Equipment item : facility.getEquipment()) {
                if (item.getOperationalStatus() == OperationalStatus.DAMAGED) {
                    targets.add(item);
                }
            }
        }
        return targets;
    }

    private static String repairTargetLabel(Object target) {
        if (target instanceof Facilities facility) {
            return humanize(facility.getClass().getSimpleName()) + " | " + facility.getLocation();
        }
        if (target instanceof Equipment equipment) {
            return "Equipment | " + equipment.getName();
        }
        return String.valueOf(target);
    }

    private void checkMissionTeam() {
        Mission mission = selectedObject(missionTable, missionModel, Mission.class);
        List<Agent> team = teamList.getSelectedValuesList();
        if (mission == null) {
            showInfo("Select a mission first.");
            return;
        }
        List<String> individual = new ArrayList<>();
        try {
            for (Agent agent : team) {
                individual.add(agentLabel(agent) + " — "
                        + (agent.canPerform(mission) ? "can take this mission" : "cannot take this mission")
                        + (agent.isAvailable() ? " and is available" : " and is busy"));
            }
            boolean composition = mission.hasValidTeamComposition(team);
            showText("Team check", (composition
                    ? "This team matches the mission's team requirements."
                    : "This team does not meet the mission's team requirements.")
                    + "\n\nSelected team members: " + team.size()
                    + "\n\nMember checks:\n" + (individual.isEmpty()
                            ? "Select one or more people or robots to check the team."
                            : String.join("\n", individual))
                    + "\n\nThe final assignment check also checks the colony's current mission and supply status.");
        } catch (RuntimeException failure) {
            reportFailure("Check mission team", mission.getClass().getSimpleName(), failure);
        }
    }

    private void submitMission() {
        Mission mission = selectedObject(missionTable, missionModel, Mission.class);
        if (mission == null) {
            showInfo("Select a mission draft first.");
            return;
        }
        if (!session.missionDrafts().contains(mission)) {
            showInfo("Only a draft can be submitted from this action.");
            return;
        }
        List<Agent> team = teamList.getSelectedValuesList();
        submitMissionObject(mission, team);
    }

    private void unassignAgent() {
        Mission mission = selectedObject(missionTable, missionModel, Mission.class);
        Agent agent = teamList.getSelectedValue();
        if (mission == null || agent == null) {
            showInfo("Select a mission and one assigned agent first.");
            return;
        }
        try {
            mission.unassign(agent);
            record("Unassign agent", agentLabel(agent), "UPDATED",
                    humanize(mission.getClass().getSimpleName()) + " mission is now "
                            + humanize(mission.getState() == null ? "Draft" : mission.getState().name()));
        } catch (RuntimeException failure) {
            reportFailure("Unassign agent", agentLabel(agent), failure);
        }
    }

    private void startActiveMission() {
        Mission active = session.colony().getActiveMission();
        if (active == null) {
            showInfo("There is no active mission to start.");
            return;
        }
        try {
            Map<Resources, Double> resourcesBefore = session.colony().getColonyResourceTotal();
            boolean started = session.colony().startActiveMission();
            Map<Resources, Double> resourcesAfter = session.colony().getColonyResourceTotal();
            record("Start mission", active.getClass().getSimpleName(),
                    started ? "STARTED" : "NOT STARTED",
                    "State " + active.getState() + (active.getFailureReason() == null
                            ? "" : "; " + active.getFailureReason()));
            String changes = resourceChanges(resourcesBefore, resourcesAfter);
            if (!"No resource total changed".equals(changes)) {
                record("Mission resource consumption", active.getClass().getSimpleName(),
                        "UPDATED", changes);
            }
            showFeedback(started ? "Mission started" : "Mission did not start",
                    started ? "The mission is now in progress. Use Advance when time passes."
                            : friendlyMissionRejection(active.getFailureReason()),
                    "Current mission state: " + active.getState()
                            + "\nReason: " + active.getFailureReason(),
                    started ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException failure) {
            reportFailure("Start mission", active.getClass().getSimpleName(), failure);
        }
    }

    private void advanceTime() {
        String input = timeUnitsField.getText().trim();
        try {
            int units = Integer.parseInt(input);
            Mission activeBefore = session.colony().getActiveMission();
            Mission_Emergency_States stateBefore = activeBefore == null
                    ? null : activeBefore.getState();
            Simulation.advanceTime(session.colony(), units);
            record("Advance simulation time",
                    activeBefore == null ? "Colony" : activeBefore.getClass().getSimpleName(),
                    "ADVANCED", units + " time unit(s); Simulation time " + Simulation.time);
            if (activeBefore != null && activeBefore.getState() != stateBefore) {
                record("Mission lifecycle", activeBefore.getClass().getSimpleName(),
                        String.valueOf(activeBefore.getState()),
                        activeBefore.getFailureReason());
            }
        } catch (RuntimeException failure) {
            reportFailure("Advance simulation time", input, failure);
        }
    }

    private void finishMission() {
        Mission active = session.colony().getActiveMission();
        if (active == null) {
            showInfo("There is no active mission to complete.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Mark the " + humanize(active.getClass().getSimpleName()) + " mission as complete?",
                "Confirm completion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean finished = session.colony().finishActiveMission(true);
            record("Complete mission", active.getClass().getSimpleName(),
                    finished ? "COMPLETED" : "NOT COMPLETED", "State " + active.getState());
            showFeedback(finished ? "Mission completed" : "Mission not completed",
                    finished ? "The mission is complete and the colony has been updated."
                            : "The mission could not be completed in its current state.",
                    "Current mission state: " + active.getState(),
                    finished ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException failure) {
            reportFailure("Complete mission", active.getClass().getSimpleName(), failure);
        }
    }

    private void failMission() {
        Mission active = session.colony().getActiveMission();
        if (active == null) {
            showInfo("There is no active mission to fail.");
            return;
        }
        String reason = JOptionPane.showInputDialog(this,
                "Why did the mission fail?", "Operator requested failure");
        if (reason == null) {
            return;
        }
        try {
            boolean failed = session.colony().failActiveMission(reason);
            record("Fail mission", active.getClass().getSimpleName(),
                    failed ? "FAILURE RECORDED" : "NOT FAILED", active.getFailureReason());
            showFeedback(failed ? "Mission marked as failed" : "Mission could not be failed",
                    failed ? "The failure was recorded. The mission is no longer active."
                            : "The current mission state does not allow this action.",
                    "Current mission state: " + active.getState()
                            + "\nFailure reason: " + active.getFailureReason(),
                    failed ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException failure) {
            reportFailure("Fail mission", active.getClass().getSimpleName(), failure);
        }
    }

    private void showMissionDetails() {
        Mission mission = selectedObject(missionTable, missionModel, Mission.class);
        if (mission == null) {
            showInfo("Select a mission first.");
            return;
        }
        String extra = "";
        if (mission instanceof Exploration exploration) {
            extra = "\nRegion: " + exploration.getRegion();
        } else if (mission instanceof Research research) {
            extra = "\nTopic: " + research.getTopic()
                    + "\nExperiment completed: " + research.isExperimentCompleted();
        } else if (mission instanceof Repair repair) {
            extra = "\nRepair target: " + repairTargetLabel(repair.getObject());
        }
        showText("Mission details", "Mission: " + humanize(mission.getClass().getSimpleName())
                + "\nStatus: " + humanize(mission.getState() == null ? "Draft" : mission.getState().name())
                + "\nDuration: " + mission.getNeededTimesUnits()
                + "\nElapsed: " + mission.getElapsedTimeUnits()
                + "\nFacility: " + (mission.getFacility() == null ? "None"
                        : mission.getFacility().getLocation())
                + "\nRequired abilities: " + formatEnums(mission.getRequiredCapabilities())
                + "\nRequired supplies: " + resourceSummary(mission.getRequiredResources())
                + "\nTeam: " + mission.getAgents().stream().map(AdminDashboard::agentLabel).toList()
                + "\nFailure reason: " + mission.getFailureReason() + extra);
    }

    private void setEnvironment() {
        EnviromentStates environment = chooseEnum("Colony environment", "Choose the current conditions",
                EnviromentStates.values(), session.environment().GetEnvironmentState());
        if (environment == null) {
            return;
        }
        try {
            session.environment().SetEnvironmentState(environment);
            session.colony().setMarsEnvironment(session.environment());
            record("Set environment", "Mars environment", "UPDATED", environment.name());
        } catch (RuntimeException failure) {
            reportFailure("Set environment", "Mars environment", failure);
        }
    }

    private void showColonyDetails() {
        Colony colony = session.colony();
        Mission active = colony.getActiveMission();
        showText("Colony snapshot", "Personnel: " + colony.getPersonnel().size()
                + "\nRobotic units: " + colony.getRoboticUnits().size()
                + "\nFacilities: " + colony.getFacilities().size()
                + "\nMissions: " + session.missions().size()
                + "\nEmergencies: " + session.emergencies().size()
                + "\nActive mission: " + (active == null ? "None"
                        : active.getClass().getSimpleName() + " / " + active.getState())
                + "\nEnvironment: " + session.environment().GetEnvironmentState()
                + "\nResources: " + colony.getColonyResourceTotal());
    }

    private void clearActivity() {
        if (JOptionPane.showConfirmDialog(this,
                "Clear the activity log displayed by this GUI?",
                "Clear activity log", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        session.clearActivity();
        refreshAll();
    }

    private void record(String operation, String related, String result, String details) {
        session.record(operation, related, result, details);
        updateSessionStatus(operation, related, result, details);
        refreshAll();
    }

    private void reportFailure(String operation, String related, RuntimeException failure) {
        String detail = failure.getClass().getSimpleName() + ": " + messageOf(failure);
        boolean invalidInput = failure instanceof NumberFormatException
                || failure instanceof IllegalArgumentException;
        String result = invalidInput ? "INVALID INPUT" :
                failure instanceof IllegalStateException ? "REJECTED" : "FAILED";
        session.record(operation, related, result, detail);
        updateSessionStatus(operation, related, result, detail);
        refreshAll();
        showFeedback(invalidInput ? "Check the information" :
                        result.equals("REJECTED") ? "Action not available" : "Action failed",
                friendlyFailureMessage(failure), detail,
                invalidInput ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private void updateSessionStatus(
            String operation, String related, String result, String details) {
        String message = switch (result.toUpperCase(java.util.Locale.ROOT)) {
            case "ACCEPTED" -> "Mission assigned · ready to start";
            case "CREATED / DRAFT" -> "Draft saved · " + humanize(related);
            case "CREATED" -> humanize(related) + " added";
            case "STARTED" -> "Mission started";
            case "COMPLETED" -> "Mission completed";
            case "RESOLVED" -> "Emergency response completed";
            case "FAILURE RECORDED" -> "Mission failure recorded";
            case "UNRESOLVED" -> "Response unsuccessful · see Activity for details";
            case "UPDATED", "ADVANCED" -> humanize(operation) + " saved";
            case "REMOVED" -> humanize(related) + " removed";
            case "REJECTED" -> "Action rejected · see Activity for details";
            case "INVALID INPUT" -> "Check the entered information";
            case "FAILED", "REJECTED / ERROR", "NOT COMPLETED", "NOT STARTED" ->
                    "Action failed · see Activity for details";
            default -> humanize(operation) + " · " + humanize(result);
        };
        sessionStatus.setText(message);
        sessionStatus.setToolTipText(details == null || details.isBlank() ? message : details);
        boolean problem = result.contains("REJECTED") || result.contains("FAIL")
                || result.contains("INVALID") || result.startsWith("NOT ")
                || result.equalsIgnoreCase("UNRESOLVED");
        boolean warning = result.contains("REJECTED") || result.contains("INVALID")
                || result.equalsIgnoreCase("UNRESOLVED");
        sessionStatus.setForeground(problem
                ? (warning ? WARNING_TEXT : DANGER_TEXT) : SUCCESS_TEXT);
        sessionStatus.setBackground(problem
                ? (warning ? WARNING_BG : DANGER_BG) : SUCCESS_BG);
        sessionStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(problem
                        ? (warning ? WARNING_BORDER : DANGER_BORDER)
                        : SUCCESS_BORDER),
                new EmptyBorder(9, 11, 9, 11)));
    }

    private String friendlyFailureMessage(RuntimeException failure) {
        if (failure instanceof NumberFormatException) {
            return "One of the numbers is not valid. Review the number fields and try again.";
        }
        String message = messageOf(failure).toLowerCase(java.util.Locale.ROOT);
        if (message.contains("unavailable") || message.contains("not available")) {
            return "This person or robot is busy. Choose someone who is available.";
        }
        if (message.contains("capabilit") || message.contains("skill")) {
            return "The selected team does not have the skills or equipment this task requires.";
        }
        if (message.contains("resource") || message.contains("oxygen")
                || message.contains("spare part")) {
            return "There are not enough suitable supplies for this operation.";
        }
        if (message.contains("active mission") || message.contains("already active")) {
            return "Finish or fail the current mission before assigning another one.";
        }
        if (failure instanceof IllegalStateException) {
            return "This action is not available in the item's current status.";
        }
        if (failure instanceof IllegalArgumentException) {
            return "Some information does not meet the required rules. Review the form and try again.";
        }
        return "The operation could not be completed. Review the item and try again.";
    }

    private void showFeedback(String title, String message, String technicalDetails, int type) {
        JDialog dialog = new JDialog(this, title, true);
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(new EmptyBorder(18, 20, 14, 20));
        JLabel summary = new JLabel(message);
        summary.setForeground(type == JOptionPane.ERROR_MESSAGE ? DANGER_TEXT
                : type == JOptionPane.WARNING_MESSAGE ? WARNING_TEXT : GREEN);
        summary.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        content.add(summary, BorderLayout.NORTH);

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        JToggleButton detailsToggle = new JToggleButton("Show technical details");
        JTextArea details = new JTextArea(technicalDetails, 6, 48);
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(details);
        detailsScroll.setPreferredSize(new Dimension(500, 135));
        detailsScroll.setVisible(false);
        detailsToggle.setVisible(technicalDetails != null && !technicalDetails.isBlank());
        detailsToggle.addActionListener(event -> {
            boolean visible = detailsToggle.isSelected();
            detailsScroll.setVisible(visible);
            detailsToggle.setText(visible ? "Hide technical details" : "Show technical details");
            dialog.pack();
        });
        footer.add(detailsToggle, BorderLayout.WEST);
        footer.add(detailsScroll, BorderLayout.CENTER);
        JButton close = new JButton("Close");
        close.addActionListener(event -> dialog.dispose());
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closePanel.add(close);
        footer.add(closePanel, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.CENTER);

        dialog.setContentPane(content);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize(Math.min(dialog.getWidth(), screen.width - 40),
                Math.min(dialog.getHeight(), screen.height - 60));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static String messageOf(Throwable failure) {
        return failure.getMessage() == null ? "(no message)" : failure.getMessage();
    }

    private static String humanize(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }
        String spaced = value.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace('_', ' ').replaceAll("(?i)inprogress", "in progress");
        String[] words = spaced.toLowerCase(java.util.Locale.ROOT).split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) result.append(' ');
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return result.toString();
    }

    private static String formatEnums(Collection<? extends Enum<?>> values) {
        if (values == null || values.isEmpty()) return "None";
        return values.stream().map(value -> humanize(value.name()))
                .reduce((left, right) -> left + ", " + right).orElse("None");
    }

    private static <T> void humanizeEnumChoices(JComboBox<T> choices) {
        choices.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focus);
                label.setText(value instanceof Enum<?> enumValue
                        ? humanize(enumValue.name()) : String.valueOf(value));
                return label;
            }
        });
    }

    private <E extends Enum<E>> E chooseEnum(
            String title, String prompt, E[] values, E current) {
        JComboBox<E> options = new JComboBox<>(values);
        humanizeEnumChoices(options);
        if (current != null) options.setSelectedItem(current);
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel(prompt), BorderLayout.NORTH);
        panel.add(options, BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        return options.getSelectedIndex() < 0 ? null : options.getItemAt(options.getSelectedIndex());
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "MARS-OPS", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showText(String title, String text) {
        JTextArea area = new JTextArea(text, 9, 48);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static JPanel form(Object... labelAndFields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.gridy = 0;
        for (int i = 0; i < labelAndFields.length; i += 2, constraints.gridy++) {
            constraints.gridx = 0;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            panel.add(new JLabel(String.valueOf(labelAndFields[i])), constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            panel.add((Component) labelAndFields[i + 1], constraints);
        }
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.VERTICAL;
        panel.add(new JPanel(), constraints);
        return panel;
    }

    private static JScrollPane boundedFormContent(
            Component content, int preferredWidth, int preferredHeight) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(preferredWidth, Math.max(320, screen.width - 80));
        int height = Math.min(preferredHeight, Math.max(260, screen.height - 180));
        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(width, height));
        return scroll;
    }

    private static void configureAgentList(JList<Agent> list) {
        list.setBackground(CARD);
        list.setForeground(TEXT);
        list.setSelectionBackground(SELECTED);
        list.setSelectionForeground(TEXT);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> source, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        source, value, index, selected, focus);
                if (value instanceof Agent agent) {
                    label.setText(agentLabel(agent));
                    label.setForeground(agent.isAvailable() || selected ? TEXT : MUTED);
                }
                label.setBackground(selected ? SELECTED : CARD);
                label.setOpaque(true);
                label.setBorder(new EmptyBorder(5, 7, 5, 7));
                return label;
            }
        });
    }

    private JComboBox<String> stringChoices(List<String> availableValues, String fallback) {
        List<String> values = new ArrayList<>(new java.util.LinkedHashSet<>(availableValues));
        if (values.isEmpty()) {
            values.add(fallback);
        }
        JComboBox<String> combo = new JComboBox<>(values.toArray(String[]::new));
        combo.setSelectedItem(values.contains(fallback) ? fallback : values.get(0));
        return combo;
    }

    private List<String> existingLocations() {
        java.util.LinkedHashSet<String> locations = new java.util.LinkedHashSet<>();
        for (Facilities facility : session.colony().getFacilities()) {
            locations.add(facility.getLocation());
        }
        for (Agent agent : session.agents()) {
            locations.add(agent.getCurrentLocation());
        }
        return new ArrayList<>(locations);
    }

    private List<String> personnelHealthStatuses() {
        return session.colony().getPersonnel().stream()
                .map(Personnel::getHealthStatus).distinct().toList();
    }

    private List<String> robotHealthStatuses() {
        return session.colony().getRoboticUnits().stream()
                .map(RoboticUnit::getHealthStatus).distinct().toList();
    }

    private Set<Capability> fixedRobotCapabilities(String type) {
        String location = existingLocations().stream().findFirst().orElse("Preview");
        return switch (type) {
            case "Repair robot" -> new RepairRobot(100.0, "Operational", location, true)
                    .getCapabilities();
            case "Drone" -> new Drone(100.0, "Operational", location, true)
                    .getCapabilities();
            default -> Set.of();
        };
    }

    private static String resourceSummary(Map<Resources, Double> resources) {
        if (resources == null || resources.isEmpty()) {
            return "None";
        }
        return resources.entrySet().stream()
                .map(entry -> humanize(entry.getKey().name()) + " = " + entry.getValue())
                .reduce((left, right) -> left + ", " + right).orElse("None");
    }

    private static String resourceChanges(
            Map<Resources, Double> before, Map<Resources, Double> after) {
        List<String> changes = new ArrayList<>();
        Set<Resources> resources = EnumSet.noneOf(Resources.class);
        resources.addAll(before.keySet());
        resources.addAll(after.keySet());
        for (Resources resource : resources) {
            double oldAmount = before.getOrDefault(resource, 0.0);
            double newAmount = after.getOrDefault(resource, 0.0);
            if (Double.compare(oldAmount, newAmount) != 0) {
                changes.add(humanize(resource.name()) + " " + oldAmount + " -> " + newAmount);
            }
        }
        return changes.isEmpty() ? "No resource total changed" : String.join("; ", changes);
    }

    private static String equipmentNames(List<Equipment> equipment) {
        return equipment.stream().map(Equipment::getName).toList().toString();
    }

    private static String agentLabel(Agent agent) {
        if (agent instanceof Personnel person) {
            return humanize(person.getClass().getSimpleName()) + " | " + person.getName()
                    + " (#" + person.getId() + ") | "
                    + (person.isAvailable() ? "available" : "busy");
        }
        if (agent instanceof RoboticUnit robot) {
            return humanize(robot.getClass().getSimpleName()) + " | " + robot.getCurrentLocation()
                    + " | " + (robot.isAvailable() ? "available" : "busy");
        }
        return humanize(agent.getClass().getSimpleName()) + " | "
                + (agent.isAvailable() ? "available" : "busy");
    }

    private static <T> DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JTable table(DefaultTableModel model, boolean hideLastColumn) {
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setSelectionBackground(SELECTED);
        table.setSelectionForeground(TEXT);
        table.setGridColor(BORDER);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int column) {
                Component component = super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                if (!selected) {
                    component.setBackground(CARD);
                    component.setForeground(TEXT);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return component;
            }
        });
        if (hideLastColumn) {
            table.removeColumn(table.getColumnModel().getColumn(
                    table.getColumnModel().getColumnCount() - 1));
        }
        return table;
    }

    private static void clear(DefaultTableModel model) {
        model.setRowCount(0);
    }

    private static <T> T selectedObject(JTable table, DefaultTableModel model, Class<T> type) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object selected = model.getValueAt(modelRow, model.getColumnCount() - 1);
        return type.isInstance(selected) ? type.cast(selected) : null;
    }

    private static final class Choice<T> {
        private final String label;
        private final T value;

        Choice(String label, T value) {
            this.label = label;
            this.value = value;
        }

        T value() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class EnumSelectionPanel<E extends Enum<E>> extends JPanel {
        private final Class<E> enumType;
        private final Map<E, JCheckBox> choices;

        EnumSelectionPanel(Class<E> enumType) {
            super(new GridLayout(0, 2, 5, 3));
            this.enumType = enumType;
            this.choices = new EnumMap<>(enumType);
            setBackground(CARD);
            for (E value : enumType.getEnumConstants()) {
                JCheckBox checkbox = new JCheckBox(humanize(value.name()));
                checkbox.setOpaque(false);
                checkbox.setForeground(TEXT);
                checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                choices.put(value, checkbox);
                add(checkbox);
            }
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(7, 8, 7, 8)));
        }

        void setSelection(Set<E> selected) {
            for (Map.Entry<E, JCheckBox> choice : choices.entrySet()) {
                choice.getValue().setSelected(selected.contains(choice.getKey()));
            }
        }

        Set<E> selectedValues() {
            EnumSet<E> selected = EnumSet.noneOf(enumType);
            choices.forEach((value, checkbox) -> {
                if (checkbox.isSelected()) {
                    selected.add(value);
                }
            });
            return selected;
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (choices != null) {
                choices.values().forEach(checkbox -> checkbox.setEnabled(enabled));
            }
        }
    }

}
