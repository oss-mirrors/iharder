<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:h="http://iharder.net/xmlizable"
    version="1.0" >

  <!--
    This XSL stylesheet is for displaying java.util.Map's that were XML-ized
    using the net.iharder.xml utilities.
    The following maps, mostly from java.util, are supported:
        o HashMap
        o TreeMap
        o HashTable
        o Properties
        o WeakHashMap
        o java.security.Provider
        o javax.swing.UIDefaults
        o java.awt.RenderingHints
        o java.util.jar.Attributes


    Param           Default     Description
    =====           =======     ============
    key-legend      'key'       The heading for the first column, the column for the map's keys.
    value-legend    'value'     The heading for the second column, the column for the map's values..
    name                        The name of the object. It may be generated automatically by an
                                enclosing collection or map XSL transformation.
    caption                     The caption for the table.
    caption-align   'bottom'    The caption's alignment, according to the HTML 4.0 options for
                                the 'align' attribute of a table's 'caption' element
    border          '1'         The border for the table according to the HTML 4.0 options for
                                the 'border' attribute of a table.


    The 'table' element enclosing the map is given an 'id' attribute specified
    as the table's name (or blank if no name was passed) followed by an underscore (_)
    and a unique identifier given by the XSL processor's generate-id(.) function.

    The table's 'class' attribute is given as 'java_util_Map', regardless of the classname
    of the actual map used.

    Additionally, a 'title' attribute is given either as the map's name
    and classname or just the map's classname,
    if a name was not passed down from another stylesheet.

    Each row of the table corresponds to one key/value pair of the map.
    The 'td' element for the value is given the 'title' attribute
    of that value's key in the map.

    When xsl:apply-templates is called on the key/value pair's value,
    a 'name' parameter is passed with the value's key in the map.



 
    I am placing this code in the Public Domain. Do with it as you will.
    This software comes with no guarantees or warranties but with
    plenty of well-wishing instead!
    Please visit <a href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
    periodically to check for updates or to contribute improvements.

    Author: Robert Harder, rharder@usa.net

  -->

  <xsl:template match="
    h:object[@h:class='java.util.HashMap'       or 
             @h:class='java.util.TreeMap'       or
             @h:class='java.util.HashTable'     or
             @h:class='java.util.Properties'    or
             @h:class='java.util.WeakHashMap'   or
             @h:class='java.security.Provider'  or
             @h:class='javax.swing.UIDefaults'  or
             @h:class='java.awt.RenderingHints' or
             @h:class='java.util.jar.Attributes'] |
          h:o[@h:c='java.util.HashMap'      or 
             @h:c='java.util.TreeMap'       or
             @h:c='java.util.HashTable'     or
             @h:c='java.util.Properties'    or
             @h:c='java.util.WeakHashMap'   or
             @h:c='java.security.Provider'  or
             @h:c='javax.swing.UIDefaults'  or
             @h:c='java.awt.RenderingHints' or
             @h:c='java.util.jar.Attributes']">
    <xsl:param name="key-legend" select="'key'" />
    <xsl:param name="value-legend" select="'value'" />
    <xsl:param name="name" />
    <xsl:param name="caption" />
    <xsl:param name="caption-align" select="'bottom'" />
    <xsl:param name="border" select="'1'" />

      <table id="{normalize-space(name(.))}_{generate-id(.)}" 
             border="{$border}" 
             class="java_util_Map" >

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

        <!-- Optional caption element -->
        <xsl:choose>
          <xsl:when test="$caption">
            <caption>
              <xsl:attribute name="align">
                <xsl:value-of select="$caption-align" />
              </xsl:attribute>
              <xsl:value-of select="$caption" />
            </caption>
          </xsl:when>
        </xsl:choose>

        <!-- Table header with optional legends -->
        <thead>
          <th><xsl:value-of select="$key-legend" /></th>
          <th><xsl:value-of select="$value-legend" /></th>
        </thead>

        <!-- Table body -->
        <tbody>

          <xsl:choose>
            <!--
                When the map is empty.
              -->
            <xsl:when test="not(*)">
              <tr>
                <td colspan="2" style="text-align:center">
                  <xsl:text>Empty</xsl:text>
                </td>
              </tr>
            </xsl:when>

            <!--
                When there's something in the map.
              -->
            <xsl:otherwise>
              <xsl:apply-templates />
            </xsl:otherwise>
          </xsl:choose>
        </tbody>
      </table>
  </xsl:template>


  <!--
      Template for each map entry (key/value pair).
    -->
  <xsl:template match="
    h:object[@h:class='java.util.HashMap'       or 
             @h:class='java.util.TreeMap'       or
             @h:class='java.util.HashTable'     or
             @h:class='java.util.Properties'    or
             @h:class='java.util.WeakHashMap'   or
             @h:class='java.security.Provider'  or
             @h:class='javax.swing.UIDefaults'  or
             @h:class='java.awt.RenderingHints' or
             @h:class='java.util.jar.Attributes']/h:entry |
          h:o[@h:c='java.util.HashMap'      or 
             @h:c='java.util.TreeMap'       or
             @h:c='java.util.HashTable'     or
             @h:c='java.util.Properties'    or
             @h:c='java.util.WeakHashMap'   or
             @h:c='java.security.Provider'  or
             @h:c='javax.swing.UIDefaults'  or
             @h:c='java.awt.RenderingHints' or
             @h:c='java.util.jar.Attributes']/h:e">
    <tr>
      <td>
        <xsl:apply-templates select="h:key/* | h:k/*" />
      </td>
      <td title="{normalize-space(concat(h:key,h:k))}" >
        <xsl:apply-templates select="h:value/* | h:v/*" >
          <xsl:with-param name="name" select="concat(h:key,h:k)" />
        </xsl:apply-templates>
      </td>
    </tr>
  </xsl:template>



  <xsl:template mode="get"
    match="
    h:object[@h:class='java.util.HashMap'       or 
             @h:class='java.util.TreeMap'       or
             @h:class='java.util.HashTable'     or
             @h:class='java.util.Properties'    or
             @h:class='java.util.WeakHashMap'   or
             @h:class='java.security.Provider'  or
             @h:class='javax.swing.UIDefaults'  or
             @h:class='java.awt.RenderingHints' or
             @h:class='java.util.jar.Attributes']" >
    <xsl:param name="key" />
      <xsl:apply-templates select="h:entry/h:value/*[ normalize-space( ../../h:key ) = $key ]" />
  </xsl:template>



</xsl:stylesheet>