package com.mileway.core.data.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * PLAN_V22 P2.3: proves the pure `commonMain` SHA-256 ([sha256Hex]) against known-answer test
 * vectors (NIST/FIPS 180-4 examples) so [PinHashStore]/[SwitchAccountViewModel] can trust it
 * without a platform crypto dependency.
 */
class PinHashingTest {
    @Test
    fun `hash of empty string matches the well-known SHA-256 vector`() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            sha256Hex(""),
        )
    }

    @Test
    fun `hash of abc matches the well-known SHA-256 vector`() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            sha256Hex("abc"),
        )
    }

    @Test
    fun `hash of a 4-digit PIN is deterministic`() {
        assertEquals(sha256Hex("1234"), sha256Hex("1234"))
    }

    @Test
    fun `different PINs hash differently`() {
        assertNotEquals(sha256Hex("1234"), sha256Hex("4321"))
    }
}
