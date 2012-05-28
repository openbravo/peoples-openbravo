<%@ page import="org.openbravo.retail.posterminal.POSUtils" %>
<%
String manifest = "manifest=\"../../org.openbravo.client.kernel/OBPOS_Main/AppCacheManifest\"";
if(POSUtils.isModuleInDevelopment()) {
  manifest = "";
}
%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" <%=manifest%>>
 <head>
     <title>Openbravo Point of Sale</title>
     <meta charset="utf-8">
     <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0">
     <meta name="description" content="Openbravo Point of Sale window">
     <meta name="author" content="Openbravo, S.L.U.">

     <!--  Apple application capable attributes -->
     <meta name="apple-mobile-web-app-capable" content="yes" />
     <meta name="apple-mobile-web-app-status-bar-style" content="black" />
     <meta name="apple-touch-fullscreen" content="yes" />      
     <link rel="apple-touch-startup-image" href="img/openbravopos.png"/>  

     <!-- Application icons -->
     <link rel="apple-touch-icon" href="img/openbravopos57x57.png" />
     <link rel="apple-touch-icon" sizes="72x72" href="img/openbravopos72x72.png" />
     <link rel="apple-touch-icon" sizes="114x114" href="img/openbravopos114x114.png" />
     <link rel="shortcut icon" type="image/x-icon" href="favicon.ico"/>    
     
     <link rel="stylesheet/less" href="js/libs/bootstrap/less/bootstrap.less">
     <link rel="stylesheet/less" href="js/libs/bootstrap/less/responsive.less">
     <script src="js/libs/less/less-1.3.0.min.js"></script>    

	<style type="text/css">
	 ul,ol {
	   margin-bottom: 0px;
	 }
	 
	.btnlink-toolbar:after {
    clear: both;
  } 
	
	a.btn-dropdown {
    color: white;
    cursor: pointer;  
    text-decoration: none;  	
	}
	
  a.btnkeyboard {
    display: block;

    width: 100%;
    font-size: 110%;
    text-align: center;
    padding: 11px 0px 11px 0px;
    text-decoration: none;
    background-color: #e2e2e2;
    color: black;
    cursor: pointer;      
  } 	
  a.btnkeyboard:active {
    background-color: #cccccc;
  }
  a.btnactive {
    background-color: #ccccff;
  }
  
  a.btnclear {
    padding:5px;
		font-size: 30px;
		font-weight: bold;
		line-height: 18px;
		color: black;
		text-shadow: 0 1px 0 white;
		opacity: 0.2;  
    cursor: pointer;  
    text-decoration: none;
  }
  
  div.btnkeyboard {
    display: block;

    width: 100%;
    font-size: 110%;
    text-align: center;
    padding: 11px 0px 11px 0px;
    text-decoration: none;
    background-color: darkgray;
    color: black;  
  }
  
  div.btnlink {
    display: inline-block;
    padding: 12px 15px 12px 15px;   
    text-decoration: none;
    background-color: darkgray;
    color: white;   
    margin:5px; 
    float:left;     
  }    
    
	a.btnlink {
	  display: inline-block;
    padding: 12px 15px 12px 15px;	  
    text-decoration: none;
    background-color: #6cb33f;
    color: white;
    cursor: pointer;      
    margin:5px; 
  
	}	 
  a.btnlink:active {
    background-color: #cccccc;
  }  
  
  a.btnlink-small {
    padding: 5px 15px 5px 15px;    
  } 
  
  a.btnlink-orange {
    background-color: orange;
    color: black;  
  }
  a.btnlink-lightblue {
    background-color: lightblue;
    color: black;  
  }   
  a.btnlink-lightpink {
    background-color: lightpink;
    color: black;  
  } 
  a.btnlink-lightgreen {
    background-color: lightgreen;
    color: black;  
  } 
  a.btnlink-wheat {
    background-color: wheat;
    color: black;  
  }  
  a.btnlink-gray {
    background-color: #e2e2e2;
    color: black;    
  }   
  
	ul.nav-pos:after {
	  clear: both;
	}  
	
  ul.nav-pos > li {
    float: left;
  }
  
  ul.nav-pos > li.active > a {
    background-color: #7da7d9;
  }
  
  div.btnselect {
    display: block;
    position: relative;
    padding: 8px 10px 8px 10px;
    text-decoration: none;
    border-bottom: 1px solid #cccccc;       
  }
      	 
	a.btnselect {
	  display: block;
	  position: relative;
    padding: 8px 10px 8px 10px;
	  text-decoration: none;
    color: black;
    cursor: pointer;  
    border-bottom: 1px solid #cccccc;   
	}
	a.btnselect:active {
	  background-color: #cccccc;
  }
  
  li.selected > a {
    background-color : #049cdb;
    color: #ffffff;
  }

