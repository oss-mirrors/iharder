//
//  PhoneNumber.m
//  Address Book Scrubber
//
//  Created by Robert Harder on Fri Nov 08 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

#import "PhoneNumber.h"

@interface PhoneNumber (PrivateAPI)

+ (NSString *)numbersInString:(NSString *)source;
- (void)parse;
- (void)parse_us;


@end // Private API


@implementation PhoneNumber

/*!
 * Broken. Should be autoreleased, but for some reason
 * autoreleasing causes crashes.
 */
+ (PhoneNumber *)phoneNumberWithString:(NSString *)phoneNumber withLocale:(NSString *)loc
{
    PhoneNumber *pn;
    pn = [[PhoneNumber alloc] initWithString:phoneNumber withLocale:loc];
    
    return pn;
}


- (PhoneNumber *)init
{
    if( ![super init] )
        return nil;
    else
        return self;
}	// end init


- (PhoneNumber *)initWithString:(NSString *)phoneNumber withLocale:(NSString *)loc
{
    if( ![super init] )
        return nil;

    
    if( phoneNumber )
        _original    = [[phoneNumber copyWithZone:nil] retain];
    
    if( loc )
        _locale      = [[loc copyWithZone:nil] retain];
    
    _countryCode = nil;
    _areaCode    = nil;
    _prefix      = nil;
    _line        = nil;
    _extension   = nil;

    // Parse
    if( _original != nil )
        [self parse];
    
    return self;
}	// end initFromString:


- (void)dealloc
{
    // Release
    [_original    release];
    [_locale      release];
    [_countryCode release];
    [_areaCode    release];
    [_prefix      release];
    [_line        release];
    [_extension   release];

    // Set nil for good measure
    _original    = nil;
    _locale      = nil;
    _countryCode = nil;
    _areaCode    = nil;
    _prefix      = nil;
    _line        = nil;
    _extension   = nil;

}	// end dealloc



/*!
 * Formats phone number
 *  %a(#)% - give (area code) if area code exists
 *  %c..#...% - Country code
 *  %a..#...% - Area code
 *  %p..#...% - Prefix
 *  %l..#...% - Line
 *  %x..#...% - Extension
 *  %% - Percent sign
 *  Example: (999) 555-1212  ->  %c+# %%a(#) %%p#%-%l#%%x x#%
 */
- (NSString *)format:(NSString *)format
{
    NSMutableString *formatted;
    unsigned int     i, length;
    unichar          c;
    NSString        *field;
    Boolean          inField;
    
    formatted = [[[NSMutableString alloc] initWithCapacity:([format length]<<1)] retain];
    length    = [format length];
    field     = nil;
    inField   = NO;
    
    for( i = 0; i < length; i++ ){
        
        c = [format characterAtIndex:i];

        if( c == '%' ){
            if( inField ){
                field = nil;
                inField = NO;
            }	// end if: end of field
            else{
                if( i+1 < length ){
                    
                    c = [format characterAtIndex:(++i)];
                    inField = YES;

                    switch( c ){
                        case 'c':
                            field = _countryCode;
                            break;
                        case 'a':
                            field = _areaCode;
                            break;
                        case 'p':
                            field = _prefix;
                            break;
                        case 'l':
                            field = _line;
                            break;
                        case 'x':
                            field = _extension;
                            break;
                        default:
                            field = @"ERR";
                            break;
                    }	// end switch: on c
                }	// end if: more in string
            }	// end else: no field exists yet
        }	// end if: percent

        else if( c == '#' && field ){
            if( field == _countryCode ){
                
                if( _useCountryCode )
                    [formatted appendString:field];
            }
            else
                [formatted appendString:field];
        }	// end else if: a # sign and in field
        else if( inField ){
            
            if( field == _countryCode ){
                
                if( _useCountryCode )
                    [formatted appendFormat:@"%c",c];
                
            }	// end if: country code
                
            else if( field )
                [formatted appendFormat:@"%c",c];
            
        }	// end else: just copy char
        else{
            [formatted appendFormat:@"%c",c];
        }	// end else: outside field
            
        
    }	// end for: each char in format

    NSString *returnValue = [[NSString alloc] initWithString:formatted];
    [formatted release];
    
    return returnValue;
}	// end format:


- (Boolean) appearsValid
{
    if( !_areaCode && !_prefix && !_line )
        return NO;
    else return YES;
}	// end appearsValid



/* ********  S E T T E R   M E T H O D S  ******** */


- (PhoneNumber *)setLocale:(NSString *)locale
{
    [_locale release];
    _locale = [[[NSString alloc] initWithString:locale] retain];
    return self;
}	// end setLocale:


- (PhoneNumber *)setCountryCode:(NSString *)countryCode
{
    [_countryCode release];
    _countryCode = [[[NSString alloc] initWithString:countryCode] retain];
    return self;
}	// end setCountryCode:



