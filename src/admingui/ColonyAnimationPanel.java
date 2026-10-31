package admingui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import agents.Agent;
import colony.Colony;
import emergencies.Emergency;
import enums.EmergencyStatus;
import enums.EmergencyType;
import enums.EnviromentStates;
import enums.Mission_Emergency_States;
import enums.OperationalStatus;
import facilities.Facilities;
import marsenvironment.MarsEnvironment;
import missions.Mission;
import personnel.Personnel;
import roboticunit.RoboticUnit;

/**
 * Always-on-screen "live video" of the colony: it re-reads the live Colony on
 * every paint, so whatever the simulation is doing (missions progressing,
 * emergencies flaring up, dust storms rolling in, an admin editing a record)
 * shows up immediately as little animated characters, vehicles and effects.
 * Purely cosmetic - it never mutates any domain object, only reads from it.
 */
final class ColonyAnimationPanel extends javax.swing.JPanel {

    private final SimulationContext context;
    private long phase;
    private final List<Object[]> recentEmergencyFlashes = new ArrayList<>();
    private final List<SimulationContext.Effect> effects = new ArrayList<>();
    private final Random random = new Random(7);
    private final List<double[]> stormParticles = new ArrayList<>();
    private final List<double[]> dustTrail = new ArrayList<>();
    private final IdentityHashMap<Mission, Double> arrivalProgress = new IdentityHashMap<>();

    ColonyAnimationPanel(SimulationContext context) {
        this.context = context;
        setPreferredSize(new java.awt.Dimension(330, 10));
        setBackground(new Color(40, 16, 10));
        for (int i = 0; i < 70; i++) {
            stormParticles.add(new double[] {
                    random.nextDouble(), random.nextDouble(), 0.3 + random.nextDouble() * 0.7 });
        }
    }

    void flashEmergency(Object location) {
        recentEmergencyFlashes.add(new Object[] { System.nanoTime(), location });
    }

    void addEffect(SimulationContext.Effect effect) {
        effects.add(effect);
    }

