package net.iharder.xmlizable;

/**
 *  <p>
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
public class ObjectHandler
	 extends org.xml.sax.helpers.DefaultHandler
	 implements XmlConstants
{

	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	protected Object object;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	protected String className;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	private java.util.Stack stack = new java.util.Stack();

	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	private org.xml.sax.ContentHandler altHandler;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	private java.util.Stack altHandlerElementStack;

	/**
	 *  Utility field used by bound properties.
	 *
	 * @since
	 */
	private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport( this );// end getAltHandler


	/**
	 *  Sets the altHandler attribute of the ObjectHandler object
	 *
	 * @param  handler  The new altHandler value
	 * @since
	 */
	public void setAltHandler( org.xml.sax.ContentHandler handler )
	{
		this.altHandler = handler;
	}// end getAltHandlerElementStack


	/**
	 *  Sets the altHandlerElementStack attribute of the ObjectHandler object
	 *
	 * @param  stack  The new altHandlerElementStack value
	 * @since
	 */
	public void setAltHandlerElementStack( java.util.Stack stack )
	{
		this.altHandlerElementStack = stack;
	}// end getObject


	/**
	 *  Sets the object attribute of the ObjectHandler object
	 *
	 * @param  obj  The new object value
	 * @since
	 */
	public void setObject( Object obj )
	{
		Object oldObj = this.object;
		this.object = obj;
		propertyChangeSupport.firePropertyChange( "object", oldObj, obj );
	}


	/**
	 *  Gets the altHandler attribute of the ObjectHandler object
	 *
	 * @return    The altHandler value
	 * @since
	 */
	public org.xml.sax.ContentHandler getAltHandler()
	{
		return altHandler;
	}// end setAltHandler



	/**
	 *  Gets the altHandlerElementStack attribute of the ObjectHandler object
	 *
	 * @return    The altHandlerElementStack value
	 * @since
	 */
	public java.util.Stack getAltHandlerElementStack()
	{
		return altHandlerElementStack;
	}// end resetAltHandlerStack


	/**
	 *  Gets the object attribute of the ObjectHandler object
	 *
	 * @return    The object value
	 * @since
	 */
	public Object getObject()
	{
		return object;
	}// end setAltHandler


	/**
	 *  Description of the Method
	 *
	 * @since
	 */
	public void clearAltHandler()
	{
		setAltHandler( null );
	}// end setAltHandlerElementStack


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Returned Value
	 * @since
	 */
	public java.util.Stack resetAltHandlerElementStack()
	{
		return this.altHandlerElementStack = new java.util.Stack();
	}// end resetAltHandlerStack


	/**
	 *  Description of the Method
	 *
	 * @since
	 */
	public void clearAltHandlerElementStack()
	{
		setAltHandlerElementStack( null );
	}// end setObject



	/**
	 *  Add a PropertyChangeListener to the listener list.
	 *
	 * @param  listener  The feature to be added to the PropertyChangeListener
	 *      attribute
	 * @since
	 */
	public void addPropertyChangeListener( java.beans.PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}// end addPropertyChangeListener


	/**
	 *  Removes a PropertyChangeListener from the listener list.
	 *
	 * @param  listener  Description of Parameter
	 * @since
	 */
	public void removePropertyChangeListener( java.beans.PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}// end removePropertyChangeListener



	/**
	 *  Add a PropertyChangeListener to the listener list.
	 *
	 * @param  property  The feature to be added to the PropertyChangeListener
	 *      attribute
	 * @param  listener  The feature to be added to the PropertyChangeListener
	 *      attribute
	 * @since
	 */
	public void addPropertyChangeListener( String property, java.beans.PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( property, listener );
	}// end addPropertyChangeListener


	/**
	 *  Removes a PropertyChangeListener from the listener list.
	 *
	 * @param  property  Description of Parameter
	 * @param  listener  Description of Parameter
	 * @since
	 */
	public void removePropertyChangeListener( String property, java.beans.PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( property, listener );
	}// end removePropertyChangeListener




	/*
	 *  ********  D O C U M E N T   H A N D L E R   M E T H O D S  ********
	 */

	/**
	 *  Description of the Method
	 *
	 * @param  namespaceURI                  Description of Parameter
	 * @param  localName                     Description of Parameter
	 * @param  qName                         Description of Parameter
	 * @param  atts                          Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since
	 */
	public void startElement( String namespaceURI, String localName, String qName, org.xml.sax.Attributes atts )
		throws org.xml.sax.SAXException
	{
		if ( getAltHandler() != null )
		{// Another handler is in charge.
			getAltHandlerElementStack().push( localName );
			getAltHandler().startElement( namespaceURI, localName, qName, atts );
		}// end if: another handler has taken over for a while

		// Else is it our namespace?
		else if ( NAMESPACE.equals( namespaceURI ) )
		{
			// This must be an "object", "primitive", "object-array", 
                        // "primitive-array", "array-array", or "null" element
			if ( OBJECT_C.equals( localName ) || OBJECT.equals( localName ) )
			{
				// Get attributes
				String strCurrentClass = null;
				String strEncoding = null;
				String attUri = NAMESPACE;

				// Class
				strCurrentClass = atts.getValue( attUri, CLASS_C );
				if ( strCurrentClass == null )
				{
					strCurrentClass = atts.getValue( attUri, CLASS );
				}

				// Make sure the class was specified, as required
				if ( strCurrentClass == null )
				{
					throw new org.xml.sax.SAXException( "No class specified for " + localName );
				}// end if: no currentClass

				// Get optional encoding attribute
				strEncoding = atts.getValue( attUri, ENCODING_C );
				if ( strEncoding == null )
				{
					strEncoding = atts.getValue( attUri, ENCODING );
				}

				// Get actual Class object
				Class currentClass = null;
				try
				{
					currentClass = Class.forName( strCurrentClass );
				}// end try
				catch ( ClassNotFoundException e )
				{
					e.printStackTrace();
					throw new org.xml.sax.SAXException( strCurrentClass + " not found." );
				}// end catch

				// See what we can do with this kind of class.
				// The first thing to check is if it's Base64-encoded
				if ( BASE64_C.equals( strEncoding ) || BASE64.equals( strEncoding ) )
				{
					setAltHandler( new Base64ContentsHandler() );
					resetAltHandlerElementStack().push( localName );
				}// end if: Base64 encoded

				// String ?
				else if ( java.lang.String.class.isAssignableFrom( currentClass ) )
				{
					// Set up a string handler as an alternate to this one.
					setAltHandler( new StringContentsHandler() );
					resetAltHandlerElementStack().push( localName );
				}// end if: string

				// File
				else if ( java.io.File.class.isAssignableFrom( currentClass ) )
				{
					// Set up a file handler as an alternate to this one.
					setAltHandler( new FileContentsHandler() );
					resetAltHandlerElementStack().push( localName );
				}// end if: file

				// Collection
				else if ( java.util.Collection.class.isAssignableFrom( currentClass ) )
				{
					// Get the collection object
					java.util.Collection coll = null;
					try
					{
						coll = (java.util.Collection) currentClass.newInstance();
					}
					catch ( InstantiationException e )
					{
						e.printStackTrace();
					}// end catch
					catch ( IllegalAccessException e )
					{
						e.printStackTrace();
					}// end catch
					finally
					{
						if ( coll == null )
						{
							throw new org.xml.sax.SAXException( "Error instantiating " + currentClass );
						}
					}// end finally

					// Set up a collection handler as an alternate to this one.
					setAltHandler( new CollectionContentsHandler( coll ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: collection

				// Map
				else if ( java.util.Map.class.isAssignableFrom( currentClass ) )
				{
					// Get the map object
					java.util.Map map = null;
					try
					{
						map = (java.util.Map) currentClass.newInstance();
					}
					catch ( InstantiationException e )
					{
						e.printStackTrace();
					}// end catch
					catch ( IllegalAccessException e )
					{
						e.printStackTrace();
					}// end catch
					finally
					{
						if ( map == null )
						{
							throw new org.xml.sax.SAXException( "Error instantiating " + currentClass );
						}
					}// end finally

					// Set up a collection handler as an alternate to this one.
					setAltHandler( new MapContentsHandler( map ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: map

				// Xmlizable
				else if ( net.iharder.xmlizable.Xmlizable.class.isAssignableFrom( currentClass ) )
				{
					// Get the xmlizable object
					Xmlizable xmlObj = null;
					try
					{
						xmlObj = (Xmlizable) currentClass.newInstance();
					}
					catch ( InstantiationException e )
					{
						e.printStackTrace();
					}// end catch
					catch ( IllegalAccessException e )
					{
						e.printStackTrace();
					}// end catch
					finally
					{
						if ( xmlObj == null )
						{
							throw new org.xml.sax.SAXException( "Error instantiating " + currentClass );
						}
					}// end finally

					// Set up its handler as an alternate to this one.
					setAltHandler( xmlObj.fromXml() );
					resetAltHandlerElementStack().push( localName );

					setObject( xmlObj );// Since there's no way to retrieve the Xmlizable
					// object when it's done (we don't require the
					// 'fromXml' method to return an 'ObjectHandler')
					// we just save the object now.
				}// end if: Xmlizable

				// Number
				else if ( java.lang.Number.class.isAssignableFrom( currentClass ) )
				{// Set up a primitive handler as an alternate to this one.
					setAltHandler( new PrimitiveContentsHandler( currentClass ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: a number

				// Character
				else if ( java.lang.Character.class.isAssignableFrom( currentClass ) )
				{// Set up a primitive handler as an alternate to this one.
					setAltHandler( new PrimitiveContentsHandler( currentClass ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: a Character

//  jbm start add support for boolean as a primitive from parseObject
				// Boolean
				else if ( java.lang.Boolean.class.isAssignableFrom( currentClass ) )
				{// Set up a primitive handler as an alternate to this one.
					setAltHandler( new PrimitiveContentsHandler( currentClass ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: a Boolean
//  jbm end

				// Can't do this class
				else
				{
					throw new org.xml.sax.SAXException( "Can't parse class " + currentClass );
				}// end else: can't do object
			}// end if: an "object" element

			// Else is it a "primitive"?
			else if ( PRIMITIVE_C.equals( localName ) || PRIMITIVE.equals( localName ) )
			{
				// Get attributes
				String strType = null;
				String attUri = NAMESPACE;

				String attLocalName = TYPE_C;
				strType = atts.getValue( attUri, attLocalName );
				if ( strType == null )
				{
					strType = atts.getValue( attUri, TYPE );
				}

				// Make sure the type was specified, as required
				if ( strType == null )
				{
					throw new org.xml.sax.SAXException( "No type specified for primitive " + localName );
				}// end if: no currentClass

				// A char
				if ( CHAR_C.equals( strType ) || CHAR.equals( strType ) )
				{// Set up a primitive handler as an alternate to this one.
					setAltHandler( new PrimitiveContentsHandler( java.lang.Character.class ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: char

				// A short
				else if ( SHORT_C.equals( strType ) || SHORT.equals( strType ) )
				{// Set up a primitive handler as an alternate to this one.
					setAltHandler( new PrimitiveContentsHandler( java.lang.Short.class ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: short

				// An int
				else if ( INT_C.equals( strType ) || INT.equals( strType ) )
				{// Set up a primitive handler as an alternate to this one.
					setAltHandler( new PrimitiveContentsHandler( java.lang.Integer.class ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: int

				// A long
				else if ( LONG_C.equals( strType ) || LONG.equals( strType ) )
				{// Set up a primitive handler as an alternate to this one.
					setAltHandler( new PrimitiveContentsHandler( java.lang.Long.class ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: long

				// A float
				else if ( FLOAT_C.equals( strType ) || FLOAT.equals( strType ) )
				{// Set up a primitive handler as an alternate to this one.
					setAltHandler( new PrimitiveContentsHandler( java.lang.Float.class ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: float

				// A double
				else if ( DOUBLE_C.equals( strType ) || DOUBLE.equals( strType ) )
				{// Set up a primitive handler as an alternate to this one.
					setAltHandler( new PrimitiveContentsHandler( java.lang.Double.class ) );
					resetAltHandlerElementStack().push( localName );
				}// end if: double

				// Can't do this primitive type
				else
				{
					throw new org.xml.sax.SAXException( "Invalid primitive type: " + strType );
				}// end else: can't do this primitive (invalid primitive)
			}// end if: a "primitive" element

			// Else is it a "primitive-array"?
			else if ( PRIMITIVE_ARRAY_C.equals( localName ) || PRIMITIVE_ARRAY.equals( localName ) )
			{
				// Get type and length
				String strType = null;
				String strLength = null;
				String attUri = NAMESPACE;

				String attLocalName = TYPE_C;
				strType = atts.getValue( attUri, attLocalName );
				if ( strType == null )
				{
					strType = atts.getValue( attUri, TYPE );
				}
				if ( strType == null )
				{
					throw new org.xml.sax.SAXException( "Primitive type not declared." );
				}

				attLocalName = LENGTH_C;
				strLength = atts.getValue( attUri, attLocalName );
				if ( strLength == null )
				{
					strLength = atts.getValue( attUri, LENGTH );
				}
				if ( strLength == null )
				{
					throw new org.xml.sax.SAXException( "Array length not declared." );
				}
				int length = -1;
				try
				{
					length = new Integer( strLength ).intValue();
				}// end try
				catch ( Exception e )
				{
				}
				finally
				{
					if ( length < 0 )
					{
						throw new org.xml.sax.SAXException( "Invalid array length: " + length );
					}
				}// end finally

				// Set up a primitive array handler as an alternate to this one.
				setAltHandler( new PrimitiveArrayContentsHandler( strType, length ) );
				resetAltHandlerElementStack().push( localName );
			}// end else if: primitive-array

			// Else is it an "object-array"?
			else if ( OBJECT_ARRAY_C.equals( localName ) || OBJECT_ARRAY.equals( localName ) )
			{
				// Find out how long the array is.
				String strLength = null;
				String attUri = NAMESPACE;

				strLength = atts.getValue( attUri, LENGTH_C );
				if ( strLength == null )
				{
					strLength = atts.getValue( attUri, LENGTH );
				}

				// Make sure the class was specified, as required
				if ( strLength == null )
				{
					throw new org.xml.sax.SAXException( "No length specified for " + localName );
				}// end if: no length
				final int length = new Integer( strLength ).intValue();

				// Get the class type
				String strCurrentClass = null;
				strCurrentClass = atts.getValue( attUri, CLASS_C );

				// If null, try the long name
				if ( strCurrentClass == null )
				{
					strCurrentClass = atts.getValue( attUri, CLASS );
				}

				// Make sure the class was specified, as required
				if ( strCurrentClass == null )
				{
					throw new org.xml.sax.SAXException( "No class specified for " + localName );
				}// end if: no currentClass

				// Get actual Class object
				Class currentClass = null;
				try
				{
					currentClass = Class.forName( strCurrentClass );
				}// end try
				catch ( ClassNotFoundException e )
				{
					e.printStackTrace();
					throw new org.xml.sax.SAXException( strCurrentClass + " not found." );
				}// end catch

				// Create the object array
				Object[] objArr = null;
				try
				{
					objArr = (Object[]) java.lang.reflect.Array.newInstance( currentClass, length );
				}
				catch ( NegativeArraySizeException e )
				{
					throw new org.xml.sax.SAXException( e.getMessage() );
				}// end catch

				// Set up an object array handler as an alternate to this one.
				setAltHandler( new ObjectArrayContentsHandler( objArr ) );
				resetAltHandlerElementStack().push( localName );

			}// end else if: object-array
                        
			// Else is it an "array-array"?
			else if ( ARRAY_ARRAY_C.equals( localName ) || ARRAY_ARRAY.equals( localName ) )
			{
				// Find out how long the array is.
				String strLength = null;
				String attUri = NAMESPACE;

				strLength = atts.getValue( attUri, LENGTH_C );
				if ( strLength == null )
				{
					strLength = atts.getValue( attUri, LENGTH );
				}

				// Make sure the length was specified, as required
				if ( strLength == null )
				{
					throw new org.xml.sax.SAXException( "No length specified for " + localName );
				}// end if: no length
				final int length = new Integer( strLength ).intValue();

				// Create the array array
				Object[] arrArr = new Object[length];

				// Set up an object array handler as an alternate to this one.
				setAltHandler( new ArrayArrayContentsHandler( arrArr ) );
				resetAltHandlerElementStack().push( localName );

			}// end else if: array-array

			// Was it a 'null' element?
			else if ( NULL_C.equals( localName ) || NULL.equals( localName ) )
			{// Set up an object handler which will return null when
				// nothing is actually encountered.
				setAltHandler( new ObjectHandler() );
				resetAltHandlerElementStack().push( localName );
			}// end else if: null element

			// Else it was our namespace, but not our element
			else
			{
				throw new org.xml.sax.SAXException(
					"The element " + localName + " is not a valid element name for the " +
					NAMESPACE + " namespace" );
			}// end else: some other element
		}// end else if: it was our namespace

		// Else found some other namespace
		// Ignore it by ignoring all SAX2 events until the
		// close of the element.
		else
		{
			setAltHandler( new org.xml.sax.helpers.DefaultHandler() );
			resetAltHandlerElementStack().push( localName );
			getAltHandler().startElement( namespaceURI, localName, qName, atts );
		}// end else: some other element
	}// end startElement



	/**
	 *  Description of the Method
	 *
	 * @param  namespaceURI                  Description of Parameter
	 * @param  localName                     Description of Parameter
	 * @param  qName                         Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since
	 */
	public void endElement( final String namespaceURI, final String localName, final String qName )
		throws org.xml.sax.SAXException
	{
		// Are we the primary handler?
		if ( getAltHandler() != null )
		{
			getAltHandlerElementStack().pop();
			if ( getAltHandlerElementStack().isEmpty() )
			{
				// Get the object
				if (
				/*
				 *  (getObject() == null) ||
				 */
					!( getObject() instanceof Xmlizable ) )
				{
					if ( getAltHandler() instanceof ObjectHandler )
					{
						setObject( ( (ObjectHandler) getAltHandler() ).getObject() );
					}// end if: instance of object handler
					else
					{
						throw new org.xml.sax.SAXException( "Unknown handler type: " + getAltHandler() );
					}// end else: not instance of object handler
				}// end if: not xmlizable
				else
				{
					// Do nothing.
				}// end else: xmlizable

				setAltHandlerElementStack( null );
				setAltHandler( null );
			}// end if: stack empty
			else
			{
				getAltHandler().endElement( namespaceURI, localName, qName );
			}// end else: stack not empty
		}// end if: altHandler not null
		else
		{
			throw new org.xml.sax.SAXException( "Element " + localName + " ended without a handler" );
		}// end else: alt handler null

	}// end endElement


	/**
	 *  Default behavior is to pass the event on to the alternate handler, if there
	 *  is one.
	 *
	 * @param  values                        Description of Parameter
	 * @param  offset                        Description of Parameter
	 * @param  length                        Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since
	 */
	public void characters( final char[] values, final int offset, final int length )
		throws org.xml.sax.SAXException
	{
		final org.xml.sax.ContentHandler altHandler = getAltHandler();

		if ( altHandler != null )
		{
			altHandler.characters( values, offset, length );
		}// end if: another handler has taken over for a while

	}// end startElement

}