.image-wrap {
   position: relative;
   display: inline-block;
   max-width: 100%; 
  vertical-align: bottom;
  
}
.image-wrap:after {
  content: ' ';
  width: 100%;
  height: 100%;
  position: absolute;
  top: -1px;
  left: -1px;
  border: solid 1px #cccccc;

/*   -wekbit-box-shadow: inset 0 0 1px rgba(255,255,255,.4), inset 0 1px 0 rgba(255,255,255,.4), 0 1px 2px rgba(0,0,0,.3); */
/*   -moz-box-shadow: inset 0 0 1px rgba(255,255,255,.4), inset 0 1px 0 rgba(255,255,255,.4), 0 1px 2px rgba(0,0,0,.3); */
/*   box-shadow: inset 0 0 1px rgba(255,255,255,.4), inset 0 1px 0 rgba(255,255,255,.4), 0 1px 2px rgba(0,0,0,.3); */

  -webkit-border-radius: 7px;
  -moz-border-radius: 7px;
  border-radius: 7px;
}

.image-wrap div {

  vertical-align: bottom;

/*   -webkit-box-shadow: 0 1px 2px rgba(0,0,0,.4); */
/*   -moz-box-shadow: 0 1px 2px rgba(0,0,0,.4); */
/*   box-shadow: 0 1px 2px rgba(0,0,0,.4); */

  -webkit-border-radius: 6px;
  -moz-border-radius: 6px;
  border-radius: 6px;
}

div.pos-clock-container {
  position:absolute;
  bottom: 0px;
  right: 0px;
  text-align: center;
}

div.pos-clock-time {
  font-size:300%;
  margin: 10px;
}

div.pos-clock-date {
  font-size:125%;
  margin: 10px;
}

	</style>

  <style type="text/css">
   /* Styles for login window */

    div.login-header-row {
      height: 90px;
    }

    div.login-header-company {
      width: 400px;
      height: 80px;
      float: left;
      background-image: url('img/CompanyLogo.png');
      background-repeat:  no-repeat;
    }

    div.login-header-ob {
      width: 80px;
      height: 80px;
      float: right;
      background-image: url('img/OBLogo.png');
      background-repeat:  no-repeat;
    }

    div.login-user-container {
      height: 79%;
      overflow: auto;
    }

    div.login-user-button {
      float: left;
      width: 100px;
      height: 100px;
      background-image: url('img/anonymous-icon.png');
      background-size: 100% 100%;
      margin: 0px 7px 7px 0px;
      cursor: pointer;
    }

    div.login-user-button-bottom {
      width: 100px;
      height: 18px;
      background-color: white;
      opacity: 0.88;
      margin: 82px 0px 0px 0px;
      overflow: hidden;
    }

    span.login-user-button-bottom-icon {
      display: inline-block;
      width: 20px;
      background-image: url('img/login-not-connected.png');
      background-repeat: no-repeat;
      background-position: 5px 3px;
    }

    span.login-user-button-bottom-text {
      font-weight: bold;
    }

    div.login-inputs-container {
      height: 207px;
      background-color: #6CB33F;
    }

    div.login-inputs-screenlocked {
      text-align: right;
      font-weight: bold;
      font-size: 16px;
      color: white;
      margin: 10px 0px 0px -10px;
    }

    div.login-inputs-userpassword {
      padding: 0px 0px 0px 20px;
    }

    a.login-inputs-button {
      display: inline-block;
      font-weight: bold;
      font-size: 18px;
      padding: 9px 20px 12px 20px;
      text-decoration: none;
      margin-top: 40px;
      margin-left: 20%;
      float: left;
      background-color: white;
      color: #72B446;
    }

    div.login-clock-container {
      margin: 40px 0px 0px -40px;
      text-align: center;
      color: white;
    }

    div.login-clock-time {
      font-size: 300%;
    }

    div.login-clock-date {
      font-size: 125%;
      margin: 10px 0px 0px 0px;
    }
  </style>
