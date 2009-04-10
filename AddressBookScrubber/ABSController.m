#import "ABSController.h"

// Thanks to http://cocoadevcentral.com/articles/000068.php
// for the excellent Plugin tutorial.


@interface ABSController (PrivateAPI)

- (NSString *)dashesFormat;

// Plugin stuff
- (void)initPlugins;
+ (NSArray *)findPluginBundles;
+ (NSArray *)loadPluginClasses:(NSArray *)pluginBundles;
+ (NSArray *)loadPluginInstances:(NSArray *)pluginClasses;
+ (NSArray *)selectPluginsFrom:(NSArray *)pluginInstances conformingTo:(id)proto;
- (NSArray *)selectedPlugins;


// Three kinds of plugin activity
- (void)fixPerson:(ABPerson *)person withPlugins:(NSArray *)plugins;
- (void)fixPhonesFrom:(ABPerson *)person withPlugins:(NSArray *)plugins;
- (void)fixAddressesFrom:(ABPerson *)person withPlugins:(NSArray *)plugins;

@end




@implementation ABSController


-(id)init{
    if( self = [super init] ){
        [self initPlugins];
    }
    return self;
}



- (void)awakeFromNib
{
    //[self initPlugins];    
    [self pluginSelected:_pluginTable];
    
    //[self loadAllBundles];
    //NSLog( @"Bundles: %d\n", [bundles count] );
    //for( i = 0; i < [bundles count]; i++ ){
    //    NSLog( [[bundles objectAtIndex:i] description] );
    //}

    // Is Backup installed?
//    if( [[NSWorkspace sharedWorkspace] fullPathForApplication:@"Backup"] ){
//            [launchBackupButton setTransparent:NO];
//            [launchBackupButton setEnabled:YES];
//    }	// end if

    // Old way I checked for Backup
    //if( [fm fileExistsAtPath:backupApp isDirectory:&isDir] && isDir ){
    //    [launchBackupButton setTransparent:NO];
    //    [launchBackupButton setEnabled:YES];
    //}	// end if


    // Get 'me' pic
    ABAddressBook *ab = [ABAddressBook sharedAddressBook];
    ABPerson      *me = [ab me];
    NSImage  *meImage = [[[NSImage alloc] initWithData:[me imageData]] retain];

    if( meImage ){
        [meImageView setImage:meImage];
        [meImageView setImageFrameStyle:NSImageFramePhoto];
    }	// end if: got my pic
    [meImage release];

    /*
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
    
    //if( [PREFS objectForKey:@"doCDYNEServices"] == nil )
    //    [PREFS setBool:YES forKey:@"doCDYNEServices"];
    
    if( [PREFS objectForKey:@"useParens"] == nil )
        [PREFS setBool:YES forKey:@"useParens"];
    
    if( [PREFS objectForKey:@"useDefaultAreaCode"] == nil )
        [PREFS setBool:NO forKey:@"useDefaultAreaCode"];
    
    if( [PREFS objectForKey:@"useDefaultCountryCode"] == nil )
        [PREFS setBool:NO forKey:@"useDefaultCountryCode"];

    if( [PREFS objectForKey:@"phoneFormatIndex"] == nil )
        [PREFS setInteger:0 forKey:@"phoneFormatIndex"];

    // Set controls
    if( [PREFS boolForKey:@"fixLabels"] )
        [fixLabelsCheckBox setState:YES];

    if( [PREFS boolForKey:@"formatPhoneNumbers"] )
        [formatPhoneNumbersCheckBox setState:YES];

    //if( [PREFS boolForKey:@"doCDYNEServices"] )
    //    [CDYNECheckBox setState:YES];

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
    
    if( [PREFS boolForKey:@"useDefaultCountryCode"] )
        [defaultCountryCodeCheckBox setState:YES];

    [formatPopup selectItemAtIndex:[PREFS integerForKey:@"phoneFormatIndex"]];


    // Set spinner style to spinner
    [fixLabelsProgress setStyle:NSProgressIndicatorSpinningStyle];
    [formatPhoneNumbersProgress setStyle:NSProgressIndicatorSpinningStyle];
    [fixLabelsProgress setDisplayedWhenStopped:NO];
    [formatPhoneNumbersProgress setDisplayedWhenStopped:NO];
    [CDYNEProgress setDisplayedWhenStopped:NO];
        */
}	// end awakeFromNib





