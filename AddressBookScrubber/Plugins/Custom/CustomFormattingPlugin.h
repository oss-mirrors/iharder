//
//  CustomFormattingPlugin.h
//  Custom
//
//  Created by Robert Harder on 11/5/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "ABSPlugin.h"

@interface CustomFormattingPlugin : NSObject<ABSPlugin> {

    NSString *theViewName;
    id theObject;
    IBOutlet NSView *theView;
    IBOutlet NSTextField *theFormatField;
    
}


- (id)initWithObject:(id)anObject name:(NSString*)name;
+ (CustomFormattingPlugin *)pluginFor:(id)anObject;

@end
