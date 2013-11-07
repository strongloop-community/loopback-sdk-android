## Getting Started

If you haven't already created your application backend, see the
[LoopBack Quick Start](http://docs.strongloop.com/loopback#quick-start).
Once you've got your LoopBack-powered backend running, it's time to integrate
it with your mobile application.

### Prerequisites

Before you start, make sure you've installed the [Eclipse Android Development Tools](http://developer.android.com/sdk/index.html) (ADT).

Now make sure you have the necesssary SDK tools installed.  

1. In ADT, choose **Window > Android SDK Manager**.
1. Install the following if they are not already installed:

 * Tools:
   * Android SDK Platform-tools 18 or newer (e.g. 18.1.1 or 19)
   * Android SDK Build-tools 18 (e.g. 18.1.1, but not 19)
 * Android 4.3 (API 18) 
   * SDK Platform.
 * Extras 
   * Android Support Library.

For on-device testing, you need an Android device with Android 4 or higher, or you can use a virtual Android device.
See [AVD Manager](http://developer.android.com/tools/help/avd-manager.html) for more information.

To get the  LoopBack Android guide application, you will need either the [git](http://git-scm.com/) command-line tool
or a GitHub account.

### Getting Started with the Guide App

The easiest way to get started with the LoopBack Android SDK
is with the LoopBack Android guide app. The guide app comes
ready to compile with Android Studio and each tab in the app
will guide you through the SDK features available to mobile apps.

<img src="img/getting-started-app-01.png" alt="tab Home" width="20%" />
<img src="img/getting-started-app-02.png" alt="tab 1" width="20%" />
<img src="img/getting-started-app-03.png" alt="tab 2" width="20%" />
<img src="img/getting-started-app-04.png" alt="tab 3" width="20%" />

Before you start, make sure you have set up at least one Android virtual device:
Choose **Window > Android Virtual Device Manager**.  See See [AVD Manager](http://developer.android.com/tools/help/avd-manager.html) for more information.

Start the StrongLoop Suite sample backend application.
In the directory where you installed StrongLoop Suite, enter these commands
```sh
$ cd strongloop/samples/sls-sample-app
$ slc run app
```
 
Now follow these steps to run the LoopBack Android guide app:
 
 1. Download the LoopBack guide application to your local machine from
 [GitHub](https://github.com/strongloop/loopback-android-getting-started).

    ```sh
    $ git clone git@github.com:strongloop/loopback-android-getting-started.git
    ```
  Alternatively, if you have a GitHub account, you can clone or download the repository as a zip file from 
  https://github.com/strongloop/loopback-android-getting-started.

 1. Open ADT Eclipse.

 1. Import the Loopback Guide Application to your workspace: 
     1. Choose Choose **File > Import**.
     1. Choose **Android > Existing Android Code into Workspace**.
     1. Click **Next**.
     1. Browse to the `loopback-android-getting-started` directory.
     1. Click **Finish**.

 1. Click the green **Run** button in the toolbar to run the application. Each tab (fragment) shows a different way
    to interact with the LoopBack server.
    Look at source code of fragments to see implementation details.

It takes some time for the app to initialize: Eventually, you'll see an Android virtual device window.
Click the LoopBack app icon in the home screen to view the LoopBack Android guide app.

#### Troubleshooting

**Problem**: Build fails with the message `Unable to resolve target 'android-18'`.

**Resolution**: You need to install Android 4.3 (API 18) SDK. See [Prerequisites](#prerequisites) for instructions on how to
install SDK components.

If you don't want to install an older SDK and want to use the most recent one 
(for example, Android 4.4 API 19), follow these steps:

1. Close Eclipse ADT.
1. Edit the file `project.properties` in the `loopback-android-getting-started` directory and change 
 the `target` property to the API version you have installed. For example: `target=android-19`.
1. Open Eclipse ADT again. The project should build correctly now.

### Getting started with the LoopBack SDK

If you are creating a new Android application or want to integrate an existing
application with LoopBack, then use the LoopBack SDK 
independently of the guide application.

 1. Open the ADT project you want to use with LoopBack, or
    create a new one.

 1. Open the `/sdks` folder of the distribution:

    ```sh
    open /usr/local/share/strongloop-node/strongloop/sdks/loopback-android-sdk
    ```

 1. Drag all files and folders from the new Finder window into `libs` folder
    of your ADT application.

 1. Somewhere, we're going to need an adapter to tell the SDK where to find our
 server:

    ```java
    RestAdapter adapter = new RestAdapter("http://example.com");
    ```

    This `RestAdapter` provides the starting point for all our interactions
    with the running server.

 1. Once we have access to `adapter` (for the sake of example, we'll assume the
 Adapter is available through our Fragment subclass), we can create
 basic `Model` and `ModelRepository` objects. Assuming we've previously
 created [a model named "product"](http://docs.strongloop.com/loopback#model):

    ```java
    ModelRepository productRepository = adapter.createRepository("product");
    Model pen = productRepository.createModel(
                    ImmutableMap.of("name", "Awesome Pen"));
    ```

    All the normal, magical `Model` and `ModelRepository` methods (for example,
    `create`, `destroy`, `findById`) are now available through
    `productRepository` and `pen`!

 1. Go forth and develop! Check out the [API docs](http://docs.strongloop.com/loopback-android/api/index.html) or create more
 Models with the LoopBack [CLI](http://docs.strongloop.com/loopback#model) or
 [Node API](http://docs.strongloop.com/loopback#a-simple-example).


