package io.github.nullpointerbhaiya.secureprefs;

import android.content.Context;
import android.content.SharedPreferences;

import javax.crypto.SecretKey;

/**
 * SecureSharedPreference - Encrypted SharedPreferences Library
 * 
 * A drop-in replacement for Android SharedPreferences with automatic encryption.
 * All data is encrypted using AES-256-GCM before storage, providing both
 * confidentiality and integrity protection.
 * 
 * Features:
 * - Hardware-backed encryption using Android KeyStore
 * - AES-256-GCM authenticated encryption
 * - No deprecated methods - fully compatible with modern Android
 * - Faster than androidx.security.crypto
 * - No external dependencies
 * - Simple API - works like standard SharedPreferences
 * - Supports String, int, and boolean data types
 * - Compatible with Android API 23+ (Marshmallow and above)
 * 
 * Security Benefits:
 * - Keys stored in hardware-backed KeyStore (never exposed to app)
 * - Each value encrypted with unique IV (Initialization Vector)
 * - Authentication tags prevent tampering
 * - Keys and values are both encrypted
 * 
 * Usage Example:
 * <pre>
 * // Initialize
 * SecureSharedPreference prefs = new SecureSharedPreference(context, "my_prefs");
 * 
 * // Store encrypted data
 * prefs.putString("username", "john_doe");
 * prefs.putInt("age", 25);
 * prefs.putBoolean("isPremium", true);
 * 
 * // Retrieve decrypted data
 * String username = prefs.getString("username", "guest");
 * int age = prefs.getInt("age", 0);
 * boolean isPremium = prefs.getBoolean("isPremium", false);
 * </pre>
 * 
 * @author NullPointerBhaiya (Abhi)
 * @version 1.0
 * @since API 23
 */
public class SecureSharedPreference {
    
    // Standard Android SharedPreferences for storing encrypted data
    private final SharedPreferences preferences;
    
    // Encryption engine for AES-GCM operations
    private final GuardSquareEncryption encryption;
    
    // KeyStore manager for secure key generation and retrieval
    private final GuardSquare guardSquare;
    
    // Cached encryption key to avoid repeated KeyStore access (performance optimization)
    private SecretKey cachedKey;

    /**
     * Creates a new SecureSharedPreference instance.
     * 
     * This constructor:
     * 1. Creates/opens a SharedPreferences file
     * 2. Initializes the encryption engine
     * 3. Generates or retrieves the encryption key from KeyStore
     * 4. Caches the key for better performance
     * 
     * @param context Application or Activity context
     * @param preferenceName Name of the SharedPreferences file (e.g., "user_prefs")
     * @throws RuntimeException if key initialization fails
     */
    public SecureSharedPreference(Context context, String preferenceName) {
        // Initialize standard SharedPreferences in private mode
        this.preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        
        // Initialize encryption engine
        this.encryption = new GuardSquareEncryption();
        
        // Initialize KeyStore manager
        this.guardSquare = new GuardSquare();
        
        // Generate/retrieve encryption key and cache it
        initializeKey();
    }

    /**
     * Initializes the encryption key from Android KeyStore.
     * 
     * This method:
     * 1. Generates a new key if one doesn't exist
     * 2. Retrieves the existing key if already generated
     * 3. Caches the key for performance
     * 
     * The key is stored in hardware-backed KeyStore and never leaves
     * the secure environment. Only a reference is cached.
     * 
     * @throws RuntimeException if key generation or retrieval fails
     */
    private void initializeKey() {
        try {
            // Generate key if it doesn't exist
            guardSquare.generateKey();
            
            // Retrieve and cache the key
            cachedKey = guardSquare.getKey();
        } catch (Exception e) {
            // Wrap exception with descriptive message
            throw new RuntimeException("Failed to initialize encryption key. " +
                    "Ensure device supports Android KeyStore (API 23+)", e);
        }
    }

    /**
     * Stores an encrypted string value.
     * 
     * Process:
     * 1. Encrypt the key name
     * 2. Encrypt the value
     * 3. Store encrypted key-value pair in SharedPreferences
     * 
     * Both key and value are encrypted to prevent information leakage.
     * 
     * @param key The key name (will be encrypted)
     * @param value The string value to encrypt and store
     * @throws RuntimeException if encryption or storage fails
     */
    public void putString(String key, String value) {
        try {
            // Encrypt both key and value
            String encryptedKey = encryption.encrypt(key, cachedKey);
            String encryptedValue = encryption.encrypt(value, cachedKey);
            
            // Store encrypted data
            preferences.edit().putString(encryptedKey, encryptedValue).apply();
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt and store string for key: " + key, e);
        }
    }

    /**
     * Retrieves and decrypts a string value.
     * 
     * Process:
     * 1. Encrypt the key name (to match stored encrypted key)
     * 2. Retrieve encrypted value from SharedPreferences
     * 3. Decrypt the value
     * 4. Return plaintext or default value
     * 
     * @param key The key name (will be encrypted for lookup)
     * @param defaultValue Default value if key doesn't exist or decryption fails
     * @return Decrypted string value or defaultValue
     */
    public String getString(String key, String defaultValue) {
        try {
            // Encrypt key to match stored encrypted key
            String encryptedKey = encryption.encrypt(key, cachedKey);
            
            // Retrieve encrypted value
            String encryptedValue = preferences.getString(encryptedKey, null);
            
            // Return default if key doesn't exist
            if (encryptedValue == null) {
                return defaultValue;
            }
            
            // Decrypt and return value
            return encryption.decrypt(encryptedValue, cachedKey);
        } catch (Exception e) {
            // Return default value on any error (key not found, decryption failure, etc.)
            return defaultValue;
        }
    }

