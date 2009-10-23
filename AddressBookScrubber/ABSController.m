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
    


}


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
    //NSLog(@"fixPerson:withPlugins: not yet implemented.");
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
    //    NSLog(@"fixAddressesFrom:withPlugins: not yet implemented.");
}




- (IBAction)launchBackup:(id)sender
{
    //NSString *backupApp = [@"/Applications/Backup.app" stringByStandardizingPath];
    //[NSTask launchedTaskWithLaunchPath:@"/usr/bin/open"
    //                         arguments:[NSArray arrayWithObject: backupApp]];
    [[NSWorkspace sharedWorkspace] launchApplication:@"Backup"];
}	// end launchBackup:




/* ********  P L U G I N   A R C H I T E C T U R E  ******** */


- (NSMutableArray *)plugins{
    NSLog(@"Plugins requested: %@",_pluginsTracker);
    return _pluginsTracker;
}


- (IBAction)pluginSelected:(id)sender{
    if( sender == _pluginTable ){
        NSObject<ABSPlugin> *plug = [_selectedPlugin content];
        NSLog(@"Selected: %@", plug);
        //id prev = [_settingsBox contentView];
        //if ( prev == nil ) {
            [[_settingsBox animator] setContentView:[plug theView]];
        //} else {
        //    [[_settingsBox animator] replaceSubview:prev with:[plug theView]];
        //}
        
        
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
    NSLog(@"Looking here: %@", folderPath);
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
