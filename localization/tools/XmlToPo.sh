#/bin/sh
# 
# This script file generates a batch of PO files into XML files
# 
# See http://wiki.openbravo.com/wiki/index.php/Translating_Openbravo
# for details
#

echo Generating POs from source XML
cd src_xml
for n in *.xml
do
	echo Processing $n 
	xml2po $n > ../po/$n.po
done
cd ..
