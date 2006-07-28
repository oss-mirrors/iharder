#import "XMLTree.h"

@interface XMLTree (PrivateAPI)


@end // End Private API



@implementation XMLTree



+(XMLTree *)treeWithURL:(NSURL *)url
{
    return [[[XMLTree alloc] initWithURL:url] autorelease];
}   // end treeWithURL




+(XMLTree *)treeWithCFXMLTreeRef:(CFXMLTreeRef)ref
{
    return [[[XMLTree alloc] initWithCFXMLTreeRef:ref] autorelease];
}   // end treeWithCFXMLTreeRef






-(XMLTree *)init
{
    if( (self = [super init]) == nil )
        return nil;

    _tree = NULL;
    _node = NULL;

    return self;
}   // end init


-(XMLTree *)initWithCFXMLTreeRef:(CFXMLTreeRef)ref
{
    if( [self init] == nil )
        return nil;

    // Clean up?
    if( _tree != NULL )
        CFRelease( _tree );
    if( _node != NULL )
        CFRelease( _node );

    _tree = ref;
    _node = CFXMLTreeGetNode( _tree );
    
    CFRetain( _tree );
    CFRetain( _node );
    
    return self;
}	// end initWithCFXMLTreeRef:




-(XMLTree *)initWithURL:(NSURL *)url
{    
    _tree = CFXMLTreeCreateWithDataFromURL(
                                           kCFAllocatorDefault,
                                           (CFURLRef)url,
                                           kCFXMLParserSkipWhitespace, 
                                           NULL ); //CFIndex
    
    _node = CFXMLTreeGetNode( _tree );

    // _tree is implicitly retained
    CFRetain( _node );

    return self;
}   // end initWithURL



-(void)dealloc
{
    //NSLog( @"dealloc %@", self );

    if( _tree != NULL )
        CFRelease( _tree );

    if( _node != NULL )
        CFRelease( _node );

    _tree = NULL;
    _node = NULL;

}	// end dealloc





/* ********  A B O U T   P A R E N T  ******** */



-(XMLTree *)parent
{
    if( _tree == NULL )
        return nil;

    CFTreeRef  parent;
    XMLTree   *returnVal;

    parent    = CFTreeGetParent( _tree );
    returnVal = nil;

    if( parent ){
        CFRetain( parent );
        returnVal = [XMLTree treeWithCFXMLTreeRef:parent];
        CFRelease( parent );
        parent = NULL;
    }	// end if: got parent

    return returnVal;
}	// end parent





/* ********  A B O U T   C H I L D R E N  ******** */


-(int)count
{
    if( _tree == NULL )
        return -1;
    
    return CFTreeGetChildCount( _tree );
}	// end count



-(XMLTree *)childAtIndex:(int)index
{
    CFXMLTreeRef child;
    
    if( _tree == NULL )
        return nil;

    if( index >= CFTreeGetChildCount( _tree ) )
        return nil;

    child = CFTreeGetChildAtIndex(_tree, index);
    // Don't need to retain or release child. I think.
    
    return [XMLTree treeWithCFXMLTreeRef:child];    
}	// end childAtIndex:



-(XMLTree *)childNamed:(NSString *)name
{
    CFXMLTreeRef  childTree;
    CFXMLNodeRef  childNode;
    CFStringRef   childName;
    XMLTree      *returnVal;
    int           childCount;
    int           i;

    if( _tree == NULL )
        return nil;

    childCount = CFTreeGetChildCount( _tree );
    returnVal  = nil;
    
    for( i = 0; i < childCount; i++ ){
        
        childTree = CFTreeGetChildAtIndex(_tree, i );
        CFRetain( childTree );
        
        childNode = CFXMLTreeGetNode( childTree );
        CFRetain( childNode );
        
        childName = CFXMLNodeGetString( childNode );
        CFRetain( childName );

        if( CFStringCompare( (CFStringRef)name, childName, NULL ) == kCFCompareEqualTo )
            returnVal = [XMLTree treeWithCFXMLTreeRef:childTree];

        CFRelease( childTree );
        CFRelease( childNode );
        CFRelease( childName );

        if( returnVal )
            break;
        
    }	// end for: each child

    return nil;
}	// end childNamed:


-(XMLTree *)descendentNamed:(NSString *)name
{
    CFXMLTreeRef       descTree;
    XMLTree           *returnVal;

    if( _tree == NULL )
        return nil;
    
    descTree = XMLTreeDescendentNamed( (CFStringRef)name, _tree );

    if( descTree == NULL )
        return nil;

    // descTree will have a +1 retain count that we
    // are responsible for releasing (see comments on that function).
    returnVal = [XMLTree treeWithCFXMLTreeRef:descTree];
    CFRelease( descTree );

    return returnVal;
}	// end descendentNamed:



/* ********  A B O U T   S E L F  ******** */




-(NSString *)name
{
    if( _node == NULL )
        return nil;

    return [NSString stringWithString:(NSString *)CFXMLNodeGetString(_node)];
}	// end name


