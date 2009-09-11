//
//  ImageCapture.m
//  ImageCapture
//
//  Created by Robert Harder on 9/10/09.
//

#import "ImageCapture.h"


@implementation ImageCapture



- (id)init{
	self = [super init];
	mCurrentImageBuffer = nil;
	mSnapshotSaved = NO;
	return self;
}

- (void)dealloc{
	
	[mCaptureSession release];
	[mCaptureDeviceInput release];
	[mCaptureDecompressedVideoOutput release];
	[mSavePath release];
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
    
    NSArray *devices = [ImageCapture videoDevices];
	for( QTCaptureDevice *device in devices ){
        if ( [name isEqualToString:[device description]] ){
            result = device;
        }   // end if: match
    }   // end for: each device
    
    return result;
}   // end



+ (BOOL) saveImageBuffer:(CVImageBufferRef)buffer toPath: (NSString*)path{
    NSCIImageRep *imageRep = [NSCIImageRep imageRepWithCIImage:[CIImage imageWithCVImageBuffer:buffer]];
    NSImage *image = [[NSImage alloc] initWithSize:[imageRep size]];
    [image addRepresentation:imageRep];
    BOOL success = [ImageCapture saveImage:image toPath:path];
    [image release];
    return success;
}


// Saves the image to the file.
+ (BOOL) saveImage:(NSImage *)image toPath: (NSString*)path{
    
    NSString *ext = [path pathExtension];
    NSData *tiffData = [image TIFFRepresentation];
    
    NSBitmapImageFileType imageType = 0;
    NSDictionary *imageProps = nil;
    
    // TIFF. Special case. Can save immediately.
    if( [@"tiff" caseInsensitiveCompare:ext] == NSOrderedSame || 
        [@"tif" caseInsensitiveCompare:ext] == NSOrderedSame ){
        return [tiffData writeToFile:path atomically:YES];
    }
    
    // JPEG
    else if( [@"jpeg" caseInsensitiveCompare:ext] == NSOrderedSame || 
             [@"jpg" caseInsensitiveCompare:ext] == NSOrderedSame ){
        imageType = NSJPEGFileType;
        imageProps = [NSDictionary dictionaryWithObject:[NSNumber numberWithFloat:0.9] forKey:NSImageCompressionFactor];

    }
    
    // PNG
    else if( [@"png" caseInsensitiveCompare:ext] == NSOrderedSame ){
        imageType = NSPNGFileType;
    }
    
    // BMP
    else if( [@"bmp" caseInsensitiveCompare:ext] == NSOrderedSame ){
        imageType = NSBMPFileType;
    }
    
    // GIF
    else if( [@"gif" caseInsensitiveCompare:ext] == NSOrderedSame ){
        imageType = NSGIFFileType;
    }
    
    
    // Save if file type is supported.
    if( imageType == 0 ){
        fprintf(stderr, "Unsupported file type: %s\n", [ext UTF8String] );
        return NO;
    } else {
        NSBitmapImageRep *imageRep = [NSBitmapImageRep imageRepWithData:tiffData];
        NSData *photoData = [imageRep representationUsingType:imageType properties:imageProps];
        return [photoData writeToFile:path atomically:NO];
    }
    
    return NO;
}





// Asynchronously captures and saves image to file.
-(int)saveSnapshotFromDevice:(QTCaptureDevice *)device 
					   toFile:(NSString *)path{
    
    NSError *error = nil;
    BOOL success;
    
	
	// Create the capture session
    g_verbose ? printf("Creating QTCaptureSession..."):0;
	mCaptureSession = [[QTCaptureSession alloc] init];
	success = [device open:&error];
	if( !success ){
		fprintf( stderr, "Could not create capture session.\n");
		return success;
	}
    g_verbose ? printf("Done.\n"):0;
    
	
	// Create input object from the device
    g_verbose ? printf("Creating QTCaptureDeviceInput..."):0;
	mCaptureDeviceInput = [[QTCaptureDeviceInput alloc] initWithDevice:device];
	success = [mCaptureSession addInput:mCaptureDeviceInput error:&error];
	if (!success) {
		fprintf(stderr, "Could not convert device to input device.\n");
		return success;
	}
    g_verbose ? printf("Done.\n"):0;
    
	
	// Decompressed video output
    g_verbose ? printf("Creating QTCaptureDecompressedVideoOutput..."):0;
	mCaptureDecompressedVideoOutput = [[QTCaptureDecompressedVideoOutput alloc] init];
	[mCaptureDecompressedVideoOutput setDelegate:self];
	success = [mCaptureSession addOutput:mCaptureDecompressedVideoOutput error:&error];
	if (!success) {
		fprintf(stderr, "Could not create decompressed output.\n");
		return success;
	}
    g_verbose ? printf("Done.\n"):0;
	
	[mCaptureSession retain];
	[mCaptureDeviceInput retain];
	[mCaptureDecompressedVideoOutput retain];
    mSavePath = path;
    [mSavePath retain];
    
    g_verbose ? printf("Starting QTCaptureSession running...\n"):0;
	[mCaptureSession startRunning];
    
	success = YES;
	return success;
    
}   // end



