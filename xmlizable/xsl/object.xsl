<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:h="http://iharder.net/xmlizable"
    version="1.0" >

<!--
    This XSL stylesheet is for displaying 'object' tags,
    presumably that were created with the net.iharder.xmlizable utilities.

    A 'span' object is created with a 'class' attribute equal to the object's
    classname with underscores (_) in place of periods (.).

    Additionally, a 'title' attribute is given either as the object's name
    and classname or just the object's classname,
    if a name was not passed down from another stylesheet.

    xsl:apply-templates is then called on the contents, with a 'name' parameter
    being passed along, if a name was passed to the object to begin with.


 
    I am placing this code in the Public Domain. Do with it as you will.
    This software comes with no guarantees or warranties but with
    plenty of well-wishing instead!
    Please visit <a href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
    periodically to check for updates or to contribute improvements.

    Author: Robert Harder, rharder@usa.net

  -->

  <xsl:template match="h:object | h:o">
    <xsl:param name="name" />
    <span class="{translate( concat(@h:class,@h:c), '.', '_' )}" >

        <!-- 'title' attribute is in the form 'name: class'
             or just 'class' if no name is given. -->
        <xsl:attribute name="title">
          <xsl:choose>
            <xsl:when test="$name">
              <xsl:value-of select="concat( normalize-space($name), ' (', concat(@h:class,@h:c), ')' )" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="concat(@h:class,@h:c)" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>

      <xsl:choose>
        <xsl:when test="@h:encoding='base64' or @h:en='b64'">
          <xsl:text>Serialized object stored in Base64 notation: </xsl:text>
          <xsl:value-of select="concat( '[', substring(normalize-space(.),1,4), '...', substring(.,string-length(.)-4), ']' )" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates >
            <xsl:with-param name="name" select="$name" />
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </span>
  </xsl:template>

</xsl:stylesheet>