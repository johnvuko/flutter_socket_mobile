import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

import 'socket_mobile_device.dart';
import 'socket_mobile_message.dart';

class SocketMobile {
  static const MethodChannel _channel = MethodChannel('socket_mobile');

  static final shared = SocketMobile._();
  SocketMobile._();

  List<SocketMobileDevice> devices = [];

  Stream<List<SocketMobileDevice>> get devicesStream => _devicesController.stream;
  Stream<SocketMobileMessage> get messageStream => _messageController.stream;

  final _devicesController = StreamController<List<SocketMobileDevice>>.broadcast();
  final _messageController = StreamController<SocketMobileMessage>.broadcast();

  Future<void> configure({
    required String developerIdIOS,
    required String appKeyIOS,
    required String appIdIOS,
  }) async {
    Map<String, dynamic> params;

    if (Platform.isIOS) {
      params = {
        'developerId': developerIdIOS,
        'appKey': appKeyIOS,
        'appId': appIdIOS,
      };
    } else {
      params = {};
    }

    _channel.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'devices') {
        final devicesJson = call.arguments as List;
        final devices = devicesJson.map((x) => x as Map).map((x) => SocketMobileDevice.fromJson(x)).toList();

        this.devices = devices;
        _devicesController.sink.add(devices);
      } else if (call.method == 'data') {
        final String text = call.arguments['message'];
        final Map deviceJson = call.arguments['device'];
        final device = SocketMobileDevice.fromJson(deviceJson);
        final message = SocketMobileMessage(text, device);

        _messageController.sink.add(message);
      } else {
        print('[SocketMobile] unknown method ${call.method}');
      }
    });

    await _channel.invokeMethod('configure', params);
  }
}
