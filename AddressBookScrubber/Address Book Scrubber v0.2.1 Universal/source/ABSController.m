#import "ABSController.h"


@interface ABSController (PrivateAPI)

- (NSString *)dashesFormat;

@end




@implementation ABSController

- (NSString *)dashesFormat
{
    NSString *format;
    unsigned int index = [formatPopup indexOfSelectedItem];

    switch( index ){
        case 0: // Dash
            format = @"%a#-%%p#-%%l#%%x x#%";
            break;
        case 1: // Period
            format = @"%a#.%%p#.%%l#%%x x#%";
            break;
        case 2: // Space
            format = @"%a# %%p# %%l#%%x x#%";
            break;
        default:
            format = @"%a#-%%p#-%%l#%%x x#%";
            break;
    }	// end switch

    return format;
}	// end dashesFormat




- (void)awakeFromNib
{
    NSString *backupApp = [@"/Applications/Backup.app" stringByStandardizingPath];
    NSFileManager *fm = [NSFileManager defaultManager];
    Boolean isDir;

    // Is Backup installed?
    if( [fm fileExistsAtPath:backupApp isDirectory:&isDir] && isDir ){
        [launchBackupButton setTransparent:NO];
        [launchBackupButton setEnabled:YES];
    }	// end if


    // Get 'me' pic
    ABAddressBook *ab = [ABAddressBook sharedAddressBook];
    ABPerson      *me = [ab me];
    NSImage  *meImage = [[[NSImage alloc] initWithData:[me imageData]] retain];

    if( meImage ){
        [meImageView setImage:meImage];
        [meImageView setImageFrameStyle:NSImageFramePhoto];
    }	// end if: got my pic
    [meImage release];

    // Get 'me' area code
    if( [PREFS objectForKey:@"defaultAreaCode"] == nil ){
        ABMultiValue *myPhones;
        NSString     *onePhoneStr;
        PhoneNumber  *onePhone;
        NSString     *myAreaCode;
        myPhones    = [[me valueForProperty:kABPhoneProperty] retain];
        onePhoneStr = [(NSString *)[Utility findFirstMatch:myPhones
                                                withLabel:kABPhoneHomeLabel] retain];
        
        onePhone    = [[[PhoneNumber alloc] initWithString:onePhoneStr
                                                withLocale:@"us"] retain];
        myAreaCode  = [[onePhone areaCode] retain];
        [myPhones release];
        [onePhoneStr release];
        [onePhone release];
        myPhones    = nil;
        onePhoneStr = nil;
        onePhone    = nil;
        
        if( myAreaCode ){
            [defaultAreaCode setStringValue:myAreaCode];
            [defaultAreaCode setEnabled:YES];
            [defaultAreaCodeCheckBox setState:NSOffState];
            [myAreaCode release];
        }	// end if: got an area code
    }	// end if: need an area code
    else{
        [defaultAreaCode setStringValue:[PREFS stringForKey:@"defaultAreaCode"]];
    }	// end else


    // Check to see if prefs have ever been set
    if( [PREFS objectForKey:@"fixLabels"] == nil )
        [PREFS setBool:YES forKey:@"fixLabels"];
    
    if( [PREFS objectForKey:@"formatPhoneNumbers"] == nil )
        [PREFS setBool:YES forKey:@"formatPhoneNumbers"];
    
    if( [PREFS objectForKey:@"fixZipCodes"] == nil )
        [PREFS setBool:YES forKey:@"fixZipCodes"];
    
    if( [PREFS objectForKey:@"useParens"] == nil )
        [PREFS setBool:YES forKey:@"useParens"];
    
    if( [PREFS objectForKey:@"useDefaultAreaCode"] == nil )
        [PREFS setBool:NO forKey:@"useDefaultAreaCode"];

    if( [PREFS objectForKey:@"phoneFormatIndex"] == nil )
        [PREFS setInteger:0 forKey:@"phoneFormatIndex"];

    // Set controls
    if( [PREFS boolForKey:@"fixLabels"] )
        [fixLabelsCheckBox setState:YES];

    if( [PREFS boolForKey:@"formatPhoneNumbers"] )
        [formatPhoneNumbersCheckBox setState:YES];

    if( [PREFS boolForKey:@"fixZipCodes"] )
        [zipCodeCheckBox setState:YES];

    if( [PREFS boolForKey:@"useParens"] ){
        [formatParens setState:NSOnState];
        [formatDashes setState:NSOffState];
        [formatPopup setEnabled:NO];
    }
    else{
        [formatParens setState:NSOffState];
        [formatDashes setState:NSOnState];
        [formatPopup setEnabled:YES];
    }
        

    if( [PREFS boolForKey:@"useDefaultAreaCode"] )
        [defaultAreaCodeCheckBox setState:YES];

    [formatPopup selectItemAtIndex:[PREFS integerForKey:@"phoneFormatIndex"]];


    // Set spinner style to spinner
    [fixLabelsProgress setStyle:NSProgressIndicatorSpinningStyle];
    [formatPhoneNumbersProgress setStyle:NSProgressIndicatorSpinningStyle];
    [zipCodeProgress setStyle:NSProgressIndicatorSpinningStyle];
    [fixLabelsProgress setDisplayedWhenStopped:NO];
    [formatPhoneNumbersProgress setDisplayedWhenStopped:NO];
    [zipCodeProgress setDisplayedWhenStopped:NO];
        
}	// end awakeFromNib



