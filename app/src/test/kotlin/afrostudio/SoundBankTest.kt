package com.yaray.afrostudio
// uses JUnit 5
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import tech.apter.junit.jupiter.robolectric.RobolectricExtension // using experimental https://github.com/apter-tech/junit5-robolectric-extension

@ExtendWith(RobolectricExtension::class)
class SoundBankTest {

    private lateinit var soundBank: SoundBank
    private lateinit var context: Context

    @BeforeEach
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
        assertNotNull(retrievedSound, "Retrieved sound should not be null")
        assertArrayEquals(testData, retrievedSound, "Retrieved sound should match added sound")
    }

    @Test
    fun testGetSoundDefaultVariant() {
        val family = "test_family"
        val soundType = "test_sound"
        val testData = byteArrayOf(1, 2, 3, 4)

        soundBank.addSound(family, soundType, 0, testData)

        val retrievedSound = soundBank.getSound(family, soundType)
        assertNotNull(retrievedSound, "Retrieved sound should not be null")
        assertArrayEquals(testData, retrievedSound, "Retrieved sound should match added sound")
    }

    @Test
    fun testGetNonExistentSound() {
        val retrievedSound = soundBank.getSound("non_existent", "non_existent", 0)
        assertNotNull(retrievedSound, "Retrieved sound should not be null even if not found")
        assertEquals(0, retrievedSound.size, "Non-existent sound should return empty array")
    }

    @Test
    fun testLoadAllSounds() {
        soundBank.loadAllSounds(context)

        val djembeBass = soundBank.getSound("djembe", "bass", 0)
        val dunBass = soundBank.getSound("dun", "bass_bell", 0)
        val silence = soundBank.getSound("special", "silence", 0)

        assertTrue(djembeBass.isNotEmpty(), "Djembe bass sound should be loaded")
        assertTrue(dunBass.isNotEmpty(), "Dun bass bell sound should be loaded")
        assertTrue(silence.isNotEmpty(), "Silence sound should be created")
        assertEquals(176400, silence.size, "Silence sound should be 176400 bytes")
    }

    @Test
    fun testDjembeVariations() {
        soundBank.loadAllSounds(context)

        for (variant in 0..2) {
            val bassDjembe = soundBank.getSound("djembe", "bass", variant)
            val toneDjembe = soundBank.getSound("djembe", "tone", variant)
            val slapDjembe = soundBank.getSound("djembe", "slap", variant)

            assertNotNull(bassDjembe, "Djembe bass variant $variant should not be null")
            assertNotNull(toneDjembe, "Djembe tone variant $variant should not be null")
            assertNotNull(slapDjembe, "Djembe slap variant $variant should not be null")

            assertTrue(bassDjembe.isNotEmpty(), "Djembe bass variant $variant should have data")
            assertTrue(toneDjembe.isNotEmpty(), "Djembe tone variant $variant should have data")
            assertTrue(slapDjembe.isNotEmpty(), "Djembe slap variant $variant should have data")
        }
    }

    @Test
    fun testDjembeFlams() {
        soundBank.loadAllSounds(context)

        for (variant in 0..2) {
            val bassFlam = soundBank.getSound("djembe", "bass_flam", variant)
            val toneFlam = soundBank.getSound("djembe", "tone_flam", variant)
            val slapFlam = soundBank.getSound("djembe", "slap_flam", variant)

            assertNotNull(bassFlam, "Djembe bass_flam variant $variant should not be null")
            assertNotNull(toneFlam, "Djembe tone_flam variant $variant should not be null")
            assertNotNull(slapFlam, "Djembe slap_flam variant $variant should not be null")

            assertTrue(bassFlam.isNotEmpty(), "Djembe bass_flam variant $variant should have data")
            assertTrue(toneFlam.isNotEmpty(), "Djembe tone_flam variant $variant should have data")
            assertTrue(slapFlam.isNotEmpty(), "Djembe slap_flam variant $variant should have data")
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

            assertNotNull(bass, "$instrument bass should not be null")
            assertNotNull(bassMute, "$instrument bass_mute should not be null")
            assertNotNull(bassBell, "$instrument bass_bell should not be null")
            assertNotNull(bell, "$instrument bell should not be null")
            assertNotNull(bassBellMute, "$instrument bass_bell_mute should not be null")

            assertTrue(bass.isNotEmpty(), "$instrument bass should have data")
            assertTrue(bassMute.isNotEmpty(), "$instrument bass_mute should have data")
            assertTrue(bassBell.isNotEmpty(), "$instrument bass_bell should have data")
            assertTrue(bell.isNotEmpty(), "$instrument bell should have data")
            assertTrue(bassBellMute.isNotEmpty(), "$instrument bass_bell_mute should have data")
        }
    }

    @Test
    fun testBaletSounds() {
        soundBank.loadAllSounds(context)

        val baletSounds = arrayOf("dun", "sag", "ken", "dun_mute", "sag_mute", "ken_mute", "ring")
        for (soundType in baletSounds) {
            val sound = soundBank.getSound("balet", soundType, 0)
            assertNotNull(sound, "Balet $soundType should not be null")
            assertTrue(sound.isNotEmpty(), "Balet $soundType should have data")
        }
    }

    @Test
    fun testSpecialSounds() {
        soundBank.loadAllSounds(context)

        val ring = soundBank.getSound("special", "ring", 0)
        val silence = soundBank.getSound("special", "silence", 0)

        assertNotNull(ring, "Special ring sound should not be null")
        assertNotNull(silence, "Special silence sound should not be null")

        assertTrue(ring.isNotEmpty(), "Special ring sound should have data")
        assertEquals(176400, silence.size, "Silence should be exactly 176400 bytes")

        val allZeros = silence.all { it == 0.toByte() }
        assertTrue(allZeros, "Silence should contain all zeros")
    }

    @Test
    fun testShekSounds() {
        soundBank.loadAllSounds(context)

        val shek = soundBank.getSound("shek", "standard", 0)

        assertNotNull(shek, "Shekere sound should not be null")
        assertTrue(shek.isNotEmpty(), "Shekere sound should have data")
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

        assertArrayEquals(variant0Data, soundBank.getSound(family, soundType, 0), "Variant 0 data should match")
        assertArrayEquals(variant1Data, soundBank.getSound(family, soundType, 1), "Variant 1 data should match")
        assertArrayEquals(variant2Data, soundBank.getSound(family, soundType, 2), "Variant 2 data should match")
    }

    @Test
    fun testVariantBoundaries() {
        val family = "test_family"
        val soundType = "test_sound"
        val testData = byteArrayOf(1, 2, 3, 4)

        soundBank.addSound(family, soundType, 0, testData)

        val retrievedVariant1 = soundBank.getSound(family, soundType, 1)
        val retrievedVariant2 = soundBank.getSound(family, soundType, 2)

        assertEquals(0, retrievedVariant1.size, "Non-existent variant should return empty array")
        assertEquals(0, retrievedVariant2.size, "Non-existent variant should return empty array")
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

                    assertNotNull(actual, "Sound should not be null for $family:$soundType:$v")
                    assertArrayEquals(expected, actual, "Sound data should match for $family:$soundType:$v")
                }
            }
        }
    }
}
