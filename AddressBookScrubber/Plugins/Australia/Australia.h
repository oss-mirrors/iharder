//
//  Australia.h
//  Australia
//
//  Created by Robert Harder on 11/1/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "ABSPlugin.h"


@interface Australia : NSObject<ABSPlugin> {
    NSString *theViewName;
    id theObject;
    IBOutlet NSView *australiaView;
}


- (id)initWithObject:(id)anObject name:(NSString*)name;
+ (Australia *)pluginFor:(id)anObject;

@end