/*
- (NSString *)dashesFormat
{
    NSString *format;
    unsigned int index = [formatPopup indexOfSelectedItem];

    switch( index ){
        case 0: // Dash
            format = @"%c#-%%a#-%%p#-%%l#%%x x#%";
            break;
        case 1: // Period
            format = @"%c#.%%a#.%%p#.%%l#%%x x#%";
            break;
        case 2: // Space
            format = @"%c# %%a# %%p# %%l#%%x x#%";
            break;
        default:
            format = @"%c#-%%a#-%%p#-%%l#%%x x#%";
            break;
    }	// end switch

    return format;
}	// end dashesFormat

*/


/* ********  C O N T R O L S   C H A N G E D   M E T H O D S  ******** */

/*
- (IBAction)fixLabelsCheckBoxChanged:(id)sender
{
    [PREFS setBool:([fixLabelsCheckBox state] == NSOnState) forKey:@"fixLabels"];
}	// end fixLabelsCheckBoxChanged:



- (IBAction)formatPhoneNumbersCheckBoxChanged:(id)sender
{
    [PREFS setBool:([formatPhoneNumbersCheckBox state] == NSOnState) forKey:@"formatPhoneNumbers"];
}	// end formatPhoneNumbersCheckBoxChanged:


- (IBAction)CDYNECheckBoxChanged:(id)sender
{
    [PREFS setBool:([CDYNECheckBox state] == NSOnState) forKey:@"CDYNEServices"];
}	// end CDYNECheckBoxChanged:



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





- (IBAction)defaultCountryCodeCheckBoxChanged:(id)sender
{
    if( [defaultCountryCodeCheckBox state] == NSOnState ){
        [defaultCountryCode setEnabled:YES];
        [PREFS setBool:YES forKey:@"useDefaultCountryCode"];
    }	// end if: use default
    else{
        [defaultCountryCode setEnabled:NO];
        [PREFS setBool:NO forKey:@"useDefaultCountryCode"];
    }	// end else: don't use default
}	// end defaultCountryCodeCheckBoxChanged:





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

 */



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
        
        // Make dict to hold instance variables that we'll want proxies for on worker thread
        NSMutableDictionary *thingsIWillNeed;
         thingsIWillNeed = (NSMutableDictionary *)[NSMutableDictionary dictionaryWithCapacity:10];
        [thingsIWillNeed setObject:self                     forKey:@"self"];
        [thingsIWillNeed setObject:[self selectedPlugins]   forKey:@"selectedPlugins"];
        [thingsIWillNeed setObject:overallProgress          forKey:@"overallProgress"];

        // Start progress indicator
        [overallProgress setIndeterminate:YES];
        [overallProgress startAnimation:self];
        
        
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
- (id)scrubAddressBookLongTask:(id)thingsINeed 
{
    // Gather objects from other thread via proxy
    NSArray *selectedPlugins  = [thingsINeed objectForKey:@"selectedPlugins"];  // Selected plugins
    NSProgressIndicator *prog = [thingsINeed objectForKey:@"overallProgress"];  // Progress indicator
    
    // Gather other variables
    ABAddressBook *ab = [ABAddressBook sharedAddressBook];                  // Get address book
    NSArray *people = [ab people];
    
    // Pull out plugin types
    NSMutableArray *personPlugs  = [[NSMutableArray alloc] init];
    NSMutableArray *phonePlugs   = [[NSMutableArray alloc] init];
    NSMutableArray *addressPlugs = [[NSMutableArray alloc] init];
    for( NSObject<ABSPlugin> *plug in selectedPlugins ){
        if( [plug conformsToProtocol:@protocol(ABSPersonPlugin)] ){
            [personPlugs addObject:plug];
        } else if( [plug conformsToProtocol:@protocol(ABSPhonePlugin)] ){
            [phonePlugs addObject:plug];
        } else if( [plug conformsToProtocol:@protocol(ABSAddressPlugin)] ){
            [addressPlugs addObject:plug];
        }   // end if: plugin type
    }   // end for: each plugin
    
    // Set up progress bar
    [prog setMinValue:0];                                                   // Min value for progress indicator
    [prog setMaxValue:[people count]];                                      // Max value for progress indicator
    [prog setDoubleValue:0];                                                // Set value to zero
    [prog setIndeterminate:NO];                                             // Turn off indeterminate
    
    // Loop through each person
    for( ABPerson *currPerson in people ){
        
        [prog incrementBy:1];                                               // Move progress indicator along
        [self fixPerson:currPerson withPlugins:personPlugs];                // Apply person plugins
        [self fixPhonesFrom:currPerson withPlugins:phonePlugs];             // Apply phone plugins
        [self fixAddressesFrom:currPerson withPlugins:addressPlugs];        // Apply address plugin

    }   // end for: each person
    
    
    // Check for changes
/*    NSString *msg;
    if( [ab hasUnsavedChanges] ){
        [ab save];
        msg = [NSString localizedStringWithFormat:@"Changes were made to %@ contacts.",
               numContactsChanged];
    }	// end else: changes
    else{
        msg = @"No changes were made.";
    }	// end else: no changes
*/
    
/*
    [PREFS setObject:[defaultAreaCode stringValue] forKey:@"defaultAreaCode"];

    if( [fixLabelsCheckBox state] == NSOnState && !_cancelFlag ){
        [self fixLabels:arg];
    }	// end if: fix labels

    if( [formatPhoneNumbersCheckBox state] == NSOnState  && !_cancelFlag ){
        [self formatPhoneNumbers:arg];
    }	// end if: format numbers

    if( [CDYNECheckBox state] == NSOnState  && !_cancelFlag ){
        [self doCDYNEServices:arg];
    }	// end if: CDYNE
*/
    return nil;
}	// end scrubAddressBookLongTask:



