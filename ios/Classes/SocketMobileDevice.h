#ifndef SocketMobileDevice_h
#define SocketMobileDevice_h

#import <Foundation/Foundation.h>

@interface SocketMobileDevice: NSObject

-(instancetype _Nonnull)initWithName:(NSString *_Nonnull)name uuid:(NSString *_Nonnull)uuid guid:(NSString *_Nonnull)guid;

@property (strong, readonly, nonnull) NSString* name;
@property (strong, readonly, nonnull) NSString* uuid;
@property (strong, readonly, nonnull) NSString* guid;

@end

#endif /* SocketMobileDevice_h */