- (PhoneNumber *)setAreaCode:(NSString *)areaCode
{
    [_areaCode release];
    _areaCode = [[[NSString alloc] initWithString:areaCode] retain];
    return self;
}	// end setAreaCode:


- (PhoneNumber *)setPrefix:(NSString *)prefix
{
    [_prefix release];
    _prefix = [[[NSString alloc] initWithString:prefix] retain];
    return self;
}	// end setPrefix:


- (PhoneNumber *)setLine:(NSString *)line
{
    [_line release];
    _line = [[[NSString alloc] initWithString:line] retain];
    return self;
}	// end setLine:


- (PhoneNumber *)setExtension:(NSString *)extension
{
    [_extension release];
    _extension = [[[NSString alloc] initWithString:extension] retain];
    return self;
}	// end setExtension:



/* ********  G E T T E R   M E T H O D S  ******** */


- (NSString *)original
{
    return _original;
}	// end original


- (NSString *)locale
{
    return _locale;
}	// end locale


- (NSString *)countryCode
{
    return _countryCode;
}	// end countryCode


- (NSString *)areaCode
{
    return _areaCode;
}	// end areaCode


- (NSString *)prefix
{
    return _prefix;
}	// end prefix


- (NSString *)line
{
    return _line;
}	// end line


- (NSString *)extension
{
    return _extension;
}	// end extension



- (PhoneNumber *)setUseCountryCode:(BOOL)use
{
    _useCountryCode = use;
    return self;
}	// end useCountryCode:



- (BOOL)useCountryCode
{
    return _useCountryCode;
}	// end useCountryCode


/* ********  P R I V A T E   A P I  ******** */


+ (NSString *)numbersInString:(NSString *)source
{
    NSMutableString *numbersOnly;
    unsigned int     i, length;
    unichar          c;

    length      = [source length];
    numbersOnly = [[[NSMutableString alloc] initWithCapacity:length] retain];

    for( i = 0; i < length; i++ ){

        c = [source characterAtIndex:i];
        if( c >= '0' && c <= '9' )
            [numbersOnly appendFormat:@"%c",c];
        
    }	// end for: each char

    NSString *returnVal = [[NSString alloc] initWithString:numbersOnly];
    [numbersOnly release];
    
    return returnVal;
}	// end numbersInString




/*!
 * Called from init to parse nasty input.
 */
- (void)parse
{
    // Locale. For now I only support "us" locale
    NSComparisonResult result;

    if( _original != nil ){
        
        result = [@"us" compare:_locale];
        if( result == NSOrderedSame ){
            [self parse_us];
        }	// end if: us
        
    }	// end if: not null
    else{
        NSLog(@"Got nil phone number");
    }	// end else: nil
}	// end parse


/*!
 * Parse "us" locale.
 */
- (void)parse_us
{
    NSMutableString *stripped;
    unichar          c;
    unsigned int     i, length;

    length   = [_original length];
    stripped = [[[NSMutableString alloc] initWithString:_original] retain];

    // Trim up to (and after) country code
    for( i = 0; i < [stripped length]; i++ ){
        c = [stripped characterAtIndex:0];
        if( c == '1' )
            _countryCode = @"1";
        if( c < '2' || c > '9' )
            [stripped deleteCharactersInRange:NSMakeRange(0,1)];
        else
            break;
    }	// end for: looking for start

    // Look for extension
    NSRange extLoc = [stripped rangeOfString:@"x"
                                     options:(NSCaseInsensitiveSearch |
                                              NSBackwardsSearch)];
    if( extLoc.location != NSNotFound ){
        NSMutableString *afterExt;
        afterExt = [[[NSMutableString alloc] initWithString:[stripped
                    substringFromIndex:(extLoc.location + extLoc.length)]] retain];
        _extension = [[PhoneNumber numbersInString:afterExt] retain];;
        [stripped deleteCharactersInRange:NSMakeRange( extLoc.location,
                                                       [stripped length] -
                                                       extLoc.location )];
        [afterExt release];
        afterExt = nil;
    }	// end if: found extension

    // We should now have seven or ten numbers
    NSString *primaryNumbers;
    primaryNumbers = [[PhoneNumber numbersInString:stripped] retain];
    
    switch( [primaryNumbers length] ){
        case 7:
            _prefix = [primaryNumbers substringWithRange:NSMakeRange(0,3)];
            _line   = [primaryNumbers substringWithRange:NSMakeRange(3,4)];
            break;
        case 10:
            _areaCode = [primaryNumbers substringWithRange:NSMakeRange(0,3)];
            _prefix   = [primaryNumbers substringWithRange:NSMakeRange(3,3)];
            _line     = [primaryNumbers substringWithRange:NSMakeRange(6,4)];
            break;
        default:
            
            break;
    }	// end switch
    
    [primaryNumbers release];
    [stripped release];
}	// end parse_us


@end
