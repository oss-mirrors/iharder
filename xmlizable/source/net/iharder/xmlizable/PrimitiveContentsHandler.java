package net.iharder.xmlizable;

/**
 *  <p>
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
 * @version    1.2
 */
public class PrimitiveContentsHandler extends ObjectHandler
{

	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	private Class primitiveClass;


	/**
	 *  Creates a <tt>PrimitiveContentsHandler</tt> without specifying the type of
	 *  primitive to expect. You must call {@link #setPrimitiveClass
	 *  setPrimitiveClass(...)} before this class receives an event.
	 *
	 * @since    1.2
	 */
	public PrimitiveContentsHandler()
	{
		super();
	}// end constructor


	/**
	 *  Creates a <tt>PrimitiveContentsHandler</tt> and specifies the type of
	 *  primitive to expect.
	 *
	 * @param  primitiveClass  The wrapper class for the expected primitive type
	 * @since                  1.2
	 */
	public PrimitiveContentsHandler( Class primitiveClass )
	{
		super();
		setPrimitiveClass( primitiveClass );
	}// end constructor


	/**
	 *  Sets the wrapper class to be used with the expected primitive. For example,
	 *  if you expect an <tt>int</tt> , then you would call <code>setPrimitiveClass( java.lang.Integer.class )</code>
	 *  . Don't forget the ".class" at the end.
	 *
	 * @param  primitiveClass  The wrapper class for the expected primitive type
	 * @since                  1.2
	 */
	public void setPrimitiveClass( Class primitiveClass )
	{
// jbm		if ( !java.lang.Number.class.isAssignableFrom( primitiveClass )
// jbm			 || !java.lang.Character.class.isAssignableFrom( primitiveClass ) )
/* jbm
*  change the conditional connectors from "or" to "and" because of logic bug.
*  the problem was because (!A || !B) was used instead of (!A && !B)  in other words, when
*  primitiveClass was not a Number (!A above) then no matter what B is, the total condition is True.
*
* jbm and to add Boolean support
*/
		if ( !java.lang.Number.class.isAssignableFrom( primitiveClass )
			 && !java.lang.Character.class.isAssignableFrom( primitiveClass )
             && !java.lang.Boolean.class.isAssignableFrom(primitiveClass))
		{
			throw new IllegalArgumentException( "Not of type Number, Character or Boolean: " + primitiveClass );
		}

		this.primitiveClass = primitiveClass;
	}// end setNumberClass


	/**
	 *  Returns the wrapper class to be used with the expected primitive.
	 *
	 * @return    The wrapper class for the expected primitive type
	 * @since     1.2
	 */
	public Class getPrimitiveClass()
	{
		return primitiveClass;
	}// end primitiveClass


	/**
	 *  If the primitive was a number, this will return an appropriate {@link
	 *  java.lang.Number} object such as an {@link java.lang.Integer}. Otherwise a
	 *  <tt>null</tt> is returned.
	 *
	 * @return    The primitive wrapped in an appropriate object
	 * @since     1.2
	 */
	public Number getNumber()
	{
		Object obj = getObject();

		if ( obj == null )
		{
			return null;
		}

		if ( !( obj instanceof Number ) )
		{
			return null;
		}

		return (Number) obj;
	}// end getNumber


	/**
	 *  If the primitive was a character, this will return an appropriate {@link
	 *  java.lang.Character} object. Otherwise a <tt>null</tt> is returned.
	 *
	 * @return    The primitive wrapped in an appropriate object
	 * @since     1.2
	 */
	public Character getCharacter()
	{
		Object obj = getObject();

		if ( obj == null )
		{
			return null;
		}

		if ( !( obj instanceof Character ) )
		{
			return null;
		}

		return (Character) obj;
	}// end getCharacter




	/**
	 *  Returns the primitive as a <tt>char</tt> if it was a <tt>char</tt> to begin
	 *  with, or the zero value if there was an error or the primitive was a
	 *  numeric primitive.
	 *
	 * @return    primitive as a <tt>char</tt>
	 * @since     1.2
	 */
	public char getChar()
	{
		Character c = getCharacter();
		return c == null ? 0 : c.charValue();
	}// end getChar




