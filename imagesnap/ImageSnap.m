//
//  ImageSnap.m
//  ImageSnap
//
//  Created by Robert Harder on 9/10/09.
//
// CHANGELOG
// 2009-10-13: Fixed bug whereby everything was saved as TIFF. Oops.

#import "ImageSnap.h"


@interface ImageSnap()


- (void)captureOutput:(QTCaptureOutput *)captureOutput 
  didOutputVideoFrame:(CVImageBufferRef)videoFrame 
     withSampleBuffer:(QTSampleBuffer *)sampleBuffer 
       fromConnection:(QTCaptureConnection *)connection;

@end


@implementation ImageSnap



- (id)init{
	self = [super init];
    mCaptureSession = nil;
    mCaptureDeviceInput = nil;
    mCaptureDecompressedVideoOutput = nil;
	mCurrentImageBuffer = nil;
	return self;
}

- (void)dealloc{
	
	mCaptureSession ? [mCaptureSession release]:0;
	mCaptureDeviceInput ? [mCaptureDeviceInput release]:0;
	mCaptureDecompressedVideoOutput ? [mCaptureDecompressedVideoOutput release]:0;
    CVBufferRelease(mCurrentImageBuffer);
    
    [super dealloc];
}


// Returns an array of video devices attached to this computer.
+ (NSArray *)videoDevices{
    NSMutableArray *results = [NSMutableArray arrayWithCapacity:3];
    [results addObjectsFromArray:[QTCaptureDevice inputDevicesWithMediaType:QTMediaTypeVideo]];
    [results addObjectsFromArray:[QTCaptureDevice inputDevicesWithMediaType:QTMediaTypeMuxed]];
    return results;
}

// Returns the default video device or nil if none found.
+ (QTCaptureDevice *)defaultVideoDevice{
	QTCaptureDevice *device = nil;
    
	device = [QTCaptureDevice defaultInputDeviceWithMediaType:QTMediaTypeVideo];
	if( device == nil ){
        device = [QTCaptureDevice defaultInputDeviceWithMediaType:QTMediaTypeMuxed];
	}
    return device;
}


+(QTCaptureDevice *)deviceNamed:(NSString *)name{
    QTCaptureDevice *result = nil;
    
    NSArray *devices = [ImageSnap videoDevices];
	for( QTCaptureDevice *device in devices ){
        if ( [name isEqualToString:[device description]] ){
            result = device;
        }   // end if: match
    }   // end for: each device
    
    return result;
}   // end


// Saves an image to a file or standard out if path == nil.
+ (BOOL) saveImage:(NSImage *)image toPath: (NSString*)path{
    
    NSString *ext = [path pathExtension];
    NSData *photoData = [ImageSnap dataFrom:image asType:ext];
    
    // If path is a dash, that means write to standard out
    if( path == nil || [@"-" isEqualToString:path] ){
        NSUInteger length = [photoData length];
        NSUInteger i;
        char *start = (char *)[photoData bytes];
        for( i = 0; i < length; ++i ){
            putc( start[i], stdout );
        }   // end for: write out
        return YES;
    } else {
        return [photoData writeToFile:path atomically:NO];
    }

    
    return NO;
}


/**
 * Converts an NSImage into NSData. Defaults to jpeg if
 * format cannot be determined.
 */
+(NSData *)dataFrom:(NSImage *)image asType:(NSString *)format{
    
    NSData *tiffData = [image TIFFRepresentation];
    
    NSBitmapImageFileType imageType = NSJPEGFileType;
    NSDictionary *imageProps = nil;
    
    
    // TIFF. Special case. Can save immediately.
    if( [@"tif" rangeOfString:format options:NSCaseInsensitiveSearch].location != NSNotFound ){
        return tiffData;
    }
    
    // JPEG
    else if( [@"jpeg" rangeOfString:format options:NSCaseInsensitiveSearch].location != NSNotFound || 
             [@"jpg"  rangeOfString:format options:NSCaseInsensitiveSearch].location != NSNotFound ){
        imageType = NSJPEGFileType;
        imageProps = [NSDictionary dictionaryWithObject:[NSNumber numberWithFloat:0.9] forKey:NSImageCompressionFactor];
        
    }
    
    // PNG
    else if( [@"png" rangeOfString:format options:NSCaseInsensitiveSearch].location != NSNotFound ){
        imageType = NSPNGFileType;
    }
    
    // BMP
    else if( [@"bmp" rangeOfString:format options:NSCaseInsensitiveSearch].location != NSNotFound ){
        imageType = NSBMPFileType;
    }
    
    // GIF
    else if( [@"gif" rangeOfString:format options:NSCaseInsensitiveSearch].location != NSNotFound ){
        imageType = NSGIFFileType;
    }
    
    NSBitmapImageRep *imageRep = [NSBitmapImageRep imageRepWithData:tiffData];
    NSData *photoData = [imageRep representationUsingType:imageType properties:imageProps];

    return photoData;
}   // end dataFrom



/**
 * Primary one-stop-shopping message for capturing an image.
 * Activates the video source, saves a frame, stops the source,
 * and saves the file.
 */
