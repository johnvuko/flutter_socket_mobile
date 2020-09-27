#import "SocketMobileService.h"

@interface SocketMobileDevice()
@end

@implementation SocketMobileDevice

-(instancetype)initWithName:(NSString *)name uuid:(NSString *)uuid guid:(NSString *)guid
{
	self = [super init];
	if (self != nil) {
        self->_name = name;
		self->_uuid = uuid;
		self->_guid = guid;
	}

	return self;
}

@end
