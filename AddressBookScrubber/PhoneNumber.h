//
//  PhoneNumber.h
//  Address Book Scrubber
//
//  Created by Robert Harder on Fri Nov 08 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface PhoneNumber : NSObject {
    NSString *_original;
    NSString *_locale;
    NSString *_countryCode;
    NSString *_areaCode;
    NSString *_prefix;
    NSString *_line;
    NSString *_extension;
    BOOL      _useCountryCode;
}

+ (PhoneNumber *)phoneNumberWithString:(NSString *)phoneNumber
                            withLocale:(NSString *)locale;


- (PhoneNumber *)init;

- (PhoneNumber *)initWithString:(NSString *)phoneNumber withLocale:(NSString *)locale;

- (NSString *)format:(NSString *)format;

- (Boolean)appearsValid;

- (PhoneNumber *)setLocale:(NSString *)locale;
- (PhoneNumber *)setCountryCode:(NSString *)countryCode;
- (PhoneNumber *)setAreaCode:(NSString *)areaCode;
- (PhoneNumber *)setPrefix:(NSString *)prefix;
- (PhoneNumber *)setLine:(NSString *)line;
- (PhoneNumber *)setExtension:(NSString *)extension;

- (PhoneNumber *)setUseCountryCode:(BOOL)use;
- (BOOL)useCountryCode;


- (NSString *)original;

- (NSString *)locale;

- (NSString *)countryCode;

- (NSString *)areaCode;

- (NSString *)prefix;

- (NSString *)line;

- (NSString *)extension;

@end
