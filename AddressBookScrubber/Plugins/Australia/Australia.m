//
//  Australia.m
//  Australia
//
//  Created by Robert Harder on 11/1/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "Australia.h"

//	Variables common to all plug-in instances and subclasses are declared as
//	static globals. Here we store a reference to the plug-in's bundle, which would be
//	convenient to access local resources like icons and help files. We also use the bundle
//	reference as an indicator to check whether the plug-in has already been initialized.

static NSBundle* pluginBundle = nil;

@implementation Australia

+ (BOOL)initializeClass:(NSBundle*)theBundle {
	if (pluginBundle) {
		return NO;
	}
	pluginBundle = [theBundle retain];
	return YES;
}


+ (Australia *)pluginFor:(id)anObject{
    Australia* instance = [[[Australia alloc] initWithObject:anObject name:@"Australia"] autorelease];
    if (instance && [NSBundle loadNibNamed:@"AustraliaView" owner:instance] && [instance theView]) {
        return instance;
    }
    return nil;
}
 
 - (id)initWithObject:(id)anObject name:(NSString*)name {
    self = [super init];
    theViewName = [name retain];
    theObject = [anObject retain];
    return self;
}


- (void)dealloc {
    [theViewName release];
    [theObject release];
    [super dealloc];
}
 

+ (void)terminateClass {
    [pluginBundle release];
    pluginBundle = nil;
}


+ (NSEnumerator*)pluginsFor:(id)anObject {
    Australia *plug;
    NSMutableArray* plugs = [[[NSMutableArray alloc] init] autorelease];
    if ((plug = [Australia pluginFor:anObject])) {
        [plugs addObject:plug];
    }
    return [plugs count]?[plugs objectEnumerator]:nil;
}   //end pluginsFor




- (NSView*)theView{ 
    return australiaView; 
}


- (NSString*)theViewName{ 
    return theViewName;
}


@end
