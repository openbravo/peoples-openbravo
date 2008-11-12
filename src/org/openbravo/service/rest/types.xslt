<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
     
<xsl:template match="Types">
<html> 
	<body>
     <h1>Types List</h1>
     <table> 
     <xsl:apply-templates/>
     </table>
	</body>
</html>
</xsl:template>

<xsl:template match="Type">
     <xsl:variable name="href"><xsl:value-of select="@entityName"/></xsl:variable>
     <tr><td>
     <xsl:element name="a">
     	<xsl:attribute name="href"><xsl:value-of select="$href"/>?template=bolist.xslt</xsl:attribute>
     	<xsl:value-of select="$href"/>
     </xsl:element>
     </td><td style="padding-left:20px">
      <a href="dalws/{$href}">xml</a>
     </td></tr>
</xsl:template>
</xsl:stylesheet> 