- (void)scrubAddressBookDidEnd:(id)returnedFromLongTask
{
    _scrubTW = nil;
    [overallProgress setIndeterminate:YES];
    [overallProgress stopAnimation:self];
    
    NSLog(@"didEnd");
    [scrubAddressBookButton setEnabled:YES]; 
}	// end scrubAddressBookDidEnd:



- (Boolean)cancelled
{
    return _cancelFlag;
}	// end cancelled




/**
 * Loops through each plugin and applies plugin to person.
 */
- (void)fixPerson:(ABPerson *)person withPlugins:(NSArray *)plugins{
    
}



/**
 * Loops through each plugin and applies plugin to each phone number for the person.
 */
- (void)fixPhonesFrom:(ABPerson *)person withPlugins:(NSArray *)plugins{
    
    BOOL changesMade = NO;                                              // Record if any phone numbers changed
    ABMutableMultiValue *mutablePhones = [[person valueForProperty:kABPhoneProperty] mutableCopy];  // All phone numbers
    int count = [mutablePhones count];                                  // Count of phone numbers
    NSEnumerator *plugEnum = [plugins objectEnumerator];                // Enumerate each plugin
    NSObject<ABSPhonePlugin> *currPlug;                                 // Current plugin
    
    while( currPlug = [plugEnum nextObject] ){                          // Loop
        
        int i;                                                          // For loop counter
        for( i = 0; i < count; i++ ){                                   // Loop through phone numbers
            NSString *oldLabel = [mutablePhones labelAtIndex:i];        // Label
            id        oldValue = [mutablePhones valueAtIndex:i];        // Value
            NSString *newValue = nil;
            @try{                                                       // Try to call plugin
                    [currPlug fixPhone:[oldValue description]           //   with phone number
                            withLabel:oldLabel                          //   and label
                           fromPerson:person];                          //   and person.
            }   // end try
            @catch( NSException *exc ){
                NSLog(@"Exception in plugin %@ with person %@.", currPlug, person);
            }
            
            if( newValue ){                                             // Was a new value returned?
                [mutablePhones replaceValueAtIndex:i 
                                         withValue:newValue];           // Save new value
                changesMade = YES;                                      // Remember that a change was made
            }   // end if: value changed
        }   // end for: each phone
    }   // end while: each plugin
    
    if( changesMade ){
        [person setValue:mutablePhones forProperty:kABPhoneProperty];
    }
         
}   // end fixPhonesFrom: withPlugins:

         

/**
 * Loops through each plugin and applies plugin to each address for the person.
 */
- (void)fixAddressesFrom:(ABPerson *)person withPlugins:(NSArray *)plugins{

}




