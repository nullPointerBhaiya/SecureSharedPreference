package io.github.nullpointerbhaiya.secureprefs;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * GuardSquare - Secure key management using Android KeyStore System
 * 
 * This class handles the generation and retrieval of encryption keys using
 * Android's hardware-backed KeyStore. Keys are stored securely and never
 * leave the secure hardware environment.
 * 
 * Features:
 * - Hardware-backed key storage (when available)
 * - AES-256 encryption keys
 * - GCM mode for authenticated encryption
 * - Compatible with Android API 23+ (Marshmallow and above)
 * 
 * @author NullPointerBhaiya (Abhi)
 * @version 1.0
 * @since API 23
 */
public class GuardSquare {
    
    // Unique alias for storing the key in Android KeyStore
    private static final String ALIAS = "NULLPOINTER_BHAIYA_SECURE_PREFS";
    
    // Android KeyStore provider name
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    
    // Encryption algorithm - AES (Advanced Encryption Standard)
    private static final String ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    
    // Block mode - GCM (Galois/Counter Mode) for authenticated encryption
    private static final String BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM;
    
    // Padding - None required for GCM mode
    private static final String PADDING = KeyProperties.ENCRYPTION_PADDING_NONE;
    
    // Key size in bits - 256-bit AES key
    private static final int KEY_SIZE = 256;

    /**
     * Generates a new AES-256 encryption key in Android KeyStore.
     * 
     * This method creates a new key only if one doesn't already exist with the
     * specified alias. The key is stored in hardware-backed KeyStore (when available)
     * and is configured for both encryption and decryption operations.
     * 
     * Key Properties:
     * - Algorithm: AES-256
     * - Block Mode: GCM (Galois/Counter Mode)
     * - Padding: None (GCM doesn't require padding)
     * - Randomized Encryption: Enabled (each encryption uses unique IV)
     * - Device Unlock Required: Yes (API 28+)
     * 
     * @throws KeyStoreException if KeyStore initialization fails
     * @throws CertificateException if certificate loading fails
     * @throws IOException if KeyStore loading fails
     * @throws NoSuchAlgorithmException if AES algorithm is not available
     * @throws NoSuchProviderException if AndroidKeyStore provider is not available
     * @throws InvalidAlgorithmParameterException if key parameters are invalid
     */
    public void generateKey() throws KeyStoreException, CertificateException, IOException, 
            NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        
        // Load the Android KeyStore
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        
        // Check if key already exists to avoid regeneration
        if (!keyStore.containsAlias(ALIAS)) {
            
            // Initialize AES key generator with Android KeyStore provider
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM, ANDROID_KEYSTORE);
            
            // Build key generation parameters
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setKeySize(KEY_SIZE)
                    .setRandomizedEncryptionRequired(true);

            // Add API 28+ specific security features
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.setUnlockedDeviceRequired(true)  // Key only accessible when device is unlocked
                       .setIsStrongBoxBacked(false);      // Use TEE instead of StrongBox for compatibility
            }

            // Initialize generator with parameters and create the key
            keyGenerator.init(builder.build());
            keyGenerator.generateKey();
        }
    }

    /**
     * Retrieves the encryption key from Android KeyStore.
     * 
     * This method loads the previously generated key from the KeyStore.
     * The key never leaves the secure hardware environment - only a reference
     * is returned that can be used for cryptographic operations.
     * 
     * @return SecretKey object for encryption/decryption operations
     * @throws KeyStoreException if KeyStore access fails
     * @throws CertificateException if certificate loading fails
     * @throws IOException if KeyStore loading fails
     * @throws NoSuchAlgorithmException if key algorithm is not available
     * @throws UnrecoverableEntryException if key cannot be retrieved
     */
    public SecretKey getKey() throws KeyStoreException, CertificateException, IOException, 
            NoSuchAlgorithmException, UnrecoverableEntryException {
        
        // Load the Android KeyStore
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        
        // Retrieve the key entry from KeyStore
        KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null);
        
        // Return the SecretKey for cryptographic operations
        return entry.getSecretKey();
    }
}