// This delegate method is called whenever the QTCaptureDecompressedVideoOutput receives a frame
- (void)captureOutput:(QTCaptureOutput *)captureOutput 
  didOutputVideoFrame:(CVImageBufferRef)videoFrame 
     withSampleBuffer:(QTSampleBuffer *)sampleBuffer 
       fromConnection:(QTCaptureConnection *)connection
{
    if (videoFrame == nil ) {
        g_verbose ? printf("Null video frame received.\n"):0;
        return;
    }
    g_verbose ? printf("Frame received. "):0;
    
	// If we already have one frame, ignore others
	// until the stopRunning message is received.
	if( mCurrentImageBuffer ){
        g_verbose ? printf("Ignoring.\n"):0;
		return;
	}
    
    
	mCurrentImageBuffer = videoFrame;
	CVBufferRetain(mCurrentImageBuffer);
    printf("[X]");
    fflush(stdout);
    

    // Save image to a file
    // For when we all have Grand Central Dispatch...
    /*dispatch_async(dispatch_get_main_queue(), ^*/{
        g_verbose ? printf("Saving..."):0;
        NSCIImageRep *imageRep = [NSCIImageRep imageRepWithCIImage:[CIImage imageWithCVImageBuffer:videoFrame]];
        NSImage *image = [[[NSImage alloc] initWithSize:[imageRep size]] autorelease];
        [image addRepresentation:imageRep];
        if( ![ImageCapture saveImage:image toPath:mSavePath] ){
            fprintf(stderr, "Error saving image.\n" );
        }   // end if: error
        g_verbose ? printf("Done.\n"):0;
        /*dispatch_async(dispatch_get_main_queue(), ^*/{
            mSnapshotSaved = YES;
            g_verbose ? printf("Stopping QTCaptureSession..."):0;
            [mCaptureSession stopRunning];
            g_verbose ? printf("Stopped.\n"):0;
        }//);
    }//);
    

    
}

-(CVImageBufferRef)currentImageBuffer{
    return mCurrentImageBuffer;
}


// Flag telling if snapshot is saved, though not exactly true.
// It tells if the save process is complete, whether or not
// an error occurred.
-(BOOL)snapshotSaved{
	return mSnapshotSaved;
}


@end


// //////////////////////////////////////////////////////////
//
// ////////  B E G I N   C - L E V E L   M A I N  //////// //
//
// //////////////////////////////////////////////////////////

int processArguments(int argc, const char * argv[], NSRunLoop *runLoop);
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
	
	NSRunLoop *runLoop;
	runLoop = [NSRunLoop currentRunLoop];
	
	int result = processArguments(argc, argv, runLoop);

	[pool release];
    return result;
}



/**
 * Process command line arguments and execute program.
 */
int processArguments(int argc, const char * argv[], NSRunLoop *runLoop){
	
	NSString *filename = nil;
	QTCaptureDevice *device = nil;
	
	int i;
	for( i = 1; i < argc; ++i ){
		
		// Handle command line switches
		if (argv[i][0] == '-') {
			
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
                        device = [ImageCapture deviceNamed:[NSString stringWithUTF8String:argv[i+1]]];
                        if( device == nil ){
                            fprintf( stderr, "Device \"%s\" not found.\n", argv[i+1] );
                            return 11;
                        }   // end if: not found
                        ++i;
                    } else {
                        fprintf( stderr, "Not enough arguments given.\n" );
                        return 10;
                    }
					
			}	// end switch: flag value
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
        fprintf( stderr, "No suitable filename could be determined.\n" );
        return 1;
    }
	
    
    // Make sure we have a device
	if( device == nil ){
		device = getDefaultDevice();
	}	// end if: no device given
    if( device == nil ){
        fprintf( stderr, "No video devices found.\n" );
        return 2;
    } else {
        printf( "Capturing image from device \"%s\"", [[device description] UTF8String] );
    }
	
    
    // Begin asynchronous image capture
	ImageCapture *ic = [[ImageCapture alloc] init];
    [ic saveSnapshotFromDevice:device toFile:filename];
    
    // Wait for async image capture to complete
	//while( ![ic snapshotSaved] ){
    while( [ic snapshotSaved] == NO ){
        printf(".");
        fflush(stdout);
        [runLoop runUntilDate:[NSDate dateWithTimeIntervalSinceNow: 0.2]];
	}
    printf( "%s\n", [filename UTF8String] );
    
    [ic release];
    return 0;
}



void printUsage(int argc, const char * argv[]){
    printf( "USAGE: %s [options] [filename]\n", argv[0] );
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
	NSArray *devices = [ImageCapture videoDevices];
    
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
 * For now, simply queries ImageCapture. May be fancier
 * in the future.
 */
QTCaptureDevice *getDefaultDevice(){
	return [ImageCapture defaultVideoDevice];
}	// end



