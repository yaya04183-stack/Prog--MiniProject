package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicButtonUI;

import agents.Agent;
import colony.Colony;
import emergencies.Fire;
import emergencies.OxygenLeak;
import enums.Capability;
import enums.EmergencyStatus;
import enums.EnviromentStates;
import enums.Mission_Emergency_States;
import enums.OperationalStatus;
import enums.Resources;
import enums.Skills;
import facilities.Equipment;
import facilities.Habitat;
import marsenvironment.MarsEnvironment;
import missions.Exploration;
import missions.Mission;
import missions.Rescue;
import personnel.Engineer;
import personnel.Scientist;
import roboticunit.ExplorationRover;
import roboticunit.RepairRobot;
import simulation.Simulation;

/** Executes checks against fresh instances of the actual project classes. */
final class ScenarioLabPanel extends JPanel {

    private static final Color CARD = new Color(249, 246, 235);
    private static final Color TEXT = new Color(39, 51, 44);
    private static final Color MUTED = new Color(99, 111, 101);
    private static final Color GREEN = new Color(69, 105, 78);
    private static final Color RED = new Color(157, 58, 45);
    private static final Color BORDER = new Color(201, 191, 172);
    private static final Color CONTROL = new Color(238, 232, 219);
    private static final Color SELECTED = new Color(232, 216, 196);
    private static final Color TABLE_HEADER = new Color(222, 214, 196);
    private static final Color ACCENT = new Color(174, 83, 54);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AppSession session;
    private final Runnable refreshDashboard;
    private final DefaultTableModel resultModel = new DefaultTableModel(
            new Object[]{"Time", "Test", "Status", "Actual result", "Technical details"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable resultTable = new JTable(resultModel);
    private final DefaultListModel<String> categoryModel = new DefaultListModel<>();
    private final JList<String> categoryList = new JList<>(categoryModel);
    private final DefaultListModel<TestCase> testModel = new DefaultListModel<>();
    private final JList<TestCase> testList = new JList<>(testModel);
    private final JLabel selectedTestName = new JLabel("Choose a test");
    private final JLabel selectedTestDescription = new JLabel("Select a test to see what it checks.");
    private final JTextArea selectedInputs = new JTextArea(3, 48);
    private final JTextArea expectedResult = new JTextArea(3, 48);
    private final JTextArea resultDetails = new JTextArea(4, 48);
    private final JLabel summary = new JLabel("Choose a scenario and press Run this test.");
    private final JTextField search = new JTextField(20);
    private final JButton runSelectedButton = new JButton("Run selected test");
    private final JButton runCategoryButton = new JButton("Run category");
    private final List<String> categories = List.of("All tests", "Agent Tests",
            "Mission Tests", "Resource Tests", "Facility Tests", "Emergency Tests",
            "Validation Tests", "Edge Case Tests");
    private final List<TestCase> testCases = buildTestCases();

    ScenarioLabPanel(AppSession session, Runnable refreshDashboard) {
        this.session = session;
        this.refreshDashboard = refreshDashboard;
        setLayout(new BorderLayout(0, 11));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(19, 22, 19, 22));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildWorkspace(), BorderLayout.CENTER);
        configureTable();
        populateCategories();
        populateTests();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(12, 10));
        panel.setOpaque(false);

