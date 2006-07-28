#import <Cocoa/Cocoa.h>
#import <AddressBook/AddressBook.h>
#import "PhoneNumber.h"


@interface Utility : NSObject
{

}

+(NSDictionary *)callSOAPWithURL:(NSURL *)url
                      methodName:(NSString *)methodName
                 methodNamespace:(NSString *)methodNamespace
                      parameters:(NSDictionary *)parameters
                      SOAPAction:(NSString *)SOAPAction;


+ (id) findFirstMatch:(ABMultiValue *)multiValue withLabel:(NSString *)label;



+ (NSNumber *) formatPhoneNumbersInAddressBook:(ABAddressBook *)ab
                                    withFormat:(NSString *)format
                               defaultAreaCode:(NSString *)defaultAreaCode
                               defaultCountryCode:(NSString *)defaultCountryCode
                                   cancelCheck:(id)cancelCheck;

+ (ABMutableMultiValue *) formatPhoneNumbers:(ABMultiValue *)phones
                                  withFormat:(NSString *)format
                             defaultAreaCode:(NSString *)defaultAreaCode
                          defaultCountryCode:(NSString *)defaultCountryCode;



+ (NSNumber *) scrubLabelsInAddressBook:(ABAddressBook *)ab cancelCheck:(id)cancelCheck;
+ (ABMutableMultiValue *) scrubLabelsInAddresses:(ABMultiValue *)addresses;
+ (ABMutableMultiValue *) scrubLabelsInPhones:(ABMultiValue *)phones;
+ (ABMutableMultiValue *) scrubLabelsInEmails:(ABMultiValue *)emails;



+ (NSNumber *)fixZipCodesInAddressBook:(ABAddressBook *)ab cancelCheck:(id)cancelCheck;
+ (ABMutableMultiValue *) fixZipCodesInAddresses:(ABMultiValue *)addresses;

+ (NSString *)numbersInString:(NSString *)source;

@end // end Utility.h
