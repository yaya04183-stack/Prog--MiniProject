package admingui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import enums.EmergencyType;

/**
 * Synthesised soundtrack for the admin console - no audio files needed.
 * <ul>
 *   <li>CALM: a friendly looping melody while everything is going right.</li>
 *   <li>ALERT: a theme that depends on the type of the active emergency.</li>
 *   <li>One-shot effects for mission / emergency outcomes.</li>
 * </ul>
 * If no audio device is available everything silently becomes a no-op.
 */
final class SoundManager {

    enum Effect { MISSION_DONE, MISSION_FAILED, EMERGENCY_STARTED, EMERGENCY_RESOLVED, EMERGENCY_UNRESOLVED }

    private static final float RATE = 44100f;
    private static final AudioFormat FORMAT = new AudioFormat(RATE, 16, 1, true, false);

    // Note frequencies (Hz)
    private static final double C4 = 261.63, D4 = 293.66, E4 = 329.63, G4 = 392.00, A4 = 440.00,
            C5 = 523.25, D5 = 587.33, E5 = 659.25, G5 = 783.99, REST = 0;

    // Piano theme: 8 bars in C major (C - G - Am - F - C - G - F - C), 4 beats per bar.
    private static final double BEAT = 0.55; // seconds per beat (~109 bpm, relaxed)
    // Left-hand chords {root, fifth, third} as MIDI numbers
    private static final int[][] CHORDS = {
        {48, 55, 64}, {43, 55, 59}, {45, 52, 60}, {41, 53, 57},
        {48, 55, 64}, {43, 55, 59}, {41, 53, 57}, {48, 55, 64},
    };
    // Right-hand melody {midi, beats}; a midi of 0 is a rest
    private static final int[][] MELODY = {
        {76, 1}, {79, 1}, {76, 1}, {72, 1},
        {74, 2}, {71, 1}, {74, 1},
        {72, 1}, {76, 1}, {72, 1}, {69, 1},
        {69, 1}, {72, 1}, {77, 2},
        {76, 1}, {79, 1}, {84, 1}, {79, 1},
        {79, 1}, {74, 1}, {71, 1}, {74, 1},
        {72, 1}, {69, 1}, {72, 1}, {77, 1},
        {76, 1}, {72, 1}, {72, 2},
    };
    private final byte[] pianoSong = renderPianoSong();
    /** Calm theme: assets/theme.wav (44.1 kHz mono 16-bit) if present, else the built-in piano piece. */
    private final byte[] calmTheme = loadThemeFile(pianoSong);

    private volatile boolean muted = false;
    private volatile EmergencyType alertType = null; // null => calm
    private volatile boolean running = true;
    private SourceDataLine musicLine;

    SoundManager() {
        System.out.println("[SoundManager] started");
        play(Effect.MISSION_DONE); // startup chime so you can tell sound works
        Thread t = new Thread(this::musicLoop, "mars-music");
        t.setDaemon(true);
        t.start();
    }

    boolean isMuted() {
        return muted;
    }

    void setMuted(boolean muted) {
        this.muted = muted;
        if (muted && musicLine != null) {
            musicLine.flush();
        }
    }

    /** Pass the type of the currently active emergency, or null when all is calm. */
    void setAlert(EmergencyType type) {
        this.alertType = type;
    }

    void shutdown() {
        running = false;
    }

    // ---------------------------------------------------------------- music

    private void musicLoop() {
        try {
            musicLine = AudioSystem.getSourceDataLine(FORMAT);
            musicLine.open(FORMAT, 4096);
            musicLine.start();
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
            System.err.println("[SoundManager] music line unavailable: " + e);
            return; // no audio device - stay silent
        }
        int calmIndex = 0;
        int alertStep = 0;
        while (running) {
            EmergencyType alert = alertType;
            if (muted) {
                sleep(100);
                continue;
            }
            if (alert == null) {
                alertStep = 0;
                int chunk = Math.min(4096, calmTheme.length - calmIndex);
                musicLine.write(calmTheme, calmIndex, chunk);
                calmIndex = (calmIndex + chunk) % calmTheme.length;
            } else {
                calmIndex = 0;
                byte[] data = alertBeat(alert, alertStep++);
                musicLine.write(data, 0, data.length);
            }
        }
        musicLine.drain();
        musicLine.close();
    }

