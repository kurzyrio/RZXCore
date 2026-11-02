# RZXCore v1.0.1 Release Changelog

## Security and Exploit Fixes
* **GUI Drag Exploit:** Added InventoryDragEvent validation to MenuListener to prevent players from moving or duplicating items via drag-clicking across menu boundaries.
* **Pagination State and Theft Protection:** Implemented InventoryHolder on the Menu class to ensure deterministic inventory identification. Removed premature close calls on pagination controls to eliminate desynchronization and race conditions where subsequent pages lost protection.

## Database and Storage
* **SQLite Concurrency and Lock Prevention:** Capped SQLite connection pool size to 1 and configured write-ahead logging (WAL) mode alongside a 5000ms busy timeout, eliminating SQLITE_BUSY errors during concurrent operations.
* **Atomic Batch Persistence:** Wrapped player data and namespaced extra data saves in atomic transactions with explicit commit and rollback handling.
* **Dirty State Synchronization:** Corrected dirty-tracking order in PlayerDataService so modifications occurring during an active save are preserved for subsequent synchronization.

## API Enhancements
* **Cooldown Service:** Exposed CooldownManager via CoreAPI (getCooldownManager).
* **Cross-Server Synchronization:** Added getCrossServerSync to CoreAPI and wired NoOpCrossServerSync with graceful resource disposal on plugin disable.
* **Command Framework Factory:** Added createCommandFramework to CoreAPI.
* **Subcommand Tab Completion:** Added automatic permission-checked tab completion for subcommands registered through CommandFramework.
* **Command Execution Fallthrough:** Resolved an issue where non-subcommand arguments triggered an unknown-command error instead of passing execution to the root command handler.
* **ItemBuilder Improvements:** Added an ItemStack copy constructor and updated build() to return cloned instances, preventing accidental shared mutation.
* **PlaceholderAPI Integration:** Implemented getRequiredPlugin in PlaceholderAPIHook for clean registration lifecycle.

## Build and Infrastructure
* **Java 25 Shading:** Updated Maven shade plugin ASM dependencies to version 9.10.1 to provide full support for Java 25 class file major version 69.
* **Automated CI/CD:** Enabled automated headless compilation and packaging under Eclipse Temurin JDK 25 on GitHub Actions.
* **Encoding Corrections:** Corrected corrupted UTF-8 mojibake characters across internal documentation and log strings.
