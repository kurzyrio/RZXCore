# RZXCore

High-performance, shared core library plugin for the RZX plugin network running on Paper 1.21.1+.

## Features

- **Configuration Management**: Reusable, hot-reloadable configuration manager with default merging and automated version migrations.
- **Localization**: Built-in translation manager with Adventure MiniMessage rendering (gradients, click/hover actions) and legacy formatting fallback.
- **Reflection Command Framework**: Annotation-based command registry (`@Command`, `@SubCommand`, `@TabComplete`) registering directly into Bukkit's internal command map.
- **Connection Pooling**: Swappable storage layer supporting SQLite (WAL mode) and MySQL with shaded and relocated HikariCP.
- **Shared Player Data**: Thread-safe in-memory cache backed by asynchronous database I/O and transactional batch persistence.
- **Optional Integrations**: Built-in hooks for Vault (Economy, Permissions) and PlaceholderAPI expansion registration.
- **GUI Engine**: Fluent inventory `MenuBuilder` with pagination controls, `InventoryHolder` verification, and drag-click exploit protection.
- **Modern Utilities**: Paper region/async scheduler utilities, fluent `ItemBuilder` item stack construction, and per-player cooldown tracking.

## Requirements

- **Server Software**: Paper 1.21.1 or compatible forks.
- **Java Runtime**: Java 25 or newer.

## Developer Integration

Add RZXCore as a provided dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>com.rzx</groupId>
    <artifactId>core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Declare dependency in `plugin.yml`:

```yaml
depend:
  - RZXCore
```

Access the shared API at runtime:

```java
import com.rzx.core.RZXCore;
import com.rzx.core.api.CoreAPI;

CoreAPI api = RZXCore.getAPI();
```

## Building

```bash
mvn clean package
```
