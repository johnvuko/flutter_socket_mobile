#ifndef SocketMobileService_h
#define SocketMobileService_h

#import <Foundation/Foundation.h>
#import "SocketMobileDevice.h"

@protocol SocketMobileServiceDelegate <NSObject>

- (void)didChangeDevices:(NSArray<SocketMobileDevice *> *_Nonnull)devices;
- (void)didReadData:(NSString *_Nonnull)message fromDevice:(SocketMobileDevice *_Nonnull)device;

@end

@interface SocketMobileService: NSObject

@property (nonatomic, weak, nullable) id <SocketMobileServiceDelegate> delegate;

+ (nonnull instancetype)sharedInstance;
- (void)configureWithDeveloperId:(NSString *_Nonnull)developerID appKey:(NSString *_Nonnull)appKey appId:(NSString *_Nonnull)appId;

@end

#endif /* SocketMobileService_h */

