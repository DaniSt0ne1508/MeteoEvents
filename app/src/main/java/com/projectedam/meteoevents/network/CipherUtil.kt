package com.projectedam.meteoevents.network

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64


//Copiat i basat en la classe d'en Rober CipherUtil.java
object CipherUtil {

    private const val ALGORITHM = "AES" // Algoritme de xifrat utilitzat
    private const val PREFIX = "ENC_" // Prefix que identifica les dades xifrades
    private const val SECRET_KEY = "MeteoEventsSecrt" // Clau secreta de 16 bytes (identica al servidor)

    // Clau secreta com a objecte SecretKey
    private val secretKey: SecretKey = SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), ALGORITHM)

    /**
     * Xifra un text pla utilitzant l'algoritme AES i afegeix el prefix "ENC_".
     * @param data Text pla que es vol xifrar.
     * @return Text xifrat en Base64 amb el prefix "ENC_".
     */
    fun encrypt(data: String): String {
        // Inicialitza el Cipher en mode ENCRYPT (xifrat)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        // Xifra el text pla i genera les dades xifrades
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        // Codifica les dades xifrades a Base64
        val base64Encrypted = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)

        // Retorna el text xifrat amb el prefix "ENC_"
        return PREFIX + base64Encrypted.trim()
    }

    /**
     * Desxifra un text xifrat amb el prefix "ENC_".
     * @param encryptedData Text xifrat amb el prefix "ENC_".
     * @return Text pla desxifrat.
     */
    fun decrypt(encryptedData: String): String {
        // Comprova que el text comenci amb el prefix "ENC_"
        if (!encryptedData.startsWith(PREFIX)) {
            throw IllegalArgumentException("El text no conté el prefix esperat ($PREFIX).")
        }

        // Elimina el prefix "ENC_" del text xifrat
        val base64Data = encryptedData.removePrefix(PREFIX)

        // Decodifica les dades de Base64
        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)

        // Inicialitza el Cipher en mode DECRYPT (desxifrat)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        // Desxifra les dades i retorna el text pla
        return String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
    }
}