/* ********  C O N T R O L S   C H A N G E D   M E T H O D S  ******** */


- (IBAction)fixLabelsCheckBoxChanged:(id)sender
{
    [PREFS setBool:([fixLabelsCheckBox state] == NSOnState) forKey:@"fixLabels"];
}	// end fixLabelsCheckBoxChanged:



- (IBAction)formatPhoneNumbersCheckBoxChanged:(id)sender
{
    [PREFS setBool:([formatPhoneNumbersCheckBox state] == NSOnState) forKey:@"formatPhoneNumbers"];
}	// end formatPhoneNumbersCheckBoxChanged:


- (IBAction)fixZipCodesCheckBoxChanged:(id)sender
{
    [PREFS setBool:([zipCodeCheckBox state] == NSOnState) forKey:@"fixZipCodes"];
}	// end fixZipCodesCheckBoxChanged:



- (IBAction)defaultAreaCodeCheckBoxChanged:(id)sender
{
    if( [defaultAreaCodeCheckBox state] == NSOnState ){
        [defaultAreaCode setEnabled:YES];
        [PREFS setBool:YES forKey:@"useDefaultAreaCode"];
    }	// end if: use default
    else{
        [defaultAreaCode setEnabled:NO];
        [PREFS setBool:NO forKey:@"useDefaultAreaCode"];
    }	// end else: don't use default
}	// end defaultAreaCodeCheckBoxChanged:



- (IBAction)formatPopupChanged:(id)sender
{
    unsigned int index = [formatPopup indexOfSelectedItem];
    char c = '-';

    switch( index ){
        case 0:
            c = '-';
            break;
        case 1:
            c = '.';
            break;
        case 2:
            c = ' ';
            break;
        default:
            c = '-';
            break;
    }	// end switch

    [PREFS setInteger:index forKey:@"phoneFormatIndex"];

    [formatDashes setTitle:[NSString
        localizedStringWithFormat:@"999%c555%c1212",c,c]];

}	// end formatPopupChanged




- (IBAction)formatRadioChanged:(id)sender
{
    NSButtonCell *selected = [[formatMatrix selectedCell] retain];

    if( selected == formatParens ){
        [formatPopup setEnabled:NO];
        [PREFS setBool:YES forKey:@"useParens"];
    }	// end if: parens
    else if( selected == formatDashes ){
        [formatPopup setEnabled:YES];
        [PREFS setBool:NO forKey:@"useParens"];
    }	// end else if: dashes
    else
        NSLog(@"Logic error with format radio buttons. Investigate this.");

    [selected release];
}	// end formatRadioChanged




