/*
   MyGreatImageApp
   Graphics Filter Interface version 0
   MyAppBitmapGraphicsFiltering.h
*/
 
#import <Cocoa/Cocoa.h>
#import <AddressBook/AddressBook.h> 
 
 




@protocol ABSPlugin

+ (NSArray*)allPlugins;

- (NSView*)theView;

- (NSString*)theViewName;

@end // ABSPlugin




@protocol ABSPhonePlugin<ABSPlugin>

- (NSString *)fixPhone:(NSString *)phone 
            withLabel:(NSString *)label     
           fromPerson:(ABPerson *)person;
           
@end



@protocol ABSAddressPlugin<ABSPlugin>

- (ABMultiValue *)fixAddress:(ABMultiValue *)address 
                   withLabel:(NSString *)label 
                  fromPerson:(ABPerson *)person;
                  
@end


@protocol ABSPersonPlugin<ABSPlugin>

- (ABPerson *)fixPerson:(ABPerson *)person;

@end


