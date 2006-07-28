#import "Utility.h"

@implementation Utility	

/*!
 * Finds the first value in an ABMultiValue that matches a given label.
 * For instance, since an ABPerson can have more than one "home" address,
 * you should not assume there will be only one and should instead either
 * offer a choice to the user, if the context is appropriate, or simply
 * pick the first one.
 */
+ (id) findFirstMatch:(ABMultiValue *)multiValue withLabel:(NSString *)label
{
    unsigned int mvCount = 0;
    int i;

    mvCount = [multiValue count];

    for( i = 0; i < mvCount; i++ ){

        NSString *text = [multiValue labelAtIndex:i];
        NSComparisonResult result = [text compare:label];
        if( result == NSOrderedSame )
            return [multiValue valueAtIndex:i];

    }   // end for: each multivalue item

    return nil;
}   // end findFirstMatch:withLabel






+ (NSNumber *) scrubLabelsInAddressBook:(ABAddressBook *)ab cancelCheck:(id)cancelCheck
{
    NSArray      *people;
    NSEnumerator *peopleEnum;
    ABPerson     *person;
    unsigned int  numChanges;

    people     = [ab people];
    peopleEnum = [people objectEnumerator];
    numChanges = 0;

    while (person = (ABPerson *)[peopleEnum nextObject]) {

        Boolean changeMade = NO;

        if( [cancelCheck cancelled] )
            break;
        
        // Scrub Addresses
        ABMultiValue *addresses = [person valueForProperty:kABAddressProperty];
        if( addresses = [Utility scrubLabelsInAddresses:addresses] ){
            [person setValue:addresses forProperty:kABAddressProperty];
            changeMade = YES;
            [addresses release];
        }	// end if: change was made

        // Scrub Phone numbers
        ABMultiValue *phones = [person valueForProperty:kABPhoneProperty];
        if( phones = [Utility scrubLabelsInPhones:phones] ){
            [person setValue:phones forProperty:kABPhoneProperty];
            changeMade = YES;
            [phones release];
        }	// end if: change was made

        // Scrub emails
        ABMultiValue *emails = [person valueForProperty:kABEmailProperty];
        if( emails = [Utility scrubLabelsInEmails:emails ] ){
            [person setValue:emails forProperty:kABEmailProperty];
            changeMade = YES;
            [emails release];
        }	// end if: change was made

        if( changeMade )
            numChanges++;
    }	// end while: each person

    return [NSNumber numberWithInt:numChanges];
}	// end scrubLabelsInAddressBook:










+ (ABMutableMultiValue *) scrubLabelsInAddresses:(ABMultiValue *)addresses
{
    ABMutableMultiValue *mutableAddresses;
    unsigned int mvCount;
    unsigned int i;
    Boolean hasUnsavedChanges = NO;

    // Make sure what we have is changeable (mutable)
    mutableAddresses = [addresses mutableCopy];

    // Loop through addresses
    mvCount = [mutableAddresses count];
    for( i = 0; i < mvCount; i++ ){

        // Check to see if labels are properly named
        NSString *text;
        NSComparisonResult result;

        // Home
        text = [mutableAddresses labelAtIndex:i];
        result = [text caseInsensitiveCompare:@"home"];
        if( result == NSOrderedSame ){
            [mutableAddresses replaceLabelAtIndex:i withLabel:kABAddressHomeLabel];
            hasUnsavedChanges = YES;
        }   // end if: found match

        // Work
        result = [text caseInsensitiveCompare:@"work"];
        if( result == NSOrderedSame ){
            [mutableAddresses replaceLabelAtIndex:i withLabel:kABAddressWorkLabel];
            hasUnsavedChanges = YES;
        }   // end if: found match

    }	// end for: each address

    // 'mutableAddresses' does not need to be released

    return hasUnsavedChanges ? mutableAddresses : nil;
}	// end scrubLabelsInAddresses:






/*!
* Cleans up addresses and returns the modified addresses,
 * if any changes were made, or nil if no changes were made.
 */
