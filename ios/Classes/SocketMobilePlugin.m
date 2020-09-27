#import "SocketMobilePlugin.h"
#import "SocketMobileService.h"

@interface SocketMobilePlugin()<SocketMobileServiceDelegate>
    @property(nonatomic, retain) FlutterMethodChannel *channel;
@end

@implementation SocketMobilePlugin

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel methodChannelWithName:@"socket_mobile" binaryMessenger:[registrar messenger]];
    SocketMobilePlugin* instance = [SocketMobilePlugin new];
    instance.channel = channel;
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"configure" isEqualToString:call.method]) {
        NSString *developerId = [[call arguments] objectForKey:@"developerId"];
        NSString *appKey = [[call arguments] objectForKey:@"appKey"];
        NSString *appId = [[call arguments] objectForKey:@"appId"];
        [[SocketMobileService sharedInstance] setDelegate:self];
        [[SocketMobileService sharedInstance] configureWithDeveloperId:developerId appKey:appKey appId:appId];
        
        result([NSNull null]);
    } else {
        result(FlutterMethodNotImplemented);
    }
}

#pragma mark - SocketMobileServiceDelegate

- (void)didChangeDevices:(NSArray<SocketMobileDevice *> * _Nonnull)devices {
    NSMutableArray *devicesJSON = [NSMutableArray new];
    
    for (SocketMobileDevice *device in devices) {
        NSDictionary *deviceJSON = @{
            @"uuid": device.uuid,
            @"name": device.name,
            @"guid": device.guid,
        };
        
        [devicesJSON addObject:deviceJSON];
    }
    
    [_channel invokeMethod:@"devices" arguments:devicesJSON];
}

- (void)didReadData:(NSString * _Nonnull)message fromDevice:(SocketMobileDevice * _Nonnull)device {
    [_channel invokeMethod:@"data" arguments:@{
        @"message": message,
        @"device": @{
            @"name": device.name,
            @"uuid": device.uuid,
            @"guid": device.guid,
        }
    }];
}

@end
