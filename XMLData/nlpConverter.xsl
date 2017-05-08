<?xml version="1.0" encoding="UTF-8"?>
<?altova_samplexml C:\Documents and Settings\Superuser\Desktop\nlpDocument.xml?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/|*|@*|text()" priority="1">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="t" priority="2">
			<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="p" priority="2">
			<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="text" priority="2">
			<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="nlpDocument" priority="2">
		<UTTERANCE>
			<xsl:apply-templates/>
		</UTTERANCE>
	</xsl:template>

	<xsl:template match="chunk[@type='NP']" priority="2">
		<OBJECT>
			<xsl:apply-templates/>
		</OBJECT>
	</xsl:template>
	
	<xsl:template match="chunk[@type='VP']" priority="2">
		<ACTION>
			<xsl:apply-templates/>
		</ACTION>
	</xsl:template>	
	
	<xsl:template match="s" priority="2">
		<CLAUSE>
			<xsl:apply-templates/>
		</CLAUSE>
	</xsl:template>

</xsl:stylesheet>