+ (ABMutableMultiValue *) scrubLabelsInPhones:(ABMultiValue *)phones
{
    ABMutableMultiValue *mutablePhones;
    NSMutableDictionary *labelMap;
    unsigned int mvCount;
    unsigned int i;
    Boolean hasUnsavedChanges = NO;

    // Make sure what we have is changeable (mutable)
    mutablePhones = [phones mutableCopy];

    // Map common mistakes in labels to real label names
    labelMap = [NSMutableDictionary dictionaryWithCapacity:20];
    [labelMap setObject:kABPhoneHomeLabel    forKey:@"home"];
    [labelMap setObject:kABPhoneWorkLabel    forKey:@"work"];
    [labelMap setObject:kABPhoneMobileLabel  forKey:@"mobile"];
    [labelMap setObject:kABPhoneMobileLabel  forKey:@"cell"];
    [labelMap setObject:kABPhoneMainLabel    forKey:@"main"];
    [labelMap setObject:kABPhoneHomeFAXLabel forKey:@"home fax"];
    [labelMap setObject:kABPhoneHomeFAXLabel forKey:@"homefax"];
    [labelMap setObject:kABPhoneWorkFAXLabel forKey:@"work fax"];
    [labelMap setObject:kABPhoneHomeFAXLabel forKey:@"workfax"];
    [labelMap setObject:kABPhonePagerLabel   forKey:@"pager"];

    // Loop through phones
    mvCount = [mutablePhones count];
    for( i = 0; i < mvCount; i++ ){

        // Check to see if labels are properly named
        NSString           *existingLabel;
        NSString           *commonLabel;
        NSEnumerator       *commonEnum;
        NSComparisonResult  result;

        // Set up
        existingLabel = [mutablePhones labelAtIndex:i];
        commonEnum    = [labelMap keyEnumerator];

        // Each known common mistake with labels
        while( commonLabel = (NSString *)[commonEnum nextObject] ){
            result = [existingLabel caseInsensitiveCompare:commonLabel];
            if( result == NSOrderedSame ){
                // Fix mistake
                [mutablePhones
                    replaceLabelAtIndex:i
                              withLabel:[labelMap objectForKey:commonLabel]];
                hasUnsavedChanges = YES;
                break; // Out of while loop
            }   // end if: found match
        }   // end while: each common mistake

    }	// end for: each phone

    // 'mutablePhones' does not need to be released

    return hasUnsavedChanges ? mutablePhones : nil;
}	// end scrubLabelsInPhones:





+ (ABMutableMultiValue *) scrubLabelsInEmails:(ABMultiValue *)emails
{
    ABMutableMultiValue *mutableEmails;
    NSMutableDictionary *labelMap;
    unsigned int mvCount;
    unsigned int i;
    Boolean hasUnsavedChanges = NO;

    // Make sure what we have is changeable (mutable)
    mutableEmails = [emails mutableCopy];

    // Map common mistakes in labels to real label names
    labelMap = [NSMutableDictionary dictionaryWithCapacity:20];
    [labelMap setObject:kABEmailHomeLabel    forKey:@"home"];
    [labelMap setObject:kABEmailWorkLabel    forKey:@"work"];

    // Loop through phones
    mvCount = [mutableEmails count];
    for( i = 0; i < mvCount; i++ ){

        // Check to see if labels are properly named
        NSString           *existingLabel;
        NSString           *commonLabel;
        NSEnumerator       *commonEnum;
        NSComparisonResult  result;

        // Set up
        existingLabel = [mutableEmails labelAtIndex:i];
        commonEnum    = [labelMap keyEnumerator];

        // Each known common mistake with labels
        while( commonLabel = (NSString *)[commonEnum nextObject] ){
            result = [existingLabel caseInsensitiveCompare:commonLabel];
            if( result == NSOrderedSame ){
                // Fix mistake
                [mutableEmails
                    replaceLabelAtIndex:i
                              withLabel:[labelMap objectForKey:commonLabel]];
                hasUnsavedChanges = YES;
                break; // Out of while loop
            }   // end if: found match
        }   // end while: each common mistake

    }	// end for: each email

    // 'mutableEmails' does not need to be released

    return hasUnsavedChanges ? mutableEmails : nil;
}	// end scrubLabelsInEmails:












