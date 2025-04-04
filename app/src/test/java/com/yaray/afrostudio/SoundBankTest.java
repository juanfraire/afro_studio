package com.yaray.afrostudio;
//uses JUnit 4
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;


@RunWith(RobolectricTestRunner.class)
public class SoundBankTest {

    private SoundBank soundBank;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        soundBank = new SoundBank();
    }

    @Test
    public void testAddAndGetSound() {
        // Test data
        String family = "test_family";
        String soundType = "test_sound";
        int variant = 0;
        byte[] testData = new byte[]{1, 2, 3, 4};

        // Add sound to sound bank
        soundBank.addSound(family, soundType, variant, testData);

        // Get sound and verify it matches
        byte[] retrievedSound = soundBank.getSound(family, soundType, variant);
        assertNotNull("Retrieved sound should not be null", retrievedSound);
        assertArrayEquals("Retrieved sound should match added sound", testData, retrievedSound);
    }

    @Test
    public void testGetSoundDefaultVariant() {
        // Test data
        String family = "test_family";
        String soundType = "test_sound";
        byte[] testData = new byte[]{1, 2, 3, 4};

        // Add sound to sound bank
        soundBank.addSound(family, soundType, 0, testData);

        // Get sound with default variant and verify
        byte[] retrievedSound = soundBank.getSound(family, soundType);
        assertNotNull("Retrieved sound should not be null", retrievedSound);
        assertArrayEquals("Retrieved sound should match added sound", testData, retrievedSound);
    }

    @Test
    public void testGetNonExistentSound() {
        // Get non-existent sound
        byte[] retrievedSound = soundBank.getSound("non_existent", "non_existent", 0);
        assertNotNull("Retrieved sound should not be null even if not found", retrievedSound);
        assertEquals("Non-existent sound should return empty array", 0, retrievedSound.length);
    }

    @Test
    public void testLoadAllSounds() {
        // Load all sounds
        soundBank.loadAllSounds(context);

        // Verify some essential sounds were loaded
        byte[] djembeBass = soundBank.getSound("djembe", "bass", 0);
        byte[] dunBass = soundBank.getSound("dun", "bass_bell", 0);
        byte[] silence = soundBank.getSound("special", "silence", 0);

        assertTrue("Djembe bass sound should be loaded", djembeBass.length > 0);
        assertTrue("Dun bass bell sound should be loaded", dunBass.length > 0);
        assertTrue("Silence sound should be created", silence.length > 0);
        assertEquals("Silence sound should be 176400 bytes", 176400, silence.length);
    }

    @Test
    public void testDjembeVariations() {
        // Load sounds first
        soundBank.loadAllSounds(context);

        // Test all three variants of djembe sounds
        for (int variant = 0; variant < 3; variant++) {
            byte[] bassDjembe = soundBank.getSound("djembe", "bass", variant);
            byte[] toneDjembe = soundBank.getSound("djembe", "tone", variant);
            byte[] slapDjembe = soundBank.getSound("djembe", "slap", variant);

            assertNotNull("Djembe bass variant " + variant + " should not be null", bassDjembe);
            assertNotNull("Djembe tone variant " + variant + " should not be null", toneDjembe);
            assertNotNull("Djembe slap variant " + variant + " should not be null", slapDjembe);

            assertTrue("Djembe bass variant " + variant + " should have data", bassDjembe.length > 0);
            assertTrue("Djembe tone variant " + variant + " should have data", toneDjembe.length > 0);
            assertTrue("Djembe slap variant " + variant + " should have data", slapDjembe.length > 0);
        }
    }

    @Test
    public void testDjembeFlams() {
        soundBank.loadAllSounds(context);

        // Test flam sounds for djembe
        for (int variant = 0; variant < 3; variant++) {
            byte[] bassFlam = soundBank.getSound("djembe", "bass_flam", variant);
            byte[] toneFlam = soundBank.getSound("djembe", "tone_flam", variant);
            byte[] slapFlam = soundBank.getSound("djembe", "slap_flam", variant);

            assertNotNull("Djembe bass_flam variant " + variant + " should not be null", bassFlam);
            assertNotNull("Djembe tone_flam variant " + variant + " should not be null", toneFlam);
            assertNotNull("Djembe slap_flam variant " + variant + " should not be null", slapFlam);

            assertTrue("Djembe bass_flam variant " + variant + " should have data", bassFlam.length > 0);
            assertTrue("Djembe tone_flam variant " + variant + " should have data", toneFlam.length > 0);
            assertTrue("Djembe slap_flam variant " + variant + " should have data", slapFlam.length > 0);
        }
    }

    @Test
    public void testPercussionInstruments() {
        soundBank.loadAllSounds(context);

        // Test dun, ken, and sag sounds
        String[] instruments = {"dun", "ken", "sag"};
        for (String instrument : instruments) {
            byte[] bass = soundBank.getSound(instrument, "bass", 0);
            byte[] bassMute = soundBank.getSound(instrument, "bass_mute", 0);
            byte[] bassBell = soundBank.getSound(instrument, "bass_bell", 0);
            byte[] bell = soundBank.getSound(instrument, "bell", 0);
            byte[] bassBellMute = soundBank.getSound(instrument, "bass_bell_mute", 0);

            assertNotNull(instrument + " bass should not be null", bass);
            assertNotNull(instrument + " bass_mute should not be null", bassMute);
            assertNotNull(instrument + " bass_bell should not be null", bassBell);
            assertNotNull(instrument + " bell should not be null", bell);
            assertNotNull(instrument + " bass_bell_mute should not be null", bassBellMute);

            assertTrue(instrument + " bass should have data", bass.length > 0);
            assertTrue(instrument + " bass_mute should have data", bassMute.length > 0);
            assertTrue(instrument + " bass_bell should have data", bassBell.length > 0);
            assertTrue(instrument + " bell should have data", bell.length > 0);
            assertTrue(instrument + " bass_bell_mute should have data", bassBellMute.length > 0);
        }
    }

    @Test
    public void testBaletSounds() {
        soundBank.loadAllSounds(context);

        // Test balet sounds (these reuse other instrument sounds)
        String[] baletSounds = {"dun", "sag", "ken", "dun_mute", "sag_mute", "ken_mute", "ring"};
        for (String soundType : baletSounds) {
            byte[] sound = soundBank.getSound("balet", soundType, 0);
            assertNotNull("Balet " + soundType + " should not be null", sound);
            assertTrue("Balet " + soundType + " should have data", sound.length > 0);
        }
    }

    @Test
    public void testSpecialSounds() {
        soundBank.loadAllSounds(context);

        // Test special sounds
        byte[] ring = soundBank.getSound("special", "ring", 0);
        byte[] silence = soundBank.getSound("special", "silence", 0);

        assertNotNull("Special ring sound should not be null", ring);
        assertNotNull("Special silence sound should not be null", silence);

        assertTrue("Special ring sound should have data", ring.length > 0);
        assertEquals("Silence should be exactly 176400 bytes", 176400, silence.length);

        // Check that silence is all zeros
        boolean allZeros = true;
        for (byte b : silence) {
            if (b != 0) {
                allZeros = false;
                break;
            }
        }
        assertTrue("Silence should contain all zeros", allZeros);
    }

    @Test
    public void testShekSounds() {
        soundBank.loadAllSounds(context);

        // Test shekere sound
        byte[] shek = soundBank.getSound("shek", "standard", 0);

        assertNotNull("Shekere sound should not be null", shek);
        assertTrue("Shekere sound should have data", shek.length > 0);
    }

    @Test
    public void testMultipleVariantRetrieval() {
        // Test data with multiple variants
        String family = "test_family";
        String soundType = "test_sound";
        byte[] variant0Data = new byte[]{1, 2, 3, 4};
        byte[] variant1Data = new byte[]{5, 6, 7, 8};
        byte[] variant2Data = new byte[]{9, 10, 11, 12};

        // Add sounds with different variants
        soundBank.addSound(family, soundType, 0, variant0Data);
        soundBank.addSound(family, soundType, 1, variant1Data);
        soundBank.addSound(family, soundType, 2, variant2Data);

        // Verify each variant can be retrieved correctly
        assertArrayEquals("Variant 0 data should match", variant0Data,
                soundBank.getSound(family, soundType, 0));
        assertArrayEquals("Variant 1 data should match", variant1Data,
                soundBank.getSound(family, soundType, 1));
        assertArrayEquals("Variant 2 data should match", variant2Data,
                soundBank.getSound(family, soundType, 2));
    }

    @Test
    public void testVariantBoundaries() {
        // Add only variant 0
        String family = "test_family";
        String soundType = "test_sound";
        byte[] testData = new byte[]{1, 2, 3, 4};

        soundBank.addSound(family, soundType, 0, testData);

        // Try to access non-existent variants (should return empty array)
        byte[] retrievedVariant1 = soundBank.getSound(family, soundType, 1);
        byte[] retrievedVariant2 = soundBank.getSound(family, soundType, 2);

        assertEquals("Non-existent variant should return empty array", 0, retrievedVariant1.length);
        assertEquals("Non-existent variant should return empty array", 0, retrievedVariant2.length);
    }

    @Test
    public void testSoundBankCapacity() {
        // Test adding multiple families and sound types
        int numFamilies = 100;
        int numSoundTypes = 50;
        int numVariants = 3;

        // Generate and add test data
        for (int f = 0; f < numFamilies; f++) {
            String family = "family_" + f;

            for (int s = 0; s < numSoundTypes; s++) {
                String soundType = "sound_" + s;

                for (int v = 0; v < numVariants; v++) {
                    // Create unique data for each sound
                    byte[] data = new byte[4];
                    data[0] = (byte)f;
                    data[1] = (byte)s;
                    data[2] = (byte)v;
                    data[3] = (byte)(f + s + v);

                    soundBank.addSound(family, soundType, v, data);
                }
            }
        }

        // Verify all sounds can be retrieved correctly
        for (int f = 0; f < numFamilies; f++) {
            String family = "family_" + f;

            for (int s = 0; s < numSoundTypes; s++) {
                String soundType = "sound_" + s;

                for (int v = 0; v < numVariants; v++) {
                    byte[] expected = new byte[4];
                    expected[0] = (byte)f;
                    expected[1] = (byte)s;
                    expected[2] = (byte)v;
                    expected[3] = (byte)(f + s + v);

                    byte[] actual = soundBank.getSound(family, soundType, v);

                    assertNotNull("Sound should not be null for " + family + ":" + soundType + ":" + v, actual);
                    assertArrayEquals("Sound data should match for " + family + ":" + soundType + ":" + v,
                            expected, actual);
                }
            }
        }
    }
}