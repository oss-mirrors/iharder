package net.iharder.xmlizable;

/**
 *  A collection of utilities as static methods for parsing Java objects into
 *  SAX2 events that can then be presumably written to an output stream as
 *  familiar XML code. <p/>
 *
 *  Suppose you had a {@link java.util.Map} of stock ticker names and values. A
 *  call to <code>ParserUtilities.parseObject( stockMap )</code> would generate
 *  SAX2 events resulting in something like the following XML code: <pre>
 *   &lt;?xml version="1.0" encoding="iso-8859-1"?&gt;
 *   &lt;h:object h:class="java.util.HashMap" xmlns:h="http://www.iharder.net/xml-util"&gt;
 *     &lt;h:entry&gt;
 *       &lt;h:key&gt;&lt;h:object h:class="java.lang.String"&gt;AAPL&lt;/h:object&gt;&lt;/h:key&gt;
 *       &lt;h:value&gt;&lt;h:object h:class="java.lang.Double"&gt;20.38&lt;/h:object&lt;/h:value&gt;
 *     &lt;/h:entry&gt;
 *     ...
 *     &lt;h:entry&gt;
 *       &lt;h:key&gt;&lt;h:object h:class="java.lang.String"&gt;SUNW&lt;/h:object&gt;&lt;/h:key&gt;
 *       &lt;h:value&gt;&lt;h:object h:class="java.lang.Double"&gt;24.12&lt;/h:object&lt;/h:value&gt;
 *     &lt;/h:entry&gt;
 *   &lt;/h:object&gt;
 * </pre> <P>
 *
 *  You might then read in that XML document using your favorite XML parser,
 *  such as Xerces or an Aelfred variant, and pass a new {@link ObjectHandler}
 *  as the {@link org.xml.sax.ContentHandler} that the parser expects. After the
 *  parsing, a call to the {@link ObjectHandler}'s {@link
 *  ObjectHandler#getObject getObject()} method would return the {@link
 *  java.util.Map} contained within. </P> <P>
 *
 *  There is built-in support for some common Java classes such as the
 *  primitives (<tt>int</tt> , <tt>double</tt> , and so forth) as well as their
 *  object equivalents (<tt>Integer</tt> , <tt>Double</tt> ...). Also supported
 *  internally are {@link java.util.Collection}s, {@link java.util.Map}s, and
 *  {@link java.io.File}. </P> <P>
 *
 *  You can make your own objects eligible to be converted to XML by
 *  implementing the {@link Xmlizable} interface. Unlike the {@link
 *  java.io.Serializable} interface, there are actually methods that you must
 *  implement. </P> <P>
 *
 *  If all else fails and the object is {@link java.io.Serializable}, then the
 *  object will be serialized, converted to Base64 notation, and stored in the
 *  XML stream that way. </P> <P>
 *
 *  To save a little extra space, you can also specify that the parser should
 *  use condensed names which uses one-character element and attribute names.
 *  The table below shows the long and condensed names: </P> <P
 *  style="text-align: center">
 *
 *  <style type="text/css"> <!-- #namesTable .center { text-align: center } -->
 *  </style>
 *  <tableid="namesTable">
 *
 *    <thead>
 *
 *      <tr>
 *
 *        <th>
 *          Long name
 *        </th>
 *
 *        <th>
 *          Condensed name
 *        </th>
 *
 *        <th>
 *          XML role
 *        </th>
 *
 *      </tr>
 *
 *    </thead>
 *    <tbody>
 *    <tr>
 *
 *      <td>
 *        object
 *      </td>
 *
 *      <tdclass="center">
 *        o
 *      </td>
 *
 *      <td>
 *        element
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        primitive
 *      </td>
 *
 *      <tdclass="center">
 *        p
 *      </td>
 *
 *      <td>
 *        element
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        object-array
 *      </td>
 *
 *      <tdclass="center">
 *        oa
 *      </td>
 *
 *      <td>
 *        element
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        primitive-array
 *      </td>
 *
 *      <tdclass="center">
 *        pa
 *      </td>
 *
 *      <td>
 *        element
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        null
 *      </td>
 *
 *      <tdclass="center">
 *        n
 *      </td>
 *
 *      <td>
 *        element
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        class
 *      </td>
 *
 *      <tdclass="center">
 *        c
 *      </td>
 *
 *      <td>
 *        attribute
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        type
 *      </td>
 *
 *      <tdclass="center">
 *        t
 *      </td>
 *
 *      <td>
 *        attribute
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        encoding
 *      </td>
 *
 *      <tdclass="center">
 *        en
 *      </td>
 *
 *      <td>
 *        attribute
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        boolean
 *      </td>
 *
 *      <tdclass="center">
 *        bo
 *      </td>
 *
 *      <td>
 *        attribute value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        byte
 *      </td>
 *
 *      <tdclass="center">
 *        by
 *      </td>
 *
 *      <td>
 *        attribute value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        char
 *      </td>
 *
 *      <tdclass="center">
 *        c
 *      </td>
 *
 *      <td>
 *        attribute value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        short
 *      </td>
 *
 *      <tdclass="center">
 *        s
 *      </td>
 *
 *      <td>
 *        attribute value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        int
 *      </td>
 *
 *      <tdclass="center">
 *        i
 *      </td>
 *
 *      <td>
 *        attribute value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        long
 *      </td>
 *
 *      <tdclass="center">
 *        lo
 *      </td>
 *
 *      <td>
 *        attribute value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        float
 *      </td>
 *
 *      <tdclass="center">
 *        f
 *      </td>
 *
 *      <td>
 *        attribute value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        double
 *      </td>
 *
 *      <tdclass="center">
 *        d
 *      </td>
 *
 *      <td>
 *        attribute value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        length
 *      </td>
 *
 *      <tdclass="center">
 *        le
 *      </td>
 *
 *      <td>
 *        attribute value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        base64
 *      </td>
 *
 *      <tdclass="center">
 *        b64
 *      </td>
 *
 *      <td>
 *        attribute value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        true
 *      </td>
 *
 *      <tdclass="center">
 *        1
 *      </td>
 *
 *      <td>
 *        boolean array value
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        false
 *      </td>
 *
 *      <tdclass="center">
 *        0
 *      </td>
 *
 *      <td>
 *        boolean array value
 *      </td>
 *
 *    </tr>
 *    </tbody>
 *  </table>
 *  </P> <p>
 *
 *  I am placing this code in the Public Domain. Do with it as you will. This
 *  software comes with no guarantees or warranties but with plenty of
 *  well-wishing instead! Please visit <a href="http://iharder.net/xmlizable">
 *  http://iharder.net/xmlizable</a> periodically to check for updates or to
 *  contribute improvements. </p>
 *
 * @author     Robert Harder
 * @author     rharder@usa.net
 * @created    February 27, 2002
 * @version    1.3
 */
