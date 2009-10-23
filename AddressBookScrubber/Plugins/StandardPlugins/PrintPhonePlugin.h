//
//  PrintPhonePlugin.h
//  PrintPhoneNumbersPlugin
//
//  Created by Robert Harder on 11/8/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <AddressBook/AddressBook.h>
#import "ABSPlugin.h"
#import "USPhoneNumbersPlugin.h"

@interface PrintPhonePlugin : NSObject<ABSPhonePlugin>  {
    IBOutlet NSView         *printPhoneView;
}

@end
