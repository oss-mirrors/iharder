

#import <Foundation/Foundation.h>
#import <AddressBook/AddressBook.h>


void printUsage(){
    printf("USAGE: abfind [options] search\n");
    printf("  Finds people in your address book.\n");
    printf("  Version 0.1\n");
    printf("  http://iHarder.net/abfind\n");
    printf("EXAMPLE: abfind -name smith\n" );
    printf("  -name    Searches first and last name, case insensitive\n");
    printf("  -fname   Searches first name, case insensitive\n");
    printf("  -lname   Searches last name, case insensitive\n");
    printf("  -city    Searches city, case insensitive\n");
    printf("  -zip     Searches zip, case insensitive\n");
    printf("  -note    Searches the note field, case insensitive\n");
    printf("  -id      Searches for the exact person ID\n");
    printf("  -me      Returns the person marked as 'me'. No search terms required.\n");
    printf("  -?       This help\n");
    printf("  If no options are given, all fields are searched.\n");
}   // end printUsage



ABSearchElement *simpleSearch( NSString *prop, NSString *value ){
    return [ABPerson searchElementForProperty:prop
                                        label:nil 
                                          key:nil 
                                        value:value 
                                   comparison:kABContainsSubStringCaseInsensitive ];
    
}


ABSearchElement * processOpt_name( NSMutableArray *args ){
    [args removeObjectAtIndex:0];
    if( [args count] < 1 ){
        fprintf(stderr, "The -name search requires a trailing argument.\n");
        //printUsage();
        return nil;
    }   // end if: error
    
    NSString *name = [args objectAtIndex:0];
    
    ABSearchElement *firstNameSearch = simpleSearch(kABFirstNameProperty, name );
    ABSearchElement *lastNameSearch  = simpleSearch(kABLastNameProperty, name );
    
    ABSearchElement *search = [ABSearchElement searchElementForConjunction:kABSearchOr
                                                                  children:[NSArray arrayWithObjects:
                                                                            firstNameSearch, lastNameSearch, nil]];
    [args removeObjectAtIndex:0];
    
    return search;
}   // end processOpt_name




ABSearchElement * processOpt_fname( NSMutableArray *args ){
    [args removeObjectAtIndex:0];
    if( [args count] < 1 ){
        fprintf(stderr, "The -fname search requires a trailing argument.\n");
        //printUsage();
        return nil;
    }   // end if: error
    
    NSString *name = [args objectAtIndex:0];
    
    ABSearchElement *firstNameSearch = simpleSearch(kABFirstNameProperty, name );
    [args removeObjectAtIndex:0];
    
    return firstNameSearch;
}   // end processOpt_fname



ABSearchElement * processOpt_lname( NSMutableArray *args ){
    [args removeObjectAtIndex:0];
    if( [args count] < 1 ){
        fprintf(stderr, "The -lname search requires a trailing argument.\n");
        //printUsage();
        return nil;
    }   // end if: error
    
    NSString *name = [args objectAtIndex:0];
    
    ABSearchElement *lastNameSearch = simpleSearch(kABLastNameProperty, name );
    [args removeObjectAtIndex:0];
    
    return lastNameSearch;
}   // end processOpt_lname


ABSearchElement * processOpt_city( NSMutableArray *args ){
    [args removeObjectAtIndex:0];
    if( [args count] < 1 ){
        fprintf(stderr, "The -city search requires a trailing argument.\n");
        //printUsage();
        return nil;
    }   // end if: error
    
    NSString *value = [args objectAtIndex:0];
    
    ABSearchElement *citySearch = 
    [ABPerson searchElementForProperty:kABAddressProperty
                                 label:nil
                                   key:kABAddressCityKey
                                 value:value
                            comparison:kABContainsSubStringCaseInsensitive];
    [args removeObjectAtIndex:0];
    
    return citySearch;
}   // end processOpt_city


ABSearchElement * processOpt_zip( NSMutableArray *args ){
    [args removeObjectAtIndex:0];
    if( [args count] < 1 ){
        fprintf(stderr, "The -zip search requires a trailing argument.\n");
        //printUsage();
        return nil;
    }   // end if: error
    
    NSString *value = [args objectAtIndex:0];
    
    ABSearchElement *zipSearch = 
    [ABPerson searchElementForProperty:kABAddressProperty
                                 label:nil
                                   key:kABAddressZIPKey
                                 value:value
                            comparison:kABContainsSubStringCaseInsensitive];
    [args removeObjectAtIndex:0];
    
    return zipSearch;
}   // end processOpt_zip


ABSearchElement * processOpt_note( NSMutableArray *args ){
    [args removeObjectAtIndex:0];
    if( [args count] < 1 ){
        fprintf(stderr, "The -note search requires a trailing argument.\n");
        //printUsage();
        return nil;
    }   // end if: error
    
    NSString *value = [args objectAtIndex:0];
    
    ABSearchElement *noteSearch = simpleSearch(kABNoteProperty, value );
    [args removeObjectAtIndex:0];
    
    return noteSearch;
}   // end processOpt_note


ABSearchElement * processOpt_id( NSMutableArray *args ){
    [args removeObjectAtIndex:0];
    if( [args count] < 1 ){
        fprintf(stderr, "The -id search requires a trailing argument.\n");
        //printUsage();
        return nil;
    }   // end if: error
    
    NSString *value = [args objectAtIndex:0];
    
    ABSearchElement *idSearch = 
    [ABPerson searchElementForProperty:kABUIDProperty
                                 label:nil
                                   key:nil
                                 value:value
                            comparison:kABEqual];
    [args removeObjectAtIndex:0];
    
    return idSearch;
}   // end processOpt_id


