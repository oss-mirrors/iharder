
#import <AddressBook/AddressBook.h>
#import <Foundation/Foundation.h>
#import "XMLTree.h"
#import "ABSController.h"


//NSString *kCDYNEDoServices = @"CDYNEDoServices"; 


@interface CDYNE : NSObject {
    
}


+ (NSNumber *)fixAddressBook:(ABAddressBook *)ab
                 cancelCheck:(id)cancelCheck
                    progress:(NSProgressIndicator *)progress
                     license:(NSString *)license;


+(NSString *)urlencode:(NSString *)source;

+ (ABMutableMultiValue *) doCDYNEWithAddress:(ABMultiValue *)addresses
                                     license:(NSString *)license;



+(NSDictionary *)wsResultForAddress:(NSDictionary *)address
                           license:(NSString *)license;

+(XMLTree *)treeForAddress:(NSDictionary *)address
                   license:(NSString *)license;



@end
