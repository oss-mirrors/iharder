<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:h="http://iharder.net/xmlizable"
    version="1.0" >

<!--
    This XSL stylesheet is for displaying some common 
    java.util.Collection's that were XML-ized
    using the net.iharder.xml utilities.
    The following collections are supported:
        o HashSet
        o TreeSet
        o ArrayList
        o LinkedList
        o Vector
        o Stack

    The entire collection is first encased in a 'div' element with a 'class'
    attribute equal to 'java_util_Collection', regardless of the actual type
    of collection used.

    An 'id' attribute is given as the collection's name (or blank if no name was passed) 
    followed by an underscore (_) and a unique identifier given by the XSL processor's 
    generate-id(.) function.

    A 'ul' (unordered list) is then constructed with each object in the collection
    getting it's own 'li' element. Each 'li' element is given the attribute 'title'
    that equals the collection's name (or blank if no name was specified) followed
    by the position of the object within the collection, e.g., myList[5], starting with zero.

    When xsl:apply-templates is called on the objects within the collection, a 'name'
    parameter is passed that is equal to the enclosing 'li' element's 'title' attribute.



 
    I am placing this code in the Public Domain. Do with it as you will.
    This software comes with no guarantees or warranties but with
    plenty of well-wishing instead!
    Please visit <a href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
    periodically to check for updates or to contribute improvements.

    Author: Robert Harder, rharder@usa.net

  -->


  <!-- Match the specific document types that we intend to match -->
  <xsl:template match="
    h:object[@h:class='java.util.Stack'       or
             @h:class='java.util.Vector'      or
             @h:class='java.util.TreeSet'     or
             @h:class='java.util.HashSet'     or
             @h:class='java.util.ArrayList'   or 
             @h:class='java.util.LinkedList'] |
         h:o[@h:c='java.util.Stack'       or
             @h:c='java.util.Vector'      or
             @h:c='java.util.TreeSet'     or
             @h:c='java.util.HashSet'     or
             @h:c='java.util.ArrayList'   or 
             @h:c='java.util.LinkedList'] ">
    <xsl:param name="name" />

    <!-- Enclose entire collection in a 'div' and give it a distinctive id. -->
    <div class="java_util_Collection" 
         id="{normalize-space(name(.))}_{generate-id(.)}" >
        <ul>
          <xsl:for-each select="*">
            <li title="{$name}[{position()-1}]" 
                class="coll_{position() mod 2}">

              <xsl:apply-templates select=".">
                <xsl:with-param name="name" select="concat( normalize-space($name), '[', position()-1, ']' )" />
              </xsl:apply-templates>

            </li>
          </xsl:for-each>
        </ul>
    </div>
  </xsl:template>


</xsl:stylesheet>