# socket_mobile

Capture SDK of [Socket Mobile](https://www.socketmobile.com) for Flutter using native SDK.

## Installation

Be sure to read requirements of the native SDK:
https://docs.socketmobile.com

Add this to your package's pubspec.yaml file:

```
dependencies:
  socket_mobile: ^2.0.0
```

### iOS

In the `Podfile` uncomment:

```
    platform :ios, '10.0'
```

In the `Info.plist` add:

```
	<key>UISupportedExternalAccessoryProtocols</key>
	<array>
		<string>com.socketmobile.chs</string>
	</array>
```

### Android

You must have installed the [Campanion application](https://play.google.com/store/apps/details?id=com.socketmobile.companion).
More informations here: https://www.socketmobile.com/support/application-mode-for-android-8

In `android/app/src/main/AndroidManifest.xml` add:

```xml
<application android:networkSecurityConfig="@xml/network_security_config">
<uses-permission android:name="android.permission.BLUETOOTH" />
```

In `android/app/src/main/res/xml/network_security_config.xml` add:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
  <base-config cleartextTrafficPermitted="false" />
  <domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">localhost</domain>
    <domain includeSubdomains="false">127.0.0.1</domain>
  </domain-config>
</network-security-config>
```

## Usage

```dart
SocketMobile.shared.configure(
	developerId: appConfiguration.socketDeveloperId,
	appKeyIOS: appConfiguration.iOSSocketMobileAppKey,
	appIdIOS: appConfiguration.iOSSocketMobileAppId,
	appKeyAndroid: appConfiguration.androidSocketMobileAppKey,
	appIdAndroid: appConfiguration.androidSocketMobileAppId,
);
```

## Author

- [Jonathan VUKOVICH-TRIBOUHARET](https://github.com/jonathantribouharet)
