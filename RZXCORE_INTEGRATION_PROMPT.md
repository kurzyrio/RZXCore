# Prompt: Integrate Main Plugin with RZXCore Required Dependency

## Context
I have created a shared core library plugin called `RZXCore`. I want to refactor my main Spigot/Paper plugin to declare `RZXCore` as a hard required dependency, remove any duplicated utility/service code, and interface with the shared services provided by `RZXCore` at runtime.

> [!IMPORTANT]
> Since this is a fresh update from an older version, make sure to delete the old `KurzCore` (or any old `RZXCore`) jar from the server's `plugins/` directory before deploying the new `RZXCore` jar to avoid conflicts, as the packaging, naming conventions, and database tables have changed.

## RZXCore Specifications
- **Java target:** Compiled under Java 25 (`--release 25`).
- **Plugin Name:** `RZXCore`
- **Maven Coordinates:** `groupId: com.rzx`, `artifactId: core`, `version: 1.0.0-SNAPSHOT`
- **Base package:** `com.rzx.core`
- **Public API contract:** Accessible via `com.rzx.core.api.CoreAPI`

---

## Refactoring Requirements

> [!WARNING]
> Prior to writing any code, thoroughly analyze the existing files to check if they already contain any RZXCore imports, integrations, or dependencies. If any references exist, make sure to preserve existing features and avoid removing any necessary imports or logic. **Do not modify, touch, or alter any core features, functionalities, or gameplay mechanics of the plugin itself.**

Please guide me through the following steps to refactor my main plugin:

### 1. Maven Configuration (`pom.xml`)
Add `RZXCore` as a `provided` compile dependency to our `pom.xml`:
```xml
<dependency>
    <groupId>com.rzx</groupId>
    <artifactId>core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### 2. Plugin Description (`plugin.yml`)
Declare `RZXCore` as a hard required dependency so that our plugin only loads after `RZXCore` has successfully enabled:
```yaml
depend:
  - RZXCore
```

### 3. API Bootstrap & Version Check
On startup (`onEnable()`), lookup the `CoreAPI` instance using Bukkit's `ServicesManager` or the convenience accessor `com.rzx.core.RZXCore.getAPI()`. Check version compatibility (ensuring it matches major version `1.x.x`) and gracefully disable our plugin if it is too old:

```java
import com.rzx.core.api.CoreAPI;
import com.rzx.core.RZXCore;

private CoreAPI rzxCoreAPI;

@Override
public void onEnable() {
    // Lookup API
    rzxCoreAPI = RZXCore.getAPI(); // Or Bukkit.getServicesManager().load(CoreAPI.class);
    if (rzxCoreAPI == null) {
        getLogger().severe("RZXCore not found! Disabling plugin.");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }

    // Version Check
    String coreVersion = rzxCoreAPI.getApiVersion();
    if (!coreVersion.startsWith("1.")) {
        getLogger().severe("Incompatible RZXCore version found: " + coreVersion + ". Expected v1.x. Disabling plugin.");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }
    
    getLogger().info("Successfully hooked into RZXCore v" + coreVersion);
}
```

### 4. Code Refactoring (Replacing local logic with RZXCore services)
Modify our plugin's classes to use the following services exposed by `CoreAPI`:

#### A. Configurations & Language
Replace custom YAML file management and MiniMessage wrappers with `rzxCoreAPI.createConfigManager(...)` and `rzxCoreAPI.createMessageManager(...)`:
```java
// Create a config manager for 'custom.yml' inside our plugin's directory
ConfigManager customConfig = rzxCoreAPI.createConfigManager(this, "custom.yml");
customConfig.load();

// Load language localized files (e.g. lang/en_US.yml) using MiniMessage parsing
MessageManager langManager = rzxCoreAPI.createMessageManager(this, "en_US");
player.sendMessage(langManager.getMessage("some-key", "player", player.getName()));
```

#### B. Storage & Player Data Service
Stop maintaining our own database connections and player tables. Interact with RZXCore's shared connection pool and namespaced player data bucket instead:
```java
// Retrieve database connection (SQLite or MySQL depending on server configuration)
try (java.sql.Connection conn = rzxCoreAPI.getDatabaseManager().getConnection()) {
    // Run custom plugin-specific database queries
}

// Retrieve or update namespaced custom data for a player (runs thread-safely)
UUID uuid = player.getUniqueId();
String namespace = "myplugin";

// Set values (RZXCore caches and saves them asynchronously)
rzxCoreAPI.getPlayerDataService().setExtraData(uuid, namespace, "rank", "VIP");

// Get values
String rank = rzxCoreAPI.getPlayerDataService().getExtraData(uuid, namespace, "rank");
```

#### C. GUI Menus
Replace custom inventory listener registrations with the shared `MenuBuilder` framework:
```java
import com.rzx.core.gui.MenuBuilder;
import com.rzx.core.util.ItemBuilder;
import org.bukkit.Material;

new MenuBuilder("My Custom UI", 27)
    .setItem(13, new ItemBuilder(Material.DIAMOND).name(net.kyori.adventure.text.Component.text("Click Me!")).build(), (clicker, clickType) -> {
        clicker.sendMessage("You clicked the diamond!");
    })
    .build()
    .open(player);
```

#### D. Optional Hooks & Placeholders
Delegate third-party integrations (Vault Economy/Permissions and PlaceholderAPI) to `HookManager`:
```java
// Get player balance
double bal = rzxCoreAPI.getHookManager().getVaultHook().getBalance(player);

// Resolve internal and external placeholders in text
String parsed = rzxCoreAPI.getHookManager().resolvePlaceholders(player, "Welcome %player_name%! Playtime: %rzxcore_playtime%");
```