	/**
	 *  Returns <tt>short</tt> value of the primitive, if it was a primitive number
	 *  type to begin with, or zero if there was an error or the primitive was a
	 *  <tt>char</tt> .
	 *
	 * @return    <tt>short</tt> value of a numeric primitive.
	 * @since     1.2
	 */
	public short getShort()
	{
		Number num = getNumber();
		return num == null ? 0 : num.shortValue();
	}// end getShort



	/**
	 *  Returns <tt>int</tt> value of the primitive, if it was a primitive number
	 *  type to begin with, or zero if there was an error or the primitive was a
	 *  <tt>char</tt> .
	 *
	 * @return    <tt>int</tt> value of a numeric primitive.
	 * @since     1.2
	 */
	public int getInt()
	{
		Number num = getNumber();
		return num == null ? 0 : num.intValue();
	}// end getInt



	/**
	 *  Returns <tt>long</tt> value of the primitive, if it was a primitive number
	 *  type to begin with, or zero if there was an error or the primitive was a
	 *  <tt>char</tt> .
	 *
	 * @return    <tt>long</tt> value of a numeric primitive.
	 * @since     1.2
	 */
	public long getLong()
	{
		Number num = getNumber();
		return num == null ? 0 : num.longValue();
	}// end getLong



	/**
	 *  Returns <tt>float</tt> value of the primitive, if it was a primitive number
	 *  type to begin with, or zero if there was an error or the primitive was a
	 *  <tt>char</tt> .
	 *
	 * @return    <tt>float</tt> value of a numeric primitive.
	 * @since     1.2
	 */
	public float getFloat()
	{
		Number num = getNumber();
		return num == null ? 0 : num.floatValue();
	}// end getFloat



	/**
	 *  Returns <tt>double</tt> value of the primitive, if it was a primitive
	 *  number type to begin with, or zero if there was an error or the primitive
	 *  was a <tt>char</tt> .
	 *
	 * @return    <tt>double</tt> value of a numeric primitive.
	 * @since     1.2
	 */
	public double getDouble()
	{
		Number num = getNumber();
		return num == null ? 0 : num.doubleValue();
	}// end getDouble



	/**
	 *  Captures the SAX2 event with the characters between the <tt>
	 *  &lt;primitive&gt;...&lt;/primitive&gt;</tt> tags.
	 *
	 * @param  values                        The char array
	 * @param  offset                        Description of Parameter
	 * @param  length                        Description of Parameter
	 * @exception  org.xml.sax.SAXException  Description of Exception
	 * @since                                1.2
	 * @offset                               The offset to start at
	 * @length                               The length of characters to read
	 */
	public void characters( final char[] values, final int offset, final int length )
		throws org.xml.sax.SAXException
	{
		final String s = new String( values, offset, length );

		// Try to get a constructor that accepts a string as its input,
		// or a char if it's a Character
		final Class c = getPrimitiveClass();
		if ( java.lang.Number.class.isAssignableFrom( c ) )
		{
			try
			{
				final java.lang.reflect.Constructor con = c.getConstructor( new Class[]{java.lang.String.class} );
				setObject( con.newInstance( new Object[]{s} ) );
			}// end try
			catch ( NoSuchMethodException e )
			{
				throw new org.xml.sax.SAXException( e.getMessage() );
			}// end catch
			catch ( java.lang.reflect.InvocationTargetException e )
			{
				throw new org.xml.sax.SAXException( e.getMessage() );
			}// end catch
			catch ( IllegalAccessException e )
			{
				throw new org.xml.sax.SAXException( e.getMessage() );
			}// end catch
			catch ( InstantiationException e )
			{
				throw new org.xml.sax.SAXException( e.getMessage() );
			}// end catch
		}// end if: number

		// Else a char
		else if ( java.lang.Character.class.isAssignableFrom( c ) )
		{
			try
			{
				setObject( new Character( s.charAt( 0 ) ) );
			}// end try
			catch ( IndexOutOfBoundsException e )
			{
				throw new org.xml.sax.SAXException( e.getMessage() );
			}// end catch

		}// end if: character

//  jbm add support for boolean as a primitive
		// Else a boolean
		else if ( java.lang.Boolean.class.isAssignableFrom( c ) )
		{
          setObject( new java.lang.Boolean(s));
		}// end if: boolean
//  jbm

	}// end characters

}

