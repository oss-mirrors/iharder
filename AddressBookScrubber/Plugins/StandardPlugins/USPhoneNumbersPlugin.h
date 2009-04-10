//
//  USPhoneNumbers.h
//  StandardPlugins
//
//  Created by Robert Harder on 11/11/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "ABSPlugin.h"
#import "PrintPhonePlugin.h"

@interface USPhoneNumbersPlugin : NSObject<ABSPhonePlugin> {
    
    IBOutlet NSView         *usPhoneView;
    IBOutlet NSButton       *noCountryEqualsUSCheckbox;
    
    IBOutlet NSButton       *addAreaCodeCheckbox;
    IBOutlet NSTextField    *defaultAreaCode;
    
    
    IBOutlet NSButton       *formatPhoneNumbersCheckbox;
    IBOutlet NSMatrix       *formatPhoneNumbersRadioMatrix;
    IBOutlet NSPopUpButton  *formatPhoneNumbersDashesPopUp;
    IBOutlet NSTextField    *formatPhoneNumbersOtherFormatField;
    
    
}


//- (IBAction)defaultAreaCodeCheckBoxChanged:(id)sender;  // Checkbox clicked


@end
