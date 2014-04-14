<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml"/>
  <xsl:template match="/shared-file-export">
	<rss version="2.0" 
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
		xmlns:admin="http://webns.net/mvcb/"
		xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		xmlns:content="http://purl.org/rss/1.0/modules/content/">

	<channel>
		<title>Exported magnet-list</title>
<!--
		<link>is to be set manually, the link to this list</link>
-->
		<description>Exported magnet-links</description>
		<dc:language>en-en</dc:language>
		<dc:creator>Someone who somehow got to find these files</dc:creator>
<!--
		<dc:rights></dc:rights>
-->
<!--
		<dc:date>2004-08-10T15:48:54+02:00</dc:date>
-->
		<admin:generatorAgent rdf:resource="http://phex.org" />
<!--
		<sy:updatePeriod>hourly</sy:updatePeriod>
		<sy:updateFrequency>1</sy:updateFrequency>
-->
		<sy:updateBase>2000-01-01T12:00+00:00</sy:updateBase>
		
	<xsl:for-each select="shared-file-list/shared-file">
	<item>
		<title><xsl:apply-templates select="name"/></title>
		<link><xsl:apply-templates select="magnet-url"/></link>
		<comments>exported magnet</comments>
		<description><![CDATA[ File-name: ]]><xsl:apply-templates select="name"/> <![CDATA[ <br />File-size: ]]><xsl:apply-templates select="file-size"/> <![CDATA[ <br />Magnet-link: ]]><xsl:apply-templates select="magnet-url"/></description>
		<magnet><xsl:apply-templates select="magnet-url"/><xsl:if test="/shared-file-export/export-options/option[@name='UseMagnetURLWithFreeBase'] = 'true'">&amp;as=http://www.freebase.be/g2/dlcount.php?sha1=<xsl:apply-templates select="sha1"/></xsl:if></magnet>
    </item>
    </xsl:for-each>
	</channel>
	</rss>
  </xsl:template>  
</xsl:stylesheet>