/*!
  @discussion
   Returns the node type, as defined by Apple's XML parser.
   The values will be one of the following constants:
 <pre>
 enum CFXMLNodeTypeCode {
     kCFXMLNodeTypeDocument = 1,
     kCFXMLNodeTypeElement = 2,
     kCFXMLNodeTypeAttribute = 3,
     kCFXMLNodeTypeProcessingInstruction = 4,
     kCFXMLNodeTypeComment = 5,
     kCFXMLNodeTypeText = 6,
     kCFXMLNodeTypeCDATASection = 7,
     kCFXMLNodeTypeDocumentFragment = 8,
     kCFXMLNodeTypeEntity = 9,
     kCFXMLNodeTypeEntityReference = 10,
     kCFXMLNodeTypeDocumentType = 11,
     kCFXMLNodeTypeWhitespace = 12,
     kCFXMLNodeTypeNotation = 13,
     kCFXMLNodeTypeElementTypeDeclaration = 14,
     kCFXMLNodeTypeAttributeListDeclaration = 15
 };
 </pre>
 */
-(CFXMLNodeTypeCode)type
{
    return CFXMLNodeGetTypeCode(_node);
}	// end type



-(NSDictionary *)attributes
{
    CFXMLElementInfo eInfo;

    if( CFXMLNodeGetTypeCode( _node ) != kCFXMLNodeTypeElement )
        return nil;

    eInfo = *(CFXMLElementInfo *)CFXMLNodeGetInfoPtr(_node);

    return [[(NSDictionary *)eInfo.attributes retain] autorelease];
}	// end attributes







-(NSString *)attributeNamed:(NSString *)name
{
    if( _tree == NULL )
        return nil;

    return [[[[[self attributes] objectForKey:name] description] retain] autorelease];
}	// end attributeNamed:





-(NSString *)description
{
    NSMutableString *descr;
    
    descr = [NSMutableString string];

    //NSLog( @"Description for type %d", CFXMLNodeGetTypeCode(_node) );
    
    switch( CFXMLNodeGetTypeCode(_node) ){

        case kCFXMLNodeTypeDocument:
        case kCFXMLNodeTypeElement:
            XMLTreeDescription( (CFMutableStringRef)descr, _tree );
            break;
            
        case kCFXMLNodeTypeProcessingInstruction:
        case kCFXMLNodeTypeAttribute:
        case kCFXMLNodeTypeComment:
        case kCFXMLNodeTypeText:
        case kCFXMLNodeTypeCDATASection:
        case kCFXMLNodeTypeDocumentFragment:
        case kCFXMLNodeTypeEntity:
        case kCFXMLNodeTypeEntityReference:
        case kCFXMLNodeTypeDocumentType:
        case kCFXMLNodeTypeWhitespace:
        case kCFXMLNodeTypeNotation:
        case kCFXMLNodeTypeElementTypeDeclaration:
        case kCFXMLNodeTypeAttributeListDeclaration:
        default:
            [descr appendString:(NSString *)CFXMLNodeGetString(_node)];
    }	// end switch

    return descr;
}	// end description




-(NSString *)xml
{
    CFDataRef  xmlData;

    if( _tree == NULL )
        return nil;

    xmlData = CFXMLTreeCreateXMLData(
                                     kCFAllocatorDefault,
                                     _tree );
    if( xmlData == NULL )
        return nil;

    return [[[NSString alloc] initWithData:(NSData *)xmlData
                                  encoding:NSASCIIStringEncoding] autorelease];

}	// end xml




@end // End implementation



CFStringRef XMLTreeDescription( CFMutableStringRef descr, CFXMLTreeRef tree )
{
    CFXMLTreeRef childTree;
    CFXMLNodeRef childNode;
    int childCount;
    int i;

    childCount = CFTreeGetChildCount( tree );

    for( i = 0; i < childCount; i++ ){

        childTree = CFTreeGetChildAtIndex( tree, i );
        CFRetain( childTree );
        
        childNode = CFXMLTreeGetNode( childTree );
        CFRetain( childNode );

        switch( CFXMLNodeGetTypeCode( childNode ) ){

            case kCFXMLNodeTypeText:
                CFStringAppend( descr, CFXMLNodeGetString( childNode ) );
                break;

            case kCFXMLNodeTypeElement:
                XMLTreeDescription( descr, childTree );
                break;

            default:
                break;
        }	// end switch: node type

        CFRelease( childTree );
        CFRelease( childNode );
    }	// end for

    return descr;
}	// end XMLTreeDescription



CFXMLTreeRef XMLTreeDescendentNamed( CFStringRef name, CFXMLTreeRef tree )
{
    CFXMLTreeRef childTree;
    CFXMLTreeRef descTree;
    CFXMLNodeRef childNode;
    CFStringRef  childName;
    CFXMLTreeRef returnVal;
    int childCount;
    int i;
    
    childCount = CFTreeGetChildCount( tree );
    returnVal  = NULL;
    descTree   = NULL;

    for( i = 0; i < childCount; i++ ){

        childTree = CFTreeGetChildAtIndex( tree, i );
        CFRetain( childTree );
        
        childNode = CFXMLTreeGetNode( childTree );
        CFRetain( childNode );
        
        childName = CFXMLNodeGetString( childNode );
        CFRetain( childName );

        // Is this it?
        if( CFStringCompare( name, childName, NULL ) == kCFCompareEqualTo ){
            returnVal = childTree;
            CFRetain( returnVal );
        }	// end if: found it
        
        // Else if child is an element, search recursively
        else if( CFXMLNodeGetTypeCode( childNode ) == kCFXMLNodeTypeElement ){

            descTree = XMLTreeDescendentNamed( name, childTree );
                    
                // Got a match?
            if( descTree != NULL ){
                returnVal = descTree; // Alread +1 retain count
            }	// end if: got match
                        
        }	// end if: element node type
        
    }	// end for

    return returnVal;
}	// end XMLTreeDescendentNamed:

