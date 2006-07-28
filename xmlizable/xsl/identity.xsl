<?xml version="1.0" encoding="iso-8859-1" ?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0">


  <!--
 
    Copies nodes and attributes verbatim.

 
    I am placing this code in the Public Domain. Do with it as you will.
    This software comes with no guarantees or warranties but with
    plenty of well-wishing instead!
    Please visit <a href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
    periodically to check for updates or to contribute improvements.

    Author: Robert Harder, rharder@usa.net

  -->


  <xsl:template match="* | @* | text() | comment() | processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="* | @* | text() | comment() | processing-instruction()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

