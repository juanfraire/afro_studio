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
    /**
     * Reads a WAV file header to get the data chunk size.
     *
     * @param wavStream Input stream containing WAV data
     * @return Size of the data chunk in bytes, or -1 if an error occurred
     * @throws IOException If there's an error reading from the stream
     */
    private int readWavHeader(InputStream wavStream) throws IOException {
        final int HEADER_SIZE = 44;  // Standard WAV header size
        final int CHUNK_DESCRIPTOR_SIZE = 8;  // Size of chunk descriptor
        final String DATA_MARKER = "data";  // Marker for the data chunk

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Read the entire header
        if (wavStream.read(buffer.array(), 0, buffer.capacity()) != buffer.capacity()) {
            Log.e(TAG, "Failed to read WAV header");
            return -1;
        }

        buffer.rewind();

        // Skip RIFF header (4) + chunk size (4) + WAVE format (4) + fmt marker (4) + fmt chunk size (4)
        buffer.position(20);

        // Verify format is PCM (1)
        int format = buffer.getShort();
        if (format != 1) {
            Log.w(TAG, "Unexpected WAV format: " + format + " (expected 1 for PCM)");
        }

        // Read important format data
        int channels = buffer.getShort();
        int sampleRate = buffer.getInt();

        // Skip byte rate (4) + block align (2)
        buffer.position(buffer.position() + 6);

        // Read bits per sample
        int bitsPerSample = buffer.getShort();

        if (channels != 2 || sampleRate != 44100 || bitsPerSample != 16) {
            Log.w(TAG, String.format("Non-standard WAV format: channels=%d, rate=%d, bits=%d",
                    channels, sampleRate, bitsPerSample));
        }

        // Look for the data chunk
        int chunkId = buffer.getInt();
        while (chunkId != 0x61746164) {  // "data" in little-endian
            // Read size of the current chunk
            int chunkSize = buffer.getInt();

            // Skip this chunk
            if (wavStream.skip(chunkSize) != chunkSize) {
                Log.e(TAG, "Failed to skip non-data chunk");
                return -1;
            }

            // Read next chunk header
            buffer.rewind();
            if (wavStream.read(buffer.array(), 0, CHUNK_DESCRIPTOR_SIZE) != CHUNK_DESCRIPTOR_SIZE) {
                Log.e(TAG, "Failed to read chunk descriptor");
                return -1;
            }

            buffer.rewind();
            chunkId = buffer.getInt();
        }

        // Read the data chunk size
        int dataSize = buffer.getInt();

        if (dataSize <= 0) {
            Log.e(TAG, "Invalid data chunk size: " + dataSize);
            return -1;
        }

        return dataSize;
    }

}