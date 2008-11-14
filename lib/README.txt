Overview of libraries and their dependencies in Openbravo

TODO: it would be best to also list download location and license information. 

>>> Jars in lib/build <<<

>> The following jars are used for image sprites (for more information see:
http://wiki.openbravo.com/wiki/Projects/CSS_Sprites):
smartsprites-alpha-2.1.jar
It depends on:
commons-io-1.4.jar
commons-lang-2.4.jar
commons-math-1.2.jar
google-collections.jar

>> Unit testing uses the following jar (in lib/build):
junit.jar

>> Used to prevent compile errors during ant javac
servlet-api.jar

>>> Jars in lib/runtime <<<

>> The hibernate functionality depends on the following jar files:
antlr-2.7.6.jar
antlr-runtime-3.0.jar
asm.jar
cglib-2.1.3.jar
commons-collections.jar
commons-logging-1.1.jar
commons-beanutils-1.7.jar
commons-dbcp.jar
commons-pool.jar
dom4j-1.6.1.jar
ehcache-1.2.3.jar
hibernate3.jar
jta.jar

>> Process Scheduling uses the following libraries:
quartz-1.6.2.jar is used for process scheduling.
jettison-1.0.1.jar and xstream-1.3.jar are used for JSON support.

>> Generating business object entity classes (in src-gen) requires the following jars:
org.eclipse.emf.common_2.4.0.v200806091234.jar
org.eclipse.emf.ecore_2.4.0.v200806091234.jar
org.eclipse.emf.ecore.xmi_2.4.0.v200806091234.jar
org.eclipse.equinox.common_3.4.0.v20080421-2006.jar
org.eclipse.text_3.4.0.v20080605-1800.jar
org.openarchitectureware.core.emftools_4.3.1.20080910-1400PRD.jar
org.openarchitectureware.core.expressions_4.3.1.20080910-1400PRD.jar
org.openarchitectureware.core.workflow_4.3.1.20080910-1400PRD.jar
org.openarchitectureware.core.xpand2_4.3.1.20080910-1400PRD.jar
hybridlabs-beautifier-1.1.9.jar
jalopy-1.5-rc3p1.jar
xml-apis.jar

>> SOAP webservices require the following jars:
axis.jar
axis-ant.jar
wsdl4j-1.5.1.jar

>> Jasper Reports
jasperreports-3.0.1.jar
iText-2.1.3.jar
jfreechart-1.0.10.jar
jcommon-1.0.13.jar

>> Common libraries (used in various locations)
log4j-1.2.8.jar
postgresql-jdbc3-8.2.jar
ant-1.6.5.jar
mail.jar
ojdbc14.jar (oracle driver)
commons-fileupload.jar

>> Html parser, used by xml-engine
nekohtml.jar

>> Authentication
lam-client.jar

>> Candidate for removal:
comm.jar
avalon-framework-4.1.5.jar


>>> Not found use for these:
logkit-1.0.1.jar
jxl-2.6.jar
ant-launcher.jar
xercesImpl.jar
activation.jar
ant-nodeps.jar
barcode4j-fop-ext-0.20.5-complete.jar
batik.jar
commons-codec-1.3.jar
commons-digester-1.8.jar
commons-discovery-0.2.jar
fop.jar
jaxrpc.jar
jdtcore-3.1.0.jar
org.apache.commons.cli_1.0.0.jar
renderFoRmi.jar
saaj.jar
ws-commons-util-1.0.1.jar
xmlrpc-client-3.1.jar
xmlrpc-common-3.1.jar
