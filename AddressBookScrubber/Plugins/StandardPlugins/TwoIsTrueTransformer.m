//
//  TwoIsTrueTransformer.m
//  StandardPlugins
//
//  Created by Robert Harder on 11/26/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "TwoIsTrueTransformer.h"


@implementation TwoIsTrueTransformer

+ (Class)transformedValueClass
{
    return [NSNumber class];
}

+ (BOOL)allowsReverseTransformation
{
    return NO;
}

- (id)transformedValue:(id)value{   
    
    if( value == nil ) return nil;
    if ([value respondsToSelector: @selector(intValue)]) {
        if( [value intValue] == 2 ){
            return [NSNumber numberWithBool:YES];
        }   // end if: a one
    }   // end if: intValue
    return nil;
}

@end
