xquery version "1.0";

declare namespace ns1 = "urn:nci:concept-entry";

<ns1:conceptEntryList>
	{
	for $dataEntry in /ns1:conceptEntryList/ns1:conceptEntry 
	return 
		if( $dataEntry[ns1:CUI and (ns1:CUI != '')] ) then ($dataEntry)
		else
			<ns1:conceptEntry>
				<ns1:Concept>
					{
					data($dataEntry/ns1:Concept)
					}
				</ns1:Concept>
				<ns1:CUI>
					{
					tokenize(replace($dataEntry/ns1:ConcantenationValuesOnly,"[,]+",":"),"[\(][\s]*[0-9a-zA-Z\s\-]*[\s]*[\)]")
					}
				</ns1:CUI>
				<ns1:ConcantenationFormula>
					{
					data($dataEntry/ns1:ConcanenationFormula)
					}
				</ns1:ConcantenationFormula>
				<ns1:Definition>
					{
					data($dataEntry/ns1:Definition)
					}
				</ns1:Definition>
				<ns1:Note>
					{
					data($dataEntry/ns1:Note)
					}
				</ns1:Note>
			</ns1:conceptEntry>
	}
</ns1:conceptEntryList>