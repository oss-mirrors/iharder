//
//  main.m
//  XMLTree
//
//  Created by Robert Harder on Mon Dec 02 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "XMLTree.h"

int main(int argc, const char *argv[])
{
    /*
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];

//	NSURL *xmlURL = [NSURL fileURLWithPath:@"/Users/rob/Projects/XMLTree/test.xml"];
	NSURL *xmlURL = [NSURL URLWithString:@"http://iharder.sourceforge.net/macosx/xmltree/xmltree.xsa.xml"];

	XMLTree *tree = [XMLTree treeWithURL:xmlURL];
    NSLog(@"URL: %@", xmlURL);
	NSLog(@"XML: %@", [tree xml] );

    XMLTree *sub = [tree descendentNamed:@"product"];
    NSLog(@"Working with: %@", [sub xml] );
    //NSLog(@"Sub @id: %@", [sub xpath:@"@id"] );



	[pool release];
    return 0;
     */

    
    return NSApplicationMain(argc, argv);
}
