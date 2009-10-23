/*
   MyGreatImageApp
   Graphics Filter Interface version 0
   MyAppBitmapGraphicsFiltering.h
*/
 
#import <Cocoa/Cocoa.h>
#import <AddressBook/AddressBook.h> 
 
 



#pragma mark General Plugin Protocol
@protocol ABSPlugin

/**
 * Class-level method that returns
 * an array of all ABSPlugins that your bundle can load.
 * Each array object should be an instantiated instance 
 * (that's redundant, I know) of your plugin object that
 * implements at least one of the ABSPlugin protocols:
 * ABSPhonePlugin and ABSAddressPlugin.
 */
+ (NSArray*)allPlugins;

/**
 * Return an NSView that stores preferences or a description
 * of your plugin. This will be displayed when the user
 * selects your plugin.
 */
- (NSView*)theView;

/**
 * This is the human-readable name that will be presented
 * to the user as the name of your plugin.
 */
- (NSString*)theViewName;

@end // ABSPlugin



#pragma mark Protocol for Fixing One Phone Number at a Time
@protocol ABSPhonePlugin<ABSPlugin>

/**
 * This is called for each phone number in the Address Book.
 * The method should return the corrected phone number
 * or nil if no change was made.
 */
- (NSString *)fixPhone:(NSString *)phone 
            withLabel:(NSString *)label     
           fromPerson:(ABPerson *)person;
           
@end


#pragma mark Protocol for Fixing One Address at a Time
@protocol ABSAddressPlugin<ABSPlugin>

/**
 * This is called for each address in the Address Book.
 * The method should return the corrected address.
 */
- (ABMultiValue *)fixAddress:(ABMultiValue *)address 
                   withLabel:(NSString *)label 
                  fromPerson:(ABPerson *)person;
                  
@end


#pragma mark Protocol for Fixing a Person's Whole Record
@protocol ABSPersonPlugin<ABSPlugin>

/**
 * This is called for each person in the Address Book.
 * The method should return the person.
 */
- (ABPerson *)fixPerson:(ABPerson *)person;

@end


