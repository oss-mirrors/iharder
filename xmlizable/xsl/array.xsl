<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:h="http://iharder.net/xmlizable"
    version="1.0" >

<!--
    This XSL stylesheet is for displaying 
    arrays of objects.

    The entire array is first encased in a 'div' element with a 'class'
    attribute equal to 'object_array', regardless of the actual type
    of object contained in the array.

    An 'id' attribute is given as the array's name (or blank if no name was passed) 
    followed by an underscore (_) and a unique identifier given by the XSL processor's 
    generate-id(.) function.

    A 'ul' (unordered list) is then constructed with each object in the array
    getting it's own 'li' element. Each 'li' element is given the attribute 'title'
    that equals the array's name (or blank if no name was specified) followed
    by the position of the object within the array, e.g., myList[5], starting with zero.

    When xsl:apply-templates is called on the objects within the array, a 'name'
    parameter is passed that is equal to the enclosing 'li' element's 'title' attribute.



 
    I am placing this code in the Public Domain. Do with it as you will.
    This software comes with no guarantees or warranties but with
    plenty of well-wishing instead!
    Please visit <a href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
    periodically to check for updates or to contribute improvements.

    Author: Robert Harder, rharder@usa.net

  -->


  <!-- Match the specific document types that we intend to match -->
  <xsl:template match="h:object-array | h:oa">
    <xsl:param name="name" />

    <!-- Enclose entire array in a 'div' and give it a distinctive id. -->
    <div class="object_array" 
         id="{normalize-space(name(.))}_{generate-id(.)}" >
        <ul>
          <xsl:for-each select="*">
            <li title="{$name}[{position()-1}]" 
                class="obj_arr_{(position()-1) mod 2}">

              <xsl:apply-templates select=".">
                <xsl:with-param name="name" select="concat( normalize-space($name), '[', position()-1, ']' )" />
              </xsl:apply-templates>

            </li>
          </xsl:for-each>
        </ul>
    </div>
  </xsl:template>


  <!-- Match the specific document types that we intend to match -->
  <xsl:template match="h:primitive-array | h:pa">
    <xsl:param name="name" />

    <!-- Enclose entire array in a 'div' and give it a distinctive id. -->
    <div class="primitive_array" 
         id="{normalize-space(name(.))}_{generate-id(.)}" >
        <xsl:value-of select="concat( @type, ' array:')" />
        <ul>
          <xsl:call-template name="comma-sep-to-list">
            <xsl:with-param name="list" select="." />
            <xsl:with-param name="name" select="concat(normalize-space($name), ' ', @type)" />
          </xsl:call-template>
        </ul>
    </div>
  </xsl:template>

  <!--
    Converts a comma-separated list of values to <li>tags</li>.
    -->
  <xsl:template name="comma-sep-to-list">
    <xsl:param name="name" />
    <xsl:param name="list" />
    <xsl:param name="num" select="'0'" />
    <xsl:variable name="normList" select="normalize-space( translate( $list, ',',' ' ) )" />

    <xsl:if test="$normList">
      <li title="{$name}[{$num}]" 
          class="prim_arr_{$num mod 2}"><xsl:value-of select="substring-before(concat($normList,' '), ' ')"/></li>
      <xsl:call-template name="comma-sep-to-list">
        <xsl:with-param name="name" select="$name" />
        <xsl:with-param name="list" select="substring-after($normList, ' ')" />
        <xsl:with-param name="num"  select="$num + 1" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>