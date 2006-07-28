//
//  CDYNE.m
//  Address Book Scrubber
//
//  Created by Robert Harder on Thu Dec 05 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

#import "CDYNE.h"


@implementation CDYNE






+ (NSNumber *)fixAddressBook:(ABAddressBook *)ab
                 cancelCheck:(ABSController *)cancelCheck
                    progress:(NSProgressIndicator *)progress
                     license:(NSString *)license
{
    NSArray      *people;
    NSEnumerator *peopleEnum;
    ABPerson     *person;
    unsigned int  numChanges; 

    
    people     = [ab people];
    peopleEnum = [people objectEnumerator];
    numChanges = 0;

    // Set up progress indicator
    [progress setMinValue:0];
    [progress setMaxValue:[people count]];
    [progress setIndeterminate:NO];

    while (person = (ABPerson *)[peopleEnum nextObject]) {

        Boolean changeMade = NO;

        NSLog(@"Working on person: %@", person );

        if( [cancelCheck cancelled] ){
            NSLog(@"CDYNE cancelled.");
            break;
        }	// end if: cancelled
 
        // Scrub Addresses
        ABMultiValue *addresses = [person valueForProperty:kABAddressProperty];
        NS_DURING
        if( addresses = [CDYNE doCDYNEWithAddress:addresses license:license] ){
            [person setValue:addresses forProperty:kABAddressProperty];
            changeMade = YES;
        }	// end if: change was made
        NS_HANDLER
            if ([[localException name] isEqualToString:@"CDYNELicenseRestricted"]) {
                NSRunAlertPanel(@"CDYNE license was rejected",
                                @"You probably need to wait for an hour or two. (%@)",
                                @"OK",
                                nil,
                                nil,
                                localException);
                [progress setDoubleValue:[progress maxValue]];
                break;
            }	// end if: CDYNE error
            else
                [localException raise]; /* Re-raise the exception. */
        NS_ENDHANDLER
        

        if( changeMade ){
            numChanges++;
            NSLog(@"Changed: %@", addresses );
        }	// end if: changes made

        // Only make a few changes while developing with trial license
        //if( numChanges > 1 ){
        //    numChanges = 0;
        //    break;
        //}	// end if

        // Release addresses
        [addresses release];
        addresses = nil;

        [progress incrementBy:1];
    }	// end while: each person

    [progress setIndeterminate:YES];

    return [NSNumber numberWithInt:numChanges];
}	// end fixAddressBook:







+ (ABMutableMultiValue *) doCDYNEWithAddress:(ABMultiValue *)addresses license:(NSString *)license
{
    ABMutableMultiValue *mutableAddresses;
    unsigned int mvCount;
    unsigned int i;
    Boolean hasUnsavedChanges = NO;
    XMLTree *tree;

    // Make sure what we have is changeable (mutable)
    mutableAddresses = [addresses mutableCopy];

    // Loop through addresses
    mvCount = [mutableAddresses count];
    for( i = 0; i < mvCount; i++ ){

        NSMutableDictionary *address;
        NSString            *country;
        NSString            *countryCode;

        address     = (NSMutableDictionary *)[[mutableAddresses valueAtIndex:i] mutableCopy];
        country     = (NSString *)[address objectForKey:kABAddressCountryKey];
        countryCode = (NSString *)[address objectForKey:kABAddressCountryCodeKey];

        // Only do US addresses
        if( country != nil ){
            if( [country caseInsensitiveCompare:@"US"] != NSOrderedSame )
                if( [country caseInsensitiveCompare:@"USA"] != NSOrderedSame )
                    break;
        }	// end if: country not nil
        if( countryCode != nil ){
            if( [countryCode caseInsensitiveCompare:@"US"] != NSOrderedSame )
                if( [countryCode caseInsensitiveCompare:@"USA"] != NSOrderedSame )
                    break;
        }	// end if: countryCode not nil


        // SOAP Call
        NSDictionary *result = [self wsResultForAddress:address license:@"0"];
        NSLog(@"Result: %@", [result descriptionInStringsFileFormat] );
        // Error?
        if ( WSMethodResultIsFault ((CFDictionaryRef) result) ){
            NSLog(@"Error");

            [[NSException exceptionWithName:@"CDYNE Error"
                                     reason:@"see log"
                                   userInfo:nil] raise];
        }	// end if: error

        // Else no error
        else{
            NSLog(@"OK");
        }	// end else

        

        /*
         NSLog(@"Calling treeForAddress...");
         tree = [CDYNE treeForAddress:address license:license];
        // Error?
        if( [[[tree descendentNamed:@"AddressError"] description] isEqualToString:@"true"] ){

            // License dead?
            if( [[[tree descendentNamed:@"DeliveryAddress"] description] isEqualToString:@"Your license key is restricted."] ){
                NSLog(@"License overused.");
                [[NSException exceptionWithName:@"CDYNELicenseRestricted"
                                         reason:[tree description]
                                       userInfo:nil] raise];
            }	// end if: bad license
        }	// end if: error
        else if( tree ){

            
            double dLat = [[[tree descendentNamed:@"AvgLatitude"] description] doubleValue];
            double dLon = [[[tree descendentNamed:@"AvgLongitude"] description] doubleValue];
            NSString *lat = [NSString localizedStringWithFormat:@"%f %c",
                                            dLat < 0 ? dLat*-1 : dLat,
                                                dLat < 0 ? 'S' : 'N'];
            NSString *lon = [NSString localizedStringWithFormat:@"%f %c",
                                            dLon < 0 ? dLon*-1 : dLon,
                                                dLon < 0 ? 'W' : 'E'];
            NSString *zip = [[tree descendentNamed:@"ZipCode"] description];
            
            
            [address setObject:lat forKey:@"Latitude"];
            [address setObject:lon forKey:@"Longitude"];
            [address setObject:zip forKey:kABAddressZIPKey];
            [address setObject:[address objectForKey:kABAddressStreetKey]
                                              forKey:@"CDYNE.address-used"];
            
            [mutableAddresses replaceValueAtIndex:i withValue:address];
            
            hasUnsavedChanges = YES;
        }	// end esle
         */
        
    }	// end for: each address

    // 'mutableAddresses' does not need to be released

    return hasUnsavedChanges ? mutableAddresses : nil;
}	// end :




