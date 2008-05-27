
function runInBackground( url, target )
{
	var response = confirm( "Info:  Reports that prompt for parameters are not supported with this feature."
		+ "\nRun in Background may generate content with incorrect results." );

	if ( response )
	{
		url = url + "&background=true";
		if ( target.toLowerCase().indexOf( 'new' ) >= 0 )
		{
			var targetWin = window.open( url );
		}
		else
		{
			window.location = url;
		}
	}
	return undefined;	// forces current page to remain unchanged when target=new
}

function getOptions( solution, path, filename, target, isFolder, furl, properties ) {

	var actions = "";
	if( filename.indexOf( '.url' ) == (filename.length - 4) ) {
		actions += "<a href=\""+furl+"\" target=\""+target+"\">Run</a>";
		return actions;
	}

	var actions = "";
	if( !isFolder ) {
		var url = "ViewAction?solution="+solution+"&path="+path+"&action=" + encodeSingleQuote( filename );
		actions += "<a href=\""+url+"\" target=\""+target+"\">Run</a>&nbsp;|&nbsp;";
		actions += "<a href='javascript:runInBackground(\"" + url + "\", \"" + target + "\");'>Background</a>";

		if ( isWaqrAction( filename ) )
		{
			var waqrUrl = getWaqrEditUrl( solution, path, filename, furl );
			actions +=  "&nbsp;|&nbsp;<a href=\""+waqrUrl+"\" target=\""+target+"\">Edit</a>";
			var filePath = getWaqrDeletePath( solution, path, filename );
			filePath = encodeSingleQuote( filePath );
			filename = encodeSingleQuote( filename );
			actions +=  "&nbsp;|&nbsp;<a href='javascript:deleteWaqrReport(\"" + filePath + "\", \"" + filename + "\" );'>Delete</a>";
		}
		
		if( properties.indexOf( "subscribable=true" ) != -1 ) {		
			actions += "<br/><a href='"+url+"&subscribepage=yes' target='"+target+"'>Subscribe</a>";
		}

	}

	return actions;
}

/**
 * replace single quotes with the hexadecimal character reference &#x27;
 * @param str String string to be encoded
 */
function encodeSingleQuote( str )
{
	return str.replace( /'/g, "&#x27" );
}

function getWaqrDeletePath( solution, path, filename )
{
	var baseFilename = getBaseFilename( filename );
	return "/"	+ solution + "/" + path + "/" + baseFilename;
}
function deleteWaqrReport( filePath, reportName )
{
	var yes = window.confirm( "Please confirm that you want to delete " + reportName + ".");
	try {
		if ( yes )
		{
			var component = "deleteWaqrReport";
			var params = { filePath: filePath };
			var responseMsg = WebServiceProxy.post( WebServiceProxy.ADHOC_WEBSERVICE_URL, component, params, undefined, 'text/xml' );
			var errorMsg = XmlUtil.getErrorMsg( responseMsg );
			if ( errorMsg )
			{
				alert( errorMsg );
			}
			else
			{
				var statusMsg = XmlUtil.getStatusMsg( responseMsg );
				if ( statusMsg )
				{
					alert( statusMsg );
				}
				// refresh the page so that the deleted xaction is no longer there
				window.location = window.location;
			}
		}
	}
	catch( e )
	{
		alert( e );
	}
}

function getWaqrEditUrl( solution, path, filename, url )
{
	var beginUrl = getProtocolHostPortContextParts( url );
	var middleUrl = "/adhoc/waqr.html?reportSpecPath=";
	var baseFilename = getBaseWaqrFilename( filename );
	var endUrl = "/"	+ solution + "/" + path + "/" + baseFilename + ".xreportspec";

	return beginUrl + middleUrl + endUrl;
}
function getBaseWaqrFilename( filename )
{
	var matched = filename.match( /(.*)\.xaction/ );	// get everything in the filename except the .xaction extension
	return matched[ 1 ];
}
function getBaseFilename( filename )
{
	var matched = filename.match( /(.*)\.waqr\.xaction/ );	// get everything in the filename except the .xaction extension
	return matched[ 1 ];
}
function getProtocolHostPortContextParts( strUrl )
{
	var matched = strUrl.match( /(.*)\/ViewAction.*/ );	// get everything before "/ViewAction ..."
	return matched[ 1 ];
}
function isWaqrAction( filename )
{
	return null != filename.match( /.*\.waqr\.xaction/ );	// test to see if filename ends with .waqr.xaction
}

function getAdminOptions( solution, path, filename, target, isFolder, properties ) {

	var actions = "";
	var permUrl = "PropertiesEditor?path=/pentaho-solutions/" + ( solution != null ? solution+"/" : "" )  + ( path != null ? path+"/" : "" ) + ( filename != null ? filename : "" )  ;
	actions += "<br/><br/><a href=\""+permUrl+"\" >Permissions</a>";
	return actions;
}

