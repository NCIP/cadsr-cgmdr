java -cp saxon9.jar net.sf.saxon.Query -s:conceptExport.xml -q:transform.xquery -o:firstPassTransform.xml
java -jar saxon9.jar -s:firstPassTransform.xml -xsl:concept_transform.xsl -o:terminologyImport.rdf
