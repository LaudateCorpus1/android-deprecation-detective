# What is this?
Deprecation Detective searches through all installed Android SDK platforms and extracts classes and methods which have been marked as @deprecated.

# How to run
Compile a self-contained executable jar with:
`./gradlew fatjar`

Run it with: `java -jar build/libs/android-deprecation-detective-fat.jar -s <android-sdk-platforms> -o <output file>`

If your Android SDK platforms installation is in the default directory `/opt/android-sdk/platforms`, you can omit the arguments.

# Dependencies
- Java 8
- at least one Android SDK
- gradle
