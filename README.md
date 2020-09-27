# socket_mobile

WORK IN PROGRESS (only iOS for now)

Capture SDK of [Socket Mobile](https://www.socketmobile.com) for Flutter using native SDK.

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter, view our
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

## Installation

Be sure to read requirements of the native SDK:
https://docs.socketmobile.com

### iOS

In the `Podfile` uncomment:

```
    platform :ios, '9.0'
```

In the `Info.plist` add:

```
	<key>UISupportedExternalAccessoryProtocols</key>
	<array>
		<string>com.socketmobile.chs</string>
	</array>
```

### Android

https://docs.socketmobile.com/capture/java/en/latest/android/getting-started.html

in your `AndroidManifest.xml¶`:

```
    <meta-data
        android:name="com.socketmobile.capture.APP_KEY"
        android:value="..."/>
    <meta-data
        android:name="com.socketmobile.capture.DEVELOPER_ID"
        android:value="..."/>
```


## Usage

```
```

## Author

- [Jonathan VUKOVICH-TRIBOUHARET](https://github.com/jonathantribouharet)
