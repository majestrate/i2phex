<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml"/>
  <xsl:template match="/shared-file-export">
    <metalink version="3.0" xmlns="http://www.metalinker.org/"
        type="static" generator="Phex - http://phex.org" >
  
<!--
  Metalink, an Open Standard, makes downloading easier by
  bundling the various ways (FTP/HTTP/P2P) to get files into
  one format for easier downloads, as well as bundling
  checksums to automatically verify completed downloads.
  Currently the following applications support this file:

  - aria2 (Unix) [ http://aria2.sourceforge.net/ ]
  - GetRight 6 (Windows) [ http://www.getright.com/ ]
  - Speed Download (Mac) [ http://www.yazsoft.com/ ]
  - wxDownload Fast (All)[ http://dfast.sourceforge.net/ ]

  For further information please visit http://www.metalinker.org/.
  For OpenSource metalinks, generated with the precious help of
  RoEduNet Iasi, see http://download.packages.ro/metalinks/

-->

        <description>Phex Library</description>
        <identity>Phex Library</identity>

        <files>  
<xsl:for-each select="shared-file-list/shared-file">
            <file name="{name}">
            <verification>
                <hash type="sha1"><xsl:apply-templates select="sha1"/></hash>
            </verification>
            <size><xsl:apply-templates select="file-size"/></size>
            <resources>
                <url type="magnet" preference="100">
                    <xsl:apply-templates select="magnet-url"/><xsl:if test="/shared-file-export/export-options/option[@name='UseMagnetURLWithFreeBase'] = 'true'">&amp;as=http://www.freebase.be/g2/dlcount.php?sha1=<xsl:apply-templates select="sha1"/></xsl:if>
                </url>
                <xsl:if test="/shared-file-export/export-options/option[@name='UseMagnetURLWithXS'] = 'true'">
                <url type="http">
                    <xsl:value-of select="name2res-url"/>
                </url>
                <xsl:if test="/shared-file-export/export-options/option[@name='UseMagnetURLWithFreeBase'] = 'true'">
                <url type="http">
                    http://www.freebase.be/g2/dlcount.php?sha1=<xsl:apply-templates select="sha1"/>
                </url>
                </xsl:if>
                </xsl:if>
            </resources>
       </file>
</xsl:for-each>
    </files> 
</metalink>
  </xsl:template>  
</xsl:stylesheet>