/* ********  P R I M A R Y   A C T I O N S  ******** */


- (IBAction)scrubAddressBook:(id)sender
{
    // Cancel?
    if( _scrubTW != nil ){
        // Cancel
        NSLog(@"Trying to cancel from thread %@",[[NSThread currentThread] description]);

        _cancelFlag = YES;
        
    }	// end if: cancel
    else{
        
        // Make dict to hold instance variables that we'll want procies for.
        NSMutableDictionary *thingsIWillNeed;
        thingsIWillNeed = (NSMutableDictionary *)[NSMutableDictionary dictionaryWithCapacity:10];
        [thingsIWillNeed setObject:self forKey:@"self"];
        [thingsIWillNeed setObject:fixLabelsStatus forKey:@"fixLabelsStatus"];
        [thingsIWillNeed setObject:fixLabelsProgress forKey:@"fixLabelsProgress"];
        [thingsIWillNeed setObject:formatPhoneNumbersStatus forKey:@"formatPhoneNumbersStatus"];
        [thingsIWillNeed setObject:formatPhoneNumbersProgress forKey:@"formatPhoneNumbersProgress"];
        [thingsIWillNeed setObject:defaultAreaCodeCheckBox forKey:@"defaultAreaCodeCheckBox"];
        [thingsIWillNeed setObject:formatParens forKey:@"formatParens"];
        [thingsIWillNeed setObject:formatDashes forKey:@"formatDashes"];
        [thingsIWillNeed setObject:zipCodeStatus forKey:@"zipCodeStatus"];
        [thingsIWillNeed setObject:zipCodeProgress forKey:@"zipCodeProgress"];

        _cancelFlag = NO;
        _scrubTW = [ThreadWorker workOn:self
                           withSelector:@selector(scrubAddressBookLongTask:)
                             withObject:thingsIWillNeed
                         didEndSelector:@selector(scrubAddressBookDidEnd:)];

        [scrubAddressBookButton setEnabled:NO];
    }	// end else: start
    
}	// end scrubAddressBook



/*!
 * Expect this message to be called from a worker thead.
 */
- (id)scrubAddressBookLongTask:(id)arg 
{

    [PREFS setObject:[defaultAreaCode stringValue] forKey:@"defaultAreaCode"];

    if( [fixLabelsCheckBox state] == NSOnState && !_cancelFlag ){
        [self fixLabels:arg];
    }	// end if: fix labels

    if( [formatPhoneNumbersCheckBox state] == NSOnState  && !_cancelFlag ){
        [self formatPhoneNumbers:arg];
    }	// end if: format numbers

    if( [zipCodeCheckBox state] == NSOnState  && !_cancelFlag ){
        [self fixZipCodes:arg];
    }	// end if: fix zip codes

    return nil;
}	// end scrubAddressBookLongTask:





- (void)scrubAddressBookDidEnd:(id)arg
{
    _scrubTW = nil;

    NSLog(@"didEnd");
    [scrubAddressBookButton setEnabled:YES]; 
}	// end scrubAddressBookDidEnd:



- (Boolean)cancelled
{
    return _cancelFlag;
}	// end cancelled





- (IBAction)launchBackup:(id)sender
{
    NSString *backupApp = [@"/Applications/Backup.app" stringByStandardizingPath];
    [NSTask launchedTaskWithLaunchPath:@"/usr/bin/open"
                             arguments:[NSArray arrayWithObject: backupApp]];
}	// end launchBackup:




