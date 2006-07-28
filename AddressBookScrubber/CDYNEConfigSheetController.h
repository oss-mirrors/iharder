/* CDYNEConfigSheetController */

#import <Cocoa/Cocoa.h>

@interface CDYNEConfigSheetController : NSWindowController
{
    IBOutlet NSButton *gatherLatLongCheckBox;
    IBOutlet NSButton *gatherZipPlus4CheckBox;
    IBOutlet NSTextField *license;
}
- (IBAction)cancel:(id)sender;
- (IBAction)goToCDYNEDotCom:(id)sender;
- (IBAction)OK:(id)sender;
@end
