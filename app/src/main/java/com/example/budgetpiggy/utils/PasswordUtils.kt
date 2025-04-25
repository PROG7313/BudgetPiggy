package com.example.budgetpiggy.utils

import org.bouncycastle.crypto.generators.BCrypt
import java.nio.charset.StandardCharsets
import java.util.Base64

object PasswordUtils {

    fun hashPassword(password: String): String {
        val salt = ByteArray(16) { it.toByte() } // Use static for dev; use SecureRandom for production
        val hashed = BCrypt.generate(password.toByteArray(StandardCharsets.UTF_8), salt, 10)
        return Base64.getEncoder().encodeToString(hashed)
    }

    fun verifyPassword(password: String, hashedBase64: String): Boolean {
        val salt = ByteArray(16) { it.toByte() } // must match the one used in hashPassword
        val testHash = BCrypt.generate(password.toByteArray(StandardCharsets.UTF_8), salt, 10)
        return Base64.getEncoder().encodeToString(testHash) == hashedBase64
    }
}