+(BOOL)saveSingleSnapshotFrom:(QTCaptureDevice *)device toFile:(NSString *)path{
    ImageSnap *snap;
    NSImage *image = nil;
    
    snap = [[ImageSnap alloc] init];            // Instance of this ImageSnap class
    if( [snap startSession:device] ){           // Try starting session
        image = [snap snapshot];                // Capture a frame
        [snap stopSession];                     // Stop session
    }   // end if: able to start session
    
    [snap release];
    return image == nil ? NO : [ImageSnap saveImage:image toPath:path];
}   // end


/**
 * Returns current snapshot or nil if there is a problem
 * or session is not started.
 */
-(NSImage *)snapshot{
    
    CVImageBufferRef frame = nil;               // Hold frame we find
    while( frame == nil ){                      // While waiting for a frame
        @synchronized(self){                    // Lock since capture is on another thread
            frame = mCurrentImageBuffer;        // Hold current frame
            CVBufferRetain(frame);              // Retain it (OK if nil)
        }   // end sync: self
        if( frame == nil ){                     // Still no frame? Wait a little while.
            [[NSRunLoop currentRunLoop] runUntilDate:[NSDate dateWithTimeIntervalSinceNow: 0.1]];
        }   // end if: still nothing, wait
    }   // end while: no frame yet
    
    // Convert frame to an NSImage
    NSCIImageRep *imageRep = [NSCIImageRep imageRepWithCIImage:[CIImage imageWithCVImageBuffer:frame]];
    NSImage *image = [[[NSImage alloc] initWithSize:[imageRep size]] autorelease];
    [image addRepresentation:imageRep];
    
    return image;
}




/**
 * Blocks until session is stopped.
 */
-(void)stopSession{
    
    // Make sure we've stopped
    while( mCaptureSession != nil ){

        [mCaptureSession stopRunning];

        if( [mCaptureSession isRunning] ){
            [[NSRunLoop currentRunLoop] runUntilDate:[NSDate dateWithTimeIntervalSinceNow: 0.1]];
        }else {
            
            mCaptureSession ? [mCaptureSession release]:0;
            mCaptureDeviceInput ? [mCaptureDeviceInput release]:0;
            mCaptureDecompressedVideoOutput ? [mCaptureDecompressedVideoOutput release]:0;
            
            mCaptureSession = nil;
            mCaptureDeviceInput = nil;
            mCaptureDecompressedVideoOutput = nil;
        }   // end if: stopped
        
    }   // end while: not stopped
}


/**
 * Begins the capture session. Frames begin coming in.
 */
-(BOOL)startSession:(QTCaptureDevice *)device{
    if( device == nil ) return NO;
    
    NSError *error = nil;
    
    // If we've already started with this device, return
    if( [device isEqual:[mCaptureDeviceInput device]] &&
         mCaptureSession != nil &&
        [mCaptureSession isRunning] ){
        return YES;
    }   // end if: already running
    else if( mCaptureSession != nil ){
        [self stopSession];
    }   // end if: else stop session
    
	
	// Create the capture session
    mCaptureSession = [[QTCaptureSession alloc] init];
	if( ![device open:&error] ){
		error( "Could not create capture session.\n" );
        [mCaptureSession release];
        mCaptureSession = nil;
		return NO;
	}
    
	
	// Create input object from the device
	mCaptureDeviceInput = [[QTCaptureDeviceInput alloc] initWithDevice:device];
	if (![mCaptureSession addInput:mCaptureDeviceInput error:&error]) {
		error( "Could not convert device to input device.\n");
        [mCaptureSession release];
        [mCaptureDeviceInput release];
        mCaptureSession = nil;
        mCaptureDeviceInput = nil;
		return NO;
	}
    
	
	// Decompressed video output
	mCaptureDecompressedVideoOutput = [[QTCaptureDecompressedVideoOutput alloc] init];
	[mCaptureDecompressedVideoOutput setDelegate:self];
	if (![mCaptureSession addOutput:mCaptureDecompressedVideoOutput error:&error]) {
		error( "Could not create decompressed output.\n");
        [mCaptureSession release];
        [mCaptureDeviceInput release];
        [mCaptureDecompressedVideoOutput release];
        mCaptureSession = nil;
        mCaptureDeviceInput = nil;
        mCaptureDecompressedVideoOutput = nil;
		return NO;
	}

    // Clear old image?
    @synchronized(self){
        if( mCurrentImageBuffer != nil ){
            CVBufferRelease(mCurrentImageBuffer);
            mCurrentImageBuffer = nil;
        }   // end if: clear old image
    }   // end sync: self
    
	[mCaptureSession startRunning];
    
    return YES;
}   // end startSession



// This delegate method is called whenever the QTCaptureDecompressedVideoOutput receives a frame
- (void)captureOutput:(QTCaptureOutput *)captureOutput 
  didOutputVideoFrame:(CVImageBufferRef)videoFrame 
     withSampleBuffer:(QTSampleBuffer *)sampleBuffer 
       fromConnection:(QTCaptureConnection *)connection
{
    if (videoFrame == nil ) {
        return;
    }
    
    // Swap out old frame for new one
    CVImageBufferRef imageBufferToRelease;
    CVBufferRetain(videoFrame);
    @synchronized(self){
        imageBufferToRelease = mCurrentImageBuffer;
        mCurrentImageBuffer = videoFrame;
    }   // end sync
    CVBufferRelease(imageBufferToRelease);
    
}

