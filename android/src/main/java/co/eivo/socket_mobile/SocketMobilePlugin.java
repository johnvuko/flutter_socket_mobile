package co.eivo.socket_mobile;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** SocketMobilePlugin */
public class SocketMobilePlugin implements FlutterPlugin, MethodCallHandler, SocketMobileServiceDelegate {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "socket_mobile");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("configure")) {
      String developerId = call.argument("developerId");
      String appId = call.argument("appId");
      String appKey = call.argument("appKey");

      SocketMobileService.getInstance().setDelegate(this);
      SocketMobileService.getInstance().configure(developerId, appKey, appId);

      result.success(null);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void didChangeDevices(final ArrayList<SocketMobileDevice> devices) {
    final ArrayList<HashMap<String, String>> devicesJSON = new ArrayList<HashMap<String, String>>();

    for (SocketMobileDevice device: devices) {
      devicesJSON.add(device.toMap());
    }

    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        channel.invokeMethod("devices", devicesJSON);
      }
    });
  }

  @Override
  public void didReadData(final SocketMobileDevice device, final String message) {

    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        channel.invokeMethod("data", new HashMap<String, Object>(){{
          put("message", message);
          put("device", device.toMap());
        }});
      }
    });
  }
}