        JPanel titles = new JPanel(new BorderLayout(0, 5));
        titles.setOpaque(false);
        JLabel title = new JLabel("Scenario console");
        title.setForeground(TEXT);
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        JLabel subtitle = new JLabel("Pick a test, see the setup and expected result, then run it. Your live colony is not changed.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.CENTER);
        panel.add(titles, BorderLayout.NORTH);

        JPanel steps = new JPanel(new java.awt.GridLayout(1, 3, 1, 0));
        steps.setBackground(CARD);
        steps.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        String[][] instructions = {
                {"01 / CATEGORY", "Choose a test group"},
                {"02 / SCENARIO", "Read setup and expected result"},
                {"03 / RUN", "Press Run this test"}
        };
        for (String[] instruction : instructions) {
            JPanel step = new JPanel(new BorderLayout(0, 3));
            step.setOpaque(false);
            step.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 8));
            JLabel stepName = new JLabel(instruction[0]);
            stepName.setForeground(ACCENT);
            stepName.setFont(new Font("Consolas", Font.BOLD, 8));
            JLabel stepHint = new JLabel(instruction[1]);
            stepHint.setForeground(TEXT);
            stepHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            step.add(stepName, BorderLayout.NORTH);
            step.add(stepHint, BorderLayout.CENTER);
            steps.add(step);
        }
        panel.add(steps, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        JButton clear = actionButton("Clear results", false);
        clear.addActionListener(event -> {
            resultModel.setRowCount(0);
            resultDetails.setText("Select a result to review what happened.");
            updateSummary();
        });
        search.setToolTipText("Search the test names and required inputs");
        search.setColumns(18);
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        controls.add(new JLabel("Find a scenario:"));
        controls.add(search);
        controls.add(clear);
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildWorkspace() {
        JSplitPane workspace = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildCatalog(), buildTestAndResults());
        workspace.setResizeWeight(0.28);
        workspace.setDividerLocation(270);
        workspace.setBorder(null);
        workspace.setOpaque(false);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(workspace, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildCatalog() {
        JPanel panel = new JPanel(new BorderLayout(0, 9));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(13, 13, 13, 13)));
        JLabel heading = new JLabel("Test categories");
        heading.setForeground(TEXT);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(heading, BorderLayout.NORTH);

        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setBackground(CARD);
        categoryList.setForeground(TEXT);
        categoryList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryList.setSelectionBackground(SELECTED);
        categoryList.setSelectionForeground(TEXT);
        categoryList.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focus);
                String category = String.valueOf(value);
                label.setText(category + "  ·  " + countForCategory(category));
                label.setBorder(BorderFactory.createEmptyBorder(9, 8, 9, 8));
                return label;
            }
        });
        categoryList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) populateTests();
        });
        JScrollPane categoryScroll = new JScrollPane(categoryList);
        categoryScroll.setBorder(BorderFactory.createEmptyBorder());
        categoryScroll.getViewport().setBackground(CARD);

        JPanel testChoices = new JPanel(new BorderLayout(0, 6));
        testChoices.setOpaque(false);
        JLabel testsHeading = new JLabel("Available tests");
        testsHeading.setForeground(TEXT);
        testsHeading.setFont(new Font("Segoe UI", Font.BOLD, 13));
        testChoices.add(testsHeading, BorderLayout.NORTH);
        JScrollPane testsScroll = new JScrollPane(testList);
        testsScroll.setBorder(BorderFactory.createEmptyBorder());
        testsScroll.getViewport().setBackground(CARD);
        testChoices.add(testsScroll, BorderLayout.CENTER);

        JSplitPane catalogLists = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                categoryScroll, testChoices);
        catalogLists.setResizeWeight(0.42);
        catalogLists.setDividerLocation(210);
        catalogLists.setBorder(null);
        panel.add(catalogLists, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTestAndResults() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buildSelectedTest(), buildResults());
        split.setResizeWeight(0.48);
        split.setDividerLocation(310);
        split.setBorder(null);
        split.setOpaque(false);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSelectedTest() {
        JPanel panel = new JPanel(new BorderLayout(0, 9));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(13, 14, 13, 14)));
        JPanel details = new JPanel(new BorderLayout(0, 5));
        details.setOpaque(false);
        selectedTestName.setForeground(TEXT);
        selectedTestName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        selectedTestDescription.setForeground(MUTED);
        selectedTestDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        details.add(selectedTestName, BorderLayout.NORTH);
        details.add(selectedTestDescription, BorderLayout.CENTER);
        panel.add(details, BorderLayout.NORTH);

        selectedInputs.setEditable(false);
        selectedInputs.setLineWrap(true);
        selectedInputs.setWrapStyleWord(true);
        expectedResult.setEditable(false);
        expectedResult.setLineWrap(true);
        expectedResult.setWrapStyleWord(true);
        JPanel explanation = new JPanel(new BorderLayout(0, 8));
        explanation.setOpaque(false);
        explanation.add(infoBlock("What you need", selectedInputs), BorderLayout.NORTH);
        explanation.add(infoBlock("Expected result", expectedResult), BorderLayout.CENTER);
        panel.add(explanation, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        actions.setOpaque(false);
        runSelectedButton.setText("Run this test");
        styleButton(runSelectedButton, true);
        runSelectedButton.addActionListener(event -> runSelected());
        runCategoryButton.setText("Run this category");
        styleButton(runCategoryButton, false);
        runCategoryButton.addActionListener(event -> runCategory());
        JButton runAll = actionButton("Run all " + testCases.size() + " scenarios", false);
        runAll.addActionListener(event -> runAll());
        actions.add(runSelectedButton);
        actions.add(runCategoryButton);
        actions.add(runAll);
        panel.add(actions, BorderLayout.SOUTH);

        testList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        testList.setBackground(CARD);
        testList.setForeground(TEXT);
        testList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        testList.setSelectionBackground(SELECTED);
        testList.setSelectionForeground(TEXT);
        testList.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focus);
                label.setText(value instanceof TestCase testCase ? testCase.name() : "");
                label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                return label;
            }
        });
        testList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) showSelectedTest();
        });
        return panel;
    }

    private JPanel infoBlock(String title, JTextArea content) {
        JPanel block = new JPanel(new BorderLayout(0, 3));
        block.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setForeground(TEXT);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        content.setBackground(CONTROL);
        content.setForeground(TEXT);
        content.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        block.add(heading, BorderLayout.NORTH);
        block.add(content, BorderLayout.CENTER);
        return block;
    }

    private JPanel buildResults() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(true);
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        summary.setForeground(MUTED);
        summary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(summary, BorderLayout.NORTH);
        resultDetails.setEditable(false);
        resultDetails.setLineWrap(true);
        resultDetails.setWrapStyleWord(true);
        resultDetails.setBackground(CONTROL);
        resultDetails.setForeground(TEXT);
        resultDetails.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
        resultDetails.setText("Run a test to see the actual result and any technical details here.");
        JScrollPane scrollPane = new JScrollPane(resultTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD);
        panel.add(scrollPane, BorderLayout.CENTER);
        JScrollPane detailsScroll = new JScrollPane(resultDetails);
        detailsScroll.setPreferredSize(new java.awt.Dimension(0, 82));
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Selected result details"));
        panel.add(detailsScroll, BorderLayout.SOUTH);
        resultTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) showSelectedResult();
        });
        return panel;
    }

    private void configureTable() {
        resultTable.setFillsViewportHeight(true);
        resultTable.setRowHeight(30);
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultTable.setBackground(CARD);
        resultTable.setForeground(TEXT);
        resultTable.setSelectionBackground(SELECTED);
        resultTable.setSelectionForeground(TEXT);
        resultTable.setGridColor(BORDER);
        resultTable.getTableHeader().setBackground(TABLE_HEADER);
        resultTable.getTableHeader().setForeground(TEXT);
        resultTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable source, Object value, boolean selected,
                    boolean focused, int row, int column) {
                Component component = super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                if (!selected) {
                    component.setBackground(CARD);
                    component.setForeground(TEXT);
                    if (column == 2 && "PASS".equals(value)) {
                        component.setForeground(GREEN);
                    } else if (column == 2 && "FAIL".equals(value)) {
                        component.setForeground(RED);
                    }
                }
                setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 9));
                return component;
            }
        });
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(280);
        resultTable.getColumnModel().getColumn(4).setPreferredWidth(240);
    }

    private JButton actionButton(String text, boolean primary) {
        JButton button = new JButton(text);
        styleButton(button, primary);
        return button;
    }

    private void styleButton(JButton button, boolean primary) {
        button.setUI(new BasicButtonUI());
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(primary ? Color.WHITE : TEXT);
        button.setBackground(primary ? ACCENT : CONTROL);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? ACCENT : BORDER),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
    }

    private void runAll() {
        for (TestCase testCase : testCases) {
            run(testCase);
        }
    }

    private void runSelected() {
        TestCase selected = testList.getSelectedValue();
        if (selected == null) {
            summary.setText("Choose a test from the list before running it.");
            return;
        }
        run(selected);
    }

    private void runCategory() {
        String selectedCategory = categoryList.getSelectedValue();
        testCases.stream().filter(testCase -> belongsTo(testCase, selectedCategory))
                .forEach(this::run);
    }

    private void run(TestCase testCase) {
        TestObservation observation;
        int previousSimulationTime = Simulation.time;
        try {
            Simulation.resettime();
            observation = testCase.action().run();
        } catch (Throwable unexpected) {
            observation = new TestObservation(false,
                    "Unexpected exception", unexpected.getClass().getSimpleName(),
                    messageOf(unexpected));
        } finally {
            Simulation.time = previousSimulationTime;
        }
        String result = observation.passed() ? "PASS" : "FAIL";
        String exception = observation.exceptionType().isBlank()
                ? "" : observation.exceptionType() + ": " + observation.exceptionMessage();
        resultModel.insertRow(0, new Object[]{
                LocalDateTime.now().format(TIME_FORMAT),
                testCase.name(),
                result,
                observation.actual(),
                exception});
        resultTable.setRowSelectionInterval(0, 0);
        showSelectedResult();
        session.record("Scenario test", testCase.name(), result,
                "Expected: " + testCase.expected() + " | Actual: " + observation.actual()
                        + (exception.isBlank() ? "" : " | " + exception));
        updateSummary();
        refreshDashboard.run();
    }

    private void updateSummary() {
        int passed = 0;
        int failed = 0;
        for (int i = 0; i < resultModel.getRowCount(); i++) {
            if ("PASS".equals(resultModel.getValueAt(i, 2))) {
                passed++;
            } else {
                failed++;
            }
        }
        summary.setText("Results this session: " + passed + " passed · " + failed
                + " failed · " + (passed + failed) + " runs");
    }

    private void filter() {
        populateTests();
    }

    private void populateCategories() {
        categories.forEach(categoryModel::addElement);
        categoryList.setSelectedValue("All tests", true);
    }

    private void populateTests() {
        String category = categoryList.getSelectedValue();
        if (category == null) category = "All tests";
        String query = search.getText().trim().toLowerCase(java.util.Locale.ROOT);
        TestCase previouslySelected = testList.getSelectedValue();
        testModel.clear();
        for (TestCase testCase : testCases) {
            if (!belongsTo(testCase, category)) continue;
            String searchable = (testCase.name() + " " + testCase.input() + " "
                    + testCase.expected()).toLowerCase(java.util.Locale.ROOT);
            if (query.isEmpty() || searchable.contains(query)) testModel.addElement(testCase);
        }
        if (previouslySelected != null) {
            for (int i = 0; i < testModel.size(); i++) {
                if (testModel.get(i).equals(previouslySelected)) {
                    testList.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (!testModel.isEmpty()) testList.setSelectedIndex(0);
        else showSelectedTest();
        categoryList.repaint();
    }

    private void showSelectedTest() {
        TestCase selected = testList.getSelectedValue();
        boolean available = selected != null;
        runSelectedButton.setEnabled(available);
        runCategoryButton.setEnabled(categoryList.getSelectedValue() != null
                && countForCategory(categoryList.getSelectedValue()) > 0);
        if (!available) {
            selectedTestName.setText("No matching tests");
            selectedTestDescription.setText("Try another category or search term.");
            selectedInputs.setText("");
            expectedResult.setText("");
            return;
        }
        selectedTestName.setText(selected.name());
        selectedTestDescription.setText(descriptionFor(selected));
        selectedInputs.setText(humanizeScenarioText(selected.input()));
        expectedResult.setText(humanizeScenarioText(selected.expected()));
    }

    private void showSelectedResult() {
        int row = resultTable.getSelectedRow();
        if (row < 0) return;
        int modelRow = resultTable.convertRowIndexToModel(row);
        String name = String.valueOf(resultModel.getValueAt(modelRow, 1));
        TestCase testCase = testCases.stream().filter(test -> test.name().equals(name))
                .findFirst().orElse(null);
        String actual = String.valueOf(resultModel.getValueAt(modelRow, 3));
        String technical = String.valueOf(resultModel.getValueAt(modelRow, 4));
        resultDetails.setText((testCase == null ? "Inputs: Unknown"
                : "Inputs: " + humanizeScenarioText(testCase.input()))
                + "\nExpected: " + (testCase == null ? "Unknown"
                        : humanizeScenarioText(testCase.expected()))
                + "\nActual: " + actual
                + (technical.isBlank() ? "" : "\nTechnical details: " + technical));
        resultDetails.setCaretPosition(0);
    }

    private int countForCategory(String category) {
        return (int) testCases.stream().filter(testCase -> belongsTo(testCase, category)).count();
    }

    private boolean belongsTo(TestCase testCase, String category) {
        if (category == null || "All tests".equals(category)) return true;
        String name = testCase.name().toLowerCase(java.util.Locale.ROOT);
        String input = testCase.input().toLowerCase(java.util.Locale.ROOT);
        return switch (category) {
            case "Agent Tests" -> name.contains("agent") || name.contains("personnel")
                    || input.contains("agent") || input.contains("engineer")
                    || input.contains("scientist") || input.contains("rover");
            case "Mission Tests" -> name.contains("mission") || name.contains("rescue")
                    || name.contains("exploration");
            case "Resource Tests" -> name.contains("resource") || input.contains("oxygen")
                    || name.contains("oxygen leak");
            case "Facility Tests" -> name.contains("facility") || input.contains("facility")
                    || name.contains("laboratory");
            case "Emergency Tests" -> name.contains("emergency") || name.contains("oxygen leak");
            case "Validation Tests" -> name.contains("rejected") || name.contains("invalid")
                    || name.contains("wrong") || name.contains("duplicate")
                    || name.contains("missing") || name.contains("unavailable")
                    || name.contains("unsuitable") || name.contains("zero");
            case "Edge Case Tests" -> name.contains("dust storm") || name.contains("one active")
                    || name.contains("cannot") || name.contains("repeated")
                    || name.contains("failed active");
            default -> false;
        };
    }

    private static String descriptionFor(TestCase testCase) {
        return switch (testCase.name()) {
            case "Valid exploration submission" ->
                    "Checks that a scientist and equipped rover can submit an exploration mission.";
            case "Unavailable agent is rejected" ->
                    "Checks that a busy crew member cannot be assigned to a mission.";
            case "Unsuitable personnel is rejected" ->
                    "Checks that a crew member without the required skills is rejected.";
            case "Duplicate agent is rejected" ->
                    "Checks that listing the same crew member twice does not create an assignment.";
            case "Missing robot capability is rejected" ->
                    "Checks that a rover missing a required ability cannot join the mission.";
            case "Insufficient resources are not consumed" ->
                    "Checks that a mission short on oxygen is rejected without consuming supplies.";
            case "One active mission at a time" ->
                    "Checks that a second mission cannot replace the colony's active mission.";
            case "Rescue team composition is enforced" ->
                    "Checks that a rescue team without communication-capable personnel is rejected.";
            case "Valid Rescue team is accepted" ->
                    "Checks that a communication-capable crew member and suitable vehicle can be assigned.";
            case "Dust storm blocks exploration" ->
                    "Checks that a dust storm prevents an exploration mission from being accepted.";
            case "Successful mission completes and releases agents" ->
                    "Checks that a completed mission releases its team and clears the active mission.";
            case "Failed active mission releases agents" ->
                    "Checks that a failed active mission releases its team and clears the active mission.";
            case "Mission cannot start before association" ->
                    "Checks that a mission cannot start before the colony accepts it.";
            case "Mission cannot complete before it starts" ->
                    "Checks that an assigned mission cannot be completed before it starts.";
            case "Oxygen leak uses real emergency response" ->
                    "Checks that responders resolve an oxygen leak and the required supplies are consumed.";
            case "Wrong facility type is rejected" ->
                    "Checks that an emergency requiring a laboratory rejects a habitat.";
            case "Invalid resource amount is rejected" ->
                    "Checks that a facility rejects a non-finite supply amount.";
            case "Zero simulation time is rejected" ->
                    "Checks that the simulation rejects an advance of zero time units.";
            case "Repeated mission submission is rejected safely" ->
                    "Checks that resubmitting an active mission leaves its original assignment intact.";
            default -> "Shows the required inputs and expected result for this scenario.";
        };
    }

    private static String humanizeScenarioText(String value) {
        return value.replace("IllegalArgumentException", "invalid input")
                .replace("IllegalStateException", "invalid mission status")
                .replace("; state ASSIGNED", "; assigned")
                .replace("state ASSIGNED", "assigned")
                .replace("FAILED", "failed")
                .replace("COMPLETED", "completed")
                .replace("INPROGRESS", "in progress")
                .replace("DUST_STORM", "dust storm")
                .replace("ASSIGNED", "assigned")
                .replace("RESOLVED", "resolved")
                .replace("oxygen", "oxygen");
    }

    private List<TestCase> buildTestCases() {
        List<TestCase> cases = new ArrayList<>();

        cases.add(new TestCase("Valid exploration submission",
                "Scientist + equipped rover; sufficient oxygen",
                "Accepted; state ASSIGNED; mission active", () -> {
            Fixture fixture = new Fixture(8.0);
            List<Agent> team = fixture.addExplorationTeam();
            Mission mission = fixture.explorationMission(2.0, 1);
            boolean accepted = fixture.colony.submitMission(mission, team);
            String actual = "accepted=" + accepted + ", state=" + mission.getState()
                    + ", active=" + (fixture.colony.getActiveMission() == mission);
            return check(accepted
                    && mission.getState() == Mission_Emergency_States.ASSIGNED
                    && fixture.colony.getActiveMission() == mission, actual);
        }));

        cases.add(new TestCase("Unavailable agent is rejected",
                "Scientist constructed unavailable",
                "Rejected; FAILED; agent remains unavailable", () -> {
            Fixture fixture = new Fixture(8.0);
            Scientist scientist = fixture.addScientist(false);
            Mission mission = fixture.explorationMission(1.0, 1);
            boolean accepted = fixture.colony.submitMission(mission, List.of(scientist));
            String actual = "accepted=" + accepted + ", state=" + mission.getState()
                    + ", reason=" + mission.getFailureReason()
                    + ", available=" + scientist.isAvailable();
            return check(!accepted && mission.getState() == Mission_Emergency_States.FAILED
                    && !scientist.isAvailable(), actual);
        }));

        cases.add(new TestCase("Unsuitable personnel is rejected",
                "Mechanical engineer submitted to Exploration",
                "Rejected with FAILED mission; no active mission", () -> {
            Fixture fixture = new Fixture(8.0);
            Engineer engineer = fixture.addEngineer(EnumSet.of(Skills.MECHANICAL));
            Mission mission = fixture.explorationMission(1.0, 1);
            boolean accepted = fixture.colony.submitMission(mission, List.of(engineer));
            String actual = "accepted=" + accepted + ", state=" + mission.getState()
                    + ", reason=" + mission.getFailureReason()
                    + ", active=" + (fixture.colony.getActiveMission() == mission);
            return check(!accepted && mission.getState() == Mission_Emergency_States.FAILED
                    && fixture.colony.getActiveMission() == null, actual);
        }));

        cases.add(new TestCase("Duplicate agent is rejected",
                "Same scientist appears twice in submission team",
                "Rejected; scientist remains available", () -> {
            Fixture fixture = new Fixture(8.0);
            Scientist scientist = fixture.addScientist(true);
            Mission mission = fixture.explorationMission(1.0, 1);
            boolean accepted = fixture.colony.submitMission(
                    mission, List.of(scientist, scientist));
            String actual = "accepted=" + accepted + ", state=" + mission.getState()
                    + ", available=" + scientist.isAvailable()
                    + ", reason=" + mission.getFailureReason();
            return check(!accepted && scientist.isAvailable()
                    && mission.getState() == Mission_Emergency_States.FAILED, actual);
        }));

        cases.add(new TestCase("Missing robot capability is rejected",
                "Rover lacks EXPLORATION capability required by mission",
                "Rejected with FAILED mission", () -> {
            Fixture fixture = new Fixture(8.0);
            Scientist scientist = fixture.addScientist(true);
            ExplorationRover rover = fixture.addRover(
                    EnumSet.of(Capability.SURFACE_TRAVEL,
                            Capability.EQUIPMENT_CARRYING));
            Mission mission = fixture.explorationMission(1.0, 1);
            boolean accepted = fixture.colony.submitMission(
                    mission, List.of(scientist, rover));
            String actual = "accepted=" + accepted + ", state=" + mission.getState()
                    + ", reason=" + mission.getFailureReason();
            return check(!accepted && mission.getState() == Mission_Emergency_States.FAILED,
                    actual);
        }));

        cases.add(new TestCase("Insufficient resources are not consumed",
                "Mission requires 5 oxygen; habitat has 2",
                "Rejected; oxygen remains 2", () -> {
            Fixture fixture = new Fixture(2.0);
            List<Agent> team = fixture.addExplorationTeam();
            Mission mission = fixture.explorationMission(5.0, 1);
            double before = fixture.habitat.getResourceAmount(Resources.OXYGEN);
            boolean accepted = fixture.colony.submitMission(mission, team);
            double after = fixture.habitat.getResourceAmount(Resources.OXYGEN);
            String actual = "accepted=" + accepted + ", state=" + mission.getState()
                    + ", oxygen=" + before + "->" + after;
            return check(!accepted && before == after
                    && mission.getState() == Mission_Emergency_States.FAILED, actual);
        }));

        cases.add(new TestCase("One active mission at a time",
                "Submit a second exploration while the first is active",
                "First remains active; second is FAILED", () -> {
            Fixture fixture = new Fixture(10.0);
            List<Agent> team = fixture.addExplorationTeam();
            Mission first = fixture.explorationMission(1.0, 2);
            Mission second = fixture.explorationMission(1.0, 1);
            boolean firstAccepted = fixture.colony.submitMission(first, team);
            boolean secondAccepted = fixture.colony.submitMission(second, team);
            String actual = "first=" + firstAccepted + ", second=" + secondAccepted
                    + ", activeIsFirst=" + (fixture.colony.getActiveMission() == first)
                    + ", secondState=" + second.getState();
            return check(firstAccepted && !secondAccepted
                    && fixture.colony.getActiveMission() == first
                    && second.getState() == Mission_Emergency_States.FAILED, actual);
        }));

        cases.add(new TestCase("Rescue team composition is enforced",
                "Capable rover submitted without communication-skilled personnel",
                "Rejected because the team is incomplete", () -> {
            Fixture fixture = new Fixture(8.0);
            ExplorationRover rover = fixture.addRover(EnumSet.of(Capability.SURFACE_TRAVEL));
            Mission rescue = new Rescue(1, fixture.habitat, Map.of(), Set.of());
            boolean accepted = fixture.colony.submitMission(rescue, List.of(rover));
            String actual = "accepted=" + accepted + ", state=" + rescue.getState()
                    + ", reason=" + rescue.getFailureReason();
            return check(!accepted && rescue.getState() == Mission_Emergency_States.FAILED,
                    actual);
        }));

        cases.add(new TestCase("Valid Rescue team is accepted",
                "Communication-skilled engineer + surface vehicle",
                "Accepted; state ASSIGNED", () -> {
            Fixture fixture = new Fixture(8.0);
            Engineer engineer = fixture.addEngineer(
                    EnumSet.of(Skills.MECHANICAL, Skills.COMMUNICATION));
            ExplorationRover rover = fixture.addRover(EnumSet.of(Capability.SURFACE_TRAVEL));
            Mission rescue = new Rescue(1, fixture.habitat, Map.of(), Set.of());
            boolean accepted = fixture.colony.submitMission(rescue, List.of(engineer, rover));
            String actual = "accepted=" + accepted + ", state=" + rescue.getState()
                    + ", active=" + (fixture.colony.getActiveMission() == rescue);
            return check(accepted && rescue.getState() == Mission_Emergency_States.ASSIGNED,
                    actual);
        }));

        cases.add(new TestCase("Dust storm blocks exploration",
                "Valid team submits during DUST_STORM",
                "Rejected with environmental failure reason", () -> {
            Fixture fixture = new Fixture(8.0);
            fixture.colony.setMarsEnvironment(
                    new MarsEnvironment(EnviromentStates.DUST_STORM));
            List<Agent> team = fixture.addExplorationTeam();
            Mission mission = fixture.explorationMission(1.0, 1);
            boolean accepted = fixture.colony.submitMission(mission, team);
            String actual = "accepted=" + accepted + ", state=" + mission.getState()
                    + ", reason=" + mission.getFailureReason();
            return check(!accepted && mission.getState() == Mission_Emergency_States.FAILED,
                    actual);
        }));

        cases.add(new TestCase("Successful mission completes and releases agents",
                "Submit, start, advance one time unit",
                "COMPLETED; agents available; active mission cleared", () -> {
            Fixture fixture = new Fixture(8.0);
            List<Agent> team = fixture.addExplorationTeam();
            Mission mission = fixture.explorationMission(1.0, 1);
            boolean submitted = fixture.colony.submitMission(mission, team);
            boolean started = fixture.colony.startActiveMission();
            Simulation.advanceTime(fixture.colony, 1);
            boolean allAvailable = team.stream().allMatch(Agent::isAvailable);
            String actual = "submitted=" + submitted + ", started=" + started
                    + ", state=" + mission.getState() + ", agentsAvailable=" + allAvailable
                    + ", active=" + (fixture.colony.getActiveMission() == mission);
            return check(submitted && started
                    && mission.getState() == Mission_Emergency_States.COMPLETED
                    && allAvailable && fixture.colony.getActiveMission() == null, actual);
        }));

        cases.add(new TestCase("Failed active mission releases agents",
                "Submit, start, fail through Colony",
                "FAILED; agents available; active mission cleared", () -> {
            Fixture fixture = new Fixture(8.0);
            List<Agent> team = fixture.addExplorationTeam();
            Mission mission = fixture.explorationMission(1.0, 2);
            boolean submitted = fixture.colony.submitMission(mission, team);
            boolean started = fixture.colony.startActiveMission();
            boolean failed = fixture.colony.failActiveMission("Scenario failure");
            boolean allAvailable = team.stream().allMatch(Agent::isAvailable);
            String actual = "submitted=" + submitted + ", started=" + started
                    + ", failed=" + failed + ", state=" + mission.getState()
                    + ", agentsAvailable=" + allAvailable;
            return check(submitted && started && failed
                    && mission.getState() == Mission_Emergency_States.FAILED
                    && allAvailable && fixture.colony.getActiveMission() == null, actual);
        }));

        cases.add(new TestCase("Mission cannot start before association",
                "Call startMission on a new mission",
                "IllegalStateException from the domain method", () ->
                expectException(IllegalStateException.class,
                        () -> new Fixture(5.0).explorationMission(1.0, 1).startMission())));

        cases.add(new TestCase("Mission cannot complete before it starts",
                "Submit mission; call finishActiveMission while ASSIGNED",
                "IllegalStateException; active mission remains assigned", () -> {
            Fixture fixture = new Fixture(8.0);
            List<Agent> team = fixture.addExplorationTeam();
            Mission mission = fixture.explorationMission(1.0, 1);
            fixture.colony.submitMission(mission, team);
            TestObservation exception = expectException(IllegalStateException.class,
                    () -> fixture.colony.finishActiveMission(true));
            boolean intact = fixture.colony.getActiveMission() == mission
                    && mission.getState() == Mission_Emergency_States.ASSIGNED;
            String actual = exception.actual() + ", assignmentIntact=" + intact;
            return new TestObservation(exception.passed() && intact, actual,
                    exception.exceptionType(), exception.exceptionMessage());
        }));

        cases.add(new TestCase("Oxygen leak uses real emergency response",
                "Engineer + repair robot; habitat has oxygen and spare parts",
                "Resolved; oxygen and spare parts decrease; responders released", () -> {
            Fixture fixture = new Fixture(10.0);
            Engineer engineer = fixture.addEngineer(EnumSet.of(Skills.MECHANICAL));
            RepairRobot robot = new RepairRobot(100.0, "Operational", "Habitat-Test", true);
            fixture.colony.addRobot(robot);
            OxygenLeak leak = new OxygenLeak(fixture.habitat, 1.0);
            boolean handled = fixture.colony.handleEmergency(leak);
            String actual = "handled=" + handled + ", status=" + leak.getStatus()
                    + ", repaired=" + leak.isRepaired()
                    + ", oxygen=" + fixture.habitat.getResourceAmount(Resources.OXYGEN)
                    + ", spareParts=" + fixture.habitat.getResourceAmount(Resources.SPARE_PARTS)
                    + ", respondersAvailable=" + engineer.isAvailable() + "/" + robot.isAvailable();
            return check(handled && leak.isRepaired()
                    && leak.getStatus() == EmergencyStatus.RESOLVED
                    && fixture.habitat.getResourceAmount(Resources.OXYGEN) == 9.0
                    && fixture.habitat.getResourceAmount(Resources.SPARE_PARTS) == 4.0
                    && engineer.isAvailable() && robot.isAvailable(), actual);
        }));

        cases.add(new TestCase("Wrong facility type is rejected",
                "Create Fire with a Habitat instead of a Laboratory",
                "IllegalArgumentException from Fire constructor", () ->
                expectException(IllegalArgumentException.class,
                        () -> new Fire(new Fixture(5.0).habitat))));

        cases.add(new TestCase("Invalid resource amount is rejected",
                "Add NaN oxygen to a facility",
                "IllegalArgumentException from facility API", () ->
                expectException(IllegalArgumentException.class,
                        () -> new Fixture(5.0).habitat.addResource(Resources.OXYGEN,
                                Double.NaN))));

        cases.add(new TestCase("Zero simulation time is rejected",
                "Advance a colony by zero time units",
                "IllegalArgumentException from Simulation", () ->
                expectException(IllegalArgumentException.class,
                        () -> Simulation.advanceTime(new Colony(), 0))));

        cases.add(new TestCase("Repeated mission submission is rejected safely",
                "Submit the same mission twice",
                "Second submission rejected; original remains active", () -> {
            Fixture fixture = new Fixture(8.0);
            List<Agent> team = fixture.addExplorationTeam();
            Mission mission = fixture.explorationMission(1.0, 1);
            boolean first = fixture.colony.submitMission(mission, team);
            boolean second = fixture.colony.submitMission(mission, team);
            String actual = "first=" + first + ", second=" + second
                    + ", state=" + mission.getState()
                    + ", active=" + (fixture.colony.getActiveMission() == mission);
            return check(first && !second
                    && mission.getState() == Mission_Emergency_States.ASSIGNED
                    && fixture.colony.getActiveMission() == mission, actual);
        }));

        return cases;
    }

    private static TestObservation check(boolean passed, String actual) {
        return new TestObservation(passed, actual, "", "");
    }

    private static TestObservation expectException(
            Class<? extends Throwable> expectedType, ThrowingAction action) {
        try {
            action.run();
            return new TestObservation(false, "No exception was thrown", "", "");
        } catch (Throwable actual) {
            boolean passed = expectedType.isInstance(actual);
            String message = messageOf(actual);
            return new TestObservation(passed,
                    actual.getClass().getSimpleName() + ": " + message,
                    actual.getClass().getSimpleName(), message);
        }
    }

    private static String messageOf(Throwable throwable) {
        return throwable.getMessage() == null ? "(no message)" : throwable.getMessage();
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface TestAction {
        TestObservation run() throws Exception;
    }

    private record TestCase(
            String name, String input, String expected, TestAction action) { }

    private record TestObservation(
            boolean passed, String actual, String exceptionType, String exceptionMessage) { }

    private static final class Fixture {
        private final Colony colony = new Colony();
        private final Habitat habitat;

        private Fixture(double oxygen) {
            habitat = new Habitat("Habitat-Test", 25.0,
                    OperationalStatus.OPERATIONAL,
                    Map.of(Resources.OXYGEN, oxygen,
                            Resources.SPARE_PARTS, 5.0),
                    List.of(), List.of());
            colony.addFacility(habitat);
        }

        private Scientist addScientist(boolean available) {
            Scientist scientist = new Scientist("Test Scientist", 1,
                    "Healthy", "Habitat-Test",
                    EnumSet.of(Skills.SCIENTIFIC), 0.0, available);
            colony.addPersonnel(scientist);
            return scientist;
        }

        private Engineer addEngineer(Set<Skills> skills) {
            Engineer engineer = new Engineer("Test Engineer", 2,
                    "Healthy", "Habitat-Test", skills, 0.0, true);
            colony.addPersonnel(engineer);
            return engineer;
        }

        private ExplorationRover addRover(Set<Capability> capabilities) {
            ExplorationRover rover = new ExplorationRover(
                    100.0, "Operational", "Habitat-Test", true, capabilities);
            rover.setEquipment(List.of(new Equipment(
                    "Test Survey Kit", OperationalStatus.OPERATIONAL)));
            colony.addRobot(rover);
            return rover;
        }

        private List<Agent> addExplorationTeam() {
            Scientist scientist = addScientist(true);
            ExplorationRover rover = addRover(EnumSet.of(
                    Capability.SURFACE_TRAVEL,
                    Capability.EQUIPMENT_CARRYING,
                    Capability.EXPLORATION));
            return List.of(scientist, rover);
        }

        private Mission explorationMission(double oxygen, int duration) {
            return new Exploration(duration, habitat,
                    Map.of(Resources.OXYGEN, oxygen),
                    EnumSet.of(Capability.SURFACE_TRAVEL,
                            Capability.EQUIPMENT_CARRYING,
                            Capability.EXPLORATION),
                    "Test Ridge");
        }
    }
}
