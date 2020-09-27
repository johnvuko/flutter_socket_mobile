import 'package:equatable/equatable.dart';

class SocketMobileDevice extends Equatable {
  /// friendly name
  final String name;

  /// unique ID, corresponding to the bluetooth address
  final String uuid;

  /// session ID
  final String guid;

  const SocketMobileDevice(this.name, this.uuid, this.guid);

  factory SocketMobileDevice.fromJson(Map json) {
    return SocketMobileDevice(
      json['name'],
      json['uuid'],
      json['guid'],
    );
  }

  @override
  List<Object> get props => [uuid];
}
