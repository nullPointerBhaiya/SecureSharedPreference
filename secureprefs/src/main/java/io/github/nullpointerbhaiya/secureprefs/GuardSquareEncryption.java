package io.github.nullpointerbhaiya.secureprefs;

import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * GuardSquareEncryption - High-performance AES-GCM encryption implementation
 * 
 * This class provides authenticated encryption using AES-GCM (Galois/Counter Mode).
 * GCM mode provides both confidentiality and authenticity, protecting against
 * tampering and ensuring data integrity.
 * 
 * Features:
 * - AES-256-GCM authenticated encryption
 * - Unique IV (Initialization Vector) for each encryption
 * - 128-bit authentication tag for integrity verification
 * - Base64 encoding for safe string storage
 * - No deprecated methods
 * - Faster than androidx.security.crypto
 * 
 * Security Properties:
 * - Confidentiality: Data is encrypted and unreadable without the key
 * - Authenticity: Any tampering with encrypted data is detected
 * - Integrity: Data cannot be modified without detection
 * 
 * @author NullPointerBhaiya (Abhi)
 * @version 1.0
 * @since API 23
 */
public class GuardSquareEncryption {
    
    // Cipher transformation: Algorithm/Mode/Padding
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    
    // GCM Initialization Vector length in bytes (96 bits = 12 bytes is optimal for GCM)
    private static final int GCM_IV_LENGTH = 12;
    
    // GCM authentication tag length in bits (128 bits provides strong security)
    private static final int GCM_TAG_LENGTH = 128;
    
    // Cryptographically secure random number generator for IV generation
    private final SecureRandom secureRandom;

    /**
     * Constructor - Initializes the encryption engine
     * Creates a SecureRandom instance for generating unique IVs
     */
    public GuardSquareEncryption() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * Encrypts plaintext using AES-GCM authenticated encryption.
     * 
     * Process:
     * 1. Generate a random 12-byte IV (Initialization Vector)
     * 2. Initialize AES-GCM cipher with the key and IV
     * 3. Encrypt the plaintext
     * 4. Combine IV + ciphertext (IV is needed for decryption)
     * 5. Encode as Base64 for safe string storage
     * 
     * The IV is prepended to the ciphertext because:
     * - IV must be unique for each encryption
     * - IV is not secret and can be stored with ciphertext
     * - Decryption requires the same IV used during encryption
     * 
     * @param plaintext The text to encrypt (UTF-8 encoded)
     * @param key The SecretKey from Android KeyStore
     * @return Base64 encoded string containing [IV + Ciphertext + Auth Tag]
     * @throws NoSuchPaddingException if padding scheme is not available
     * @throws NoSuchAlgorithmException if AES/GCM is not available
     * @throws InvalidAlgorithmParameterException if GCM parameters are invalid
     * @throws InvalidKeyException if the key is invalid
     * @throws IllegalBlockSizeException if block size is incorrect
     * @throws BadPaddingException if padding is incorrect (shouldn't happen with GCM)
     */
    public String encrypt(String plaintext, SecretKey key) throws NoSuchPaddingException, 
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, 
            IllegalBlockSizeException, BadPaddingException {
        
        // Generate a random IV for this encryption operation
        // Each encryption MUST use a unique IV for security
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        // Initialize cipher in encryption mode
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        // Encrypt the plaintext
        // Output includes: encrypted data + 16-byte authentication tag
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Combine IV and ciphertext into a single byte array
        // Format: [IV (12 bytes)][Ciphertext + Auth Tag (variable length)]
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);

        // Encode as Base64 for safe storage in SharedPreferences
        // NO_WRAP flag removes line breaks for cleaner storage
        return Base64.encodeToString(byteBuffer.array(), Base64.NO_WRAP);
    }

    /**
     * Decrypts ciphertext using AES-GCM authenticated decryption.
     * 
     * Process:
     * 1. Decode Base64 string to bytes
     * 2. Extract IV from first 12 bytes
     * 3. Extract ciphertext from remaining bytes
     * 4. Initialize AES-GCM cipher with key and IV
     * 5. Decrypt and verify authentication tag
     * 6. Return plaintext as UTF-8 string
     * 
     * Security Note:
     * GCM mode automatically verifies the authentication tag during decryption.
     * If data has been tampered with, BadPaddingException will be thrown.
     * 
     * @param ciphertext Base64 encoded string containing [IV + Ciphertext + Auth Tag]
     * @param key The SecretKey from Android KeyStore (same key used for encryption)
     * @return Decrypted plaintext string (UTF-8 encoded)
     * @throws NoSuchPaddingException if padding scheme is not available
     * @throws NoSuchAlgorithmException if AES/GCM is not available
     * @throws InvalidAlgorithmParameterException if GCM parameters are invalid
     * @throws InvalidKeyException if the key is invalid
     * @throws IllegalBlockSizeException if block size is incorrect
     * @throws BadPaddingException if authentication fails (data was tampered with)
     */
    public String decrypt(String ciphertext, SecretKey key) throws NoSuchPaddingException, 
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, 
            IllegalBlockSizeException, BadPaddingException {
        
        // Decode Base64 string to byte array
        byte[] decoded = Base64.decode(ciphertext, Base64.NO_WRAP);
        ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

        // Extract IV from first 12 bytes
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);

        // Extract ciphertext + auth tag from remaining bytes
        byte[] ciphertextBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(ciphertextBytes);

        // Initialize cipher in decryption mode with the same IV used for encryption
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        // Decrypt and verify authentication tag
        // If tag verification fails, BadPaddingException is thrown
        byte[] plaintext = cipher.doFinal(ciphertextBytes);
        
        // Convert decrypted bytes back to UTF-8 string
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}
