package com.mhmdeve.realmeotafinder.utils

import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Crypto {
    companion object {
        val keys = listOf(
            "oppo1997", "baed2017", "java7865", "231uiedn", "09e32ji6",
            "0oiu3jdy", "0pej387l", "2dkliuyt", "20odiuye", "87j3id7w"
        )

        fun getKey(key: String): ByteArray {
            val index = key[0].digitToInt()
            val combinedKey = keys[index] + key.substring(4, 12)
            return combinedKey.toByteArray(Charsets.UTF_8)
        }

        fun getRandomKey(): ByteArray {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            val secretKey = keyGen.generateKey()
            return secretKey.encoded
        }

        fun getIV(): ByteArray {
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            return iv
        }

        fun enc_dec_AES_CTR(
            data: ByteArray,
            key: ByteArray,
            iv: ByteArray,
            mode: String
        ): ByteArray {
            val cipher = Cipher.getInstance("AES/CTR/NoPadding")
            val ivSpec = IvParameterSpec(iv)
            val secretKeySpec = SecretKeySpec(key, "AES")
            cipher.init(
                if (mode == "encrypt") Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE,
                secretKeySpec,
                ivSpec
            )
            return cipher.doFinal(data)
        }

        fun enc_AES_CTR(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
            return enc_dec_AES_CTR(data, key, iv, "encrypt")
        }

        fun dec_AES_CTR(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
            return enc_dec_AES_CTR(data, key, iv, "decrypt")
        }

        fun enc_dec_AES_ECB(data: ByteArray, key: ByteArray, mode: String): ByteArray {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val secretKeySpec = SecretKeySpec(key, "AES")
            cipher.init(
                if (mode == "encrypt") Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE,
                secretKeySpec
            )
            return cipher.doFinal(data)
        }

        fun enc_AES_ECB(data: ByteArray, key: ByteArray): ByteArray {
            return enc_dec_AES_ECB(data, key, "encrypt")
        }

        fun dec_AES_ECB(data: ByteArray, key: ByteArray): ByteArray {
            return enc_dec_AES_ECB(data, key, "decrypt")
        }

        fun encryptCtr(buf: String): String {
            // Generate a pseudo key: first character is a random digit, followed by 14 random digits
            val keyPseudo = (0..9).random().toString() + (1..14).map { ('0'..'9').random() }.joinToString("")

            // Get the real key using the getKey() function from Crypto class
            val keyReal = getKey(keyPseudo)

            // Generate the IV using MD5 of the real key
            val md = MessageDigest.getInstance("MD5")
            val iv = md.digest(keyReal)

            // Encrypt using AES-CTR
            val encrypted = enc_AES_CTR(buf.toByteArray(Charsets.UTF_8), keyReal, iv)

            // Return the base64 encoded ciphertext concatenated with the key pseudo
            return Base64.getEncoder().encodeToString(encrypted) + keyPseudo
        }

        fun decryptCtr(buf: String): String {
            // Decode the base64-encoded ciphertext (excluding the last 15 characters for the key pseudo)
            val data = Base64.getDecoder().decode(buf.dropLast(15))

            // Get the real key using the getKey() function from Crypto class
            val keyReal = getKey(buf.takeLast(15))

            // Generate the IV using MD5 of the real key
            val md = MessageDigest.getInstance("MD5")
            val iv = md.digest(keyReal)

            // Decrypt using AES-CTR
            val decrypted = dec_AES_CTR(data, keyReal, iv)

            // Convert decrypted bytes to String and return
            return String(decrypted, Charsets.UTF_8)
        }

        fun encrypt_ctr_v2(data: String): Triple<String, String, String> {
            val key = getRandomKey()
            val iv = getIV()
            val encrypted = enc_AES_CTR(data.toByteArray(Charsets.UTF_8), key, iv)
            return Triple(
                Base64.getEncoder().encodeToString(encrypted),
                Base64.getEncoder().encodeToString(key),
                Base64.getEncoder().encodeToString(iv)
            )
        }

        fun decrypt_ctr_v2(data: String, key: String, iv: String): String {
            val decodedData = Base64.getDecoder().decode(data)
            val decodedKey = Base64.getDecoder().decode(key)
            val decodedIv = Base64.getDecoder().decode(iv)
            val decrypted = dec_AES_CTR(decodedData, decodedKey, decodedIv)
            return String(decrypted, Charsets.UTF_8)
        }

        fun encrypt_ecb(data: String): String {
            val keyPseudo =
                (0..9).random().toString() + (1..14).map { ('a'..'z').random() }.joinToString("")
            val keyReal = getKey(keyPseudo)
            val encrypted = enc_AES_ECB(data.toByteArray(Charsets.UTF_8), keyReal)
            return Base64.getEncoder().encodeToString(encrypted) + keyPseudo
        }

        fun decrypt_ecb(data: String): String {
            val encryptedData = Base64.getDecoder().decode(data.substring(0, data.length - 15))
            val key = getKey(data.takeLast(15))
            val plain = dec_AES_ECB(encryptedData, key)
            return String(plain, Charsets.UTF_8)
        }

        fun sha256(data: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(data.toByteArray(Charsets.UTF_8))
            return hash.joinToString("") { "%02X".format(it) }
        }

        fun encryptRSA(data: ByteArray, pubKey: ByteArray): ByteArray {
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKeySpec = X509EncodedKeySpec(pubKey)
            val publicKey: PublicKey = keyFactory.generatePublic(publicKeySpec)
            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return cipher.doFinal(data)
        }

        fun generateProtectedKey(key: String, pubKey: String): String {
            val encrypted = encryptRSA(key.toByteArray(Charsets.UTF_8), Base64.getDecoder().decode(pubKey.toByteArray(Charsets.UTF_8)))
            return Base64.getEncoder().encodeToString(encrypted)
        }

    }
}