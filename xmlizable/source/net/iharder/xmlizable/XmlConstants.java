package net.iharder.xmlizable;

/**
 * Some constants used to generate XML tags and such.
 *
 *
 * <p>
 * I am placing this code in the Public Domain. Do with it as you will.
 * This software comes with no guarantees or warranties but with
 * plenty of well-wishing instead!
 * Please visit <a href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
 * periodically to check for updates or to contribute improvements.
 * </p>
 *
 * @author Robert Harder
 * @author rharder@usa.net
 * @version 1.2
 */
public interface XmlConstants 
{
    /** Namespace for the net.iharder.xml package: http://iharder.net/xmlizable */
    public final static String NAMESPACE    = "http://iharder.net/xmlizable";
    
    /** Default location of XSL file for simple representation of xmlized objects. */
    public final static String DEFAULT_XSL_URL = "http://iharder.net/xmlizable/xsl/default.xsl";
    
    
    /* The XML element <tt>object</tt> */
    public final static String OBJECT           = "object";
    
    /* The XML element <tt>primitive</tt> */
    public final static String PRIMITIVE        = "primitive";
    
    /* The XML element <tt>object-array</tt> */
    public final static String OBJECT_ARRAY     = "object-array";
    
    /* The XML element <tt>primitive-array</tt> */
    public final static String PRIMITIVE_ARRAY  = "primitive-array";
    
    /* The XML element <tt>array-array</tt> */
    public final static String ARRAY_ARRAY      = "array-array";
    
    /* The XML element <tt>null</tt> */
    public final static String NULL             = "null";
    
    
    
    /* The XML element <tt>o</tt>, condensed form of <tt>object</tt> */
    public final static String OBJECT_C     = "o";
    
    /* The XML element <tt>p</tt>, condensed form of <tt>primitive</tt> */
    public final static String PRIMITIVE_C  = "p";
    
    /* The XML element <tt>oa</tt>, condensed form of <tt>object-array</tt> */
    public final static String OBJECT_ARRAY_C  = "oa";
    
    /* The XML element <tt>pa</tt>, condensed form of <tt>primitive-array</tt> */
    public final static String PRIMITIVE_ARRAY_C  = "pa";
    
    /* The XML element <tt>aa</tt>, condensed form of <tt>array-array</tt> */
    public final static String ARRAY_ARRAY_C      = "aa";
    
    /* The XML element <tt>n</tt>, condensed form of <tt>null</tt> */
    public final static String NULL_C       = "n";
    
    
    /* The XML attribute <tt>class</tt> */
    public final static String CLASS        = "class";
    
    /* The XML attribute <tt>type</tt> */
    public final static String TYPE         = "type";
    
    /* The XML attribute <tt>length</tt> */
    public final static String LENGTH         = "length";
    
    /* The XML attribute <tt>encoding</tt> */
    public final static String ENCODING       = "encoding";
    
    
    /* The XML attribute <tt>c</tt>, condensed form of <tt>class</tt> */
    public final static String CLASS_C      = "c";
    
    /* The XML attribute <tt>t</tt>, condensed form of <tt>type</tt> */
    public final static String TYPE_C       = "t";
    
    /* The XML attribute <tt>t</tt>, condensed form of <tt>type</tt> */
    public final static String LENGTH_C       = "le";
    
    /* The XML attribute <tt>en</tt>, condensed form of <tt>encoding</tt> */
    public final static String ENCODING_C     = "en";
    
    
    
    /* The XML "type" attribute value <tt>boolean</tt> */
    public final static String BOOLEAN      = "boolean";
    
    /* The XML "type" attribute value <tt>byte</tt> */
    public final static String BYTE         = "byte";
    
    /* The XML "type" attribute value <tt>char</tt> */
    public final static String CHAR         = "char";
    
    /* The XML "type" attribute value <tt>short</tt> */
    public final static String SHORT        = "short";
    
    /* The XML "type" attribute value <tt>int</tt> */
    public final static String INT          = "int";
    
    /* The XML "type" attribute value <tt>long</tt> */
    public final static String LONG         = "long";
    
    /* The XML "type" attribute value <tt>float</tt> */
    public final static String FLOAT        = "float";
    
    /* The XML "type" attribute value <tt>double</tt> */
    public final static String DOUBLE       = "double";
    
    /* The XML "type" attribute value <tt>array</tt> */
    public final static String ARRAY       = "array";
    
    /* The XML "encoding" attribute value <tt>base64</tt> */
    public final static String BASE64       = "base64";
    
    
    /* The XML "type" attribute value <tt>bo</tt>, condensed form of <tt>boolean</tt> */
    public final static String BOOLEAN_C    = "bo";
    
    /* The XML "type" attribute value <tt>by</tt>, condensed form of <tt>byte</tt> */
    public final static String BYTE_C       = "by";
    
    /* The XML "type" attribute value <tt>c</tt>, condensed form of <tt>char</tt> */
    public final static String CHAR_C       = "c";
    
    /* The XML "type" attribute value <tt>s</tt>, condensed form of <tt>short</tt> */
    public final static String SHORT_C      = "s";
    
    /* The XML "type" attribute value <tt>i</tt>, condensed form of <tt>int</tt> */
    public final static String INT_C        = "i";
    
    /* The XML "type" attribute value <tt>lo</tt>, condensed form of <tt>long</tt> */
    public final static String LONG_C       = "lo";
    
    /* The XML "type" attribute value <tt>f</tt>, condensed form of <tt>float</tt> */
    public final static String FLOAT_C      = "f";
    
    /* The XML "type" attribute value <tt>d</tt>, condensed form of <tt>double</tt> */
    public final static String DOUBLE_C     = "d";
    
    /* The XML "type" attribute value <tt>a</tt>, condensed form of <tt>array</tt> */
    public final static String ARRAY_C      = "a";
    
    /* The XML "encoding" attribute value <tt>base64</tt>, condensed form of <tt>base64</tt> */
    public final static String BASE64_C     = "b64";
    
    
    
    /* The XML boolean array value <tt>true</tt> */
    public final static String TRUE         = "true";
    
    /* The XML boolean array value <tt>false</tt> */
    public final static String FALSE        = "false";
    
    
    /* The XML boolean array value <tt>1</tt>, condensed form of <tt>true</tt> */
    public final static String TRUE_C       = "1";
    
    /* The XML boolean array value <tt>0</tt>, condensed form of <tt>false</tt> */
    public final static String FALSE_C      = "0";
    
    
    
    /* The XML element <tt>entry</tt>, used for {@link java.util.Map}s. */
    public final static String MAP_ENTRY    = "entry";
    
    /* The XML element <tt>key</tt>, used for {@link java.util.Map}s. */
    public final static String MAP_KEY      = "key";
    
    /* The XML element <tt>value</tt>, used for {@link java.util.Map}s. */
    public final static String MAP_VALUE    = "value";
    
    
    /* The XML element <tt>e</tt>, condensed form of <tt>entry</tt>, used for {@link java.util.Map}s. */
    public final static String MAP_ENTRY_C  = "e";
    
    /* The XML element <tt>k</tt>, condensed form of <tt>key</tt>, used for {@link java.util.Map}s. */
    public final static String MAP_KEY_C    = "k";
    
    /* The XML element <tt>v</tt>, condensed form of <tt>value</tt>, used for {@link java.util.Map}s. */
    public final static String MAP_VALUE_C  = "v";
    
    
    /* The string <tt>CDATA</tt>. */
    public final static String CDATA       = "CDATA";
    

}   // end interface XmlConstants

