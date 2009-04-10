//
//  PrintPhonePlugin.m
//  PrintPhoneNumbersPlugin
//
//  Created by Robert Harder on 11/8/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "PrintPhonePlugin.h"


@implementation PrintPhonePlugin


/**
 * Class-level method to return a list of all plugins in our bundle of plugins.
 * In this case, we only have one kind of plugin: PrintPhonePlugin
 */
+ (NSArray*)allPlugins{
    return [USPhoneNumbersPlugin allPlugins]; // USPhoneNumbersPlugin is handler
}   // end plugins


- (NSView*)theView{
    if( !printPhoneView ){
        [NSBundle loadNibNamed:@"PrintPhoneToConsoleView" owner:self];   // Load nib
    }
    return printPhoneView;
}


- (NSString*)theViewName{
    return @"Print phone to console";

}


- (NSString *)fixPhone:(NSString *)phone withLabel:(NSString *)label fromPerson:(ABPerson *)person{
    printf("%s\n", [phone UTF8String]);
    return nil;
}

@end
