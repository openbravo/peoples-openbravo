<xsl:stylesheet version='1.0'
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
	<xsl:template match="/">
		<xsl:for-each select="*">
			<html>
				<body>
					<h1>
						<xsl:value-of select="name(.)" />
						(
						<xsl:value-of select="@identifier" />
						-
						<xsl:value-of select="@id" />
						)
					</h1>
					<table>
						<xsl:for-each select="*">
							<xsl:call-template name="handleFields" />
						</xsl:for-each>
					</table>
				</body>
			</html>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="handleFields">
		<tr>
			<td width="200" style="background-color: lightgreen;">
				<xsl:value-of select="name(.)" />
			</td>
			<td style="padding-left: 10px">
				<xsl:choose>
					<xsl:when test="@id">
						<xsl:call-template name="handleReference"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates />
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</tr>
	</xsl:template>
	<xsl:template name="handleReference">
		<xsl:variable name="href">
			<xsl:value-of select="@entityName" />
			?id=
			<xsl:value-of select="@id" />
		</xsl:variable>
		<a href="{$href}&amp;template=bo.xslt">
			<xsl:value-of select="@identifier" />
			(<xsl:value-of select="@id" />)
		</a>
		<a href="{$href}"> (xml)</a>
	</xsl:template>
</xsl:stylesheet>