</head>

<body style="padding-top: 20px;background-color: darkgray; background: url(img/BACKGROUND-PNG24.png) top left">
<div id="container" class="container">
  <div id="topsection" class="section">
    <div class="row">
      <div class="span12" style="color: white;">
        <div id="online" style="display: inline-block; margin: 5px;"><span class="badge badge-success">Online</span></div>
        <div id="terminal" style="display: inline-block; margin: 5px;"></div>
        <div class="dropdown" style="display: inline-block; margin-left:30px;" >
          <a id="yourcompany" class="btn-dropdown" href="#" class="dropdown-toggle" data-toggle="dropdown"></a> 
          <div class="dropdown-menu" style="color: black; padding:5px; width: 400px;">
            <img src="../../utility/ShowImageLogo?logo=yourcompanymenu" alt="Your company" style="display:block; float:left;width: 20%;"/>
            <div id="yourcompanyproperties" style="display:block; margin-left:10px; float:left;width: 70%"></div>
            <div style="clear:both;"></div>
          </div>
        </div>
        <div class="dropdown" style="display: inline-block; margin-left:30px;" >
          <a id="loggeduser" class="btn-dropdown" href="#" class="dropdown-toggle" data-toggle="dropdown"></a> 
          <div id="loggeduserproperties" class="dropdown-menu" style="color: black; padding:5px; width: 400px;">
          </div>
        </div>        
        <div style="display: inline-block; font-weight: bold; float:right">
          <span>Openbravo Point of Sale </span>
          <a id="logoutaction" href="#" class=""><i class="icon-off icon-white"></i></a>
        </div>
      </div>
    </div>
  </div>
  <div>
    <div id="containerwindow">
      <!-- Here it goes the POS window... -->
    </div>
  </div>
</div>

<script src="../org.openbravo.client.kernel/js/BigDecimal-all-1.0.1.min.js"></script>

<script src="js/libs/jquery-1.7.2.js"></script>
<script src="js/libs/underscore-1.3.3.js"></script>
<script src="js/libs/backbone-0.9.2.js"></script>
<script src="js/libs/bootstrap/js/bootstrap-tab.js"></script>
<script src="js/libs/bootstrap/js/bootstrap-dropdown.js"></script>
<script src="js/libs/bootstrap/js/bootstrap-modal.js"></script>
<script src="js/libs/bootstrap/js/bootstrap-alert.js"></script>

<script data-main="js/main" src="js/libs/require-1.0.7.min.js"></script>

<script>
$('#logoutaction').click(function (e) {
  e.preventDefault();
  OB.POS.logout();
});


// Hack focus captured by location bar in android browser.
(function () {      
	var locationwarning = true;
	var focuskeeper = $('<input id="focuskeeper" style="position:fixed; top:-1000px; left:-1000px;" type="text"/>');  
	$("body").append(focuskeeper);
	$("body").focusin(function() {
	  locationwarning = false;
	});
	$("body").focusout(function() {
	  locationwarning = true;
	}); 
	
	window.fixFocus = function () {
	  
		if (locationwarning) {
		  focuskeeper.focus();
		}	 
		var t = document.activeElement.tagName;
		var id = document.activeElement.id;
		return (id === 'focuskeeper' || (t !=='INPUT' && t !=='SELECT' && t !=='TEXTAREA')); // process key
	}
}());
        
</script>

</body>
</html>
