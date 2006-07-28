// You'll nee to include one of the following two lines.
// Try each one to see which one works for you.
#import <AppKit/AppKit.h>
//#import <Cocoa/Cocoa.h>


/*!
 * @class ThreadWorker
 * @version 0.6.2
 * @abstract Throws a task onto another thread and notifies you when it's done.
 * @discussion
 *
 * Usage:
 * 
 *     [ThreadWorker 
 *         workOn:self 
 *         withSelector:@selector(longTask:) 
 *         withArgument:someData
 *         didEndSelector:@selector(longTaskFinished:) ];
 *
 * The ThreadWorker class was designed to be simple and
 * to make multi-threading even simpler. You can offload
 * tasks to another thread and be notified when the task
 * is finished.
 * 
 * In this sense it is similar to the Java SwingWorker
 * class, though the need for such a class in Cocoa
 * and Objective-C is as different as the implementation.
 *
 * Be sure to copy the ThreadWorker.h and ThreadWorker.m
 * files to your project directory.
 *
 * To see how to use this class, see the documentation for
 * the "workOn" method below.
 *
 * I'm releasing this code into the Public Domain.
 * Do with it as you will. Enjoy!
 *
 * Original author: Robert Harder, rharder@usa.net
 *
 *
 * Change History
 *
 * 0.6.2 - Moved [super dealloc] to the end of the dealloc method and
 *       ensured "init" returns nil if it fails.
 *
 * 0.6.1 - Added [super dealloc] to the dealloc method and moved the
 *       dealloc declaration out of the private API and into the
 *       public .h file as it should be.
 *
 * 0.6 - Eliminated the need for the runSelectorInCallingThread method
 *       by making a proxy to the target available to the task working
 *       in the new thread. This makes for much less overhead.
 *       Also changed static method signature from withArgument to withObject.
 *
 * 0.5.1 - Oops. Forget a necessary thread lock for the NSConnection creation.
 *
 * 0.5 - Uses NSConnection to communicate between threads, so although we
 *       might have been thread-safe before (depending on whether or not
 *       addTimer in NSRunLoop is thread-safe), we're definitely thread-safe
 *       now - or so we think. =)
 *       In the process we had to do away with the helper functions that took
 *       a bit of hassle out using runSelectorInCallingThread with arguments
 *       that are not objects. Sorry.
 *
 * 0.4.1 - Fixed some typos in commented sections.
 *
 * 0.4 - Released into the Public Domain. Enjoy!
 *
 * 0.3 - Permitted "workOn" method to accept a second argument of type
 *       ThreadWorker* to allow for passing of the parent ThreadWorker
 *       to the secondary thread. This makes it easy and reliable to
 *       call other methods on the calling thread.
 *
 * 0.2 - Added runSelectorInCallingThread so that you could make calls
 *       back to the main, i.e. calling, thread from the secondary thread.
 *
 * 0.1 - Initial release.
 *
 */
@interface ThreadWorker : NSObject
{
   id               _target;            // The object whose selector will be called
   SEL              _selector;          // The selector that will be called in another thread
   id               _argument;          // The argument that will be passed to the selector
   SEL              _didEndSelector;    // Selector for final notice
   NSConnection    *_callingConnection; // Connection used to safely communicate between threads
   NSPort          *_port1;
   NSPort          *_port2;
   NSConnection    *_conn2;
}



/*!
 * @method workOn:withSelector:withArgument:didEndSelector:
 * @param target The object to receive the selector message. It is retained.
 * @param selector The selector to be called on the target in the worker thread.
 * @param argument An optional argument if you wish to pass one to the selector
 *        and target. It is retained.
 * @param didEndSelector An optional selector to call on the target. Use the
 *        value 0 (zero) if you don't want a selector called at the end.
 * @result Returns an autoreleased ThreadWorker that manages the worker thread.
 *
 * @abstract Call this class method to work on something in another thread. 
 * @discussion
 *
 * Example:
 *
 *     [ThreadWorker workOn:self 
 *                   withSelector:@selector(longTask:) 
 *                   withArgument:someData
 *                   didEndSelector:@selector(longTaskFinished:)];
 *
 * 
 * The longTask method in self will then be called and should look something like this:
 *
 *     - (id)longTask:(id)someData
 *     {
 *         // Do something that takes a while and uses 'someData' if you want
 *         // ...
 *
 *         return userInfo; // Will be passed to didEndSelector
 *     }    
 *
 * Optionally you can have this "longTask" method accept a second argument of type
 * 'id' which will give you access to a proxy of your target (presumably a form of 'self').
 * Calling methods on this proxy will cause the message to be executed in the calling thread.
 * Your "longTask" method might then look like this:
 *
 *     - (id)longTask:(id)someData targetProxy:(id)pseudoself
 *    {
 *        ...
 *        [pseudoself messageToExecuteOnCallingThread];
 *        ...
 *    }
 *
 * You don't have to name your parameter "targetProxy." You only have to match what you
 * name it to the selector you passed earlier:
 *
 *     [ThreadWorker workOn:self 
 *                   withSelector:@selector(longTask: targetProxy:) 
 *                   withArgument:someData
 *                   didEndSelector:@selector(longTaskFinished:)];
 *
 *
 * When your longTask method is finished, whatever is returned from it will
 * be passed to the didEndSelector (if it's not nil) as that selector's
 * only argument. The didEndSelector will be called on the original thread,
 * so if you launched the thread as a result of a user clicking on something,
 * the longTaskFinished will be called on the main thread, which is what you
 * need if you want to then modify any GUI components.
 * The longTaskFinished method might look something like this, then:
 *
 *     - (void)longTaskFinished:(id)userInfo
 *     {
 *         //Do something now that the thread is done
 *         // ...
 *     }    
 *
 * Of course you will have to have imported the ThreadWorker.h
 * file in your class's header file. The top of your header file
 * might then look like this:
 *
 *     import <Cocoa/Cocoa.h>
 *     import "ThreadWorker.h"
 *
 * Enjoy.
 * 
 */
+ (ThreadWorker *)
    workOn:(id)target 
    withSelector:(SEL)selector 
    withObject:(id)argument
    didEndSelector:(SEL)didEndSelector;



/*!
 * @discussion
 * Just a little note to say, "Good job, Rob!" to
 * the original author of this Public Domain software.
 */
+ (NSString *)description;




    /*!
    * @method dealloc
    * @discussion
    * Make sure we clean up after ourselves.
    */
- (void) dealloc;

@end