    void tick() {
        phase++;
        long now = System.nanoTime();
        recentEmergencyFlashes.removeIf(entry -> (now - (long) entry[0]) > 2_500_000_000L);
        effects.removeIf(effect -> (now - effect.startNanos()) > 1_900_000_000L);
        dustTrail.removeIf(p -> {
            p[2] -= 1;
            return p[2] <= 0;
        });

        Mission active = context.colony().getActiveMission();
        if (active != null && active.getState() == Mission_Emergency_States.INPROGRESS) {
            double current = arrivalProgress.getOrDefault(active, 0.0);
            if (current < 1.0) {
                arrivalProgress.put(active, Math.min(1.0, current + 0.035));
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        Colony colony = context.colony();
        MarsEnvironment env = context.environment();
        boolean duststorm = env != null && env.GetEnvironmentState() == EnviromentStates.DUST_STORM;

        paintSky(g2, w, h, duststorm);
        paintTitle(g2, w);

        String banner = recentNonFacilityBanner();
        int top = 58;
        if (banner != null) {
            double pulse = 0.5 + 0.5 * Math.sin(phase * 0.3);
            g2.setColor(new Color(255, 60, 60, (int) (170 + 85 * pulse)));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("⚠ " + banner, 12, 56);
            top = 72;
        }

        Mission activeMission = colony.getActiveMission();
        Set<Agent> awayOnMission = activeMission != null && activeMission.getState() == Mission_Emergency_States.INPROGRESS
                ? new HashSet<>(activeMission.getAgents())
                : Set.of();
        Map<String, Integer> facilityRowCenters = new LinkedHashMap<>();

        int bottom = h - 92;
        List<Facilities> facilities = colony.getFacilities();
        paintFacilities(g2, facilities, colony, w, top, bottom, awayOnMission, facilityRowCenters);
        paintMissionLane(g2, activeMission, colony, w, h);
        paintTravelingCrew(g2, activeMission, facilityRowCenters, w, h);
        paintEnvironmentOverlay(g2, env, w, h);
        paintEffects(g2, w, h);
        paintFooter(g2, colony, env, w, h);
    }

    // ---------------------------------------------------------------- sky

    private void paintSky(Graphics2D g2, int w, int h, boolean duststorm) {
        Color top = duststorm ? new Color(120, 60, 20) : new Color(255, 170, 110);
        Color mid = duststorm ? new Color(90, 40, 20) : new Color(214, 92, 58);
        g2.setPaint(new GradientPaint(0, 0, top, 0, h * 0.55f, mid));
        g2.fillRect(0, 0, w, (int) (h * 0.55));
        g2.setPaint(new GradientPaint(0, h * 0.5f, new Color(0x7A, 0x2E, 0x18), 0, h, new Color(0x2A, 0x10, 0x08)));
        g2.fillRect(0, (int) (h * 0.5), w, (int) (h * 0.5));

        // sun / glow
        float sunX = w - 46, sunY = 34;
        RadialGradientPaint sunGlow = new RadialGradientPaint(sunX, sunY, 40,
                new float[] { 0f, 1f }, new Color[] { new Color(255, 230, 180, 220), new Color(255, 230, 180, 0) });
        g2.setPaint(sunGlow);
        g2.fillOval((int) (sunX - 40), (int) (sunY - 40), 80, 80);
        g2.setColor(new Color(255, 244, 214));
        g2.fillOval((int) (sunX - 12), (int) (sunY - 12), 24, 24);

        // twinkling stars, upper-left
        g2.setColor(Color.WHITE);
        for (int i = 0; i < 10; i++) {
            double tw = 0.5 + 0.5 * Math.sin(phase * 0.1 + i * 2.1);
            int sx = 10 + (i * 17) % 140;
            int sy = 6 + (i * 11) % 28;
            g2.setColor(new Color(255, 255, 255, (int) (80 * tw) + 40));
            g2.fillOval(sx, sy, 2, 2);
        }

        // distant mountain silhouette along the horizon
        Path2D mountains = new Path2D.Double();
        int horizonY = (int) (h * 0.52);
        mountains.moveTo(0, horizonY);
        for (int x = 0; x <= w; x += 20) {
            double ridge = Math.sin(x * 0.04 + 3) * 10 + Math.sin(x * 0.012) * 16;
            mountains.lineTo(x, horizonY - 14 - ridge);
        }
        mountains.lineTo(w, horizonY);
        mountains.closePath();
        g2.setColor(new Color(110, 45, 28, 180));
        g2.fill(mountains);
    }

    private void paintTitle(Graphics2D g2, int w) {
        g2.setColor(new Color(255, 236, 214));
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString("🛰 COLONY LIVE VIEW", 12, 22);
        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(new Color(255, 200, 140, 160));
        g2.drawLine(10, 30, w - 10, 30);
    }

    // ---------------------------------------------------------- facilities

    private void paintFacilities(Graphics2D g2, List<Facilities> facilities, Colony colony, int w, int top, int bottom,
            Set<Agent> awayOnMission, Map<String, Integer> facilityRowCenters) {
        if (facilities.isEmpty()) {
            g2.setColor(new Color(255, 255, 255, 190));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString("No facilities yet. Add one from the Facilities tab.", 14, top + 20);
            return;
        }
        int margin = 10;
        int boxW = w - margin * 2;
        int available = bottom - top;
        int boxH = Math.max(34, Math.min(62, available / facilities.size()));
        int gap = facilities.size() > 1 ? Math.max(5, (available - boxH * facilities.size()) / (facilities.size() - 1)) : 0;
        int y = top;

        List<Emergency> emergencies = colony.getEmergencies();

        for (Facilities facility : facilities) {
            facilityRowCenters.put(facility.getLocation(), y + boxH / 2);

            drawGroundStrip(g2, margin - 4, y + boxH - 6, boxW + 8, 14);
            drawBuilding(g2, facility, margin, y, boxW, boxH);

            boolean flagged = isFacilityFlagged(facility, emergencies);
            if (flagged) {
                drawAlertRing(g2, margin, y, boxW, boxH);
                drawEmergencyGlyph(g2, facility, emergencies, margin + boxW - 24, y - 4);
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(Color.WHITE);
            g2.drawString(truncate(facility.getLocation(), 22), margin + 34, y + 14);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2.setColor(new Color(255, 255, 255, 210));
            g2.drawString(facility.getClass().getSimpleName() + " · " + facility.getOperationalStatus(),
                    margin + 34, y + boxH - 8);

            paintAgentsNear(g2, colony, facility.getLocation(), margin + boxW - 92, y + boxH - 20, 86, boxH - 24,
                    awayOnMission);

            y += boxH + gap;
        }
    }

    private void drawGroundStrip(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(60, 24, 14, 160));
        g2.fillRoundRect(x, y, w, h, 6, 6);
    }

    private void drawBuilding(Graphics2D g2, Facilities facility, int x, int y, int w, int h) {
        Color tint = statusColor(facility.getOperationalStatus());
        Color light = tint.brighter();
        Color dark = tint.darker();

        RoundRectangle2D hull = new RoundRectangle2D.Double(x, y, w, h, 10, 10);
        g2.setPaint(new GradientPaint(x, y, light, x, y + h, dark));
        g2.fill(hull);
        g2.setColor(new Color(0, 0, 0, 90));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(hull);

        // a small icon on the left identifying the facility type
        int ix = x + 10;
        int iy = y + h / 2;
        g2.setColor(new Color(255, 255, 255, 235));
        String type = facility.getClass().getSimpleName();
        switch (type) {
            case "Habitat" -> {
                g2.fillArc(ix - 10, iy - 10, 20, 20, 0, 180);
                g2.fillRect(ix - 10, iy, 20, 4);
            }
            case "Laboratory" -> {
                g2.fillRect(ix - 9, iy - 6, 18, 12);
                g2.drawLine(ix, iy - 6, ix, iy - 14);
                g2.fillOval(ix - 2, iy - 17, 4, 4);
            }
            case "PowerStation" -> {
                g2.fillRect(ix - 2, iy - 12, 4, 20);
                g2.fillRect(ix - 10, iy - 12, 20, 4);
                g2.fillRect(ix - 10, iy - 4, 20, 4);
            }
            case "StorageFacility" -> g2.fillRoundRect(ix - 10, iy - 10, 20, 20, 4, 4);
            case "CommunicationStation" -> {
                g2.drawLine(ix, iy + 10, ix, iy - 6);
                g2.drawArc(ix - 10, iy - 16, 20, 20, 20, 140);
            }
            default -> g2.fillOval(ix - 8, iy - 8, 16, 16);
        }

        // blinking status light
        boolean on = (phase / 8 + x) % 2 == 0;
        g2.setColor(on ? new Color(255, 255, 200, 230) : new Color(255, 255, 200, 70));
        g2.fillOval(x + w - 14, y + 6, 6, 6);
    }

    private void drawAlertRing(Graphics2D g2, int x, int y, int w, int h) {
        double pulse = 0.5 + 0.5 * Math.sin(phase * 0.3);
        RoundRectangle2D box = new RoundRectangle2D.Double(x, y, w, h, 10, 10);
        g2.setStroke(new BasicStroke((float) (2 + 2 * pulse)));
        g2.setColor(new Color(255, 40, 40, (int) (140 + 100 * pulse)));
        g2.draw(box);
    }

    private void drawEmergencyGlyph(Graphics2D g2, Facilities facility, List<Emergency> emergencies, int x, int y) {
        EmergencyType activeType = null;
        for (Emergency emergency : emergencies) {
            if (matchesFacility(emergency.getLocation(), facility) && emergency.getStatus() == EmergencyStatus.UNRESOLVED) {
                activeType = emergency.getType();
                break;
            }
        }
        if (activeType == null && !recentEmergencyFlashes.isEmpty()) {
            for (Object[] flash : recentEmergencyFlashes) {
                if (matchesFacility(flash[1], facility)) {
                    activeType = EmergencyType.FIRE;
                }
            }
        }
        if (activeType == null) {
            return;
        }
        switch (activeType) {
            case FIRE -> drawFlame(g2, x, y);
            case OXYGEN_LEAK -> drawLeakSwirl(g2, x, y);
            case MEDICAL -> drawMedicalCross(g2, x, y);
            default -> drawWrench(g2, x, y);
        }
    }

    private void drawFlame(Graphics2D g2, int x, int y) {
        double flick = Math.sin(phase * 0.5) * 2;
        Path2D flame = new Path2D.Double();
        flame.moveTo(x, y + 14);
        flame.curveTo(x - 8, y + 4, x - 2 + flick, y - 10, x, y - 16);
        flame.curveTo(x + 2 - flick, y - 10, x + 8, y + 4, x, y + 14);
        flame.closePath();
        g2.setColor(new Color(255, 90, 20, 230));
        g2.fill(flame);
        g2.setColor(new Color(255, 210, 60, 220));
        g2.fillOval(x - 3, y, 6, 8);
    }

    private void drawLeakSwirl(Graphics2D g2, int x, int y) {
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(120, 220, 255, 190));
        Path2D swirl = new Path2D.Double();
        swirl.moveTo(x - 8, y + 10);
        for (int i = 0; i <= 10; i++) {
            double t = i / 10.0;
            swirl.lineTo(x - 8 + t * 16, y + 10 - t * 16 + Math.sin(phase * 0.3 + t * 6) * 3);
        }
        g2.draw(swirl);
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.drawString("O₂", x - 6, y + 22);
    }

    private void drawMedicalCross(Graphics2D g2, int x, int y) {
        double pulse = 0.6 + 0.4 * Math.sin(phase * 0.4);
        g2.setColor(new Color(255, 60, 70, (int) (200 * pulse) + 50));
        g2.fillRoundRect(x - 2, y - 10, 4, 16, 1, 1);
        g2.fillRoundRect(x - 8, y - 4, 16, 4, 1, 1);
    }

    private void drawWrench(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(200, 200, 200, 220));
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(x - 6, y + 8, x + 6, y - 8);
        g2.fillOval(x - 9, y + 5, 8, 8);
        g2.fillOval(x + 3, y - 11, 8, 8);
        // small grey smoke puffs
        for (int i = 0; i < 3; i++) {
            double puff = (phase * 0.4 + i * 2) % 10;
            g2.setColor(new Color(180, 180, 180, (int) (150 - puff * 12)));
            g2.fillOval(x - 2 + i * 3, (int) (y - 14 - puff * 2), 6, 6);
        }
    }

