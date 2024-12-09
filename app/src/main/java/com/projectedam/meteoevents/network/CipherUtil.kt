package com.projectedam.meteoevents.network

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

object CipherUtil {

    private const val ALGORITHM = "AES"
    private val SECRET_KEY = "MeteoEventsSecrt".toByteArray()
    private const val ENCRYPTION_PREFIX = "ENC_"

    @Throws(Exception::class)
    fun encrypt(data: String): String {
        val secretKey: SecretKey = SecretKeySpec(SECRET_KEY, ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedData = cipher.doFinal(data.toByteArray())
        return ENCRYPTION_PREFIX + Base64.encodeToString(encryptedData, Base64.NO_WRAP)
    }

    @Throws(Exception::class)
    fun decrypt(encryptedData: String): String {
        if (!encryptedData.startsWith(ENCRYPTION_PREFIX)) {
            throw IllegalArgumentException("Les dades no estan xifrades amb el format esperat.")
        }

        val base64Data = encryptedData.substring(ENCRYPTION_PREFIX.length)
        if (!base64Data.matches("^[A-Za-z0-9+/=]*$".toRegex())) {
            throw IllegalArgumentException("Les dades contenen caràcters no vàlids per Base64.")
        }

        val secretKey: SecretKey = SecretKeySpec(SECRET_KEY, ALGORITHM)
        if (SECRET_KEY.size != 16) {
            throw IllegalArgumentException("La clau secreta ha de tenir 16 bytes.")
        }

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedData = Base64.decode(base64Data, Base64.NO_WRAP)
        return String(cipher.doFinal(decodedData))
    }
}