@end


// //////////////////////////////////////////////////////////
//
// ////////  B E G I N   C - L E V E L   M A I N  //////// //
//
// //////////////////////////////////////////////////////////

int processArguments(int argc, const char * argv[]);
void printUsage(int argc, const char * argv[]);
int listDevices();
NSString *generateFilename();
QTCaptureDevice *getDefaultDevice();


// Main entry point. Since we're using Cocoa and all kinds of fancy
// classes, we have to set up appropriate pools and loops.
// Thanks to the example http://lists.apple.com/archives/cocoa-dev/2003/Apr/msg01638.html
// for reminding me how to do it.
int main (int argc, const char * argv[]) {
    
	NSAutoreleasePool *pool;
	pool = [[NSAutoreleasePool alloc] init];
    [NSApplication sharedApplication];
	
    int result = processArguments(argc, argv);
    
    //    [NSApp run];
    
    //	[pool release];
    [pool drain];
    return result;
}



/**
 * Process command line arguments and execute program.
 */
int processArguments(int argc, const char * argv[] ){
	
	NSString *filename = nil;
	QTCaptureDevice *device = nil;
	
	int i;
	for( i = 1; i < argc; ++i ){
		
		// Handle command line switches
		if (argv[i][0] == '-') {
            
            // Dash only? Means write image to stdout
            if( argv[i][1] == 0 ){
                filename = @"-";
                g_quiet = YES;
            } else {
                
                // Which switch was given
                switch (argv[i][1]) {
                    
                    // Help
                    case '?':
                    case 'h':
                        printUsage( argc, argv );
                        return 0;
                        break;

                        
                    // Verbose
                    case 'v':
                        g_verbose = YES;
                        break;

                        
                    // List devices
                    case 'l': 
                        listDevices();
                        return 0;
                        break;
                        
                    // Specify device
                    case 'd':
                        if( i+1 < argc ){
                            device = [ImageSnap deviceNamed:[NSString stringWithUTF8String:argv[i+1]]];
                            if( device == nil ){
                                error( "Device \"%s\" not found.\n", argv[i+1] );
                                return 11;
                            }   // end if: not found
                            ++i;
                        } else {
                            error( "Not enough arguments given.\n" );
                            return 10;
                        }
                        
                }	// end switch: flag value
            }   // end else: not dash only
		}	// end if: '-'
        
        // Else assume it's a filename
		else {
			filename = [NSString stringWithUTF8String:argv[i]];
		}

	}	// end for: each command line argument
	
    
    // Make sure we have a filename
	if( filename == nil ){
		filename = generateFilename();
	}	// end if: no filename given
    if( filename == nil ){
        error( "No suitable filename could be determined.\n" );
        return 1;
    }
	
    
    // Make sure we have a device
	if( device == nil ){
		device = getDefaultDevice();
	}	// end if: no device given
    if( device == nil ){
        error( "No video devices found.\n" );
        return 2;
    } else {
        console( "Capturing image from device \"%s\"...", [[device description] UTF8String] );
    }
	
    
    // Image capture
    if( [ImageSnap saveSingleSnapshotFrom:device toFile:filename] ){
        console( "%s\n", [filename UTF8String] );
    } else {
        error( "Error.\n" );
    }   // end else
    
    return 0;
}



void printUsage(int argc, const char * argv[]){
    printf( "USAGE: %s [options] [filename]\n", argv[0] );
    printf( "Version: %s\n", [VERSION UTF8String] );
    printf( "Captures an image from a video device and saves it in a file.\n" );
    printf( "If no device is specified, the system default will be used.\n" );
    printf( "If no filename is specfied, snapshot.jpg will be used.\n" );
    printf( "Supported image types: JPEG, TIFF, PNG, GIF, BMP\n" );
    printf( "  -h          This help message\n" );
    printf( "  -v          Verbose mode\n");
    printf( "  -l          List available video devices\n" );
    printf( "  -d device   Use named video device\n" );
}





/**
 * Prints a list of video capture devices to standard out.
 */
int listDevices(){
	NSArray *devices = [ImageSnap videoDevices];
    
    [devices count] > 0 ?
    printf("Video Devices:\n") :
    printf("No video devices found.\n");
    
	for( QTCaptureDevice *device in devices ){
		printf( "%s\n", [[device description] UTF8String] );
	}	// end for: each device
    return [devices count];
}

/**
 * Generates a filename for saving the image, presumably
 * because the user didn't specify a filename.
 * Currently returns snapshot.tiff.
 */
NSString *generateFilename(){
	NSString *result = @"snapshot.jpg";
	return result;
}	// end


/**
 * Gets a default video device, or nil if none is found.
 * For now, simply queries ImageSnap. May be fancier
 * in the future.
 */
QTCaptureDevice *getDefaultDevice(){
	return [ImageSnap defaultVideoDevice];
}	// end



