<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>
  <xsl:template match="/shared-file-export">
    <html>
      <style type="text/css">
        <![CDATA[<!--
        BODY {
	        background-color : #e5e5e5;
          font-family : Verdana, Arial, Helvetica, sans-serif;
          font-size: 11px;
          color : #333333;
        }
        .bodyline {
	        background-color: #FFFFFF;
	        border: 1px solid #DD6900;
        }
        .cell {
	        background-color: #FFCF9B;
	        font-family : Verdana, Arial, Helvetica, sans-serif;
          font-size: 11px;
        }
        -->]]>
      </style>
      <body>
      <table border="0" width="100%" cellpadding="2" cellspacing="0">
      <tr valign="top"><td class="bodyline">
        <table width="100%" border="0" cellspacing="0" cellpadding="10">
        <tr valign="top" height="100%"><td valign="top">
        <b>Shared File Export</b>
        </td></tr></table>
        <table width="100%" border="0" cellspacing="0" cellpadding="10">
        <tr valign="top" height="100%"><td valign="top">
        <table class="bodyline" cellspacing="1" cellpadding="3">
          <tr>
            <td bgcolor="#DD6900"><b>File Name</b></td>
            <td bgcolor="#DD6900"><b>File Size</b></td>
            <td bgcolor="#DD6900"><b>Magnet</b></td>
          </tr>
      		<xsl:for-each select="shared-file-list/shared-file">
      		<tr>
      		  <td class="cell"><xsl:apply-templates select="name"/></td>
      		  <td class="cell" align="right"><xsl:apply-templates select="file-size"/></td>
      		  <td class="cell"><xsl:apply-templates select="magnet-url"/><xsl:if test="/shared-file-export/export-options/option[@name='UseMagnetURLWithFreeBase'] = 'true'">&amp;as=http://www.freebase.be/g2/dlcount.php?sha1=<xsl:apply-templates select="sha1"/></xsl:if></td>
      		</tr>
      		</xsl:for-each>
        </table>
        </td></tr></table>
        </td></tr>
        </table>
        <center><div>Copyright 2006 The Phex Team - License: GPL-2 or later.</div>
        <div><a href="http://phex.org">
          <img src="http://phex.kouk.de/img/phexbtn.gif" width="88" height="31" border="0" alt="Download Phex"/>
        </a>&#160;&#160;&#160;&#160;&#160;&#160;
        <a href="http://sf.net/projects/phex" target="_blank">
        <img src="http://sourceforge.net/sflogo.php?group_id=27021&amp;type=1" width="88" height="31" border="0" alt="SourceForge.net Logo"/>
        </a></div></center>
      </body>
    </html>
  </xsl:template>  
</xsl:stylesheet>
