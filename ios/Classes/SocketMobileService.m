#import "SocketMobileService.h"

#import <CaptureSDK/CaptureSDK.h>

@interface SocketMobileService()<SKTCaptureHelperDelegate>{
	SKTCaptureHelper* _capture;
    NSMutableArray<SocketMobileDevice *> *_connectedDevices;
}

@end

@implementation SocketMobileService

+ (nonnull instancetype)sharedInstance
{
    static SocketMobileService *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [SocketMobileService new];
    });
    return sharedInstance;
}

- (void)configureWithDeveloperId:(NSString *_Nonnull)developerID appKey:(NSString *_Nonnull)appKey appId:(NSString *_Nonnull)appId
{
    // avoid bug when hot reload
    // else should close `_capture`
    if (_capture) {
        NSLog(@"[SocketMobileService] configure call ignored because already ready");
        return;
    }
    
    _connectedDevices = [NSMutableArray<SocketMobileDevice *> new];
    
    SKTAppInfo* appInfo = [SKTAppInfo new];
    appInfo.DeveloperID = developerID;
    appInfo.AppKey = appKey;
    appInfo.AppID = appId;
    
    _capture = [SKTCaptureHelper sharedInstance];
    [_capture pushDelegate:self];
    [_capture openWithAppInfo:appInfo completionHandler:^(SKTResult result) {
        NSLog(@"[SocketMobileService] opening capture returns: %d", result);
    }];
}

- (void)didChangeDevices
{
    [_delegate didChangeDevices:_connectedDevices];
}

#pragma mark - SKTCaptureHelper delegate

/**
 * called when a error needs to be reported to the application
 *
 * @param error contains the error code
 * @param message contains an optional message, can be null
 */
-(void)didReceiveError:(SKTResult) error withMessage:(NSString*) message{
    NSLog(@"[SocketMobileService] didReceiveError %d with message: %@", error, message);
}

/**
 * called when a device has connected to the host
 *
 * @param device identifies the device that just connected
 * @param result contains an error if something went wrong during the device connection
 */
-(void)didNotifyArrivalForDevice:(SKTCaptureHelperDevice*) device withResult:(SKTResult) result{
    if (result != SKTCaptureE_NOERROR) {
        NSLog(@"[SocketMobileService] didNotifyArrivalForDevice error: %d", result);
        return;
    }
            
    NSLog(@"[SocketMobileService] didNotifyArrivalForDevice: %@ - %@", device.friendlyName, device.guid);

    [device getBluetoothAddressWithCompletionHandler:^(SKTResult result, NSArray *address) {
        if (result != SKTCaptureE_NOERROR) {
            NSLog(@"[SocketMobileService] getBluetoothAddressWithCompletionHandler error: %@ - %@ - %d", device.friendlyName, device.guid, result);
            return;
        }
        
        NSMutableArray *addressValues = [NSMutableArray new];
        for(NSNumber *value in address) {
            [addressValues addObject:[NSString stringWithFormat:@"%02X", [value intValue]]];
        }
        NSString *addressText = [addressValues componentsJoinedByString:@":"];
        
        SocketMobileDevice *socketDevice = [[SocketMobileDevice alloc] initWithName:device.friendlyName uuid:addressText guid:device.guid];
        [self->_connectedDevices addObject:socketDevice];
        [self didChangeDevices];
    }];
}

/**
 * called when a device has disconnected from the host
 *
 * @param device identifies the device that has just disconnected
 * @param result contains an error if something went wrong during the device disconnection
 */
-(void)didNotifyRemovalForDevice:(SKTCaptureHelperDevice*) device withResult:(SKTResult) result{
    if (result != SKTCaptureE_NOERROR) {
        NSLog(@"[SocketMobileService] didNotifyRemovalForDevice error: %d", result);
        return;
    }
    
    NSLog(@"[SocketMobileService] didNotifyRemovalForDevice: %@ - %@", device.friendlyName, device.guid);
  
    NSUInteger index = [_connectedDevices indexOfObjectPassingTest:^BOOL(SocketMobileDevice * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([obj.guid isEqualToString:device.guid]) {
            return true;
        }
                
        return false;
    }];
    
    if (index != NSNotFound) {
        [_connectedDevices removeObjectAtIndex:index];
        [self didChangeDevices];
    }
}

/**
 * called when decoded data are received from a device
 *
 * @param decodedData contains the decoded data
 * @param device identifies the device from which the decoded data comes from
 * @param result contains an error if something wrong happen while getting the decoded data
 * or if the SoftScan trigger operation has been cancelled
 */
-(void)didReceiveDecodedData:(SKTCaptureDecodedData*) decodedData fromDevice:(SKTCaptureHelperDevice*) device withResult:(SKTResult) result{
    NSString *text = [[decodedData.stringFromDecodedData componentsSeparatedByCharactersInSet:[NSCharacterSet newlineCharacterSet]] componentsJoinedByString:@""];
    NSLog(@"[SocketMobileService] didReceiveDecodedData %@", text);
    
    SocketMobileDevice *connectedDevice;
    for (SocketMobileDevice *currentDevice in _connectedDevices) {
        if ([currentDevice.guid isEqualToString:device.guid]) {
            connectedDevice = currentDevice;
            break;
        }
    }
    
    // never supposed to happen
    if (connectedDevice == nil) {
        NSLog(@"[SocketMobileService] didReceiveDecodedData could not find device");
        return;
    }
    
    [_delegate didReadData:text fromDevice:connectedDevice];
}

@end
