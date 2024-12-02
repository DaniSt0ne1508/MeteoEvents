package com.projectedam.meteoevents.network

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64


//Copiat i basat en la classe d'en Rober CipherUtil.java
object CipherUtil {

    private const val ALGORITHM = "AES"
    private const val ENCRYPTION_PREFIX = "ENC_"
    private val SECRET_KEY = "MeteoEventsSecrt".toByteArray()

    fun encrypt(data: String): String {
        val secretKey: SecretKey = SecretKeySpec(SECRET_KEY, ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return ENCRYPTION_PREFIX + Base64.encodeToString(encryptedData, Base64.NO_WRAP)
    }

    fun decrypt(encryptedData: String): String {
        if (!encryptedData.startsWith(ENCRYPTION_PREFIX)) {
            throw IllegalArgumentException("El texto no está cifrado con el formato esperado.")
        }
        val base64Data = encryptedData.removePrefix(ENCRYPTION_PREFIX)
        if (!base64Data.matches("^[A-Za-z0-9+/=]*$".toRegex())) {
            throw IllegalArgumentException("Los datos contienen caracteres no válidos para Base64.")
        }
        val secretKey: SecretKey = SecretKeySpec(SECRET_KEY, ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedData = Base64.decode(base64Data, Base64.NO_WRAP)
        return String(cipher.doFinal(decodedData), Charsets.UTF_8)
    }
}
