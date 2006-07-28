#import "Controller.h"

@implementation Controller



- (void)start:(id)sender
{
    NSLog(@"Button clicked in %@", [NSThread currentThread]);

    // Prepare GUI components
    [_startButton setEnabled:NO];
    [_input setEnabled:NO];
    [_status setStringValue:@"Fake preparation..."];
    [_pi setIndeterminate:YES];
    [_pi startAnimation:self];

    // Prep things we'll need in the other thread.
    // Any GUI components should be accessed this way.
    NSMutableDictionary *thingsIllNeed = [NSMutableDictionary dictionary];
    [thingsIllNeed setObject:_pi forKey:@"progress"];
    [thingsIllNeed setObject:_status forKey:@"status"];

    // Start work on new thread
    _tw = [[ThreadWorker workOn:self
                   withSelector:@selector(longTask:worker:)
                     withObject:thingsIllNeed
                 didEndSelector:@selector(longTaskFinished:)] retain];

    [_cancelButton setEnabled:YES];
}	// end start:


- (void)cancel:(id)sender
{
    if( _tw ){
        [_status setStringValue:@"Cancelling..."];
        [_tw markAsCancelled];
        [_cancelButton setEnabled:NO];
    }	// end if: _tw exists
}	// end cancel



-(id)longTask:(id)userInfo worker:(ThreadWorker *)tw
{
    NSDictionary        *thingsIllNeed;
    NSProgressIndicator *progress;
    NSTextField         *status;
    int                  i;
    id                   returnVal;

    // Get stuff I'll need to talk to on the other thread.
    thingsIllNeed  = (NSDictionary *)userInfo;
    progress       = (NSProgressIndicator *)[thingsIllNeed objectForKey:@"progress"];
    status         = (NSTextField *)[thingsIllNeed objectForKey:@"status"];
    returnVal      = nil;

    NSLog(@"Long task working in %@", [NSThread currentThread]);

    // Initial (fake) pause
    [NSThread sleepUntilDate:[NSDate dateWithTimeIntervalSinceNow:1.0]];

    // Fake work
    [status setStringValue:@"Fake working on long task..."];
    for( i = 0; i <= 100 ; i++ )
    {
        // Cancelled by user?
        if( [tw cancelled] ){
            [progress setDoubleValue:100.0];
            returnVal = @"I was cancelled.";
            break;
        }	// end if: cancelled
        
        [NSThread sleepUntilDate:[NSDate dateWithTimeIntervalSinceNow:0.02]];
        if( [progress isIndeterminate] )
        {
            [progress stopAnimation:self];
            [progress setIndeterminate:NO];
        }   // end if: turning on determinate

        [progress setDoubleValue:(i*1.0)];

        returnVal = @"Look what I did!";
    }   // end for: work


    return returnVal;
}	// end longTask:




-(void)longTaskFinished:(id)userInfo
{
    [_cancelButton setEnabled:NO];
    [_pi setIndeterminate:YES];
    [_pi startAnimation:self];
    
    [_status setStringValue:[userInfo description]];

    NSLog(@"Long task finishing in %@", [NSThread currentThread]);

    [_status setStringValue:[@"Finished with message: "
                             stringByAppendingString:[userInfo description]]];
    [_input setEnabled:YES];
    [_startButton setEnabled:YES];
    [_pi stopAnimation:self];
    
    [_tw release];
    _tw = nil;
}	// end longTaskFinished




@end
