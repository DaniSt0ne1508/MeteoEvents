package com.projectedam.meteoevents.network

import android.annotation.SuppressLint
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


/**
 * Classe utilitària per gestionar la xifra i desxifra de dades.
 * Aquesta implementació utilitza l'algoritme AES per protegir les dades sensibles
 * com les contrasenyes.
 *
 * @author Chat-GPT. Prompt: Construeix classe unitaria per xifrar i desxifrar utilitzant Cipher
 */
object CipherUtil {
    private const val ALGORITHM = "AES"
    private val SECRET_KEY =
        "MeteoEventsSecrt".toByteArray()
    private const val ENCRYPTION_PREFIX = "ENC_"

    /**
     * Xifra una cadena de text utilitzant l'algoritme AES i afegeix el prefix "ENC_".
     *
     * @param data el text pla que es vol xifrar.
     * @return el text xifrat en format Base64 amb el prefix "ENC_".
     * @throws Exception si hi ha algun error durant la xifra.
     */
    @SuppressLint("NewApi")
    @Throws(Exception::class)
    fun encrypt(data: String): String {
        val secretKey: SecretKey = SecretKeySpec(SECRET_KEY, ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedData = cipher.doFinal(data.toByteArray())
        return ENCRYPTION_PREFIX + java.util.Base64.getEncoder()
            .encodeToString(encryptedData)
    }

    /**
     * Desxifra una cadena de text xifrada utilitzant l'algoritme AES.
     * Només es desxifra si el text comença amb el prefix "ENC_".
     *
     * @param encryptedData el text xifrat en format Base64 amb el prefix "ENC_".
     * @return el text desxifrat en text pla.
     * @throws IllegalArgumentException si el text no conté el prefix "ENC_".
     * @throws Exception si hi ha algun error durant la desxifra.
     */
    @SuppressLint("NewApi")
    @Throws(Exception::class)
    fun decrypt(encryptedData: String): String {
        require(encryptedData.startsWith(ENCRYPTION_PREFIX)) { "Les dades no estan xifrades amb el format esperat." }

        val base64Data = encryptedData.substring(ENCRYPTION_PREFIX.length)
        require(base64Data.matches("^[A-Za-z0-9+/=]*$".toRegex())) { "Les dades contenen caràcters no vàlids per Base64." }

        val secretKey: SecretKey = SecretKeySpec(SECRET_KEY, ALGORITHM)
        require(SECRET_KEY.size == 16) { "La clau secreta ha de tenir 16 bytes." }

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedData = java.util.Base64.getDecoder().decode(base64Data)
        return String(cipher.doFinal(decodedData))
    }
}