    private boolean isFacilityFlagged(Facilities facility, List<Emergency> emergencies) {
        for (Emergency emergency : emergencies) {
            if (matchesFacility(emergency.getLocation(), facility) && emergency.getStatus() == EmergencyStatus.UNRESOLVED) {
                return true;
            }
        }
        for (Object[] flash : recentEmergencyFlashes) {
            if (matchesFacility(flash[1], facility)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesFacility(Object location, Facilities facility) {
        if (location == facility) {
            return true;
        }
        return location instanceof String text && text.equals(facility.getLocation());
    }

    private String recentNonFacilityBanner() {
        for (int i = recentEmergencyFlashes.size() - 1; i >= 0; i--) {
            Object location = recentEmergencyFlashes.get(i)[1];
            if (location instanceof String text) {
                return text;
            }
        }
        return null;
    }

    // ------------------------------------------------------------- agents

    private void paintAgentsNear(Graphics2D g2, Colony colony, String location, int x, int baseY, int areaW, int areaH,
            Set<Agent> awayOnMission) {
        List<Agent> here = new ArrayList<>();
        for (Agent agent : colony.getPersonnel()) {
            if (location.equals(agent.getCurrentLocation()) && !awayOnMission.contains(agent)) {
                here.add(agent);
            }
        }
        for (Agent agent : colony.getRoboticUnits()) {
            if (location.equals(agent.getCurrentLocation()) && !awayOnMission.contains(agent)) {
                here.add(agent);
            }
        }
        int shown = Math.min(here.size(), 4);
        for (int i = 0; i < shown; i++) {
            Agent agent = here.get(i);
            double walk = Math.sin(phase * 0.2 + i * 1.9 + location.hashCode());
            int dotX = x + (i * 20) % Math.max(1, areaW);
            boolean busy = !agent.isAvailable();
            if (agent instanceof RoboticUnit unit) {
                drawRobotSprite(g2, dotX, baseY, walk, unit, busy);
            } else if (agent instanceof Personnel person) {
                drawPersonSprite(g2, dotX, baseY, walk, person, busy);
            }
        }
        if (here.size() > shown) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2.drawString("+" + (here.size() - shown), x + areaW - 4, baseY + 4);
        }
    }

    private void drawPersonSprite(Graphics2D g2, int x, int y, double walk, Personnel person, boolean busy) {
        Color suit = roleColor(person);
        int legSwing = (int) (walk * 3);
        g2.setColor(suit.darker());
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x - 3, y + 6, x - 3 + legSwing, y + 13);
        g2.drawLine(x + 3, y + 6, x + 3 - legSwing, y + 13);
        g2.setColor(suit);
        g2.fillRoundRect(x - 4, y - 2, 8, 9, 3, 3);
        g2.setColor(new Color(255, 214, 170));
        g2.fillOval(x - 3, y - 9, 6, 6);
        if (busy) {
            g2.setColor(new Color(255, 255, 255, 160));
            g2.fillOval(x - 1, y - 14, 2, 2);
        } else {
            double bob = Math.sin(phase * 0.3) * 1.2;
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillOval(x - 1, (int) (y - 13 + bob), 2, 2);
        }
    }

    private void drawRobotSprite(Graphics2D g2, int x, int y, double walk, RoboticUnit unit, boolean busy) {
        String kind = unit.getClass().getSimpleName();
        Color body = switch (kind) {
            case "ExplorationRover" -> new Color(255, 170, 60);
            case "RepairRobot" -> new Color(170, 180, 190);
            default -> new Color(140, 210, 255);
        };
        g2.setColor(busy ? new Color(body.getRed(), body.getGreen(), body.getBlue(), 160) : body);
        switch (kind) {
            case "ExplorationRover" -> {
                g2.fillRoundRect(x - 7, y - 3, 14, 7, 2, 2);
                double spin = (phase * (busy ? 4 : 1)) % 360;
                g2.setColor(Color.DARK_GRAY);
                g2.fillOval(x - 6, y + 2, 4, 4);
                g2.fillOval(x + 2, y + 2, 4, 4);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.drawLine(x - 4 + (int) (2 * Math.cos(Math.toRadians(spin))),
                        y + 4 + (int) (2 * Math.sin(Math.toRadians(spin))), x - 4, y + 4);
            }
            case "RepairRobot" -> {
                g2.fillRoundRect(x - 5, y - 6, 10, 10, 2, 2);
                double arm = Math.sin(phase * 0.4) * 4;
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(x + 5, y - 2, (int) (x + 9 + arm), y - 6);
            }
            default -> {
                g2.fillOval(x - 5, y - 4, 10, 6);
                double blade = (phase * 6) % 360;
                g2.setStroke(new BasicStroke(1.4f));
                for (int a = 0; a < 4; a++) {
                    double angle = Math.toRadians(blade + a * 90);
                    g2.drawLine(x, y - 6, (int) (x + 7 * Math.cos(angle)), (int) (y - 6 + 7 * Math.sin(angle)));
                }
            }
        }
    }

    private Color roleColor(Personnel person) {
        return switch (person.getClass().getSimpleName()) {
            case "Engineer" -> new Color(255, 180, 50);
            case "Scientist" -> new Color(110, 220, 230);
            case "MedicalOfficer" -> new Color(230, 90, 100);
            default -> new Color(220, 220, 220);
        };
    }

    // --------------------------------------------------------- mission lane

    private void paintMissionLane(Graphics2D g2, Mission mission, Colony colony, int w, int h) {
        int laneY = h - 70;
        drawGroundStrip(g2, 8, laneY - 4, w - 16, 20);
        g2.setColor(new Color(255, 230, 190, 70));
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] { 3, 5 }, 0));
        g2.drawLine(16, laneY, w - 50, laneY);

        // site flag
        g2.setColor(new Color(220, 220, 220));
        g2.fillRect(w - 40, laneY - 18, 2, 18);
        g2.setColor(Theme.SKY);
        g2.fillRect(w - 38, laneY - 18, 10, 7);

        if (mission == null || mission.getState() != Mission_Emergency_States.INPROGRESS) {
            g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
            g2.setColor(new Color(255, 255, 255, 170));
            g2.drawString("No mission currently in progress.", 16, laneY - 10);
            return;
        }

        double span = w - 70;
        double t = (Math.sin(phase * 0.08) + 1) / 2.0;
        int roverX = (int) (18 + t * span);

        if (phase % 2 == 0) {
            dustTrail.add(new double[] { roverX - 6, laneY + 2, 14 });
        }
        for (double[] puff : dustTrail) {
            g2.setColor(new Color(200, 150, 100, (int) (puff[2] * 10)));
            g2.fillOval((int) puff[0], (int) puff[1], (int) (16 - puff[2]), (int) (16 - puff[2]));
        }

        g2.setColor(new Color(255, 120, 80));
        g2.fillRoundRect(roverX - 8, laneY - 6, 16, 8, 3, 3);
        g2.setColor(Color.DARK_GRAY);
        g2.fillOval(roverX - 7, laneY + 1, 5, 5);
        g2.fillOval(roverX + 3, laneY + 1, 5, 5);
        g2.setColor(new Color(255, 220, 190));
        g2.drawLine(roverX, laneY - 6, roverX, laneY - 12);

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(new Color(255, 230, 210));
        String name = mission.getClass().getSimpleName();
        g2.drawString(name + " in progress - " + mission.getElapsedTimeUnits() + "/" + mission.getNeededTimesUnits()
                + " time units", 16, laneY - 12);

        int barW = w - 32;
        int filled = mission.getNeededTimesUnits() <= 0 ? 0
                : (int) (barW * Math.min(1.0, (double) mission.getElapsedTimeUnits() / mission.getNeededTimesUnits()));
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(16, laneY + 10, barW, 6, 3, 3);
        g2.setColor(Theme.LEAF);
        g2.fillRoundRect(16, laneY + 10, filled, 6, 3, 3);
    }

    /**
     * Agents assigned to the active mission walk from their home facility over to wherever the
     * mission is actually happening - the mission's own designated facility when it has one
     * (e.g. the lab a Repair is fixing, the habitat an Exploration launches from), falling back
     * to the roaming site marker on the mission lane for facility-less missions (e.g. a Rescue
     * out on the open surface). They travel there once, then hold position and idle in place.
     */
    private void paintTravelingCrew(Graphics2D g2, Mission mission, Map<String, Integer> facilityRowCenters, int w, int h) {
        if (mission == null || mission.getState() != Mission_Emergency_States.INPROGRESS) {
            return;
        }
        List<Agent> crew = mission.getAgents();
        if (crew.isEmpty()) {
            return;
        }

        int laneY = h - 70;
        int destX;
        int destY;
        Facilities destinationFacility = mission.getFacility();
        Integer destinationRowY = destinationFacility != null ? facilityRowCenters.get(destinationFacility.getLocation()) : null;
        if (destinationRowY != null) {
            destX = w - 70;
            destY = destinationRowY;
        } else {
            double span = w - 70;
            double roam = (Math.sin(phase * 0.08) + 1) / 2.0;
            destX = (int) (18 + roam * span);
            destY = laneY - 14;
        }
        int originX = 46;

        double t = arrivalProgress.getOrDefault(mission, 0.0);
        double ease = t * t * (3 - 2 * t);

        for (int i = 0; i < crew.size(); i++) {
            Agent agent = crew.get(i);
            int originY = facilityRowCenters.getOrDefault(agent.getCurrentLocation(), 70);
            double jitter = ease > 0.95 ? Math.sin(phase * 0.2 + i * 1.6) * 3 : 0;
            int curX = (int) (originX + ease * (destX - originX)) + (i % 3) * 7;
            int curY = (int) (originY + ease * (destY - originY) + jitter) - (i / 3) * 8;

            if (t < 0.98) {
                g2.setColor(new Color(220, 235, 245, 70));
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
                        new float[] { 2, 4 }, 0));
                g2.drawLine(originX, originY, destX, destY);
            }

            double walk = Math.sin(phase * 0.3 + i * 1.7);
            if (agent instanceof RoboticUnit unit) {
                drawRobotSprite(g2, curX, curY, walk, unit, true);
            } else if (agent instanceof Personnel person) {
                drawPersonSprite(g2, curX, curY, walk, person, true);
            }
        }
    }

    // ----------------------------------------------------- environment fx

    private void paintEnvironmentOverlay(Graphics2D g2, MarsEnvironment env, int w, int h) {
        if (env == null) {
            return;
        }
        if (env.GetEnvironmentState() == EnviromentStates.DUST_STORM) {
            g2.setColor(new Color(230, 150, 70, 70));
            for (double[] particle : stormParticles) {
                double x = ((particle[0] * w) + phase * particle[2] * 2) % (w + 40) - 20;
                double y = (particle[1] * h);
                g2.drawLine((int) x, (int) y, (int) (x - 18), (int) (y + 9));
            }
        } else if (env.GetEnvironmentState() == EnviromentStates.COMMUNICATION_DISTRIBUTION) {
            boolean on = (phase / 10) % 2 == 0;
            g2.setColor(on ? new Color(255, 70, 70) : new Color(255, 70, 70, 70));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("⚡ COMMS DISRUPTED", w - 160, 44);
        }
    }

    // ----------------------------------------------------------- effects

    private void paintEffects(Graphics2D g2, int w, int h) {
        for (SimulationContext.Effect effect : effects) {
            long age = System.nanoTime() - effect.startNanos();
            double t = Math.min(1.0, age / 1_900_000_000.0);
            int alpha = (int) (255 * (1 - t));
            if (alpha <= 0) {
                continue;
            }
            int[] spot = locateEffectSpot(effect.location(), w, h);
            int ex = spot[0];
            int ey = spot[1] - (int) (t * 30);
            Color base = effect.color() != null ? effect.color() : Theme.AMBER;
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.max(0, Math.min(255, alpha))));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(effect.text(), ex, ey);
            if (t < 0.25) {
                for (int i = 0; i < 5; i++) {
                    double angle = Math.toRadians(i * 72 + phase * 10);
                    int sx = ex + (int) (10 * Math.cos(angle));
                    int sy = ey + (int) (10 * Math.sin(angle));
                    g2.fillOval(sx, sy, 3, 3);
                }
            }
        }
    }

    private int[] locateEffectSpot(Object location, int w, int h) {
        if (location instanceof Facilities facility) {
            int index = context.colony().getFacilities().indexOf(facility);
            if (index >= 0) {
                int count = Math.max(1, context.colony().getFacilities().size());
                int approxY = 58 + index * ((h - 150) / count) + 20;
                return new int[] { w / 2 - 20, Math.min(h - 100, approxY) };
            }
        }
        return new int[] { w / 2 - 20, h - 85 };
    }

    // ------------------------------------------------------------- footer

    private void paintFooter(Graphics2D g2, Colony colony, MarsEnvironment env, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, h - 24, w, 24);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(Color.WHITE);
        String envText = env == null ? "unknown" : env.GetEnvironmentState().toString();
        int unresolved = (int) colony.getEmergencies().stream()
                .filter(e -> e.getStatus() == EmergencyStatus.UNRESOLVED).count();
        g2.drawString("Env: " + envText + "   Alerts: " + unresolved, 10, h - 8);
    }

    private static Color statusColor(OperationalStatus status) {
        return switch (status) {
            case OPERATIONAL -> new Color(45, 140, 90);
            case DAMAGED -> new Color(185, 130, 30);
            case UNAVAILABLE -> new Color(110, 100, 95);
            case DESTROYED -> new Color(140, 30, 30);
        };
    }

    private static String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }
}