- (IBAction)fixLabels:(id)sender
{
    NSNumber      *numContactsChanged;
    ABAddressBook *ab;

    // May or may not be proxies
    ABSController       *self_;
    NSProgressIndicator *progress_;
    NSTextField         *status_;

    if( [sender isKindOfClass:[NSDictionary class]] && [sender isProxy] ){
        self_     = (ABSController *)[sender objectForKey:@"self"];
        progress_ = (NSProgressIndicator *)[sender objectForKey:@"fixLabelsProgress"];
        status_   = (NSTextField *)[sender objectForKey:@"fixLabelsStatus"];
    }	// end if: by proxy
    else{
        self_     = self;
        progress_ = fixLabelsProgress;
        status_   = fixLabelsStatus;
    }	// end else: main thread
    
    // Start spinner
    [progress_ startAnimation:self_];
    
    // Stop timer if it's going
    [fixLabelsStatusTimer invalidate];
    [fixLabelsStatusTimer release];
    fixLabelsStatusTimer = nil;

    // Get address book
    ab = [ABAddressBook sharedAddressBook];

    // Scrub labels
    numContactsChanged = [Utility scrubLabelsInAddressBook:ab cancelCheck:self_];

    // Check for changes
    NSString *msg;
    if( [ab hasUnsavedChanges] ){
        [ab save];
        msg = [NSString localizedStringWithFormat:@"Changes were made to %@ contacts.",
                numContactsChanged];
    }	// end else: changes
    else{
        msg = @"No changes were made.";
    }	// end else: no changes

    // Set message
    [status_ setStringValue:msg];

    // Make timer to clear status field
    fixLabelsStatusTimer =
        [[NSTimer scheduledTimerWithTimeInterval:3
                                          target:self_
                                        selector:@selector(clearTextField:)
                                        userInfo:status_
                                         repeats:NO] retain];

    // Stop spinner
    [progress_ stopAnimation:self_];

}	// end fixLabels:





- (IBAction)formatPhoneNumbers:(id)sender
{
    NSNumber      *numContactsChanged;
    ABAddressBook *ab;
    NSString      *format;
    NSString      *defaultAreaCodeStr;


    // May or may not be proxies
    ABSController       *self_;
    NSProgressIndicator *progress_;
    NSTextField         *status_;
    NSButton            *defaultAreaCodeCheckBox_;
    NSButtonCell        *formatParens_;
    NSButtonCell        *formatDashes_;

    if( [sender isKindOfClass:[NSDictionary class]] && [sender isProxy] ){
        self_     = (ABSController *)[sender objectForKey:@"self"];
        progress_ = (NSProgressIndicator *)[sender objectForKey:@"formatPhoneNumbersProgress"];
        status_   = (NSTextField *)[sender objectForKey:@"formatPhoneNumbersStatus"];
        defaultAreaCodeCheckBox_ = (NSButton *)[sender objectForKey:@"defaultAreaCodeCheckBox"];
        formatParens_ = (NSButtonCell *)[sender objectForKey:@"formatParens"];
        formatDashes_ = (NSButtonCell *)[sender objectForKey:@"formatDashes"];
    }	// end if: by proxy
    else{
        self_     = self;
        progress_ = formatPhoneNumbersProgress;
        status_   = formatPhoneNumbersStatus;
        defaultAreaCodeCheckBox_ = defaultAreaCodeCheckBox;
        formatParens_ = formatParens;
        formatDashes_ = formatDashes;
    }	// end else: main thread

    // Start spinner
    [progress_ startAnimation:self_];
    
    // Stop timer if it's going
    [formatPhoneNumbersStatusTimer invalidate];
    [formatPhoneNumbersStatusTimer release];
    formatPhoneNumbersStatusTimer = nil;

    // Get address book
    ab = [ABAddressBook sharedAddressBook];

    // Determine format
    if( NSOnState == [formatParens_ state] )
        format = @"%a(#) %%p#%-%l#%%x x#%";
    else if( NSOnState == [formatDashes_ state] )
        format = [self_ dashesFormat];
    else
        format = @"ERR: %a(#) %%p#%-%l#%%x x#%";

    // Default area code
    defaultAreaCodeStr = nil;
    if( [defaultAreaCodeCheckBox_ state] == NSOnState ){
        
        defaultAreaCodeStr = [[defaultAreaCode stringValue] retain];
        if( [defaultAreaCodeStr length] == 0 ){
            [defaultAreaCodeStr release];
            defaultAreaCodeStr = nil;
        }	// end if: default area code
    }	// end if: use default area code
  
    // Format    
    numContactsChanged = [Utility formatPhoneNumbersInAddressBook:ab
                                                       withFormat:format
                                                  defaultAreaCode:defaultAreaCodeStr
                                                      cancelCheck:self_];


    // Check for changes
    NSString *msg;
    if( [ab hasUnsavedChanges] ){
        [ab save];
        msg = [NSString localizedStringWithFormat:@"Changes were made to %@ contacts.",
            numContactsChanged];
    }	// end else: changes
    else{
        msg = @"No changes were made.";
    }	// end else: no changes
    [status_ setStringValue:msg];

    // Make timer to clear status field
    formatPhoneNumbersStatusTimer =
        [[NSTimer scheduledTimerWithTimeInterval:3
                                          target:self_
                                        selector:@selector(clearTextField:)
                                        userInfo:status_
                                         repeats:NO] retain];
    if( defaultAreaCodeStr ){
        [defaultAreaCodeStr release];
        defaultAreaCodeStr = nil;
    }	// end if: release

    // Stop spinner
    [progress_ stopAnimation:self_];

}	// end formatPhoneNumbers:








- (IBAction)fixZipCodes:(id)sender
{
    NSNumber      *numContactsChanged;
    ABAddressBook *ab;

    // May or may not be proxies
    ABSController       *self_;
    NSProgressIndicator *progress_;
    NSTextField         *status_;

    if( [sender isKindOfClass:[NSDictionary class]] && [sender isProxy] ){
        self_     = (ABSController *)[sender objectForKey:@"self"];
        progress_ = (NSProgressIndicator *)[sender objectForKey:@"zipCodeProgress"];
        status_   = (NSTextField *)[sender objectForKey:@"zipCodeStatus"];
    }	// end if: by proxy
    else{
        self_     = self;
        progress_ = zipCodeProgress;
        status_   = zipCodeStatus;
    }	// end else: main thread

    // Start spinner
    [progress_ startAnimation:self_];
    

    // Stop timer if it's going
    [zipCodeStatusTimer invalidate];
    [zipCodeStatusTimer release];
    zipCodeStatusTimer = nil;

    // Get address book
    ab = [ABAddressBook sharedAddressBook];

    // Fix codes
    numContactsChanged = [Utility fixZipCodesInAddressBook:ab cancelCheck:self_];

    // Check for changes
    NSString *msg;
    if( [ab hasUnsavedChanges] ){
        [ab save];
        msg = [NSString localizedStringWithFormat:@"Changes were made to %@ contacts.",
                numContactsChanged];
    }	// end else: changes
    else{
        msg = @"No changes were made.";
    }	// end else: no changes
    [status_ setStringValue:msg];

    // Make timer to clear status field
    zipCodeStatusTimer =
        [[NSTimer scheduledTimerWithTimeInterval:3
                                          target:self_
                                        selector:@selector(clearTextField:)
                                        userInfo:status_
                                         repeats:NO] retain];

    // Stop spinner
    [progress_ stopAnimation:self_];
    
}	// end fixZipCodes:







- (void)clearTextField:(NSTimer *)timer
{
    [[timer userInfo] setStringValue:@""];

    if( timer == formatPhoneNumbersStatusTimer ){
        [formatPhoneNumbersStatusTimer release];
        formatPhoneNumbersStatusTimer = nil;
    }	// end if: phone timer
    
    else if( timer == fixLabelsStatusTimer ){
        [fixLabelsStatusTimer release];
        fixLabelsStatusTimer = nil;
    }	// end if: label timer
    
    else if( timer == zipCodeStatusTimer ){
        [zipCodeStatusTimer release];
        zipCodeStatusTimer = nil;
    }	// end if: zip codes timer
    
    else{
        NSLog(@"Unknown timer: %@", [timer description]);
    }	// end else: unknown
}	// end clearTextField:





@end
