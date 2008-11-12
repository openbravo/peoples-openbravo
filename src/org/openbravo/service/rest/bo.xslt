<xsl:stylesheet version='1.0'
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:ob="http://www.openbravo.com">
	<!--
		Note if the namespace of openbravo changes then the namespace
		declaration above has to be changed
	--> 
	<xsl:template match="ob:OpenBravo">
		<xsl:for-each select="*">
			<xsl:call-template name="handleEntity" />
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="OpenBravo">
		<xsl:for-each select="*">
			<xsl:call-template name="handleEntity" />
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="handleEntity">
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
						<xsl:choose>
							<xsl:when test="count(*)>0">
								<xsl:call-template name="handleManyField" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="handleField" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:template name="handleField">
		<tr>
			<td width="200" style="background-color: lightgreen;vertical-align: top;">
				<xsl:value-of select="name(.)" />
			</td>
			<td style="padding-left: 10px">
				<xsl:choose>
					<xsl:when test="@id">
						<xsl:call-template name="handleReference" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates />
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</tr>
	</xsl:template>
	<xsl:template name="handleManyField">
		<tr>
			<td width="200" style="background-color: lightgreen;vertical-align: top;">
				<xsl:value-of select="name(.)" />
			</td>
			<td style="padding-left: 10px">
				<xsl:for-each select="*">
					<h3>
						<xsl:value-of select="name(.)" />
						(
						<xsl:value-of select="@identifier" />
						-
						<xsl:value-of select="@id" />
						)
					</h3>
					<table>
						<xsl:for-each select="*">
							<xsl:choose>
								<xsl:when test="count(*)>0">
									<xsl:call-template name="handleManyField" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="handleField" />
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</table>
				</xsl:for-each>
			</td>
		</tr>
	</xsl:template>
	<xsl:template name="handleReference">
		<xsl:variable name="href">
			<xsl:value-of select="@entity-name" />
			/
			<xsl:value-of select="@id" />
		</xsl:variable>
		<a href="../{$href}?template=bo.xslt">
			<xsl:value-of select="@identifier" />
			(
			<xsl:value-of select="@id" />
			)
		</a>
		<a href="../{$href}"> (xml)</a>
	</xsl:template>
</xsl:stylesheet>