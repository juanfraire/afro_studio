package com.yaray.afrostudio;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class SoundBank {
    // Map of instrument families to their sound variations
    private Map<String, Map<String, byte[][]>> sounds;

    private static final String TAG = "SoundBank";

    public SoundBank() {
        sounds = new HashMap<>();
    }

    /**
     * Adds a sound for a specific instrument type and variant
     *
     * @param family The instrument family (e.g., "djembe", "dun")
     * @param soundType The sound type (e.g., "bass", "tone", "slap")
     * @param variant The variant index (for different sound sets)
     * @param soundData The raw sound data
     */
    public void addSound(String family, String soundType, int variant, byte[] soundData) {
        if (!sounds.containsKey(family)) {
            sounds.put(family, new HashMap<>());
        }

        Map<String, byte[][]> familySounds = sounds.get(family);
        String key = soundType;

        if (!familySounds.containsKey(key)) {
            // Initialize with enough space for all variants
            familySounds.put(key, new byte[3][]);
        }

        familySounds.get(key)[variant] = soundData;
    }

    /**
     * Gets a sound for a specific instrument
     *
     * @param family Instrument family (e.g., "djembe")
     * @param soundType Sound type (e.g., "bass")
     * @param variant Variant index (0-2)
     * @return The sound data
     */
    public byte[] getSound(String family, String soundType, int variant) {
        if (!sounds.containsKey(family) ||
                !sounds.get(family).containsKey(soundType) ||
                sounds.get(family).get(soundType)[variant] == null) {
            return new byte[0]; // Return empty array if sound not found
        }

        return sounds.get(family).get(soundType)[variant];
    }

    public byte[] getSound(String family, String soundType) {
        return getSound(family, soundType, 0); // Default variant to 0
    }

    /**
     * Loads all sounds from resources
     *
     * @param context The application context
     */
    public void loadAllSounds(Context context) {
        // Load djembe sounds with variations
        for (int variant = 0; variant < 3; variant++) {
            int bassId = getResourceId(context, "snd_djembe" + (variant > 0 ? (variant+1) : "") + "_bass", "raw");
            int toneId = getResourceId(context, "snd_djembe" + (variant > 0 ? (variant+1) : "") + "_tone", "raw");
            int slapId = getResourceId(context, "snd_djembe" + (variant > 0 ? (variant+1) : "") + "_slap", "raw");

            addSound("djembe", "bass", variant, loadRawSound(context, bassId));
            addSound("djembe", "tone", variant, loadRawSound(context, toneId));
            addSound("djembe", "slap", variant, loadRawSound(context, slapId));

            // Also load flam variations
            int bassFlam = getResourceId(context, "snd_djembe" + (variant > 0 ? (variant+1) : "") + "_bass_flam", "raw");
            int toneFlam = getResourceId(context, "snd_djembe" + (variant > 0 ? (variant+1) : "") + "_tone_flam", "raw");
            int slapFlam = getResourceId(context, "snd_djembe" + (variant > 0 ? (variant+1) : "") + "_slap_flam", "raw");

            addSound("djembe", "bass_flam", variant, loadRawSound(context, bassFlam));
            addSound("djembe", "tone_flam", variant, loadRawSound(context, toneFlam));
            addSound("djembe", "slap_flam", variant, loadRawSound(context, slapFlam));
        }

        // Load other instruments (single variant)
        String[] instruments = {"dun", "ken", "sag"};
        String[] soundTypes = {"bass", "bass_mute", "bass_bell", "bell", "bass_bell_mute"};

        // TODO: interesting, we only use "bell", "bass_bell" and "bass_bell_mute". Can we use the others?

        for (String instrument : instruments) {
            for (String soundType : soundTypes) {
                int resId = getResourceId(context, "snd_" + instrument + "_" + soundType, "raw");
                addSound(instrument, soundType, 0, loadRawSound(context, resId));
            }
        }

        // Add balet sounds, reusing sounds from other instruments
        int dunBassId = getResourceId(context, "snd_dun_bass", "raw");
        int sagBassId = getResourceId(context, "snd_sag_bass", "raw");
        int kenBassId = getResourceId(context, "snd_ken_bass", "raw");
        int dunBassMuteId = getResourceId(context, "snd_dun_bass_mute", "raw");
        int sagBassMuteId = getResourceId(context, "snd_sag_bass_mute", "raw");
        int kenBassMuteId = getResourceId(context, "snd_ken_bass_mute", "raw");
        int ringId = getResourceId(context, "snd_ring", "raw");

        addSound("balet", "dun", 0, loadRawSound(context, dunBassId));
        addSound("balet", "sag", 0, loadRawSound(context, sagBassId));
        addSound("balet", "ken", 0, loadRawSound(context, kenBassId));
        addSound("balet", "dun_mute", 0, loadRawSound(context, dunBassMuteId));
        addSound("balet", "sag_mute", 0, loadRawSound(context, sagBassMuteId));
        addSound("balet", "ken_mute", 0, loadRawSound(context, kenBassMuteId));
        addSound("balet", "ring", 0, loadRawSound(context, ringId));

        // Load shekere sounds
        addSound("shek", "standard", 0, loadRawSound(context,
                getResourceId(context, "snd_shek", "raw")));

        // Load silence and other special sounds
        addSound("special", "silence", 0, createSilenceBuffer(176400)); // 500ms
        addSound("special", "ring", 0, loadRawSound(context,
                getResourceId(context, "snd_ring", "raw")));
    }

    private byte[] loadRawSound(Context context, int resourceId) {
        try (InputStream stream = context.getResources().openRawResource(resourceId)) {
            int dataSize = readWavHeader(stream);
            if (dataSize <= 0) {
                Log.e(TAG, "Invalid WAV data size: " + dataSize);
                return new byte[0];
            }

            byte[] data = new byte[dataSize];
            int bytesRead = stream.read(data, 0, dataSize);

            if (bytesRead != dataSize) {
                Log.e(TAG, "Expected to read " + dataSize + " bytes, but got " + bytesRead);
                // Return partial data
                byte[] partialData = new byte[bytesRead > 0 ? bytesRead : 0];
                if (bytesRead > 0) {
                    System.arraycopy(data, 0, partialData, 0, bytesRead);
                }
                return partialData;
            }

            return data;
        } catch (IOException e) {
            Log.e(TAG, "Error loading sound resource: " + resourceId, e);
            return new byte[0];
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Resource not found: " + resourceId, e);
            return new byte[0];
        }
    }

    private int getResourceId(Context context, String name, String defType) {
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    private byte[] createSilenceBuffer(int size) {
        return new byte[size]; // Already filled with zeros
    }

    private int readWavHeader(InputStream wavStream) {     // Read WAV Header
        final int HEADER_SIZE = 44;
        int dataSize = 0;
        try {
            boolean err=false;
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            if (wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity()) != buffer.capacity())
                err=true; //Log.e(TAG, "Not enough bytes read from snd file header!");
            buffer.rewind();
            buffer.position(buffer.position() + 20);
            int format = buffer.getShort();
            //Logd(TAG, "WavReader encoding (1 expected): " + format); // 1 = Linear PCM
            int channels = buffer.getShort();
            //Logd(TAG, "WavReader channels (2 expected): " + channels);
            int rate = buffer.getInt();
            //Logd(TAG, "WavReader rate (44100 expected): " + rate); //44100
            buffer.position(buffer.position() + 6);
            int bits = buffer.getShort();
            //Logd(TAG, "WavReader bits (16 expected): " + bits); // 16 bits
            while (buffer.getInt() != 0x61746164) { // "data" marker
                //Logd(TAG, "WavReader Skipping non-data chunk");
                int size = buffer.getInt();
                if (wavStream.skip(size) != size)
                    err=true;//Log.e(TAG, "Not enough bytes skip from snd file header!");
                buffer.rewind();
                if (wavStream.read(buffer.array(), buffer.arrayOffset(), 8) != 8)
                    err=true;//Log.e(TAG, "Not enough bytes read from snd file header!");
                buffer.rewind();
            }
            dataSize = buffer.getInt();
            //Logd(TAG, "WavReader dataSize (0+ expected): " + dataSize + " Bytes"); // 16 bits
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        return dataSize; //  total number of bytes in the chunk data
    }
}