ABSearchElement * processOpt_me( NSMutableArray *args ){
    [args removeObjectAtIndex:0];
    ABPerson *me = [[ABAddressBook sharedAddressBook] me];
    if( me ){
        return
        [ABPerson searchElementForProperty:kABUIDProperty
                                     label:nil
                                       key:nil
                                     value:[me valueForProperty:kABUIDProperty]
                                comparison:kABEqual];
    } else {
        return nil;
    }
}   // end processOpt_me



ABSearchElement *processOpt_ALL( NSMutableArray *args ){
    NSString *value = [args objectAtIndex:0];
    [args removeObjectAtIndex:0];
    
    NSArray *keys = [NSArray arrayWithObjects:
                     kABFirstNameProperty,
                     kABLastNameProperty,
                     kABFirstNamePhoneticProperty,
                     kABLastNamePhoneticProperty,
                     kABNicknameProperty,
                     kABMaidenNameProperty,
                     kABBirthdayProperty,
                     kABOrganizationProperty,
                     kABJobTitleProperty,
                     kABHomePageProperty,
                     kABURLsProperty,
#if MAC_OS_X_VERSION_MIN_REQUIRED >= MAC_OS_X_VERSION_10_5
                     kABCalendarURIsProperty, // maybe use an ifdef here for v10.4
#endif
                     kABEmailProperty,
                     kABAddressProperty,
                     kABOtherDatesProperty,
                     kABRelatedNamesProperty,
                     kABDepartmentProperty,
                     kABPersonFlags,
                     kABPhoneProperty,
                     kABAIMInstantProperty,
                     kABJabberInstantProperty,
                     kABMSNInstantProperty,
                     kABYahooInstantProperty,
                     kABICQInstantProperty,
                     kABNoteProperty,
                     kABMiddleNameProperty,
                     kABMiddleNamePhoneticProperty,
                     kABTitleProperty,
                     kABSuffixProperty,
                     kABMiddleNameProperty,
                     kABMiddleNamePhoneticProperty,
                     kABTitleProperty,
                     kABSuffixProperty,
                     nil
                     ];
    NSMutableArray *searches = [[NSMutableArray alloc] initWithCapacity:[keys count]];
    
#if MAC_OS_X_VERSION_MIN_REQUIRED >= MAC_OS_X_VERSION_10_5
    // THE 10.5+ WAY
    for( NSString *prop in keys ){
        [searches addObject:simpleSearch(prop, value)];
    }   // end for: each key
#else 
    // THE 10.4 WAY
    for( int i = 0; i < [keys count]; i++ ){
        NSString *prop = (NSString *)[keys objectAtIndex:i];
        [searches addObject:simpleSearch(prop, value)];
    }   // end for:
#endif
    
    return [ABSearchElement searchElementForConjunction:kABSearchOr children:searches];

}









void printPerson( ABPerson *person ){
    printf("******** id = %s\n%s\n",
           [[[person valueForProperty:kABUIDProperty] description] UTF8String],
           [[person description] UTF8String]
           );
    
}




int main (int argc, const char * argv[]) {
    NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];
    
    ABSearchElement *search = nil;
    
    // Turn command line arguments into mutable array
    NSMutableArray *args = [[NSMutableArray alloc] initWithCapacity:argc];
    for( int i = 1; i < argc; i++ ){
        [args addObject:[NSString stringWithUTF8String:argv[i]]];
    }   // end if: each arg
    
    
    // Process arguments until they're all gone
    while( [args count] > 0 ){
        NSString *opt = [args objectAtIndex:0];
        
        if( [@"-name" isEqualToString:opt] ){
            ABSearchElement *q = processOpt_name( args );
            search = q;
        }
        
        else if( [@"-fname" isEqualToString:opt] ){
            ABSearchElement *q = processOpt_fname( args );
            search = q;
        }
        
        else if( [@"-lname" isEqualToString:opt] ){
            ABSearchElement *q = processOpt_lname( args );
            search = q;
        }
        
        else if( [@"-city" isEqualToString:opt] ){
            ABSearchElement *q = processOpt_city( args );
            search = q;
        }
        
        else if( [@"-zip" isEqualToString:opt] ){
            ABSearchElement *q = processOpt_zip( args );
            search = q;
        }
        
        else if( [@"-note" isEqualToString:opt] ){
            ABSearchElement *q = processOpt_note( args );
            search = q;
        }
        
        else if( [@"-id" isEqualToString:opt] ){
            ABSearchElement *q = processOpt_id( args );
            search = q;
        }
        
        else if( [@"-me" isEqualToString:opt] ){
            ABSearchElement *q = processOpt_me( args );
            search = q;
        }
        
        else if( [@"-?" isEqualToString:opt] ){
            printUsage();
            return 0;
        }
        
        else {
            ABSearchElement *q = processOpt_ALL( args );
            search = q;
        }   // end else: unknown argument
    }   // end while: processing arguments
    [args release];
    args = nil;
    
    
    
    
    
    
    
    if( search == nil ){
        printUsage();
    } else {
        ABAddressBook   *ab = [ABAddressBook sharedAddressBook];
        NSArray *peopleFound = [ab recordsMatchingSearchElement:search];
        printf("Number of results: %d\n", (unsigned int)[peopleFound count]);
        
#if MAC_OS_X_VERSION_MIN_REQUIRED >= MAC_OS_X_VERSION_10_5
        // THE 10.5+ WAY
        for( ABPerson *person in peopleFound ){
            printPerson( person );
        }
#else 
        // THE 10.4 WAY
        for( int i = 0; i < [peopleFound count]; i++ ){
            printPerson((ABPerson *)[peopleFound objectAtIndex:i]);
        }   // end for: each person
#endif
        
    }   // end else: have a search
    
    
    [pool drain];
    return 0;
}