+(NSDictionary *)wsResultForAddress:(NSDictionary *)address
                           license:(NSString *)license
{
    NSURL               *soapURL;
    NSMutableDictionary *params;
    NSString		    *methodName;
    NSString 		    *methodNamespace;
    NSString            *soapAction;


    soapURL         = [NSURL URLWithString:@"http://ws.cdyne.com/psaddress/addresslookup.asmx"];
    params          = [NSMutableDictionary dictionary];
    methodName      = @"CheckAddress";
    methodNamespace = @"http://ws.cdyne.com/";
    soapAction      = @"http://ws.cdyne.com/CheckAddress";


    [params setObject:[address objectForKey:kABAddressStreetKey] forKey:@"AddressLine"];
    [params setObject:[address objectForKey:kABAddressCityKey]  forKey:@"City"];
    [params setObject:[address objectForKey:kABAddressStateKey]  forKey:@"StateAbbrev"];
    [params setObject:[address objectForKey:kABAddressZIPKey] forKey:@"ZipCode"];
    [params setObject:license forKey:@"LicenseKey"];
    
    return [Utility callSOAPWithURL:soapURL
                           methodName:methodName
                      methodNamespace:methodNamespace
                           parameters:params
                           SOAPAction:soapAction];
}	// end





+(XMLTree *)treeForAddress:(NSDictionary *)address license:(NSString *)license
{
    NSString        *urlFormat;
    NSString        *strURL1;
    NSString        *strURL2;
    NSURL           *theURL;
    NSString        *prevCDYNEaddr;

    // An indicator if we've checked this address before...
    prevCDYNEaddr = [[address objectForKey:@"CDYNE.address-used"] description];
    if( [prevCDYNEaddr isEqualToString:[address objectForKey:kABAddressStreetKey]] )
        return nil;
    
    urlFormat = @"http://ws.cdyne.com/psaddress/addresslookup.asmx/AdvancedCheckAddress?AddressLine=%@&AddressLine2=%@&City=%@&StateAbbrev=%@&ZipCode=%@&LicenseKey=%@";
    
    strURL1 = [NSString localizedStringWithFormat:urlFormat,
        [address objectForKey:kABAddressStreetKey],
        @"", // Second address line. Not used.
        [address objectForKey:kABAddressCityKey],
        [address objectForKey:kABAddressStateKey],
        [address objectForKey:kABAddressZIPKey],
        license];
    
    strURL2 = [CDYNE urlencode:strURL1];
    
    theURL = [NSURL URLWithString:strURL2];

    // Get data
    return [XMLTree treeWithURL:theURL];
}	// end treeForAddress:




+(NSString *)urlencode:(NSString *)source
{
    NSMutableString *dest;

    dest = [[NSMutableString alloc] initWithString:source];

    // Clean up spaces. Other non-url characters should be cleaned
    // up in a real version of such a program.
    [dest replaceOccurrencesOfString:@" "
                        withString:@"+"
                            options:0
                                range:NSMakeRange(0, [dest length])];

    return dest;
}	// end urlencode


@end
