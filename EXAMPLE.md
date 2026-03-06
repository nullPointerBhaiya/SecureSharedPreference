# GuardSquare Usage Examples

## Basic Example

```java
import io.github.nullpointerbhaiya.secureprefs.SecureSharedPreference;

public class MainActivity extends AppCompatActivity {
    private SecureSharedPreference securePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize
        securePrefs = new SecureSharedPreference(this, "user_preferences");

        // Save user data
        saveUserData();

        // Load user data
        loadUserData();
    }

    private void saveUserData() {
        securePrefs.putString("username", "john_doe");
        securePrefs.putString("email", "john@example.com");
        securePrefs.putInt("userId", 12345);
        securePrefs.putBoolean("isPremium", true);
    }

    private void loadUserData() {
        String username = securePrefs.getString("username", "Guest");
        String email = securePrefs.getString("email", "");
        int userId = securePrefs.getInt("userId", 0);
        boolean isPremium = securePrefs.getBoolean("isPremium", false);

        Log.d("UserData", "Username: " + username);
        Log.d("UserData", "Email: " + email);
        Log.d("UserData", "User ID: " + userId);
        Log.d("UserData", "Premium: " + isPremium);
    }
}
```

## Login Session Example

```java
import io.github.nullpointerbhaiya.secureprefs.SecureSharedPreference;

public class SessionManager {
    private SecureSharedPreference prefs;
    private static final String PREF_NAME = "user_session";

    public SessionManager(Context context) {
        prefs = new SecureSharedPreference(context, PREF_NAME);
    }

    public void createLoginSession(String username, String token, int userId) {
        prefs.putString("username", username);
        prefs.putString("auth_token", token);
        prefs.putInt("user_id", userId);
        prefs.putBoolean("is_logged_in", true);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("is_logged_in", false);
    }

    public String getAuthToken() {
        return prefs.getString("auth_token", "");
    }

    public void logout() {
        prefs.clear();
    }
}
```

## Settings Example

```java
import io.github.nullpointerbhaiya.secureprefs.SecureSharedPreference;

public class AppSettings {
    private SecureSharedPreference prefs;

    public AppSettings(Context context) {
        prefs = new SecureSharedPreference(context, "app_settings");
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.putBoolean("notifications_enabled", enabled);
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean("notifications_enabled", true);
    }

    public void setTheme(String theme) {
        prefs.putString("theme", theme);
    }

    public String getTheme() {
        return prefs.getString("theme", "light");
    }

    public void setFontSize(int size) {
        prefs.putInt("font_size", size);
    }

    public int getFontSize() {
        return prefs.getInt("font_size", 14);
    }
}
```

## API Key Storage Example

```java
import io.github.nullpointerbhaiya.secureprefs.SecureSharedPreference;

public class ApiKeyManager {
    private SecureSharedPreference prefs;

    public ApiKeyManager(Context context) {
        prefs = new SecureSharedPreference(context, "api_keys");
    }

    public void saveApiKey(String serviceName, String apiKey) {
        prefs.putString(serviceName + "_api_key", apiKey);
    }

    public String getApiKey(String serviceName) {
        return prefs.getString(serviceName + "_api_key", "");
    }

    public boolean hasApiKey(String serviceName) {
        return prefs.contains(serviceName + "_api_key");
    }

    public void removeApiKey(String serviceName) {
        prefs.remove(serviceName + "_api_key");
    }
}
```

## Migration from Standard SharedPreferences

```java
import io.github.nullpointerbhaiya.secureprefs.SecureSharedPreference;
import android.content.SharedPreferences;

public class PreferencesMigration {
    
    public static void migrateToSecure(Context context) {
        // Old unencrypted preferences
        SharedPreferences oldPrefs = context.getSharedPreferences("old_prefs", Context.MODE_PRIVATE);
        
        // New encrypted preferences
        SecureSharedPreference securePrefs = new SecureSharedPreference(context, "secure_prefs");
        
        // Migrate string values
        String username = oldPrefs.getString("username", "");
        if (!username.isEmpty()) {
            securePrefs.putString("username", username);
        }
        
        // Migrate int values
        int userId = oldPrefs.getInt("user_id", 0);
        if (userId != 0) {
            securePrefs.putInt("user_id", userId);
        }
        
        // Migrate boolean values
        boolean isLoggedIn = oldPrefs.getBoolean("is_logged_in", false);
        securePrefs.putBoolean("is_logged_in", isLoggedIn);
        
        // Clear old preferences after migration
        oldPrefs.edit().clear().apply();
    }
}
```

## Error Handling

```java
import io.github.nullpointerbhaiya.secureprefs.SecureSharedPreference;

public class SafePreferences {
    private SecureSharedPreference prefs;

    public SafePreferences(Context context) {
        try {
            prefs = new SecureSharedPreference(context, "safe_prefs");
        } catch (RuntimeException e) {
            Log.e("SafePreferences", "Failed to initialize secure preferences", e);
            // Fallback to standard SharedPreferences if needed
        }
    }

    public void saveData(String key, String value) {
        if (prefs != null) {
            prefs.putString(key, value);
        }
    }

    public String getData(String key, String defaultValue) {
        if (prefs != null) {
            return prefs.getString(key, defaultValue);
        }
        return defaultValue;
    }
}
```
