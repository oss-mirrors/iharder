/* Controller */

#import <Cocoa/Cocoa.h>
#import "XMLTree.h"

// Commment out this line if you don't have Jaguar.
// You may also have to remove the AddressBook framework on the left
// in the Frameworks -> Other Frameworks folder.
#import <AddressBook/AddressBook.h>


@interface Controller : NSObject
{
    IBOutlet NSTextField *address;
    IBOutlet NSButton *checkAddressButton;
    IBOutlet NSTextField *city;
    IBOutlet NSTextField *descendentName;
    IBOutlet NSTextField *descendentResult;
    IBOutlet NSButton *findDescendentButton;
    IBOutlet NSTextField *license;
    IBOutlet NSProgressIndicator *progress;
    IBOutlet NSTextView *rawResult;
    IBOutlet NSTextField *state;
    IBOutlet NSTextField *zip;
    IBOutlet NSTextField *url;

    XMLTree *_tree;
}
- (IBAction)checkAddress:(id)sender;
- (IBAction)findDescendent:(id)sender;
- (IBAction)updateURL:(id)sender;

- (NSURL *)getURL;

// Comment out these three functions if you aren't running Jaguar.
- (void)populateWithUsersAddress;
+ (NSDictionary *)myHomeAddress;
+ (id) findFirstMatch:(ABMultiValue *)multiValue withLabel:(NSString *)label;


@end
