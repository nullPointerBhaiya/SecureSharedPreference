# SecureSharedPreference

![GitHub issues](https://img.shields.io/github/issues/nullpointerbhaiya/SecureSharedPreference)
![GitHub forks](https://img.shields.io/github/forks/nullpointerbhaiya/SecureSharedPreference)
![GitHub stars](https://img.shields.io/github/stars/nullpointerbhaiya/SecureSharedPreference)
[![](https://jitpack.io/v/nullpointerbhaiya/SecureSharedPreference.svg)](https://jitpack.io/#nullpointerbhaiya/SecureSharedPreference)
![License](https://img.shields.io/github/license/nullpointerbhaiya/SecureSharedPreference)

An Android library that provides **encrypted SharedPreferences** with hardware-backed security using Android KeyStore. Drop-in replacement for standard SharedPreferences with automatic AES-256-GCM encryption.

## Why SecureSharedPreference?

- 🔐 **Hardware-backed encryption** - Keys stored in Android KeyStore, never exposed
- ⚡ **Fast** - Faster than androidx.security.crypto with key caching
- 🚀 **Zero dependencies** - No external libraries required
- 📱 **Wide compatibility** - Works on Android 6.0+ (API 23+)
- 🔧 **Simple API** - Works exactly like standard SharedPreferences
- ✅ **No deprecated methods** - Modern, future-proof code  

## Getting Started

To add SecureSharedPreference to your Android Studio project:

Add `implementation 'com.github.nullpointerbhaiya:SecureSharedPreference:1.0.0'` to your build.gradle dependencies block.

For example:

```gradle
dependencies {
    implementation 'com.github.nullpointerbhaiya:SecureSharedPreference:1.0.0'
}
```

## Usage

```java
import io.github.nullpointerbhaiya.secureprefs.SecureSharedPreference;

// Initialize
SecureSharedPreference prefs = new SecureSharedPreference(context, "my_prefs");

// Save data (automatically encrypted)
prefs.putString("username", "john_doe");
prefs.putInt("user_id", 12345);
prefs.putBoolean("is_premium", true);

// Retrieve data (automatically decrypted)
String username = prefs.getString("username", "guest");
int userId = prefs.getInt("user_id", 0);
boolean isPremium = prefs.getBoolean("is_premium", false);

// Other operations
prefs.contains("username");  // Check if key exists
prefs.remove("username");    // Remove a key
prefs.clear();               // Clear all data
```

See [EXAMPLE.md](EXAMPLE.md) for more examples.

## How It Works

SecureSharedPreference encrypts all data before storing it in SharedPreferences:

1. **Key Generation**: On first use, generates a hardware-backed AES-256 key in Android KeyStore
2. **Encryption**: Each value is encrypted with AES-GCM using a unique IV
3. **Storage**: Encrypted data is stored in standard SharedPreferences
4. **Decryption**: Data is automatically decrypted when retrieved

### Security Features

- **AES-256-GCM** - Industry-standard authenticated encryption
- **Hardware-backed KeyStore** - Keys never leave secure hardware
- **Unique IV per encryption** - Maximum security
- **Authentication tags** - Tamper detection built-in
- **Both keys and values encrypted** - No information leakage

## Performance

- Key caching for faster operations
- Hardware-accelerated encryption on modern devices
- ~10-20% overhead compared to standard SharedPreferences
- Faster than androidx.security.crypto

## Requirements

- **Min SDK**: 23 (Android 6.0 Marshmallow)
- **Permissions**: None
- **Dependencies**: None

## Comparison

| Feature | SecureSharedPreference | androidx.security.crypto | Standard SharedPreferences |
|---------|------------------------|--------------------------|----------------------------|
| Encryption | ✅ AES-256-GCM | ✅ AES-256-GCM | ❌ None |
| Hardware-backed | ✅ Yes | ✅ Yes | ❌ No |
| Dependencies | ✅ Zero | ❌ Multiple | ✅ Zero |
| Performance | ⚡ Fast | 🐢 Slower | ⚡ Fastest |
| Deprecated APIs | ✅ None | ⚠️ Some | ✅ None |
| Setup complexity | ✅ Simple | ⚠️ Complex | ✅ Simple |

## Supported Data Types

- `String` - Text data
- `int` - Integer numbers
- `boolean` - True/false values

More types coming in future releases!

## Note

⚠️ **Important**: While this library provides strong encryption, always follow security best practices:
- Don't store highly sensitive data (like passwords) in SharedPreferences
- Use additional security layers for critical data
- Keep your app updated with latest security patches

The encryption includes some approximation due to performance and usability constraints.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Author

Made with ❤️ by [NullPointerBhaiya](https://github.com/nullpointerbhaiya)

## Support

- ⭐ Star this repo if you find it useful
- 🐛 [Report issues](https://github.com/nullpointerbhaiya/SecureSharedPreference/issues)
- 💡 [Request features](https://github.com/nullpointerbhaiya/SecureSharedPreference/issues/new)