- (IBAction)launchBackup:(id)sender
{
    //NSString *backupApp = [@"/Applications/Backup.app" stringByStandardizingPath];
    //[NSTask launchedTaskWithLaunchPath:@"/usr/bin/open"
    //                         arguments:[NSArray arrayWithObject: backupApp]];
    [[NSWorkspace sharedWorkspace] launchApplication:@"Backup"];
}	// end launchBackup:



/*
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
    NSString	  *defaultCountryCodeStr;


    // May or may not be proxies
    ABSController       *self_;
    NSProgressIndicator *progress_;
    NSTextField         *status_;
    NSButton            *defaultAreaCodeCheckBox_;
    NSButton            *defaultCountryCodeCheckBox_;
    NSButtonCell        *formatParens_;
    NSButtonCell        *formatDashes_;

    if( [sender isKindOfClass:[NSDictionary class]] && [sender isProxy] ){
        self_     = (ABSController *)[sender objectForKey:@"self"];
        progress_ = (NSProgressIndicator *)[sender objectForKey:@"formatPhoneNumbersProgress"];
        status_   = (NSTextField *)[sender objectForKey:@"formatPhoneNumbersStatus"];
        defaultAreaCodeCheckBox_ = (NSButton *)[sender objectForKey:@"defaultAreaCodeCheckBox"];
        defaultCountryCodeCheckBox_ = (NSButton *)[sender objectForKey:@"defaultCountryCodeCheckBox"];
        formatParens_ = (NSButtonCell *)[sender objectForKey:@"formatParens"];
        formatDashes_ = (NSButtonCell *)[sender objectForKey:@"formatDashes"];
    }	// end if: by proxy
    else{
        self_     = self;
        progress_ = formatPhoneNumbersProgress;
        status_   = formatPhoneNumbersStatus;
        defaultAreaCodeCheckBox_ = defaultAreaCodeCheckBox;
        defaultCountryCodeCheckBox_ = defaultCountryCodeCheckBox;
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
        format = @"%c+# %%a(#) %%p#%-%l#%%x x#%";
    else if( NSOnState == [formatDashes_ state] )
        format = [self_ dashesFormat];
    else
        format = @"ERR: %c+# %%a(#) %%p#%-%l#%%x x#%";

    // Default area code
    defaultAreaCodeStr = nil;
    if( [defaultAreaCodeCheckBox_ state] == NSOnState ){
        
        defaultAreaCodeStr = [[defaultAreaCode stringValue] retain];
        if( [defaultAreaCodeStr length] == 0 ){
            [defaultAreaCodeStr release];
            defaultAreaCodeStr = nil;
        }	// end if: default area code
    }	// end if: use default area code

    // Default country code
    defaultCountryCodeStr = nil;
    if( [defaultCountryCodeCheckBox_ state] == NSOnState ){

        defaultCountryCodeStr = [[defaultCountryCode stringValue] retain];
        if( [defaultCountryCodeStr length] == 0 ){
            [defaultCountryCodeStr release];
            defaultCountryCodeStr = nil;
        }	// end if: default country code
    }	// end if: use default country code
  
    // Format    
    numContactsChanged = [Utility formatPhoneNumbersInAddressBook:ab
                                                       withFormat:format
                                                  defaultAreaCode:defaultAreaCodeStr
                                               defaultCountryCode:defaultCountryCodeStr
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

    if( defaultCountryCodeStr ){
        [defaultCountryCodeStr release];
        defaultCountryCodeStr = nil;
    }	// end if: release

    // Stop spinner
    [progress_ stopAnimation:self_];

}	// end formatPhoneNumbers:








- (IBAction)doCDYNEServices:(id)sender
{
    NSNumber      *numContactsChanged;
    ABAddressBook *ab;

    // May or may not be proxies
    ABSController       *self_;
    NSProgressIndicator *progress_;
    NSTextField         *status_;

    if( [sender isKindOfClass:[NSDictionary class]] && [sender isProxy] ){
        self_     = (ABSController *)[sender objectForKey:@"self"];
        progress_ = (NSProgressIndicator *)[sender objectForKey:@"CDYNEProgress"];
        status_   = (NSTextField *)[sender objectForKey:@"CDYNEStatus"];
    }	// end if: by proxy
    else{
        self_     = self;
        progress_ = CDYNEProgress;
        status_   = CDYNEStatus;
    }	// end else: main thread

    // Start spinner
    [progress_ setIndeterminate:YES];
    [progress_ startAnimation:self_];
    

    // Stop timer if it's going
    [CDYNEStatusTimer invalidate];
    [CDYNEStatusTimer release];
    CDYNEStatusTimer = nil;

    // Get address book
    ab = [ABAddressBook sharedAddressBook];

    // Fix codes
    //numContactsChanged = [Utility fixZipCodesInAddressBook:ab cancelCheck:self_];
    numContactsChanged = [CDYNE fixAddressBook:ab
                                   cancelCheck:self_
                                      progress:progress_
                                       license:@"0"];

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
    CDYNEStatusTimer =
        [[NSTimer scheduledTimerWithTimeInterval:3
                                          target:self_
                                        selector:@selector(clearTextField:)
                                        userInfo:status_
                                         repeats:NO] retain];

    // Stop spinner
    [progress_ setIndeterminate:YES];
    [progress_ stopAnimation:self_];
    [progress_ setDisplayedWhenStopped:NO];
    
}	// end doCDYNEServices:







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
    
    else if( timer == CDYNEStatusTimer ){
        [CDYNEStatusTimer release];
        CDYNEStatusTimer = nil;
    }	// end if: CDYNE timer
    
    else{
        NSLog(@"Unknown timer: %@", [timer description]);
    }	// end else: unknown
}	// end clearTextField:
*/