public class ParserUtilities implements XmlConstants
{

	/**
	 *  Blocks instantiation.
	 *
	 * @since    1.2
	 */
	private ParserUtilities()
	{
	}



	/**
	 *  Convenience method to save an object to an XML file. If the file name ends
	 *  in <tt>.gz</tt> , the file will be GZip compressed, likely reducing the
	 *  file size to 20% of its uncompressed, raw XML form.
	 *
	 * @param  obj                      The object to save
	 * @param  toFile                   The file where the object will be saved
	 * @exception  java.io.IOException  Description of Exception
	 * @since                           1.3
	 */
	public static void saveObject( Object obj, java.io.File toFile )
		throws java.io.IOException
	{
		java.io.OutputStream out = null;
		try
		{
			out = new java.io.BufferedOutputStream(
				new java.io.FileOutputStream( toFile ) );

			saveObject( obj, out, toFile.getName().endsWith( ".gz" ) );
		}// end try
		finally
		{
			try
			{
				out.close();
			}
			catch ( Exception e )
			{
			}
		}// end finally
	}// end saveObject



	/**
	 *  Convenience method to save an object to an XML stream. If <var>compress
	 *  </var> is true, the stream will be GZip compressed, likely reducing the
	 *  size to 20% of its uncompressed, raw XML form.
	 *
	 * @param  obj                      The object to save
	 * @param  toStream                 The stream where the object will be saved
	 * @param  compress                 Whether or not to GZip-compress the stream
	 * @exception  java.io.IOException  Description of Exception
	 * @since                           1.3
	 */
	public static void saveObject( Object obj, java.io.OutputStream toStream, boolean compress )
		throws java.io.IOException
	{
		com.megginson.sax.DataWriter dw = null;
		java.io.Writer wr = null;
		java.io.OutputStream out = toStream;
		try
		{
			if ( compress )
			{
				out = new java.util.zip.GZIPOutputStream( toStream );
			}

			wr = new java.io.OutputStreamWriter( out );
			dw = new com.megginson.sax.DataWriter( wr );
			dw.setIndentStep( 2 );
			dw.setPrefix( NAMESPACE, "h" );

			String piData = "type=\"text/xsl\" href=\"" + DEFAULT_XSL_URL + "\"";

			dw.startDocument();
			dw.processingInstruction( "xml-stylesheet", piData );
			ParserUtilities.parseObject( obj, dw, false );
			dw.endDocument();

		}// end try
		catch ( org.xml.sax.SAXException e )
		{
			throw new java.io.IOException( e.getMessage() );
		}// end catch
		catch ( net.iharder.xmlizable.NotXmlizableException e )
		{
			throw new java.io.IOException( e.getMessage() );
		}// end catch
		finally
		{
			if ( compress )
			{
				try
				{
					( (java.util.zip.GZIPOutputStream) out ).finish();
				}
				catch ( Exception e )
				{
				}
			}// end if: compress
		}// end finally

	}// end saveObject


