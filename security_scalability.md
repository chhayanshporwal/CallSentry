# Security, Privacy & Scalability Document
## SafeGuard - Android Application

**Version:** 1.0  
**Date:** January 31, 2026  
**Classification:** Internal

---

## 1. Security Overview

### 1.1 Security Principles

| Principle | Implementation |
|-----------|---------------|
| **Data Minimization** | Store only essential data (phone numbers, timestamps) |
| **Defense in Depth** | Multiple security layers (encryption, PIN, biometrics) |
| **Secure by Default** | All security features enabled on first run |
| **Fail Secure** | On error, default to blocking (not allowing) |

---

## 2. Data Protection

### 2.1 Data Classification

| Data Type | Sensitivity | Storage | Encryption |
|-----------|-------------|---------|------------|
| Whitelist contacts | Medium | Local DB | AES-256 |
| Blocked call logs | Low | Local DB | AES-256 |
| SMS previews | High | Local DB | AES-256 |
| App settings | Low | DataStore | Encrypted |
| PIN/Credentials | Critical | Android Keystore | Hardware-backed |

### 2.2 Encryption Implementation

```kotlin
// Database encryption using SQLCipher
val passphrase = getOrCreatePassphrase() // From Android Keystore

val db = Room.databaseBuilder(context, AppDatabase::class.java, "safeguard.db")
    .openHelperFactory(SupportFactory(passphrase))
    .build()

// Keystore-based passphrase management
private fun getOrCreatePassphrase(): ByteArray {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    
    if (!keyStore.containsAlias(DB_KEY_ALIAS)) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                DB_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()
        )
        keyGenerator.generateKey()
    }
    
    return keyStore.getKey(DB_KEY_ALIAS, null).encoded
}
```

### 2.3 Secure Data Practices

> [!IMPORTANT]
> **No Data Leaves the Device**
> - All data stored locally only
> - No cloud sync in v1.0
> - No analytics that include personal data

**Data Sanitization:**
```kotlin
// Sanitize phone numbers before storage
fun sanitizePhoneNumber(input: String): String {
    return input
        .replace(Regex("[^+0-9]"), "")
        .take(15) // ITU-T E.164 max length
}

// Sanitize display names
fun sanitizeName(input: String): String {
    return input
        .replace(Regex("[<>\"'&]"), "")
        .take(100)
}
```

---

## 3. Access Control

### 3.1 PIN Protection

```kotlin
// PIN storage using Argon2 hashing
object PinManager {
    private const val ARGON2_ITERATIONS = 3
    private const val ARGON2_MEMORY = 65536 // 64 MB
    
    fun hashPin(pin: String, salt: ByteArray): String {
        return Argon2Factory.create()
            .hash(ARGON2_ITERATIONS, ARGON2_MEMORY, 1, 
                  pin.toCharArray(), salt)
    }
    
    fun verifyPin(pin: String, hash: String): Boolean {
        return Argon2Factory.create().verify(hash, pin.toCharArray())
    }
}
```

### 3.2 Biometric Authentication

```kotlin
private fun showBiometricPrompt() {
    val biometricPrompt = BiometricPrompt(
        this,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: AuthenticationResult) {
                // Grant access
            }
            override fun onAuthenticationFailed() {
                // Show PIN fallback
            }
        }
    )
    
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock SafeGuard")
        .setSubtitle("Verify your identity")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()
    
    biometricPrompt.authenticate(promptInfo)
}
```

### 3.3 Permission Model

| Permission | Purpose | Fallback if Denied |
|------------|---------|-------------------|
| READ_PHONE_STATE | Block calls | Core feature unavailable |
| READ_CALL_LOG | Show caller info | Reduced functionality |
| RECEIVE_SMS | Block SMS | SMS blocking disabled |
| READ_CONTACTS | Import contacts | Manual entry only |
| POST_NOTIFICATIONS | Alert user | Silent operation |

---

## 4. Rate Limiting & Abuse Prevention

### 4.1 Local Rate Limiting

```kotlin
object RateLimiter {
    private val actionCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val lastReset = AtomicLong(System.currentTimeMillis())
    
    private const val WINDOW_MS = 60_000L // 1 minute
    private const val MAX_ACTIONS = 100   // Max per window
    
    fun checkLimit(action: String): Boolean {
        resetIfNeeded()
        
        val count = actionCounts.getOrPut(action) { AtomicInteger(0) }
        return count.incrementAndGet() <= MAX_ACTIONS
    }
    
    private fun resetIfNeeded() {
        val now = System.currentTimeMillis()
        if (now - lastReset.get() > WINDOW_MS) {
            actionCounts.clear()
            lastReset.set(now)
        }
    }
}
```

