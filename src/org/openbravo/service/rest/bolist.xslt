<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
     
<xsl:template match="/">
<xsl:for-each select="*"> 
<html>
	<body> 
     <h1>OpenBravo Business Object List</h1>
     <table>
     <xsl:for-each select="*">
     	<xsl:call-template name="handleEntity"/>
     </xsl:for-each>
     </table>
	</body>
</html>
</xsl:for-each>
</xsl:template>

<xsl:template name="handleEntity">
     <xsl:variable name="href"><xsl:value-of select="name(.)"/>/<xsl:value-of select="@id"/></xsl:variable>
     <tr><td>
     <a href="{$href}?template=bo.xslt"><xsl:value-of select="@identifier"/> (<xsl:value-of select="@id"/>)</a>
     </td><td style="padding-left:20px">
     <a href="{$href}">xml</a>
     </td></tr>
</xsl:template>
</xsl:stylesheet> 