    /**
     * Stores an encrypted integer value.
     * 
     * Integers are converted to strings, encrypted, and stored.
     * This approach maintains consistency with the encryption system.
     * 
     * @param key The key name (will be encrypted)
     * @param value The integer value to encrypt and store
     * @throws RuntimeException if encryption or storage fails
     */
    public void putInt(String key, int value) {
        try {
            // Encrypt key and value (convert int to string)
            String encryptedKey = encryption.encrypt(key, cachedKey);
            String encryptedValue = encryption.encrypt(String.valueOf(value), cachedKey);
            
            // Store encrypted data
            preferences.edit().putString(encryptedKey, encryptedValue).apply();
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt and store integer for key: " + key, e);
        }
    }

    /**
     * Retrieves and decrypts an integer value.
     * 
     * Process:
     * 1. Encrypt key for lookup
     * 2. Retrieve and decrypt value
     * 3. Parse string to integer
     * 4. Return integer or default value
     * 
     * @param key The key name (will be encrypted for lookup)
     * @param defaultValue Default value if key doesn't exist or parsing fails
     * @return Decrypted integer value or defaultValue
     */
    public int getInt(String key, int defaultValue) {
        try {
            // Encrypt key to match stored encrypted key
            String encryptedKey = encryption.encrypt(key, cachedKey);
            
            // Retrieve encrypted value
            String encryptedValue = preferences.getString(encryptedKey, null);
            
            // Return default if key doesn't exist
            if (encryptedValue == null) {
                return defaultValue;
            }
            
            // Decrypt and parse to integer
            return Integer.parseInt(encryption.decrypt(encryptedValue, cachedKey));
        } catch (Exception e) {
            // Return default on any error (not found, decryption failure, parse error)
            return defaultValue;
        }
    }

    /**
     * Stores an encrypted boolean value.
     * 
     * Booleans are converted to strings ("true"/"false"), encrypted, and stored.
     * 
     * @param key The key name (will be encrypted)
     * @param value The boolean value to encrypt and store
     * @throws RuntimeException if encryption or storage fails
     */
    public void putBoolean(String key, boolean value) {
        try {
            // Encrypt key and value (convert boolean to string)
            String encryptedKey = encryption.encrypt(key, cachedKey);
            String encryptedValue = encryption.encrypt(String.valueOf(value), cachedKey);
            
            // Store encrypted data
            preferences.edit().putString(encryptedKey, encryptedValue).apply();
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt and store boolean for key: " + key, e);
        }
    }

    /**
     * Retrieves and decrypts a boolean value.
     * 
     * Process:
     * 1. Encrypt key for lookup
     * 2. Retrieve and decrypt value
     * 3. Parse string to boolean
     * 4. Return boolean or default value
     * 
     * @param key The key name (will be encrypted for lookup)
     * @param defaultValue Default value if key doesn't exist or parsing fails
     * @return Decrypted boolean value or defaultValue
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            // Encrypt key to match stored encrypted key
            String encryptedKey = encryption.encrypt(key, cachedKey);
            
            // Retrieve encrypted value
            String encryptedValue = preferences.getString(encryptedKey, null);
            
            // Return default if key doesn't exist
            if (encryptedValue == null) {
                return defaultValue;
            }
            
            // Decrypt and parse to boolean
            return Boolean.parseBoolean(encryption.decrypt(encryptedValue, cachedKey));
        } catch (Exception e) {
            // Return default on any error
            return defaultValue;
        }
    }

    /**
     * Removes an encrypted key-value pair.
     * 
     * The key name is encrypted to match the stored encrypted key,
     * then the entry is removed from SharedPreferences.
     * 
     * @param key The key name to remove (will be encrypted for lookup)
     * @throws RuntimeException if encryption or removal fails
     */
    public void remove(String key) {
        try {
            // Encrypt key to match stored encrypted key
            String encryptedKey = encryption.encrypt(key, cachedKey);
            
            // Remove the encrypted entry
            preferences.edit().remove(encryptedKey).apply();
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove key: " + key, e);
        }
    }

    /**
     * Clears all encrypted preferences.
     * 
     * This removes all key-value pairs from the SharedPreferences file.
     * The encryption key in KeyStore is NOT deleted and can be reused.
     */
    public void clear() {
        preferences.edit().clear().apply();
    }

    /**
     * Checks if an encrypted key exists.
     * 
     * The key name is encrypted and checked against stored keys.
     * 
     * @param key The key name to check (will be encrypted for lookup)
     * @return true if the key exists, false otherwise
     */
    public boolean contains(String key) {
        try {
            // Encrypt key to match stored encrypted key
            String encryptedKey = encryption.encrypt(key, cachedKey);
            
            // Check if encrypted key exists
            return preferences.contains(encryptedKey);
        } catch (Exception e) {
            // Return false on any error
            return false;
        }
    }
}