### 4.2 Rate Limits by Operation

| Operation | Limit | Window | Rationale |
|-----------|-------|--------|-----------|
| Add to whitelist | 50 | 1 minute | Prevent bulk spam |
| Remove from whitelist | 50 | 1 minute | Prevent accidental mass deletion |
| Export whitelist | 5 | 1 hour | Prevent data harvesting |
| Failed PIN attempts | 5 | 15 minutes | Brute force protection |
| Blocked log queries | 100 | 1 minute | Performance protection |

### 4.3 Brute Force Protection

```kotlin
object BruteForceProtector {
    private var failedAttempts = 0
    private var lockoutUntil: Long = 0
    
    private val LOCKOUT_DURATIONS = listOf(
        0L,          // 1st failure: no lockout
        0L,          // 2nd failure: no lockout
        0L,          // 3rd failure: no lockout
        30_000L,     // 4th failure: 30 seconds
        60_000L,     // 5th failure: 1 minute
        300_000L,    // 6th failure: 5 minutes
        900_000L     // 7th+ failure: 15 minutes
    )
    
    fun recordFailedAttempt(): Long {
        failedAttempts++
        val duration = LOCKOUT_DURATIONS.getOrElse(failedAttempts) { 
            LOCKOUT_DURATIONS.last() 
        }
        lockoutUntil = System.currentTimeMillis() + duration
        return duration
    }
    
    fun isLockedOut(): Boolean = System.currentTimeMillis() < lockoutUntil
    
    fun reset() { failedAttempts = 0; lockoutUntil = 0 }
}
```

---

## 5. Privacy Compliance

### 5.1 GDPR / Privacy Considerations

| Requirement | Implementation |
|-------------|---------------|
| **Right to Access** | Export all data as JSON/CSV |
| **Right to Erasure** | One-tap "Delete All Data" in settings |
| **Data Portability** | Export/import whitelist feature |
| **Purpose Limitation** | Data used only for blocking functionality |
| **Storage Limitation** | Auto-delete blocked logs after 30 days |

### 5.2 Privacy Policy Requirements

The app requires a privacy policy covering:
- What data is collected
- How data is used
- Data retention periods
- No third-party sharing
- User rights and controls

### 5.3 Data Retention

```kotlin
// Automatic log cleanup job
class LogCleanupWorker(context: Context, params: WorkerParameters) 
    : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        blockedLogDao.deleteOlderThan(thirtyDaysAgo)
        return Result.success()
    }
}

// Schedule periodic cleanup
fun scheduleLogCleanup(context: Context) {
    val request = PeriodicWorkRequestBuilder<LogCleanupWorker>(
        1, TimeUnit.DAYS
    ).build()
    
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "log_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
}
```

---

## 6. Threat Mitigation

### 6.1 Threat Model

| Threat | Severity | Mitigation |
|--------|----------|------------|
| Device theft | High | PIN/biometric lock, encrypted DB |
| Malware reading DB | High | SQLCipher encryption |
| Unauthorized whitelist changes | Medium | PIN required to modify |
| Denial of service (uninstall) | Medium | Device admin option (v2.0) |
| Shoulder surfing | Low | Masked PIN entry |
| Backup extraction | Medium | Exclude from auto-backup |

### 6.2 Backup Exclusion

```xml
<!-- AndroidManifest.xml -->
<application
    android:allowBackup="false"
    android:fullBackupContent="false"
    android:dataExtractionRules="@xml/data_extraction_rules">
```

```xml
<!-- res/xml/data_extraction_rules.xml -->
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="sharedpref" path="." />
        <exclude domain="database" path="." />
    </cloud-backup>
    <device-transfer>
        <exclude domain="database" path="." />
    </device-transfer>
</data-extraction-rules>
```

---

## 7. Scalability Considerations

### 7.1 Local Scalability

| Component | Limit | Optimization |
|-----------|-------|--------------|
| Whitelist size | 10,000 contacts | Indexed queries, in-memory cache |
| Blocked logs | 100,000 entries | Pagination, auto-cleanup |
| Lookup latency | < 10ms | Bloom filter pre-check |

### 7.2 Database Optimization

