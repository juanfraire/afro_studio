package com.yaray.afrostudio;

import java.util.Vector;

/**
 * Represents a single musical instrument in the ensemble with its pattern data,
 * active and volume information.
 * djembe type has 3 variations of sounds, 0, 1, 2, in a loop % 3. Other instruments have 1 variation.
 */
public class Instrument {
    private String type;
    private int variation; //
    private Vector<Integer> pattern;
    private boolean active;
    private int volume; // 0-100%

    public Instrument(String type, int variation) {
        this.type = type;
        this.pattern = new Vector<>();
        this.active = true;
        this.volume = 100;
    }

    public String getType() {
        return type;
    }

    public Vector<Integer> getPattern() {
        return pattern;
    }

    public void setNote(int beatPosition, int noteValue) {
        // TODO: check N+1 boundaries?
        if (beatPosition >= pattern.size()) {
          resizePattern(beatPosition + 1);
        }
        pattern.set(beatPosition, noteValue);
    }

    public int getNote(int beatPosition) {
        if (beatPosition < pattern.size()) {
            return pattern.get(beatPosition);
        }
        return 0;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(volume, 100));
    }

    public void resizePattern(int newLength) {
        if (newLength < pattern.size()) {
            // Truncate
            pattern.setSize(newLength);
        } else {
            // Expand and fill with zeros
            while (pattern.size() < newLength) {
                pattern.add(0);
            }
        }
    }
}