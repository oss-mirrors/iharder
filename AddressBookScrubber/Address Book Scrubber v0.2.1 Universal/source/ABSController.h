/* ABSController */

#import <Cocoa/Cocoa.h>
#import "Utility.h"
#import "ThreadWorker.h"
#define PREFS [NSUserDefaults standardUserDefaults]

@interface ABSController : NSObject
{
    IBOutlet NSImageView  *meImageView;
    IBOutlet NSButton     *launchBackupButton;

    IBOutlet NSButton     *fixLabelsCheckBox;
    IBOutlet NSTextField  *fixLabelsStatus;
    IBOutlet NSProgressIndicator *fixLabelsProgress;

    IBOutlet NSButton     *formatPhoneNumbersCheckBox;
    IBOutlet NSTextField  *formatPhoneNumbersStatus;
    IBOutlet NSProgressIndicator *formatPhoneNumbersProgress;
    IBOutlet NSPopUpButton *formatPopup;
    IBOutlet NSButtonCell *formatDashes;
    IBOutlet NSButtonCell *formatParens;
    IBOutlet NSMatrix     *formatMatrix;
    IBOutlet NSButton     *defaultAreaCodeCheckBox;
    IBOutlet NSTextField  *defaultAreaCode;

    IBOutlet NSButton            *zipCodeCheckBox;
    IBOutlet NSTextField         *zipCodeStatus;
    IBOutlet NSProgressIndicator *zipCodeProgress;

    IBOutlet NSButton     *scrubAddressBookButton;

    NSTimer *fixLabelsStatusTimer;
    NSTimer *formatPhoneNumbersStatusTimer;
    NSTimer *zipCodeStatusTimer;

    ThreadWorker *_scrubTW;
    Boolean       _cancelFlag;
}

- (void)awakeFromNib;


- (IBAction)launchBackup:(id)sender;
- (IBAction)scrubAddressBook:(id)sender;
- (IBAction)fixLabels:(id)sender;
- (IBAction)formatPhoneNumbers:(id)sender;
- (IBAction)fixZipCodes:(id)sender;

- (id)scrubAddressBookLongTask:(id)arg;
- (void)scrubAddressBookDidEnd:(id)arg;
- (Boolean)cancelled;

- (IBAction)fixLabelsCheckBoxChanged:(id)sender;
- (IBAction)formatPhoneNumbersCheckBoxChanged:(id)sender;
- (IBAction)defaultAreaCodeCheckBoxChanged:(id)sender;
- (IBAction)fixZipCodesCheckBoxChanged:(id)sender;

- (IBAction)formatPopupChanged:(id)sender;
- (IBAction)formatRadioChanged:(id)sender;


- (void)clearTextField:(NSTimer *)timer;


@end
