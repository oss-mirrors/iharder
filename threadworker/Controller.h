#import <AppKit/AppKit.h>
#import "ThreadWorker.h"

@interface Controller : NSObject
{
    id _input;
    id _pi;
    id _startButton;
    id _cancelButton;
    id _status;
    ThreadWorker *_tw;
}

- (void) start:(id)sender;

- (void) cancel:(id)sender;

-(id)longTask:(id)userInfo worker:(ThreadWorker *)tw;

- (void)longTaskFinished:(id)userInfo;



@end
