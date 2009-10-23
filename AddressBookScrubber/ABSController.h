/* ABSController */

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
//#import "Utility.h"
//#import "CDYNE.h"
#import "ThreadWorker.h"
#import "ABSPlugin.h"
#define PREFS [NSUserDefaults standardUserDefaults]

@interface ABSController : NSObject
{
    IBOutlet NSImageView  *meImageView;
//    IBOutlet NSButton     *launchBackupButton;
    
    IBOutlet NSTableView            *_pluginTable;
    IBOutlet NSBox                  *_settingsBox;
    IBOutlet NSView                 *_settingsView;
    IBOutlet NSObjectController     *_selectedPlugin;
    
    IBOutlet NSProgressIndicator    *overallProgress;

    IBOutlet NSButton     *scrubAddressBookButton;

    NSMutableArray *_plugins;   // All plugins
    NSMutableArray *_pluginsTracker;    // Array of NSMutableDictionaries

    
    ThreadWorker *_scrubTW;
    Boolean       _cancelFlag;
}

//- (id)init;
- (void)awakeFromNib;

- (NSMutableArray *)plugins;

- (IBAction)launchBackup:(id)sender;
- (IBAction)scrubAddressBook:(id)sender;

- (IBAction)pluginSelected:(id)sender;

//- (IBAction)tableViewSelected:(id)sender;

//- (IBAction)fixLabels:(id)sender;
//- (IBAction)formatPhoneNumbers:(id)sender;
//- (IBAction)doCDYNEServices:(id)sender;

- (id)scrubAddressBookLongTask:(id)arg;
- (void)scrubAddressBookDidEnd:(id)arg;
- (Boolean)cancelled;

//- (IBAction)fixLabelsCheckBoxChanged:(id)sender;
//- (IBAction)formatPhoneNumbersCheckBoxChanged:(id)sender;
//- (IBAction)defaultAreaCodeCheckBoxChanged:(id)sender;
//- (IBAction)defaultCountryCodeCheckBoxChanged:(id)sender;
//- (IBAction)CDYNECheckBoxChanged:(id)sender;

//- (IBAction)formatPopupChanged:(id)sender;
//- (IBAction)formatRadioChanged:(id)sender;


//- (void)clearTextField:(NSTimer *)timer;


@end