/* ********  P L U G I N   A R C H I T E C T U R E  ******** */


- (NSMutableArray *)plugins{
    NSLog(@"Plugins requested: %@",_pluginsTracker);
    return _pluginsTracker;
}


- (IBAction)pluginSelected:(id)sender{
    if( sender == _pluginTable ){
        NSObject<ABSPlugin> *plug = [_selectedPlugin content];
        NSLog(@"Selected: %@", plug);
        [_settingsBox setContentView:[plug theView]];
        
    }
}

#pragma mark -
#pragma mark Plugins
/**
 * Called at beginning of application lifespan.
 */
- (void)initPlugins{

    // Find Plugins
    NSArray *pluginBundles      = [ABSController findPluginBundles];                         // Locates *.absplugin bundles
    NSArray *pluginClasses      = [ABSController loadPluginClasses:pluginBundles];           // Loads classes
    NSArray *pluginInstances    = [ABSController loadPluginInstances:pluginClasses];         // Load instances from each principal class

    // Binds to Array Controller to show in the table which plugins
    // we've got and which ones are selected to be applied.
    _pluginsTracker = [[NSMutableArray alloc] init];
    for( NSObject<ABSPlugin> *absPlug in pluginInstances ){                     // Loop over plugins
        NSMutableDictionary *record = [[NSMutableDictionary alloc] init];       // Row/record dictionary
        [record setObject:[NSNumber numberWithBool:NO] forKey:@"selected"];     // Not selected initially
        [record setObject:absPlug forKey:@"plugin"];                            // Plugin object
        [_pluginsTracker addObject:record];                                     // Add record
        NSLog(@"Added plugin record %@", absPlug);                              // Log
    }   // end for: each plugin
    
}   // end initPlugins




/**
 * Called from initPlugins to locate all *.absplugin bundles.
 */
