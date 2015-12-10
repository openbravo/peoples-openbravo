#!/bin/bash

# VARIABLES
LINE="=========================================================="
URLREPO=""

# Check if we are in Openbravo root
CHECKDIR=$(ls -1 | grep modules)
if [ "$CHECKDIR" != "modules" ]
then
	echo "This script should be executed inside Openbravo root directory"
	exit 1
fi

echo "Dependecies"
for i in org.openbravo.utility.multiplebpselector org.openbravo.financial.cashflowforecast org.openbravo.agingbalance
do

	if [ -e "modules/$i/" ]
	then
	echo "$i exists"
	else
	echo "$i clone"
	cd modules/
	hg clone "https://code.openbravo.com/erp/mods/$i"
	cd ..
	fi

done

for i in org.openbravo.mobile.core org.openbravo.retail.config org.openbravo.retail.discounts org.openbravo.retail.pack org.openbravo.retail.poshwmanager org.openbravo.retail.posterminal org.openbravo.retail.returns org.openbravo.retail.sampledata

do
	if [ -e "modules/$i/" ]
	then
		echo $LINE
		echo "*** $i -- exists - pull and update"
		echo $LINE

		cd modules/$i/
		hg pull --rebase
		if [[ -z $1 ]]
		then
		echo "*** update to tip"
		hg update
		else
		echo "*** update to $1"
		hg update -r $1
		fi
		cd ..
		cd ..
	else
		echo $LINE
		echo "*** $i -- does not exists - clone"
		echo $LINE
		cd modules/
		hg clone "https://code.openbravo.com/erp/pmods/$i"
		cd $i/
		if [[ -z $1 ]]
		then
		echo "*** update to tip"
		hg update
		else
		echo "*** update to $1"
		hg update -r $1
		fi
		cd ..
		cd ..
	fi
done