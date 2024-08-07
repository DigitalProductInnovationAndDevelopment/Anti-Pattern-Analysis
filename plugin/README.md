<!-- Plugin description -->
This plugin aims to provide tools and methodologies for identifying and addressing common anti-patterns in software
development. By analyzing code bases, we aim to catch potential deadlocks, performance issues, and unnecessary code
snippets that can impede maintainability, scalability, and overall software quality.
<!-- Plugin description end -->

## Installation

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