+ (NSNumber *) formatPhoneNumbersInAddressBook:(ABAddressBook *)ab
                                    withFormat:(NSString *)format
                               defaultAreaCode:(NSString *)defaultAreaCode
                            defaultCountryCode:(NSString *)defaultCountryCode
                                   cancelCheck:(id)cancelCheck
{
    NSArray      *people;
    NSEnumerator *peopleEnum;
    ABPerson     *person;
    unsigned int  numChanges;

    people     = [ab people];
    peopleEnum = [people objectEnumerator];
    numChanges = 0;
    
    while (person = (ABPerson *)[peopleEnum nextObject]) {

        if( [cancelCheck cancelled] )
            break;
        
        // Scrub Phone numbers
        ABMultiValue *phones = [person valueForProperty:kABPhoneProperty];
        ABMultiValue *newPhones;
        
        if( newPhones = [Utility formatPhoneNumbers:phones
                                         withFormat:format
                                    defaultAreaCode:defaultAreaCode
                                 defaultCountryCode:defaultCountryCode]){
            
            [person setValue:newPhones forProperty:kABPhoneProperty];
            [newPhones release];
            numChanges++;
        }	// end if: change was made

        // 'person' does not need to be released
        // 'phones' does not need to be released
    }	// end while: each person

    // 'people' does not need to be released
    // 'peopleEnum' does not need to be released
    
    return [NSNumber numberWithInt:numChanges];
}	// end formatPhoneNumbersInAddressBook:




+ (ABMutableMultiValue *) formatPhoneNumbers:(ABMultiValue *)phones
                                  withFormat:(NSString *)format
                             defaultAreaCode:(NSString *)defaultAreaCode
                          defaultCountryCode:(NSString *)defaultCountryCode
{
    ABMutableMultiValue *mutablePhones;
    unsigned int mvCount;
    unsigned int i;
    Boolean hasUnsavedChanges = NO;

    // Make sure what we have is changeable (mutable)
    mutablePhones = [phones mutableCopy];

    // Loop through phones
    mvCount = [mutablePhones count];
    for( i = 0; i < mvCount; i++ ){

        // Parse the phone number
        NSString           *existingValue;
        PhoneNumber        *phone;

        existingValue = [[mutablePhones valueAtIndex:i] retain];
        
        phone         = [[[PhoneNumber alloc] initWithString:existingValue
                                                  withLocale:@"us"] retain];

        if( [phone appearsValid] ){

            if( defaultAreaCode && ![phone areaCode] )
                [phone setAreaCode:defaultAreaCode];
            
            if( defaultCountryCode && ![phone countryCode] )
                [phone setCountryCode:defaultCountryCode];

            if( defaultCountryCode )
                [phone setUseCountryCode:YES];
            else
                [phone setUseCountryCode:NO];
            
            NSString *newVal = [[phone format:format] retain];
            
            if( [newVal compare:existingValue] != NSOrderedSame ){
                [mutablePhones replaceValueAtIndex:i withValue:newVal];
                //        printf("Old: %s New: %s\n", [existingValue cString], [[phone format:format] cString]);
                hasUnsavedChanges = YES;
            }	// end if: change

            [newVal release];
            newVal = nil;
        }	// end if: apears valid

        [existingValue release];
        [phone release];
    }	// end for: each phone

    // 'mutablePhones' does not need to be released

    return hasUnsavedChanges ? mutablePhones : nil;
}	// end formatPhoneNumbers:withFormat:

//http://www.mulle-kybernetik.com/artikel/Optimization/opti.html







+ (NSNumber *)fixZipCodesInAddressBook:(ABAddressBook *)ab cancelCheck:(id)cancelCheck
{
    NSArray      *people;
    NSEnumerator *peopleEnum;
    ABPerson     *person;
    unsigned int  numChanges;

    people     = [ab people];
    peopleEnum = [people objectEnumerator];
    numChanges = 0;

    while (person = (ABPerson *)[peopleEnum nextObject]) {

        Boolean changeMade = NO;

        if( [cancelCheck cancelled] ){
            NSLog(@"Fixing zip codes cancelled.");
            break;
        }	// end if: cancelled

        // Scrub Addresses
        ABMultiValue *addresses = [person valueForProperty:kABAddressProperty];
        if( addresses = [Utility fixZipCodesInAddresses:addresses] ){
            [person setValue:addresses forProperty:kABAddressProperty];
            changeMade = YES;
            [addresses release];
        }	// end if: change was made
        
        if( changeMade )
            numChanges++;
    }	// end while: each person

    return [NSNumber numberWithInt:numChanges];
}	// end fixZipCodesInAddressBook:




