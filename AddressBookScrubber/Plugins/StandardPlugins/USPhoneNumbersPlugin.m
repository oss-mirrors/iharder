//
//  USPhoneNumbers.m
//  StandardPlugins
//
//  Created by Robert Harder on 11/11/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "USPhoneNumbersPlugin.h"


@implementation USPhoneNumbersPlugin



/**
 * Class-level method to return a list of all plugins in our bundle of plugins.
 * In this case, we only have one kind of plugin: PrintPhonePlugin
 */
+ (NSArray*)allPlugins{
    NSMutableArray *plugins = [[[NSMutableArray alloc] init] autorelease];  // Array to hold all (one) plugins
    
    [plugins addObject:[[[USPhoneNumbersPlugin alloc] init] autorelease]];
    [plugins addObject:[[[PrintPhonePlugin alloc] init] autorelease]];
    
    return plugins;                                                         // Return array
}   // end plugins


- (NSView*)theView{
    if( !usPhoneView ){
        [NSBundle loadNibNamed:@"USPhoneNumbersView" owner:self];   // Load nib
    }
    return usPhoneView;
}


- (NSString*)theViewName{
    return @"Format US phone numbers";
    
}


- (NSString *)fixPhone:(NSString *)phone withLabel:(NSString *)label fromPerson:(ABPerson *)person{
    bool noCountryEqualsUS = [noCountryEqualsUSCheckbox state] == NSOnState;
    bool addAreaCode = [addAreaCodeCheckbox state] == NSOnState;
    NSString *areaCode = [defaultAreaCode value];
    bool formatPhoneNumbers = [formatPhoneNumbersCheckbox state] == NSOnState;
    int  formatPhoneSelectedRow = [formatPhoneNumbersRadioMatrix selectedRow];
    
    // TODO : LEFT OFF HERE. Start moving over phone-fixing code
    PhoneNumber *pn = [[PhoneNumber alloc] initWithString:phone withLocale:@"us"];
    
    NSLog(@"From: %@, To: %@", phone, [pn format:@"%c+# %%a(#) %%p#%-%l#%%x x#%"]);
    
    
    // Find the first country code attached to an address
    /*ABMultiValue *addresses = [person valueForProperty:kABAddressProperty]; 
    NSString *countryCode;
    for( NSDictionary *addr in addresses ){
        countryCode = [addr objectForKey:kABAddressCountryCodeKey];
        if( countryCode != nil ){
            break; // Out of for loop
        }   // end if: found country code   
    }   // end for: each address
    NSLog(@"Country code for %@: %@", person, countryCode);*/
    //NSLog(@"kABAddressProperty: %@",kABAddressProperty);
    //[person valueForProperty:kABPhoneProperty];
    
//    NSLog(@"formatPhoneNumbersDashesPopup: %@",[[formatPhoneNumbersDashesPopUp selectedItem] class]);
//    NSLog(@"formatPhoneNumbersDashesPopup index: %d",[formatPhoneNumbersDashesPopUp indexOfSelectedItem]);
//    NSLog(@"formatPhoneNumbersDashesPopup title: %@",[formatPhoneNumbersDashesPopUp titleOfSelectedItem]);
    //NSLog(@"formatPhoneNumbersOtherFormatField: %@",formatPhoneNumbersOtherFormatField);
    
        
    return nil;
}

@end
