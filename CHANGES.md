2017-11-13, Version 1.6.1
=========================

 * Update LICENSE (Diana Lau)

 * - Add Change Password to UserRepository (Mekuanent)

 * update globalize string (Diana Lau)

 * Create Issue and PR Templates (#120) (Sakib Hasan)

 * update CODEOWNERS (Diana Lau)

 * Add CODEOWNER file (Diana Lau)

 * Update README.md (Rand McKinney)

 * Update paid support URL (Siddhi Pai)

 * Add translated files (gunjpan)

 * Update deps to LB 3.0.0 RC (Miroslav Bajtoš)

 * Use loopback 3.0-alpha (Amir Jafarian)

 * Add globalization (Loay)

 * Update URLs in CONTRIBUTING.md (#99) (Ryan Graham)

 * relicense as MIT only (Ryan Graham)

 * undocument unthrown exceptions (Ryan Graham)

 * [artifactory-release] Next development version (admin)

 * Implement `findOne`; add `find` alias for `all` (Miroslav Bajtoš)

 * Fix encoding of List values in request parameters (Miroslav Bajtoš)

 * Update Gradle build version (Loay Gewily)

 * Remove logging of stream request bodies (Miroslav Bajtoš)


2015-04-15, Version 1.5.2
=========================

 * [artifactory-release] Release version 1.5.2 (admin)

 * LocalInstallation: remove false warnings (Miroslav Bajtoš)

 * Fix bug introduced by upgrade to async-http 1.4.6 (Miroslav Bajtoš)

 * Rename RestAdapter.HttpClient to prevent confusion (Miroslav Bajtoš)

 * Update DEVELOPING.md (Miroslav Bajtoš)

 * Upgrade outdated dependencies. (Miroslav Bajtoš)

 * build: code cleanup (Miroslav Bajtoš)

 * Upgrade build to support Android Studio 1.x (Miroslav Bajtoš)

 * Update README.md (Rand McKinney)

 * Add 'StrongLoop Labs' (Rand McKinney)

 * Fix bad CLA URL in CONTRIBUTING.md (Ryan Graham)

 * gitignore: ignore `.idea` anywhere in the dir tree (Miroslav Bajtoš)

 * Update Gradle build to support Android Studio 0.9 (Miroslav Bajtoš)

 * [artifactory-release] Next development version (admin)

 * Set minSdkVersion to API 8 (Jonathan)


2014-10-30, Version 1.5.0
=========================

 * [artifactory-release] Release version 1.5.0 (admin)

 * Update API docs for 1.5.0 (Miroslav Bajtoš)

 * Add .npmignore file (Miroslav Bajtoš)

 * build.gradle: update to Android 5.0 (API 21) (Miroslav Bajtoš)

 * build.gradle: log more info (Miroslav Bajtoš)

 * UserRepository: fix `logout` to use `POST` method (Miroslav Bajtoš)

 * test-server: rework auth setup to 2.0 style (Miroslav Bajtoš)

 * test-server: fix setup of push-notification (Miroslav Bajtoš)

 * Fix strong-remoting tests (Miroslav Bajtoš)

 * Update contribution guidelines (Ryan Graham)

 * Upgrade to Android Studio Beta (Miroslav Bajtoš)

 * docs/index: use a relative link to javadoc pages (Miroslav Bajtoš)

 * docs: improve the content displayed on the web (Miroslav Bajtoš)

 * Remove broken link (Rand McKinney)

 * Fix link to Javadoc (Rand McKinney)

 * Update build settings and deps (Raymond Feng)

 * Update version (Raymond Feng)

 * Update the test server (Raymond Feng)

 * Add package.json (Raymond Feng)

 * Update link to doc (Rand McKinney)

 * build: update to build tools 0.10 and gradle 1.12 (Miroslav Bajtoš)

 * [artifactory-release] Next development version: 1.3.1 (StrongLoop Jenkins)


2014-05-09, Version 1.3.0
=========================

 * [artifactory-release] Release version 1.3.0 (StrongLoop Jenkins)

 * Update API javadoc for 1.3.0 (Miroslav Bajtoš)

 * Pass contentType to File.DownloadCallback. (Miroslav Bajtoš)

 * `loginUser` updates cached current user (Miroslav Bajtoš)

 * Add findCurrentUser, getCachedCurrentUser (Miroslav Bajtoš)

 * Implement UserRepository.currentUserId (Miroslav Bajtoš)

 * Add getApplicationContext() and getRestRepository() (Miroslav Bajtoš)

 * Preserve REST accessToken in SharedPreferences (Miroslav Bajtoš)

 * Refactor TestContext to a standalone class. (Miroslav Bajtoš)

 * Fix FileTest and test-server storage setup (Miroslav Bajtoš)

 * Upgrade to Android Gradle plugin v0.9 (Miroslav Bajtoš)

 * Support custom subclasses of the User model (Miroslav Bajtoš)

 * Update license (Raymond Feng)

 * Finish implementation of Container & File. (Miroslav Bajtoš)

 * Default implentation of VoidTestCallback.onSuccess (Miroslav Bajtoš)

 * Refactor Container to extend VirtualObject (Miroslav Bajtoš)

 * Add storage service to test-server. (Miroslav Bajtoš)

 * Use Repository<T>, remove BeanUtil. (Miroslav Bajtoš)

 * Extract class AsyncTask from AsyncTestCase. (Miroslav Bajtoš)

 * Extract method AsyncTestCase.createRestAdapter (Miroslav Bajtoš)

 * Code cleanup (Miroslav Bajtoš)

 * File/Container Unit Tests (Mike Christian)

 * Refactored some file and container methods (Mike Christian)

 * More file work. (Mike Christian)

 * Container and File integration work in progress (Mike Christian)

 * Start of Container support (Mike Christian)

 * File upload and download support (Mike Christian)

 * Add include=user to UserRepository.login (Miroslav Bajtoš)

 * Code clean-up (Miroslav Bajtoš)

 * User and File/Container tests (Mike Christian)

 * User Unit Tests (Mike Christian)

 * Implemented feedback from pull request (Mike Christian)

 * Access token set and removed from header (Mike Christian)

 * login and logout with access token (Mike Christian)

 * Return the nameForRestUrl to be used by UserRepository for contract creation (Mike Christian)

 * User create, login and logout support (Mike Christian)

 * Add RestAdapter.setAccessToken & removeAccessToken (Miroslav Bajtoš)

 * Add User & AccessToken models and enable ACLs. (Miroslav Bajtoš)

 * Fix the loopback-push-notification dependency version (Raymond Feng)

 * Upgrade gradle tools for Android Studio 0.4.0 (Miroslav Bajtoš)

 * Update API javadoc for 1.2.0 (Miroslav Bajtoš)

 * [artifactory-release] Next development version (Miroslav Bajtoš)


2014-01-03, Version 1.2.0
=========================

 * [artifactory-release] Release version 1.2.0 (StrongLoop Jenkins)

 * Add timeZone & subscriptions to LocalInstallation (Miroslav Bajtoš)

 * Simplify registration of Android Installations (Miroslav Bajtoš)

 * Implement Installation Model & Repository. (Miroslav Bajtoš)

 * Code cleanup: extract field REST_SERVER_URL (Miroslav Bajtoš)

 * Improve error reporting from async callbacks (Miroslav Bajtoš)

 * Remove .idea files (Miroslav Bajtoš)

 * Update README.md (Rand McKinney)

 * Update docs.json (Rand McKinney)

 * [artifactory-release] Next development version (Miroslav Bajtos)


2013-11-19, Version 1.1.2
=========================

 * [artifactory-release] Release version 1.1.2 (slnode)

 * factory-release] Next development version (Miroslav Bajtos)


2013-11-19, Version 1.1.1
=========================

 * Get android-maven-plugin from MavenCentral (Miroslav Bajtos)

 * The Eclipse bundle unzips to a subdirectory (Miroslav Bajtos)

 * GettingStarted: modify download instructions (Miroslav Bajtos)

 * Update docs for the new Guide App. (Miroslav Bajtos)

 * [artifactory-release] Next development version (slnode)

 * Setup publishing to Sonatype OSS in build.gradle (Miroslav Bajtos)

 * Update API javadoc. (Miroslav Bajtos)

 * Update AndroidStudio project files. (Miroslav Bajtos)

 * Rename Prototype to Repository. (Miroslav Bajtos)

 * Fix Android SDK version string (Miroslav Bajtos)

 * Update GettingStarted.md (Rand McKinney)

 * Describe how to use a different version of Android (Miroslav Bajtos)

 * Improve javadoc instructions in CONTRIBUTING (Miroslav Bajtos)

 * Improve javadoc build (Miroslav Bajtos)

 * Rebuild API javadoc for v1.0.2. (Miroslav Bajtos)

 * Update README.md (Rand McKinney)

 * Extracted xrefs to API reference docs, rewording. (Rand McKinney)

 * [artifactory-release] Next development version (Miroslav Bajtos)


2013-11-01, Version 1.0.2
=========================

 * Make strong-remoting-android version configurable (Miroslav Bajtos)

 * Fix the next version to 1.0.2-SNAPSHOT. (Miroslav Bajtos)

 * [artifactory-release] Next development version (slnode)


2013-11-01, Version 1.0.1
=========================

 * First release!