```kotlin
// Indexed whitelist table
@Entity(
    tableName = "whitelist",
    indices = [
        Index(value = ["phone_number"], unique = true),
        Index(value = ["created_at"])
    ]
)
data class WhitelistEntity(...)

// Efficient lookup with caching
class WhitelistRepository {
    private val cache = ConcurrentHashMap<String, Boolean>()
    
    suspend fun isWhitelisted(phoneNumber: String): Boolean {
        val normalized = normalizePhoneNumber(phoneNumber)
        
        // Check cache first
        cache[normalized]?.let { return it }
        
        // Query database
        val exists = whitelistDao.exists(normalized)
        cache[normalized] = exists
        return exists
    }
    
    fun invalidateCache() = cache.clear()
}
```

### 7.3 Memory Management

```kotlin
// Efficient blocked log pagination
@Query("""
    SELECT * FROM blocked_log 
    ORDER BY timestamp DESC 
    LIMIT :limit OFFSET :offset
""")
suspend fun getBlockedLogs(limit: Int, offset: Int): List<BlockedLogEntity>

// In ViewModel - use Paging 3
val blockedLogs = Pager(
    config = PagingConfig(pageSize = 50, prefetchDistance = 10)
) {
    BlockedLogPagingSource(repository)
}.flow.cachedIn(viewModelScope)
```

---

## 8. OEM Compatibility & Battery

### 8.1 OEM-Specific Issues

| Manufacturer | Issue | Solution |
|--------------|-------|----------|
| **Xiaomi** | Aggressive app killing | Request AutoStart permission |
| **Samsung** | Power management | Exempt from battery optimization |
| **OnePlus** | Deep optimization | Disable battery optimization |
| **Huawei** | Protected apps | Add to protected apps list |
| **Oppo/Vivo** | Auto-launch restriction | Manual enablement guide |

### 8.2 Battery Optimization Handling

```kotlin
fun requestBatteryOptimizationExemption(context: Context) {
    val packageName = context.packageName
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    
    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:$packageName")
        context.startActivity(intent)
    }
}
```

### 8.3 Power Efficiency

```kotlin
// Efficient foreground service (if needed)
class BlockingService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }
}

// Use WorkManager for non-critical background tasks
val constraints = Constraints.Builder()
    .setRequiresBatteryNotLow(true)
    .build()
```

---

## 9. Error Handling & Recovery

### 9.1 Graceful Degradation

| Failure | Impact | Recovery |
|---------|--------|----------|
| Database corruption | Critical | Restore from backup, recreate DB |
| Service killed | High | BootReceiver restarts service |
| Permission revoked | High | Prompt user, disable features |
| Low memory | Medium | Release caches, reduce logs |

### 9.2 Crash Reporting

```kotlin
// Custom exception handler
class SafeGuardExceptionHandler : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    
    override fun uncaughtException(t: Thread, e: Throwable) {
        // Log to local file (no remote reporting)
        val logFile = File(context.filesDir, "crash_log.txt")
        logFile.appendText("""
            Time: ${Date()}
            Thread: ${t.name}
            Exception: ${e.stackTraceToString()}
            ---
        """.trimIndent())
        
        defaultHandler?.uncaughtException(t, e)
    }
}
```

---

## 10. Security Checklist

### 10.1 Pre-Release Checklist

- [ ] All database columns encrypted
- [ ] No hardcoded secrets in code
- [ ] ProGuard/R8 obfuscation enabled
- [ ] No logging of sensitive data in release builds
- [ ] Certificate pinning (if using network)
- [ ] Input validation on all user inputs
- [ ] Backup exclusion configured
- [ ] Minimum SDK set appropriately (API 26+)
- [ ] Permissions requested at runtime
- [ ] No exported components without protection

### 10.2 ProGuard Configuration

```proguard
# proguard-rules.pro

# Keep Room entities
-keep class com.safeguard.data.local.entity.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Obfuscate everything else
-repackageclasses ''
-allowaccessmodification
```

---

## 11. Compliance & Legal

### 11.1 Play Store Requirements

| Requirement | Compliance |
|-------------|------------|
| CallScreeningService policy | Service properly declared |
| SMS permission policy | Used only for blocking, not reading |
| User consent | Clear in-app disclosures |
| Privacy policy | Required, linked in Play Store listing |

### 11.2 Sensitive Permission Declaration

```xml
<!-- Must declare why you need these permissions -->
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_CALL_LOG" />
```

> [!WARNING]
> **Google Play Review**
> Apps using call/SMS permissions undergo enhanced review. Prepare detailed justification and demonstration video for review team.

---

## 12. Future Security Enhancements (v2.0+)

- Certificate transparency for any network calls
- Remote wipe capability for lost devices
- Multi-device sync with E2E encryption
- Security audit by third-party
- Bug bounty program
- SOC 2 compliance (if B2B offering)