+ (NSArray *)findPluginBundles
{
    NSMutableArray *bundles = [[NSMutableArray alloc] init];
    NSLog(@"Looking for plugin bundles...");
    
    
    // First look inside the application package in Address Book Scrubber.app/Contents/Plugins   
	NSString *folderPath = [[NSBundle mainBundle] builtInPlugInsPath];      // Address Book Scrubber/Contents/Plugins    
	if (folderPath) {                                                       // Folder exists?
		NSEnumerator *pathEnum = [[NSBundle pathsForResourcesOfType:@"absplugin" inDirectory:folderPath] objectEnumerator];
		NSString *pluginPath;
		while ((pluginPath = [pathEnum nextObject])) {                      // Check each possible *.absplugin package
            NSBundle* pluginBundle = [NSBundle bundleWithPath:pluginPath];  // Try to load bundle
            if( pluginBundle ){                                             // A real bundle?
                [bundles addObject:pluginBundle];                           // Save bundle
                NSLog(@"Found bundle %@", pluginBundle);
            }   // end if: got bundle
		}   // end while: each plugin
	}   // end if: got folder path
 
 
 
    // Various locations of
    // Library/Application Support/Address Book Scrubber/Plugins
    // Loop over each "Library" and look for *.absplugin bundles
    NSArray *librarySearchPaths   = NSSearchPathForDirectoriesInDomains( NSLibraryDirectory, NSAllDomainsMask - NSSystemDomainMask, YES);
    NSEnumerator *libraryPathEnum = [librarySearchPaths objectEnumerator]; 
    NSString *currLibrary;
    while(currLibrary = [libraryPathEnum nextObject]){  // Each Library location
        NSString *folderPath = [currLibrary stringByAppendingPathComponent:@"Application Support/Address Book Scrubber/Plugins"];
        NSEnumerator *pluginEnumerator = [[NSBundle pathsForResourcesOfType:@"absplugin" inDirectory:folderPath] objectEnumerator];
        NSString *pluginPath;
		while ((pluginPath = [pluginEnumerator nextObject])) {
            NSBundle* pluginBundle = [NSBundle bundleWithPath:pluginPath];  // Try to load bundle
            if( pluginBundle ){                                             // A real bundle?
                [bundles addObject:pluginBundle];                           // Save bundle
                NSLog(@"Found bundle %@", pluginBundle);
            }   // end if: got bundle
		}   // end while: each plugin
    }   // end while: each "library"
 
 
    return bundles;
 }   // end loadBundles



/**
 * Called from initPlugins to load principal classes from *.absplugin bundles.
 */
+ (NSArray *)loadPluginClasses:(NSArray *)bundles{
    NSMutableArray *pluginClasses = [[NSMutableArray alloc] init];              // Store all principal classes here
    NSEnumerator *bundleEnum = [bundles objectEnumerator];                      // Enumerate over bundles array
    NSBundle *currBundle;                                                       // Current bundle in while loop
    
    while( currBundle = [bundleEnum nextObject] ){                              // Loop    
        NSDictionary* pluginDict = [currBundle infoDictionary];                 // info.plist dictionary
        NSString* pluginName = [pluginDict objectForKey:@"NSPrincipalClass"];   // Principal class?
        
        if (pluginName) {                                                       // Did we get a name?
            Class principalClass = NSClassFromString(pluginName);               // Load class if OS X already knows about it
            
            if (!principalClass) {                                              // Don't know about it yet?
                principalClass = [currBundle principalClass];                   // Load the class fresh
                if ([principalClass conformsToProtocol:@protocol(ABSPlugin)] && // Is it a plugin?
                    [principalClass isKindOfClass:[NSObject class]] ) {         // Is it an NSObject?
                    
                    [pluginClasses addObject:principalClass];                   // It's OK! Add the class
                    NSLog(@"Added plugin %@", principalClass);                  // Log entry
                    
                }   // end if: conforms
            }   // end if: !pluginClass
        }   // end if: pluginName
    }   // end while: each bundle
    
    return pluginClasses;
}   // end loadPluginClasses





/**
 * Called from initPlugins to load one or more instances from each plugin principal class.
 */
+ (NSArray *)loadPluginInstances:(NSArray *)pluginClasses{
    NSMutableArray *instances = [[[NSMutableArray alloc] init] autorelease];    // Store instances here
    NSEnumerator *classEnum = [pluginClasses objectEnumerator];                 // Enumerate over classes array
    Class currClass;                                                            // Current class in while loop

    while( currClass = [classEnum nextObject] ){                                // Loop        
        [instances addObjectsFromArray:[currClass allPlugins]];                 // Add all instances to array
    }   // end while: each class
    
    return instances;
}   // end loadPluginInstances




/**
 * Goes through _pluginsTracker (table model) and pulls
 * out plugins that are checked off (selected).
 */
- (NSArray *)selectedPlugins{
    NSMutableArray *selectedPlugins = [[NSMutableArray alloc] init];            // List of selected plugins
    for( NSDictionary *row in _pluginsTracker ){                                // Loop over each row
        NSNumber *selected = [row objectForKey:@"selected"];                    // "Selected" value
        if( [selected boolValue] ){                                             // If selected
            NSObject<ABSPlugin> *plugin = [row objectForKey:@"plugin"];         // Get plugin
            [selectedPlugins addObject:plugin];                                 // Add to list
        }   // end if: selected
    }   // end for: each row
    
    return selectedPlugins; 
}



@end // ABSController
