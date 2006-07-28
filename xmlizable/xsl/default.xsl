<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0" >


  <!--
    This XSL stylesheet is a simple HTML skeleton and imports the main XSL stylesheets:
        o identity.xsl
        o object.xsl
        o array.xsl
        o map.xsl
        o collection.xsl


 
    I am placing this code in the Public Domain. Do with it as you will.
    This software comes with no guarantees or warranties but with
    plenty of well-wishing instead!
    Please visit <a href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
    periodically to check for updates or to contribute improvements.

    Author: Robert Harder, rharder@usa.net

  -->




<!-- 
    Import XSL templates from most generic to most specific. 
  -->
<xsl:import href="identity.xsl" />
<xsl:import href="object.xsl" />
<xsl:import href="array.xsl" />
<xsl:import href="map.xsl" />
<xsl:import href="collection.xsl" />



<!-- 
    Specify HTML output 
  -->
<xsl:output method="html"
            indent="yes"
            encoding="iso-8859-1" />



  <xsl:template match="/">
    <html>
      <head>
        <title><xsl:value-of select="name(/*)"/></title>
        <style type="text/css">
          <xsl:comment>
            <![CDATA[

* { font-family: Arial, sans-serif }
li { padding: 0.5ex; }
td, th { font-size: smaller; font-family: Verdana; padding: 1ex; vertical-align: top; text-align: left; }
caption { font-style: italic; font-family: Times, serif; }

/* For alternating collection rows */
.coll_0  { background-color: #FFFFFF; }
.coll_1 { background-color: #FAFAFA; }
.obj_arr_0 { background-color: #FFFFFF; }
.obj_arr_1 { background-color: #FAFAFA; }

            ]]>
          </xsl:comment>
        </style>
      </head>
      <body>
        <xsl:apply-templates />
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>