	/**
	 *  Convenience method to read an object from an XML file. If the file name
	 *  ends in <tt>.gz</tt> , the file will be GZip decompressed.
	 *
	 * @param  fromFile                 The file from which the object will be read
	 * @return                          The object read from the file
	 * @exception  java.io.IOException  Description of Exception
	 * @since                           1.3
	 */
	public static Object readObject( java.io.File fromFile )
		throws java.io.IOException
	{
		java.io.InputStream in = null;
		Object obj = null;
		try
		{
			in = new java.io.BufferedInputStream(
				new java.io.FileInputStream( fromFile ) );

			obj = readObject( in, fromFile.getName().endsWith( ".gz" ) );
		}// end try
		finally
		{
			try
			{
				in.close();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}// end finally

		return obj;
	}// end readObject



	/**
	 *  Convenience method to read an object from an XML stream. If <var>decompress
	 *  </var> is true, the stream will be GZip decompressed. This method uses an
	 *  included SAX2 parser known as Aelfred&em;at least one version of it. There
	 *  are several. If you'd prefer to tie in a JAXP parser, you can review the
	 *  commented-out code in this method. Note that this version of Aelfred is
	 *  released under a BSD-style license. The actual License is kept in the same
	 *  folder as the source code. Enjoy.
	 *
	 * @param  fromStream               The stream from which the object will be
	 *      read
	 * @param  decompress               Whether or not to GZip-decompress the
	 *      stream
	 * @return                          The object read from the file
	 * @exception  java.io.IOException  Description of Exception
	 * @since                           1.3
	 */
	public static Object readObject( java.io.InputStream fromStream, boolean decompress )
		throws java.io.IOException
	{
		ObjectHandler oh = new ObjectHandler();
		java.io.InputStream is = fromStream;

		try
		{
			if ( decompress )
			{
				is = new java.util.zip.GZIPInputStream( is );
			}

			is = new java.io.BufferedInputStream( is );
// jbm  using the jaxp stuff from Sun
			/*
			 *  / Three lines that use included Aelfred parser
			 *  org.xml.sax.XMLReader xr = new org.dom4j.io.aelfred.SAXDriver();    // BSD-Licensed
			 *  xr.setContentHandler( oh );                                         // from dom4j.org
			 *  xr.parse( new org.xml.sax.InputSource( is ) );                      // Enjoy!
			 */
			/*
			 *
			 */
			// Lines that use JAXP, available in JDK1.4
			javax.xml.transform.Source src = new javax.xml.transform.stream.StreamSource( is );
			javax.xml.transform.Result rst = new javax.xml.transform.sax.SAXResult( oh );
			javax.xml.transform.Transformer trans =
				javax.xml.transform.TransformerFactory.newInstance().newTransformer();
			trans.transform( src, rst );
			/*
			 *
			 */
		}// end try
		/*
		 *  catch( org.xml.sax.SAXException e )                     // Needed if you're using the
		 *  {   throw new java.io.IOException( e.getMessage() );    // SAX2 version of Aelfred,
		 *  }   // end catch
		 */// BSD-licensed from dom4j.org
		/*
		 *
		 */
		catch ( javax.xml.transform.TransformerConfigurationException e )
		{// These catch blocks
			// are needed if you're
			throw new java.io.IOException( e.getMessage() );// using the Java API's
		}// end catch                                                    // for XML Parsing (JAXP)
		catch ( javax.xml.transform.TransformerException e )
		{// available from
			// java.sun.com separately
			throw new java.io.IOException( e.getMessage() );// or as part of JDK 1.4.
		}// end catch
		/*
		 *
		 */
		return oh.getObject();
	}// end readObject



	/**
	 *  Writes a {@link java.lang.String} to the XML stream via SAX2 events using
	 *  the long element/attribute names (<tt>object</tt> and <tt>class</tt> ).
	 *  This is equivalent to calling {@link #parseObject parseObject( s,
	 *  handler)}. This results in something like the following: <pre>
	 *   &lt;h:object class="java.lang.String"&gt;Hello, world!&lt;/h:object&gt;
	 * </pre>
	 *
	 * @param  s                             Description of Parameter
	 * @param  handler                       Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseString( final String s, final org.xml.sax.ContentHandler handler )
		throws org.xml.sax.SAXException
	{
		parseString( s, handler, false );
	}// end parseString


	/**
	 *  Writes a {@link java.lang.String} to the XML stream via SAX2 events using
	 *  the long or condensed element/attribute names as specified. This is
	 *  equivalent to calling {@link #parseObject parseObject( s, handler)}. This
	 *  results in something like the following: <pre>
	 *   &lt;h:object class="java.lang.String"&gt;Hello, world!&lt;/h:object&gt;
	 * </pre>
	 *
	 * @param  s                             The string to parse
	 * @param  handler                       The SAX2 handler to receive events
	 * @param  condensedNames                Use condensed element/attribute names
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseString(
		final String s, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// We know that the NotXmlizableException will not be thrown
		// with a string, so just set up a trivial try/catch block.
		try
		{
			parseObject( s, handler, condensedNames );
		}// end try
		catch ( NotXmlizableException e )
		{
			System.err.println( "If you see this exception, the bug is not in your code. Please contact rharder@usa.net." );
			e.printStackTrace();
		}// end catch
	}// end parseString



	/**
	 *  Parses an object and sends it an XML stream via SAX2 events using the long
	 *  element/attribute names (<tt>object</tt> , <tt>primitive</tt> , <tt>class
	 *  </tt>, <tt>type</tt> , and <tt>null</tt> ). To use the condensed element
	 *  attribute names (<tt>o</tt> , <tt>p</tt> , <tt>c</tt> , <tt>t</tt> , and
	 *  <tt>n</tt> ) and save disk space, use the full method {@link
	 *  #parseObject(Object,org.xml.sax.ContentHandler,boolean)}. There is built-in
	 *  support for some common Java types such as {@link java.util.Collection} and
	 *  {@link java.util.Map}. <p/>
	 *
	 *  Your classes can be parsed with this method by implementing the {@link
	 *  Xmlizable} interface and generating your own SAX2 events to the supplied
	 *  handler in your {@link Xmlizable#toXml toXml(...)} method. <p/>
	 *
	 *  All objects follow this general style: <pre>
	 *   &lt;h:object class="com.mypkg.MyXmlizableClass"&gt;
	 *     ...
	 *   &lt;/h:object&gt;
	 * </pre> <p/>
	 *
	 *  This throws a {@link NotXmlizableException} if an object is encountered
	 *  that cannot be converted to XML.
	 *
	 * @param  obj                           The {@link java.lang.Object} to parse
	 * @param  handler                       Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @exception  NotXmlizableException     Description of Exception
	 * @since                                1.2
	 * @handler                              The SAX2 handler to receive the events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 * @throw                                NotXmlizableException if an object is
	 *      encountered that cannot be converted to XML.
	 */
	public final static void parseObject(
		final Object obj, final org.xml.sax.ContentHandler handler )
		throws org.xml.sax.SAXException, NotXmlizableException
	{
		parseObject( obj, handler, false );
	}// end parseObject


	/**
	 *  Parses an object and sends it an XML stream via SAX2 events using either
	 *  long or condensed element/attribute names, as specified. There is built-in
	 *  support for some common Java types such as {@link java.util.Collection} and
	 *  {@link java.util.Map}. <p/>
	 *
	 *  Your classes can be parsed with this method by implementing the {@link
	 *  Xmlizable} interface and generating your own SAX2 events to the supplied
	 *  handler in your {@link Xmlizable#toXml toXml(...)} method. <p/>
	 *
	 *  All objects follow this general style: <pre>
	 *   &lt;h:object class="com.mypkg.MyXmlizableClass"&gt;
	 *     ...
	 *   &lt;/h:object&gt;
	 * </pre> <p/>
	 *
	 *  This throws a {@link NotXmlizableException} if an object is encountered
	 *  that cannot be converted to XML.
	 *
	 * @param  obj                           The {@link java.lang.Object} to parse
	 * @param  handler                       The SAX2 handler to receive the events
	 * @param  condensedNames                Use condensed element/attribute names
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @exception  NotXmlizableException     Description of Exception
	 * @since                                1.2
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 * @throw                                NotXmlizableException if an object is
	 *      encountered that cannot be converted to XML.
	 */
	public final static void parseObject(
		final Object obj, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException, NotXmlizableException
	{
            if ( obj == null )
            {
                    parseNull( handler, condensedNames );
            }

            // First, if it's an array, handle it differently.
            else if ( obj.getClass().isArray() )
            {
                final Class compType = obj.getClass().getComponentType();

                // Is it an array of arrays (a matrix)?
                if( compType.isArray() )
                {
                    parseArrayArray( (Object[])obj, handler, condensedNames );
                }   // end if: array of arrays

                // Next, is it an Object array?
                else if ( java.lang.Object.class.isAssignableFrom( compType ) )
                {
                    // Prep attribute list: Class and then length
                    final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
                    final String uri = NAMESPACE;
                    String localName = condensedNames ? CLASS_C : CLASS;
                    final String qName = "";
                    final String type = CDATA;
                    String value = compType.getName();
                    attList.addAttribute( uri, localName, qName, type, value );

                    // Length attribute
                    final Object[] objArr = (Object[]) obj;
                    final int len = objArr.length;
                    localName = condensedNames ? LENGTH_C : LENGTH;
                    value = new Integer( len ).toString();
                    attList.addAttribute( uri, localName, qName, type, value );

                    // Start element
                    localName = condensedNames ? OBJECT_ARRAY_C : OBJECT_ARRAY;
                    handler.startElement( uri, localName, qName, attList );

                    for ( int i = 0; i < len; i++ )
                    {
                        parseObject( objArr[i], handler, condensedNames );
                    }

                    // End element
                    handler.endElement( uri, localName, qName );
                }// end if: object array

                // Else, primitive array
                else
                {
                    // Determine primitive type: boolean?
                    if ( java.lang.Boolean.TYPE.isAssignableFrom( compType ) )
                    {
                            parseBooleanArray( (boolean[]) obj, handler, condensedNames );
                    }

                    // Determine primitive type: byte?
                    else if ( java.lang.Byte.TYPE.isAssignableFrom( compType ) )
                    {
                            parseByteArray( (byte[]) obj, handler, condensedNames );
                    }

                    // Determine primitive type: char?
                    else if ( java.lang.Character.TYPE.isAssignableFrom( compType ) )
                    {
                            parseCharArray( (char[]) obj, handler, condensedNames );
                    }

                    // Determine primitive type: short?
                    else if ( java.lang.Short.TYPE.isAssignableFrom( compType ) )
                    {
                            parseShortArray( (short[]) obj, handler, condensedNames );
                    }

                    // Determine primitive type: int?
                    else if ( java.lang.Integer.TYPE.isAssignableFrom( compType ) )
                    {
                            parseIntArray( (int[]) obj, handler, condensedNames );
                    }

                    // Determine primitive type: long?
                    else if ( java.lang.Long.TYPE.isAssignableFrom( compType ) )
                    {
                            parseLongArray( (long[]) obj, handler, condensedNames );
                    }

                    // Determine primitive type: float?
                    else if ( java.lang.Float.TYPE.isAssignableFrom( compType ) )
                    {
                            parseFloatArray( (float[]) obj, handler, condensedNames );
                    }

                    // Determine primitive type: double?
                    else if ( java.lang.Double.TYPE.isAssignableFrom( compType ) )
                    {
                            parseDoubleArray( (double[]) obj, handler, condensedNames );
                    }

                    // Else, unknown primitive type
                    else
                    {
                            throw new NotXmlizableException( "Unknown primitive: " + compType );
                    }
                }// end else: primitive array
            }// end if: array

            // Else it's not an array, just a regular object.
            else
            {
                    // Prep attribute list
                    final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
                    final String uri = NAMESPACE;
                    String localName = condensedNames ? CLASS_C : CLASS;
                    final String qName = "";
                    final String type = CDATA;
                    String value = obj.getClass().getName();
                    attList.addAttribute( uri, localName, qName, type, value );

                    // parse element
                    if ( obj instanceof String || obj instanceof java.io.File )
                    {
                            // Start element
                            localName = condensedNames ? OBJECT_C : OBJECT;
                            handler.startElement( uri, localName, qName, attList );

                            parseStringContents( obj.toString(), handler );
                    }

                    else if ( obj instanceof Number )
                    {
                            // Start element
                            localName = condensedNames ? OBJECT_C : OBJECT;
                            handler.startElement( uri, localName, qName, attList );

                            parseStringContents( ( (Number) obj ).toString(), handler );
                    }

// jbm start add suport for boolean as a primitive from parseObject
                    else if ( obj instanceof Boolean )
                    {
                            // Start element
                            localName = condensedNames ? OBJECT_C : OBJECT;
                            handler.startElement( uri, localName, qName, attList );

            parseStringContents(((Boolean) obj).toString(), handler);
                    }
// jbm end

                    else if ( obj instanceof java.util.Collection )
                    {
                            // Start element
                            localName = condensedNames ? OBJECT_C : OBJECT;
                            handler.startElement( uri, localName, qName, attList );

                            parseCollectionContents( (java.util.Collection) obj, handler, condensedNames );
                    }

                    else if ( obj instanceof java.util.Map )
                    {
                            // Start element
                            localName = condensedNames ? OBJECT_C : OBJECT;
                            handler.startElement( uri, localName, qName, attList );

                            parseMapContents( (java.util.Map) obj, handler, condensedNames );
                    }

                    else if ( obj instanceof Xmlizable )
                    {
                            // Start element
                            localName = condensedNames ? OBJECT_C : OBJECT;
                            handler.startElement( uri, localName, qName, attList );

                            ( (Xmlizable) obj ).toXml( handler );
                    }

                    else if ( obj instanceof java.io.Serializable )
                    {

                            localName = condensedNames ? ENCODING_C : ENCODING;
                            value = condensedNames ? BASE64_C : BASE64;
                            attList.addAttribute( uri, localName, qName, type, value );

                            // Start element
                            localName = condensedNames ? OBJECT_C : OBJECT;
                            handler.startElement( uri, localName, qName, attList );

                            parseSerializableContents( obj, handler );
                    }

                    else
                    {
                            throw new NotXmlizableException( "Not Xmlizable: " + obj.getClass() );
                    }

                    // End element
                    handler.endElement( uri, localName, qName );
            }// end if: obj not null
	}// end parseObject



	/**
	 *  If a <tt>null</tt> object is encountered, this method is called to generate
	 *  a <tt>&lt;h:null/&gt;</tt> XML element.
	 *
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @handler                              The SAX2 handler to receive the events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseNull(
		final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		final String localName = condensedNames ? NULL_C : NULL;
		final String qName = "";
		handler.startElement( uri, localName, qName, attList );
		handler.endElement( uri, localName, qName );
	}// end parseNull

	/**
	 *  Parses an array and sends its contents to the XML
	 *  stream via SAX2 events. This simply iterates over the contents and makes
	 *  multiple calls to {@link #parseObject parseObject(...)}.
         *  This will result in a series of arrays.
	 *
	 * @param  a                             The array to parse
	 * @param  handler                       
	 * @param  condensedNames                
	 * @exception  org.xml.sax.SAXException  
	 * @exception  NotXmlizableException     
	 * @since                                1.2
	 * @handler                              The SAX2 handler to receive the events
	 * @throw                                org.xml.sax.SAXException if a SAX exception is encountered
	 * @throw                                NotXmlizableException if an object is encountered that cannot be converted to XML.
	 */
	private final static void parseArrayArray(
		final Object[] arr, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException, NotXmlizableException
	{
		// Prep attribute list: Type and then length
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		String value = condensedNames ? ARRAY_C : ARRAY;
		attList.addAttribute( uri, localName, qName, type, value );

		final int len = arr.length;
		localName = condensedNames ? LENGTH_C : LENGTH;
		value = new Integer( len ).toString();
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? ARRAY_ARRAY_C : ARRAY_ARRAY;
		handler.startElement( uri, localName, qName, attList );

            // List each array within
            for( int i = 0; i < arr.length; i++ )
                parseObject( arr[i], handler );

		// End element
		handler.endElement( uri, localName, qName );

	}// end parseArrayArray



	/*
	 *  ********  P R I M I T I V E   A R R A Y S  ********
	 */

	/**
	 *  Parses a primitive <tt>boolean</tt> array to the XML handler stream by
	 *  firing appropriate SAX2 events. A call such as <code>parseBooleanArray( myArray, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive-array type="boolean" length="3"&gt;true,true,false&lt;/h:primitive-array&gt;
	 * </pre> Or if <var>condensedNames</var> is <tt>true</tt> : <pre>
	 *     &lt;h:pa t="bo" le="3"&gt;1,1,0&lt;/h:pa&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>boolean</tt> array
	 *      to parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.3
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseBooleanArray(
		final boolean[] val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list: Type and then length
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		String value = condensedNames ? BOOLEAN_C : BOOLEAN;
		attList.addAttribute( uri, localName, qName, type, value );

		final int len = val.length;
		localName = condensedNames ? LENGTH_C : LENGTH;
		value = new Integer( len ).toString();
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_ARRAY_C : PRIMITIVE_ARRAY;
		handler.startElement( uri, localName, qName, attList );

		// Build a comma-separated list of boolean
		final StringBuffer s = new StringBuffer( len << 2 );
		final int lenM1 = len - 1;
		for ( int i = 0; i < lenM1; i++ )
		{
			s.append( val[i] ? ( condensedNames ? TRUE_C : TRUE ) : condensedNames ? FALSE_C : FALSE ).append( ',' );
		}
		s.append( val[len - 1] ? ( condensedNames ? TRUE_C : TRUE ) : condensedNames ? FALSE_C : FALSE );
		parseStringContents( s.toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseBooleanArray



	/**
	 *  Parses a primitive <tt>byte</tt> array to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseByteArray( myArray, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive-array type="byte" length="3"&gt;42,23,35&lt;/h:primitive-array&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>byte</tt> array to
	 *      parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.3
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseByteArray(
		final byte[] val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list: Type and then length
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		String value = condensedNames ? BYTE_C : BYTE;
		attList.addAttribute( uri, localName, qName, type, value );

		final int len = val.length;
		localName = condensedNames ? LENGTH_C : LENGTH;
		value = new Integer( len ).toString();
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_ARRAY_C : PRIMITIVE_ARRAY;
		handler.startElement( uri, localName, qName, attList );

		// Build a comma-separated list of bytes
		final StringBuffer s = new StringBuffer( len << 2 );
		final int lenM1 = len - 1;
		for ( int i = 0; i < lenM1; i++ )
		{
			s.append( val[i] ).append( ',' );
		}
		s.append( val[len - 1] );
		parseStringContents( s.toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseByteArray



	/**
	 *  Parses a primitive <tt>char</tt> array to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseCharArray( myArray, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive-array type="char" length="22"&gt;Shoulda' been a string&lt;/h:primitive-array&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>char</tt> array to
	 *      parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.3
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseCharArray(
		final char[] val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list: Type and then length
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		String value = condensedNames ? CHAR_C : CHAR;
		attList.addAttribute( uri, localName, qName, type, value );

		final int len = val.length;
		localName = condensedNames ? LENGTH_C : LENGTH;
		value = new Integer( len ).toString();
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_ARRAY_C : PRIMITIVE_ARRAY;
		handler.startElement( uri, localName, qName, attList );

		// Send characters like a string
		handler.characters( val, 0, len );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseCharArray




	/**
	 *  Parses a primitive <tt>short</tt> array to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseShortArray( myArray, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive-array type="short" length="3"&gt;42,23,35&lt;/h:primitive-array&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>short</tt> array to
	 *      parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.3
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseShortArray(
		final short[] val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list: Type and then length
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		String value = condensedNames ? SHORT_C : SHORT;
		attList.addAttribute( uri, localName, qName, type, value );

		final int len = val.length;
		localName = condensedNames ? LENGTH_C : LENGTH;
		value = new Integer( len ).toString();
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_ARRAY_C : PRIMITIVE_ARRAY;
		handler.startElement( uri, localName, qName, attList );

		// Build a comma-separated list of shorts
		final StringBuffer s = new StringBuffer( len << 2 );
		final int lenM1 = len - 1;
		for ( int i = 0; i < lenM1; i++ )
		{
			s.append( val[i] ).append( ',' );
		}
		s.append( val[len - 1] );
		parseStringContents( s.toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseShortArray



	/**
	 *  Parses a primitive <tt>int</tt> array to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseIntArray( myArray, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive-array type="int" length="3"&gt;42,23,35&lt;/h:primitive-array&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>int</tt> array to
	 *      parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.3
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseIntArray(
		final int[] val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list: Type and then length
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		String value = condensedNames ? INT_C : INT;
		attList.addAttribute( uri, localName, qName, type, value );

		final int len = val.length;
		localName = condensedNames ? LENGTH_C : LENGTH;
		value = new Integer( len ).toString();
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_ARRAY_C : PRIMITIVE_ARRAY;
		handler.startElement( uri, localName, qName, attList );

		// Build a comma-separated list of ints
		final StringBuffer s = new StringBuffer( len << 2 );
		final int lenM1 = len - 1;
		for ( int i = 0; i < lenM1; i++ )
		{
			s.append( val[i] ).append( ',' );
		}
		s.append( val[len - 1] );
		parseStringContents( s.toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseIntArray



	/**
	 *  Parses a primitive <tt>long</tt> array to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseIntArray( myArray, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive-array type="long" length="3"&gt;42,23,35&lt;/h:primitive-array&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>long</tt> array to
	 *      parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.3
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseLongArray(
		final long[] val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list: Type and then length
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		String value = condensedNames ? LONG_C : LONG;
		attList.addAttribute( uri, localName, qName, type, value );

		final int len = val.length;
		localName = condensedNames ? LENGTH_C : LENGTH;
		value = new Integer( len ).toString();
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_ARRAY_C : PRIMITIVE_ARRAY;
		handler.startElement( uri, localName, qName, attList );

		// Build a comma-separated list of longs
		final StringBuffer s = new StringBuffer( len << 2 );
		final int lenM1 = len - 1;
		for ( int i = 0; i < lenM1; i++ )
		{
			s.append( val[i] ).append( ',' );
		}
		s.append( val[len - 1] );
		parseStringContents( s.toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseLongArray



	/**
	 *  Parses a primitive <tt>float</tt> array to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseFloatArray( myArray, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive-array type="float" length="3"&gt;3.14,2.79,1.01&lt;/h:primitive-array&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>float</tt> array to
	 *      parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.3
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseFloatArray(
		final float[] val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list: Type and then length
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		String value = condensedNames ? FLOAT_C : FLOAT;
		attList.addAttribute( uri, localName, qName, type, value );

		final int len = val.length;
		localName = condensedNames ? LENGTH_C : LENGTH;
		value = new Integer( len ).toString();
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_ARRAY_C : PRIMITIVE_ARRAY;
		handler.startElement( uri, localName, qName, attList );

		// Build a comma-separated list of floats
		final StringBuffer s = new StringBuffer( len << 2 );
		final int lenM1 = len - 1;
		for ( int i = 0; i < lenM1; i++ )
		{
			s.append( val[i] ).append( ',' );
		}
		s.append( val[len - 1] );
		parseStringContents( s.toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseFloatArray



	/**
	 *  Parses a primitive <tt>double</tt> array to the XML handler stream by
	 *  firing appropriate SAX2 events. A call such as <code>parseDoubleArray( myArray, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive-array type="double" length="3"&gt;3.14,2.79,1.01&lt;/h:primitive-array&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>double</tt> array to
	 *      parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.3
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseDoubleArray(
		final double[] val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list: Type and then length
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		String value = condensedNames ? DOUBLE_C : DOUBLE;
		attList.addAttribute( uri, localName, qName, type, value );

		final int len = val.length;
		localName = condensedNames ? LENGTH_C : LENGTH;
		value = new Integer( len ).toString();
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_ARRAY_C : PRIMITIVE_ARRAY;
		handler.startElement( uri, localName, qName, attList );

		// Build a comma-separated list of doubles
		final StringBuffer s = new StringBuffer( len << 2 );
		final int lenM1 = len - 1;
		for ( int i = 0; i < lenM1; i++ )
		{
			s.append( val[i] ).append( ',' );
		}
		s.append( val[len - 1] );
		parseStringContents( s.toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseDoubleArray



	/*
	 *  ********  P R I M I T I V E S  ********
	 */

	/**
	 *  Parses a primitive <tt>boolean</tt> to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseBoolean( true, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive type="boolean"&gt;true&lt;/h:primitive&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>boolean</tt> to
	 *      parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.3
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseBoolean(
		final boolean val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		final String value = condensedNames ? BOOLEAN_C : BOOLEAN;
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_C : PRIMITIVE;
		handler.startElement( uri, localName, qName, attList );

		// parse element
		parseStringContents( val ? ( condensedNames ? TRUE_C : TRUE ) : condensedNames ? FALSE_C : FALSE, handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseChar



	/**
	 *  Parses a primitive <tt>byte</tt> to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseByte( (byte)42, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive type="byte"&gt;42&lt;/h:primitive&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>byte</tt> to parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseByte(
		final byte val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		final String value = condensedNames ? BYTE_C : BYTE;
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_C : PRIMITIVE;
		handler.startElement( uri, localName, qName, attList );

		// parse element
		parseStringContents( new Byte( val ).toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseChar


	/**
	 *  parses a primitive <tt>char</tt> to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseChar( 'c', myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive type="char"&gt;c&lt;/h:primitive&gt;
	 * </pre> This has not been tested with UTF-16 characters, although there
	 *  should be nothing in the code that might mess up a two-byte Unicode
	 *  character. The value <var>val</var> is in fact converted to a
	 *  {@java.lang.String}, so one would expect an appropriate conversion.
	 *
	 * @param  val                           The primitive <tt>char</tt> to parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseChar(
		final char val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		final String value = condensedNames ? CHAR_C : CHAR;
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_C : PRIMITIVE;
		handler.startElement( uri, localName, qName, attList );

		// parse element
		parseStringContents( new Character( val ).toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseChar



	/**
	 *  parses a primitive <tt>short</tt> to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseShort( 42, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive type="short"&gt;42&lt;/h:primitive&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>short</tt> to parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseShort(
		final short val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		final String value = condensedNames ? SHORT_C : SHORT;
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_C : PRIMITIVE;
		handler.startElement( uri, localName, qName, attList );

		// parse element
		parseStringContents( new Short( val ).toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseShort



	/**
	 *  parses a primitive <tt>int</tt> to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseInt( 42, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive type="int"&gt;42&lt;/h:primitive&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>int</tt> to parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseInt(
		final int val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		final String value = condensedNames ? INT_C : INT;
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_C : PRIMITIVE;
		handler.startElement( uri, localName, qName, attList );

		// parse element
		parseStringContents( new Integer( val ).toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseInt



	/**
	 *  parses a primitive <tt>long</tt> to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseLong( 42, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive type="long"&gt;42&lt;/h:primitive&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>int</tt> to parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseLong(
		final long val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		final String value = condensedNames ? LONG_C : LONG;
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_C : PRIMITIVE;
		handler.startElement( uri, localName, qName, attList );

		// parse element
		parseStringContents( new Long( val ).toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseLong



	/**
	 *  parses a primitive <tt>float</tt> to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseFloat( 42.3f, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive type="float"&gt;42.3&lt;/h:primitive&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>float</tt> to parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseFloat(
		final float val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		final String value = condensedNames ? FLOAT_C : FLOAT;
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_C : PRIMITIVE;
		handler.startElement( uri, localName, qName, attList );

		// parse element
		parseStringContents( new Float( val ).toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseFloat



	/**
	 *  parses a primitive <tt>double</tt> to the XML handler stream by firing
	 *  appropriate SAX2 events. A call such as <code>parseDouble( 42.3, myHandler )</code>
	 *  would suggest the following XML code: <pre>
	 *     &lt;h:primitive type="double"&gt;42.3&lt;/h:primitive&gt;
	 * </pre>
	 *
	 * @param  val                           The primitive <tt>double</tt> to parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @handler                              The {@link org.xml.sax.ContentHandler}
	 *      receiving the SAX events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	public final static void parseDouble(
		final double val, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException
	{
		// Prep attribute list
		final org.xml.sax.helpers.AttributesImpl attList = new org.xml.sax.helpers.AttributesImpl();
		final String uri = NAMESPACE;
		String localName = condensedNames ? TYPE_C : TYPE;
		final String qName = "";
		final String type = CDATA;
		final String value = condensedNames ? DOUBLE_C : DOUBLE;
		attList.addAttribute( uri, localName, qName, type, value );

		// Start element
		localName = condensedNames ? PRIMITIVE_C : PRIMITIVE;
		handler.startElement( uri, localName, qName, attList );

		// parse element
		parseStringContents( new Double( val ).toString(), handler );

		// End element
		handler.endElement( uri, localName, qName );
	}// end parseDouble



	/*
	 *  ********  P R I V A T E   H E L P E R   M E T H O D S  ********
	 */

	/**
	 *  Writes a {@link java.lang.String} to the XML stream by calling the {@link
	 *  org.xml.sax.ContentHandler#characters} method and passing it the string
	 *  <var>s</var> .
	 *
	 * @param  s                             Description of Parameter
	 * @param  handler                       Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	private final static void parseStringContents(
		final String s, final org.xml.sax.ContentHandler handler )
		throws org.xml.sax.SAXException
	{
		handler.characters( s.toCharArray(), 0, s.length() );
	}// end parseStringContents


	/**
	 *  Parses a {@link java.util.Collection} and sends its contents to the XML
	 *  stream via SAX2 events. This simply iterates over the contents and makes
	 *  multiple calls to {@link #parseObject parseObject(...)}. This results in
	 *  something like the following (formatting will depend on your XML output
	 *  options): <pre>
	 *   &lt;h:object class="java.lang.String"&gt;Hello, world!&lt;/h:object&gt;
	 *   ...
	 *   &lt;h:object class="java.lang.Double"&gt;3.14&lt;/h:object&gt;
	 * </pre>
	 *
	 * @param  c                             The {@link java.util.Collection} to
	 *      parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @exception  NotXmlizableException     Description of Exception
	 * @since                                1.2
	 * @handler                              The SAX2 handler to receive the events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 * @throw                                NotXmlizableException if an object is
	 *      encountered that cannot be converted to XML.
	 */
	private final static void parseCollectionContents(
		final java.util.Collection c, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException, NotXmlizableException
	{
		final java.util.Iterator iter = c.iterator();
		while ( iter.hasNext() )
		{
			parseObject( iter.next(), handler, condensedNames );
		}
	}// end parseCollectionContents


	/**
	 *  Parses a {@link java.util.Map} and sends its contents to the XML stream via
	 *  SAX2 events. This results in something like the following (formatting will
	 *  depend on your XML output options): <pre>
	 *   &lt;h:entry&gt;
	 *     &lt;h:key&gt;&lt;h:object class="java.lang.String"&gt;name&lt;/h:object&gt;&lt;/h:key&gt;
	 *     &lt;h:value&gt;&lt;h:object class="java.lang.String"&gt;John Doe&lt;/h:object&gt;&lt;/h:value&gt;
	 *   &lt;/h:entry&gt;
	 *   ...
	 *   &lt;h:entry&gt;
	 *     &lt;h:key&gt;&lt;h:object class="java.lang.String"gt;age&lt;/h:object&gt;&lt;/h:key&gt;
	 *     &lt;h:value&gt;&lt;h:object class="java.lang.Integer"&gt;24&lt;/h:object&gt;&lt;/h:value&gt;
	 *   &lt;/h:entry&gt;
	 * </pre>
	 *
	 * @param  m                             The {@link java.util.Map} to parse
	 * @param  handler                       Description of Parameter
	 * @param  condensedNames                Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @exception  NotXmlizableException     Description of Exception
	 * @since                                1.2
	 * @handler                              The SAX2 handler to receive the events
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 * @throw                                NotXmlizableException if an object is
	 *      encountered that cannot be converted to XML.
	 */
	private final static void parseMapContents(
		final java.util.Map m, final org.xml.sax.ContentHandler handler, final boolean condensedNames )
		throws org.xml.sax.SAXException, NotXmlizableException
	{
		final java.util.Iterator keys = m.keySet().iterator();
		final String map_entry = condensedNames ? MAP_ENTRY_C : MAP_ENTRY;
		final String map_key = condensedNames ? MAP_KEY_C : MAP_KEY;
		final String map_value = condensedNames ? MAP_VALUE_C : MAP_VALUE;
		final String uri = NAMESPACE;
		final String qName = "";
		final org.xml.sax.helpers.AttributesImpl emptyAtts = new org.xml.sax.helpers.AttributesImpl();

		while ( keys.hasNext() )
		{
			final Object key = keys.next();

			// Start "entry" element
			String localName = map_entry;
			handler.startElement( uri, localName, qName, emptyAtts );

			// Start "key" element
			localName = map_key;
			handler.startElement( uri, localName, qName, emptyAtts );
			// Parse "key"
			parseObject( key, handler, condensedNames );
			// End "key" element
			handler.endElement( uri, localName, qName );

			// Start "value" element
			localName = map_value;
			handler.startElement( uri, localName, qName, emptyAtts );
			// Parse "value"
			parseObject( m.get( key ), handler, condensedNames );
			// End "value" element
			handler.endElement( uri, localName, qName );

			// End "entry" element
			localName = map_entry;
			handler.endElement( uri, localName, qName );
		}// end while: each entry

	}// end parseMapContents



	/**
	 *  Writes a {@link java.io.Serializable} to the XML stream by converting the
	 *  serialized data into Base64 encoding.
	 *
	 * @param  obj                           Description of Parameter
	 * @param  handler                       Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.3
	 * @throw                                org.xml.sax.SAXException if a SAX
	 *      exception is encountered
	 */
	private final static void parseSerializableContents(
		final Object obj, final org.xml.sax.ContentHandler handler )
		throws org.xml.sax.SAXException
	{
		java.io.ByteArrayOutputStream baos = null;
		Base64.OutputStream b64os = null;
		java.io.ObjectOutputStream oos = null;

		try
		{
			// Create streams
			baos = new java.io.ByteArrayOutputStream();
			b64os = new Base64.OutputStream( baos, Base64.ENCODE );
			oos = new java.io.ObjectOutputStream( b64os );

			// Serialize obj to Base64
			oos.writeObject( obj );

			// Close
			try
			{
				oos.close();
			}
			catch ( Exception e )
			{
			}
			try
			{
				b64os.close();
			}
			catch ( Exception e )
			{
			}
			try
			{
				baos.close();
			}
			catch ( Exception e )
			{
			}

			// Pass to parseStringContents
			parseStringContents( "\n" + baos.toString(), handler );

		}// end try
		catch ( java.io.IOException e )
		{
			throw new org.xml.sax.SAXException( e.getMessage() );
		}// end catch
		finally
		{
			// Make sure we close, in case there was an error.
			try
			{
				oos.close();
			}
			catch ( Exception e )
			{
			}
			try
			{
				b64os.close();
			}
			catch ( Exception e )
			{
			}
			try
			{
				baos.close();
			}
			catch ( Exception e )
			{
			}
		}
	}// end parseSerializableContents


}