    private byte[] alertBeat(EmergencyType type, int step) {
        switch (type) {
            case FIRE: // fast two-tone siren
                return tone(step % 2 == 0 ? 880 : 660, 0.18, 0.30, Wave.SQUARE);
            case OXYGEN_LEAK: // slow descending hiss-like warning
                return mix(tone(520 - (step % 4) * 60, 0.35, 0.25, Wave.SAW),
                        noise(0.35, 0.05));
            case ROVER_BREAKDOWN: // low stuttering buzz
                return step % 2 == 0 ? tone(110, 0.22, 0.32, Wave.SQUARE) : silence(0.12);
            case MEDICAL: // heart-monitor beep
                return step % 2 == 0 ? tone(1000, 0.12, 0.28, Wave.SINE) : silence(0.55);
            default:
                return tone(600, 0.25, 0.28, Wave.SQUARE);
        }
    }

    // -------------------------------------------------------------- effects

    void play(Effect effect) {
        // Mission effects ignore the mute button; it only silences music and emergency sounds.
        boolean missionEffect = effect == Effect.MISSION_DONE || effect == Effect.MISSION_FAILED;
        if (muted && !missionEffect) {
            return;
        }
        Thread t = new Thread(() -> playEffect(effect), "mars-sfx");
        t.setDaemon(true);
        t.start();
    }

    private void playEffect(Effect effect) {
        byte[] data;
        switch (effect) {
            case MISSION_DONE:
                data = concat(tone(C5, 0.12, 0.3, Wave.TRIANGLE), tone(E5, 0.12, 0.3, Wave.TRIANGLE),
                        tone(G5, 0.25, 0.3, Wave.TRIANGLE));
                break;
            case MISSION_FAILED:
                data = loadSfxFile("failed.wav");
                if (data == null) data = concat(tone(E4, 0.2, 0.3, Wave.TRIANGLE), tone(D4, 0.2, 0.3, Wave.TRIANGLE),
                        tone(C4 / 1.5, 0.4, 0.3, Wave.TRIANGLE));
                break;
            case EMERGENCY_STARTED:
                data = concat(tone(880, 0.15, 0.35, Wave.SQUARE), tone(660, 0.15, 0.35, Wave.SQUARE),
                        tone(880, 0.15, 0.35, Wave.SQUARE), tone(660, 0.15, 0.35, Wave.SQUARE));
                break;
            case EMERGENCY_RESOLVED:
                data = concat(tone(G4, 0.1, 0.3, Wave.SINE), tone(C5, 0.1, 0.3, Wave.SINE),
                        tone(E5, 0.1, 0.3, Wave.SINE), tone(G5, 0.3, 0.3, Wave.SINE));
                break;
            default: // EMERGENCY_UNRESOLVED
                data = concat(tone(300, 0.3, 0.35, Wave.SAW), tone(200, 0.5, 0.35, Wave.SAW));
                break;
        }
        try (SourceDataLine line = AudioSystem.getSourceDataLine(FORMAT)) {
            line.open(FORMAT);
            line.start();
            line.write(data, 0, data.length);
            line.drain();
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
            System.err.println("[SoundManager] effect failed: " + e);
        }
    }

    // ------------------------------------------------------------ synthesis

    private enum Wave { SINE, TRIANGLE, SQUARE, SAW }

    private static byte[] tone(double freq, double seconds, double volume, Wave wave) {
        int n = (int) (RATE * seconds);
        byte[] out = new byte[n * 2];
        for (int i = 0; i < n; i++) {
            double phase = (freq * i / RATE) % 1.0;
            double s;
            switch (wave) {
                case SQUARE: s = phase < 0.5 ? 1 : -1; break;
                case SAW: s = 2 * phase - 1; break;
                case TRIANGLE: s = 4 * Math.abs(phase - 0.5) - 1; break;
                default: s = Math.sin(2 * Math.PI * phase); break;
            }
            // short attack/release envelope to avoid clicks
            double env = Math.min(1.0, Math.min(i, n - i) / (RATE * 0.01));
            put(out, i, s * env * volume);
        }
        return out;
    }

    /** Additive-synthesis piano note: bright hammer attack, then a decaying, mellow ring. */
    private static void addPianoNote(float[] buf, int startSample, int midi, double ringSecs, double volume) {
        double freq = 440.0 * Math.pow(2, (midi - 69) / 12.0);
        double[] harmonics = {1.0, 0.55, 0.30, 0.18, 0.10, 0.05};
        int n = (int) (RATE * ringSecs);
        for (int i = 0; i < n && startSample + i < buf.length; i++) {
            double t = i / (double) RATE;
            double sample = 0;
            for (int h = 0; h < harmonics.length; h++) {
                double decay = Math.exp(-t * (1.6 + h * 1.8));
                sample += harmonics[h] * decay * Math.sin(2 * Math.PI * freq * (h + 1) * t);
            }
            double attack = Math.min(1.0, t / 0.004);
            double release = Math.min(1.0, (n - i) / (RATE * 0.05));
            buf[startSample + i] += (float) (sample * attack * release * volume);
        }
    }

