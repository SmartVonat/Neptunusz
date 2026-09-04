package com.example.neptunusz

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpGenerator {

    /**
     * Generates a 6-digit TOTP code for the given Base32 secret.
     */
    fun generateCode(secretBase32: String): String {
        return try {
            val decodedSecret = decodeBase32(secretBase32)
            val timeStep = System.currentTimeMillis() / 1000 / 30
            
            val data = ByteBuffer.allocate(8).putLong(timeStep).array()
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(decodedSecret, "HmacSHA1"))
            val hash = mac.doFinal(data)
            
            val offset = hash[hash.size - 1].toInt() and 0xf
            val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                         ((hash[offset + 1].toInt() and 0xff) shl 16) or
                         ((hash[offset + 2].toInt() and 0xff) shl 8) or
                         (hash[offset + 3].toInt() and 0xff)
            
            val otp = binary % 10.0.pow(6).toInt()
            otp.toString().padStart(6, '0')
        } catch (e: Exception) {
            "000000" // Fallback or handle appropriately in UI
        }
    }

    /**
     * Pure Kotlin Base32 decoder (RFC 4648).
     */
    private fun decodeBase32(base32: String): ByteArray {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleanInput = base32.uppercase().replace("=", "")
        var bits = 0
        var value = 0
        val output = mutableListOf<Byte>()

        for (char in cleanInput) {
            val index = alphabet.indexOf(char)
            if (index == -1) continue // Should be cleaned by SecureStorageManager anyway
            
            value = (value shl 5) or index
            bits += 5
            
            if (bits >= 8) {
                output.add(((value shr (bits - 8)) and 0xFF).toByte())
                bits -= 8
            }
        }
        return output.toByteArray()
    }
}