+ (ABMutableMultiValue *) fixZipCodesInAddresses:(ABMultiValue *)addresses
{
    ABMutableMultiValue *mutableAddresses;
    unsigned int mvCount;
    unsigned int i;
    Boolean hasUnsavedChanges = NO;

    // Make sure what we have is changeable (mutable)
    mutableAddresses = [addresses mutableCopy];

    // Loop through addresses
    mvCount = [mutableAddresses count];
    for( i = 0; i < mvCount; i++ ){

        NSMutableDictionary *address;
        NSString            *country;
        NSString            *countryCode;
        NSString            *priorZip;
        NSString            *newZip;
        NSString            *numbersOnly;

        address     = (NSMutableDictionary *)[[mutableAddresses valueAtIndex:i] mutableCopy];
        priorZip    = (NSString *)[address objectForKey:kABAddressZIPKey];
        country     = (NSString *)[address objectForKey:kABAddressCountryKey];
        countryCode = (NSString *)[address objectForKey:kABAddressCountryCodeKey];
        newZip      = nil;
        numbersOnly = nil;
        
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
        
        // Check zip code
        numbersOnly = [[Utility numbersInString:priorZip] retain];
        NSString *plusFour;
        
        if( [numbersOnly length] == 5){
            
            newZip = [[numbersOnly substringToIndex:5] retain];
        }	// end if: 5
        
        else if( [numbersOnly length] >= 6 ){
            
                plusFour = [[numbersOnly substringFromIndex:5] retain];
                switch( [plusFour intValue] ){
                    case 0:
                    case INT_MIN:
                    case INT_MAX:
                        newZip = [[numbersOnly substringToIndex:5] retain];
                        break;
                    default:
                        if( [numbersOnly length] == 9 ){
                            newZip = [NSString localizedStringWithFormat:@"%@-%@",
                                [numbersOnly substringToIndex:5],
                                [numbersOnly substringWithRange:NSMakeRange(5,4)]];
                        }	// end if: zip+4
                        else{
                            // ...
                        }	// end else: wrong number of chars
                        break;
                }	// end switch
                [plusFour release];
                plusFour = nil;
        }	// end else if
        
        else{
            // short zip
        }	// end else
        [numbersOnly release];
        numbersOnly = nil;

        if( newZip != nil && [newZip compare:priorZip] != NSOrderedSame){
            
            [address setObject:newZip forKey:kABAddressZIPKey];
            [mutableAddresses replaceValueAtIndex:i withValue:address];
            hasUnsavedChanges = YES;
            [newZip release];
            newZip = nil;
        }   // end if: found match
         

    }	// end for: each address

    // 'mutableAddresses' does not need to be released

    return hasUnsavedChanges ? mutableAddresses : nil;
}	// end scrubLabelsInAddresses:




+ (NSString *)numbersInString:(NSString *)source
{
    NSMutableString *numbersOnly;
    unsigned int     i, length;
    unichar          c;

    length      = [source length];
    numbersOnly = [[[NSMutableString alloc] initWithCapacity:length] retain];

    for( i = 0; i < length; i++ ){

        c = [source characterAtIndex:i];
        if( c >= '0' && c <= '9' )
            [numbersOnly appendFormat:@"%c",c];

    }	// end for: each char

    NSString *returnVal = [[NSString alloc] initWithString:numbersOnly];
    [numbersOnly release];

    return returnVal;
}	// end numbersInString




+(NSDictionary *)callSOAPWithURL:(NSURL *)url
                      methodName:(NSString *)methodName
                 methodNamespace:(NSString *)methodNamespace
                      parameters:(NSDictionary *)parameters
                      SOAPAction:(NSString *)SOAPAction
{
    WSMethodInvocationRef soapCall;

    // Create call
    soapCall = WSMethodInvocationCreate(
                                        (CFURLRef)url,
                                        (CFStringRef)methodName,
                                        kWSSOAP2001Protocol );

    // Set Namespace
    if( methodNamespace )
        WSMethodInvocationSetProperty(
                                      soapCall,
                                      kWSSOAPMethodNamespaceURI,
                                      (CFTypeRef)methodNamespace );

    // Set Parameters
    if( parameters )
        WSMethodInvocationSetParameters(
                                        soapCall,
                                        (CFDictionaryRef)parameters,
                                        NULL );
    // Set SOAPAction
    if( SOAPAction )
        WSMethodInvocationSetProperty(
                                      soapCall,
                                      kWSHTTPExtraHeaders,
                                      (CFTypeRef)[NSDictionary dictionaryWithObject:
                                                                  SOAPAction forKey:@"SOAPAction"] );
    
    // Set Timeout
    /*
        WSMethodInvocationSetProperty(
                                      soapCall,
                                      kWSMethodInvocationTimeoutValue,
                                      [NSNumber numberWithInt:20] );
    */

    return (NSDictionary *)WSMethodInvocationInvoke( soapCall );
}	// end callSOAPWithURL:methodName:methodNamespace:parameters:SOAPAction:










@end // end Utility.m