    /** Optional custom effect from assets/<name> (any WAV; converted to the mixer format), or null. */
    private static byte[] loadSfxFile(String name) {
        for (String dir : new String[] {"assets/", "../assets/"}) {
            java.io.File f = new java.io.File(dir + name);
            if (!f.isFile()) {
                continue;
            }
            try (javax.sound.sampled.AudioInputStream in = AudioSystem.getAudioInputStream(f);
                 javax.sound.sampled.AudioInputStream pcm = AudioSystem.getAudioInputStream(FORMAT, in)) {
                byte[] data = pcm.readAllBytes();
                return java.util.Arrays.copyOf(data, data.length - data.length % 2);
            } catch (Exception e) {
                System.err.println("[SoundManager] could not read " + f + ": " + e);
            }
        }
        return null;
    }

    private static byte[] loadThemeFile(byte[] fallback) {
        String[] candidates = {"assets/theme.wav", "../assets/theme.wav", "theme.wav"};
        for (String path : candidates) {
            java.io.File f = new java.io.File(path);
            if (!f.isFile()) {
                continue;
            }
            try (javax.sound.sampled.AudioInputStream in = AudioSystem.getAudioInputStream(f);
                 javax.sound.sampled.AudioInputStream pcm = AudioSystem.getAudioInputStream(FORMAT, in)) {
                byte[] data = pcm.readAllBytes();
                data = java.util.Arrays.copyOf(data, data.length - data.length % 2);
                if (data.length > 0) {
                    System.out.println("[SoundManager] theme loaded: " + f.getAbsolutePath());
                    return data;
                }
            } catch (Exception e) {
                System.err.println("[SoundManager] could not read " + path + ": " + e);
            }
        }
        System.out.println("[SoundManager] assets/theme.wav not found - using built-in piano theme");
        return fallback;
    }

    private static byte[] renderPianoSong() {
        int bars = CHORDS.length;
        int total = (int) (RATE * (bars * 4 * BEAT + 2.0));
        float[] buf = new float[total];
        // left hand: gentle broken chords in eighth notes (root, 5th, 3rd, 5th)
        for (int bar = 0; bar < bars; bar++) {
            int[] c = CHORDS[bar];
            int[] pattern = {c[0], c[1], c[2], c[1], c[0], c[1], c[2], c[1]};
            for (int e = 0; e < 8; e++) {
                int start = (int) (RATE * (bar * 4 + e * 0.5) * BEAT);
                addPianoNote(buf, start, pattern[e], 1.2, 0.16);
            }
        }
        // right hand melody
        double beat = 0;
        for (int[] note : MELODY) {
            if (note[0] != 0) {
                addPianoNote(buf, (int) (RATE * beat * BEAT), note[0], Math.min(2.2, note[1] * BEAT + 1.0), 0.26);
            }
            beat += note[1];
        }
        // soft limiter + convert to 16-bit
        int len = (int) (RATE * bars * 4 * BEAT); // drop the tail so the loop stays seamless-ish
        byte[] out = new byte[len * 2];
        for (int i = 0; i < len; i++) {
            double v = Math.tanh(buf[i] + (i + len < total ? buf[i + len] : 0));
            put(out, i, v * 0.8);
        }
        return out;
    }

    private static byte[] noise(double seconds, double volume) {
        int n = (int) (RATE * seconds);
        byte[] out = new byte[n * 2];
        java.util.Random r = new java.util.Random(7);
        for (int i = 0; i < n; i++) {
            put(out, i, (r.nextDouble() * 2 - 1) * volume);
        }
        return out;
    }

    private static byte[] silence(double seconds) {
        return new byte[(int) (RATE * seconds) * 2];
    }

    private static byte[] mix(byte[] a, byte[] b) {
        int n = Math.min(a.length, b.length) / 2;
        byte[] out = new byte[n * 2];
        for (int i = 0; i < n; i++) {
            int v = get(a, i) + get(b, i);
            v = Math.max(-32767, Math.min(32767, v));
            out[i * 2] = (byte) v;
            out[i * 2 + 1] = (byte) (v >> 8);
        }
        return out;
    }

    private static byte[] concat(byte[]... parts) {
        int total = 0;
        for (byte[] p : parts) {
            total += p.length;
        }
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, pos, p.length);
            pos += p.length;
        }
        return out;
    }

    private static void put(byte[] buf, int i, double sample) {
        int v = (int) (Math.max(-1, Math.min(1, sample)) * 32767);
        buf[i * 2] = (byte) v;
        buf[i * 2 + 1] = (byte) (v >> 8);
    }

    private static int get(byte[] buf, int i) {
        return (short) ((buf[i * 2] & 0xFF) | (buf[i * 2 + 1] << 8));
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
