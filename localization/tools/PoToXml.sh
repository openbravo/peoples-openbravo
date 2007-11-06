#/bin/sh
# 
# This script file generates a batch of XML files into PO
# 
# See http://wiki.openbravo.com/wiki/index.php/Translating_Openbravo
# for details
#
 
echo Generating final XML from PO
cd src_xml
for n in *.xml
do
echo Processing $n 
xml2po -p ../po/$n.po $n > ../out_xml/$n
done
cd ..
