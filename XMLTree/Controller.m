#import "Controller.h"

@implementation Controller


- (void)awakeFromNib
{
    [progress setStyle:NSProgressIndicatorSpinningStyle];
    [progress setDisplayedWhenStopped:NO];

    // Commment out this line if you don't have Jaguar
    [self populateWithUsersAddress];
        
    [self updateURL:nil];
}	// end awakeFromNib



- (IBAction)checkAddress:(id)sender
{
    NSData   *respData;
    NSURL    *theURL;
    NSString *resp;
    
    [progress startAnimation:self];

    theURL = [self getURL];
    [self updateURL:sender];

    respData = [theURL resourceDataUsingCache:NO];
    [_tree release];
    _tree    = [[XMLTree treeWithURL:theURL] retain];
    
    resp = [[NSString alloc] initWithData:respData encoding:NSASCIIStringEncoding];
    
    [rawResult setString:resp];
    [rawResult display];
    
    [progress stopAnimation:self];
}



- (IBAction)findDescendent:(id)sender
{
    XMLTree *desc;
    
    desc = [_tree descendentNamed:[descendentName stringValue]];
    
    if( desc )
        [descendentResult setStringValue:[desc description]];
    else
        [descendentResult setStringValue:@"NOT FOUND"];

}



- (IBAction)updateURL:(id)sender
{
    NSURL *theURL = [self getURL];

    [url setStringValue:[theURL description]];
    [url display];
}	// end udpateURL



- (NSURL *)getURL
{
    NSString        *urlFormat;
    NSMutableString *strURL;
    
    urlFormat = @"http://ws.cdyne.com/psaddress/addresslookup.asmx/AdvancedCheckAddress?AddressLine=%@&AddressLine2=%@&City=%@&StateAbbrev=%@&ZipCode=%@&LicenseKey=%@";

    strURL = [NSMutableString localizedStringWithFormat:urlFormat,
        [address stringValue],
        @"foo", // Address line 2. Not used.
        [city stringValue],
        [state stringValue],
        [zip stringValue],
        [license stringValue] ];

    // Clean up spaces. Other non-url characters should be cleaned
    // up in a real version of such a program.
    [strURL replaceOccurrencesOfString:@" "
                            withString:@"+"
                               options:0
                                 range:NSMakeRange(0, [strURL length])];
    [strURL replaceOccurrencesOfString:@"#"
                            withString:@""
                               options:0
                                 range:NSMakeRange(0, [strURL length])];

    
    return [NSURL URLWithString:strURL];
}	// end getURL



// Commment out these last three functions if you don't have Jaguar


- (void)populateWithUsersAddress
{
    NSDictionary *addr;
    addr = [Controller myHomeAddress];
    if( addr ){
        [address setStringValue:(NSString *)[addr objectForKey:kABAddressStreetKey]];
        [city    setStringValue:(NSString *)[addr objectForKey:kABAddressCityKey]];
        [state   setStringValue:(NSString *)[addr objectForKey:kABAddressStateKey]];
        [zip     setStringValue:(NSString *)[addr objectForKey:kABAddressZIPKey]];
    }	// end if: got address
}	// end populateWithUsersAddress


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


+ (NSDictionary *)myHomeAddress
{
    ABPerson *me;
    me = [[ABAddressBook sharedAddressBook] me];

    if( me == nil )
        return nil;

    ABMultiValue *multi;
    multi = [me valueForProperty:kABAddressProperty];

    return (NSDictionary *)[Controller findFirstMatch:multi
                                            withLabel:kABAddressHomeLabel];
}



@end
