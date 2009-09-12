//
//  ImageSnap.h
//  ImageSnap
//
//  Created by Robert Harder on 9/10/09.
//
#import <Cocoa/Cocoa.h>
#import <QTKit/QTKit.h>
#include "ImageSnap.h"

BOOL g_verbose = NO;

@interface ImageSnap : NSObject {
    
    
    QTCaptureSession                    *mCaptureSession;
    QTCaptureDeviceInput                *mCaptureDeviceInput;
    QTCaptureDecompressedVideoOutput    *mCaptureDecompressedVideoOutput;
	NSString							*mSavePath;
    
    CVImageBufferRef                    mCurrentImageBuffer;
	BOOL								mSnapshotSaved;

}


/**
 * Returns all attached QTCaptureDevice objects that have video.
 * This includes video-only devices (QTMediaTypeVideo) and
 * audio/video devices (QTMediaTypeMuxed).
 *
 * @return autoreleased array of video devices
 */
+(NSArray *)videoDevices;

/**
 * Returns the default QTCaptureDevice object for video
 * or nil if none is found.
 */
+(QTCaptureDevice *)defaultVideoDevice;

+(QTCaptureDevice *)deviceNamed:(NSString *)name;

+ (BOOL) saveImage:(NSImage *)image toPath: (NSString*)path;
+ (BOOL) saveImageBuffer:(CVImageBufferRef)image toPath: (NSString*)path;

-(id)init;
-(void)dealloc;

-(CVImageBufferRef)currentImageBuffer;

/**
 * Captures an image from the given device and saves it
 * to the specified file. Currently only TIFF images can be saved.
 */
-(int)saveSnapshotFromDevice:(QTCaptureDevice *)device toFile:(NSString *)path;

-(BOOL)snapshotSaved;

- (void)captureOutput:(QTCaptureOutput *)captureOutput 
  didOutputVideoFrame:(CVImageBufferRef)videoFrame 
     withSampleBuffer:(QTSampleBuffer *)sampleBuffer 
       fromConnection:(QTCaptureConnection *)connection;

@end
