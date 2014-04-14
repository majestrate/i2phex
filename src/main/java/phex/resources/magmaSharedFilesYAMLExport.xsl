<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
 xmlns:encoder="xalan://java.net.URLEncoder">
<xsl:output method="text"/>
<xsl:template match="/shared-file-export" >#MAGMAv0.2 magnet:?mt=.&amp;dn=Magnetlist.magma
#With this List your Library opens its doors. 

list:<xsl:for-each select="shared-file-list/shared-file">
 
 - "magnet:?xt=<xsl:apply-templates select="urn"/>
   &amp;dn=<xsl:value-of select="name-urlenc"/><xsl:if test="/shared-file-export/export-options/option[@name='UseMagnetURLWithFreeBase'] = 'true'">
   &amp;as=http://www.freebase.be/g2/dlcount.php?sha1=<xsl:apply-templates select="sha1"/></xsl:if><xsl:if test="/shared-file-export/export-options/option[@name='UseMagnetURLWithXS'] = 'true'">
   &amp;xs=<xsl:value-of select="name2res-url"/></xsl:if>"
  file-name: <xsl:apply-templates select="name"/>
  file-size: <xsl:apply-templates select="file-size"/>
  urn: <xsl:apply-templates select="urn"/>
 </xsl:for-each>
    
# Phex: Copyright 2006 The Phex Team - License: GPL-2 or later.
# Link: http://phex.org
# Banner: http://phex.kouk.de/img/phexbtn.gif
# Developer-link: http://sf.net/projects/phex
# Developer-Logo: http://sourceforge.net/sflogo.php?group_id=27021&amp;type=1
</xsl:template>
</xsl:stylesheet>
