# How to contribute

Check out
[LoopBack guidelines](https://github.com/strongloop/loopback/wiki/How-To-Contribute)
for general conventions on bug reporting, coding style and pull request flow.

## Set up your development environment

 * Install [Android Studio](http://developer.android.com/sdk/installing/studio.html)
 * Open Tools &gt; Android &gt; SDK Manager, make sure you have these modules
   installed:
   * Tools &gt; Android SDK Platform-tools 18.1.0
   * Tools &gt; Android SDK Build-tools 18.1.0
   * Android 4.3 (API 18) &gt; SDK Platform
   * Extras &gt; Android Support Repository
   * Extras &gt; Android Support Library
 * Compile and run unit-test:
   * Run test server in the terminal: `node test-server`
   * In Android Studio, right-click on strong-remoting-android and
     select "Run All Tests"
   * Add an emulator configuration in AVD manager (you will do this only once)
   * Watch the tests pass.
 * Run the Gradle task `dist` to create a ZIP archive containing all JARs
   needed for using LoopBack Android SDK in an Eclipse ADT project. The archive
   will be created in `build/distributions/`.
 * Run the Gradle task `publishToMavenLocal` to publish artefacts to your
   local Maven cache. This will way you can test the changes in your
   application and/or other modules before publishing an official version.

## Update the auto-generated API javadoc

Follow these steps to update the API documentation at http://docs.strongloop.com:

 1. Update javadoc files

        $ rm -rf build/docs/javadoc/
        $ ./gradlew updateApiDocs -P version={version-to-print-in-docs} \
           -P strongRemotingVersion={version-to-reference}

 1. Commit your changes to git

        $ git add docs/api
        $ git commit
        # enter commit message
        $ git push

 1. Submit a GitHub pull request
