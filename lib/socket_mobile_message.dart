import 'package:equatable/equatable.dart';

import 'socket_mobile_device.dart';

class SocketMobileMessage extends Equatable {
  final String message;
  final SocketMobileDevice device;

  const SocketMobileMessage(this.message, this.device);

  @override
  List<Object> get props => [
        message,
        device,
      ];
}
