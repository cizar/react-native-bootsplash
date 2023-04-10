# 🚀 react-native-bootsplash

Show a splash screen during app startup. Hide it when you are ready.<br>
**For migration from the v3, check the [`MIGRATION.md` guide](https://github.com/zoontek/react-native-bootsplash/blob/master/MIGRATION.md).**

[![mit licence](https://img.shields.io/dub/l/vibe-d.svg?style=for-the-badge)](https://github.com/zoontek/react-native-bootsplash/blob/main/LICENSE)
[![npm version](https://img.shields.io/npm/v/react-native-bootsplash?style=for-the-badge)](https://www.npmjs.org/package/react-native-bootsplash)
[![npm downloads](https://img.shields.io/npm/dt/react-native-bootsplash.svg?label=downloads&style=for-the-badge)](https://www.npmjs.org/package/react-native-bootsplash)
<br />
[![platform - android](https://img.shields.io/badge/platform-Android-3ddc84.svg?logo=android&style=for-the-badge)](https://www.android.com)
[![platform - ios](https://img.shields.io/badge/platform-iOS-000.svg?logo=apple&style=for-the-badge)](https://developer.apple.com/ios)

<p>
  <img height="520" width="256" src="https://raw.githubusercontent.com/zoontek/react-native-bootsplash/HEAD/docs/ios_demo.gif?raw=true" alt="iOS demo"></img>
  <img height="500" width="259" src="https://raw.githubusercontent.com/zoontek/react-native-bootsplash/HEAD/docs/android_demo.gif?raw=true" alt="android demo"></img>
</p>

## Funding

<a href="https://github.com/sponsors/zoontek">
  <img align="right" width="150" alt="This library helped you? Consider sponsoring!" src=".github/funding-octocat.svg">
</a>

This module is provided **as is**, I work on it in my free time.

If your company uses it in a production app, consider sponsoring this project 💰. You also can contact me for **premium** enterprise support, help with issues, prioritize bugfixes, feature requests, etc.

## Support

| version | react-native version |
| ------- | -------------------- |
| 5.0.0+  | 0.70.0+              |
| 4.0.0+  | 0.65.0+              |

## Installation

```bash
$ npm install --save react-native-bootsplash
# --- or ---
$ yarn add react-native-bootsplash
```

_⚠️  Don't forget going into the `ios` directory to execute a `pod install`._

## Setup

### Assets generation

In order to speed up the setup, we provide a **CLI** to generate assets, create the Android Drawable XML file and the iOS Storyboard file automatically ✨.

```bash
$ npx react-native generate-bootsplash --help
# --- or ---
$ yarn react-native generate-bootsplash --help
```

The command can take multiple arguments:

```bash
yarn react-native generate-bootsplash <logoPath>

Generate a launch screen using an original logo file (PNG or SVG)

Options:
  --background-color <color>  color used as launch screen background (in hexadecimal format) (default: "#fff")
  --logo-width <width>        logo width at @1x (in dp - we recommend approximately ~100) (default: 100)
  --assets-path [path]        path to your static assets directory (useful to require the logo file in JS)
  --flavor <flavor>           [android only] flavor build variant (outputs in an android resource directory other than "main")
  --platforms <platforms>     platforms to generate assets for (comma separated) (default: "android,ios")
  -h, --help                  output usage information
```

#### Full command usage example

```bash
yarn react-native generate-bootsplash assets/bootsplash_logo_original.png \
  --background-color=F5FCFF \
  --logo-width=100 \
  --assets-path=assets \
  --flavor=main \
  --platforms=android,ios
```

![](https://raw.githubusercontent.com/zoontek/react-native-bootsplash/master/docs/cli_tool.png?raw=true)

This tool relies on the naming conventions that are used in the `/example` project and will therefore create the following files:

```bash
# Only if --assets-path was specified
assets/bootsplash_logo.png
assets/bootsplash_logo@1,5x.png
assets/bootsplash_logo@2x.png
assets/bootsplash_logo@3x.png
assets/bootsplash_logo@4x.png

android/app/src/main/res/values/colors.xml (creation and edition)
android/app/src/main/res/mipmap-hdpi/bootsplash_logo.png
android/app/src/main/res/mipmap-mdpi/bootsplash_logo.png
android/app/src/main/res/mipmap-xhdpi/bootsplash_logo.png
android/app/src/main/res/mipmap-xxhdpi/bootsplash_logo.png
android/app/src/main/res/mipmap-xxxhdpi/bootsplash_logo.png

ios/YourProjectName/BootSplash.storyboard
ios/YourProjectName/Images.xcassets/BootSplashLogo.imageset/bootsplash_logo.png
ios/YourProjectName/Images.xcassets/BootSplashLogo.imageset/bootsplash_logo@2x.png
ios/YourProjectName/Images.xcassets/BootSplashLogo.imageset/bootsplash_logo@3x.png
```

### iOS

_⚠️  Only `.storyboard` files are supported ([Apple has deprecated other methods in April 2020](https://developer.apple.com/news/?id=01132020b))._

---

Edit the `ios/YourProjectName/AppDelegate.mm` file:

```obj-c
#import "AppDelegate.h"
#import "RNBootSplash.h" // add the header import

// …

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  // …
  // add this just before the function return:
  UIView *rootView = self.window.rootViewController.view; // only required by react-native >= 0.71
  [RNBootSplash initWithStoryboard:@"BootSplash" rootView:rootView]; // initialize the splash screen

  return YES;
}
```

Set the `BootSplash.storyboard` as launch screen file:

| Drag and drop the file                                                                                  | Create folder reference                                                                                 | Set as Launch Screen File                                                                               |
| ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| ![](https://raw.githubusercontent.com/zoontek/react-native-bootsplash/master/docs/xcode-1.png?raw=true) | ![](https://raw.githubusercontent.com/zoontek/react-native-bootsplash/master/docs/xcode-2.png?raw=true) | ![](https://raw.githubusercontent.com/zoontek/react-native-bootsplash/master/docs/xcode-3.png?raw=true) |

### Android

_⚠️  On Android 12, the splash screen will not appear if you start your app from the terminal / Android Studio. To see it, kill your app and restart it in normal conditions (tap on your app icon in the app launcher)._

---

1. Edit your `android/app/src/main/res/values/styles.xml` file:

```xml
<resources>

  <style name="AppTheme" parent="Theme.AppCompat.DayNight.NoActionBar">
      <!-- Your base theme customization -->
  </style>

  <!-- BootTheme should inherit from Theme.SplashScreen -->
  <style name="BootTheme" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/bootsplash_background</item>
    <item name="windowSplashScreenAnimatedIcon">@mipmap/bootsplash_logo</item>
    <item name="postSplashScreenTheme">@style/AppTheme</item>
  </style>

</resources>
```

2. Edit your `android/app/src/main/AndroidManifest.xml` file:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
  package="com.rnbootsplashexample">

  <!-- … -->

  <activity
    android:name=".MainActivity"
    android:label="@string/app_name"
    android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|screenSize|smallestScreenSize|uiMode"
    android:launchMode="singleTask"
    android:windowSoftInputMode="adjustResize"
    android:exported="true"
    android:theme="@style/BootTheme"> <!-- Apply @style/BootTheme to MainActivity -->
    <!-- … -->
  </application>
</manifest>

```

3. Finally edit your `android/app/src/main/java/com/yourprojectname/MainActivity.java` file:

```java
// …

// Add these required imports:
import android.os.Bundle;
import com.zoontek.rnbootsplash.RNBootSplash;

public class MainActivity extends ReactActivity {

  // …

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    RNBootSplash.init(this); // initialize the splash screen
    super.onCreate(savedInstanceState); // or super.onCreate(null) with react-native-screens
  }
}
```

## API

### hide()

#### Method type

```ts
type hide = (config?: { duration?: number }) => Promise<void>;
```

_⚠️  Duration values smaller than 220ms will be ignored and the default duration will be used._

#### Usage

```js
import RNBootSplash from "react-native-bootsplash";

RNBootSplash.hide(); // immediate
RNBootSplash.hide({ duration: 250 }); // fade
```

### getVisibilityStatus()

#### Method type

```ts
type VisibilityStatus = "hidden" | "visible";
type getVisibilityStatus = () => Promise<VisibilityStatus>;
```

#### Usage

```js
import RNBootSplash from "react-native-bootsplash";

RNBootSplash.getVisibilityStatus().then((status) => console.log(status));
```

## Real world example

```js
import React, { useEffect } from "react";
import { Text } from "react-native";
import RNBootSplash from "react-native-bootsplash";

function App() {
  useEffect(() => {
    const init = async () => {
      // …do multiple sync or async tasks
    };

    init().finally(async () => {
      await RNBootSplash.hide({ duration: 250 });
      console.log("BootSplash has been hidden successfully");
    });
  }, []);

  return <Text>My awesome app</Text>;
}
```

**🤙 A more complex example is available in the [`/example` folder](example).**

## With React Navigation

If you are using React Navigation, you can hide the splash screen once the navigation container and all children have finished mounting by using the `onReady` function.

```js
import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import RNBootSplash from "react-native-bootsplash";

function App() {
  return (
    <NavigationContainer onReady={() => RNBootSplash.hide()}>
      {/* content */}
    </NavigationContainer>
  );
}
```

## With react-native-bars

In order to keep fully transparent status and navigation bars on Android once the splash screen is hidden (and control them), this library play nicely with [react-native-bars](https://github.com/zoontek/react-native-bars). Check its [README](https://github.com/zoontek/react-native-bars/blob/main/README.md#with-react-native-bootsplash) for more informations.

## Testing with Jest

Testing code which uses this library requires some setup since we need to mock the native methods.

To add the mocks, create a file _jest/setup.js_ (or any other file name) containing the following code:

```js
jest.mock("react-native-bootsplash", () => {
  return {
    hide: jest.fn().mockResolvedValueOnce(),
    getVisibilityStatus: jest.fn().mockResolvedValue("hidden"),
  };
});
```

After that, we need to add the setup file in the jest config. You can add it under [setupFiles](https://jestjs.io/docs/en/configuration.html#setupfiles-array) option in your jest config file:

```json
{
  "setupFiles": ["<rootDir>/jest/setup.js"]
}
```
