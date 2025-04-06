package com.yaray.afrostudio
// uses JUnit4 due to robolectric compatibility issues with JUnit5
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SoundBankTest {

    private lateinit var soundBank: SoundBank
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        soundBank = SoundBank()
    }

    @Test
    fun testAddAndGetSound() {
        val family = "test_family"
        val soundType = "test_sound"
        val variant = 0
        val testData = byteArrayOf(1, 2, 3, 4)

        soundBank.addSound(family, soundType, variant, testData)

        val retrievedSound = soundBank.getSound(family, soundType, variant)
        assertNotNull("Retrieved sound should not be null", retrievedSound)
        assertArrayEquals("Retrieved sound should match added sound", testData, retrievedSound)
    }

    @Test
    fun testGetSoundDefaultVariant() {
        val family = "test_family"
        val soundType = "test_sound"
        val testData = byteArrayOf(1, 2, 3, 4)

        soundBank.addSound(family, soundType, 0, testData)

        val retrievedSound = soundBank.getSound(family, soundType)
        assertNotNull("Retrieved sound should not be null", retrievedSound)
        assertArrayEquals("Retrieved sound should match added sound", testData, retrievedSound)
    }

    @Test
    fun testGetNonExistentSound() {
        val retrievedSound = soundBank.getSound("non_existent", "non_existent", 0)
        assertNotNull("Retrieved sound should not be null even if not found", retrievedSound)
        assertEquals("Non-existent sound should return empty array", 0, retrievedSound.size)
    }

    @Test
    fun testLoadAllSounds() {
        soundBank.loadAllSounds(context)

        val djembeBass = soundBank.getSound("djembe", "bass", 0)
        val dunBass = soundBank.getSound("dun", "bass_bell", 0)
        val silence = soundBank.getSound("special", "silence", 0)

        assertTrue("Djembe bass sound should be loaded", djembeBass.isNotEmpty())
        assertTrue("Dun bass bell sound should be loaded", dunBass.isNotEmpty())
        assertTrue("Silence sound should be created", silence.isNotEmpty())
        assertEquals("Silence sound should be 176400 bytes", 176400, silence.size)
    }

    @Test
    fun testDjembeVariations() {
        soundBank.loadAllSounds(context)

        for (variant in 0..2) {
            val bassDjembe = soundBank.getSound("djembe", "bass", variant)
            val toneDjembe = soundBank.getSound("djembe", "tone", variant)
            val slapDjembe = soundBank.getSound("djembe", "slap", variant)

            assertNotNull("Djembe bass variant $variant should not be null", bassDjembe)
            assertNotNull("Djembe tone variant $variant should not be null", toneDjembe)
            assertNotNull("Djembe slap variant $variant should not be null", slapDjembe)

            assertTrue("Djembe bass variant $variant should have data", bassDjembe.isNotEmpty())
            assertTrue("Djembe tone variant $variant should have data", toneDjembe.isNotEmpty())
            assertTrue("Djembe slap variant $variant should have data", slapDjembe.isNotEmpty())
        }
    }

    @Test
    fun testDjembeFlams() {
        soundBank.loadAllSounds(context)

        for (variant in 0..2) {
            val bassFlam = soundBank.getSound("djembe", "bass_flam", variant)
            val toneFlam = soundBank.getSound("djembe", "tone_flam", variant)
            val slapFlam = soundBank.getSound("djembe", "slap_flam", variant)

            assertNotNull("Djembe bass_flam variant $variant should not be null", bassFlam)
            assertNotNull("Djembe tone_flam variant $variant should not be null", toneFlam)
            assertNotNull("Djembe slap_flam variant $variant should not be null", slapFlam)

            assertTrue("Djembe bass_flam variant $variant should have data", bassFlam.isNotEmpty())
            assertTrue("Djembe tone_flam variant $variant should have data", toneFlam.isNotEmpty())
            assertTrue("Djembe slap_flam variant $variant should have data", slapFlam.isNotEmpty())
        }
    }

    @Test
    fun testPercussionInstruments() {
        soundBank.loadAllSounds(context)

        val instruments = arrayOf("dun", "ken", "sag")
        for (instrument in instruments) {
            val bass = soundBank.getSound(instrument, "bass", 0)
            val bassMute = soundBank.getSound(instrument, "bass_mute", 0)
            val bassBell = soundBank.getSound(instrument, "bass_bell", 0)
            val bell = soundBank.getSound(instrument, "bell", 0)
            val bassBellMute = soundBank.getSound(instrument, "bass_bell_mute", 0)

            assertNotNull("$instrument bass should not be null", bass)
            assertNotNull("$instrument bass_mute should not be null", bassMute)
            assertNotNull("$instrument bass_bell should not be null", bassBell)
            assertNotNull("$instrument bell should not be null", bell)
            assertNotNull("$instrument bass_bell_mute should not be null", bassBellMute)

            assertTrue("$instrument bass should have data", bass.isNotEmpty())
            assertTrue("$instrument bass_mute should have data", bassMute.isNotEmpty())
            assertTrue("$instrument bass_bell should have data", bassBell.isNotEmpty())
            assertTrue("$instrument bell should have data", bell.isNotEmpty())
            assertTrue("$instrument bass_bell_mute should have data", bassBellMute.isNotEmpty())
        }
    }

    @Test
    fun testBaletSounds() {
        soundBank.loadAllSounds(context)

        val baletSounds = arrayOf("dun", "sag", "ken", "dun_mute", "sag_mute", "ken_mute", "ring")
        for (soundType in baletSounds) {
            val sound = soundBank.getSound("balet", soundType, 0)
            assertNotNull("Balet $soundType should not be null", sound)
            assertTrue("Balet $soundType should have data", sound.isNotEmpty())
        }
    }

    @Test
    fun testSpecialSounds() {
        soundBank.loadAllSounds(context)

        val ring = soundBank.getSound("special", "ring", 0)
        val silence = soundBank.getSound("special", "silence", 0)

        assertNotNull("Special ring sound should not be null", ring)
        assertNotNull("Special silence sound should not be null", silence)

        assertTrue("Special ring sound should have data", ring.isNotEmpty())
        assertEquals("Silence should be exactly 176400 bytes", 176400, silence.size)

        val allZeros = silence.all { it == 0.toByte() }
        assertTrue("Silence should contain all zeros", allZeros)
    }

    @Test
    fun testShekSounds() {
        soundBank.loadAllSounds(context)

        val shek = soundBank.getSound("shek", "standard", 0)

        assertNotNull("Shekere sound should not be null", shek)
        assertTrue("Shekere sound should have data", shek.isNotEmpty())
    }

    @Test
    fun testMultipleVariantRetrieval() {
        val family = "test_family"
        val soundType = "test_sound"
        val variant0Data = byteArrayOf(1, 2, 3, 4)
        val variant1Data = byteArrayOf(5, 6, 7, 8)
        val variant2Data = byteArrayOf(9, 10, 11, 12)

        soundBank.addSound(family, soundType, 0, variant0Data)
        soundBank.addSound(family, soundType, 1, variant1Data)
        soundBank.addSound(family, soundType, 2, variant2Data)

        assertArrayEquals("Variant 0 data should match", variant0Data, soundBank.getSound(family, soundType, 0))
        assertArrayEquals("Variant 1 data should match", variant1Data, soundBank.getSound(family, soundType, 1))
        assertArrayEquals("Variant 2 data should match", variant2Data, soundBank.getSound(family, soundType, 2))
    }

    @Test
    fun testVariantBoundaries() {
        val family = "test_family"
        val soundType = "test_sound"
        val testData = byteArrayOf(1, 2, 3, 4)

        soundBank.addSound(family, soundType, 0, testData)

        val retrievedVariant1 = soundBank.getSound(family, soundType, 1)
        val retrievedVariant2 = soundBank.getSound(family, soundType, 2)

        assertEquals("Non-existent variant should return empty array", 0, retrievedVariant1.size)
        assertEquals("Non-existent variant should return empty array", 0, retrievedVariant2.size)
    }

    @Test
    fun testSoundBankCapacity() {
        val numFamilies = 100
        val numSoundTypes = 50
        val numVariants = 3

        for (f in 0 until numFamilies) {
            val family = "family_$f"

            for (s in 0 until numSoundTypes) {
                val soundType = "sound_$s"

                for (v in 0 until numVariants) {
                    val data = byteArrayOf(f.toByte(), s.toByte(), v.toByte(), (f + s + v).toByte())
                    soundBank.addSound(family, soundType, v, data)
                }
            }
        }

        for (f in 0 until numFamilies) {
            val family = "family_$f"

            for (s in 0 until numSoundTypes) {
                val soundType = "sound_$s"

                for (v in 0 until numVariants) {
                    val expected = byteArrayOf(f.toByte(), s.toByte(), v.toByte(), (f + s + v).toByte())
                    val actual = soundBank.getSound(family, soundType, v)

                    assertNotNull("Sound should not be null for $family:$soundType:$v", actual)
                    assertArrayEquals("Sound data should match for $family:$soundType:$v", expected, actual)
                }
            }
        }
    }
}
