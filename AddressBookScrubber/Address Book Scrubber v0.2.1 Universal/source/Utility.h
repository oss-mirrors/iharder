#import <Cocoa/Cocoa.h>
#import <AddressBook/AddressBook.h>
#import "PhoneNumber.h"


@interface Utility : NSObject
{

}




+ (id) findFirstMatch:(ABMultiValue *)multiValue withLabel:(NSString *)label;



+ (NSNumber *) formatPhoneNumbersInAddressBook:(ABAddressBook *)ab
                                    withFormat:(NSString *)format
                               defaultAreaCode:(NSString *)defaultAreaCode
                                   cancelCheck:(id)cancelCheck;

+ (ABMutableMultiValue *) formatPhoneNumbers:(ABMultiValue *)phones
                                  withFormat:(NSString *)format
                             defaultAreaCode:(NSString *)defaultAreaCode;



+ (NSNumber *) scrubLabelsInAddressBook:(ABAddressBook *)ab cancelCheck:(id)cancelCheck;
+ (ABMutableMultiValue *) scrubLabelsInAddresses:(ABMultiValue *)addresses;
+ (ABMutableMultiValue *) scrubLabelsInPhones:(ABMultiValue *)phones;
+ (ABMutableMultiValue *) scrubLabelsInEmails:(ABMultiValue *)emails;



+ (NSNumber *)fixZipCodesInAddressBook:(ABAddressBook *)ab cancelCheck:(id)cancelCheck;
+ (ABMutableMultiValue *) fixZipCodesInAddresses:(ABMultiValue *)addresses;

+ (NSString *)numbersInString:(NSString *)source;

@end // end Utility.h
