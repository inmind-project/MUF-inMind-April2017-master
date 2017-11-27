<!--  

   PunctuationGenerator.xsl
     - XSL-based generator for the BEAT gesture toolkit

   BEAT is Copyright(C) 2000-2001 by the MIT Media Laboratory.  
   All Rights Reserved.

   Developed by Hannes Vilhjalmsson, Timothy Bickmore, Yang Gao and Justine 
   Cassell at the Media Laboratory, MIT, Cambridge, Massachusetts, with 
   support from France Telecom, AT&T and the other generous sponsors of the 
   MIT Media Lab.

   For use by academic research labs, only with prior approval of Professor
   Justine Cassell, MIT Media Lab.

   This distribution is approved by Walter Bender, Director of the Media
   Laboratory, MIT.

   Permission to use, copy, or modify this software for educational and 
   research purposes only and without fee is hereby granted, provided  
   that this copyright notice and the original authors' names appear on all 
   copies and supporting documentation. If individual files are separated 
   from this distribution directory structure, this copyright notice must be 
   included. For any other uses of this software in original or modified form, 
   including but not limited to distribution in whole or in part, specific 
   prior permission must be obtained from MIT.  These programs shall not be 
   used, rewritten, or adapted as the basis of a commercial software or 
   hardware product without first obtaining appropriate licenses from MIT. 
   MIT makes no representation about the suitability of this software for 
   any purpose. It is provided "as is" without express or implied warranty.
-->


<xsl:transform version="1.0" 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Create EYEBROWS to enclose the contents of any CLAUSE element that
contains "!" or "?" as punctuation. -->
<xsl:template match="CLAUSE[.//@SYN='PE' or .//@SYN='PQ']" priority="12">
  <EYEBROWS>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </EYEBROWS>
</xsl:template>

<!-- DEFAULT RULE: Copy every input node to output unless overridden by above rules. -->
<xsl:template match="@*|node()">
<xsl:copy>
 <xsl:apply-templates select="@*|node()"/>
 </xsl:copy>
</xsl:template>

</xsl:transform>



