<!-- Plugin description -->

## Anti-Pattern Detection Plugin

This plugin provides a tool for identifying and addressing anti-patterns in Java code bases. Enhance your code's
reliability with automated analysis and actionable insights.

**Key Features:**

- Detects deadlocks and performance issues with static and dynamic code analysis.
- Improves maintainability, scalability, and software quality.

**Detectable Anti-Patterns:**

- **For Loop Database Anti-Performance:** Identifies inefficient database access patterns within loops that can lead to
  performance bottlenecks.

To run the plugin, open the plugin window by going to `View -> Tool Windows -> Anti-Pattern Detection` or by using the
right sidebar, then fill the required configurations for your project and click on the `Run Analysis` button. The plugin
will analyze the current project and display the results

### Supported Languages

Currently, the plugin supports the following languages:

- Java and Kotlin (Note: Kotlin support is experimental)

More languages will be supported in the future.

### Privacy

Your code is yours. This plugin does not collect any data from your code or send it to any external servers. Everything
stays on your machine.

<!-- Plugin description end -->

### Building and Running the Plugin

To build the plugin, run the following command:

```shell
./gradlew buildPlugin
```

The plugin will be built to the `build/distributions` directory. You can install it in your IDE by going
to `Settings -> Plugins -> Install Plugin from Disk...`.

To run the plugin in the IDE, use the following command:

```shell
./gradlew runIde
```

To debug the plugin, run the following command (Can be used in IntelliJ IDEA):

```shell
./gradlew runIde --debug-jvm
```