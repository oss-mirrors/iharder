//
//  CustomFormattingPlugin.m
//  Custom
//
//  Created by Robert Harder on 11/5/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "CustomFormattingPlugin.h"

static NSBundle* pluginBundle = nil;

@implementation CustomFormattingPlugin

+ (BOOL)initializeClass:(NSBundle*)theBundle {
	if (pluginBundle) {
		return NO;
	}
	pluginBundle = [theBundle retain];
	return YES;
}

+ (CustomFormattingPlugin *)pluginFor:(id)anObject{
    CustomFormattingPlugin* instance = [[[CustomFormattingPlugin alloc] initWithObject:anObject name:@"Custom"] autorelease];
    if (instance && [NSBundle loadNibNamed:@"MainView" owner:instance] && [instance theView]) {
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
    CustomFormattingPlugin *plug;
    NSMutableArray* plugs = [[[NSMutableArray alloc] init] autorelease];
    if ((plug = [CustomFormattingPlugin pluginFor:anObject])) {
        [plugs addObject:plug];
    }
    return [plugs count]?[plugs objectEnumerator]:nil;
}   //end pluginsFor


- (NSView*)theView{ 
    return theView; 
}


- (NSString*)theViewName{ 
    return theViewName;
}


@end
