/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

/*
	This is a compiled version of Dojo, built for deployment and not for
	development. To get an editable version, please visit:

		http://dojotoolkit.org

	for documentation and information on getting the source.
*/

if(typeof dojo=="undefined"){
var dj_global=this;
var dj_currentContext=this;
function dj_undef(_1,_2){
return (typeof (_2||dj_currentContext)[_1]=="undefined");
}
if(dj_undef("djConfig",this)){
var djConfig={};
}
if(dj_undef("dojo",this)){
var dojo={};
}
dojo.global=function(){
return dj_currentContext;
};
dojo.locale=djConfig.locale;
dojo.version={major:0,minor:0,patch:0,flag:"dev",revision:Number("$Rev: 7616 $".match(/[0-9]+/)[0]),toString:function(){
with(dojo.version){
return major+"."+minor+"."+patch+flag+" ("+revision+")";
}
}};
dojo.evalProp=function(_3,_4,_5){
if((!_4)||(!_3)){
return undefined;
}
if(!dj_undef(_3,_4)){
return _4[_3];
}
return (_5?(_4[_3]={}):undefined);
};
dojo.parseObjPath=function(_6,_7,_8){
var _9=(_7||dojo.global());
var _a=_6.split(".");
var _b=_a.pop();
for(var i=0,l=_a.length;i<l&&_9;i++){
_9=dojo.evalProp(_a[i],_9,_8);
}
return {obj:_9,prop:_b};
};
dojo.evalObjPath=function(_e,_f){
if(typeof _e!="string"){
return dojo.global();
}
if(_e.indexOf(".")==-1){
return dojo.evalProp(_e,dojo.global(),_f);
}
var ref=dojo.parseObjPath(_e,dojo.global(),_f);
if(ref){
return dojo.evalProp(ref.prop,ref.obj,_f);
}
return null;
};
dojo.errorToString=function(_11){
if(!dj_undef("message",_11)){
return _11.message;
}else{
if(!dj_undef("description",_11)){
return _11.description;
}else{
return _11;
}
}
};
dojo.raise=function(_12,_13){
if(_13){
_12=_12+": "+dojo.errorToString(_13);
}else{
_12=dojo.errorToString(_12);
}
try{
if(djConfig.isDebug){
dojo.hostenv.println("FATAL exception raised: "+_12);
}
}
catch(e){
}
throw _13||Error(_12);
};
dojo.debug=function(){
};
dojo.debugShallow=function(obj){
};
dojo.profile={start:function(){
},end:function(){
},stop:function(){
},dump:function(){
}};
function dj_eval(_15){
return dj_global.eval?dj_global.eval(_15):eval(_15);
}
dojo.unimplemented=function(_16,_17){
var _18="'"+_16+"' not implemented";
if(_17!=null){
_18+=" "+_17;
}
dojo.raise(_18);
};
dojo.deprecated=function(_19,_1a,_1b){
var _1c="DEPRECATED: "+_19;
if(_1a){
_1c+=" "+_1a;
}
if(_1b){
_1c+=" -- will be removed in version: "+_1b;
}
dojo.debug(_1c);
};
dojo.render=(function(){
function vscaffold(_1d,_1e){
var tmp={capable:false,support:{builtin:false,plugin:false},prefixes:_1d};
for(var i=0;i<_1e.length;i++){
tmp[_1e[i]]=false;
}
return tmp;
}
return {name:"",ver:dojo.version,os:{win:false,linux:false,osx:false},html:vscaffold(["html"],["ie","opera","khtml","safari","moz"]),svg:vscaffold(["svg"],["corel","adobe","batik"]),vml:vscaffold(["vml"],["ie"]),swf:vscaffold(["Swf","Flash","Mm"],["mm"]),swt:vscaffold(["Swt"],["ibm"])};
})();
dojo.hostenv=(function(){
var _21={isDebug:false,allowQueryConfig:false,baseScriptUri:"",baseRelativePath:"",libraryScriptUri:"",iePreventClobber:false,ieClobberMinimal:true,preventBackButtonFix:true,delayMozLoadingFix:false,searchIds:[],parseWidgets:true};
if(typeof djConfig=="undefined"){
djConfig=_21;
}else{
for(var _22 in _21){
if(typeof djConfig[_22]=="undefined"){
djConfig[_22]=_21[_22];
}
}
}
return {name_:"(unset)",version_:"(unset)",getName:function(){
return this.name_;
},getVersion:function(){
return this.version_;
},getText:function(uri){
dojo.unimplemented("getText","uri="+uri);
}};
})();
dojo.hostenv.getBaseScriptUri=function(){
if(djConfig.baseScriptUri.length){
return djConfig.baseScriptUri;
}
var uri=new String(djConfig.libraryScriptUri||djConfig.baseRelativePath);
if(!uri){
dojo.raise("Nothing returned by getLibraryScriptUri(): "+uri);
}
var _25=uri.lastIndexOf("/");
djConfig.baseScriptUri=djConfig.baseRelativePath;
return djConfig.baseScriptUri;
};
(function(){
var _26={pkgFileName:"__package__",loading_modules_:{},loaded_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},setModulePrefix:function(_27,_28){
this.modulePrefixes_[_27]={name:_27,value:_28};
},moduleHasPrefix:function(_29){
var mp=this.modulePrefixes_;
return Boolean(mp[_29]&&mp[_29].value);
},getModulePrefix:function(_2b){
if(this.moduleHasPrefix(_2b)){
return this.modulePrefixes_[_2b].value;
}
return _2b;
},getTextStack:[],loadUriStack:[],loadedUris:[],post_load_:false,modulesLoadedListeners:[],unloadListeners:[],loadNotifying:false};
for(var _2c in _26){
dojo.hostenv[_2c]=_26[_2c];
}
})();
dojo.hostenv.loadPath=function(_2d,_2e,cb){
var uri;
if(_2d.charAt(0)=="/"||_2d.match(/^\w+:/)){
uri=_2d;
}else{
uri=this.getBaseScriptUri()+_2d;
}
if(djConfig.cacheBust&&dojo.render.html.capable){
uri+="?"+String(djConfig.cacheBust).replace(/\W+/g,"");
}
try{
return !_2e?this.loadUri(uri,cb):this.loadUriAndCheck(uri,_2e,cb);
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.hostenv.loadUri=function(uri,cb){
if(this.loadedUris[uri]){
return true;
}
var _33=this.getText(uri,null,true);
if(!_33){
return false;
}
this.loadedUris[uri]=true;
if(cb){
_33="("+_33+")";
}
var _34=dj_eval(_33);
if(cb){
cb(_34);
}
return true;
};
dojo.hostenv.loadUriAndCheck=function(uri,_36,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dojo.debug("failed loading ",uri," with error: ",e);
}
return Boolean(ok&&this.findModule(_36,false));
};
dojo.loaded=function(){
};
dojo.unloaded=function(){
};
dojo.hostenv.loaded=function(){
this.loadNotifying=true;
this.post_load_=true;
var mll=this.modulesLoadedListeners;
for(var x=0;x<mll.length;x++){
mll[x]();
}
this.modulesLoadedListeners=[];
this.loadNotifying=false;
dojo.loaded();
};
dojo.hostenv.unloaded=function(){
var mll=this.unloadListeners;
while(mll.length){
(mll.pop())();
}
dojo.unloaded();
};
dojo.addOnLoad=function(obj,_3d){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.modulesLoadedListeners.push(obj);
}else{
if(arguments.length>1){
dh.modulesLoadedListeners.push(function(){
obj[_3d]();
});
}
}
if(dh.post_load_&&dh.inFlightCount==0&&!dh.loadNotifying){
dh.callLoaded();
}
};
dojo.addOnUnload=function(obj,_40){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.unloadListeners.push(obj);
}else{
if(arguments.length>1){
dh.unloadListeners.push(function(){
obj[_40]();
});
}
}
};
dojo.hostenv.modulesLoaded=function(){
if(this.post_load_){
return;
}
if(this.loadUriStack.length==0&&this.getTextStack.length==0){
if(this.inFlightCount>0){
dojo.debug("files still in flight!");
return;
}
dojo.hostenv.callLoaded();
}
};
dojo.hostenv.callLoaded=function(){
if(typeof setTimeout=="object"||(djConfig["useXDomain"]&&dojo.render.html.opera)){
setTimeout("dojo.hostenv.loaded();",0);
}else{
dojo.hostenv.loaded();
}
};
dojo.hostenv.getModuleSymbols=function(_42){
var _43=_42.split(".");
for(var i=_43.length;i>0;i--){
var _45=_43.slice(0,i).join(".");
if((i==1)&&!this.moduleHasPrefix(_45)){
_43[0]="../"+_43[0];
}else{
var _46=this.getModulePrefix(_45);
if(_46!=_45){
_43.splice(0,i,_46);
break;
}
}
}
return _43;
};
dojo.hostenv._global_omit_module_check=false;
dojo.hostenv.loadModule=function(_47,_48,_49){
if(!_47){
return;
}
_49=this._global_omit_module_check||_49;
var _4a=this.findModule(_47,false);
if(_4a){
return _4a;
}
if(dj_undef(_47,this.loading_modules_)){
this.addedToLoadingCount.push(_47);
}
this.loading_modules_[_47]=1;
var _4b=_47.replace(/\./g,"/")+".js";
var _4c=_47.split(".");
var _4d=this.getModuleSymbols(_47);
var _4e=((_4d[0].charAt(0)!="/")&&!_4d[0].match(/^\w+:/));
var _4f=_4d[_4d.length-1];
var ok;
if(_4f=="*"){
_47=_4c.slice(0,-1).join(".");
while(_4d.length){
_4d.pop();
_4d.push(this.pkgFileName);
_4b=_4d.join("/")+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,!_49?_47:null);
if(ok){
break;
}
_4d.pop();
}
}else{
_4b=_4d.join("/")+".js";
_47=_4c.join(".");
var _51=!_49?_47:null;
ok=this.loadPath(_4b,_51);
if(!ok&&!_48){
_4d.pop();
while(_4d.length){
_4b=_4d.join("/")+".js";
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
_4d.pop();
_4b=_4d.join("/")+"/"+this.pkgFileName+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
}
}
if(!ok&&!_49){
dojo.raise("Could not load '"+_47+"'; last tried '"+_4b+"'");
}
}
if(!_49&&!this["isXDomain"]){
_4a=this.findModule(_47,false);
if(!_4a){
dojo.raise("symbol '"+_47+"' is not defined after loading '"+_4b+"'");
}
}
return _4a;
};
dojo.hostenv.startPackage=function(_52){
var _53=String(_52);
var _54=_53;
var _55=_52.split(/\./);
if(_55[_55.length-1]=="*"){
_55.pop();
_54=_55.join(".");
}
var _56=dojo.evalObjPath(_54,true);
this.loaded_modules_[_53]=_56;
this.loaded_modules_[_54]=_56;
return _56;
};
dojo.hostenv.findModule=function(_57,_58){
var lmn=String(_57);
if(this.loaded_modules_[lmn]){
return this.loaded_modules_[lmn];
}
if(_58){
dojo.raise("no loaded module named '"+_57+"'");
}
return null;
};
dojo.kwCompoundRequire=function(_5a){
var _5b=_5a["common"]||[];
var _5c=_5a[dojo.hostenv.name_]?_5b.concat(_5a[dojo.hostenv.name_]||[]):_5b.concat(_5a["default"]||[]);
for(var x=0;x<_5c.length;x++){
var _5e=_5c[x];
if(_5e.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_5e);
}else{
dojo.hostenv.loadModule(_5e);
}
}
};
dojo.require=function(_5f){
dojo.hostenv.loadModule.apply(dojo.hostenv,arguments);
};
dojo.requireIf=function(_60,_61){
var _62=arguments[0];
if((_62===true)||(_62=="common")||(_62&&dojo.render[_62].capable)){
var _63=[];
for(var i=1;i<arguments.length;i++){
_63.push(arguments[i]);
}
dojo.require.apply(dojo,_63);
}
};
dojo.requireAfterIf=dojo.requireIf;
dojo.provide=function(_65){
return dojo.hostenv.startPackage.apply(dojo.hostenv,arguments);
};
dojo.registerModulePath=function(_66,_67){
return dojo.hostenv.setModulePrefix(_66,_67);
};
if(djConfig["modulePaths"]){
for(var param in djConfig["modulePaths"]){
dojo.registerModulePath(param,djConfig["modulePaths"][param]);
}
}
dojo.setModulePrefix=function(_68,_69){
dojo.deprecated("dojo.setModulePrefix(\""+_68+"\", \""+_69+"\")","replaced by dojo.registerModulePath","0.5");
return dojo.registerModulePath(_68,_69);
};
dojo.exists=function(obj,_6b){
var p=_6b.split(".");
for(var i=0;i<p.length;i++){
if(!obj[p[i]]){
return false;
}
obj=obj[p[i]];
}
return true;
};
dojo.hostenv.normalizeLocale=function(_6e){
var _6f=_6e?_6e.toLowerCase():dojo.locale;
if(_6f=="root"){
_6f="ROOT";
}
return _6f;
};
dojo.hostenv.searchLocalePath=function(_70,_71,_72){
_70=dojo.hostenv.normalizeLocale(_70);
var _73=_70.split("-");
var _74=[];
for(var i=_73.length;i>0;i--){
_74.push(_73.slice(0,i).join("-"));
}
_74.push(false);
if(_71){
_74.reverse();
}
for(var j=_74.length-1;j>=0;j--){
var loc=_74[j]||"ROOT";
var _78=_72(loc);
if(_78){
break;
}
}
};
dojo.hostenv.localesGenerated=["ROOT","es-es","es","it-it","pt-br","de","fr-fr","zh-cn","pt","en-us","zh","fr","zh-tw","it","en-gb","xx","de-de","ko-kr","ja-jp","ko","en","ja"];
dojo.hostenv.registerNlsPrefix=function(){
dojo.registerModulePath("nls","nls");
};
dojo.hostenv.preloadLocalizations=function(){
if(dojo.hostenv.localesGenerated){
dojo.hostenv.registerNlsPrefix();
function preload(_79){
_79=dojo.hostenv.normalizeLocale(_79);
dojo.hostenv.searchLocalePath(_79,true,function(loc){
for(var i=0;i<dojo.hostenv.localesGenerated.length;i++){
if(dojo.hostenv.localesGenerated[i]==loc){
dojo["require"]("nls.dojo_"+loc);
return true;
}
}
return false;
});
}
preload();
var _7c=djConfig.extraLocale||[];
for(var i=0;i<_7c.length;i++){
preload(_7c[i]);
}
}
dojo.hostenv.preloadLocalizations=function(){
};
};
dojo.requireLocalization=function(_7e,_7f,_80,_81){
dojo.hostenv.preloadLocalizations();
var _82=dojo.hostenv.normalizeLocale(_80);
var _83=[_7e,"nls",_7f].join(".");
var _84="";
if(_81){
var _85=_81.split(",");
for(var i=0;i<_85.length;i++){
if(_82.indexOf(_85[i])==0){
if(_85[i].length>_84.length){
_84=_85[i];
}
}
}
if(!_84){
_84="ROOT";
}
}
var _87=_81?_84:_82;
var _88=dojo.hostenv.findModule(_83);
var _89=null;
if(_88){
if(djConfig.localizationComplete&&_88._built){
return;
}
var _8a=_87.replace("-","_");
var _8b=_83+"."+_8a;
_89=dojo.hostenv.findModule(_8b);
}
if(!_89){
_88=dojo.hostenv.startPackage(_83);
var _8c=dojo.hostenv.getModuleSymbols(_7e);
var _8d=_8c.concat("nls").join("/");
var _8e;
dojo.hostenv.searchLocalePath(_87,_81,function(loc){
var _90=loc.replace("-","_");
var _91=_83+"."+_90;
var _92=false;
if(!dojo.hostenv.findModule(_91)){
dojo.hostenv.startPackage(_91);
var _93=[_8d];
if(loc!="ROOT"){
_93.push(loc);
}
_93.push(_7f);
var _94=_93.join("/")+".js";
_92=dojo.hostenv.loadPath(_94,null,function(_95){
var _96=function(){
};
_96.prototype=_8e;
_88[_90]=new _96();
for(var j in _95){
_88[_90][j]=_95[j];
}
});
}else{
_92=true;
}
if(_92&&_88[_90]){
_8e=_88[_90];
}else{
_88[_90]=_8e;
}
if(_81){
return true;
}
});
}
if(_81&&_82!=_84){
_88[_82.replace("-","_")]=_88[_84.replace("-","_")];
}
};
(function(){
var _98=djConfig.extraLocale;
if(_98){
if(!_98 instanceof Array){
_98=[_98];
}
var req=dojo.requireLocalization;
dojo.requireLocalization=function(m,b,_9c,_9d){
req(m,b,_9c,_9d);
if(_9c){
return;
}
for(var i=0;i<_98.length;i++){
req(m,b,_98[i],_9d);
}
};
}
})();
}
if(typeof window!="undefined"){
(function(){
if(djConfig.allowQueryConfig){
var _9f=document.location.toString();
var _a0=_9f.split("?",2);
if(_a0.length>1){
var _a1=_a0[1];
var _a2=_a1.split("&");
for(var x in _a2){
var sp=_a2[x].split("=");
if((sp[0].length>9)&&(sp[0].substr(0,9)=="djConfig.")){
var opt=sp[0].substr(9);
try{
djConfig[opt]=eval(sp[1]);
}
catch(e){
djConfig[opt]=sp[1];
}
}
}
}
}
if(((djConfig["baseScriptUri"]=="")||(djConfig["baseRelativePath"]==""))&&(document&&document.getElementsByTagName)){
var _a6=document.getElementsByTagName("script");
var _a7=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_a6.length;i++){
var src=_a6[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_a7);
if(m){
var _ab=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_ab+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_ab;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_ab;
}
break;
}
}
}
var dr=dojo.render;
var drh=dojo.render.html;
var drs=dojo.render.svg;
var dua=(drh.UA=navigator.userAgent);
var dav=(drh.AV=navigator.appVersion);
var t=true;
var f=false;
drh.capable=t;
drh.support.builtin=t;
dr.ver=parseFloat(drh.AV);
dr.os.mac=dav.indexOf("Macintosh")>=0;
dr.os.win=dav.indexOf("Windows")>=0;
dr.os.linux=dav.indexOf("X11")>=0;
drh.opera=dua.indexOf("Opera")>=0;
drh.khtml=(dav.indexOf("Konqueror")>=0)||(dav.indexOf("Safari")>=0);
drh.safari=dav.indexOf("Safari")>=0;
var _b3=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_b3>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_b3+6,_b3+14);
}
drh.ie=(document.all)&&(!drh.opera);
drh.ie50=drh.ie&&dav.indexOf("MSIE 5.0")>=0;
drh.ie55=drh.ie&&dav.indexOf("MSIE 5.5")>=0;
drh.ie60=drh.ie&&dav.indexOf("MSIE 6.0")>=0;
drh.ie70=drh.ie&&dav.indexOf("MSIE 7.0")>=0;
var cm=document["compatMode"];
drh.quirks=(cm=="BackCompat")||(cm=="QuirksMode")||drh.ie55||drh.ie50;
dojo.locale=dojo.locale||(drh.ie?navigator.userLanguage:navigator.language).toLowerCase();
dr.vml.capable=drh.ie;
drs.capable=f;
drs.support.plugin=f;
drs.support.builtin=f;
var _b5=window["document"];
var tdi=_b5["implementation"];
if((tdi)&&(tdi["hasFeature"])&&(tdi.hasFeature("org.w3c.dom.svg","1.0"))){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
if(drh.safari){
var tmp=dua.split("AppleWebKit/")[1];
var ver=parseFloat(tmp.split(" ")[0]);
if(ver>=420){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
}else{
}
})();
dojo.hostenv.startPackage("dojo.hostenv");
dojo.render.name=dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
dojo.hostenv._XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _b9=null;
var _ba=null;
try{
_b9=new XMLHttpRequest();
}
catch(e){
}
if(!_b9){
for(var i=0;i<3;++i){
var _bc=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_b9=new ActiveXObject(_bc);
}
catch(e){
_ba=e;
}
if(_b9){
dojo.hostenv._XMLHTTP_PROGIDS=[_bc];
break;
}
}
}
if(!_b9){
return dojo.raise("XMLHTTP not available",_ba);
}
return _b9;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_be,_bf){
if(!_be){
this._blockAsync=true;
}
var _c0=this.getXmlhttpObject();
function isDocumentOk(_c1){
var _c2=_c1["status"];
return Boolean((!_c2)||((200<=_c2)&&(300>_c2))||(_c2==304));
}
if(_be){
var _c3=this,_c4=null,gbl=dojo.global();
var xhr=dojo.evalObjPath("dojo.io.XMLHTTPTransport");
_c0.onreadystatechange=function(){
if(_c4){
gbl.clearTimeout(_c4);
_c4=null;
}
if(_c3._blockAsync||(xhr&&xhr._blockAsync)){
_c4=gbl.setTimeout(function(){
_c0.onreadystatechange.apply(this);
},10);
}else{
if(4==_c0.readyState){
if(isDocumentOk(_c0)){
_be(_c0.responseText);
}
}
}
};
}
_c0.open("GET",uri,_be?true:false);
try{
_c0.send(null);
if(_be){
return null;
}
if(!isDocumentOk(_c0)){
var err=Error("Unable to load "+uri+" status:"+_c0.status);
err.status=_c0.status;
err.responseText=_c0.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_bf)&&(!_be)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _c0.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_c8){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_c8);
}else{
try{
var _c9=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_c9){
_c9=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_c8));
_c9.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_c8+"</div>");
}
catch(e2){
window.status=_c8;
}
}
}
};
dojo.addOnLoad(function(){
dojo.hostenv._println_safe=true;
while(dojo.hostenv._println_buffer.length>0){
dojo.hostenv.println(dojo.hostenv._println_buffer.shift());
}
});
function dj_addNodeEvtHdlr(_cb,_cc,fp){
var _ce=_cb["on"+_cc]||function(){
};
_cb["on"+_cc]=function(){
fp.apply(_cb,arguments);
_ce.apply(_cb,arguments);
};
return true;
}
function dj_load_init(e){
var _d0=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_d0!="domcontentloaded"&&_d0!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _d1=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_d1();
dojo.hostenv.modulesLoaded();
}else{
dojo.hostenv.modulesLoadedListeners.unshift(_d1);
}
}
if(document.addEventListener){
if(dojo.render.html.opera||(dojo.render.html.moz&&(djConfig["enableMozDomContentLoaded"]===true))){
document.addEventListener("DOMContentLoaded",dj_load_init,null);
}
window.addEventListener("load",dj_load_init,null);
}
if(dojo.render.html.ie&&dojo.render.os.win){
document.attachEvent("onreadystatechange",function(e){
if(document.readyState=="complete"){
dj_load_init();
}
});
}
if(/(WebKit|khtml)/i.test(navigator.userAgent)){
var _timer=setInterval(function(){
if(/loaded|complete/.test(document.readyState)){
dj_load_init();
}
},10);
}
if(dojo.render.html.ie){
dj_addNodeEvtHdlr(window,"beforeunload",function(){
dojo.hostenv._unloading=true;
window.setTimeout(function(){
dojo.hostenv._unloading=false;
},0);
});
}
dj_addNodeEvtHdlr(window,"unload",function(){
dojo.hostenv.unloaded();
if((!dojo.render.html.ie)||(dojo.render.html.ie&&dojo.hostenv._unloading)){
dojo.hostenv.unloaded();
}
});
dojo.hostenv.makeWidgets=function(){
var _d3=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_d3=_d3.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_d3=_d3.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_d3.length>0)){
if(dojo.evalObjPath("dojo.widget.Parse")){
var _d4=new dojo.xml.Parse();
if(_d3.length>0){
for(var x=0;x<_d3.length;x++){
var _d6=document.getElementById(_d3[x]);
if(!_d6){
continue;
}
var _d7=_d4.parseElement(_d6,null,true);
dojo.widget.getParser().createComponents(_d7);
}
}else{
if(djConfig.parseWidgets){
var _d7=_d4.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_d7);
}
}
}
}
};
dojo.addOnLoad(function(){
if(!dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
});
try{
if(dojo.render.html.ie){
document.namespaces.add("v","urn:schemas-microsoft-com:vml");
document.createStyleSheet().addRule("v\\:*","behavior:url(#default#VML)");
}
}
catch(e){
}
dojo.hostenv.writeIncludes=function(){
};
if(!dj_undef("document",this)){
dj_currentDocument=this.document;
}
dojo.doc=function(){
return dj_currentDocument;
};
dojo.body=function(){
return dojo.doc().body||dojo.doc().getElementsByTagName("body")[0];
};
dojo.byId=function(id,doc){
if((id)&&((typeof id=="string")||(id instanceof String))){
if(!doc){
doc=dj_currentDocument;
}
var ele=doc.getElementById(id);
if(ele&&(ele.id!=id)&&doc.all){
ele=null;
eles=doc.all[id];
if(eles){
if(eles.length){
for(var i=0;i<eles.length;i++){
if(eles[i].id==id){
ele=eles[i];
break;
}
}
}else{
ele=eles;
}
}
}
return ele;
}
return id;
};
dojo.setContext=function(_dc,_dd){
dj_currentContext=_dc;
dj_currentDocument=_dd;
};
dojo._fireCallback=function(_de,_df,_e0){
if((_df)&&((typeof _de=="string")||(_de instanceof String))){
_de=_df[_de];
}
return (_df?_de.apply(_df,_e0||[]):_de());
};
dojo.withGlobal=function(_e1,_e2,_e3,_e4){
var _e5;
var _e6=dj_currentContext;
var _e7=dj_currentDocument;
try{
dojo.setContext(_e1,_e1.document);
_e5=dojo._fireCallback(_e2,_e3,_e4);
}
finally{
dojo.setContext(_e6,_e7);
}
return _e5;
};
dojo.withDoc=function(_e8,_e9,_ea,_eb){
var _ec;
var _ed=dj_currentDocument;
try{
dj_currentDocument=_e8;
_ec=dojo._fireCallback(_e9,_ea,_eb);
}
finally{
dj_currentDocument=_ed;
}
return _ec;
};
}
dojo.requireIf((djConfig["isDebug"]||djConfig["debugAtAllCosts"]),"dojo.debug");
dojo.requireIf(djConfig["debugAtAllCosts"]&&!window.widget&&!djConfig["useXDomain"],"dojo.browser_debug");
dojo.requireIf(djConfig["debugAtAllCosts"]&&!window.widget&&djConfig["useXDomain"],"dojo.browser_debug_xd");
dojo.provide("dojo.dom");
dojo.dom.ELEMENT_NODE=1;
dojo.dom.ATTRIBUTE_NODE=2;
dojo.dom.TEXT_NODE=3;
dojo.dom.CDATA_SECTION_NODE=4;
dojo.dom.ENTITY_REFERENCE_NODE=5;
dojo.dom.ENTITY_NODE=6;
dojo.dom.PROCESSING_INSTRUCTION_NODE=7;
dojo.dom.COMMENT_NODE=8;
dojo.dom.DOCUMENT_NODE=9;
dojo.dom.DOCUMENT_TYPE_NODE=10;
dojo.dom.DOCUMENT_FRAGMENT_NODE=11;
dojo.dom.NOTATION_NODE=12;
dojo.dom.dojoml="http://www.dojotoolkit.org/2004/dojoml";
dojo.dom.xmlns={svg:"http://www.w3.org/2000/svg",smil:"http://www.w3.org/2001/SMIL20/",mml:"http://www.w3.org/1998/Math/MathML",cml:"http://www.xml-cml.org",xlink:"http://www.w3.org/1999/xlink",xhtml:"http://www.w3.org/1999/xhtml",xul:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul",xbl:"http://www.mozilla.org/xbl",fo:"http://www.w3.org/1999/XSL/Format",xsl:"http://www.w3.org/1999/XSL/Transform",xslt:"http://www.w3.org/1999/XSL/Transform",xi:"http://www.w3.org/2001/XInclude",xforms:"http://www.w3.org/2002/01/xforms",saxon:"http://icl.com/saxon",xalan:"http://xml.apache.org/xslt",xsd:"http://www.w3.org/2001/XMLSchema",dt:"http://www.w3.org/2001/XMLSchema-datatypes",xsi:"http://www.w3.org/2001/XMLSchema-instance",rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",rdfs:"http://www.w3.org/2000/01/rdf-schema#",dc:"http://purl.org/dc/elements/1.1/",dcq:"http://purl.org/dc/qualifiers/1.0","soap-env":"http://schemas.xmlsoap.org/soap/envelope/",wsdl:"http://schemas.xmlsoap.org/wsdl/",AdobeExtensions:"http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/"};
dojo.dom.isNode=function(wh){
if(typeof Element=="function"){
try{
return wh instanceof Element;
}
catch(e){
}
}else{
return wh&&!isNaN(wh.nodeType);
}
};
dojo.dom.getUniqueId=function(){
var _ef=dojo.doc();
do{
var id="dj_unique_"+(++arguments.callee._idIncrement);
}while(_ef.getElementById(id));
return id;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_f1,_f2){
var _f3=_f1.firstChild;
while(_f3&&_f3.nodeType!=dojo.dom.ELEMENT_NODE){
_f3=_f3.nextSibling;
}
if(_f2&&_f3&&_f3.tagName&&_f3.tagName.toLowerCase()!=_f2.toLowerCase()){
_f3=dojo.dom.nextElement(_f3,_f2);
}
return _f3;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_f4,_f5){
var _f6=_f4.lastChild;
while(_f6&&_f6.nodeType!=dojo.dom.ELEMENT_NODE){
_f6=_f6.previousSibling;
}
if(_f5&&_f6&&_f6.tagName&&_f6.tagName.toLowerCase()!=_f5.toLowerCase()){
_f6=dojo.dom.prevElement(_f6,_f5);
}
return _f6;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(_f7,_f8){
if(!_f7){
return null;
}
do{
_f7=_f7.nextSibling;
}while(_f7&&_f7.nodeType!=dojo.dom.ELEMENT_NODE);
if(_f7&&_f8&&_f8.toLowerCase()!=_f7.tagName.toLowerCase()){
return dojo.dom.nextElement(_f7,_f8);
}
return _f7;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(_f9,_fa){
if(!_f9){
return null;
}
if(_fa){
_fa=_fa.toLowerCase();
}
do{
_f9=_f9.previousSibling;
}while(_f9&&_f9.nodeType!=dojo.dom.ELEMENT_NODE);
if(_f9&&_fa&&_fa.toLowerCase()!=_f9.tagName.toLowerCase()){
return dojo.dom.prevElement(_f9,_fa);
}
return _f9;
};
dojo.dom.moveChildren=function(_fb,_fc,_fd){
var _fe=0;
if(_fd){
while(_fb.hasChildNodes()&&_fb.firstChild.nodeType==dojo.dom.TEXT_NODE){
_fb.removeChild(_fb.firstChild);
}
while(_fb.hasChildNodes()&&_fb.lastChild.nodeType==dojo.dom.TEXT_NODE){
_fb.removeChild(_fb.lastChild);
}
}
while(_fb.hasChildNodes()){
_fc.appendChild(_fb.firstChild);
_fe++;
}
return _fe;
};
dojo.dom.copyChildren=function(_ff,_100,trim){
var _102=_ff.cloneNode(true);
return this.moveChildren(_102,_100,trim);
};
dojo.dom.replaceChildren=function(node,_104){
var _105=[];
if(dojo.render.html.ie){
for(var i=0;i<node.childNodes.length;i++){
_105.push(node.childNodes[i]);
}
}
dojo.dom.removeChildren(node);
node.appendChild(_104);
for(var i=0;i<_105.length;i++){
dojo.dom.destroyNode(_105[i]);
}
};
dojo.dom.removeChildren=function(node){
var _108=node.childNodes.length;
while(node.hasChildNodes()){
dojo.dom.removeNode(node.firstChild);
}
return _108;
};
dojo.dom.replaceNode=function(node,_10a){
return node.parentNode.replaceChild(_10a,node);
};
dojo.dom.destroyNode=function(node){
if(node.parentNode){
node=dojo.dom.removeNode(node);
}
if(node.nodeType!=3){
if(dojo.evalObjPath("dojo.event.browser.clean",false)){
dojo.event.browser.clean(node);
}
if(dojo.render.html.ie){
node.outerHTML="";
}
}
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_10e,_10f){
var _110=[];
var _111=(_10e&&(_10e instanceof Function||typeof _10e=="function"));
while(node){
if(!_111||_10e(node)){
_110.push(node);
}
if(_10f&&_110.length>0){
return _110[0];
}
node=node.parentNode;
}
if(_10f){
return null;
}
return _110;
};
dojo.dom.getAncestorsByTag=function(node,tag,_114){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_114);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_119,_11a){
if(_11a&&node){
node=node.parentNode;
}
while(node){
if(node==_119){
return true;
}
node=node.parentNode;
}
return false;
};
dojo.dom.innerXML=function(node){
if(node.innerXML){
return node.innerXML;
}else{
if(node.xml){
return node.xml;
}else{
if(typeof XMLSerializer!="undefined"){
return (new XMLSerializer()).serializeToString(node);
}
}
}
};
dojo.dom.createDocument=function(){
var doc=null;
var _11d=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _11e=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_11e.length;i++){
try{
doc=new ActiveXObject(_11e[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_11d.implementation)&&(_11d.implementation.createDocument)){
doc=_11d.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_121){
if(!_121){
_121="text/xml";
}
if(!dj_undef("DOMParser")){
var _122=new DOMParser();
return _122.parseFromString(str,_121);
}else{
if(!dj_undef("ActiveXObject")){
var _123=dojo.dom.createDocument();
if(_123){
_123.async=false;
_123.loadXML(str);
return _123;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _124=dojo.doc();
if(_124.createElement){
var tmp=_124.createElement("xml");
tmp.innerHTML=str;
if(_124.implementation&&_124.implementation.createDocument){
var _126=_124.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_126.importNode(tmp.childNodes.item(i),true);
}
return _126;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_129){
if(_129.firstChild){
_129.insertBefore(node,_129.firstChild);
}else{
_129.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_12c){
if((_12c!=true)&&(node===ref||node.nextSibling===ref)){
return false;
}
var _12d=ref.parentNode;
_12d.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_130){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_130!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_130);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_134){
if((!node)||(!ref)||(!_134)){
return false;
}
switch(_134.toLowerCase()){
case "before":
return dojo.dom.insertBefore(node,ref);
case "after":
return dojo.dom.insertAfter(node,ref);
case "first":
if(ref.firstChild){
return dojo.dom.insertBefore(node,ref.firstChild);
}else{
ref.appendChild(node);
return true;
}
break;
default:
ref.appendChild(node);
return true;
}
};
dojo.dom.insertAtIndex=function(node,_136,_137){
var _138=_136.childNodes;
if(!_138.length||_138.length==_137){
_136.appendChild(node);
return true;
}
if(_137==0){
return dojo.dom.prependChild(node,_136);
}
return dojo.dom.insertAfter(node,_138[_137-1]);
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _13b=dojo.doc();
dojo.dom.replaceChildren(node,_13b.createTextNode(text));
return text;
}else{
if(node.textContent!=undefined){
return node.textContent;
}
var _13c="";
if(node==null){
return _13c;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
_13c+=dojo.dom.textContent(node.childNodes[i]);
break;
case 3:
case 2:
case 4:
_13c+=node.childNodes[i].nodeValue;
break;
default:
break;
}
}
return _13c;
}
};
dojo.dom.hasParent=function(node){
return Boolean(node&&node.parentNode&&dojo.dom.isNode(node.parentNode));
};
dojo.dom.isTag=function(node){
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName==String(arguments[i])){
return String(arguments[i]);
}
}
}
return "";
};
dojo.dom.setAttributeNS=function(elem,_142,_143,_144){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_142,_143,_144);
}else{
var _145=elem.ownerDocument;
var _146=_145.createNode(2,_143,_142);
_146.nodeValue=_144;
elem.setAttributeNode(_146);
}
};
dojo.provide("dojo.xml.Parse");
dojo.xml.Parse=function(){
var isIE=((dojo.render.html.capable)&&(dojo.render.html.ie));
function getTagName(node){
try{
return node.tagName.toLowerCase();
}
catch(e){
return "";
}
}
function getDojoTagName(node){
var _14a=getTagName(node);
if(!_14a){
return "";
}
if((dojo.widget)&&(dojo.widget.tags[_14a])){
return _14a;
}
var p=_14a.indexOf(":");
if(p>=0){
return _14a;
}
if(_14a.substr(0,5)=="dojo:"){
return _14a;
}
if(dojo.render.html.capable&&dojo.render.html.ie&&node.scopeName!="HTML"){
return node.scopeName.toLowerCase()+":"+_14a;
}
if(_14a.substr(0,4)=="dojo"){
return "dojo:"+_14a.substring(4);
}
var djt=node.getAttribute("dojoType")||node.getAttribute("dojotype");
if(djt){
if(djt.indexOf(":")<0){
djt="dojo:"+djt;
}
return djt.toLowerCase();
}
djt=node.getAttributeNS&&node.getAttributeNS(dojo.dom.dojoml,"type");
if(djt){
return "dojo:"+djt.toLowerCase();
}
try{
djt=node.getAttribute("dojo:type");
}
catch(e){
}
if(djt){
return "dojo:"+djt.toLowerCase();
}
if((dj_global["djConfig"])&&(!djConfig["ignoreClassNames"])){
var _14d=node.className||node.getAttribute("class");
if((_14d)&&(_14d.indexOf)&&(_14d.indexOf("dojo-")!=-1)){
var _14e=_14d.split(" ");
for(var x=0,c=_14e.length;x<c;x++){
if(_14e[x].slice(0,5)=="dojo-"){
return "dojo:"+_14e[x].substr(5).toLowerCase();
}
}
}
}
return "";
}
this.parseElement=function(node,_152,_153,_154){
var _155=getTagName(node);
if(isIE&&_155.indexOf("/")==0){
return null;
}
try{
var attr=node.getAttribute("parseWidgets");
if(attr&&attr.toLowerCase()=="false"){
return {};
}
}
catch(e){
}
var _157=true;
if(_153){
var _158=getDojoTagName(node);
_155=_158||_155;
_157=Boolean(_158);
}
var _159={};
_159[_155]=[];
var pos=_155.indexOf(":");
if(pos>0){
var ns=_155.substring(0,pos);
_159["ns"]=ns;
if((dojo.ns)&&(!dojo.ns.allow(ns))){
_157=false;
}
}
if(_157){
var _15c=this.parseAttributes(node);
for(var attr in _15c){
if((!_159[_155][attr])||(typeof _159[_155][attr]!="array")){
_159[_155][attr]=[];
}
_159[_155][attr].push(_15c[attr]);
}
_159[_155].nodeRef=node;
_159.tagName=_155;
_159.index=_154||0;
}
var _15d=0;
for(var i=0;i<node.childNodes.length;i++){
var tcn=node.childNodes.item(i);
switch(tcn.nodeType){
case dojo.dom.ELEMENT_NODE:
var ctn=getDojoTagName(tcn)||getTagName(tcn);
if(!_159[ctn]){
_159[ctn]=[];
}
_159[ctn].push(this.parseElement(tcn,true,_153,_15d));
if((tcn.childNodes.length==1)&&(tcn.childNodes.item(0).nodeType==dojo.dom.TEXT_NODE)){
_159[ctn][_159[ctn].length-1].value=tcn.childNodes.item(0).nodeValue;
}
_15d++;
break;
case dojo.dom.TEXT_NODE:
if(node.childNodes.length==1){
_159[_155].push({value:node.childNodes.item(0).nodeValue});
}
break;
default:
break;
}
}
return _159;
};
this.parseAttributes=function(node){
var _162={};
var atts=node.attributes;
var _164,i=0;
while((_164=atts[i++])){
if(isIE){
if(!_164){
continue;
}
if((typeof _164=="object")&&(typeof _164.nodeValue=="undefined")||(_164.nodeValue==null)||(_164.nodeValue=="")){
continue;
}
}
var nn=_164.nodeName.split(":");
nn=(nn.length==2)?nn[1]:_164.nodeName;
_162[nn]={value:_164.nodeValue};
}
return _162;
};
};
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_167,_168){
if(!dojo.lang.isFunction(_168)){
dojo.raise("dojo.inherits: superclass argument ["+_168+"] must be a function (subclass: ["+_167+"']");
}
_167.prototype=new _168();
_167.prototype.constructor=_167;
_167.superclass=_168.prototype;
_167["super"]=_168.prototype;
};
dojo.lang._mixin=function(obj,_16a){
var tobj={};
for(var x in _16a){
if((typeof tobj[x]=="undefined")||(tobj[x]!=_16a[x])){
obj[x]=_16a[x];
}
}
if(dojo.render.html.ie&&(typeof (_16a["toString"])=="function")&&(_16a["toString"]!=obj["toString"])&&(_16a["toString"]!=tobj["toString"])){
obj.toString=_16a.toString;
}
return obj;
};
dojo.lang.mixin=function(obj,_16e){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(obj,arguments[i]);
}
return obj;
};
dojo.lang.extend=function(_171,_172){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(_171.prototype,arguments[i]);
}
return _171;
};
dojo.inherits=dojo.lang.inherits;
dojo.mixin=dojo.lang.mixin;
dojo.extend=dojo.lang.extend;
dojo.lang.find=function(_175,_176,_177,_178){
if(!dojo.lang.isArrayLike(_175)&&dojo.lang.isArrayLike(_176)){
dojo.deprecated("dojo.lang.find(value, array)","use dojo.lang.find(array, value) instead","0.5");
var temp=_175;
_175=_176;
_176=temp;
}
var _17a=dojo.lang.isString(_175);
if(_17a){
_175=_175.split("");
}
if(_178){
var step=-1;
var i=_175.length-1;
var end=-1;
}else{
var step=1;
var i=0;
var end=_175.length;
}
if(_177){
while(i!=end){
if(_175[i]===_176){
return i;
}
i+=step;
}
}else{
while(i!=end){
if(_175[i]==_176){
return i;
}
i+=step;
}
}
return -1;
};
dojo.lang.indexOf=dojo.lang.find;
dojo.lang.findLast=function(_17e,_17f,_180){
return dojo.lang.find(_17e,_17f,_180,true);
};
dojo.lang.lastIndexOf=dojo.lang.findLast;
dojo.lang.inArray=function(_181,_182){
return dojo.lang.find(_181,_182)>-1;
};
dojo.lang.isObject=function(it){
if(typeof it=="undefined"){
return false;
}
return (typeof it=="object"||it===null||dojo.lang.isArray(it)||dojo.lang.isFunction(it));
};
dojo.lang.isArray=function(it){
return (it&&it instanceof Array||typeof it=="array");
};
dojo.lang.isArrayLike=function(it){
if((!it)||(dojo.lang.isUndefined(it))){
return false;
}
if(dojo.lang.isString(it)){
return false;
}
if(dojo.lang.isFunction(it)){
return false;
}
if(dojo.lang.isArray(it)){
return true;
}
if((it.tagName)&&(it.tagName.toLowerCase()=="form")){
return false;
}
if(dojo.lang.isNumber(it.length)&&isFinite(it.length)){
return true;
}
return false;
};
dojo.lang.isFunction=function(it){
return (it instanceof Function||typeof it=="function");
};
(function(){
if((dojo.render.html.capable)&&(dojo.render.html["safari"])){
dojo.lang.isFunction=function(it){
if((typeof (it)=="function")&&(it=="[object NodeList]")){
return false;
}
return (it instanceof Function||typeof it=="function");
};
}
})();
dojo.lang.isString=function(it){
return (typeof it=="string"||it instanceof String);
};
dojo.lang.isAlien=function(it){
if(!it){
return false;
}
return !dojo.lang.isFunction(it)&&/\{\s*\[native code\]\s*\}/.test(String(it));
};
dojo.lang.isBoolean=function(it){
return (it instanceof Boolean||typeof it=="boolean");
};
dojo.lang.isNumber=function(it){
return (it instanceof Number||typeof it=="number");
};
dojo.lang.isUndefined=function(it){
return ((typeof (it)=="undefined")&&(it==undefined));
};
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_18d,_18e){
var fcn=(dojo.lang.isString(_18e)?_18d[_18e]:_18e)||function(){
};
return function(){
return fcn.apply(_18d,arguments);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_190,_191,_192){
var nso=(_191||dojo.lang.anon);
if((_192)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_190){
return x;
}
}
catch(e){
}
}
}
var ret="__"+dojo.lang.anonCtr++;
while(typeof nso[ret]!="undefined"){
ret="__"+dojo.lang.anonCtr++;
}
nso[ret]=_190;
return ret;
};
dojo.lang.forward=function(_196){
return function(){
return this[_196].apply(this,arguments);
};
};
dojo.lang.curry=function(_197,func){
var _199=[];
_197=_197||dj_global;
if(dojo.lang.isString(func)){
func=_197[func];
}
for(var x=2;x<arguments.length;x++){
_199.push(arguments[x]);
}
var _19b=(func["__preJoinArity"]||func.length)-_199.length;
function gather(_19c,_19d,_19e){
var _19f=_19e;
var _1a0=_19d.slice(0);
for(var x=0;x<_19c.length;x++){
_1a0.push(_19c[x]);
}
_19e=_19e-_19c.length;
if(_19e<=0){
var res=func.apply(_197,_1a0);
_19e=_19f;
return res;
}else{
return function(){
return gather(arguments,_1a0,_19e);
};
}
}
return gather([],_199,_19b);
};
dojo.lang.curryArguments=function(_1a3,func,args,_1a6){
var _1a7=[];
var x=_1a6||0;
for(x=_1a6;x<args.length;x++){
_1a7.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[_1a3,func].concat(_1a7));
};
dojo.lang.tryThese=function(){
for(var x=0;x<arguments.length;x++){
try{
if(typeof arguments[x]=="function"){
var ret=(arguments[x]());
if(ret){
return ret;
}
}
}
catch(e){
dojo.debug(e);
}
}
};
dojo.lang.delayThese=function(farr,cb,_1ad,_1ae){
if(!farr.length){
if(typeof _1ae=="function"){
_1ae();
}
return;
}
if((typeof _1ad=="undefined")&&(typeof cb=="number")){
_1ad=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_1ad){
_1ad=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_1ad,_1ae);
},_1ad);
};
dojo.provide("dojo.lang.array");
dojo.lang.mixin(dojo.lang,{has:function(obj,name){
try{
return typeof obj[name]!="undefined";
}
catch(e){
return false;
}
},isEmpty:function(obj){
if(dojo.lang.isObject(obj)){
var tmp={};
var _1b3=0;
for(var x in obj){
if(obj[x]&&(!tmp[x])){
_1b3++;
break;
}
}
return _1b3==0;
}else{
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length==0;
}
}
},map:function(arr,obj,_1b7){
var _1b8=dojo.lang.isString(arr);
if(_1b8){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_1b7)){
_1b7=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_1b7){
var _1b9=obj;
obj=_1b7;
_1b7=_1b9;
}
}
if(Array.map){
var _1ba=Array.map(arr,_1b7,obj);
}else{
var _1ba=[];
for(var i=0;i<arr.length;++i){
_1ba.push(_1b7.call(obj,arr[i]));
}
}
if(_1b8){
return _1ba.join("");
}else{
return _1ba;
}
},reduce:function(arr,_1bd,obj,_1bf){
var _1c0=_1bd;
if(arguments.length==2){
_1bf=_1bd;
_1c0=arr[0];
arr=arr.slice(1);
}else{
if(arguments.length==3){
if(dojo.lang.isFunction(obj)){
_1bf=obj;
obj=null;
}
}else{
if(dojo.lang.isFunction(obj)){
var tmp=_1bf;
_1bf=obj;
obj=tmp;
}
}
}
var ob=obj||dj_global;
dojo.lang.map(arr,function(val){
_1c0=_1bf.call(ob,_1c0,val);
});
return _1c0;
},forEach:function(_1c4,_1c5,_1c6){
if(dojo.lang.isString(_1c4)){
_1c4=_1c4.split("");
}
if(Array.forEach){
Array.forEach(_1c4,_1c5,_1c6);
}else{
if(!_1c6){
_1c6=dj_global;
}
for(var i=0,l=_1c4.length;i<l;i++){
_1c5.call(_1c6,_1c4[i],i,_1c4);
}
}
},_everyOrSome:function(_1c9,arr,_1cb,_1cc){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[_1c9?"every":"some"](arr,_1cb,_1cc);
}else{
if(!_1cc){
_1cc=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _1cf=_1cb.call(_1cc,arr[i],i,arr);
if(_1c9&&!_1cf){
return false;
}else{
if((!_1c9)&&(_1cf)){
return true;
}
}
}
return Boolean(_1c9);
}
},every:function(arr,_1d1,_1d2){
return this._everyOrSome(true,arr,_1d1,_1d2);
},some:function(arr,_1d4,_1d5){
return this._everyOrSome(false,arr,_1d4,_1d5);
},filter:function(arr,_1d7,_1d8){
var _1d9=dojo.lang.isString(arr);
if(_1d9){
arr=arr.split("");
}
var _1da;
if(Array.filter){
_1da=Array.filter(arr,_1d7,_1d8);
}else{
if(!_1d8){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_1d8=dj_global;
}
_1da=[];
for(var i=0;i<arr.length;i++){
if(_1d7.call(_1d8,arr[i],i,arr)){
_1da.push(arr[i]);
}
}
}
if(_1d9){
return _1da.join("");
}else{
return _1da;
}
},unnest:function(){
var out=[];
for(var i=0;i<arguments.length;i++){
if(dojo.lang.isArrayLike(arguments[i])){
var add=dojo.lang.unnest.apply(this,arguments[i]);
out=out.concat(add);
}else{
out.push(arguments[i]);
}
}
return out;
},toArray:function(_1df,_1e0){
var _1e1=[];
for(var i=_1e0||0;i<_1df.length;i++){
_1e1.push(_1df[i]);
}
return _1e1;
}});
dojo.provide("dojo.lang.extras");
dojo.lang.setTimeout=function(func,_1e4){
var _1e5=window,_1e6=2;
if(!dojo.lang.isFunction(func)){
_1e5=func;
func=_1e4;
_1e4=arguments[2];
_1e6++;
}
if(dojo.lang.isString(func)){
func=_1e5[func];
}
var args=[];
for(var i=_1e6;i<arguments.length;i++){
args.push(arguments[i]);
}
return dojo.global().setTimeout(function(){
func.apply(_1e5,args);
},_1e4);
};
dojo.lang.clearTimeout=function(_1e9){
dojo.global().clearTimeout(_1e9);
};
dojo.lang.getNameInObj=function(ns,item){
if(!ns){
ns=dj_global;
}
for(var x in ns){
if(ns[x]===item){
return new String(x);
}
}
return null;
};
dojo.lang.shallowCopy=function(obj,deep){
var i,ret;
if(obj===null){
return null;
}
if(dojo.lang.isObject(obj)){
ret=new obj.constructor();
for(i in obj){
if(dojo.lang.isUndefined(ret[i])){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}
}else{
if(dojo.lang.isArray(obj)){
ret=[];
for(i=0;i<obj.length;i++){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}else{
ret=obj;
}
}
return ret;
};
dojo.lang.firstValued=function(){
for(var i=0;i<arguments.length;i++){
if(typeof arguments[i]!="undefined"){
return arguments[i];
}
}
return undefined;
};
dojo.lang.getObjPathValue=function(_1f2,_1f3,_1f4){
with(dojo.parseObjPath(_1f2,_1f3,_1f4)){
return dojo.evalProp(prop,obj,_1f4);
}
};
dojo.lang.setObjPathValue=function(_1f5,_1f6,_1f7,_1f8){
dojo.deprecated("dojo.lang.setObjPathValue","use dojo.parseObjPath and the '=' operator","0.6");
if(arguments.length<4){
_1f8=true;
}
with(dojo.parseObjPath(_1f5,_1f7,_1f8)){
if(obj&&(_1f8||(prop in obj))){
obj[prop]=_1f6;
}
}
};
dojo.provide("dojo.lang.declare");
dojo.lang.declare=function(_1f9,_1fa,init,_1fc){
if((dojo.lang.isFunction(_1fc))||((!_1fc)&&(!dojo.lang.isFunction(init)))){
var temp=_1fc;
_1fc=init;
init=temp;
}
var _1fe=[];
if(dojo.lang.isArray(_1fa)){
_1fe=_1fa;
_1fa=_1fe.shift();
}
if(!init){
init=dojo.evalObjPath(_1f9,false);
if((init)&&(!dojo.lang.isFunction(init))){
init=null;
}
}
var ctor=dojo.lang.declare._makeConstructor();
var scp=(_1fa?_1fa.prototype:null);
if(scp){
scp.prototyping=true;
ctor.prototype=new _1fa();
scp.prototyping=false;
}
ctor.superclass=scp;
ctor.mixins=_1fe;
for(var i=0,l=_1fe.length;i<l;i++){
dojo.lang.extend(ctor,_1fe[i].prototype);
}
ctor.prototype.initializer=null;
ctor.prototype.declaredClass=_1f9;
if(dojo.lang.isArray(_1fc)){
dojo.lang.extend.apply(dojo.lang,[ctor].concat(_1fc));
}else{
dojo.lang.extend(ctor,(_1fc)||{});
}
dojo.lang.extend(ctor,dojo.lang.declare._common);
ctor.prototype.constructor=ctor;
ctor.prototype.initializer=(ctor.prototype.initializer)||(init)||(function(){
});
var _203=dojo.parseObjPath(_1f9,null,true);
_203.obj[_203.prop]=ctor;
return ctor;
};
dojo.lang.declare._makeConstructor=function(){
return function(){
var self=this._getPropContext();
var s=self.constructor.superclass;
if((s)&&(s.constructor)){
if(s.constructor==arguments.callee){
this._inherited("constructor",arguments);
}else{
this._contextMethod(s,"constructor",arguments);
}
}
var ms=(self.constructor.mixins)||([]);
for(var i=0,m;(m=ms[i]);i++){
(((m.prototype)&&(m.prototype.initializer))||(m)).apply(this,arguments);
}
if((!this.prototyping)&&(self.initializer)){
self.initializer.apply(this,arguments);
}
};
};
dojo.lang.declare._common={_getPropContext:function(){
return (this.___proto||this);
},_contextMethod:function(_209,_20a,args){
var _20c,_20d=this.___proto;
this.___proto=_209;
try{
_20c=_209[_20a].apply(this,(args||[]));
}
catch(e){
throw e;
}
finally{
this.___proto=_20d;
}
return _20c;
},_inherited:function(prop,args){
var p=this._getPropContext();
do{
if((!p.constructor)||(!p.constructor.superclass)){
return;
}
p=p.constructor.superclass;
}while(!(prop in p));
return (dojo.lang.isFunction(p[prop])?this._contextMethod(p,prop,args):p[prop]);
},inherited:function(prop,args){
dojo.deprecated("'inherited' method is dangerous, do not up-call! 'inherited' is slated for removal in 0.5; name your super class (or use superclass property) instead.","0.5");
this._inherited(prop,args);
}};
dojo.declare=dojo.lang.declare;
dojo.provide("dojo.ns");
dojo.ns={namespaces:{},failed:{},loading:{},loaded:{},register:function(name,_214,_215,_216){
if(!_216||!this.namespaces[name]){
this.namespaces[name]=new dojo.ns.Ns(name,_214,_215);
}
},allow:function(name){
if(this.failed[name]){
return false;
}
if((djConfig.excludeNamespace)&&(dojo.lang.inArray(djConfig.excludeNamespace,name))){
return false;
}
return ((name==this.dojo)||(!djConfig.includeNamespace)||(dojo.lang.inArray(djConfig.includeNamespace,name)));
},get:function(name){
return this.namespaces[name];
},require:function(name){
var ns=this.namespaces[name];
if((ns)&&(this.loaded[name])){
return ns;
}
if(!this.allow(name)){
return false;
}
if(this.loading[name]){
dojo.debug("dojo.namespace.require: re-entrant request to load namespace \""+name+"\" must fail.");
return false;
}
var req=dojo.require;
this.loading[name]=true;
try{
if(name=="dojo"){
req("dojo.namespaces.dojo");
}else{
if(!dojo.hostenv.moduleHasPrefix(name)){
dojo.registerModulePath(name,"../"+name);
}
req([name,"manifest"].join("."),false,true);
}
if(!this.namespaces[name]){
this.failed[name]=true;
}
}
finally{
this.loading[name]=false;
}
return this.namespaces[name];
}};
dojo.ns.Ns=function(name,_21d,_21e){
this.name=name;
this.module=_21d;
this.resolver=_21e;
this._loaded=[];
this._failed=[];
};
dojo.ns.Ns.prototype.resolve=function(name,_220,_221){
if(!this.resolver||djConfig["skipAutoRequire"]){
return false;
}
var _222=this.resolver(name,_220);
if((_222)&&(!this._loaded[_222])&&(!this._failed[_222])){
var req=dojo.require;
req(_222,false,true);
if(dojo.hostenv.findModule(_222,false)){
this._loaded[_222]=true;
}else{
if(!_221){
dojo.raise("dojo.ns.Ns.resolve: module '"+_222+"' not found after loading via namespace '"+this.name+"'");
}
this._failed[_222]=true;
}
}
return Boolean(this._loaded[_222]);
};
dojo.registerNamespace=function(name,_225,_226){
dojo.ns.register.apply(dojo.ns,arguments);
};
dojo.registerNamespaceResolver=function(name,_228){
var n=dojo.ns.namespaces[name];
if(n){
n.resolver=_228;
}
};
dojo.registerNamespaceManifest=function(_22a,path,name,_22d,_22e){
dojo.registerModulePath(name,path);
dojo.registerNamespace(name,_22d,_22e);
};
dojo.registerNamespace("dojo","dojo.widget");
dojo.provide("dojo.event.common");
dojo.event=new function(){
this._canTimeout=dojo.lang.isFunction(dj_global["setTimeout"])||dojo.lang.isAlien(dj_global["setTimeout"]);
function interpolateArgs(args,_230){
var dl=dojo.lang;
var ao={srcObj:dj_global,srcFunc:null,adviceObj:dj_global,adviceFunc:null,aroundObj:null,aroundFunc:null,adviceType:(args.length>2)?args[0]:"after",precedence:"last",once:false,delay:null,rate:0,adviceMsg:false,maxCalls:-1};
switch(args.length){
case 0:
return;
case 1:
return;
case 2:
ao.srcFunc=args[0];
ao.adviceFunc=args[1];
break;
case 3:
if((dl.isObject(args[0]))&&(dl.isString(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
}else{
if((dl.isString(args[1]))&&(dl.isString(args[2]))){
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
}else{
if((dl.isObject(args[0]))&&(dl.isString(args[1]))&&(dl.isFunction(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
var _233=dl.nameAnonFunc(args[2],ao.adviceObj,_230);
ao.adviceFunc=_233;
}else{
if((dl.isFunction(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=dj_global;
var _233=dl.nameAnonFunc(args[0],ao.srcObj,_230);
ao.srcFunc=_233;
ao.adviceObj=args[1];
ao.adviceFunc=args[2];
}
}
}
}
break;
case 4:
if((dl.isObject(args[0]))&&(dl.isObject(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isString(args[1]))&&(dl.isObject(args[2]))){
ao.adviceType=args[0];
ao.srcObj=dj_global;
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isFunction(args[1]))&&(dl.isObject(args[2]))){
ao.adviceType=args[0];
ao.srcObj=dj_global;
var _233=dl.nameAnonFunc(args[1],dj_global,_230);
ao.srcFunc=_233;
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))&&(dl.isFunction(args[3]))){
ao.srcObj=args[1];
ao.srcFunc=args[2];
var _233=dl.nameAnonFunc(args[3],dj_global,_230);
ao.adviceObj=dj_global;
ao.adviceFunc=_233;
}else{
if(dl.isObject(args[1])){
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=dj_global;
ao.adviceFunc=args[3];
}else{
if(dl.isObject(args[2])){
ao.srcObj=dj_global;
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
ao.srcObj=ao.adviceObj=ao.aroundObj=dj_global;
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
ao.aroundFunc=args[3];
}
}
}
}
}
}
break;
case 6:
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=args[3];
ao.adviceFunc=args[4];
ao.aroundFunc=args[5];
ao.aroundObj=dj_global;
break;
default:
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=args[3];
ao.adviceFunc=args[4];
ao.aroundObj=args[5];
ao.aroundFunc=args[6];
ao.once=args[7];
ao.delay=args[8];
ao.rate=args[9];
ao.adviceMsg=args[10];
ao.maxCalls=(!isNaN(parseInt(args[11])))?args[11]:-1;
break;
}
if(dl.isFunction(ao.aroundFunc)){
var _233=dl.nameAnonFunc(ao.aroundFunc,ao.aroundObj,_230);
ao.aroundFunc=_233;
}
if(dl.isFunction(ao.srcFunc)){
ao.srcFunc=dl.getNameInObj(ao.srcObj,ao.srcFunc);
}
if(dl.isFunction(ao.adviceFunc)){
ao.adviceFunc=dl.getNameInObj(ao.adviceObj,ao.adviceFunc);
}
if((ao.aroundObj)&&(dl.isFunction(ao.aroundFunc))){
ao.aroundFunc=dl.getNameInObj(ao.aroundObj,ao.aroundFunc);
}
if(!ao.srcObj){
dojo.raise("bad srcObj for srcFunc: "+ao.srcFunc);
}
if(!ao.adviceObj){
dojo.raise("bad adviceObj for adviceFunc: "+ao.adviceFunc);
}
if(!ao.adviceFunc){
dojo.debug("bad adviceFunc for srcFunc: "+ao.srcFunc);
dojo.debugShallow(ao);
}
return ao;
}
this.connect=function(){
if(arguments.length==1){
var ao=arguments[0];
}else{
var ao=interpolateArgs(arguments,true);
}
if(dojo.lang.isArray(ao.srcObj)&&ao.srcObj!=""){
var _235={};
for(var x in ao){
_235[x]=ao[x];
}
var mjps=[];
dojo.lang.forEach(ao.srcObj,function(src){
if((dojo.render.html.capable)&&(dojo.lang.isString(src))){
src=dojo.byId(src);
}
_235.srcObj=src;
mjps.push(dojo.event.connect.call(dojo.event,_235));
});
return mjps;
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc);
if(ao.adviceFunc){
var mjp2=dojo.event.MethodJoinPoint.getForMethod(ao.adviceObj,ao.adviceFunc);
}
mjp.kwAddAdvice(ao);
return mjp;
};
this.log=function(a1,a2){
var _23d;
if((arguments.length==1)&&(typeof a1=="object")){
_23d=a1;
}else{
_23d={srcObj:a1,srcFunc:a2};
}
_23d.adviceFunc=function(){
var _23e=[];
for(var x=0;x<arguments.length;x++){
_23e.push(arguments[x]);
}
dojo.debug("("+_23d.srcObj+")."+_23d.srcFunc,":",_23e.join(", "));
};
this.kwConnect(_23d);
};
this.connectBefore=function(){
var args=["before"];
for(var i=0;i<arguments.length;i++){
args.push(arguments[i]);
}
return this.connect.apply(this,args);
};
this.connectAround=function(){
var args=["around"];
for(var i=0;i<arguments.length;i++){
args.push(arguments[i]);
}
return this.connect.apply(this,args);
};
this.connectOnce=function(){
var ao=interpolateArgs(arguments,true);
ao.once=true;
return this.connect(ao);
};
this.connectRunOnce=function(){
var ao=interpolateArgs(arguments,true);
ao.maxCalls=1;
return this.connect(ao);
};
this._kwConnectImpl=function(_246,_247){
var fn=(_247)?"disconnect":"connect";
if(typeof _246["srcFunc"]=="function"){
_246.srcObj=_246["srcObj"]||dj_global;
var _249=dojo.lang.nameAnonFunc(_246.srcFunc,_246.srcObj,true);
_246.srcFunc=_249;
}
if(typeof _246["adviceFunc"]=="function"){
_246.adviceObj=_246["adviceObj"]||dj_global;
var _249=dojo.lang.nameAnonFunc(_246.adviceFunc,_246.adviceObj,true);
_246.adviceFunc=_249;
}
_246.srcObj=_246["srcObj"]||dj_global;
_246.adviceObj=_246["adviceObj"]||_246["targetObj"]||dj_global;
_246.adviceFunc=_246["adviceFunc"]||_246["targetFunc"];
return dojo.event[fn](_246);
};
this.kwConnect=function(_24a){
return this._kwConnectImpl(_24a,false);
};
this.disconnect=function(){
if(arguments.length==1){
var ao=arguments[0];
}else{
var ao=interpolateArgs(arguments,true);
}
if(!ao.adviceFunc){
return;
}
if(dojo.lang.isString(ao.srcFunc)&&(ao.srcFunc.toLowerCase()=="onkey")){
if(dojo.render.html.ie){
ao.srcFunc="onkeydown";
this.disconnect(ao);
}
ao.srcFunc="onkeypress";
}
if(!ao.srcObj[ao.srcFunc]){
return null;
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc,true);
mjp.removeAdvice(ao.adviceObj,ao.adviceFunc,ao.adviceType,ao.once);
return mjp;
};
this.kwDisconnect=function(_24d){
return this._kwConnectImpl(_24d,true);
};
};
dojo.event.MethodInvocation=function(_24e,obj,args){
this.jp_=_24e;
this.object=obj;
this.args=[];
for(var x=0;x<args.length;x++){
this.args[x]=args[x];
}
this.around_index=-1;
};
dojo.event.MethodInvocation.prototype.proceed=function(){
this.around_index++;
if(this.around_index>=this.jp_.around.length){
return this.jp_.object[this.jp_.methodname].apply(this.jp_.object,this.args);
}else{
var ti=this.jp_.around[this.around_index];
var mobj=ti[0]||dj_global;
var meth=ti[1];
return mobj[meth].call(mobj,this);
}
};
dojo.event.MethodJoinPoint=function(obj,_256){
this.object=obj||dj_global;
this.methodname=_256;
this.methodfunc=this.object[_256];
this.squelch=false;
};
dojo.event.MethodJoinPoint.getForMethod=function(obj,_258){
if(!obj){
obj=dj_global;
}
var ofn=obj[_258];
if(!ofn){
ofn=obj[_258]=function(){
};
if(!obj[_258]){
dojo.raise("Cannot set do-nothing method on that object "+_258);
}
}else{
if((typeof ofn!="function")&&(!dojo.lang.isFunction(ofn))&&(!dojo.lang.isAlien(ofn))){
return null;
}
}
var _25a=_258+"$joinpoint";
var _25b=_258+"$joinpoint$method";
var _25c=obj[_25a];
if(!_25c){
var _25d=false;
if(dojo.event["browser"]){
if((obj["attachEvent"])||(obj["nodeType"])||(obj["addEventListener"])){
_25d=true;
dojo.event.browser.addClobberNodeAttrs(obj,[_25a,_25b,_258]);
}
}
var _25e=ofn.length;
obj[_25b]=ofn;
_25c=obj[_25a]=new dojo.event.MethodJoinPoint(obj,_25b);
if(!_25d){
obj[_258]=function(){
return _25c.run.apply(_25c,arguments);
};
}else{
obj[_258]=function(){
var args=[];
if(!arguments.length){
var evt=null;
try{
if(obj.ownerDocument){
evt=obj.ownerDocument.parentWindow.event;
}else{
if(obj.documentElement){
evt=obj.documentElement.ownerDocument.parentWindow.event;
}else{
if(obj.event){
evt=obj.event;
}else{
evt=window.event;
}
}
}
}
catch(e){
evt=window.event;
}
if(evt){
args.push(dojo.event.browser.fixEvent(evt,this));
}
}else{
try {
for(var x=0;x<arguments.length;x++){
if((x==0)&&(dojo.event.browser.isEvent(arguments[x]))){
args.push(dojo.event.browser.fixEvent(arguments[x],this));
}else{
args.push(arguments[x]);
}
}
} catch (ignored) {}
}
return _25c.run.apply(_25c,args);
};
}
obj[_258].__preJoinArity=_25e;
}
return _25c;
};
dojo.lang.extend(dojo.event.MethodJoinPoint,{squelch:false,unintercept:function(){
this.object[this.methodname]=this.methodfunc;
this.before=[];
this.after=[];
this.around=[];
},disconnect:dojo.lang.forward("unintercept"),run:function(){
var obj=this.object||dj_global;
var args=arguments;
var _264=[];
for(var x=0;x<args.length;x++){
_264[x]=args[x];
}
var _266=function(marr){
if(!marr){
dojo.debug("Null argument to unrollAdvice()");
return;
}
var _268=marr[0]||dj_global;
var _269=marr[1];
if(!_268[_269]){
dojo.raise("function \""+_269+"\" does not exist on \""+_268+"\"");
}
var _26a=marr[2]||dj_global;
var _26b=marr[3];
var msg=marr[6];
var _26d=marr[7];
if(_26d>-1){
if(_26d==0){
return;
}
marr[7]--;
}
var _26e;
var to={args:[],jp_:this,object:obj,proceed:function(){
return _268[_269].apply(_268,to.args);
}};
to.args=_264;
var _270=parseInt(marr[4]);
var _271=((!isNaN(_270))&&(marr[4]!==null)&&(typeof marr[4]!="undefined"));
if(marr[5]){
var rate=parseInt(marr[5]);
var cur=new Date();
var _274=false;
if((marr["last"])&&((cur-marr.last)<=rate)){
if(dojo.event._canTimeout){
if(marr["delayTimer"]){
clearTimeout(marr.delayTimer);
}
var tod=parseInt(rate*2);
var mcpy=dojo.lang.shallowCopy(marr);
marr.delayTimer=setTimeout(function(){
mcpy[5]=0;
_266(mcpy);
},tod);
}
return;
}else{
marr.last=cur;
}
}
if(_26b){
_26a[_26b].call(_26a,to);
}else{
if((_271)&&((dojo.render.html)||(dojo.render.svg))){
dj_global["setTimeout"](function(){
if(msg){
_268[_269].call(_268,to);
}else{
_268[_269].apply(_268,args);
}
},_270);
}else{
if(msg){
_268[_269].call(_268,to);
}else{
_268[_269].apply(_268,args);
}
}
}
};
var _277=function(){
if(this.squelch){
try{
return _266.apply(this,arguments);
}
catch(e){
dojo.debug(e);
}
}else{
return _266.apply(this,arguments);
}
};
if((this["before"])&&(this.before.length>0)){
dojo.lang.forEach(this.before.concat(new Array()),_277);
}
var _278;
try{
if((this["around"])&&(this.around.length>0)){
var mi=new dojo.event.MethodInvocation(this,obj,args);
_278=mi.proceed();
}else{
if(this.methodfunc){
_278=this.object[this.methodname].apply(this.object,args);
}
}
}
catch(e){
if(!this.squelch){
dojo.debug(e,"when calling",this.methodname,"on",this.object,"with arguments",args);
dojo.raise(e);
}
}
if((this["after"])&&(this.after.length>0)){
dojo.lang.forEach(this.after.concat(new Array()),_277);
}
return (this.methodfunc)?_278:null;
},getArr:function(kind){
var type="after";
if((typeof kind=="string")&&(kind.indexOf("before")!=-1)){
type="before";
}else{
if(kind=="around"){
type="around";
}
}
if(!this[type]){
this[type]=[];
}
return this[type];
},kwAddAdvice:function(args){
this.addAdvice(args["adviceObj"],args["adviceFunc"],args["aroundObj"],args["aroundFunc"],args["adviceType"],args["precedence"],args["once"],args["delay"],args["rate"],args["adviceMsg"],args["maxCalls"]);
},addAdvice:function(_27d,_27e,_27f,_280,_281,_282,once,_284,rate,_286,_287){
var arr=this.getArr(_281);
if(!arr){
dojo.raise("bad this: "+this);
}
var ao=[_27d,_27e,_27f,_280,_284,rate,_286,_287];
if(once){
if(this.hasAdvice(_27d,_27e,_281,arr)>=0){
return;
}
}
if(_282=="first"){
arr.unshift(ao);
}else{
arr.push(ao);
}
},hasAdvice:function(_28a,_28b,_28c,arr){
if(!arr){
arr=this.getArr(_28c);
}
var ind=-1;
for(var x=0;x<arr.length;x++){
var aao=(typeof _28b=="object")?(new String(_28b)).toString():_28b;
var a1o=(typeof arr[x][1]=="object")?(new String(arr[x][1])).toString():arr[x][1];
if((arr[x][0]==_28a)&&(a1o==aao)){
ind=x;
}
}
return ind;
},removeAdvice:function(_292,_293,_294,once){
var arr=this.getArr(_294);
var ind=this.hasAdvice(_292,_293,_294,arr);
if(ind==-1){
return false;
}
while(ind!=-1){
arr.splice(ind,1);
if(once){
break;
}
ind=this.hasAdvice(_292,_293,_294,arr);
}
return true;
}});
dojo.provide("dojo.event.topic");
dojo.event.topic=new function(){
this.topics={};
this.getTopic=function(_298){
if(!this.topics[_298]){
this.topics[_298]=new this.TopicImpl(_298);
}
return this.topics[_298];
};
this.registerPublisher=function(_299,obj,_29b){
var _299=this.getTopic(_299);
_299.registerPublisher(obj,_29b);
};
this.subscribe=function(_29c,obj,_29e){
var _29c=this.getTopic(_29c);
_29c.subscribe(obj,_29e);
};
this.unsubscribe=function(_29f,obj,_2a1){
var _29f=this.getTopic(_29f);
_29f.unsubscribe(obj,_2a1);
};
this.destroy=function(_2a2){
this.getTopic(_2a2).destroy();
delete this.topics[_2a2];
};
this.publishApply=function(_2a3,args){
var _2a3=this.getTopic(_2a3);
_2a3.sendMessage.apply(_2a3,args);
};
this.publish=function(_2a5,_2a6){
var _2a5=this.getTopic(_2a5);
var args=[];
for(var x=1;x<arguments.length;x++){
args.push(arguments[x]);
}
_2a5.sendMessage.apply(_2a5,args);
};
};
dojo.event.topic.TopicImpl=function(_2a9){
this.topicName=_2a9;
this.subscribe=function(_2aa,_2ab){
var tf=_2ab||_2aa;
var to=(!_2ab)?dj_global:_2aa;
return dojo.event.kwConnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.unsubscribe=function(_2ae,_2af){
var tf=(!_2af)?_2ae:_2af;
var to=(!_2af)?null:_2ae;
return dojo.event.kwDisconnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this._getJoinPoint=function(){
return dojo.event.MethodJoinPoint.getForMethod(this,"sendMessage");
};
this.setSquelch=function(_2b2){
this._getJoinPoint().squelch=_2b2;
};
this.destroy=function(){
this._getJoinPoint().disconnect();
};
this.registerPublisher=function(_2b3,_2b4){
dojo.event.connect(_2b3,_2b4,this,"sendMessage");
};
this.sendMessage=function(_2b5){
};
};
dojo.provide("dojo.event.browser");
dojo._ie_clobber=new function(){
this.clobberNodes=[];
function nukeProp(node,prop){
try{
node[prop]=null;
}
catch(e){
}
try{
delete node[prop];
}
catch(e){
}
try{
node.removeAttribute(prop);
}
catch(e){
}
}
this.clobber=function(_2b8){
var na;
var tna;
if(_2b8){
tna=_2b8.all||_2b8.getElementsByTagName("*");
na=[_2b8];
for(var x=0;x<tna.length;x++){
if(tna[x]["__doClobber__"]){
na.push(tna[x]);
}
}
}else{
try{
window.onload=null;
}
catch(e){
}
na=(this.clobberNodes.length)?this.clobberNodes:document.all;
}
tna=null;
var _2bc={};
for(var i=na.length-1;i>=0;i=i-1){
var el=na[i];
try{
if(el&&el["__clobberAttrs__"]){
for(var j=0;j<el.__clobberAttrs__.length;j++){
nukeProp(el,el.__clobberAttrs__[j]);
}
nukeProp(el,"__clobberAttrs__");
nukeProp(el,"__doClobber__");
}
}
catch(e){
}
}
na=null;
};
};
if(dojo.render.html.ie){
dojo.addOnUnload(function(){
dojo._ie_clobber.clobber();
try{
if((dojo["widget"])&&(dojo.widget["manager"])){
dojo.widget.manager.destroyAll();
}
}
catch(e){
}
if(dojo.widget){
for(var name in dojo.widget._templateCache){
if(dojo.widget._templateCache[name].node){
dojo.dom.destroyNode(dojo.widget._templateCache[name].node);
dojo.widget._templateCache[name].node=null;
delete dojo.widget._templateCache[name].node;
}
}
}
try{
window.onload=null;
}
catch(e){
}
try{
window.onunload=null;
}
catch(e){
}
dojo._ie_clobber.clobberNodes=[];
});
}
dojo.event.browser=new function(){
var _2c1=0;
this.normalizedEventName=function(_2c2){
switch(_2c2){
case "CheckboxStateChange":
case "DOMAttrModified":
case "DOMMenuItemActive":
case "DOMMenuItemInactive":
case "DOMMouseScroll":
case "DOMNodeInserted":
case "DOMNodeRemoved":
case "RadioStateChange":
return _2c2;
break;
default:
var lcn=_2c2.toLowerCase();
return (lcn.indexOf("on")==0)?lcn.substr(2):lcn;
break;
}
};
this.clean=function(node){
if(dojo.render.html.ie){
dojo._ie_clobber.clobber(node);
}
};
this.addClobberNode=function(node){
if(!dojo.render.html.ie){
return;
}
if(!node["__doClobber__"]){
node.__doClobber__=true;
dojo._ie_clobber.clobberNodes.push(node);
node.__clobberAttrs__=[];
}
};
this.addClobberNodeAttrs=function(node,_2c7){
if(!dojo.render.html.ie){
return;
}
this.addClobberNode(node);
for(var x=0;x<_2c7.length;x++){
node.__clobberAttrs__.push(_2c7[x]);
}
};
this.removeListener=function(node,_2ca,fp,_2cc){
if(!_2cc){
var _2cc=false;
}
_2ca=dojo.event.browser.normalizedEventName(_2ca);
if(_2ca=="key"){
if(dojo.render.html.ie){
this.removeListener(node,"onkeydown",fp,_2cc);
}
_2ca="keypress";
}
if(node.removeEventListener){
node.removeEventListener(_2ca,fp,_2cc);
}
};
this.addListener=function(node,_2ce,fp,_2d0,_2d1){
if(!node){
return;
}
if(!_2d0){
var _2d0=false;
}
_2ce=dojo.event.browser.normalizedEventName(_2ce);
if(_2ce=="key"){
if(dojo.render.html.ie){
this.addListener(node,"onkeydown",fp,_2d0,_2d1);
}
_2ce="keypress";
}
if(!_2d1){
var _2d2=function(evt){
if(!evt){
evt=window.event;
}
var ret=fp(dojo.event.browser.fixEvent(evt,this));
if(_2d0){
dojo.event.browser.stopEvent(evt);
}
return ret;
};
}else{
_2d2=fp;
}
if(node.addEventListener){
node.addEventListener(_2ce,_2d2,_2d0);
return _2d2;
}else{
_2ce="on"+_2ce;
if(typeof node[_2ce]=="function"){
var _2d5=node[_2ce];
node[_2ce]=function(e){
_2d5(e);
return _2d2(e);
};
}else{
node[_2ce]=_2d2;
}
if(dojo.render.html.ie){
this.addClobberNodeAttrs(node,[_2ce]);
}
return _2d2;
}
};
this.isEvent=function(obj){
return (typeof obj!="undefined")&&(obj)&&(typeof Event!="undefined")&&(obj.eventPhase);
};
this.currentEvent=null;
this.callListener=function(_2d8,_2d9){
if(typeof _2d8!="function"){
dojo.raise("listener not a function: "+_2d8);
}
dojo.event.browser.currentEvent.currentTarget=_2d9;
return _2d8.call(_2d9,dojo.event.browser.currentEvent);
};
this._stopPropagation=function(){
dojo.event.browser.currentEvent.cancelBubble=true;
};
this._preventDefault=function(){
dojo.event.browser.currentEvent.returnValue=false;
};
this.keys={KEY_BACKSPACE:8,KEY_TAB:9,KEY_CLEAR:12,KEY_ENTER:13,KEY_SHIFT:16,KEY_CTRL:17,KEY_ALT:18,KEY_PAUSE:19,KEY_CAPS_LOCK:20,KEY_ESCAPE:27,KEY_SPACE:32,KEY_PAGE_UP:33,KEY_PAGE_DOWN:34,KEY_END:35,KEY_HOME:36,KEY_LEFT_ARROW:37,KEY_UP_ARROW:38,KEY_RIGHT_ARROW:39,KEY_DOWN_ARROW:40,KEY_INSERT:45,KEY_DELETE:46,KEY_HELP:47,KEY_LEFT_WINDOW:91,KEY_RIGHT_WINDOW:92,KEY_SELECT:93,KEY_NUMPAD_0:96,KEY_NUMPAD_1:97,KEY_NUMPAD_2:98,KEY_NUMPAD_3:99,KEY_NUMPAD_4:100,KEY_NUMPAD_5:101,KEY_NUMPAD_6:102,KEY_NUMPAD_7:103,KEY_NUMPAD_8:104,KEY_NUMPAD_9:105,KEY_NUMPAD_MULTIPLY:106,KEY_NUMPAD_PLUS:107,KEY_NUMPAD_ENTER:108,KEY_NUMPAD_MINUS:109,KEY_NUMPAD_PERIOD:110,KEY_NUMPAD_DIVIDE:111,KEY_F1:112,KEY_F2:113,KEY_F3:114,KEY_F4:115,KEY_F5:116,KEY_F6:117,KEY_F7:118,KEY_F8:119,KEY_F9:120,KEY_F10:121,KEY_F11:122,KEY_F12:123,KEY_F13:124,KEY_F14:125,KEY_F15:126,KEY_NUM_LOCK:144,KEY_SCROLL_LOCK:145};
this.revKeys=[];
for(var key in this.keys){
this.revKeys[this.keys[key]]=key;
}
this.fixEvent=function(evt,_2dc){
if(!evt){
if(window["event"]){
evt=window.event;
}
}
if((evt["type"])&&(evt["type"].indexOf("key")==0)){
evt.keys=this.revKeys;
for(var key in this.keys){
evt[key]=this.keys[key];
}
if(evt["type"]=="keydown"&&dojo.render.html.ie){
switch(evt.keyCode){
case evt.KEY_SHIFT:
case evt.KEY_CTRL:
case evt.KEY_ALT:
case evt.KEY_CAPS_LOCK:
case evt.KEY_LEFT_WINDOW:
case evt.KEY_RIGHT_WINDOW:
case evt.KEY_SELECT:
case evt.KEY_NUM_LOCK:
case evt.KEY_SCROLL_LOCK:
case evt.KEY_NUMPAD_0:
case evt.KEY_NUMPAD_1:
case evt.KEY_NUMPAD_2:
case evt.KEY_NUMPAD_3:
case evt.KEY_NUMPAD_4:
case evt.KEY_NUMPAD_5:
case evt.KEY_NUMPAD_6:
case evt.KEY_NUMPAD_7:
case evt.KEY_NUMPAD_8:
case evt.KEY_NUMPAD_9:
case evt.KEY_NUMPAD_PERIOD:
break;
case evt.KEY_NUMPAD_MULTIPLY:
case evt.KEY_NUMPAD_PLUS:
case evt.KEY_NUMPAD_ENTER:
case evt.KEY_NUMPAD_MINUS:
case evt.KEY_NUMPAD_DIVIDE:
break;
case evt.KEY_PAUSE:
case evt.KEY_TAB:
case evt.KEY_BACKSPACE:
case evt.KEY_ENTER:
case evt.KEY_ESCAPE:
case evt.KEY_PAGE_UP:
case evt.KEY_PAGE_DOWN:
case evt.KEY_END:
case evt.KEY_HOME:
case evt.KEY_LEFT_ARROW:
case evt.KEY_UP_ARROW:
case evt.KEY_RIGHT_ARROW:
case evt.KEY_DOWN_ARROW:
case evt.KEY_INSERT:
case evt.KEY_DELETE:
case evt.KEY_F1:
case evt.KEY_F2:
case evt.KEY_F3:
case evt.KEY_F4:
case evt.KEY_F5:
case evt.KEY_F6:
case evt.KEY_F7:
case evt.KEY_F8:
case evt.KEY_F9:
case evt.KEY_F10:
case evt.KEY_F11:
case evt.KEY_F12:
case evt.KEY_F12:
case evt.KEY_F13:
case evt.KEY_F14:
case evt.KEY_F15:
case evt.KEY_CLEAR:
case evt.KEY_HELP:
evt.key=evt.keyCode;
break;
default:
if(evt.ctrlKey||evt.altKey){
var _2de=evt.keyCode;
if(_2de>=65&&_2de<=90&&evt.shiftKey==false){
_2de+=32;
}
if(_2de>=1&&_2de<=26&&evt.ctrlKey){
_2de+=96;
}
evt.key=String.fromCharCode(_2de);
}
}
}else{
if(evt["type"]=="keypress"){
if(dojo.render.html.opera){
if(evt.which==0){
evt.key=evt.keyCode;
}else{
if(evt.which>0){
switch(evt.which){
case evt.KEY_SHIFT:
case evt.KEY_CTRL:
case evt.KEY_ALT:
case evt.KEY_CAPS_LOCK:
case evt.KEY_NUM_LOCK:
case evt.KEY_SCROLL_LOCK:
break;
case evt.KEY_PAUSE:
case evt.KEY_TAB:
case evt.KEY_BACKSPACE:
case evt.KEY_ENTER:
case evt.KEY_ESCAPE:
evt.key=evt.which;
break;
default:
var _2de=evt.which;
if((evt.ctrlKey||evt.altKey||evt.metaKey)&&(evt.which>=65&&evt.which<=90&&evt.shiftKey==false)){
_2de+=32;
}
evt.key=String.fromCharCode(_2de);
}
}
}
}else{
if(dojo.render.html.ie){
if(!evt.ctrlKey&&!evt.altKey&&evt.keyCode>=evt.KEY_SPACE){
evt.key=String.fromCharCode(evt.keyCode);
}
}else{
if(dojo.render.html.safari){
switch(evt.keyCode){
case 25:
evt.key=evt.KEY_TAB;
evt.shift=true;
break;
case 63232:
evt.key=evt.KEY_UP_ARROW;
break;
case 63233:
evt.key=evt.KEY_DOWN_ARROW;
break;
case 63234:
evt.key=evt.KEY_LEFT_ARROW;
break;
case 63235:
evt.key=evt.KEY_RIGHT_ARROW;
break;
case 63236:
evt.key=evt.KEY_F1;
break;
case 63237:
evt.key=evt.KEY_F2;
break;
case 63238:
evt.key=evt.KEY_F3;
break;
case 63239:
evt.key=evt.KEY_F4;
break;
case 63240:
evt.key=evt.KEY_F5;
break;
case 63241:
evt.key=evt.KEY_F6;
break;
case 63242:
evt.key=evt.KEY_F7;
break;
case 63243:
evt.key=evt.KEY_F8;
break;
case 63244:
evt.key=evt.KEY_F9;
break;
case 63245:
evt.key=evt.KEY_F10;
break;
case 63246:
evt.key=evt.KEY_F11;
break;
case 63247:
evt.key=evt.KEY_F12;
break;
case 63250:
evt.key=evt.KEY_PAUSE;
break;
case 63272:
evt.key=evt.KEY_DELETE;
break;
case 63273:
evt.key=evt.KEY_HOME;
break;
case 63275:
evt.key=evt.KEY_END;
break;
case 63276:
evt.key=evt.KEY_PAGE_UP;
break;
case 63277:
evt.key=evt.KEY_PAGE_DOWN;
break;
case 63302:
evt.key=evt.KEY_INSERT;
break;
case 63248:
case 63249:
case 63289:
break;
default:
evt.key=evt.charCode>=evt.KEY_SPACE?String.fromCharCode(evt.charCode):evt.keyCode;
}
}else{
evt.key=evt.charCode>0?String.fromCharCode(evt.charCode):evt.keyCode;
}
}
}
}
}
}
if(dojo.render.html.ie){
if(!evt.target){
evt.target=evt.srcElement;
}
if(!evt.currentTarget){
evt.currentTarget=(_2dc?_2dc:evt.srcElement);
}
if(!evt.layerX){
evt.layerX=evt.offsetX;
}
if(!evt.layerY){
evt.layerY=evt.offsetY;
}
var doc=(evt.srcElement&&evt.srcElement.ownerDocument)?evt.srcElement.ownerDocument:document;
var _2e0=((dojo.render.html.ie55)||(doc["compatMode"]=="BackCompat"))?doc.body:doc.documentElement;
if(!evt.pageX){
evt.pageX=evt.clientX+(_2e0.scrollLeft||0);
}
if(!evt.pageY){
evt.pageY=evt.clientY+(_2e0.scrollTop||0);
}
if(evt.type=="mouseover"){
evt.relatedTarget=evt.fromElement;
}
if(evt.type=="mouseout"){
evt.relatedTarget=evt.toElement;
}
this.currentEvent=evt;
evt.callListener=this.callListener;
evt.stopPropagation=this._stopPropagation;
evt.preventDefault=this._preventDefault;
}
return evt;
};
this.stopEvent=function(evt){
if(window.event){
evt.cancelBubble=true;
evt.returnValue=false;
}else{
evt.preventDefault();
evt.stopPropagation();
}
};
};
dojo.kwCompoundRequire({common:["dojo.event.common","dojo.event.topic"],browser:["dojo.event.browser"],dashboard:["dojo.event.browser"]});
dojo.provide("dojo.event.*");
dojo.provide("dojo.widget.Manager");
dojo.widget.manager=new function(){
this.widgets=[];
this.widgetIds=[];
this.topWidgets={};
var _2e2={};
var _2e3=[];
this.getUniqueId=function(_2e4){
var _2e5;
do{
_2e5=_2e4+"_"+(_2e2[_2e4]!=undefined?++_2e2[_2e4]:_2e2[_2e4]=0);
}while(this.getWidgetById(_2e5));
return _2e5;
};
this.add=function(_2e6){
this.widgets.push(_2e6);
if(!_2e6.extraArgs["id"]){
_2e6.extraArgs["id"]=_2e6.extraArgs["ID"];
}
if(_2e6.widgetId==""){
if(_2e6["id"]){
_2e6.widgetId=_2e6["id"];
}else{
if(_2e6.extraArgs["id"]){
_2e6.widgetId=_2e6.extraArgs["id"];
}else{
_2e6.widgetId=this.getUniqueId(_2e6.ns+"_"+_2e6.widgetType);
}
}
}
if(this.widgetIds[_2e6.widgetId]){
dojo.debug("widget ID collision on ID: "+_2e6.widgetId);
}
this.widgetIds[_2e6.widgetId]=_2e6;
};
this.destroyAll=function(){
for(var x=this.widgets.length-1;x>=0;x--){
try{
this.widgets[x].destroy(true);
delete this.widgets[x];
}
catch(e){
}
}
};
this.remove=function(_2e8){
if(dojo.lang.isNumber(_2e8)){
var tw=this.widgets[_2e8].widgetId;
delete this.topWidgets[tw];
delete this.widgetIds[tw];
this.widgets.splice(_2e8,1);
}else{
this.removeById(_2e8);
}
};
this.removeById=function(id){
if(!dojo.lang.isString(id)){
id=id["widgetId"];
if(!id){
dojo.debug("invalid widget or id passed to removeById");
return;
}
}
for(var i=0;i<this.widgets.length;i++){
if(this.widgets[i].widgetId==id){
this.remove(i);
break;
}
}
};
this.getWidgetById=function(id){
if(dojo.lang.isString(id)){
return this.widgetIds[id];
}
return id;
};
this.getWidgetsByType=function(type){
var lt=type.toLowerCase();
var _2ef=(type.indexOf(":")<0?function(x){
return x.widgetType.toLowerCase();
}:function(x){
return x.getNamespacedType();
});
var ret=[];
dojo.lang.forEach(this.widgets,function(x){
if(_2ef(x)==lt){
ret.push(x);
}
});
return ret;
};
this.getWidgetsByFilter=function(_2f4,_2f5){
var ret=[];
dojo.lang.every(this.widgets,function(x){
if(_2f4(x)){
ret.push(x);
if(_2f5){
return false;
}
}
return true;
});
return (_2f5?ret[0]:ret);
};
this.getAllWidgets=function(){
return this.widgets.concat();
};
this.getWidgetByNode=function(node){
var w=this.getAllWidgets();
node=dojo.byId(node);
for(var i=0;i<w.length;i++){
if(w[i].domNode==node){
return w[i];
}
}
return null;
};
this.byId=this.getWidgetById;
this.byType=this.getWidgetsByType;
this.byFilter=this.getWidgetsByFilter;
this.byNode=this.getWidgetByNode;
var _2fb={};
var _2fc=["dojo.widget"];
for(var i=0;i<_2fc.length;i++){
_2fc[_2fc[i]]=true;
}
this.registerWidgetPackage=function(_2fe){
if(!_2fc[_2fe]){
_2fc[_2fe]=true;
_2fc.push(_2fe);
}
};
this.getWidgetPackageList=function(){
return dojo.lang.map(_2fc,function(elt){
return (elt!==true?elt:undefined);
});
};
this.getImplementation=function(_300,_301,_302,ns){
var impl=this.getImplementationName(_300,ns);
if(impl){
var ret=_301?new impl(_301):new impl();
return ret;
}
};
function buildPrefixCache(){
for(var _306 in dojo.render){
if(dojo.render[_306]["capable"]===true){
var _307=dojo.render[_306].prefixes;
for(var i=0;i<_307.length;i++){
_2e3.push(_307[i].toLowerCase());
}
}
}
}
var _309=function(_30a,_30b){
if(!_30b){
return null;
}
for(var i=0,l=_2e3.length,_30e;i<=l;i++){
_30e=(i<l?_30b[_2e3[i]]:_30b);
if(!_30e){
continue;
}
for(var name in _30e){
if(name.toLowerCase()==_30a){
return _30e[name];
}
}
}
return null;
};
var _310=function(_311,_312){
var _313=dojo.evalObjPath(_312,false);
return (_313?_309(_311,_313):null);
};
this.getImplementationName=function(_314,ns){
var _316=_314.toLowerCase();
ns=ns||"dojo";
var imps=_2fb[ns]||(_2fb[ns]={});
var impl=imps[_316];
if(impl){
return impl;
}
if(!_2e3.length){
buildPrefixCache();
}
var _319=dojo.ns.get(ns);
if(!_319){
dojo.ns.register(ns,ns+".widget");
_319=dojo.ns.get(ns);
}
if(_319){
_319.resolve(_314);
}
impl=_310(_316,_319.module);
if(impl){
return (imps[_316]=impl);
}
_319=dojo.ns.require(ns);
if((_319)&&(_319.resolver)){
_319.resolve(_314);
impl=_310(_316,_319.module);
if(impl){
return (imps[_316]=impl);
}
}
dojo.deprecated("dojo.widget.Manager.getImplementationName","Could not locate widget implementation for \""+_314+"\" in \""+_319.module+"\" registered to namespace \""+_319.name+"\". "+"Developers must specify correct namespaces for all non-Dojo widgets","0.5");
for(var i=0;i<_2fc.length;i++){
impl=_310(_316,_2fc[i]);
if(impl){
return (imps[_316]=impl);
}
}
throw new Error("Could not locate widget implementation for \""+_314+"\" in \""+_319.module+"\" registered to namespace \""+_319.name+"\"");
};
this.resizing=false;
this.onWindowResized=function(){
if(this.resizing){
return;
}
try{
this.resizing=true;
for(var id in this.topWidgets){
var _31c=this.topWidgets[id];
if(_31c.checkSize){
_31c.checkSize();
}
}
}
catch(e){
}
finally{
this.resizing=false;
}
};
if(typeof window!="undefined"){
dojo.addOnLoad(this,"onWindowResized");
dojo.event.connect(window,"onresize",this,"onWindowResized");
}
};
(function(){
var dw=dojo.widget;
var dwm=dw.manager;
var h=dojo.lang.curry(dojo.lang,"hitch",dwm);
var g=function(_321,_322){
dw[(_322||_321)]=h(_321);
};
g("add","addWidget");
g("destroyAll","destroyAllWidgets");
g("remove","removeWidget");
g("removeById","removeWidgetById");
g("getWidgetById");
g("getWidgetById","byId");
g("getWidgetsByType");
g("getWidgetsByFilter");
g("getWidgetsByType","byType");
g("getWidgetsByFilter","byFilter");
g("getWidgetByNode","byNode");
dw.all=function(n){
var _324=dwm.getAllWidgets.apply(dwm,arguments);
if(arguments.length>0){
return _324[n];
}
return _324;
};
g("registerWidgetPackage");
g("getImplementation","getWidgetImplementation");
g("getImplementationName","getWidgetImplementationName");
dw.widgets=dwm.widgets;
dw.widgetIds=dwm.widgetIds;
dw.root=dwm.root;
})();
dojo.provide("dojo.uri.Uri");
dojo.uri=new function(){
this.dojoUri=function(uri){
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri(),uri);
};
this.moduleUri=function(_326,uri){
var loc=dojo.hostenv.getModuleSymbols(_326).join("/");
if(!loc){
return null;
}
if(loc.lastIndexOf("/")!=loc.length-1){
loc+="/";
}
var _329=loc.indexOf(":");
var _32a=loc.indexOf("/");
if(loc.charAt(0)!="/"&&(_329==-1||_329>_32a)){
loc=dojo.hostenv.getBaseScriptUri()+loc;
}
return new dojo.uri.Uri(loc,uri);
};
this.Uri=function(){
var uri=arguments[0];
for(var i=1;i<arguments.length;i++){
if(!arguments[i]){
continue;
}
var _32d=new dojo.uri.Uri(arguments[i].toString());
var _32e=new dojo.uri.Uri(uri.toString());
if((_32d.path=="")&&(_32d.scheme==null)&&(_32d.authority==null)&&(_32d.query==null)){
if(_32d.fragment!=null){
_32e.fragment=_32d.fragment;
}
_32d=_32e;
}else{
if(_32d.scheme==null){
_32d.scheme=_32e.scheme;
if(_32d.authority==null){
_32d.authority=_32e.authority;
if(_32d.path.charAt(0)!="/"){
var path=_32e.path.substring(0,_32e.path.lastIndexOf("/")+1)+_32d.path;
var segs=path.split("/");
for(var j=0;j<segs.length;j++){
if(segs[j]=="."){
if(j==segs.length-1){
segs[j]="";
}else{
segs.splice(j,1);
j--;
}
}else{
if(j>0&&!(j==1&&segs[0]=="")&&segs[j]==".."&&segs[j-1]!=".."){
if(j==segs.length-1){
segs.splice(j,1);
segs[j-1]="";
}else{
segs.splice(j-1,2);
j-=2;
}
}
}
}
_32d.path=segs.join("/");
}
}
}
}
uri="";
if(_32d.scheme!=null){
uri+=_32d.scheme+":";
}
if(_32d.authority!=null){
uri+="//"+_32d.authority;
}
uri+=_32d.path;
if(_32d.query!=null){
uri+="?"+_32d.query;
}
if(_32d.fragment!=null){
uri+="#"+_32d.fragment;
}
}
this.uri=uri.toString();
var _332="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=this.uri.match(new RegExp(_332));
this.scheme=r[2]||(r[1]?"":null);
this.authority=r[4]||(r[3]?"":null);
this.path=r[5];
this.query=r[7]||(r[6]?"":null);
this.fragment=r[9]||(r[8]?"":null);
if(this.authority!=null){
_332="^((([^:]+:)?([^@]+))@)?([^:]*)(:([0-9]+))?$";
r=this.authority.match(new RegExp(_332));
this.user=r[3]||null;
this.password=r[4]||null;
this.host=r[5];
this.port=r[7]||null;
}
this.toString=function(){
return this.uri;
};
};
};
dojo.kwCompoundRequire({common:[["dojo.uri.Uri",false,false]]});
dojo.provide("dojo.uri.*");
dojo.provide("dojo.html.common");
dojo.lang.mixin(dojo.html,dojo.dom);
dojo.html.body=function(){
dojo.deprecated("dojo.html.body() moved to dojo.body()","0.5");
return dojo.body();
};
dojo.html.getEventTarget=function(evt){
if(!evt){
evt=dojo.global().event||{};
}
var t=(evt.srcElement?evt.srcElement:(evt.target?evt.target:null));
while((t)&&(t.nodeType!=1)){
t=t.parentNode;
}
return t;
};
dojo.html.getViewport=function(){
var _336=dojo.global();
var _337=dojo.doc();
var w=0;
var h=0;
if(dojo.render.html.mozilla){
w=_337.documentElement.clientWidth;
h=_336.innerHeight;
}else{
if(!dojo.render.html.opera&&_336.innerWidth){
w=_336.innerWidth;
h=_336.innerHeight;
}else{
if(!dojo.render.html.opera&&dojo.exists(_337,"documentElement.clientWidth")){
var w2=_337.documentElement.clientWidth;
if(!w||w2&&w2<w){
w=w2;
}
h=_337.documentElement.clientHeight;
}else{
if(dojo.body().clientWidth){
w=dojo.body().clientWidth;
h=dojo.body().clientHeight;
}
}
}
}
return {width:w,height:h};
};
dojo.html.getScroll=function(){
var _33b=dojo.global();
var _33c=dojo.doc();
var top=_33b.pageYOffset||_33c.documentElement.scrollTop||dojo.body().scrollTop||0;
var left=_33b.pageXOffset||_33c.documentElement.scrollLeft||dojo.body().scrollLeft||0;
return {top:top,left:left,offset:{x:left,y:top}};
};
dojo.html.getParentByType=function(node,type){
var _341=dojo.doc();
var _342=dojo.byId(node);
type=type.toLowerCase();
while((_342)&&(_342.nodeName.toLowerCase()!=type)){
if(_342==(_341["body"]||_341["documentElement"])){
return null;
}
_342=_342.parentNode;
}
return _342;
};
dojo.html.getAttribute=function(node,attr){
node=dojo.byId(node);
if((!node)||(!node.getAttribute)){
return null;
}
var ta=typeof attr=="string"?attr:new String(attr);
var v=node.getAttribute(ta.toUpperCase());
if((v)&&(typeof v=="string")&&(v!="")){
return v;
}
if(v&&v.value){
return v.value;
}
if((node.getAttributeNode)&&(node.getAttributeNode(ta))){
return (node.getAttributeNode(ta)).value;
}else{
if(node.getAttribute(ta)){
return node.getAttribute(ta);
}else{
if(node.getAttribute(ta.toLowerCase())){
return node.getAttribute(ta.toLowerCase());
}
}
}
return null;
};
dojo.html.hasAttribute=function(node,attr){
return dojo.html.getAttribute(dojo.byId(node),attr)?true:false;
};
dojo.html.getCursorPosition=function(e){
e=e||dojo.global().event;
var _34a={x:0,y:0};
if(e.pageX||e.pageY){
_34a.x=e.pageX;
_34a.y=e.pageY;
}else{
var de=dojo.doc().documentElement;
var db=dojo.body();
_34a.x=e.clientX+((de||db)["scrollLeft"])-((de||db)["clientLeft"]);
_34a.y=e.clientY+((de||db)["scrollTop"])-((de||db)["clientTop"]);
}
return _34a;
};
dojo.html.isTag=function(node){
node=dojo.byId(node);
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName.toLowerCase()==String(arguments[i]).toLowerCase()){
return String(arguments[i]).toLowerCase();
}
}
}
return "";
};
if(dojo.render.html.ie&&!dojo.render.html.ie70){
if(window.location.href.substr(0,6).toLowerCase()!="https:"){
(function(){
var _34f=dojo.doc().createElement("script");
_34f.src="javascript:'dojo.html.createExternalElement=function(doc, tag){ return doc.createElement(tag); }'";
dojo.doc().getElementsByTagName("head")[0].appendChild(_34f);
})();
}
}else{
dojo.html.createExternalElement=function(doc,tag){
return doc.createElement(tag);
};
}
dojo.html._callDeprecated=function(_352,_353,args,_355,_356){
dojo.deprecated("dojo.html."+_352,"replaced by dojo.html."+_353+"("+(_355?"node, {"+_355+": "+_355+"}":"")+")"+(_356?"."+_356:""),"0.5");
var _357=[];
if(_355){
var _358={};
_358[_355]=args[1];
_357.push(args[0]);
_357.push(_358);
}else{
_357=args;
}
var ret=dojo.html[_353].apply(dojo.html,args);
if(_356){
return ret[_356];
}else{
return ret;
}
};
dojo.html.getViewportWidth=function(){
return dojo.html._callDeprecated("getViewportWidth","getViewport",arguments,null,"width");
};
dojo.html.getViewportHeight=function(){
return dojo.html._callDeprecated("getViewportHeight","getViewport",arguments,null,"height");
};
dojo.html.getViewportSize=function(){
return dojo.html._callDeprecated("getViewportSize","getViewport",arguments);
};
dojo.html.getScrollTop=function(){
return dojo.html._callDeprecated("getScrollTop","getScroll",arguments,null,"top");
};
dojo.html.getScrollLeft=function(){
return dojo.html._callDeprecated("getScrollLeft","getScroll",arguments,null,"left");
};
dojo.html.getScrollOffset=function(){
return dojo.html._callDeprecated("getScrollOffset","getScroll",arguments,null,"offset");
};
dojo.provide("dojo.a11y");
dojo.a11y={imgPath:dojo.uri.moduleUri("dojo.widget","templates/images"),doAccessibleCheck:true,accessible:null,checkAccessible:function(){
if(this.accessible===null){
this.accessible=false;
if(this.doAccessibleCheck==true){
this.accessible=this.testAccessible();
}
}
return this.accessible;
},testAccessible:function(){
this.accessible=false;
if(dojo.render.html.ie||dojo.render.html.mozilla){
var div=document.createElement("div");
div.style.backgroundImage="url(\""+this.imgPath+"/tab_close.gif\")";
dojo.body().appendChild(div);
var _35b=null;
if(window.getComputedStyle){
var _35c=getComputedStyle(div,"");
_35b=_35c.getPropertyValue("background-image");
}else{
_35b=div.currentStyle.backgroundImage;
}
var _35d=false;
if(_35b!=null&&(_35b=="none"||_35b=="url(invalid-url:)")){
this.accessible=true;
}
dojo.body().removeChild(div);
}
return this.accessible;
},setCheckAccessible:function(_35e){
this.doAccessibleCheck=_35e;
},setAccessibleMode:function(){
if(this.accessible===null){
if(this.checkAccessible()){
dojo.render.html.prefixes.unshift("a11y");
}
}
return this.accessible;
}};
dojo.provide("dojo.widget.Widget");
dojo.declare("dojo.widget.Widget",null,function(){
this.children=[];
this.extraArgs={};
},{parent:null,isTopLevel:false,disabled:false,isContainer:false,widgetId:"",widgetType:"Widget",ns:"dojo",getNamespacedType:function(){
return (this.ns?this.ns+":"+this.widgetType:this.widgetType).toLowerCase();
},toString:function(){
return "[Widget "+this.getNamespacedType()+", "+(this.widgetId||"NO ID")+"]";
},repr:function(){
return this.toString();
},enable:function(){
this.disabled=false;
},disable:function(){
this.disabled=true;
},onResized:function(){
this.notifyChildrenOfResize();
},notifyChildrenOfResize:function(){
for(var i=0;i<this.children.length;i++){
var _360=this.children[i];
if(_360.onResized){
_360.onResized();
}
}
},create:function(args,_362,_363,ns){
if(ns){
this.ns=ns;
}
this.satisfyPropertySets(args,_362,_363);
this.mixInProperties(args,_362,_363);
this.postMixInProperties(args,_362,_363);
dojo.widget.manager.add(this);
this.buildRendering(args,_362,_363);
this.initialize(args,_362,_363);
this.postInitialize(args,_362,_363);
this.postCreate(args,_362,_363);
return this;
},destroy:function(_365){
if(this.parent){
this.parent.removeChild(this);
}
this.destroyChildren();
this.uninitialize();
this.destroyRendering(_365);
dojo.widget.manager.removeById(this.widgetId);
},destroyChildren:function(){
var _366;
var i=0;
while(this.children.length>i){
_366=this.children[i];
if(_366 instanceof dojo.widget.Widget){
this.removeChild(_366);
_366.destroy();
continue;
}
i++;
}
},getChildrenOfType:function(type,_369){
var ret=[];
var _36b=dojo.lang.isFunction(type);
if(!_36b){
type=type.toLowerCase();
}
for(var x=0;x<this.children.length;x++){
if(_36b){
if(this.children[x] instanceof type){
ret.push(this.children[x]);
}
}else{
if(this.children[x].widgetType.toLowerCase()==type){
ret.push(this.children[x]);
}
}
if(_369){
ret=ret.concat(this.children[x].getChildrenOfType(type,_369));
}
}
return ret;
},getDescendants:function(){
var _36d=[];
var _36e=[this];
var elem;
while((elem=_36e.pop())){
_36d.push(elem);
if(elem.children){
dojo.lang.forEach(elem.children,function(elem){
_36e.push(elem);
});
}
}
return _36d;
},isFirstChild:function(){
return this===this.parent.children[0];
},isLastChild:function(){
return this===this.parent.children[this.parent.children.length-1];
},satisfyPropertySets:function(args){
return args;
},mixInProperties:function(args,frag){
if((args["fastMixIn"])||(frag["fastMixIn"])){
for(var x in args){
this[x]=args[x];
}
return;
}
var _375;
var _376=dojo.widget.lcArgsCache[this.widgetType];
if(_376==null){
_376={};
for(var y in this){
_376[((new String(y)).toLowerCase())]=y;
}
dojo.widget.lcArgsCache[this.widgetType]=_376;
}
var _378={};
for(var x in args){
if(!this[x]){
var y=_376[(new String(x)).toLowerCase()];
if(y){
args[y]=args[x];
x=y;
}
}
if(_378[x]){
continue;
}
_378[x]=true;
if((typeof this[x])!=(typeof _375)){
if(typeof args[x]!="string"){
this[x]=args[x];
}else{
if(dojo.lang.isString(this[x])){
this[x]=args[x];
}else{
if(dojo.lang.isNumber(this[x])){
this[x]=new Number(args[x]);
}else{
if(dojo.lang.isBoolean(this[x])){
this[x]=(args[x].toLowerCase()=="false")?false:true;
}else{
if(dojo.lang.isFunction(this[x])){
if(args[x].search(/[^\w\.]+/i)==-1){
this[x]=dojo.evalObjPath(args[x],false);
}else{
var tn=dojo.lang.nameAnonFunc(new Function(args[x]),this);
dojo.event.kwConnect({srcObj:this,srcFunc:x,adviceObj:this,adviceFunc:tn});
}
}else{
if(dojo.lang.isArray(this[x])){
this[x]=args[x].split(";");
}else{
if(this[x] instanceof Date){
this[x]=new Date(Number(args[x]));
}else{
if(typeof this[x]=="object"){
if(this[x] instanceof dojo.uri.Uri){
this[x]=dojo.uri.dojoUri(args[x]);
}else{
var _37a=args[x].split(";");
for(var y=0;y<_37a.length;y++){
var si=_37a[y].indexOf(":");
if((si!=-1)&&(_37a[y].length>si)){
this[x][_37a[y].substr(0,si).replace(/^\s+|\s+$/g,"")]=_37a[y].substr(si+1);
}
}
}
}else{
this[x]=args[x];
}
}
}
}
}
}
}
}
}else{
this.extraArgs[x.toLowerCase()]=args[x];
}
}
},postMixInProperties:function(args,frag,_37e){
},initialize:function(args,frag,_381){
return false;
},postInitialize:function(args,frag,_384){
return false;
},postCreate:function(args,frag,_387){
return false;
},uninitialize:function(){
return false;
},buildRendering:function(args,frag,_38a){
dojo.unimplemented("dojo.widget.Widget.buildRendering, on "+this.toString()+", ");
return false;
},destroyRendering:function(){
dojo.unimplemented("dojo.widget.Widget.destroyRendering");
return false;
},addedTo:function(_38b){
},addChild:function(_38c){
dojo.unimplemented("dojo.widget.Widget.addChild");
return false;
},removeChild:function(_38d){
for(var x=0;x<this.children.length;x++){
if(this.children[x]===_38d){
this.children.splice(x,1);
_38d.parent=null;
break;
}
}
return _38d;
},getPreviousSibling:function(){
var idx=this.getParentIndex();
if(idx<=0){
return null;
}
return this.parent.children[idx-1];
},getSiblings:function(){
return this.parent.children;
},getParentIndex:function(){
return dojo.lang.indexOf(this.parent.children,this,true);
},getNextSibling:function(){
var idx=this.getParentIndex();
if(idx==this.parent.children.length-1){
return null;
}
if(idx<0){
return null;
}
return this.parent.children[idx+1];
}});
dojo.widget.lcArgsCache={};
dojo.widget.tags={};
dojo.widget.tags.addParseTreeHandler=function(type){
dojo.deprecated("addParseTreeHandler",". ParseTreeHandlers are now reserved for components. Any unfiltered DojoML tag without a ParseTreeHandler is assumed to be a widget","0.5");
};
dojo.widget.tags["dojo:propertyset"]=function(_392,_393,_394){
var _395=_393.parseProperties(_392["dojo:propertyset"]);
};
dojo.widget.tags["dojo:connect"]=function(_396,_397,_398){
var _399=_397.parseProperties(_396["dojo:connect"]);
};
dojo.widget.buildWidgetFromParseTree=function(type,frag,_39c,_39d,_39e,_39f){
dojo.a11y.setAccessibleMode();
var _3a0=type.split(":");
_3a0=(_3a0.length==2)?_3a0[1]:type;
var _3a1=_39f||_39c.parseProperties(frag[frag["ns"]+":"+_3a0]);
var _3a2=dojo.widget.manager.getImplementation(_3a0,null,null,frag["ns"]);
if(!_3a2){
throw new Error("cannot find \""+type+"\" widget");
}else{
if(!_3a2.create){
throw new Error("\""+type+"\" widget object has no \"create\" method and does not appear to implement *Widget");
}
}
_3a1["dojoinsertionindex"]=_39e;
var ret=_3a2.create(_3a1,frag,_39d,frag["ns"]);
return ret;
};
dojo.widget.defineWidget=function(_3a4,_3a5,_3a6,init,_3a8){
if(dojo.lang.isString(arguments[3])){
dojo.widget._defineWidget(arguments[0],arguments[3],arguments[1],arguments[4],arguments[2]);
}else{
var args=[arguments[0]],p=3;
if(dojo.lang.isString(arguments[1])){
args.push(arguments[1],arguments[2]);
}else{
args.push("",arguments[1]);
p=2;
}
if(dojo.lang.isFunction(arguments[p])){
args.push(arguments[p],arguments[p+1]);
}else{
args.push(null,arguments[p]);
}
dojo.widget._defineWidget.apply(this,args);
}
};
dojo.widget.defineWidget.renderers="html|svg|vml";
dojo.widget._defineWidget=function(_3ab,_3ac,_3ad,init,_3af){
var _3b0=_3ab.split(".");
var type=_3b0.pop();
var regx="\\.("+(_3ac?_3ac+"|":"")+dojo.widget.defineWidget.renderers+")\\.";
var r=_3ab.search(new RegExp(regx));
_3b0=(r<0?_3b0.join("."):_3ab.substr(0,r));
dojo.widget.manager.registerWidgetPackage(_3b0);
var pos=_3b0.indexOf(".");
var _3b5=(pos>-1)?_3b0.substring(0,pos):_3b0;
_3af=(_3af)||{};
_3af.widgetType=type;
if((!init)&&(_3af["classConstructor"])){
init=_3af.classConstructor;
delete _3af.classConstructor;
}
dojo.declare(_3ab,_3ad,init,_3af);
};
dojo.provide("dojo.widget.Parse");
dojo.widget.Parse=function(_3b6){
this.propertySetsList=[];
this.fragment=_3b6;
this.createComponents=function(frag,_3b8){
var _3b9=[];
var _3ba=false;
try{
if(frag&&frag.tagName&&(frag!=frag.nodeRef)){
var _3bb=dojo.widget.tags;
var tna=String(frag.tagName).split(";");
for(var x=0;x<tna.length;x++){
var ltn=tna[x].replace(/^\s+|\s+$/g,"").toLowerCase();
frag.tagName=ltn;
var ret;
if(_3bb[ltn]){
_3ba=true;
ret=_3bb[ltn](frag,this,_3b8,frag.index);
_3b9.push(ret);
}else{
if(ltn.indexOf(":")==-1){
ltn="dojo:"+ltn;
}
ret=dojo.widget.buildWidgetFromParseTree(ltn,frag,this,_3b8,frag.index);
if(ret){
_3ba=true;
_3b9.push(ret);
}
}
}
}
}
catch(e){
dojo.debug("dojo.widget.Parse: error:",e);
}
if(!_3ba){
_3b9=_3b9.concat(this.createSubComponents(frag,_3b8));
}
return _3b9;
};
this.createSubComponents=function(_3c0,_3c1){
var frag,_3c3=[];
for(var item in _3c0){
frag=_3c0[item];
if(frag&&typeof frag=="object"&&(frag!=_3c0.nodeRef)&&(frag!=_3c0.tagName)&&(!dojo.dom.isNode(frag))){
_3c3=_3c3.concat(this.createComponents(frag,_3c1));
}
}
return _3c3;
};
this.parsePropertySets=function(_3c5){
return [];
};
this.parseProperties=function(_3c6){
var _3c7={};
for(var item in _3c6){
if((_3c6[item]==_3c6.tagName)||(_3c6[item]==_3c6.nodeRef)){
}else{
var frag=_3c6[item];
if(frag.tagName&&dojo.widget.tags[frag.tagName.toLowerCase()]){
}else{
if(frag[0]&&frag[0].value!=""&&frag[0].value!=null){
try{
if(item.toLowerCase()=="dataprovider"){
var _3ca=this;
this.getDataProvider(_3ca,frag[0].value);
_3c7.dataProvider=this.dataProvider;
}
_3c7[item]=frag[0].value;
var _3cb=this.parseProperties(frag);
for(var _3cc in _3cb){
_3c7[_3cc]=_3cb[_3cc];
}
}
catch(e){
dojo.debug(e);
}
}
}
switch(item.toLowerCase()){
case "checked":
case "disabled":
if(typeof _3c7[item]!="boolean"){
_3c7[item]=true;
}
break;
}
}
}
return _3c7;
};
this.getDataProvider=function(_3cd,_3ce){
dojo.io.bind({url:_3ce,load:function(type,_3d0){
if(type=="load"){
_3cd.dataProvider=_3d0;
}
},mimetype:"text/javascript",sync:true});
};
this.getPropertySetById=function(_3d1){
for(var x=0;x<this.propertySetsList.length;x++){
if(_3d1==this.propertySetsList[x]["id"][0].value){
return this.propertySetsList[x];
}
}
return "";
};
this.getPropertySetsByType=function(_3d3){
var _3d4=[];
for(var x=0;x<this.propertySetsList.length;x++){
var cpl=this.propertySetsList[x];
var cpcc=cpl.componentClass||cpl.componentType||null;
var _3d8=this.propertySetsList[x]["id"][0].value;
if(cpcc&&(_3d8==cpcc[0].value)){
_3d4.push(cpl);
}
}
return _3d4;
};
this.getPropertySets=function(_3d9){
var ppl="dojo:propertyproviderlist";
var _3db=[];
var _3dc=_3d9.tagName;
if(_3d9[ppl]){
var _3dd=_3d9[ppl].value.split(" ");
for(var _3de in _3dd){
if((_3de.indexOf("..")==-1)&&(_3de.indexOf("://")==-1)){
var _3df=this.getPropertySetById(_3de);
if(_3df!=""){
_3db.push(_3df);
}
}else{
}
}
}
return this.getPropertySetsByType(_3dc).concat(_3db);
};
this.createComponentFromScript=function(_3e0,_3e1,_3e2,ns){
_3e2.fastMixIn=true;
var ltn=(ns||"dojo")+":"+_3e1.toLowerCase();
if(dojo.widget.tags[ltn]){
return [dojo.widget.tags[ltn](_3e2,this,null,null,_3e2)];
}
return [dojo.widget.buildWidgetFromParseTree(ltn,_3e2,this,null,null,_3e2)];
};
};
dojo.widget._parser_collection={"dojo":new dojo.widget.Parse()};
dojo.widget.getParser=function(name){
if(!name){
name="dojo";
}
if(!this._parser_collection[name]){
this._parser_collection[name]=new dojo.widget.Parse();
}
return this._parser_collection[name];
};
dojo.widget.createWidget=function(name,_3e7,_3e8,_3e9){
var _3ea=false;
var _3eb=(typeof name=="string");
if(_3eb){
var pos=name.indexOf(":");
var ns=(pos>-1)?name.substring(0,pos):"dojo";
if(pos>-1){
name=name.substring(pos+1);
}
var _3ee=name.toLowerCase();
var _3ef=ns+":"+_3ee;
_3ea=(dojo.byId(name)&&!dojo.widget.tags[_3ef]);
}
if((arguments.length==1)&&(_3ea||!_3eb)){
var xp=new dojo.xml.Parse();
var tn=_3ea?dojo.byId(name):name;
return dojo.widget.getParser().createComponents(xp.parseElement(tn,null,true))[0];
}
function fromScript(_3f2,name,_3f4,ns){
_3f4[_3ef]={dojotype:[{value:_3ee}],nodeRef:_3f2,fastMixIn:true};
_3f4.ns=ns;
return dojo.widget.getParser().createComponentFromScript(_3f2,name,_3f4,ns);
}
_3e7=_3e7||{};
var _3f6=false;
var tn=null;
var h=dojo.render.html.capable;
if(h){
tn=document.createElement("span");
}
if(!_3e8){
_3f6=true;
_3e8=tn;
if(h){
dojo.body().appendChild(_3e8);
}
}else{
if(_3e9){
dojo.dom.insertAtPosition(tn,_3e8,_3e9);
}else{
tn=_3e8;
}
}
var _3f8=fromScript(tn,name.toLowerCase(),_3e7,ns);
if((!_3f8)||(!_3f8[0])||(typeof _3f8[0].widgetType=="undefined")){
throw new Error("createWidget: Creation of \""+name+"\" widget failed.");
}
try{
if(_3f6&&_3f8[0].domNode.parentNode){
_3f8[0].domNode.parentNode.removeChild(_3f8[0].domNode);
}
}
catch(e){
dojo.debug(e);
}
return _3f8[0];
};
dojo.provide("dojo.html.style");
dojo.html.getClass=function(node){
node=dojo.byId(node);
if(!node){
return "";
}
var cs="";
if(node.className){
cs=node.className;
}else{
if(dojo.html.hasAttribute(node,"class")){
cs=dojo.html.getAttribute(node,"class");
}
}
return cs.replace(/^\s+|\s+$/g,"");
};
dojo.html.getClasses=function(node){
var c=dojo.html.getClass(node);
return (c=="")?[]:c.split(/\s+/g);
};
dojo.html.hasClass=function(node,_3fe){
return (new RegExp("(^|\\s+)"+_3fe+"(\\s+|$)")).test(dojo.html.getClass(node));
};
dojo.html.prependClass=function(node,_400){
_400+=" "+dojo.html.getClass(node);
return dojo.html.setClass(node,_400);
};
dojo.html.addClass=function(node,_402){
if(dojo.html.hasClass(node,_402)){
return false;
}
_402=(dojo.html.getClass(node)+" "+_402).replace(/^\s+|\s+$/g,"");
return dojo.html.setClass(node,_402);
};
dojo.html.setClass=function(node,_404){
node=dojo.byId(node);
var cs=new String(_404);
try{
if(typeof node.className=="string"){
node.className=cs;
}else{
if(node.setAttribute){
node.setAttribute("class",_404);
node.className=cs;
}else{
return false;
}
}
}
catch(e){
dojo.debug("dojo.html.setClass() failed",e);
}
return true;
};
dojo.html.removeClass=function(node,_407,_408){
try{
if(!_408){
var _409=dojo.html.getClass(node).replace(new RegExp("(^|\\s+)"+_407+"(\\s+|$)"),"$1$2");
}else{
var _409=dojo.html.getClass(node).replace(_407,"");
}
dojo.html.setClass(node,_409);
}
catch(e){
dojo.debug("dojo.html.removeClass() failed",e);
}
return true;
};
dojo.html.replaceClass=function(node,_40b,_40c){
dojo.html.removeClass(node,_40c);
dojo.html.addClass(node,_40b);
};
dojo.html.classMatchType={ContainsAll:0,ContainsAny:1,IsOnly:2};
dojo.html.getElementsByClass=function(_40d,_40e,_40f,_410,_411){
_411=false;
var _412=dojo.doc();
_40e=dojo.byId(_40e)||_412;
var _413=_40d.split(/\s+/g);
var _414=[];
if(_410!=1&&_410!=2){
_410=0;
}
var _415=new RegExp("(\\s|^)(("+_413.join(")|(")+"))(\\s|$)");
var _416=_413.join(" ").length;
var _417=[];
if(!_411&&_412.evaluate){
var _418=".//"+(_40f||"*")+"[contains(";
if(_410!=dojo.html.classMatchType.ContainsAny){
_418+="concat(' ',@class,' '), ' "+_413.join(" ') and contains(concat(' ',@class,' '), ' ")+" ')";
if(_410==2){
_418+=" and string-length(@class)="+_416+"]";
}else{
_418+="]";
}
}else{
_418+="concat(' ',@class,' '), ' "+_413.join(" ') or contains(concat(' ',@class,' '), ' ")+" ')]";
}
var _419=_412.evaluate(_418,_40e,null,XPathResult.ANY_TYPE,null);
var _41a=_419.iterateNext();
while(_41a){
try{
_417.push(_41a);
_41a=_419.iterateNext();
}
catch(e){
break;
}
}
return _417;
}else{
if(!_40f){
_40f="*";
}
_417=_40e.getElementsByTagName(_40f);
var node,i=0;
outer:
while(node=_417[i++]){
var _41d=dojo.html.getClasses(node);
if(_41d.length==0){
continue outer;
}
var _41e=0;
for(var j=0;j<_41d.length;j++){
if(_415.test(_41d[j])){
if(_410==dojo.html.classMatchType.ContainsAny){
_414.push(node);
continue outer;
}else{
_41e++;
}
}else{
if(_410==dojo.html.classMatchType.IsOnly){
continue outer;
}
}
}
if(_41e==_413.length){
if((_410==dojo.html.classMatchType.IsOnly)&&(_41e==_41d.length)){
_414.push(node);
}else{
if(_410==dojo.html.classMatchType.ContainsAll){
_414.push(node);
}
}
}
}
return _414;
}
};
dojo.html.getElementsByClassName=dojo.html.getElementsByClass;
dojo.html.toCamelCase=function(_420){
var arr=_420.split("-"),cc=arr[0];
for(var i=1;i<arr.length;i++){
cc+=arr[i].charAt(0).toUpperCase()+arr[i].substring(1);
}
return cc;
};
dojo.html.toSelectorCase=function(_424){
return _424.replace(/([A-Z])/g,"-$1").toLowerCase();
};
if(dojo.render.html.ie){
dojo.html.getComputedStyle=function(node,_426,_427){
node=dojo.byId(node);
if(!node||!node.style){
return _427;
}
return node.currentStyle[dojo.html.toCamelCase(_426)];
};
dojo.html.getComputedStyles=function(node){
return node.currentStyle;
};
}else{
dojo.html.getComputedStyle=function(node,_42a,_42b){
node=dojo.byId(node);
if(!node||!node.style){
return _42b;
}
var s=document.defaultView.getComputedStyle(node,null);
return (s&&s[dojo.html.toCamelCase(_42a)])||"";
};
dojo.html.getComputedStyles=function(node){
return document.defaultView.getComputedStyle(node,null);
};
}
dojo.html.getStyleProperty=function(node,_42f){
node=dojo.byId(node);
return (node&&node.style?node.style[dojo.html.toCamelCase(_42f)]:undefined);
};
dojo.html.getStyle=function(node,_431){
var _432=dojo.html.getStyleProperty(node,_431);
return (_432?_432:dojo.html.getComputedStyle(node,_431));
};
dojo.html.setStyle=function(node,_434,_435){
node=dojo.byId(node);
if(node&&node.style){
var _436=dojo.html.toCamelCase(_434);
node.style[_436]=_435;
}
};
dojo.html.setStyleText=function(_437,text){
try{
_437.style.cssText=text;
}
catch(e){
_437.setAttribute("style",text);
}
};
dojo.html.copyStyle=function(_439,_43a){
if(!_43a.style.cssText){
_439.setAttribute("style",_43a.getAttribute("style"));
}else{
_439.style.cssText=_43a.style.cssText;
}
dojo.html.addClass(_439,dojo.html.getClass(_43a));
};
dojo.html.getUnitValue=function(node,_43c,_43d){
var s=dojo.html.getComputedStyle(node,_43c);
if((!s)||((s=="auto")&&(_43d))){
return {value:0,units:"px"};
}
var _43f=s.match(/(\-?[\d.]+)([a-z%]*)/i);
if(!_43f){
return dojo.html.getUnitValue.bad;
}
return {value:Number(_43f[1]),units:_43f[2].toLowerCase()};
};
dojo.html.getUnitValue.bad={value:NaN,units:""};
if(dojo.render.html.ie){
dojo.html.toPixelValue=function(_440,_441){
if(!_441){
return 0;
}
if(_441.slice(-2)=="px"){
return parseFloat(_441);
}
var _442=0;
with(_440){
var _443=style.left;
var _444=runtimeStyle.left;
runtimeStyle.left=currentStyle.left;
try{
style.left=_441||0;
_442=style.pixelLeft;
style.left=_443;
runtimeStyle.left=_444;
}
catch(e){
}
}
return _442;
};
}else{
dojo.html.toPixelValue=function(_445,_446){
return (_446&&(_446.slice(-2)=="px")?parseFloat(_446):0);
};
}
dojo.html.getPixelValue=function(node,_448,_449){
return dojo.html.toPixelValue(node,dojo.html.getComputedStyle(node,_448));
};
dojo.html.setPositivePixelValue=function(node,_44b,_44c){
if(isNaN(_44c)){
return false;
}
node.style[_44b]=Math.max(0,_44c)+"px";
return true;
};
dojo.html.styleSheet=null;
dojo.html.insertCssRule=function(_44d,_44e,_44f){
if(!dojo.html.styleSheet){
if(document.createStyleSheet){
dojo.html.styleSheet=document.createStyleSheet();
}else{
if(document.styleSheets[0]){
dojo.html.styleSheet=document.styleSheets[0];
}else{
return null;
}
}
}
if(arguments.length<3){
if(dojo.html.styleSheet.cssRules){
_44f=dojo.html.styleSheet.cssRules.length;
}else{
if(dojo.html.styleSheet.rules){
_44f=dojo.html.styleSheet.rules.length;
}else{
return null;
}
}
}
if(dojo.html.styleSheet.insertRule){
var rule=_44d+" { "+_44e+" }";
return dojo.html.styleSheet.insertRule(rule,_44f);
}else{
if(dojo.html.styleSheet.addRule){
return dojo.html.styleSheet.addRule(_44d,_44e,_44f);
}else{
return null;
}
}
};
dojo.html.removeCssRule=function(_451){
if(!dojo.html.styleSheet){
dojo.debug("no stylesheet defined for removing rules");
return false;
}
if(dojo.render.html.ie){
if(!_451){
_451=dojo.html.styleSheet.rules.length;
dojo.html.styleSheet.removeRule(_451);
}
}else{
if(document.styleSheets[0]){
if(!_451){
_451=dojo.html.styleSheet.cssRules.length;
}
dojo.html.styleSheet.deleteRule(_451);
}
}
return true;
};
dojo.html._insertedCssFiles=[];
dojo.html.insertCssFile=function(URI,doc,_454,_455){
if(!URI){
return;
}
if(!doc){
doc=document;
}
var _456=dojo.hostenv.getText(URI,false,_455);
if(_456===null){
return;
}
_456=dojo.html.fixPathsInCssText(_456,URI);
if(_454){
var idx=-1,node,ent=dojo.html._insertedCssFiles;
for(var i=0;i<ent.length;i++){
if((ent[i].doc==doc)&&(ent[i].cssText==_456)){
idx=i;
node=ent[i].nodeRef;
break;
}
}
if(node){
var _45b=doc.getElementsByTagName("style");
for(var i=0;i<_45b.length;i++){
if(_45b[i]==node){
return;
}
}
dojo.html._insertedCssFiles.shift(idx,1);
}
}
var _45c=dojo.html.insertCssText(_456,doc);
dojo.html._insertedCssFiles.push({"doc":doc,"cssText":_456,"nodeRef":_45c});
if(_45c&&djConfig.isDebug){
_45c.setAttribute("dbgHref",URI);
}
return _45c;
};
dojo.html.insertCssText=function(_45d,doc,URI){
if(!_45d){
return;
}
if(!doc){
doc=document;
}
if(URI){
_45d=dojo.html.fixPathsInCssText(_45d,URI);
}
var _460=doc.createElement("style");
_460.setAttribute("type","text/css");
var head=doc.getElementsByTagName("head")[0];
if(!head){
dojo.debug("No head tag in document, aborting styles");
return;
}else{
head.appendChild(_460);
}
if(_460.styleSheet){
var _462=function(){
try{
_460.styleSheet.cssText=_45d;
}
catch(e){
dojo.debug(e);
}
};
if(_460.styleSheet.disabled){
setTimeout(_462,10);
}else{
_462();
}
}else{
var _463=doc.createTextNode(_45d);
_460.appendChild(_463);
}
return _460;
};
dojo.html.fixPathsInCssText=function(_464,URI){
if(!_464||!URI){
return;
}
var _466,str="",url="",_469="[\\t\\s\\w\\(\\)\\/\\.\\\\'\"-:#=&?~]+";
var _46a=new RegExp("url\\(\\s*("+_469+")\\s*\\)");
var _46b=/(file|https?|ftps?):\/\//;
regexTrim=new RegExp("^[\\s]*(['\"]?)("+_469+")\\1[\\s]*?$");
if(dojo.render.html.ie55||dojo.render.html.ie60){
var _46c=new RegExp("AlphaImageLoader\\((.*)src=['\"]("+_469+")['\"]");
while(_466=_46c.exec(_464)){
url=_466[2].replace(regexTrim,"$2");
if(!_46b.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_464.substring(0,_466.index)+"AlphaImageLoader("+_466[1]+"src='"+url+"'";
_464=_464.substr(_466.index+_466[0].length);
}
_464=str+_464;
str="";
}
while(_466=_46a.exec(_464)){
url=_466[1].replace(regexTrim,"$2");
if(!_46b.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_464.substring(0,_466.index)+"url("+url+")";
_464=_464.substr(_466.index+_466[0].length);
}
return str+_464;
};
dojo.html.setActiveStyleSheet=function(_46d){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")){
a.disabled=true;
if(a.getAttribute("title")==_46d){
a.disabled=false;
}
}
}
};
dojo.html.getActiveStyleSheet=function(){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")&&!a.disabled){
return a.getAttribute("title");
}
}
return null;
};
dojo.html.getPreferredStyleSheet=function(){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("rel").indexOf("alt")==-1&&a.getAttribute("title")){
return a.getAttribute("title");
}
}
return null;
};
dojo.html.applyBrowserClass=function(node){
var drh=dojo.render.html;
var _479={dj_ie:drh.ie,dj_ie55:drh.ie55,dj_ie6:drh.ie60,dj_ie7:drh.ie70,dj_iequirks:drh.ie&&drh.quirks,dj_opera:drh.opera,dj_opera8:drh.opera&&(Math.floor(dojo.render.version)==8),dj_opera9:drh.opera&&(Math.floor(dojo.render.version)==9),dj_khtml:drh.khtml,dj_safari:drh.safari,dj_gecko:drh.mozilla};
for(var p in _479){
if(_479[p]){
dojo.html.addClass(node,p);
}
}
};
dojo.provide("dojo.widget.DomWidget");
dojo.widget._cssFiles={};
dojo.widget._cssStrings={};
dojo.widget._templateCache={};
dojo.widget.defaultStrings={dojoRoot:dojo.hostenv.getBaseScriptUri(),dojoWidgetModuleUri:dojo.uri.moduleUri("dojo.widget"),baseScriptUri:dojo.hostenv.getBaseScriptUri()};
dojo.widget.fillFromTemplateCache=function(obj,_47c,_47d,_47e){
var _47f=_47c||obj.templatePath;
var _480=dojo.widget._templateCache;
if(!_47f&&!obj["widgetType"]){
do{
var _481="__dummyTemplate__"+dojo.widget._templateCache.dummyCount++;
}while(_480[_481]);
obj.widgetType=_481;
}
var wt=_47f?_47f.toString():obj.widgetType;
var ts=_480[wt];
if(!ts){
_480[wt]={"string":null,"node":null};
if(_47e){
ts={};
}else{
ts=_480[wt];
}
}
if((!obj.templateString)&&(!_47e)){
obj.templateString=_47d||ts["string"];
}
if(obj.templateString){
obj.templateString=this._sanitizeTemplateString(obj.templateString);
}
if((!obj.templateNode)&&(!_47e)){
obj.templateNode=ts["node"];
}
if((!obj.templateNode)&&(!obj.templateString)&&(_47f)){
var _484=this._sanitizeTemplateString(dojo.hostenv.getText(_47f));
obj.templateString=_484;
if(!_47e){
_480[wt]["string"]=_484;
}
}
if((!ts["string"])&&(!_47e)){
ts.string=obj.templateString;
}
};
dojo.widget._sanitizeTemplateString=function(_485){
if(_485){
_485=_485.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im,"");
var _486=_485.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
if(_486){
_485=_486[1];
}
}else{
_485="";
}
return _485;
};
dojo.widget._templateCache.dummyCount=0;
dojo.widget.attachProperties=["dojoAttachPoint","id"];
dojo.widget.eventAttachProperty="dojoAttachEvent";
dojo.widget.onBuildProperty="dojoOnBuild";
dojo.widget.waiNames=["waiRole","waiState"];
dojo.widget.wai={waiRole:{name:"waiRole","namespace":"http://www.w3.org/TR/xhtml2",alias:"x2",prefix:"wairole:"},waiState:{name:"waiState","namespace":"http://www.w3.org/2005/07/aaa",alias:"aaa",prefix:""},setAttr:function(node,ns,attr,_48a){
if(dojo.render.html.ie){
node.setAttribute(this[ns].alias+":"+attr,this[ns].prefix+_48a);
}else{
node.setAttributeNS(this[ns]["namespace"],attr,this[ns].prefix+_48a);
}
},getAttr:function(node,ns,attr){
if(dojo.render.html.ie){
return node.getAttribute(this[ns].alias+":"+attr);
}else{
return node.getAttributeNS(this[ns]["namespace"],attr);
}
},removeAttr:function(node,ns,attr){
var _491=true;
if(dojo.render.html.ie){
_491=node.removeAttribute(this[ns].alias+":"+attr);
}else{
node.removeAttributeNS(this[ns]["namespace"],attr);
}
return _491;
}};
dojo.widget.attachTemplateNodes=function(_492,_493,_494){
var _495=dojo.dom.ELEMENT_NODE;
function trim(str){
return str.replace(/^\s+|\s+$/g,"");
}
if(!_492){
_492=_493.domNode;
}
if(_492.nodeType!=_495){
return;
}
var _497=_492.all||_492.getElementsByTagName("*");
var _498=_493;
for(var x=-1;x<_497.length;x++){
var _49a=(x==-1)?_492:_497[x];
var _49b=[];
if(!_493.widgetsInTemplate||!_49a.getAttribute("dojoType")){
for(var y=0;y<this.attachProperties.length;y++){
var _49d=_49a.getAttribute(this.attachProperties[y]);
if(_49d){
_49b=_49d.split(";");
for(var z=0;z<_49b.length;z++){
if(dojo.lang.isArray(_493[_49b[z]])){
_493[_49b[z]].push(_49a);
}else{
_493[_49b[z]]=_49a;
}
}
break;
}
}
var _49f=_49a.getAttribute(this.eventAttachProperty);
if(_49f){
var evts=_49f.split(";");
for(var y=0;y<evts.length;y++){
if((!evts[y])||(!evts[y].length)){
continue;
}
var _4a1=null;
var tevt=trim(evts[y]);
if(evts[y].indexOf(":")>=0){
var _4a3=tevt.split(":");
tevt=trim(_4a3[0]);
_4a1=trim(_4a3[1]);
}
if(!_4a1){
_4a1=tevt;
}
var tf=function(){
var ntf=new String(_4a1);
return function(evt){
if(_498[ntf]){
_498[ntf](dojo.event.browser.fixEvent(evt,this));
}
};
}();
dojo.event.browser.addListener(_49a,tevt,tf,false,true);
}
}
for(var y=0;y<_494.length;y++){
var _4a7=_49a.getAttribute(_494[y]);
if((_4a7)&&(_4a7.length)){
var _4a1=null;
var _4a8=_494[y].substr(4);
_4a1=trim(_4a7);
var _4a9=[_4a1];
if(_4a1.indexOf(";")>=0){
_4a9=dojo.lang.map(_4a1.split(";"),trim);
}
for(var z=0;z<_4a9.length;z++){
if(!_4a9[z].length){
continue;
}
var tf=function(){
var ntf=new String(_4a9[z]);
return function(evt){
if(_498[ntf]){
_498[ntf](dojo.event.browser.fixEvent(evt,this));
}
};
}();
dojo.event.browser.addListener(_49a,_4a8,tf,false,true);
}
}
}
}
var _4ac=_49a.getAttribute(this.templateProperty);
if(_4ac){
_493[_4ac]=_49a;
}
dojo.lang.forEach(dojo.widget.waiNames,function(name){
var wai=dojo.widget.wai[name];
var val=_49a.getAttribute(wai.name);
if(val){
if(val.indexOf("-")==-1){
dojo.widget.wai.setAttr(_49a,wai.name,"role",val);
}else{
var _4b0=val.split("-");
dojo.widget.wai.setAttr(_49a,wai.name,_4b0[0],_4b0[1]);
}
}
},this);
var _4b1=_49a.getAttribute(this.onBuildProperty);
if(_4b1){
eval("var node = baseNode; var widget = targetObj; "+_4b1);
}
}
};
dojo.widget.getDojoEventsFromStr=function(str){
var re=/(dojoOn([a-z]+)(\s?))=/gi;
var evts=str?str.match(re)||[]:[];
var ret=[];
var lem={};
for(var x=0;x<evts.length;x++){
if(evts[x].length<1){
continue;
}
var cm=evts[x].replace(/\s/,"");
cm=(cm.slice(0,cm.length-1));
if(!lem[cm]){
lem[cm]=true;
ret.push(cm);
}
}
return ret;
};
dojo.declare("dojo.widget.DomWidget",dojo.widget.Widget,function(){
if((arguments.length>0)&&(typeof arguments[0]=="object")){
this.create(arguments[0]);
}
},{templateNode:null,templateString:null,templateCssString:null,preventClobber:false,domNode:null,containerNode:null,widgetsInTemplate:false,addChild:function(_4b9,_4ba,pos,ref,_4bd){
if(!this.isContainer){
dojo.debug("dojo.widget.DomWidget.addChild() attempted on non-container widget");
return null;
}else{
if(_4bd==undefined){
_4bd=this.children.length;
}
this.addWidgetAsDirectChild(_4b9,_4ba,pos,ref,_4bd);
this.registerChild(_4b9,_4bd);
}
return _4b9;
},addWidgetAsDirectChild:function(_4be,_4bf,pos,ref,_4c2){
if((!this.containerNode)&&(!_4bf)){
this.containerNode=this.domNode;
}
var cn=(_4bf)?_4bf:this.containerNode;
if(!pos){
pos="after";
}
if(!ref){
if(!cn){
cn=dojo.body();
}
ref=cn.lastChild;
}
if(!_4c2){
_4c2=0;
}
_4be.domNode.setAttribute("dojoinsertionindex",_4c2);
if(!ref){
cn.appendChild(_4be.domNode);
}else{
if(pos=="insertAtIndex"){
dojo.dom.insertAtIndex(_4be.domNode,ref.parentNode,_4c2);
}else{
if((pos=="after")&&(ref===cn.lastChild)){
cn.appendChild(_4be.domNode);
}else{
dojo.dom.insertAtPosition(_4be.domNode,cn,pos);
}
}
}
},registerChild:function(_4c4,_4c5){
_4c4.dojoInsertionIndex=_4c5;
var idx=-1;
for(var i=0;i<this.children.length;i++){
if(this.children[i].dojoInsertionIndex<=_4c5){
idx=i;
}
}
this.children.splice(idx+1,0,_4c4);
_4c4.parent=this;
_4c4.addedTo(this,idx+1);
delete dojo.widget.manager.topWidgets[_4c4.widgetId];
},removeChild:function(_4c8){
dojo.dom.removeNode(_4c8.domNode);
return dojo.widget.DomWidget.superclass.removeChild.call(this,_4c8);
},getFragNodeRef:function(frag){
if(!frag){
return null;
}
if(!frag[this.getNamespacedType()]){
dojo.raise("Error: no frag for widget type "+this.getNamespacedType()+", id "+this.widgetId+" (maybe a widget has set it's type incorrectly)");
}
return frag[this.getNamespacedType()]["nodeRef"];
},postInitialize:function(args,frag,_4cc){
var _4cd=this.getFragNodeRef(frag);
if(_4cc&&(_4cc.snarfChildDomOutput||!_4cd)){
_4cc.addWidgetAsDirectChild(this,"","insertAtIndex","",args["dojoinsertionindex"],_4cd);
}else{
if(_4cd){
if(this.domNode&&(this.domNode!==_4cd)){
this._sourceNodeRef=dojo.dom.replaceNode(_4cd,this.domNode);
}
}
}
if(_4cc){
_4cc.registerChild(this,args.dojoinsertionindex);
}else{
dojo.widget.manager.topWidgets[this.widgetId]=this;
}
if(this.widgetsInTemplate){
var _4ce=new dojo.xml.Parse();
var _4cf;
var _4d0=this.domNode.getElementsByTagName("*");
for(var i=0;i<_4d0.length;i++){
if(_4d0[i].getAttribute("dojoAttachPoint")=="subContainerWidget"){
_4cf=_4d0[i];
}
if(_4d0[i].getAttribute("dojoType")){
_4d0[i].setAttribute("isSubWidget",true);
}
}
if(this.isContainer&&!this.containerNode){
if(_4cf){
var src=this.getFragNodeRef(frag);
if(src){
dojo.dom.moveChildren(src,_4cf);
frag["dojoDontFollow"]=true;
}
}else{
dojo.debug("No subContainerWidget node can be found in template file for widget "+this);
}
}
var _4d3=_4ce.parseElement(this.domNode,null,true);
dojo.widget.getParser().createSubComponents(_4d3,this);
var _4d4=[];
var _4d5=[this];
var w;
while((w=_4d5.pop())){
for(var i=0;i<w.children.length;i++){
var _4d7=w.children[i];
if(_4d7._processedSubWidgets||!_4d7.extraArgs["issubwidget"]){
continue;
}
_4d4.push(_4d7);
if(_4d7.isContainer){
_4d5.push(_4d7);
}
}
}
for(var i=0;i<_4d4.length;i++){
var _4d8=_4d4[i];
if(_4d8._processedSubWidgets){
dojo.debug("This should not happen: widget._processedSubWidgets is already true!");
return;
}
_4d8._processedSubWidgets=true;
if(_4d8.extraArgs["dojoattachevent"]){
var evts=_4d8.extraArgs["dojoattachevent"].split(";");
for(var j=0;j<evts.length;j++){
var _4db=null;
var tevt=dojo.string.trim(evts[j]);
if(tevt.indexOf(":")>=0){
var _4dd=tevt.split(":");
tevt=dojo.string.trim(_4dd[0]);
_4db=dojo.string.trim(_4dd[1]);
}
if(!_4db){
_4db=tevt;
}
if(dojo.lang.isFunction(_4d8[tevt])){
dojo.event.kwConnect({srcObj:_4d8,srcFunc:tevt,targetObj:this,targetFunc:_4db});
}else{
alert(tevt+" is not a function in widget "+_4d8);
}
}
}
if(_4d8.extraArgs["dojoattachpoint"]){
this[_4d8.extraArgs["dojoattachpoint"]]=_4d8;
}
}
}
if(this.isContainer&&!frag["dojoDontFollow"]){
dojo.widget.getParser().createSubComponents(frag,this);
}
},buildRendering:function(args,frag){
var ts=dojo.widget._templateCache[this.widgetType];
if(args["templatecsspath"]){
args["templateCssPath"]=args["templatecsspath"];
}
var _4e1=args["templateCssPath"]||this.templateCssPath;
if(_4e1&&!dojo.widget._cssFiles[_4e1.toString()]){
if((!this.templateCssString)&&(_4e1)){
this.templateCssString=dojo.hostenv.getText(_4e1);
this.templateCssPath=null;
}
dojo.widget._cssFiles[_4e1.toString()]=true;
}
if((this["templateCssString"])&&(!dojo.widget._cssStrings[this.templateCssString])){
dojo.html.insertCssText(this.templateCssString,null,_4e1);
dojo.widget._cssStrings[this.templateCssString]=true;
}
if((!this.preventClobber)&&((this.templatePath)||(this.templateNode)||((this["templateString"])&&(this.templateString.length))||((typeof ts!="undefined")&&((ts["string"])||(ts["node"]))))){
this.buildFromTemplate(args,frag);
}else{
this.domNode=this.getFragNodeRef(frag);
}
this.fillInTemplate(args,frag);
},buildFromTemplate:function(args,frag){
var _4e4=false;
if(args["templatepath"]){
args["templatePath"]=args["templatepath"];
}
dojo.widget.fillFromTemplateCache(this,args["templatePath"],null,_4e4);
var ts=dojo.widget._templateCache[this.templatePath?this.templatePath.toString():this.widgetType];
if((ts)&&(!_4e4)){
if(!this.templateString.length){
this.templateString=ts["string"];
}
if(!this.templateNode){
this.templateNode=ts["node"];
}
}
var _4e6=false;
var node=null;
var tstr=this.templateString;
if((!this.templateNode)&&(this.templateString)){
_4e6=this.templateString.match(/\$\{([^\}]+)\}/g);
if(_4e6){
var hash=this.strings||{};
for(var key in dojo.widget.defaultStrings){
if(dojo.lang.isUndefined(hash[key])){
hash[key]=dojo.widget.defaultStrings[key];
}
}
for(var i=0;i<_4e6.length;i++){
var key=_4e6[i];
key=key.substring(2,key.length-1);
var kval=(key.substring(0,5)=="this.")?dojo.lang.getObjPathValue(key.substring(5),this):hash[key];
var _4ed;
if((kval)||(dojo.lang.isString(kval))){
_4ed=new String((dojo.lang.isFunction(kval))?kval.call(this,key,this.templateString):kval);
while(_4ed.indexOf("\"")>-1){
_4ed=_4ed.replace("\"","&quot;");
}
tstr=tstr.replace(_4e6[i],_4ed);
}
}
}else{
this.templateNode=this.createNodesFromText(this.templateString,true)[0];
if(!_4e4){
ts.node=this.templateNode;
}
}
}
if((!this.templateNode)&&(!_4e6)){
dojo.debug("DomWidget.buildFromTemplate: could not create template");
return false;
}else{
if(!_4e6){
node=this.templateNode.cloneNode(true);
if(!node){
return false;
}
}else{
node=this.createNodesFromText(tstr,true)[0];
}
}
this.domNode=node;
this.attachTemplateNodes();
if(this.isContainer&&this.containerNode){
var src=this.getFragNodeRef(frag);
if(src){
dojo.dom.moveChildren(src,this.containerNode);
}
}
},attachTemplateNodes:function(_4ef,_4f0){
if(!_4ef){
_4ef=this.domNode;
}
if(!_4f0){
_4f0=this;
}
return dojo.widget.attachTemplateNodes(_4ef,_4f0,dojo.widget.getDojoEventsFromStr(this.templateString));
},fillInTemplate:function(){
},destroyRendering:function(){
try{
dojo.dom.destroyNode(this.domNode);
delete this.domNode;
}
catch(e){
}
if(this._sourceNodeRef){
try{
dojo.dom.destroyNode(this._sourceNodeRef);
}
catch(e){
}
}
},createNodesFromText:function(){
dojo.unimplemented("dojo.widget.DomWidget.createNodesFromText");
}});
dojo.provide("dojo.html.display");
dojo.html._toggle=function(node,_4f2,_4f3){
node=dojo.byId(node);
_4f3(node,!_4f2(node));
return _4f2(node);
};
dojo.html.show=function(node){
node=dojo.byId(node);
if(dojo.html.getStyleProperty(node,"display")=="none"){
dojo.html.setStyle(node,"display",(node.dojoDisplayCache||""));
node.dojoDisplayCache=undefined;
}
};
dojo.html.hide=function(node){
node=dojo.byId(node);
if(typeof node["dojoDisplayCache"]=="undefined"){
var d=dojo.html.getStyleProperty(node,"display");
if(d!="none"){
node.dojoDisplayCache=d;
}
}
dojo.html.setStyle(node,"display","none");
};
dojo.html.setShowing=function(node,_4f8){
dojo.html[(_4f8?"show":"hide")](node);
};
dojo.html.isShowing=function(node){
return (dojo.html.getStyleProperty(node,"display")!="none");
};
dojo.html.toggleShowing=function(node){
return dojo.html._toggle(node,dojo.html.isShowing,dojo.html.setShowing);
};
dojo.html.displayMap={tr:"",td:"",th:"",img:"inline",span:"inline",input:"inline",button:"inline"};
dojo.html.suggestDisplayByTagName=function(node){
node=dojo.byId(node);
if(node&&node.tagName){
var tag=node.tagName.toLowerCase();
return (tag in dojo.html.displayMap?dojo.html.displayMap[tag]:"block");
}
};
dojo.html.setDisplay=function(node,_4fe){
dojo.html.setStyle(node,"display",((_4fe instanceof String||typeof _4fe=="string")?_4fe:(_4fe?dojo.html.suggestDisplayByTagName(node):"none")));
};
dojo.html.isDisplayed=function(node){
return (dojo.html.getComputedStyle(node,"display")!="none");
};
dojo.html.toggleDisplay=function(node){
return dojo.html._toggle(node,dojo.html.isDisplayed,dojo.html.setDisplay);
};
dojo.html.setVisibility=function(node,_502){
dojo.html.setStyle(node,"visibility",((_502 instanceof String||typeof _502=="string")?_502:(_502?"visible":"hidden")));
};
dojo.html.isVisible=function(node){
return (dojo.html.getComputedStyle(node,"visibility")!="hidden");
};
dojo.html.toggleVisibility=function(node){
return dojo.html._toggle(node,dojo.html.isVisible,dojo.html.setVisibility);
};
dojo.html.setOpacity=function(node,_506,_507){
node=dojo.byId(node);
var h=dojo.render.html;
if(!_507){
if(_506>=1){
if(h.ie){
dojo.html.clearOpacity(node);
return;
}else{
_506=0.999999;
}
}else{
if(_506<0){
_506=0;
}
}
}
if(h.ie){
if(node.nodeName.toLowerCase()=="tr"){
var tds=node.getElementsByTagName("td");
for(var x=0;x<tds.length;x++){
tds[x].style.filter="Alpha(Opacity="+_506*100+")";
}
}
node.style.filter="Alpha(Opacity="+_506*100+")";
}else{
if(h.moz){
node.style.opacity=_506;
node.style.MozOpacity=_506;
}else{
if(h.safari){
node.style.opacity=_506;
node.style.KhtmlOpacity=_506;
}else{
node.style.opacity=_506;
}
}
}
};
dojo.html.clearOpacity=function(node){
node=dojo.byId(node);
var ns=node.style;
var h=dojo.render.html;
if(h.ie){
try{
if(node.filters&&node.filters.alpha){
ns.filter="";
}
}
catch(e){
}
}else{
if(h.moz){
ns.opacity=1;
ns.MozOpacity=1;
}else{
if(h.safari){
ns.opacity=1;
ns.KhtmlOpacity=1;
}else{
ns.opacity=1;
}
}
}
};
dojo.html.getOpacity=function(node){
node=dojo.byId(node);
var h=dojo.render.html;
if(h.ie){
var opac=(node.filters&&node.filters.alpha&&typeof node.filters.alpha.opacity=="number"?node.filters.alpha.opacity:100)/100;
}else{
var opac=node.style.opacity||node.style.MozOpacity||node.style.KhtmlOpacity||1;
}
return opac>=0.999999?1:Number(opac);
};
dojo.provide("dojo.html.layout");
dojo.html.sumAncestorProperties=function(node,prop){
node=dojo.byId(node);
if(!node){
return 0;
}
var _513=0;
while(node){
if(dojo.html.getComputedStyle(node,"position")=="fixed"){
return 0;
}
var val=node[prop];
if(val){
_513+=val-0;
if(node==dojo.body()){
break;
}
}
node=node.parentNode;
}
return _513;
};
dojo.html.setStyleAttributes=function(node,_516){
node=dojo.byId(node);
var _517=_516.replace(/(;)?\s*$/,"").split(";");
for(var i=0;i<_517.length;i++){
var _519=_517[i].split(":");
var name=_519[0].replace(/\s*$/,"").replace(/^\s*/,"").toLowerCase();
var _51b=_519[1].replace(/\s*$/,"").replace(/^\s*/,"");
switch(name){
case "opacity":
dojo.html.setOpacity(node,_51b);
break;
case "content-height":
dojo.html.setContentBox(node,{height:_51b});
break;
case "content-width":
dojo.html.setContentBox(node,{width:_51b});
break;
case "outer-height":
dojo.html.setMarginBox(node,{height:_51b});
break;
case "outer-width":
dojo.html.setMarginBox(node,{width:_51b});
break;
default:
node.style[dojo.html.toCamelCase(name)]=_51b;
}
}
};
dojo.html.boxSizing={MARGIN_BOX:"margin-box",BORDER_BOX:"border-box",PADDING_BOX:"padding-box",CONTENT_BOX:"content-box"};
dojo.html.getAbsolutePosition=dojo.html.abs=function(node,_51d,_51e){
node=dojo.byId(node,node.ownerDocument);
var ret={x:0,y:0};
var bs=dojo.html.boxSizing;
if(!_51e){
_51e=bs.CONTENT_BOX;
}
var _521=2;
var _522;
switch(_51e){
case bs.MARGIN_BOX:
_522=3;
break;
case bs.BORDER_BOX:
_522=2;
break;
case bs.PADDING_BOX:
default:
_522=1;
break;
case bs.CONTENT_BOX:
_522=0;
break;
}
var h=dojo.render.html;
var db=document["body"]||document["documentElement"];
if(h.ie){
with(node.getBoundingClientRect()){
ret.x=left-2;
ret.y=top-2;
}
}else{
if(document.getBoxObjectFor){
_521=1;
try{
var bo=document.getBoxObjectFor(node);
ret.x=bo.x-dojo.html.sumAncestorProperties(node,"scrollLeft");
ret.y=bo.y-dojo.html.sumAncestorProperties(node,"scrollTop");
}
catch(e){
}
}else{
if(node["offsetParent"]){
var _526;
if((h.safari)&&(node.style.getPropertyValue("position")=="absolute")&&(node.parentNode==db)){
_526=db;
}else{
_526=db.parentNode;
}
if(node.parentNode!=db){
var nd=node;
if(dojo.render.html.opera){
nd=db;
}
ret.x-=dojo.html.sumAncestorProperties(nd,"scrollLeft");
ret.y-=dojo.html.sumAncestorProperties(nd,"scrollTop");
}
var _528=node;
do{
var n=_528["offsetLeft"];
if(!h.opera||n>0){
ret.x+=isNaN(n)?0:n;
}
var m=_528["offsetTop"];
ret.y+=isNaN(m)?0:m;
_528=_528.offsetParent;
}while((_528!=_526)&&(_528!=null));
}else{
if(node["x"]&&node["y"]){
ret.x+=isNaN(node.x)?0:node.x;
ret.y+=isNaN(node.y)?0:node.y;
}
}
}
}
if(_51d){
var _52b=dojo.html.getScroll();
ret.y+=_52b.top;
ret.x+=_52b.left;
}
var _52c=[dojo.html.getPaddingExtent,dojo.html.getBorderExtent,dojo.html.getMarginExtent];
if(_521>_522){
for(var i=_522;i<_521;++i){
ret.y+=_52c[i](node,"top");
ret.x+=_52c[i](node,"left");
}
}else{
if(_521<_522){
for(var i=_522;i>_521;--i){
ret.y-=_52c[i-1](node,"top");
ret.x-=_52c[i-1](node,"left");
}
}
}
ret.top=ret.y;
ret.left=ret.x;
return ret;
};
dojo.html.isPositionAbsolute=function(node){
return (dojo.html.getComputedStyle(node,"position")=="absolute");
};
dojo.html._sumPixelValues=function(node,_530,_531){
var _532=0;
for(var x=0;x<_530.length;x++){
_532+=dojo.html.getPixelValue(node,_530[x],_531);
}
return _532;
};
dojo.html.getMargin=function(node){
return {width:dojo.html._sumPixelValues(node,["margin-left","margin-right"],(dojo.html.getComputedStyle(node,"position")=="absolute")),height:dojo.html._sumPixelValues(node,["margin-top","margin-bottom"],(dojo.html.getComputedStyle(node,"position")=="absolute"))};
};
dojo.html.getBorder=function(node){
return {width:dojo.html.getBorderExtent(node,"left")+dojo.html.getBorderExtent(node,"right"),height:dojo.html.getBorderExtent(node,"top")+dojo.html.getBorderExtent(node,"bottom")};
};
dojo.html.getBorderExtent=function(node,side){
return (dojo.html.getStyle(node,"border-"+side+"-style")=="none"?0:dojo.html.getPixelValue(node,"border-"+side+"-width"));
};
dojo.html.getMarginExtent=function(node,side){
return dojo.html._sumPixelValues(node,["margin-"+side],dojo.html.isPositionAbsolute(node));
};
dojo.html.getPaddingExtent=function(node,side){
return dojo.html._sumPixelValues(node,["padding-"+side],true);
};
dojo.html.getPadding=function(node){
return {width:dojo.html._sumPixelValues(node,["padding-left","padding-right"],true),height:dojo.html._sumPixelValues(node,["padding-top","padding-bottom"],true)};
};
dojo.html.getPadBorder=function(node){
var pad=dojo.html.getPadding(node);
var _53f=dojo.html.getBorder(node);
return {width:pad.width+_53f.width,height:pad.height+_53f.height};
};
dojo.html.getBoxSizing=function(node){
var h=dojo.render.html;
var bs=dojo.html.boxSizing;
if(((h.ie)||(h.opera))&&node.nodeName.toLowerCase()!="img"){
var cm=document["compatMode"];
if((cm=="BackCompat")||(cm=="QuirksMode")){
return bs.BORDER_BOX;
}else{
return bs.CONTENT_BOX;
}
}else{
if(arguments.length==0){
node=document.documentElement;
}
var _544;
if(!h.ie){
_544=dojo.html.getStyle(node,"-moz-box-sizing");
if(!_544){
_544=dojo.html.getStyle(node,"box-sizing");
}
}
return (_544?_544:bs.CONTENT_BOX);
}
};
dojo.html.isBorderBox=function(node){
return (dojo.html.getBoxSizing(node)==dojo.html.boxSizing.BORDER_BOX);
};
dojo.html.getBorderBox=function(node){
node=dojo.byId(node);
return {width:node.offsetWidth,height:node.offsetHeight};
};
dojo.html.getPaddingBox=function(node){
var box=dojo.html.getBorderBox(node);
var _549=dojo.html.getBorder(node);
return {width:box.width-_549.width,height:box.height-_549.height};
};
dojo.html.getContentBox=function(node){
node=dojo.byId(node);
var _54b=dojo.html.getPadBorder(node);
return {width:node.offsetWidth-_54b.width,height:node.offsetHeight-_54b.height};
};
dojo.html.setContentBox=function(node,args){
node=dojo.byId(node);
var _54e=0;
var _54f=0;
var isbb=dojo.html.isBorderBox(node);
var _551=(isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var ret={};
if(typeof args.width!="undefined"){
_54e=args.width+_551.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_54e);
}
if(typeof args.height!="undefined"){
_54f=args.height+_551.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_54f);
}
return ret;
};
dojo.html.getMarginBox=function(node){
var _554=dojo.html.getBorderBox(node);
var _555=dojo.html.getMargin(node);
return {width:_554.width+_555.width,height:_554.height+_555.height};
};
dojo.html.setMarginBox=function(node,args){
node=dojo.byId(node);
var _558=0;
var _559=0;
var isbb=dojo.html.isBorderBox(node);
var _55b=(!isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var _55c=dojo.html.getMargin(node);
var ret={};
if(typeof args.width!="undefined"){
_558=args.width-_55b.width;
_558-=_55c.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_558);
}
if(typeof args.height!="undefined"){
_559=args.height-_55b.height;
_559-=_55c.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_559);
}
return ret;
};
dojo.html.getElementBox=function(node,type){
var bs=dojo.html.boxSizing;
switch(type){
case bs.MARGIN_BOX:
return dojo.html.getMarginBox(node);
case bs.BORDER_BOX:
return dojo.html.getBorderBox(node);
case bs.PADDING_BOX:
return dojo.html.getPaddingBox(node);
case bs.CONTENT_BOX:
default:
return dojo.html.getContentBox(node);
}
};
dojo.html.toCoordinateObject=dojo.html.toCoordinateArray=function(_561,_562,_563){
if(_561 instanceof Array||typeof _561=="array"){
dojo.deprecated("dojo.html.toCoordinateArray","use dojo.html.toCoordinateObject({left: , top: , width: , height: }) instead","0.5");
while(_561.length<4){
_561.push(0);
}
while(_561.length>4){
_561.pop();
}
var ret={left:_561[0],top:_561[1],width:_561[2],height:_561[3]};
}else{
if(!_561.nodeType&&!(_561 instanceof String||typeof _561=="string")&&("width" in _561||"height" in _561||"left" in _561||"x" in _561||"top" in _561||"y" in _561)){
var ret={left:_561.left||_561.x||0,top:_561.top||_561.y||0,width:_561.width||0,height:_561.height||0};
}else{
var node=dojo.byId(_561);
var pos=dojo.html.abs(node,_562,_563);
var _567=dojo.html.getMarginBox(node);
var ret={left:pos.left,top:pos.top,width:_567.width,height:_567.height};
}
}
ret.x=ret.left;
ret.y=ret.top;
return ret;
};
dojo.html.setMarginBoxWidth=dojo.html.setOuterWidth=function(node,_569){
return dojo.html._callDeprecated("setMarginBoxWidth","setMarginBox",arguments,"width");
};
dojo.html.setMarginBoxHeight=dojo.html.setOuterHeight=function(){
return dojo.html._callDeprecated("setMarginBoxHeight","setMarginBox",arguments,"height");
};
dojo.html.getMarginBoxWidth=dojo.html.getOuterWidth=function(){
return dojo.html._callDeprecated("getMarginBoxWidth","getMarginBox",arguments,null,"width");
};
dojo.html.getMarginBoxHeight=dojo.html.getOuterHeight=function(){
return dojo.html._callDeprecated("getMarginBoxHeight","getMarginBox",arguments,null,"height");
};
dojo.html.getTotalOffset=function(node,type,_56c){
return dojo.html._callDeprecated("getTotalOffset","getAbsolutePosition",arguments,null,type);
};
dojo.html.getAbsoluteX=function(node,_56e){
return dojo.html._callDeprecated("getAbsoluteX","getAbsolutePosition",arguments,null,"x");
};
dojo.html.getAbsoluteY=function(node,_570){
return dojo.html._callDeprecated("getAbsoluteY","getAbsolutePosition",arguments,null,"y");
};
dojo.html.totalOffsetLeft=function(node,_572){
return dojo.html._callDeprecated("totalOffsetLeft","getAbsolutePosition",arguments,null,"left");
};
dojo.html.totalOffsetTop=function(node,_574){
return dojo.html._callDeprecated("totalOffsetTop","getAbsolutePosition",arguments,null,"top");
};
dojo.html.getMarginWidth=function(node){
return dojo.html._callDeprecated("getMarginWidth","getMargin",arguments,null,"width");
};
dojo.html.getMarginHeight=function(node){
return dojo.html._callDeprecated("getMarginHeight","getMargin",arguments,null,"height");
};
dojo.html.getBorderWidth=function(node){
return dojo.html._callDeprecated("getBorderWidth","getBorder",arguments,null,"width");
};
dojo.html.getBorderHeight=function(node){
return dojo.html._callDeprecated("getBorderHeight","getBorder",arguments,null,"height");
};
dojo.html.getPaddingWidth=function(node){
return dojo.html._callDeprecated("getPaddingWidth","getPadding",arguments,null,"width");
};
dojo.html.getPaddingHeight=function(node){
return dojo.html._callDeprecated("getPaddingHeight","getPadding",arguments,null,"height");
};
dojo.html.getPadBorderWidth=function(node){
return dojo.html._callDeprecated("getPadBorderWidth","getPadBorder",arguments,null,"width");
};
dojo.html.getPadBorderHeight=function(node){
return dojo.html._callDeprecated("getPadBorderHeight","getPadBorder",arguments,null,"height");
};
dojo.html.getBorderBoxWidth=dojo.html.getInnerWidth=function(){
return dojo.html._callDeprecated("getBorderBoxWidth","getBorderBox",arguments,null,"width");
};
dojo.html.getBorderBoxHeight=dojo.html.getInnerHeight=function(){
return dojo.html._callDeprecated("getBorderBoxHeight","getBorderBox",arguments,null,"height");
};
dojo.html.getContentBoxWidth=dojo.html.getContentWidth=function(){
return dojo.html._callDeprecated("getContentBoxWidth","getContentBox",arguments,null,"width");
};
dojo.html.getContentBoxHeight=dojo.html.getContentHeight=function(){
return dojo.html._callDeprecated("getContentBoxHeight","getContentBox",arguments,null,"height");
};
dojo.html.setContentBoxWidth=dojo.html.setContentWidth=function(node,_57e){
return dojo.html._callDeprecated("setContentBoxWidth","setContentBox",arguments,"width");
};
dojo.html.setContentBoxHeight=dojo.html.setContentHeight=function(node,_580){
return dojo.html._callDeprecated("setContentBoxHeight","setContentBox",arguments,"height");
};
dojo.provide("dojo.html.util");
dojo.html.getElementWindow=function(_581){
return dojo.html.getDocumentWindow(_581.ownerDocument);
};
dojo.html.getDocumentWindow=function(doc){
if(dojo.render.html.safari&&!doc._parentWindow){
var fix=function(win){
win.document._parentWindow=win;
for(var i=0;i<win.frames.length;i++){
fix(win.frames[i]);
}
};
fix(window.top);
}
if(dojo.render.html.ie&&window!==document.parentWindow&&!doc._parentWindow){
doc.parentWindow.execScript("document._parentWindow = window;","Javascript");
var win=doc._parentWindow;
doc._parentWindow=null;
return win;
}
return doc._parentWindow||doc.parentWindow||doc.defaultView;
};
dojo.html.gravity=function(node,e){
node=dojo.byId(node);
var _589=dojo.html.getCursorPosition(e);
with(dojo.html){
var _58a=getAbsolutePosition(node,true);
var bb=getBorderBox(node);
var _58c=_58a.x+(bb.width/2);
var _58d=_58a.y+(bb.height/2);
}
with(dojo.html.gravity){
return ((_589.x<_58c?WEST:EAST)|(_589.y<_58d?NORTH:SOUTH));
}
};
dojo.html.gravity.NORTH=1;
dojo.html.gravity.SOUTH=1<<1;
dojo.html.gravity.EAST=1<<2;
dojo.html.gravity.WEST=1<<3;
dojo.html.overElement=function(_58e,e){
_58e=dojo.byId(_58e);
var _590=dojo.html.getCursorPosition(e);
var bb=dojo.html.getBorderBox(_58e);
var _592=dojo.html.getAbsolutePosition(_58e,true,dojo.html.boxSizing.BORDER_BOX);
var top=_592.y;
var _594=top+bb.height;
var left=_592.x;
var _596=left+bb.width;
return (_590.x>=left&&_590.x<=_596&&_590.y>=top&&_590.y<=_594);
};
dojo.html.renderedTextContent=function(node){
node=dojo.byId(node);
var _598="";
if(node==null){
return _598;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
var _59a="unknown";
try{
_59a=dojo.html.getStyle(node.childNodes[i],"display");
}
catch(E){
}
switch(_59a){
case "block":
case "list-item":
case "run-in":
case "table":
case "table-row-group":
case "table-header-group":
case "table-footer-group":
case "table-row":
case "table-column-group":
case "table-column":
case "table-cell":
case "table-caption":
_598+="\n";
_598+=dojo.html.renderedTextContent(node.childNodes[i]);
_598+="\n";
break;
case "none":
break;
default:
if(node.childNodes[i].tagName&&node.childNodes[i].tagName.toLowerCase()=="br"){
_598+="\n";
}else{
_598+=dojo.html.renderedTextContent(node.childNodes[i]);
}
break;
}
break;
case 3:
case 2:
case 4:
var text=node.childNodes[i].nodeValue;
var _59c="unknown";
try{
_59c=dojo.html.getStyle(node,"text-transform");
}
catch(E){
}
switch(_59c){
case "capitalize":
var _59d=text.split(" ");
for(var i=0;i<_59d.length;i++){
_59d[i]=_59d[i].charAt(0).toUpperCase()+_59d[i].substring(1);
}
text=_59d.join(" ");
break;
case "uppercase":
text=text.toUpperCase();
break;
case "lowercase":
text=text.toLowerCase();
break;
default:
break;
}
switch(_59c){
case "nowrap":
break;
case "pre-wrap":
break;
case "pre-line":
break;
case "pre":
break;
default:
text=text.replace(/\s+/," ");
if(/\s$/.test(_598)){
text.replace(/^\s/,"");
}
break;
}
_598+=text;
break;
default:
break;
}
}
return _598;
};
dojo.html.createNodesFromText=function(txt,trim){
if(trim){
txt=txt.replace(/^\s+|\s+$/g,"");
}
var tn=dojo.doc().createElement("div");
tn.style.visibility="hidden";
dojo.body().appendChild(tn);
var _5a1="none";
if((/^<t[dh][\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table><tbody><tr>"+txt+"</tr></tbody></table>";
_5a1="cell";
}else{
if((/^<tr[\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table><tbody>"+txt+"</tbody></table>";
_5a1="row";
}else{
if((/^<(thead|tbody|tfoot)[\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table>"+txt+"</table>";
_5a1="section";
}
}
}
tn.innerHTML=txt;
if(tn["normalize"]){
tn.normalize();
}
var _5a2=null;
switch(_5a1){
case "cell":
_5a2=tn.getElementsByTagName("tr")[0];
break;
case "row":
_5a2=tn.getElementsByTagName("tbody")[0];
break;
case "section":
_5a2=tn.getElementsByTagName("table")[0];
break;
default:
_5a2=tn;
break;
}
var _5a3=[];
for(var x=0;x<_5a2.childNodes.length;x++){
_5a3.push(_5a2.childNodes[x].cloneNode(true));
}
tn.style.display="none";
dojo.html.destroyNode(tn);
return _5a3;
};
dojo.html.placeOnScreen=function(node,_5a6,_5a7,_5a8,_5a9,_5aa,_5ab){
if(_5a6 instanceof Array||typeof _5a6=="array"){
_5ab=_5aa;
_5aa=_5a9;
_5a9=_5a8;
_5a8=_5a7;
_5a7=_5a6[1];
_5a6=_5a6[0];
}
if(_5aa instanceof String||typeof _5aa=="string"){
_5aa=_5aa.split(",");
}
if(!isNaN(_5a8)){
_5a8=[Number(_5a8),Number(_5a8)];
}else{
if(!(_5a8 instanceof Array||typeof _5a8=="array")){
_5a8=[0,0];
}
}
var _5ac=dojo.html.getScroll().offset;
var view=dojo.html.getViewport();
node=dojo.byId(node);
var _5ae=node.style.display;
node.style.display="";
var bb=dojo.html.getBorderBox(node);
var w=bb.width;
var h=bb.height;
node.style.display=_5ae;
if(!(_5aa instanceof Array||typeof _5aa=="array")){
_5aa=["TL"];
}
var _5b2,_5b3,_5b4=Infinity,_5b5;
for(var _5b6=0;_5b6<_5aa.length;++_5b6){
var _5b7=_5aa[_5b6];
var _5b8=true;
var tryX=_5a6-(_5b7.charAt(1)=="L"?0:w)+_5a8[0]*(_5b7.charAt(1)=="L"?1:-1);
var tryY=_5a7-(_5b7.charAt(0)=="T"?0:h)+_5a8[1]*(_5b7.charAt(0)=="T"?1:-1);
if(_5a9){
tryX-=_5ac.x;
tryY-=_5ac.y;
}
if(tryX<0){
tryX=0;
_5b8=false;
}
if(tryY<0){
tryY=0;
_5b8=false;
}
var x=tryX+w;
if(x>view.width){
x=view.width-w;
_5b8=false;
}else{
x=tryX;
}
x=Math.max(_5a8[0],x)+_5ac.x;
var y=tryY+h;
if(y>view.height){
y=view.height-h;
_5b8=false;
}else{
y=tryY;
}
y=Math.max(_5a8[1],y)+_5ac.y;
if(_5b8){
_5b2=x;
_5b3=y;
_5b4=0;
_5b5=_5b7;
break;
}else{
var dist=Math.pow(x-tryX-_5ac.x,2)+Math.pow(y-tryY-_5ac.y,2);
if(_5b4>dist){
_5b4=dist;
_5b2=x;
_5b3=y;
_5b5=_5b7;
}
}
}
if(!_5ab){
node.style.left=_5b2+"px";
node.style.top=_5b3+"px";
}
return {left:_5b2,top:_5b3,x:_5b2,y:_5b3,dist:_5b4,corner:_5b5};
};
dojo.html.placeOnScreenPoint=function(node,_5bf,_5c0,_5c1,_5c2){
dojo.deprecated("dojo.html.placeOnScreenPoint","use dojo.html.placeOnScreen() instead","0.5");
return dojo.html.placeOnScreen(node,_5bf,_5c0,_5c1,_5c2,["TL","TR","BL","BR"]);
};
dojo.html.placeOnScreenAroundElement=function(node,_5c4,_5c5,_5c6,_5c7,_5c8){
var best,_5ca=Infinity;
_5c4=dojo.byId(_5c4);
var _5cb=_5c4.style.display;
_5c4.style.display="";
var mb=dojo.html.getElementBox(_5c4,_5c6);
var _5cd=mb.width;
var _5ce=mb.height;
var _5cf=dojo.html.getAbsolutePosition(_5c4,true,_5c6);
_5c4.style.display=_5cb;
for(var _5d0 in _5c7){
var pos,_5d2,_5d3;
var _5d4=_5c7[_5d0];
_5d2=_5cf.x+(_5d0.charAt(1)=="L"?0:_5cd);
_5d3=_5cf.y+(_5d0.charAt(0)=="T"?0:_5ce);
pos=dojo.html.placeOnScreen(node,_5d2,_5d3,_5c5,true,_5d4,true);
if(pos.dist==0){
best=pos;
break;
}else{
if(_5ca>pos.dist){
_5ca=pos.dist;
best=pos;
}
}
}
if(!_5c8){
node.style.left=best.left+"px";
node.style.top=best.top+"px";
}
return best;
};
dojo.html.scrollIntoView=function(node){
if(!node){
return;
}
if(dojo.render.html.ie){
if(dojo.html.getBorderBox(node.parentNode).height<=node.parentNode.scrollHeight){
node.scrollIntoView(false);
}
}else{
if(dojo.render.html.mozilla){
node.scrollIntoView(false);
}else{
var _5d6=node.parentNode;
var _5d7=_5d6.scrollTop+dojo.html.getBorderBox(_5d6).height;
var _5d8=node.offsetTop+dojo.html.getMarginBox(node).height;
if(_5d7<_5d8){
_5d6.scrollTop+=(_5d8-_5d7);
}else{
if(_5d6.scrollTop>node.offsetTop){
_5d6.scrollTop-=(_5d6.scrollTop-node.offsetTop);
}
}
}
}
};
dojo.provide("dojo.gfx.color");
dojo.gfx.color.Color=function(r,g,b,a){
if(dojo.lang.isArray(r)){
this.r=r[0];
this.g=r[1];
this.b=r[2];
this.a=r[3]||1;
}else{
if(dojo.lang.isString(r)){
var rgb=dojo.gfx.color.extractRGB(r);
this.r=rgb[0];
this.g=rgb[1];
this.b=rgb[2];
this.a=g||1;
}else{
if(r instanceof dojo.gfx.color.Color){
this.r=r.r;
this.b=r.b;
this.g=r.g;
this.a=r.a;
}else{
this.r=r;
this.g=g;
this.b=b;
this.a=a;
}
}
}
};
dojo.gfx.color.Color.fromArray=function(arr){
return new dojo.gfx.color.Color(arr[0],arr[1],arr[2],arr[3]);
};
dojo.extend(dojo.gfx.color.Color,{toRgb:function(_5df){
if(_5df){
return this.toRgba();
}else{
return [this.r,this.g,this.b];
}
},toRgba:function(){
return [this.r,this.g,this.b,this.a];
},toHex:function(){
return dojo.gfx.color.rgb2hex(this.toRgb());
},toCss:function(){
return "rgb("+this.toRgb().join()+")";
},toString:function(){
return this.toHex();
},blend:function(_5e0,_5e1){
var rgb=null;
if(dojo.lang.isArray(_5e0)){
rgb=_5e0;
}else{
if(_5e0 instanceof dojo.gfx.color.Color){
rgb=_5e0.toRgb();
}else{
rgb=new dojo.gfx.color.Color(_5e0).toRgb();
}
}
return dojo.gfx.color.blend(this.toRgb(),rgb,_5e1);
}});
dojo.gfx.color.named={white:[255,255,255],black:[0,0,0],red:[255,0,0],green:[0,255,0],lime:[0,255,0],blue:[0,0,255],navy:[0,0,128],gray:[128,128,128],silver:[192,192,192]};
dojo.gfx.color.blend=function(a,b,_5e5){
if(typeof a=="string"){
return dojo.gfx.color.blendHex(a,b,_5e5);
}
if(!_5e5){
_5e5=0;
}
_5e5=Math.min(Math.max(-1,_5e5),1);
_5e5=((_5e5+1)/2);
var c=[];
for(var x=0;x<3;x++){
c[x]=parseInt(b[x]+((a[x]-b[x])*_5e5));
}
return c;
};
dojo.gfx.color.blendHex=function(a,b,_5ea){
return dojo.gfx.color.rgb2hex(dojo.gfx.color.blend(dojo.gfx.color.hex2rgb(a),dojo.gfx.color.hex2rgb(b),_5ea));
};
dojo.gfx.color.extractRGB=function(_5eb){
var hex="0123456789abcdef";
_5eb=_5eb.toLowerCase();
if(_5eb.indexOf("rgb")==0){
var _5ed=_5eb.match(/rgba*\((\d+), *(\d+), *(\d+)/i);
var ret=_5ed.splice(1,3);
return ret;
}else{
var _5ef=dojo.gfx.color.hex2rgb(_5eb);
if(_5ef){
return _5ef;
}else{
return dojo.gfx.color.named[_5eb]||[255,255,255];
}
}
};
dojo.gfx.color.hex2rgb=function(hex){
var _5f1="0123456789ABCDEF";
var rgb=new Array(3);
if(hex.indexOf("#")==0){
hex=hex.substring(1);
}
hex=hex.toUpperCase();
if(hex.replace(new RegExp("["+_5f1+"]","g"),"")!=""){
return null;
}
if(hex.length==3){
rgb[0]=hex.charAt(0)+hex.charAt(0);
rgb[1]=hex.charAt(1)+hex.charAt(1);
rgb[2]=hex.charAt(2)+hex.charAt(2);
}else{
rgb[0]=hex.substring(0,2);
rgb[1]=hex.substring(2,4);
rgb[2]=hex.substring(4);
}
for(var i=0;i<rgb.length;i++){
rgb[i]=_5f1.indexOf(rgb[i].charAt(0))*16+_5f1.indexOf(rgb[i].charAt(1));
}
return rgb;
};
dojo.gfx.color.rgb2hex=function(r,g,b){
if(dojo.lang.isArray(r)){
g=r[1]||0;
b=r[2]||0;
r=r[0]||0;
}
var ret=dojo.lang.map([r,g,b],function(x){
x=new Number(x);
var s=x.toString(16);
while(s.length<2){
s="0"+s;
}
return s;
});
ret.unshift("#");
return ret.join("");
};
dojo.provide("dojo.lfx.Animation");
dojo.lfx.Line=function(_5fa,end){
this.start=_5fa;
this.end=end;
if(dojo.lang.isArray(_5fa)){
var diff=[];
dojo.lang.forEach(this.start,function(s,i){
diff[i]=this.end[i]-s;
},this);
this.getValue=function(n){
var res=[];
dojo.lang.forEach(this.start,function(s,i){
res[i]=(diff[i]*n)+s;
},this);
return res;
};
}else{
var diff=end-_5fa;
this.getValue=function(n){
return (diff*n)+this.start;
};
}
};
if((dojo.render.html.khtml)&&(!dojo.render.html.safari)){
dojo.lfx.easeDefault=function(n){
return (parseFloat("0.5")+((Math.sin((n+parseFloat("1.5"))*Math.PI))/2));
};
}else{
dojo.lfx.easeDefault=function(n){
return (0.5+((Math.sin((n+1.5)*Math.PI))/2));
};
}
dojo.lfx.easeIn=function(n){
return Math.pow(n,3);
};
dojo.lfx.easeOut=function(n){
return (1-Math.pow(1-n,3));
};
dojo.lfx.easeInOut=function(n){
return ((3*Math.pow(n,2))-(2*Math.pow(n,3)));
};
dojo.lfx.IAnimation=function(){
};
dojo.lang.extend(dojo.lfx.IAnimation,{curve:null,duration:1000,easing:null,repeatCount:0,rate:10,handler:null,beforeBegin:null,onBegin:null,onAnimate:null,onEnd:null,onPlay:null,onPause:null,onStop:null,play:null,pause:null,stop:null,connect:function(evt,_60a,_60b){
if(!_60b){
_60b=_60a;
_60a=this;
}
_60b=dojo.lang.hitch(_60a,_60b);
var _60c=this[evt]||function(){
};
this[evt]=function(){
var ret=_60c.apply(this,arguments);
_60b.apply(this,arguments);
return ret;
};
return this;
},fire:function(evt,args){
if(this[evt]){
this[evt].apply(this,(args||[]));
}
return this;
},repeat:function(_610){
this.repeatCount=_610;
return this;
},_active:false,_paused:false});
dojo.lfx.Animation=function(_611,_612,_613,_614,_615,rate){
dojo.lfx.IAnimation.call(this);
if(dojo.lang.isNumber(_611)||(!_611&&_612.getValue)){
rate=_615;
_615=_614;
_614=_613;
_613=_612;
_612=_611;
_611=null;
}else{
if(_611.getValue||dojo.lang.isArray(_611)){
rate=_614;
_615=_613;
_614=_612;
_613=_611;
_612=null;
_611=null;
}
}
if(dojo.lang.isArray(_613)){
this.curve=new dojo.lfx.Line(_613[0],_613[1]);
}else{
this.curve=_613;
}
if(_612!=null&&_612>0){
this.duration=_612;
}
if(_615){
this.repeatCount=_615;
}
if(rate){
this.rate=rate;
}
if(_611){
dojo.lang.forEach(["handler","beforeBegin","onBegin","onEnd","onPlay","onStop","onAnimate"],function(item){
if(_611[item]){
this.connect(item,_611[item]);
}
},this);
}
if(_614&&dojo.lang.isFunction(_614)){
this.easing=_614;
}
};
dojo.inherits(dojo.lfx.Animation,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Animation,{_startTime:null,_endTime:null,_timer:null,_percent:0,_startRepeatCount:0,play:function(_618,_619){
if(_619){
clearTimeout(this._timer);
this._active=false;
this._paused=false;
this._percent=0;
}else{
if(this._active&&!this._paused){
return this;
}
}
this.fire("handler",["beforeBegin"]);
this.fire("beforeBegin");
if(_618>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_619);
}),_618);
return this;
}
this._startTime=new Date().valueOf();
if(this._paused){
this._startTime-=(this.duration*this._percent/100);
}
this._endTime=this._startTime+this.duration;
this._active=true;
this._paused=false;
var step=this._percent/100;
var _61b=this.curve.getValue(step);
if(this._percent==0){
if(!this._startRepeatCount){
this._startRepeatCount=this.repeatCount;
}
this.fire("handler",["begin",_61b]);
this.fire("onBegin",[_61b]);
}
this.fire("handler",["play",_61b]);
this.fire("onPlay",[_61b]);
this._cycle();
return this;
},pause:function(){
clearTimeout(this._timer);
if(!this._active){
return this;
}
this._paused=true;
var _61c=this.curve.getValue(this._percent/100);
this.fire("handler",["pause",_61c]);
this.fire("onPause",[_61c]);
return this;
},gotoPercent:function(pct,_61e){
clearTimeout(this._timer);
this._active=true;
this._paused=true;
this._percent=pct;
if(_61e){
this.play();
}
return this;
},stop:function(_61f){
clearTimeout(this._timer);
var step=this._percent/100;
if(_61f){
step=1;
}
var _621=this.curve.getValue(step);
this.fire("handler",["stop",_621]);
this.fire("onStop",[_621]);
this._active=false;
this._paused=false;
return this;
},status:function(){
if(this._active){
return this._paused?"paused":"playing";
}else{
return "stopped";
}
return this;
},_cycle:function(){
clearTimeout(this._timer);
if(this._active){
var curr=new Date().valueOf();
var step=(curr-this._startTime)/(this._endTime-this._startTime);
if(step>=1){
step=1;
this._percent=100;
}else{
this._percent=step*100;
}
if((this.easing)&&(dojo.lang.isFunction(this.easing))){
step=this.easing(step);
}
var _624=this.curve.getValue(step);
this.fire("handler",["animate",_624]);
this.fire("onAnimate",[_624]);
if(step<1){
this._timer=setTimeout(dojo.lang.hitch(this,"_cycle"),this.rate);
}else{
this._active=false;
this.fire("handler",["end"]);
this.fire("onEnd");
if(this.repeatCount>0){
this.repeatCount--;
this.play(null,true);
}else{
if(this.repeatCount==-1){
this.play(null,true);
}else{
if(this._startRepeatCount){
this.repeatCount=this._startRepeatCount;
this._startRepeatCount=0;
}
}
}
}
}
return this;
}});
dojo.lfx.Combine=function(_625){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._animsEnded=0;
var _626=arguments;
if(_626.length==1&&(dojo.lang.isArray(_626[0])||dojo.lang.isArrayLike(_626[0]))){
_626=_626[0];
}
dojo.lang.forEach(_626,function(anim){
this._anims.push(anim);
anim.connect("onEnd",dojo.lang.hitch(this,"_onAnimsEnded"));
},this);
};
dojo.inherits(dojo.lfx.Combine,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Combine,{_animsEnded:0,play:function(_628,_629){
if(!this._anims.length){
return this;
}
this.fire("beforeBegin");
if(_628>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_629);
}),_628);
return this;
}
if(_629||this._anims[0].percent==0){
this.fire("onBegin");
}
this.fire("onPlay");
this._animsCall("play",null,_629);
return this;
},pause:function(){
this.fire("onPause");
this._animsCall("pause");
return this;
},stop:function(_62a){
this.fire("onStop");
this._animsCall("stop",_62a);
return this;
},_onAnimsEnded:function(){
this._animsEnded++;
if(this._animsEnded>=this._anims.length){
this.fire("onEnd");
}
return this;
},_animsCall:function(_62b){
var args=[];
if(arguments.length>1){
for(var i=1;i<arguments.length;i++){
args.push(arguments[i]);
}
}
var _62e=this;
dojo.lang.forEach(this._anims,function(anim){
anim[_62b](args);
},_62e);
return this;
}});
dojo.lfx.Chain=function(_630){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._currAnim=-1;
var _631=arguments;
if(_631.length==1&&(dojo.lang.isArray(_631[0])||dojo.lang.isArrayLike(_631[0]))){
_631=_631[0];
}
var _632=this;
dojo.lang.forEach(_631,function(anim,i,_635){
this._anims.push(anim);
if(i<_635.length-1){
anim.connect("onEnd",dojo.lang.hitch(this,"_playNext"));
}else{
anim.connect("onEnd",dojo.lang.hitch(this,function(){
this.fire("onEnd");
}));
}
},this);
};
dojo.inherits(dojo.lfx.Chain,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Chain,{_currAnim:-1,play:function(_636,_637){
if(!this._anims.length){
return this;
}
if(_637||!this._anims[this._currAnim]){
this._currAnim=0;
}
var _638=this._anims[this._currAnim];
this.fire("beforeBegin");
if(_636>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_637);
}),_636);
return this;
}
if(_638){
if(this._currAnim==0){
this.fire("handler",["begin",this._currAnim]);
this.fire("onBegin",[this._currAnim]);
}
this.fire("onPlay",[this._currAnim]);
_638.play(null,_637);
}
return this;
},pause:function(){
if(this._anims[this._currAnim]){
this._anims[this._currAnim].pause();
this.fire("onPause",[this._currAnim]);
}
return this;
},playPause:function(){
if(this._anims.length==0){
return this;
}
if(this._currAnim==-1){
this._currAnim=0;
}
var _639=this._anims[this._currAnim];
if(_639){
if(!_639._active||_639._paused){
this.play();
}else{
this.pause();
}
}
return this;
},stop:function(){
var _63a=this._anims[this._currAnim];
if(_63a){
_63a.stop();
this.fire("onStop",[this._currAnim]);
}
return _63a;
},_playNext:function(){
if(this._currAnim==-1||this._anims.length==0){
return this;
}
this._currAnim++;
if(this._anims[this._currAnim]){
this._anims[this._currAnim].play(null,true);
}
return this;
}});
dojo.lfx.combine=function(_63b){
var _63c=arguments;
if(dojo.lang.isArray(arguments[0])){
_63c=arguments[0];
}
if(_63c.length==1){
return _63c[0];
}
return new dojo.lfx.Combine(_63c);
};
dojo.lfx.chain=function(_63d){
var _63e=arguments;
if(dojo.lang.isArray(arguments[0])){
_63e=arguments[0];
}
if(_63e.length==1){
return _63e[0];
}
return new dojo.lfx.Chain(_63e);
};
dojo.provide("dojo.html.color");
dojo.html.getBackgroundColor=function(node){
node=dojo.byId(node);
var _640;
do{
_640=dojo.html.getStyle(node,"background-color");
if(_640.toLowerCase()=="rgba(0, 0, 0, 0)"){
_640="transparent";
}
if(node==document.getElementsByTagName("body")[0]){
node=null;
break;
}
node=node.parentNode;
}while(node&&dojo.lang.inArray(["transparent",""],_640));
if(_640=="transparent"){
_640=[255,255,255,0];
}else{
_640=dojo.gfx.color.extractRGB(_640);
}
return _640;
};
dojo.provide("dojo.lfx.html");
dojo.lfx.html._byId=function(_641){
if(!_641){
return [];
}
if(dojo.lang.isArrayLike(_641)){
if(!_641.alreadyChecked){
var n=[];
dojo.lang.forEach(_641,function(node){
n.push(dojo.byId(node));
});
n.alreadyChecked=true;
return n;
}else{
return _641;
}
}else{
var n=[];
n.push(dojo.byId(_641));
n.alreadyChecked=true;
return n;
}
};
dojo.lfx.html.propertyAnimation=function(_644,_645,_646,_647,_648){
_644=dojo.lfx.html._byId(_644);
var _649={"propertyMap":_645,"nodes":_644,"duration":_646,"easing":_647||dojo.lfx.easeDefault};
var _64a=function(args){
if(args.nodes.length==1){
var pm=args.propertyMap;
if(!dojo.lang.isArray(args.propertyMap)){
var parr=[];
for(var _64e in pm){
pm[_64e].property=_64e;
parr.push(pm[_64e]);
}
pm=args.propertyMap=parr;
}
dojo.lang.forEach(pm,function(prop){
if(dj_undef("start",prop)){
if(prop.property!="opacity"){
prop.start=parseInt(dojo.html.getComputedStyle(args.nodes[0],prop.property));
}else{
prop.start=dojo.html.getOpacity(args.nodes[0]);
}
}
});
}
};
var _650=function(_651){
var _652=[];
dojo.lang.forEach(_651,function(c){
_652.push(Math.round(c));
});
return _652;
};
var _654=function(n,_656){
n=dojo.byId(n);
if(!n||!n.style){
return;
}
for(var s in _656){
try{
if(s=="opacity"){
dojo.html.setOpacity(n,_656[s]);
}else{
n.style[s]=_656[s];
}
}
catch(e){
dojo.debug(e);
}
}
};
var _658=function(_659){
this._properties=_659;
this.diffs=new Array(_659.length);
dojo.lang.forEach(_659,function(prop,i){
if(dojo.lang.isFunction(prop.start)){
prop.start=prop.start(prop,i);
}
if(dojo.lang.isFunction(prop.end)){
prop.end=prop.end(prop,i);
}
if(dojo.lang.isArray(prop.start)){
this.diffs[i]=null;
}else{
if(prop.start instanceof dojo.gfx.color.Color){
prop.startRgb=prop.start.toRgb();
prop.endRgb=prop.end.toRgb();
}else{
this.diffs[i]=prop.end-prop.start;
}
}
},this);
this.getValue=function(n){
var ret={};
dojo.lang.forEach(this._properties,function(prop,i){
var _660=null;
if(dojo.lang.isArray(prop.start)){
}else{
if(prop.start instanceof dojo.gfx.color.Color){
_660=(prop.units||"rgb")+"(";
for(var j=0;j<prop.startRgb.length;j++){
_660+=Math.round(((prop.endRgb[j]-prop.startRgb[j])*n)+prop.startRgb[j])+(j<prop.startRgb.length-1?",":"");
}
_660+=")";
}else{
_660=((this.diffs[i])*n)+prop.start+(prop.property!="opacity"?prop.units||"px":"");
}
}
ret[dojo.html.toCamelCase(prop.property)]=_660;
},this);
return ret;
};
};
var anim=new dojo.lfx.Animation({beforeBegin:function(){
_64a(_649);
anim.curve=new _658(_649.propertyMap);
},onAnimate:function(_663){
dojo.lang.forEach(_649.nodes,function(node){
_654(node,_663);
});
}},_649.duration,null,_649.easing);
if(_648){
for(var x in _648){
if(dojo.lang.isFunction(_648[x])){
anim.connect(x,anim,_648[x]);
}
}
}
return anim;
};
dojo.lfx.html._makeFadeable=function(_666){
var _667=function(node){
if(dojo.render.html.ie){
if((node.style.zoom.length==0)&&(dojo.html.getStyle(node,"zoom")=="normal")){
node.style.zoom="1";
}
if((node.style.width.length==0)&&(dojo.html.getStyle(node,"width")=="auto")){
node.style.width="auto";
}
}
};
if(dojo.lang.isArrayLike(_666)){
dojo.lang.forEach(_666,_667);
}else{
_667(_666);
}
};
dojo.lfx.html.fade=function(_669,_66a,_66b,_66c,_66d){
_669=dojo.lfx.html._byId(_669);
var _66e={property:"opacity"};
if(!dj_undef("start",_66a)){
_66e.start=_66a.start;
}else{
_66e.start=function(){
return dojo.html.getOpacity(_669[0]);
};
}
if(!dj_undef("end",_66a)){
_66e.end=_66a.end;
}else{
dojo.raise("dojo.lfx.html.fade needs an end value");
}
var anim=dojo.lfx.propertyAnimation(_669,[_66e],_66b,_66c);
anim.connect("beforeBegin",function(){
dojo.lfx.html._makeFadeable(_669);
});
if(_66d){
anim.connect("onEnd",function(){
_66d(_669,anim);
});
}
return anim;
};
dojo.lfx.html.fadeIn=function(_670,_671,_672,_673){
return dojo.lfx.html.fade(_670,{end:1},_671,_672,_673);
};
dojo.lfx.html.fadeOut=function(_674,_675,_676,_677){
return dojo.lfx.html.fade(_674,{end:0},_675,_676,_677);
};
dojo.lfx.html.fadeShow=function(_678,_679,_67a,_67b){
_678=dojo.lfx.html._byId(_678);
dojo.lang.forEach(_678,function(node){
dojo.html.setOpacity(node,0);
});
var anim=dojo.lfx.html.fadeIn(_678,_679,_67a,_67b);
anim.connect("beforeBegin",function(){
if(dojo.lang.isArrayLike(_678)){
dojo.lang.forEach(_678,dojo.html.show);
}else{
dojo.html.show(_678);
}
});
return anim;
};
dojo.lfx.html.fadeHide=function(_67e,_67f,_680,_681){
var anim=dojo.lfx.html.fadeOut(_67e,_67f,_680,function(){
if(dojo.lang.isArrayLike(_67e)){
dojo.lang.forEach(_67e,dojo.html.hide);
}else{
dojo.html.hide(_67e);
}
if(_681){
_681(_67e,anim);
}
});
return anim;
};
dojo.lfx.html.wipeIn=function(_683,_684,_685,_686){
_683=dojo.lfx.html._byId(_683);
var _687=[];
dojo.lang.forEach(_683,function(node){
var _689={};
var _68a,_68b,_68c;
with(node.style){
_68a=top;
_68b=left;
_68c=position;
top="-9999px";
left="-9999px";
position="absolute";
display="";
}
var _68d=dojo.html.getBorderBox(node).height;
with(node.style){
top=_68a;
left=_68b;
position=_68c;
display="none";
}
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:1,end:function(){
return _68d;
}}},_684,_685);
anim.connect("beforeBegin",function(){
_689.overflow=node.style.overflow;
_689.height=node.style.height;
with(node.style){
overflow="hidden";
height="1px";
}
dojo.html.show(node);
});
anim.connect("onEnd",function(){
with(node.style){
overflow=_689.overflow;
height=_689.height;
}
if(_686){
_686(node,anim);
}
});
_687.push(anim);
});
return dojo.lfx.combine(_687);
};
dojo.lfx.html.wipeOut=function(_68f,_690,_691,_692){
_68f=dojo.lfx.html._byId(_68f);
var _693=[];
dojo.lang.forEach(_68f,function(node){
var _695={};
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:function(){
return dojo.html.getContentBox(node).height;
},end:1}},_690,_691,{"beforeBegin":function(){
_695.overflow=node.style.overflow;
_695.height=node.style.height;
with(node.style){
overflow="hidden";
}
dojo.html.show(node);
},"onEnd":function(){
dojo.html.hide(node);
with(node.style){
overflow=_695.overflow;
height=_695.height;
}
if(_692){
_692(node,anim);
}
}});
_693.push(anim);
});
return dojo.lfx.combine(_693);
};
dojo.lfx.html.slideTo=function(_697,_698,_699,_69a,_69b){
_697=dojo.lfx.html._byId(_697);
var _69c=[];
var _69d=dojo.html.getComputedStyle;
if(dojo.lang.isArray(_698)){
dojo.deprecated("dojo.lfx.html.slideTo(node, array)","use dojo.lfx.html.slideTo(node, {top: value, left: value});","0.5");
_698={top:_698[0],left:_698[1]};
}
dojo.lang.forEach(_697,function(node){
var top=null;
var left=null;
var init=(function(){
var _6a2=node;
return function(){
var pos=_69d(_6a2,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_69d(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_69d(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_6a2,true);
dojo.html.setStyleAttributes(_6a2,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:(_698.top||0)},"left":{start:left,end:(_698.left||0)}},_699,_69a,{"beforeBegin":init});
if(_69b){
anim.connect("onEnd",function(){
_69b(_697,anim);
});
}
_69c.push(anim);
});
return dojo.lfx.combine(_69c);
};
dojo.lfx.html.slideBy=function(_6a6,_6a7,_6a8,_6a9,_6aa){
_6a6=dojo.lfx.html._byId(_6a6);
var _6ab=[];
var _6ac=dojo.html.getComputedStyle;
if(dojo.lang.isArray(_6a7)){
dojo.deprecated("dojo.lfx.html.slideBy(node, array)","use dojo.lfx.html.slideBy(node, {top: value, left: value});","0.5");
_6a7={top:_6a7[0],left:_6a7[1]};
}
dojo.lang.forEach(_6a6,function(node){
var top=null;
var left=null;
var init=(function(){
var _6b1=node;
return function(){
var pos=_6ac(_6b1,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_6ac(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_6ac(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_6b1,true);
dojo.html.setStyleAttributes(_6b1,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:top+(_6a7.top||0)},"left":{start:left,end:left+(_6a7.left||0)}},_6a8,_6a9).connect("beforeBegin",init);
if(_6aa){
anim.connect("onEnd",function(){
_6aa(_6a6,anim);
});
}
_6ab.push(anim);
});
return dojo.lfx.combine(_6ab);
};
dojo.lfx.html.explode=function(_6b5,_6b6,_6b7,_6b8,_6b9){
var h=dojo.html;
_6b5=dojo.byId(_6b5);
_6b6=dojo.byId(_6b6);
var _6bb=h.toCoordinateObject(_6b5,true);
var _6bc=document.createElement("div");
h.copyStyle(_6bc,_6b6);
if(_6b6.explodeClassName){
_6bc.className=_6b6.explodeClassName;
}
with(_6bc.style){
position="absolute";
display="none";
var _6bd=h.getStyle(_6b5,"background-color");
backgroundColor=_6bd?_6bd.toLowerCase():"transparent";
backgroundColor=(backgroundColor=="transparent")?"rgb(221, 221, 221)":backgroundColor;
}
dojo.body().appendChild(_6bc);
with(_6b6.style){
visibility="hidden";
display="block";
}
var _6be=h.toCoordinateObject(_6b6,true);
with(_6b6.style){
display="none";
visibility="visible";
}
var _6bf={opacity:{start:0.5,end:1}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_6bf[type]={start:_6bb[type],end:_6be[type]};
});
var anim=new dojo.lfx.propertyAnimation(_6bc,_6bf,_6b7,_6b8,{"beforeBegin":function(){
h.setDisplay(_6bc,"block");
},"onEnd":function(){
h.setDisplay(_6b6,"block");
_6bc.parentNode.removeChild(_6bc);
}});
if(_6b9){
anim.connect("onEnd",function(){
_6b9(_6b6,anim);
});
}
return anim;
};
dojo.lfx.html.implode=function(_6c2,end,_6c4,_6c5,_6c6){
var h=dojo.html;
_6c2=dojo.byId(_6c2);
end=dojo.byId(end);
var _6c8=dojo.html.toCoordinateObject(_6c2,true);
var _6c9=dojo.html.toCoordinateObject(end,true);
var _6ca=document.createElement("div");
dojo.html.copyStyle(_6ca,_6c2);
if(_6c2.explodeClassName){
_6ca.className=_6c2.explodeClassName;
}
dojo.html.setOpacity(_6ca,0.3);
with(_6ca.style){
position="absolute";
display="none";
backgroundColor=h.getStyle(_6c2,"background-color").toLowerCase();
}
dojo.body().appendChild(_6ca);
var _6cb={opacity:{start:1,end:0.5}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_6cb[type]={start:_6c8[type],end:_6c9[type]};
});
var anim=new dojo.lfx.propertyAnimation(_6ca,_6cb,_6c4,_6c5,{"beforeBegin":function(){
dojo.html.hide(_6c2);
dojo.html.show(_6ca);
},"onEnd":function(){
_6ca.parentNode.removeChild(_6ca);
}});
if(_6c6){
anim.connect("onEnd",function(){
_6c6(_6c2,anim);
});
}
return anim;
};
dojo.lfx.html.highlight=function(_6ce,_6cf,_6d0,_6d1,_6d2){
_6ce=dojo.lfx.html._byId(_6ce);
var _6d3=[];
dojo.lang.forEach(_6ce,function(node){
var _6d5=dojo.html.getBackgroundColor(node);
var bg=dojo.html.getStyle(node,"background-color").toLowerCase();
var _6d7=dojo.html.getStyle(node,"background-image");
var _6d8=(bg=="transparent"||bg=="rgba(0, 0, 0, 0)");
while(_6d5.length>3){
_6d5.pop();
}
var rgb=new dojo.gfx.color.Color(_6cf);
var _6da=new dojo.gfx.color.Color(_6d5);
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:rgb,end:_6da}},_6d0,_6d1,{"beforeBegin":function(){
if(_6d7){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+rgb.toRgb().join(",")+")";
},"onEnd":function(){
if(_6d7){
node.style.backgroundImage=_6d7;
}
if(_6d8){
node.style.backgroundColor="transparent";
}
if(_6d2){
_6d2(node,anim);
}
}});
_6d3.push(anim);
});
return dojo.lfx.combine(_6d3);
};
dojo.lfx.html.unhighlight=function(_6dc,_6dd,_6de,_6df,_6e0){
_6dc=dojo.lfx.html._byId(_6dc);
var _6e1=[];
dojo.lang.forEach(_6dc,function(node){
var _6e3=new dojo.gfx.color.Color(dojo.html.getBackgroundColor(node));
var rgb=new dojo.gfx.color.Color(_6dd);
var _6e5=dojo.html.getStyle(node,"background-image");
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:_6e3,end:rgb}},_6de,_6df,{"beforeBegin":function(){
if(_6e5){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+_6e3.toRgb().join(",")+")";
},"onEnd":function(){
if(_6e0){
_6e0(node,anim);
}
}});
_6e1.push(anim);
});
return dojo.lfx.combine(_6e1);
};
dojo.lang.mixin(dojo.lfx,dojo.lfx.html);
dojo.kwCompoundRequire({browser:["dojo.lfx.html"],dashboard:["dojo.lfx.html"]});
dojo.provide("dojo.lfx.*");
dojo.provide("dojo.lfx.toggle");
dojo.lfx.toggle.plain={show:function(node,_6e8,_6e9,_6ea){
dojo.html.show(node);
if(dojo.lang.isFunction(_6ea)){
_6ea();
}
},hide:function(node,_6ec,_6ed,_6ee){
dojo.html.hide(node);
if(dojo.lang.isFunction(_6ee)){
_6ee();
}
}};
dojo.lfx.toggle.fade={show:function(node,_6f0,_6f1,_6f2){
dojo.lfx.fadeShow(node,_6f0,_6f1,_6f2).play();
},hide:function(node,_6f4,_6f5,_6f6){
dojo.lfx.fadeHide(node,_6f4,_6f5,_6f6).play();
}};
dojo.lfx.toggle.wipe={show:function(node,_6f8,_6f9,_6fa){
dojo.lfx.wipeIn(node,_6f8,_6f9,_6fa).play();
},hide:function(node,_6fc,_6fd,_6fe){
dojo.lfx.wipeOut(node,_6fc,_6fd,_6fe).play();
}};
dojo.lfx.toggle.explode={show:function(node,_700,_701,_702,_703){
dojo.lfx.explode(_703||{x:0,y:0,width:0,height:0},node,_700,_701,_702).play();
},hide:function(node,_705,_706,_707,_708){
dojo.lfx.implode(node,_708||{x:0,y:0,width:0,height:0},_705,_706,_707).play();
}};
dojo.provide("dojo.widget.HtmlWidget");
dojo.declare("dojo.widget.HtmlWidget",dojo.widget.DomWidget,{templateCssPath:null,templatePath:null,lang:"",toggle:"plain",toggleDuration:150,initialize:function(args,frag){
},postMixInProperties:function(args,frag){
if(this.lang===""){
this.lang=null;
}
this.toggleObj=dojo.lfx.toggle[this.toggle.toLowerCase()]||dojo.lfx.toggle.plain;
},createNodesFromText:function(txt,wrap){
return dojo.html.createNodesFromText(txt,wrap);
},destroyRendering:function(_70f){
try{
if(this.bgIframe){
this.bgIframe.remove();
delete this.bgIframe;
}
if(!_70f&&this.domNode){
dojo.event.browser.clean(this.domNode);
}
dojo.widget.HtmlWidget.superclass.destroyRendering.call(this);
}
catch(e){
}
},isShowing:function(){
return dojo.html.isShowing(this.domNode);
},toggleShowing:function(){
if(this.isShowing()){
this.hide();
}else{
this.show();
}
},show:function(){
if(this.isShowing()){
return;
}
this.animationInProgress=true;
this.toggleObj.show(this.domNode,this.toggleDuration,null,dojo.lang.hitch(this,this.onShow),this.explodeSrc);
},onShow:function(){
this.animationInProgress=false;
this.checkSize();
},hide:function(){
if(!this.isShowing()){
return;
}
this.animationInProgress=true;
this.toggleObj.hide(this.domNode,this.toggleDuration,null,dojo.lang.hitch(this,this.onHide),this.explodeSrc);
},onHide:function(){
this.animationInProgress=false;
},_isResized:function(w,h){
if(!this.isShowing()){
return false;
}
var wh=dojo.html.getMarginBox(this.domNode);
var _713=w||wh.width;
var _714=h||wh.height;
if(this.width==_713&&this.height==_714){
return false;
}
this.width=_713;
this.height=_714;
return true;
},checkSize:function(){
if(!this._isResized()){
return;
}
this.onResized();
},resizeTo:function(w,h){
dojo.html.setMarginBox(this.domNode,{width:w,height:h});
if(this.isShowing()){
this.onResized();
}
},resizeSoon:function(){
if(this.isShowing()){
dojo.lang.setTimeout(this,this.onResized,0);
}
},onResized:function(){
dojo.lang.forEach(this.children,function(_717){
if(_717.checkSize){
_717.checkSize();
}
});
}});
dojo.kwCompoundRequire({common:["dojo.xml.Parse","dojo.widget.Widget","dojo.widget.Parse","dojo.widget.Manager"],browser:["dojo.widget.DomWidget","dojo.widget.HtmlWidget"],dashboard:["dojo.widget.DomWidget","dojo.widget.HtmlWidget"],svg:["dojo.widget.SvgWidget"],rhino:["dojo.widget.SwtWidget"]});
dojo.provide("dojo.widget.*");
dojo.provide("dojo.string.common");
dojo.string.trim=function(str,wh){
if(!str.replace){
return str;
}
if(!str.length){
return str;
}
var re=(wh>0)?(/^\s+/):(wh<0)?(/\s+$/):(/^\s+|\s+$/g);
return str.replace(re,"");
};
dojo.string.trimStart=function(str){
return dojo.string.trim(str,1);
};
dojo.string.trimEnd=function(str){
return dojo.string.trim(str,-1);
};
dojo.string.repeat=function(str,_71e,_71f){
var out="";
for(var i=0;i<_71e;i++){
out+=str;
if(_71f&&i<_71e-1){
out+=_71f;
}
}
return out;
};
dojo.string.pad=function(str,len,c,dir){
var out=String(str);
if(!c){
c="0";
}
if(!dir){
dir=1;
}
while(out.length<len){
if(dir>0){
out=c+out;
}else{
out+=c;
}
}
return out;
};
dojo.string.padLeft=function(str,len,c){
return dojo.string.pad(str,len,c,1);
};
dojo.string.padRight=function(str,len,c){
return dojo.string.pad(str,len,c,-1);
};
dojo.provide("dojo.string");
dojo.provide("dojo.io.common");
dojo.io.transports=[];
dojo.io.hdlrFuncNames=["load","error","timeout"];
dojo.io.Request=function(url,_72e,_72f,_730){
if((arguments.length==1)&&(arguments[0].constructor==Object)){
this.fromKwArgs(arguments[0]);
}else{
this.url=url;
if(_72e){
this.mimetype=_72e;
}
if(_72f){
this.transport=_72f;
}
if(arguments.length>=4){
this.changeUrl=_730;
}
}
};
dojo.lang.extend(dojo.io.Request,{url:"",mimetype:"text/plain",method:"GET",content:undefined,transport:undefined,changeUrl:undefined,formNode:undefined,sync:false,bindSuccess:false,useCache:false,preventCache:false,load:function(type,data,_733,_734){
},error:function(type,_736,_737,_738){
},timeout:function(type,_73a,_73b,_73c){
},handle:function(type,data,_73f,_740){
},timeoutSeconds:0,abort:function(){
},fromKwArgs:function(_741){
if(_741["url"]){
_741.url=_741.url.toString();
}
if(_741["formNode"]){
_741.formNode=dojo.byId(_741.formNode);
}
if(!_741["method"]&&_741["formNode"]&&_741["formNode"].method){
_741.method=_741["formNode"].method;
}
if(!_741["handle"]&&_741["handler"]){
_741.handle=_741.handler;
}
if(!_741["load"]&&_741["loaded"]){
_741.load=_741.loaded;
}
if(!_741["changeUrl"]&&_741["changeURL"]){
_741.changeUrl=_741.changeURL;
}
_741.encoding=dojo.lang.firstValued(_741["encoding"],djConfig["bindEncoding"],"");
_741.sendTransport=dojo.lang.firstValued(_741["sendTransport"],djConfig["ioSendTransport"],false);
var _742=dojo.lang.isFunction;
for(var x=0;x<dojo.io.hdlrFuncNames.length;x++){
var fn=dojo.io.hdlrFuncNames[x];
if(_741[fn]&&_742(_741[fn])){
continue;
}
if(_741["handle"]&&_742(_741["handle"])){
_741[fn]=_741.handle;
}
}
dojo.lang.mixin(this,_741);
}});
dojo.io.Error=function(msg,type,num){
this.message=msg;
this.type=type||"unknown";
this.number=num||0;
};
dojo.io.transports.addTransport=function(name){
this.push(name);
this[name]=dojo.io[name];
};
dojo.io.bind=function(_749){
if(!(_749 instanceof dojo.io.Request)){
try{
_749=new dojo.io.Request(_749);
}
catch(e){
dojo.debug(e);
}
}
var _74a="";
if(_749["transport"]){
_74a=_749["transport"];
if(!this[_74a]){
dojo.io.sendBindError(_749,"No dojo.io.bind() transport with name '"+_749["transport"]+"'.");
return _749;
}
if(!this[_74a].canHandle(_749)){
dojo.io.sendBindError(_749,"dojo.io.bind() transport with name '"+_749["transport"]+"' cannot handle this type of request.");
return _749;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_749))){
_74a=tmp;
break;
}
}
if(_74a==""){
dojo.io.sendBindError(_749,"None of the loaded transports for dojo.io.bind()"+" can handle the request.");
return _749;
}
}
this[_74a].bind(_749);
_749.bindSuccess=true;
return _749;
};
dojo.io.sendBindError=function(_74d,_74e){
if((typeof _74d.error=="function"||typeof _74d.handle=="function")&&(typeof setTimeout=="function"||typeof setTimeout=="object")){
var _74f=new dojo.io.Error(_74e);
setTimeout(function(){
_74d[(typeof _74d.error=="function")?"error":"handle"]("error",_74f,null,_74d);
},50);
}else{
dojo.raise(_74e);
}
};
dojo.io.queueBind=function(_750){
if(!(_750 instanceof dojo.io.Request)){
try{
_750=new dojo.io.Request(_750);
}
catch(e){
dojo.debug(e);
}
}
var _751=_750.load;
_750.load=function(){
dojo.io._queueBindInFlight=false;
var ret=_751.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
var _753=_750.error;
_750.error=function(){
dojo.io._queueBindInFlight=false;
var ret=_753.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
dojo.io._bindQueue.push(_750);
dojo.io._dispatchNextQueueBind();
return _750;
};
dojo.io._dispatchNextQueueBind=function(){
if(!dojo.io._queueBindInFlight){
dojo.io._queueBindInFlight=true;
if(dojo.io._bindQueue.length>0){
dojo.io.bind(dojo.io._bindQueue.shift());
}else{
dojo.io._queueBindInFlight=false;
}
}
};
dojo.io._bindQueue=[];
dojo.io._queueBindInFlight=false;
dojo.io.argsFromMap=function(map,_756,last){
var enc=/utf/i.test(_756||"")?encodeURIComponent:dojo.string.encodeAscii;
var _759=[];
var _75a=new Object();
for(var name in map){
var _75c=function(elt){
var val=enc(name)+"="+enc(elt);
_759[(last==name)?"push":"unshift"](val);
};
if(!_75a[name]){
var _75f=map[name];
if(dojo.lang.isArray(_75f)){
dojo.lang.forEach(_75f,_75c);
}else{
_75c(_75f);
}
}
}
return _759.join("&");
};
dojo.io.setIFrameSrc=function(_760,src,_762){
try{
var r=dojo.render.html;
if(!_762){
if(r.safari){
_760.location=src;
}else{
frames[_760.name].location=src;
}
}else{
var idoc;
if(r.ie){
idoc=_760.contentWindow.document;
}else{
if(r.safari){
idoc=_760.document;
}else{
idoc=_760.contentWindow;
}
}
if(!idoc){
_760.location=src;
return;
}else{
idoc.location.replace(src);
}
}
}
catch(e){
dojo.debug(e);
dojo.debug("setIFrameSrc: "+e);
}
};
dojo.provide("dojo.string.extras");
dojo.string.substituteParams=function(_765,hash){
var map=(typeof hash=="object")?hash:dojo.lang.toArray(arguments,1);
return _765.replace(/\%\{(\w+)\}/g,function(_768,key){
if(typeof (map[key])!="undefined"&&map[key]!=null){
return map[key];
}
dojo.raise("Substitution not found: "+key);
});
};
dojo.string.capitalize=function(str){
if(!dojo.lang.isString(str)){
return "";
}
if(arguments.length==0){
str=this;
}
var _76b=str.split(" ");
for(var i=0;i<_76b.length;i++){
_76b[i]=_76b[i].charAt(0).toUpperCase()+_76b[i].substring(1);
}
return _76b.join(" ");
};
dojo.string.isBlank=function(str){
if(!dojo.lang.isString(str)){
return true;
}
return (dojo.string.trim(str).length==0);
};
dojo.string.encodeAscii=function(str){
if(!dojo.lang.isString(str)){
return str;
}
var ret="";
var _770=escape(str);
var _771,re=/%u([0-9A-F]{4})/i;
while((_771=_770.match(re))){
var num=Number("0x"+_771[1]);
var _774=escape("&#"+num+";");
ret+=_770.substring(0,_771.index)+_774;
_770=_770.substring(_771.index+_771[0].length);
}
ret+=_770.replace(/\+/g,"%2B");
return ret;
};
dojo.string.escape=function(type,str){
var args=dojo.lang.toArray(arguments,1);
switch(type.toLowerCase()){
case "xml":
case "html":
case "xhtml":
return dojo.string.escapeXml.apply(this,args);
case "sql":
return dojo.string.escapeSql.apply(this,args);
case "regexp":
case "regex":
return dojo.string.escapeRegExp.apply(this,args);
case "javascript":
case "jscript":
case "js":
return dojo.string.escapeJavaScript.apply(this,args);
case "ascii":
return dojo.string.encodeAscii.apply(this,args);
default:
return str;
}
};
dojo.string.escapeXml=function(str,_779){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_779){
str=str.replace(/'/gm,"&#39;");
}
return str;
};
dojo.string.escapeSql=function(str){
return str.replace(/'/gm,"''");
};
dojo.string.escapeRegExp=function(str){
return str.replace(/\\/gm,"\\\\").replace(/([\f\b\n\t\r[\^$|?*+(){}])/gm,"\\$1");
};
dojo.string.escapeJavaScript=function(str){
return str.replace(/(["'\f\b\n\t\r])/gm,"\\$1");
};
dojo.string.escapeString=function(str){
return ("\""+str.replace(/(["\\])/g,"\\$1")+"\"").replace(/[\f]/g,"\\f").replace(/[\b]/g,"\\b").replace(/[\n]/g,"\\n").replace(/[\t]/g,"\\t").replace(/[\r]/g,"\\r");
};
dojo.string.summary=function(str,len){
if(!len||str.length<=len){
return str;
}
return str.substring(0,len).replace(/\.+$/,"")+"...";
};
dojo.string.endsWith=function(str,end,_782){
if(_782){
str=str.toLowerCase();
end=end.toLowerCase();
}
if((str.length-end.length)<0){
return false;
}
return str.lastIndexOf(end)==str.length-end.length;
};
dojo.string.endsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.endsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.startsWith=function(str,_786,_787){
if(_787){
str=str.toLowerCase();
_786=_786.toLowerCase();
}
return str.indexOf(_786)==0;
};
dojo.string.startsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.startsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.has=function(str){
for(var i=1;i<arguments.length;i++){
if(str.indexOf(arguments[i])>-1){
return true;
}
}
return false;
};
dojo.string.normalizeNewlines=function(text,_78d){
if(_78d=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_78d=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n").replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_78f){
var _790=[];
for(var i=0,_792=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_78f){
_790.push(str.substring(_792,i));
_792=i+1;
}
}
_790.push(str.substr(_792));
return _790;
};
dojo.provide("dojo.undo.browser");
try{
if((!djConfig["preventBackButtonFix"])&&(!dojo.hostenv.post_load_)){
document.write("<iframe style='border: 0px; width: 1px; height: 1px; position: absolute; bottom: 0px; right: 0px; visibility: visible;' name='djhistory' id='djhistory' src='"+(djConfig["dojoIframeHistoryUrl"]||dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"'></iframe>");
}
}
catch(e){
}
if(dojo.render.html.opera){
dojo.debug("Opera is not supported with dojo.undo.browser, so back/forward detection will not work.");
}
dojo.undo.browser={initialHref:(!dj_undef("window"))?window.location.href:"",initialHash:(!dj_undef("window"))?window.location.hash:"",moveForward:false,historyStack:[],forwardStack:[],historyIframe:null,bookmarkAnchor:null,locationTimer:null,setInitialState:function(args){
this.initialState=this._createState(this.initialHref,args,this.initialHash);
},addToHistory:function(args){
this.forwardStack=[];
var hash=null;
var url=null;
if(!this.historyIframe){
if(djConfig["useXDomain"]&&!djConfig["dojoIframeHistoryUrl"]){
dojo.debug("dojo.undo.browser: When using cross-domain Dojo builds,"+" please save iframe_history.html to your domain and set djConfig.dojoIframeHistoryUrl"+" to the path on your domain to iframe_history.html");
}
this.historyIframe=window.frames["djhistory"];
}
if(!this.bookmarkAnchor){
this.bookmarkAnchor=document.createElement("a");
dojo.body().appendChild(this.bookmarkAnchor);
this.bookmarkAnchor.style.display="none";
}
if(args["changeUrl"]){
hash="#"+((args["changeUrl"]!==true)?args["changeUrl"]:(new Date()).getTime());
if(this.historyStack.length==0&&this.initialState.urlHash==hash){
this.initialState=this._createState(url,args,hash);
return;
}else{
if(this.historyStack.length>0&&this.historyStack[this.historyStack.length-1].urlHash==hash){
this.historyStack[this.historyStack.length-1]=this._createState(url,args,hash);
return;
}
}
this.changingUrl=true;
setTimeout("window.location.href = '"+hash+"'; dojo.undo.browser.changingUrl = false;",1);
this.bookmarkAnchor.href=hash;
if(dojo.render.html.ie){
url=this._loadIframeHistory();
var _797=args["back"]||args["backButton"]||args["handle"];
var tcb=function(_799){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+hash+"';",1);
}
_797.apply(this,[_799]);
};
if(args["back"]){
args.back=tcb;
}else{
if(args["backButton"]){
args.backButton=tcb;
}else{
if(args["handle"]){
args.handle=tcb;
}
}
}
var _79a=args["forward"]||args["forwardButton"]||args["handle"];
var tfw=function(_79c){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_79a){
_79a.apply(this,[_79c]);
}
};
if(args["forward"]){
args.forward=tfw;
}else{
if(args["forwardButton"]){
args.forwardButton=tfw;
}else{
if(args["handle"]){
args.handle=tfw;
}
}
}
}else{
if(dojo.render.html.moz){
if(!this.locationTimer){
this.locationTimer=setInterval("dojo.undo.browser.checkLocation();",200);
}
}
}
}else{
url=this._loadIframeHistory();
}
this.historyStack.push(this._createState(url,args,hash));
},checkLocation:function(){
if(!this.changingUrl){
var hsl=this.historyStack.length;
if((window.location.hash==this.initialHash||window.location.href==this.initialHref)&&(hsl==1)){
this.handleBackButton();
return;
}
if(this.forwardStack.length>0){
if(this.forwardStack[this.forwardStack.length-1].urlHash==window.location.hash){
this.handleForwardButton();
return;
}
}
if((hsl>=2)&&(this.historyStack[hsl-2])){
if(this.historyStack[hsl-2].urlHash==window.location.hash){
this.handleBackButton();
return;
}
}
}
},iframeLoaded:function(evt,_79f){
if(!dojo.render.html.opera){
var _7a0=this._getUrlQuery(_79f.href);
if(_7a0==null){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
if(this.moveForward){
this.moveForward=false;
return;
}
if(this.historyStack.length>=2&&_7a0==this._getUrlQuery(this.historyStack[this.historyStack.length-2].url)){
this.handleBackButton();
}else{
if(this.forwardStack.length>0&&_7a0==this._getUrlQuery(this.forwardStack[this.forwardStack.length-1].url)){
this.handleForwardButton();
}
}
}
},handleBackButton:function(){
var _7a1=this.historyStack.pop();
if(!_7a1){
return;
}
var last=this.historyStack[this.historyStack.length-1];
if(!last&&this.historyStack.length==0){
last=this.initialState;
}
if(last){
if(last.kwArgs["back"]){
last.kwArgs["back"]();
}else{
if(last.kwArgs["backButton"]){
last.kwArgs["backButton"]();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("back");
}
}
}
}
this.forwardStack.push(_7a1);
},handleForwardButton:function(){
var last=this.forwardStack.pop();
if(!last){
return;
}
if(last.kwArgs["forward"]){
last.kwArgs.forward();
}else{
if(last.kwArgs["forwardButton"]){
last.kwArgs.forwardButton();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("forward");
}
}
}
this.historyStack.push(last);
},_createState:function(url,args,hash){
return {"url":url,"kwArgs":args,"urlHash":hash};
},_getUrlQuery:function(url){
var _7a8=url.split("?");
if(_7a8.length<2){
return null;
}else{
return _7a8[1];
}
},_loadIframeHistory:function(){
var url=(djConfig["dojoIframeHistoryUrl"]||dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
return url;
}};
dojo.provide("dojo.io.BrowserIO");
if(!dj_undef("window")){
dojo.io.checkChildrenForFile=function(node){
var _7ab=false;
var _7ac=node.getElementsByTagName("input");
dojo.lang.forEach(_7ac,function(_7ad){
if(_7ab){
return;
}
if(_7ad.getAttribute("type")=="file"){
_7ab=true;
}
});
return _7ab;
};
dojo.io.formHasFile=function(_7ae){
return dojo.io.checkChildrenForFile(_7ae);
};
dojo.io.updateNode=function(node,_7b0){
node=dojo.byId(node);
var args=_7b0;
if(dojo.lang.isString(_7b0)){
args={url:_7b0};
}
args.mimetype="text/html";
args.load=function(t,d,e){
while(node.firstChild){
dojo.dom.destroyNode(node.firstChild);
}
node.innerHTML=d;
};
dojo.io.bind(args);
};
dojo.io.formFilter=function(node){
var type=(node.type||"").toLowerCase();
return !node.disabled&&node.name&&!dojo.lang.inArray(["file","submit","image","reset","button"],type);
};
dojo.io.encodeForm=function(_7b7,_7b8,_7b9){
if((!_7b7)||(!_7b7.tagName)||(!_7b7.tagName.toLowerCase()=="form")){
dojo.raise("Attempted to encode a non-form element.");
}
if(!_7b9){
_7b9=dojo.io.formFilter;
}
var enc=/utf/i.test(_7b8||"")?encodeURIComponent:dojo.string.encodeAscii;
var _7bb=[];
for(var i=0;i<_7b7.elements.length;i++){
var elm=_7b7.elements[i];
if(!elm||elm.tagName.toLowerCase()=="fieldset"||!_7b9(elm)){
continue;
}
var name=enc(elm.name);
var type=elm.type.toLowerCase();
if(type=="select-multiple"){
for(var j=0;j<elm.options.length;j++){
if(elm.options[j].selected){
_7bb.push(name+"="+enc(elm.options[j].value));
}
}
}else{
if(dojo.lang.inArray(["radio","checkbox"],type)){
if(elm.checked){
_7bb.push(name+"="+enc(elm.value));
}
}else{
_7bb.push(name+"="+enc(elm.value));
}
}
}
var _7c1=_7b7.getElementsByTagName("input");
for(var i=0;i<_7c1.length;i++){
var _7c2=_7c1[i];
if(_7c2.type.toLowerCase()=="image"&&_7c2.form==_7b7&&_7b9(_7c2)){
var name=enc(_7c2.name);
_7bb.push(name+"="+enc(_7c2.value));
_7bb.push(name+".x=0");
_7bb.push(name+".y=0");
}
}
return _7bb.join("&")+"&";
};
dojo.io.FormBind=function(args){
this.bindArgs={};
if(args&&args.formNode){
this.init(args);
}else{
if(args){
this.init({formNode:args});
}
}
};
dojo.lang.extend(dojo.io.FormBind,{form:null,bindArgs:null,clickedButton:null,init:function(args){
var form=dojo.byId(args.formNode);
if(!form||!form.tagName||form.tagName.toLowerCase()!="form"){
throw new Error("FormBind: Couldn't apply, invalid form");
}else{
if(this.form==form){
return;
}else{
if(this.form){
throw new Error("FormBind: Already applied to a form");
}
}
}
dojo.lang.mixin(this.bindArgs,args);
this.form=form;
this.connect(form,"onsubmit","submit");
for(var i=0;i<form.elements.length;i++){
var node=form.elements[i];
if(node&&node.type&&dojo.lang.inArray(["submit","button"],node.type.toLowerCase())){
this.connect(node,"onclick","click");
}
}
var _7c8=form.getElementsByTagName("input");
for(var i=0;i<_7c8.length;i++){
var _7c9=_7c8[i];
if(_7c9.type.toLowerCase()=="image"&&_7c9.form==form){
this.connect(_7c9,"onclick","click");
}
}
},onSubmit:function(form){
return true;
},submit:function(e){
e.preventDefault();
if(this.onSubmit(this.form)){
dojo.io.bind(dojo.lang.mixin(this.bindArgs,{formFilter:dojo.lang.hitch(this,"formFilter")}));
}
},click:function(e){
var node=e.currentTarget;
if(node.disabled){
return;
}
this.clickedButton=node;
},formFilter:function(node){
var type=(node.type||"").toLowerCase();
var _7d0=false;
if(node.disabled||!node.name){
_7d0=false;
}else{
if(dojo.lang.inArray(["submit","button","image"],type)){
if(!this.clickedButton){
this.clickedButton=node;
}
_7d0=node==this.clickedButton;
}else{
_7d0=!dojo.lang.inArray(["file","submit","reset","button"],type);
}
}
return _7d0;
},connect:function(_7d1,_7d2,_7d3){
if(dojo.evalObjPath("dojo.event.connect")){
dojo.event.connect(_7d1,_7d2,this,_7d3);
}else{
var fcn=dojo.lang.hitch(this,_7d3);
_7d1[_7d2]=function(e){
if(!e){
e=window.event;
}
if(!e.currentTarget){
e.currentTarget=e.srcElement;
}
if(!e.preventDefault){
e.preventDefault=function(){
window.event.returnValue=false;
};
}
fcn(e);
};
}
}});
dojo.io.XMLHTTPTransport=new function(){
var _7d6=this;
var _7d7={};
this.useCache=false;
this.preventCache=false;
function getCacheKey(url,_7d9,_7da){
return url+"|"+_7d9+"|"+_7da.toLowerCase();
}
function addToCache(url,_7dc,_7dd,http){
_7d7[getCacheKey(url,_7dc,_7dd)]=http;
}
function getFromCache(url,_7e0,_7e1){
return _7d7[getCacheKey(url,_7e0,_7e1)];
}
this.clearCache=function(){
_7d7={};
};
function doLoad(_7e2,http,url,_7e5,_7e6){
if(((http.status>=200)&&(http.status<300))||(http.status==304)||(location.protocol=="file:"&&(http.status==0||http.status==undefined))||(location.protocol=="chrome:"&&(http.status==0||http.status==undefined))){
var ret;
if(_7e2.method.toLowerCase()=="head"){
var _7e8=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _7e8;
};
var _7e9=_7e8.split(/[\r\n]+/g);
for(var i=0;i<_7e9.length;i++){
var pair=_7e9[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_7e2.mimetype=="text/javascript"){
try{
ret=dj_eval(http.responseText);
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=null;
}
}else{
if(_7e2.mimetype=="text/json"||_7e2.mimetype=="application/json"){
try{
ret=dj_eval("("+http.responseText+")");
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=false;
}
}else{
if((_7e2.mimetype=="application/xml")||(_7e2.mimetype=="text/xml")){
ret=http.responseXML;
if(!ret||typeof ret=="string"||!http.getResponseHeader("Content-Type")){
ret=dojo.dom.createDocumentFromText(http.responseText);
}
}else{
ret=http.responseText;
}
}
}
}
if(_7e6){
addToCache(url,_7e5,_7e2.method,http);
}
_7e2[(typeof _7e2.load=="function")?"load":"handle"]("load",ret,http,_7e2);
}else{
var _7ec=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
_7e2[(typeof _7e2.error=="function")?"error":"handle"]("error",_7ec,http,_7e2);
}
}
function setHeaders(http,_7ee){
if(_7ee["headers"]){
for(var _7ef in _7ee["headers"]){
if(_7ef.toLowerCase()=="content-type"&&!_7ee["contentType"]){
_7ee["contentType"]=_7ee["headers"][_7ef];
}else{
http.setRequestHeader(_7ef,_7ee["headers"][_7ef]);
}
}
}
}
this.inFlight=[];
this.inFlightTimer=null;
this.startWatchingInFlight=function(){
if(!this.inFlightTimer){
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
}
};
this.watchInFlight=function(){
var now=null;
if(!dojo.hostenv._blockAsync&&!_7d6._blockAsync){
for(var x=this.inFlight.length-1;x>=0;x--){
try{
var tif=this.inFlight[x];
if(!tif||tif.http._aborted||!tif.http.readyState){
this.inFlight.splice(x,1);
continue;
}
if(4==tif.http.readyState){
this.inFlight.splice(x,1);
doLoad(tif.req,tif.http,tif.url,tif.query,tif.useCache);
}else{
if(tif.startTime){
if(!now){
now=(new Date()).getTime();
}
if(tif.startTime+(tif.req.timeoutSeconds*1000)<now){
if(typeof tif.http.abort=="function"){
tif.http.abort();
}
this.inFlight.splice(x,1);
tif.req[(typeof tif.req.timeout=="function")?"timeout":"handle"]("timeout",null,tif.http,tif.req);
}
}
}
}
catch(e){
try{
var _7f3=new dojo.io.Error("XMLHttpTransport.watchInFlight Error: "+e);
tif.req[(typeof tif.req.error=="function")?"error":"handle"]("error",_7f3,tif.http,tif.req);
}
catch(e2){
dojo.debug("XMLHttpTransport error callback failed: "+e2);
}
}
}
}
clearTimeout(this.inFlightTimer);
if(this.inFlight.length==0){
this.inFlightTimer=null;
return;
}
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
};
var _7f4=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_7f5){
return _7f4&&dojo.lang.inArray(["text/plain","text/html","application/xml","text/xml","text/javascript","text/json","application/json"],(_7f5["mimetype"].toLowerCase()||""))&&!(_7f5["formNode"]&&dojo.io.formHasFile(_7f5["formNode"]));
};
this.multipartBoundary="45309FFF-BD65-4d50-99C9-36986896A96F";
this.bind=function(_7f6){
if(!_7f6["url"]){
if(!_7f6["formNode"]&&(_7f6["backButton"]||_7f6["back"]||_7f6["changeUrl"]||_7f6["watchForURL"])&&(!djConfig.preventBackButtonFix)){
dojo.deprecated("Using dojo.io.XMLHTTPTransport.bind() to add to browser history without doing an IO request","Use dojo.undo.browser.addToHistory() instead.","0.4");
dojo.undo.browser.addToHistory(_7f6);
return true;
}
}
var url=_7f6.url;
var _7f8="";
if(_7f6["formNode"]){
var ta=_7f6.formNode.getAttribute("action");
if((ta)&&(!_7f6["url"])){
url=ta;
}
var tp=_7f6.formNode.getAttribute("method");
if((tp)&&(!_7f6["method"])){
_7f6.method=tp;
}
_7f8+=dojo.io.encodeForm(_7f6.formNode,_7f6.encoding,_7f6["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
if(_7f6["file"]){
_7f6.method="post";
}
if(!_7f6["method"]){
_7f6.method="get";
}
if(_7f6.method.toLowerCase()=="get"){
_7f6.multipart=false;
}else{
if(_7f6["file"]){
_7f6.multipart=true;
}else{
if(!_7f6["multipart"]){
_7f6.multipart=false;
}
}
}
if(_7f6["backButton"]||_7f6["back"]||_7f6["changeUrl"]){
dojo.undo.browser.addToHistory(_7f6);
}
var _7fb=_7f6["content"]||{};
if(_7f6.sendTransport){
_7fb["dojo.transport"]="xmlhttp";
}
do{
if(_7f6.postContent){
_7f8=_7f6.postContent;
break;
}
if(_7fb){
_7f8+=dojo.io.argsFromMap(_7fb,_7f6.encoding);
}
if(_7f6.method.toLowerCase()=="get"||!_7f6.multipart){
break;
}
var t=[];
if(_7f8.length){
var q=_7f8.split("&");
for(var i=0;i<q.length;++i){
if(q[i].length){
var p=q[i].split("=");
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+p[0]+"\"","",p[1]);
}
}
}
if(_7f6.file){
if(dojo.lang.isArray(_7f6.file)){
for(var i=0;i<_7f6.file.length;++i){
var o=_7f6.file[i];
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}else{
var o=_7f6.file;
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}
if(t.length){
t.push("--"+this.multipartBoundary+"--","");
_7f8=t.join("\r\n");
}
}while(false);
var _801=_7f6["sync"]?false:true;
var _802=_7f6["preventCache"]||(this.preventCache==true&&_7f6["preventCache"]!=false);
var _803=_7f6["useCache"]==true||(this.useCache==true&&_7f6["useCache"]!=false);
if(!_802&&_803){
var _804=getFromCache(url,_7f8,_7f6.method);
if(_804){
doLoad(_7f6,_804,url,_7f8,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject(_7f6);
var _806=false;
if(_801){
var _807=this.inFlight.push({"req":_7f6,"http":http,"url":url,"query":_7f8,"useCache":_803,"startTime":_7f6.timeoutSeconds?(new Date()).getTime():0});
this.startWatchingInFlight();
}else{
_7d6._blockAsync=true;
}
if(_7f6.method.toLowerCase()=="post"){
if(!_7f6.user){
http.open("POST",url,_801);
}else{
http.open("POST",url,_801,_7f6.user,_7f6.password);
}
setHeaders(http,_7f6);
http.setRequestHeader("Content-Type",_7f6.multipart?("multipart/form-data; boundary="+this.multipartBoundary):(_7f6.contentType||"application/x-www-form-urlencoded"));
try{
http.send(_7f8);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_7f6,{status:404},url,_7f8,_803);
}
}else{
var _808=url;
if(_7f8!=""){
_808+=(_808.indexOf("?")>-1?"&":"?")+_7f8;
}
if(_802){
_808+=(dojo.string.endsWithAny(_808,"?","&")?"":(_808.indexOf("?")>-1?"&":"?"))+"dojo.preventCache="+new Date().valueOf();
}
if(!_7f6.user){
http.open(_7f6.method.toUpperCase(),_808,_801);
}else{
http.open(_7f6.method.toUpperCase(),_808,_801,_7f6.user,_7f6.password);
}
setHeaders(http,_7f6);
try{
http.send(null);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_7f6,{status:404},url,_7f8,_803);
}
}
if(!_801){
doLoad(_7f6,http,url,_7f8,_803);
_7d6._blockAsync=false;
}
_7f6.abort=function(){
try{
http._aborted=true;
}
catch(e){
}
return http.abort();
};
return;
};
dojo.io.transports.addTransport("XMLHTTPTransport");
};
}
dojo.provide("dojo.io.cookie");
dojo.io.cookie.setCookie=function(name,_80a,days,path,_80d,_80e){
var _80f=-1;
if((typeof days=="number")&&(days>=0)){
var d=new Date();
d.setTime(d.getTime()+(days*24*60*60*1000));
_80f=d.toGMTString();
}
_80a=escape(_80a);
document.cookie=name+"="+_80a+";"+(_80f!=-1?" expires="+_80f+";":"")+(path?"path="+path:"")+(_80d?"; domain="+_80d:"")+(_80e?"; secure":"");
};
dojo.io.cookie.set=dojo.io.cookie.setCookie;
dojo.io.cookie.getCookie=function(name){
var idx=document.cookie.lastIndexOf(name+"=");
if(idx==-1){
return null;
}
var _813=document.cookie.substring(idx+name.length+1);
var end=_813.indexOf(";");
if(end==-1){
end=_813.length;
}
_813=_813.substring(0,end);
_813=unescape(_813);
return _813;
};
dojo.io.cookie.get=dojo.io.cookie.getCookie;
dojo.io.cookie.deleteCookie=function(name){
dojo.io.cookie.setCookie(name,"-",0);
};
dojo.io.cookie.setObjectCookie=function(name,obj,days,path,_81a,_81b,_81c){
if(arguments.length==5){
_81c=_81a;
_81a=null;
_81b=null;
}
var _81d=[],_81e,_81f="";
if(!_81c){
_81e=dojo.io.cookie.getObjectCookie(name);
}
if(days>=0){
if(!_81e){
_81e={};
}
for(var prop in obj){
if(obj[prop]==null){
delete _81e[prop];
}else{
if((typeof obj[prop]=="string")||(typeof obj[prop]=="number")){
_81e[prop]=obj[prop];
}
}
}
prop=null;
for(var prop in _81e){
_81d.push(escape(prop)+"="+escape(_81e[prop]));
}
_81f=_81d.join("&");
}
dojo.io.cookie.setCookie(name,_81f,days,path,_81a,_81b);
};
dojo.io.cookie.getObjectCookie=function(name){
var _822=null,_823=dojo.io.cookie.getCookie(name);
if(_823){
_822={};
var _824=_823.split("&");
for(var i=0;i<_824.length;i++){
var pair=_824[i].split("=");
var _827=pair[1];
if(isNaN(_827)){
_827=unescape(pair[1]);
}
_822[unescape(pair[0])]=_827;
}
}
return _822;
};
dojo.io.cookie.isSupported=function(){
if(typeof navigator.cookieEnabled!="boolean"){
dojo.io.cookie.setCookie("__TestingYourBrowserForCookieSupport__","CookiesAllowed",90,null);
var _828=dojo.io.cookie.getCookie("__TestingYourBrowserForCookieSupport__");
navigator.cookieEnabled=(_828=="CookiesAllowed");
if(navigator.cookieEnabled){
this.deleteCookie("__TestingYourBrowserForCookieSupport__");
}
}
return navigator.cookieEnabled;
};
if(!dojo.io.cookies){
dojo.io.cookies=dojo.io.cookie;
}
dojo.kwCompoundRequire({common:["dojo.io.common"],rhino:["dojo.io.RhinoIO"],browser:["dojo.io.BrowserIO","dojo.io.cookie"],dashboard:["dojo.io.BrowserIO","dojo.io.cookie"]});
dojo.provide("dojo.io.*");
dojo.provide("dojo.widget.ContentPane");
dojo.widget.defineWidget("dojo.widget.ContentPane",dojo.widget.HtmlWidget,function(){
this._styleNodes=[];
this._onLoadStack=[];
this._onUnloadStack=[];
this._callOnUnload=false;
this._ioBindObj;
this.scriptScope;
this.bindArgs={};
},{isContainer:true,adjustPaths:true,href:"",extractContent:true,parseContent:true,cacheContent:true,preload:false,refreshOnShow:false,handler:"",executeScripts:false,scriptSeparation:true,loadingMessage:"Loading...",isLoaded:false,postCreate:function(args,frag,_82b){
if(this.handler!==""){
this.setHandler(this.handler);
}
if(this.isShowing()||this.preload){
this.loadContents();
}
},show:function(){
if(this.refreshOnShow){
this.refresh();
}else{
this.loadContents();
}
dojo.widget.ContentPane.superclass.show.call(this);
},refresh:function(){
this.isLoaded=false;
this.loadContents();
},loadContents:function(){
if(this.isLoaded){
return;
}
if(dojo.lang.isFunction(this.handler)){
this._runHandler();
}else{
if(this.href!=""){
this._downloadExternalContent(this.href,this.cacheContent&&!this.refreshOnShow);
}
}
},setUrl:function(url){
this.href=url;
this.isLoaded=false;
if(this.preload||this.isShowing()){
this.loadContents();
}
},abort:function(){
var bind=this._ioBindObj;
if(!bind||!bind.abort){
return;
}
bind.abort();
delete this._ioBindObj;
},_downloadExternalContent:function(url,_82f){
this.abort();
this._handleDefaults(this.loadingMessage,"onDownloadStart");
var self=this;
this._ioBindObj=dojo.io.bind(this._cacheSetting({url:url,mimetype:"text/html",handler:function(type,data,xhr){
delete self._ioBindObj;
if(type=="load"){
self.onDownloadEnd.call(self,url,data);
}else{
var e={responseText:xhr.responseText,status:xhr.status,statusText:xhr.statusText,responseHeaders:xhr.getAllResponseHeaders(),text:"Error loading '"+url+"' ("+xhr.status+" "+xhr.statusText+")"};
self._handleDefaults.call(self,e,"onDownloadError");
self.onLoad();
}
}},_82f));
},_cacheSetting:function(_835,_836){
for(var x in this.bindArgs){
if(dojo.lang.isUndefined(_835[x])){
_835[x]=this.bindArgs[x];
}
}
if(dojo.lang.isUndefined(_835.useCache)){
_835.useCache=_836;
}
if(dojo.lang.isUndefined(_835.preventCache)){
_835.preventCache=!_836;
}
if(dojo.lang.isUndefined(_835.mimetype)){
_835.mimetype="text/html";
}
return _835;
},onLoad:function(e){
this._runStack("_onLoadStack");
this.isLoaded=true;
},onUnLoad:function(e){
dojo.deprecated(this.widgetType+".onUnLoad, use .onUnload (lowercased load)",0.5);
},onUnload:function(e){
this._runStack("_onUnloadStack");
delete this.scriptScope;
if(this.onUnLoad!==dojo.widget.ContentPane.prototype.onUnLoad){
this.onUnLoad.apply(this,arguments);
}
},_runStack:function(_83b){
var st=this[_83b];
var err="";
var _83e=this.scriptScope||window;
for(var i=0;i<st.length;i++){
try{
st[i].call(_83e);
}
catch(e){
err+="\n"+st[i]+" failed: "+e.description;
}
}
this[_83b]=[];
if(err.length){
var name=(_83b=="_onLoadStack")?"addOnLoad":"addOnUnLoad";
this._handleDefaults(name+" failure\n "+err,"onExecError","debug");
}
},addOnLoad:function(obj,func){
this._pushOnStack(this._onLoadStack,obj,func);
},addOnUnload:function(obj,func){
this._pushOnStack(this._onUnloadStack,obj,func);
},addOnUnLoad:function(){
dojo.deprecated(this.widgetType+".addOnUnLoad, use addOnUnload instead. (lowercased Load)",0.5);
this.addOnUnload.apply(this,arguments);
},_pushOnStack:function(_845,obj,func){
if(typeof func=="undefined"){
_845.push(obj);
}else{
_845.push(function(){
obj[func]();
});
}
},destroy:function(){
this.onUnload();
dojo.widget.ContentPane.superclass.destroy.call(this);
},onExecError:function(e){
},onContentError:function(e){
},onDownloadError:function(e){
},onDownloadStart:function(e){
},onDownloadEnd:function(url,data){
data=this.splitAndFixPaths(data,url);
this.setContent(data);
},_handleDefaults:function(e,_84f,_850){
if(!_84f){
_84f="onContentError";
}
if(dojo.lang.isString(e)){
e={text:e};
}
if(!e.text){
e.text=e.toString();
}
e.toString=function(){
return this.text;
};
if(typeof e.returnValue!="boolean"){
e.returnValue=true;
}
if(typeof e.preventDefault!="function"){
e.preventDefault=function(){
this.returnValue=false;
};
}
this[_84f](e);
if(e.returnValue){
switch(_850){
case true:
case "alert":
alert(e.toString());
break;
case "debug":
dojo.debug(e.toString());
break;
default:
if(this._callOnUnload){
this.onUnload();
}
this._callOnUnload=false;
if(arguments.callee._loopStop){
dojo.debug(e.toString());
}else{
arguments.callee._loopStop=true;
this._setContent(e.toString());
}
}
}
arguments.callee._loopStop=false;
},splitAndFixPaths:function(s,url){
var _853=[],_854=[],tmp=[];
var _856=[],_857=[],attr=[],_859=[];
var str="",path="",fix="",_85d="",tag="",_85f="";
if(!url){
url="./";
}
if(s){
var _860=/<title[^>]*>([\s\S]*?)<\/title>/i;
while(_856=_860.exec(s)){
_853.push(_856[1]);
s=s.substring(0,_856.index)+s.substr(_856.index+_856[0].length);
}
if(this.adjustPaths){
var _861=/<[a-z][a-z0-9]*[^>]*\s(?:(?:src|href|style)=[^>])+[^>]*>/i;
var _862=/\s(src|href|style)=(['"]?)([\w()\[\]\/.,\\'"-:;#=&?\s@]+?)\2/i;
var _863=/^(?:[#]|(?:(?:https?|ftps?|file|javascript|mailto|news):))/;
while(tag=_861.exec(s)){
str+=s.substring(0,tag.index);
s=s.substring((tag.index+tag[0].length),s.length);
tag=tag[0];
_85d="";
while(attr=_862.exec(tag)){
path="";
_85f=attr[3];
switch(attr[1].toLowerCase()){
case "src":
case "href":
if(_863.exec(_85f)){
path=_85f;
}else{
path=(new dojo.uri.Uri(url,_85f).toString());
}
break;
case "style":
path=dojo.html.fixPathsInCssText(_85f,url);
break;
default:
path=_85f;
}
fix=" "+attr[1]+"="+attr[2]+path+attr[2];
_85d+=tag.substring(0,attr.index)+fix;
tag=tag.substring((attr.index+attr[0].length),tag.length);
}
str+=_85d+tag;
}
s=str+s;
}
_860=/(?:<(style)[^>]*>([\s\S]*?)<\/style>|<link ([^>]*rel=['"]?stylesheet['"]?[^>]*)>)/i;
while(_856=_860.exec(s)){
if(_856[1]&&_856[1].toLowerCase()=="style"){
_859.push(dojo.html.fixPathsInCssText(_856[2],url));
}else{
if(attr=_856[3].match(/href=(['"]?)([^'">]*)\1/i)){
_859.push({path:attr[2]});
}
}
s=s.substring(0,_856.index)+s.substr(_856.index+_856[0].length);
}
var _860=/<script([^>]*)>([\s\S]*?)<\/script>/i;
var _864=/src=(['"]?)([^"']*)\1/i;
var _865=/.*(\bdojo\b\.js(?:\.uncompressed\.js)?)$/;
var _866=/(?:var )?\bdjConfig\b(?:[\s]*=[\s]*\{[^}]+\}|\.[\w]*[\s]*=[\s]*[^;\n]*)?;?|dojo\.hostenv\.writeIncludes\(\s*\);?/g;
var _867=/dojo\.(?:(?:require(?:After)?(?:If)?)|(?:widget\.(?:manager\.)?registerWidgetPackage)|(?:(?:hostenv\.)?setModulePrefix|registerModulePath)|defineNamespace)\((['"]).*?\1\)\s*;?/;
while(_856=_860.exec(s)){
if(this.executeScripts&&_856[1]){
if(attr=_864.exec(_856[1])){
if(_865.exec(attr[2])){
dojo.debug("Security note! inhibit:"+attr[2]+" from  being loaded again.");
}else{
_854.push({path:attr[2]});
}
}
}
if(_856[2]){
var sc=_856[2].replace(_866,"");
if(!sc){
continue;
}
while(tmp=_867.exec(sc)){
_857.push(tmp[0]);
sc=sc.substring(0,tmp.index)+sc.substr(tmp.index+tmp[0].length);
}
if(this.executeScripts){
_854.push(sc);
}
}
s=s.substr(0,_856.index)+s.substr(_856.index+_856[0].length);
}
if(this.extractContent){
_856=s.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
if(_856){
s=_856[1];
}
}
if(this.executeScripts&&this.scriptSeparation){
var _860=/(<[a-zA-Z][a-zA-Z0-9]*\s[^>]*?\S=)((['"])[^>]*scriptScope[^>]*>)/;
var _869=/([\s'";:\(])scriptScope(.*)/;
str="";
while(tag=_860.exec(s)){
tmp=((tag[3]=="'")?"\"":"'");
fix="";
str+=s.substring(0,tag.index)+tag[1];
while(attr=_869.exec(tag[2])){
tag[2]=tag[2].substring(0,attr.index)+attr[1]+"dojo.widget.byId("+tmp+this.widgetId+tmp+").scriptScope"+attr[2];
}
str+=tag[2];
s=s.substr(tag.index+tag[0].length);
}
s=str+s;
}
}
return {"xml":s,"styles":_859,"titles":_853,"requires":_857,"scripts":_854,"url":url};
},_setContent:function(cont){
this.destroyChildren();
for(var i=0;i<this._styleNodes.length;i++){
if(this._styleNodes[i]&&this._styleNodes[i].parentNode){
this._styleNodes[i].parentNode.removeChild(this._styleNodes[i]);
}
}
this._styleNodes=[];
try{
var node=this.containerNode||this.domNode;
while(node.firstChild){
dojo.html.destroyNode(node.firstChild);
}
if(typeof cont!="string"){
node.appendChild(cont);
}else{
node.innerHTML=cont;
}
}
catch(e){
e.text="Couldn't load content:"+e.description;
this._handleDefaults(e,"onContentError");
}
},setContent:function(data){
this.abort();
if(this._callOnUnload){
this.onUnload();
}
this._callOnUnload=true;
if(!data||dojo.html.isNode(data)){
this._setContent(data);
this.onResized();
this.onLoad();
}else{
if(typeof data.xml!="string"){
this.href="";
data=this.splitAndFixPaths(data);
}
this._setContent(data.xml);
for(var i=0;i<data.styles.length;i++){
if(data.styles[i].path){
this._styleNodes.push(dojo.html.insertCssFile(data.styles[i].path,dojo.doc(),false,true));
}else{
this._styleNodes.push(dojo.html.insertCssText(data.styles[i]));
}
}
if(this.parseContent){
for(var i=0;i<data.requires.length;i++){
try{
eval(data.requires[i]);
}
catch(e){
e.text="ContentPane: error in package loading calls, "+(e.description||e);
this._handleDefaults(e,"onContentError","debug");
}
}
}
var _86f=this;
function asyncParse(){
if(_86f.executeScripts){
_86f._executeScripts(data.scripts);
}
if(_86f.parseContent){
var node=_86f.containerNode||_86f.domNode;
var _871=new dojo.xml.Parse();
var frag=_871.parseElement(node,null,true);
dojo.widget.getParser().createSubComponents(frag,_86f);
}
_86f.onResized();
_86f.onLoad();
}
if(dojo.hostenv.isXDomain&&data.requires.length){
dojo.addOnLoad(asyncParse);
}else{
asyncParse();
}
}
},setHandler:function(_873){
var fcn=dojo.lang.isFunction(_873)?_873:window[_873];
if(!dojo.lang.isFunction(fcn)){
this._handleDefaults("Unable to set handler, '"+_873+"' not a function.","onExecError",true);
return;
}
this.handler=function(){
return fcn.apply(this,arguments);
};
},_runHandler:function(){
var ret=true;
if(dojo.lang.isFunction(this.handler)){
this.handler(this,this.domNode);
ret=false;
}
this.onLoad();
return ret;
},_executeScripts:function(_876){
var self=this;
var tmp="",code="";
for(var i=0;i<_876.length;i++){
if(_876[i].path){
dojo.io.bind(this._cacheSetting({"url":_876[i].path,"load":function(type,_87c){
dojo.lang.hitch(self,tmp=";"+_87c);
},"error":function(type,_87e){
_87e.text=type+" downloading remote script";
self._handleDefaults.call(self,_87e,"onExecError","debug");
},"mimetype":"text/plain","sync":true},this.cacheContent));
code+=tmp;
}else{
code+=_876[i];
}
}
try{
if(this.scriptSeparation){
delete this.scriptScope;
this.scriptScope=new (new Function("_container_",code+"; return this;"))(self);
}else{
var djg=dojo.global();
if(djg.execScript){
djg.execScript(code);
}else{
var djd=dojo.doc();
var sc=djd.createElement("script");
sc.appendChild(djd.createTextNode(code));
(this.containerNode||this.domNode).appendChild(sc);
}
}
}
catch(e){
e.text="Error running scripts from content:\n"+e.description;
this._handleDefaults(e,"onExecError","debug");
}
}});
dojo.provide("dojo.widget.html.layout");
dojo.widget.html.layout=function(_882,_883,_884){
dojo.html.addClass(_882,"dojoLayoutContainer");
_883=dojo.lang.filter(_883,function(_885,idx){
_885.idx=idx;
return dojo.lang.inArray(["top","bottom","left","right","client","flood"],_885.layoutAlign);
});
if(_884&&_884!="none"){
var rank=function(_888){
switch(_888.layoutAlign){
case "flood":
return 1;
case "left":
case "right":
return (_884=="left-right")?2:3;
case "top":
case "bottom":
return (_884=="left-right")?3:2;
default:
return 4;
}
};
_883.sort(function(a,b){
return (rank(a)-rank(b))||(a.idx-b.idx);
});
}
var f={top:dojo.html.getPixelValue(_882,"padding-top",true),left:dojo.html.getPixelValue(_882,"padding-left",true)};
dojo.lang.mixin(f,dojo.html.getContentBox(_882));
dojo.lang.forEach(_883,function(_88c){
var elm=_88c.domNode;
var pos=_88c.layoutAlign;
with(elm.style){
left=f.left+"px";
top=f.top+"px";
bottom="auto";
right="auto";
}
dojo.html.addClass(elm,"dojoAlign"+dojo.string.capitalize(pos));
if((pos=="top")||(pos=="bottom")){
dojo.html.setMarginBox(elm,{width:f.width});
var h=dojo.html.getMarginBox(elm).height;
f.height-=h;
if(pos=="top"){
f.top+=h;
}else{
elm.style.top=f.top+f.height+"px";
}
if(_88c.onResized){
_88c.onResized();
}
}else{
if(pos=="left"||pos=="right"){
var w=dojo.html.getMarginBox(elm).width;
if(_88c.resizeTo){
_88c.resizeTo(w,f.height);
}else{
dojo.html.setMarginBox(elm,{width:w,height:f.height});
}
f.width-=w;
if(pos=="left"){
f.left+=w;
}else{
elm.style.left=f.left+f.width+"px";
}
}else{
if(pos=="flood"||pos=="client"){
if(_88c.resizeTo){
_88c.resizeTo(f.width,f.height);
}else{
dojo.html.setMarginBox(elm,{width:f.width,height:f.height});
}
}
}
}
});
};
dojo.html.insertCssText(".dojoLayoutContainer{ position: relative; display: block; overflow: hidden; }\n"+"body .dojoAlignTop, body .dojoAlignBottom, body .dojoAlignLeft, body .dojoAlignRight { position: absolute; overflow: hidden; }\n"+"body .dojoAlignClient { position: absolute }\n"+".dojoAlignClient { overflow: auto; }\n");
dojo.provide("dojo.widget.LayoutContainer");
dojo.widget.defineWidget("dojo.widget.LayoutContainer",dojo.widget.HtmlWidget,{isContainer:true,layoutChildPriority:"top-bottom",postCreate:function(){
dojo.widget.html.layout(this.domNode,this.children,this.layoutChildPriority);
},addChild:function(_891,_892,pos,ref,_895){
dojo.widget.LayoutContainer.superclass.addChild.call(this,_891,_892,pos,ref,_895);
dojo.widget.html.layout(this.domNode,this.children,this.layoutChildPriority);
},removeChild:function(pane){
dojo.widget.LayoutContainer.superclass.removeChild.call(this,pane);
dojo.widget.html.layout(this.domNode,this.children,this.layoutChildPriority);
},onResized:function(){
dojo.widget.html.layout(this.domNode,this.children,this.layoutChildPriority);
},show:function(){
this.domNode.style.display="";
this.checkSize();
this.domNode.style.display="none";
this.domNode.style.visibility="";
dojo.widget.LayoutContainer.superclass.show.call(this);
}});
dojo.lang.extend(dojo.widget.Widget,{layoutAlign:"none"});
dojo.provide("dojo.html.selection");
dojo.html.selectionType={NONE:0,TEXT:1,CONTROL:2};
dojo.html.clearSelection=function(){
var _897=dojo.global();
var _898=dojo.doc();
try{
if(_897["getSelection"]){
if(dojo.render.html.safari){
_897.getSelection().collapse();
}else{
_897.getSelection().removeAllRanges();
}
}else{
if(_898.selection){
if(_898.selection.empty){
_898.selection.empty();
}else{
if(_898.selection.clear){
_898.selection.clear();
}
}
}
}
return true;
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.html.disableSelection=function(_899){
_899=dojo.byId(_899)||dojo.body();
var h=dojo.render.html;
if(h.mozilla){
_899.style.MozUserSelect="none";
}else{
if(h.safari){
_899.style.KhtmlUserSelect="none";
}else{
if(h.ie){
_899.unselectable="on";
}else{
return false;
}
}
}
return true;
};
dojo.html.enableSelection=function(_89b){
_89b=dojo.byId(_89b)||dojo.body();
var h=dojo.render.html;
if(h.mozilla){
_89b.style.MozUserSelect="";
}else{
if(h.safari){
_89b.style.KhtmlUserSelect="";
}else{
if(h.ie){
_89b.unselectable="off";
}else{
return false;
}
}
}
return true;
};
dojo.html.selectElement=function(_89d){
dojo.deprecated("dojo.html.selectElement","replaced by dojo.html.selection.selectElementChildren",0.5);
};
dojo.html.selectInputText=function(_89e){
var _89f=dojo.global();
var _8a0=dojo.doc();
_89e=dojo.byId(_89e);
if(_8a0["selection"]&&dojo.body()["createTextRange"]){
var _8a1=_89e.createTextRange();
_8a1.moveStart("character",0);
_8a1.moveEnd("character",_89e.value.length);
_8a1.select();
}else{
if(_89f["getSelection"]){
var _8a2=_89f.getSelection();
_89e.setSelectionRange(0,_89e.value.length);
}
}
_89e.focus();
};
dojo.html.isSelectionCollapsed=function(){
dojo.deprecated("dojo.html.isSelectionCollapsed","replaced by dojo.html.selection.isCollapsed",0.5);
return dojo.html.selection.isCollapsed();
};
dojo.lang.mixin(dojo.html.selection,{getType:function(){
if(dojo.doc()["selection"]){
return dojo.html.selectionType[dojo.doc().selection.type.toUpperCase()];
}else{
var _8a3=dojo.html.selectionType.TEXT;
var oSel;
try{
oSel=dojo.global().getSelection();
}
catch(e){
}
if(oSel&&oSel.rangeCount==1){
var _8a5=oSel.getRangeAt(0);
if(_8a5.startContainer==_8a5.endContainer&&(_8a5.endOffset-_8a5.startOffset)==1&&_8a5.startContainer.nodeType!=dojo.dom.TEXT_NODE){
_8a3=dojo.html.selectionType.CONTROL;
}
}
return _8a3;
}
},isCollapsed:function(){
var _8a6=dojo.global();
var _8a7=dojo.doc();
if(_8a7["selection"]){
return _8a7.selection.createRange().text=="";
}else{
if(_8a6["getSelection"]){
var _8a8=_8a6.getSelection();
if(dojo.lang.isString(_8a8)){
return _8a8=="";
}else{
return _8a8.isCollapsed||_8a8.toString()=="";
}
}
}
},getSelectedElement:function(){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
if(dojo.doc()["selection"]){
var _8a9=dojo.doc().selection.createRange();
if(_8a9&&_8a9.item){
return dojo.doc().selection.createRange().item(0);
}
}else{
var _8aa=dojo.global().getSelection();
return _8aa.anchorNode.childNodes[_8aa.anchorOffset];
}
}
},getParentElement:function(){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
var p=dojo.html.selection.getSelectedElement();
if(p){
return p.parentNode;
}
}else{
if(dojo.doc()["selection"]){
return dojo.doc().selection.createRange().parentElement();
}else{
var _8ac=dojo.global().getSelection();
if(_8ac){
var node=_8ac.anchorNode;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.parentNode;
}
return node;
}
}
}
},getSelectedText:function(){
if(dojo.doc()["selection"]){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
return null;
}
return dojo.doc().selection.createRange().text;
}else{
var _8ae=dojo.global().getSelection();
if(_8ae){
return _8ae.toString();
}
}
},getSelectedHtml:function(){
if(dojo.doc()["selection"]){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
return null;
}
return dojo.doc().selection.createRange().htmlText;
}else{
var _8af=dojo.global().getSelection();
if(_8af&&_8af.rangeCount){
var frag=_8af.getRangeAt(0).cloneContents();
var div=document.createElement("div");
div.appendChild(frag);
return div.innerHTML;
}
return null;
}
},hasAncestorElement:function(_8b2){
return (dojo.html.selection.getAncestorElement.apply(this,arguments)!=null);
},getAncestorElement:function(_8b3){
var node=dojo.html.selection.getSelectedElement()||dojo.html.selection.getParentElement();
while(node){
if(dojo.html.selection.isTag(node,arguments).length>0){
return node;
}
node=node.parentNode;
}
return null;
},isTag:function(node,tags){
if(node&&node.tagName){
for(var i=0;i<tags.length;i++){
if(node.tagName.toLowerCase()==String(tags[i]).toLowerCase()){
return String(tags[i]).toLowerCase();
}
}
}
return "";
},selectElement:function(_8b8){
var _8b9=dojo.global();
var _8ba=dojo.doc();
_8b8=dojo.byId(_8b8);
if(_8ba.selection&&dojo.body().createTextRange){
try{
var _8bb=dojo.body().createControlRange();
_8bb.addElement(_8b8);
_8bb.select();
}
catch(e){
dojo.html.selection.selectElementChildren(_8b8);
}
}else{
if(_8b9["getSelection"]){
var _8bc=_8b9.getSelection();
if(_8bc["removeAllRanges"]){
var _8bb=_8ba.createRange();
_8bb.selectNode(_8b8);
_8bc.removeAllRanges();
_8bc.addRange(_8bb);
}
}
}
},selectElementChildren:function(_8bd){
var _8be=dojo.global();
var _8bf=dojo.doc();
_8bd=dojo.byId(_8bd);
if(_8bf.selection&&dojo.body().createTextRange){
var _8c0=dojo.body().createTextRange();
_8c0.moveToElementText(_8bd);
_8c0.select();
}else{
if(_8be["getSelection"]){
var _8c1=_8be.getSelection();
if(_8c1["setBaseAndExtent"]){
_8c1.setBaseAndExtent(_8bd,0,_8bd,_8bd.innerText.length-1);
}else{
if(_8c1["selectAllChildren"]){
_8c1.selectAllChildren(_8bd);
}
}
}
}
},getBookmark:function(){
var _8c2;
var _8c3=dojo.doc();
if(_8c3["selection"]){
var _8c4=_8c3.selection.createRange();
_8c2=_8c4.getBookmark();
}else{
var _8c5;
try{
_8c5=dojo.global().getSelection();
}
catch(e){
}
if(_8c5){
var _8c4=_8c5.getRangeAt(0);
_8c2=_8c4.cloneRange();
}else{
dojo.debug("No idea how to store the current selection for this browser!");
}
}
return _8c2;
},moveToBookmark:function(_8c6){
var _8c7=dojo.doc();
if(_8c7["selection"]){
var _8c8=_8c7.selection.createRange();
_8c8.moveToBookmark(_8c6);
_8c8.select();
}else{
var _8c9;
try{
_8c9=dojo.global().getSelection();
}
catch(e){
}
if(_8c9&&_8c9["removeAllRanges"]){
_8c9.removeAllRanges();
_8c9.addRange(_8c6);
}else{
dojo.debug("No idea how to restore selection for this browser!");
}
}
},collapse:function(_8ca){
if(dojo.global()["getSelection"]){
var _8cb=dojo.global().getSelection();
if(_8cb.removeAllRanges){
if(_8ca){
_8cb.collapseToStart();
}else{
_8cb.collapseToEnd();
}
}else{
dojo.global().getSelection().collapse(_8ca);
}
}else{
if(dojo.doc().selection){
var _8cc=dojo.doc().selection.createRange();
_8cc.collapse(_8ca);
_8cc.select();
}
}
},remove:function(){
if(dojo.doc().selection){
var _8cd=dojo.doc().selection;
if(_8cd.type.toUpperCase()!="NONE"){
_8cd.clear();
}
return _8cd;
}else{
var _8cd=dojo.global().getSelection();
for(var i=0;i<_8cd.rangeCount;i++){
_8cd.getRangeAt(i).deleteContents();
}
return _8cd;
}
}});
dojo.provide("dojo.widget.Checkbox");
dojo.widget.defineWidget("dojo.widget.Checkbox",dojo.widget.HtmlWidget,{templatePath:dojo.uri.moduleUri("dojo.widget","templates/Checkbox.html"),templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/Checkbox.css"),name:"",id:"",checked:false,tabIndex:"",value:"on",postMixInProperties:function(){
dojo.widget.Checkbox.superclass.postMixInProperties.apply(this,arguments);
if(!this.disabled&&this.tabIndex==""){
this.tabIndex="0";
}
},fillInTemplate:function(){
this._setInfo();
},postCreate:function(){
var _8cf=true;
this.id=this.id!=""?this.id:this.widgetId;
if(this.id!=""){
var _8d0=document.getElementsByTagName("label");
if(_8d0!=null&&_8d0.length>0){
for(var i=0;i<_8d0.length;i++){
if(_8d0[i].htmlFor==this.id){
_8d0[i].id=(_8d0[i].htmlFor+"label");
this._connectEvents(_8d0[i]);
dojo.widget.wai.setAttr(this.domNode,"waiState","labelledby",_8d0[i].id);
break;
}
}
}
}
this._connectEvents(this.domNode);
this.inputNode.checked=this.checked;
},_connectEvents:function(node){
dojo.event.connect(node,"onmouseover",this,"mouseOver");
dojo.event.connect(node,"onmouseout",this,"mouseOut");
dojo.event.connect(node,"onkey",this,"onKey");
dojo.event.connect(node,"onclick",this,"_onClick");
dojo.html.disableSelection(node);
},_onClick:function(e){
if(this.disabled==false){
this.checked=!this.checked;
this._setInfo();
}
e.preventDefault();
e.stopPropagation();
this.onClick();
},setValue:function(bool){
if(this.disabled==false){
this.checked=bool;
this._setInfo();
}
},onClick:function(){
},onKey:function(e){
var k=dojo.event.browser.keys;
if(e.key==" "){
this._onClick(e);
}
},mouseOver:function(e){
this._hover(e,true);
},mouseOut:function(e){
this._hover(e,false);
},_hover:function(e,_8da){
if(this.disabled==false){
var _8db=this.checked?"On":"Off";
var _8dc="dojoHtmlCheckbox"+_8db+"Hover";
if(_8da){
dojo.html.addClass(this.imageNode,_8dc);
}else{
dojo.html.removeClass(this.imageNode,_8dc);
}
}
},_setInfo:function(){
var _8dd="dojoHtmlCheckbox"+(this.disabled?"Disabled":"")+(this.checked?"On":"Off");
dojo.html.setClass(this.imageNode,"dojoHtmlCheckbox "+_8dd);
this.inputNode.checked=this.checked;
if(this.disabled){
this.inputNode.setAttribute("disabled",true);
}else{
this.inputNode.removeAttribute("disabled");
}
dojo.widget.wai.setAttr(this.domNode,"waiState","checked",this.checked);
}});
dojo.widget.defineWidget("dojo.widget.a11y.Checkbox",dojo.widget.Checkbox,{templatePath:dojo.uri.moduleUri("dojo.widget","templates/CheckboxA11y.html"),fillInTemplate:function(){
},postCreate:function(args,frag){
this.inputNode.checked=this.checked;
if(this.disabled){
this.inputNode.setAttribute("disabled",true);
}
},_onClick:function(){
this.onClick();
}});
dojo.provide("dojo.i18n.common");
dojo.i18n.getLocalization=function(_8e0,_8e1,_8e2){
dojo.hostenv.preloadLocalizations();
_8e2=dojo.hostenv.normalizeLocale(_8e2);
var _8e3=_8e2.split("-");
var _8e4=[_8e0,"nls",_8e1].join(".");
var _8e5=dojo.hostenv.findModule(_8e4,true);
var _8e6;
for(var i=_8e3.length;i>0;i--){
var loc=_8e3.slice(0,i).join("_");
if(_8e5[loc]){
_8e6=_8e5[loc];
break;
}
}
if(!_8e6){
_8e6=_8e5.ROOT;
}
if(_8e6){
var _8e9=function(){
};
_8e9.prototype=_8e6;
return new _8e9();
}
dojo.raise("Bundle not found: "+_8e1+" in "+_8e0+" , locale="+_8e2);
};
dojo.i18n.isLTR=function(_8ea){
var lang=dojo.hostenv.normalizeLocale(_8ea).split("-")[0];
var RTL={ar:true,fa:true,he:true,ur:true,yi:true};
return !RTL[lang];
};
dojo.provide("dojo.widget.Textbox");
dojo.widget.defineWidget("dojo.widget.Textbox",dojo.widget.HtmlWidget,{className:"",name:"",value:"",type:"",trim:false,uppercase:false,lowercase:false,ucFirst:false,digit:false,htmlfloat:"none",templatePath:dojo.uri.moduleUri("dojo.widget","templates/Textbox.html"),textbox:null,fillInTemplate:function(){
this.textbox.value=this.value;
},filter:function(){
if(this.trim){
this.textbox.value=this.textbox.value.replace(/(^\s*|\s*$)/g,"");
}
if(this.uppercase){
this.textbox.value=this.textbox.value.toUpperCase();
}
if(this.lowercase){
this.textbox.value=this.textbox.value.toLowerCase();
}
if(this.ucFirst){
this.textbox.value=this.textbox.value.replace(/\b\w+\b/g,function(word){
return word.substring(0,1).toUpperCase()+word.substring(1).toLowerCase();
});
}
if(this.digit){
this.textbox.value=this.textbox.value.replace(/\D/g,"");
}
},onfocus:function(){
},onblur:function(){
this.filter();
},mixInProperties:function(_8ee,frag){
dojo.widget.Textbox.superclass.mixInProperties.apply(this,arguments);
if(_8ee["class"]){
this.className=_8ee["class"];
}
}});
dojo.provide("dojo.widget.ValidationTextbox");
dojo.widget.defineWidget("dojo.widget.ValidationTextbox",dojo.widget.Textbox,function(){
this.flags={};
},{required:false,rangeClass:"range",invalidClass:"invalid",missingClass:"missing",classPrefix:"dojoValidate",size:"",maxlength:"",promptMessage:"",invalidMessage:"",missingMessage:"",rangeMessage:"",listenOnKeyPress:true,htmlfloat:"none",lastCheckedValue:null,templatePath:dojo.uri.moduleUri("dojo.widget","templates/ValidationTextbox.html"),templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/Validate.css"),invalidSpan:null,missingSpan:null,rangeSpan:null,getValue:function(){
return this.textbox.value;
},setValue:function(_8f0){
this.textbox.value=_8f0;
this.update();
},isValid:function(){
return true;
},isInRange:function(){
return true;
},isEmpty:function(){
return (/^\s*$/.test(this.textbox.value));
},isMissing:function(){
return (this.required&&this.isEmpty());
},update:function(){
this.lastCheckedValue=this.textbox.value;
this.missingSpan.style.display="none";
this.invalidSpan.style.display="none";
this.rangeSpan.style.display="none";
var _8f1=this.isEmpty();
var _8f2=true;
if(this.promptMessage!=this.textbox.value){
_8f2=this.isValid();
}
var _8f3=this.isMissing();
if(_8f3){
this.missingSpan.style.display="";
}else{
if(!_8f1&&!_8f2){
this.invalidSpan.style.display="";
}else{
if(!_8f1&&!this.isInRange()){
this.rangeSpan.style.display="";
}
}
}
this.highlight();
},updateClass:function(_8f4){
var pre=this.classPrefix;
dojo.html.removeClass(this.textbox,pre+"Empty");
dojo.html.removeClass(this.textbox,pre+"Valid");
dojo.html.removeClass(this.textbox,pre+"Invalid");
dojo.html.addClass(this.textbox,pre+_8f4);
},highlight:function(){
if(this.isEmpty()){
this.updateClass("Empty");
}else{
if(this.isValid()&&this.isInRange()){
this.updateClass("Valid");
}else{
if(this.textbox.value!=this.promptMessage){
this.updateClass("Invalid");
}else{
this.updateClass("Empty");
}
}
}
},onfocus:function(evt){
if(!this.listenOnKeyPress){
this.updateClass("Empty");
}
},onblur:function(evt){
this.filter();
this.update();
},onkeyup:function(evt){
if(this.listenOnKeyPress){
this.update();
}else{
if(this.textbox.value!=this.lastCheckedValue){
this.updateClass("Empty");
}
}
},postMixInProperties:function(_8f9,frag){
dojo.widget.ValidationTextbox.superclass.postMixInProperties.apply(this,arguments);
this.messages=dojo.i18n.getLocalization("dojo.widget","validate",this.lang);
dojo.lang.forEach(["invalidMessage","missingMessage","rangeMessage"],function(prop){
if(this[prop]){
this.messages[prop]=this[prop];
}
},this);
},fillInTemplate:function(){
dojo.widget.ValidationTextbox.superclass.fillInTemplate.apply(this,arguments);
this.textbox.isValid=function(){
this.isValid.call(this);
};
this.textbox.isMissing=function(){
this.isMissing.call(this);
};
this.textbox.isInRange=function(){
this.isInRange.call(this);
};
dojo.html.setClass(this.invalidSpan,this.invalidClass);
this.update();
this.filter();
if(dojo.render.html.ie){
dojo.html.addClass(this.domNode,"ie");
}
if(dojo.render.html.moz){
dojo.html.addClass(this.domNode,"moz");
}
if(dojo.render.html.opera){
dojo.html.addClass(this.domNode,"opera");
}
if(dojo.render.html.safari){
dojo.html.addClass(this.domNode,"safari");
}
}});
dojo.provide("dojo.regexp");
dojo.evalObjPath("dojo.regexp.us",true);
dojo.regexp.tld=function(_8fc){
_8fc=(typeof _8fc=="object")?_8fc:{};
if(typeof _8fc.allowCC!="boolean"){
_8fc.allowCC=true;
}
if(typeof _8fc.allowInfra!="boolean"){
_8fc.allowInfra=true;
}
if(typeof _8fc.allowGeneric!="boolean"){
_8fc.allowGeneric=true;
}
var _8fd="arpa";
var _8fe="aero|biz|com|coop|edu|gov|info|int|mil|museum|name|net|org|pro|travel|xxx|jobs|mobi|post";
var ccRE="ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|"+"bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cu|cv|cx|cy|cz|de|dj|dk|dm|do|dz|"+"ec|ee|eg|er|eu|es|et|fi|fj|fk|fm|fo|fr|ga|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|"+"gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kr|kw|ky|kz|"+"la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|"+"my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|"+"re|ro|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sk|sl|sm|sn|sr|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tm|"+"tn|to|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw";
var a=[];
if(_8fc.allowInfra){
a.push(_8fd);
}
if(_8fc.allowGeneric){
a.push(_8fe);
}
if(_8fc.allowCC){
a.push(ccRE);
}
var _901="";
if(a.length>0){
_901="("+a.join("|")+")";
}
return _901;
};
dojo.regexp.ipAddress=function(_902){
_902=(typeof _902=="object")?_902:{};
if(typeof _902.allowDottedDecimal!="boolean"){
_902.allowDottedDecimal=true;
}
if(typeof _902.allowDottedHex!="boolean"){
_902.allowDottedHex=true;
}
if(typeof _902.allowDottedOctal!="boolean"){
_902.allowDottedOctal=true;
}
if(typeof _902.allowDecimal!="boolean"){
_902.allowDecimal=true;
}
if(typeof _902.allowHex!="boolean"){
_902.allowHex=true;
}
if(typeof _902.allowIPv6!="boolean"){
_902.allowIPv6=true;
}
if(typeof _902.allowHybrid!="boolean"){
_902.allowHybrid=true;
}
var _903="((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])";
var _904="(0[xX]0*[\\da-fA-F]?[\\da-fA-F]\\.){3}0[xX]0*[\\da-fA-F]?[\\da-fA-F]";
var _905="(0+[0-3][0-7][0-7]\\.){3}0+[0-3][0-7][0-7]";
var _906="(0|[1-9]\\d{0,8}|[1-3]\\d{9}|4[01]\\d{8}|42[0-8]\\d{7}|429[0-3]\\d{6}|"+"4294[0-8]\\d{5}|42949[0-5]\\d{4}|429496[0-6]\\d{3}|4294967[01]\\d{2}|42949672[0-8]\\d|429496729[0-5])";
var _907="0[xX]0*[\\da-fA-F]{1,8}";
var _908="([\\da-fA-F]{1,4}\\:){7}[\\da-fA-F]{1,4}";
var _909="([\\da-fA-F]{1,4}\\:){6}"+"((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])";
var a=[];
if(_902.allowDottedDecimal){
a.push(_903);
}
if(_902.allowDottedHex){
a.push(_904);
}
if(_902.allowDottedOctal){
a.push(_905);
}
if(_902.allowDecimal){
a.push(_906);
}
if(_902.allowHex){
a.push(_907);
}
if(_902.allowIPv6){
a.push(_908);
}
if(_902.allowHybrid){
a.push(_909);
}
var _90b="";
if(a.length>0){
_90b="("+a.join("|")+")";
}
return _90b;
};
dojo.regexp.host=function(_90c){
_90c=(typeof _90c=="object")?_90c:{};
if(typeof _90c.allowIP!="boolean"){
_90c.allowIP=true;
}
if(typeof _90c.allowLocal!="boolean"){
_90c.allowLocal=false;
}
if(typeof _90c.allowPort!="boolean"){
_90c.allowPort=true;
}
var _90d="([0-9a-zA-Z]([-0-9a-zA-Z]{0,61}[0-9a-zA-Z])?\\.)+"+dojo.regexp.tld(_90c);
var _90e=(_90c.allowPort)?"(\\:"+dojo.regexp.integer({signed:false})+")?":"";
var _90f=_90d;
if(_90c.allowIP){
_90f+="|"+dojo.regexp.ipAddress(_90c);
}
if(_90c.allowLocal){
_90f+="|localhost";
}
return "("+_90f+")"+_90e;
};
dojo.regexp.url=function(_910){
_910=(typeof _910=="object")?_910:{};
if(typeof _910.scheme=="undefined"){
_910.scheme=[true,false];
}
var _911=dojo.regexp.buildGroupRE(_910.scheme,function(q){
if(q){
return "(https?|ftps?)\\://";
}
return "";
});
var _913="(/([^?#\\s/]+/)*)?([^?#\\s/]+(\\?[^?#\\s/]*)?(#[A-Za-z][\\w.:-]*)?)?";
return _911+dojo.regexp.host(_910)+_913;
};
dojo.regexp.emailAddress=function(_914){
_914=(typeof _914=="object")?_914:{};
if(typeof _914.allowCruft!="boolean"){
_914.allowCruft=false;
}
_914.allowPort=false;
var _915="([\\da-z]+[-._+&'])*[\\da-z]+";
var _916=_915+"@"+dojo.regexp.host(_914);
if(_914.allowCruft){
_916="<?(mailto\\:)?"+_916+">?";
}
return _916;
};
dojo.regexp.emailAddressList=function(_917){
_917=(typeof _917=="object")?_917:{};
if(typeof _917.listSeparator!="string"){
_917.listSeparator="\\s;,";
}
var _918=dojo.regexp.emailAddress(_917);
var _919="("+_918+"\\s*["+_917.listSeparator+"]\\s*)*"+_918+"\\s*["+_917.listSeparator+"]?\\s*";
return _919;
};
dojo.regexp.integer=function(_91a){
_91a=(typeof _91a=="object")?_91a:{};
if(typeof _91a.signed=="undefined"){
_91a.signed=[true,false];
}
if(typeof _91a.separator=="undefined"){
_91a.separator="";
}else{
if(typeof _91a.groupSize=="undefined"){
_91a.groupSize=3;
}
}
var _91b=dojo.regexp.buildGroupRE(_91a.signed,function(q){
return q?"[-+]":"";
});
var _91d=dojo.regexp.buildGroupRE(_91a.separator,function(sep){
if(sep==""){
return "(0|[1-9]\\d*)";
}
var grp=_91a.groupSize,grp2=_91a.groupSize2;
if(typeof grp2!="undefined"){
var _921="(0|[1-9]\\d{0,"+(grp2-1)+"}(["+sep+"]\\d{"+grp2+"})*["+sep+"]\\d{"+grp+"})";
return ((grp-grp2)>0)?"("+_921+"|(0|[1-9]\\d{0,"+(grp-1)+"}))":_921;
}
return "(0|[1-9]\\d{0,"+(grp-1)+"}(["+sep+"]\\d{"+grp+"})*)";
});
return _91b+_91d;
};
dojo.regexp.realNumber=function(_922){
_922=(typeof _922=="object")?_922:{};
if(typeof _922.places!="number"){
_922.places=Infinity;
}
if(typeof _922.decimal!="string"){
_922.decimal=".";
}
if(typeof _922.fractional=="undefined"){
_922.fractional=[true,false];
}
if(typeof _922.exponent=="undefined"){
_922.exponent=[true,false];
}
if(typeof _922.eSigned=="undefined"){
_922.eSigned=[true,false];
}
var _923=dojo.regexp.integer(_922);
var _924=dojo.regexp.buildGroupRE(_922.fractional,function(q){
var re="";
if(q&&(_922.places>0)){
re="\\"+_922.decimal;
if(_922.places==Infinity){
re="("+re+"\\d+)?";
}else{
re=re+"\\d{"+_922.places+"}";
}
}
return re;
});
var _927=dojo.regexp.buildGroupRE(_922.exponent,function(q){
if(q){
return "([eE]"+dojo.regexp.integer({signed:_922.eSigned})+")";
}
return "";
});
return _923+_924+_927;
};
dojo.regexp.currency=function(_929){
_929=(typeof _929=="object")?_929:{};
if(typeof _929.signed=="undefined"){
_929.signed=[true,false];
}
if(typeof _929.symbol=="undefined"){
_929.symbol="$";
}
if(typeof _929.placement!="string"){
_929.placement="before";
}
if(typeof _929.signPlacement!="string"){
_929.signPlacement="before";
}
if(typeof _929.separator=="undefined"){
_929.separator=",";
}
if(typeof _929.fractional=="undefined"&&typeof _929.cents!="undefined"){
dojo.deprecated("dojo.regexp.currency: flags.cents","use flags.fractional instead","0.5");
_929.fractional=_929.cents;
}
if(typeof _929.decimal!="string"){
_929.decimal=".";
}
var _92a=dojo.regexp.buildGroupRE(_929.signed,function(q){
if(q){
return "[-+]";
}
return "";
});
var _92c=dojo.regexp.buildGroupRE(_929.symbol,function(_92d){
return "\\s?"+_92d.replace(/([.$?*!=:|\\\/^])/g,"\\$1")+"\\s?";
});
switch(_929.signPlacement){
case "before":
_92c=_92a+_92c;
break;
case "after":
_92c=_92c+_92a;
break;
}
var _92e=_929;
_92e.signed=false;
_92e.exponent=false;
var _92f=dojo.regexp.realNumber(_92e);
var _930;
switch(_929.placement){
case "before":
_930=_92c+_92f;
break;
case "after":
_930=_92f+_92c;
break;
}
switch(_929.signPlacement){
case "around":
_930="("+_930+"|"+"\\("+_930+"\\)"+")";
break;
case "begin":
_930=_92a+_930;
break;
case "end":
_930=_930+_92a;
break;
}
return _930;
};
dojo.regexp.us.state=function(_931){
_931=(typeof _931=="object")?_931:{};
if(typeof _931.allowTerritories!="boolean"){
_931.allowTerritories=true;
}
if(typeof _931.allowMilitary!="boolean"){
_931.allowMilitary=true;
}
var _932="AL|AK|AZ|AR|CA|CO|CT|DE|DC|FL|GA|HI|ID|IL|IN|IA|KS|KY|LA|ME|MD|MA|MI|MN|MS|MO|MT|"+"NE|NV|NH|NJ|NM|NY|NC|ND|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI|WY";
var _933="AS|FM|GU|MH|MP|PW|PR|VI";
var _934="AA|AE|AP";
if(_931.allowTerritories){
_932+="|"+_933;
}
if(_931.allowMilitary){
_932+="|"+_934;
}
return "("+_932+")";
};
dojo.regexp.time=function(_935){
dojo.deprecated("dojo.regexp.time","Use dojo.date.parse instead","0.5");
_935=(typeof _935=="object")?_935:{};
if(typeof _935.format=="undefined"){
_935.format="h:mm:ss t";
}
if(typeof _935.amSymbol!="string"){
_935.amSymbol="AM";
}
if(typeof _935.pmSymbol!="string"){
_935.pmSymbol="PM";
}
var _936=function(_937){
_937=_937.replace(/([.$?*!=:|{}\(\)\[\]\\\/^])/g,"\\$1");
var amRE=_935.amSymbol.replace(/([.$?*!=:|{}\(\)\[\]\\\/^])/g,"\\$1");
var pmRE=_935.pmSymbol.replace(/([.$?*!=:|{}\(\)\[\]\\\/^])/g,"\\$1");
_937=_937.replace("hh","(0[1-9]|1[0-2])");
_937=_937.replace("h","([1-9]|1[0-2])");
_937=_937.replace("HH","([01][0-9]|2[0-3])");
_937=_937.replace("H","([0-9]|1[0-9]|2[0-3])");
_937=_937.replace("mm","([0-5][0-9])");
_937=_937.replace("m","([1-5][0-9]|[0-9])");
_937=_937.replace("ss","([0-5][0-9])");
_937=_937.replace("s","([1-5][0-9]|[0-9])");
_937=_937.replace("t","\\s?("+amRE+"|"+pmRE+")\\s?");
return _937;
};
return dojo.regexp.buildGroupRE(_935.format,_936);
};
dojo.regexp.numberFormat=function(_93a){
_93a=(typeof _93a=="object")?_93a:{};
if(typeof _93a.format=="undefined"){
_93a.format="###-###-####";
}
var _93b=function(_93c){
_93c=_93c.replace(/([.$*!=:|{}\(\)\[\]\\\/^])/g,"\\$1");
_93c=_93c.replace(/\?/g,"\\d?");
_93c=_93c.replace(/#/g,"\\d");
return _93c;
};
return dojo.regexp.buildGroupRE(_93a.format,_93b);
};
dojo.regexp.buildGroupRE=function(a,re){
if(!(a instanceof Array)){
return re(a);
}
var b=[];
for(var i=0;i<a.length;i++){
b.push(re(a[i]));
}
return "("+b.join("|")+")";
};
dojo.provide("dojo.validate.common");
dojo.validate.isText=function(_941,_942){
_942=(typeof _942=="object")?_942:{};
if(/^\s*$/.test(_941)){
return false;
}
if(typeof _942.length=="number"&&_942.length!=_941.length){
return false;
}
if(typeof _942.minlength=="number"&&_942.minlength>_941.length){
return false;
}
if(typeof _942.maxlength=="number"&&_942.maxlength<_941.length){
return false;
}
return true;
};
dojo.validate.isInteger=function(_943,_944){
var re=new RegExp("^"+dojo.regexp.integer(_944)+"$");
return re.test(_943);
};
dojo.validate.isRealNumber=function(_946,_947){
var re=new RegExp("^"+dojo.regexp.realNumber(_947)+"$");
return re.test(_946);
};
dojo.validate.isCurrency=function(_949,_94a){
var re=new RegExp("^"+dojo.regexp.currency(_94a)+"$");
return re.test(_949);
};
dojo.validate._isInRangeCache={};
dojo.validate.isInRange=function(_94c,_94d){
_94c=_94c.replace(dojo.lang.has(_94d,"separator")?_94d.separator:",","","g").replace(dojo.lang.has(_94d,"symbol")?_94d.symbol:"$","");
if(isNaN(_94c)){
return false;
}
_94d=(typeof _94d=="object")?_94d:{};
var max=(typeof _94d.max=="number")?_94d.max:Infinity;
var min=(typeof _94d.min=="number")?_94d.min:-Infinity;
var dec=(typeof _94d.decimal=="string")?_94d.decimal:".";
var _951=dojo.validate._isInRangeCache;
var _952=_94c+"max"+max+"min"+min+"dec"+dec;
if(typeof _951[_952]!="undefined"){
return _951[_952];
}
var _953="[^"+dec+"\\deE+-]";
_94c=_94c.replace(RegExp(_953,"g"),"");
_94c=_94c.replace(/^([+-]?)(\D*)/,"$1");
_94c=_94c.replace(/(\D*)$/,"");
_953="(\\d)["+dec+"](\\d)";
_94c=_94c.replace(RegExp(_953,"g"),"$1.$2");
_94c=Number(_94c);
if(_94c<min||_94c>max){
_951[_952]=false;
return false;
}
_951[_952]=true;
return true;
};
dojo.validate.isNumberFormat=function(_954,_955){
var re=new RegExp("^"+dojo.regexp.numberFormat(_955)+"$","i");
return re.test(_954);
};
dojo.validate.isValidLuhn=function(_957){
var sum,_959,_95a;
if(typeof _957!="string"){
_957=String(_957);
}
_957=_957.replace(/[- ]/g,"");
_959=_957.length%2;
sum=0;
for(var i=0;i<_957.length;i++){
_95a=parseInt(_957.charAt(i));
if(i%2==_959){
_95a*=2;
}
if(_95a>9){
_95a-=9;
}
sum+=_95a;
}
return !(sum%10);
};
dojo.provide("dojo.widget.IntegerTextbox");
dojo.widget.defineWidget("dojo.widget.IntegerTextbox",dojo.widget.ValidationTextbox,{mixInProperties:function(_95c,frag){
dojo.widget.IntegerTextbox.superclass.mixInProperties.apply(this,arguments);
if((_95c.signed=="true")||(_95c.signed=="always")){
this.flags.signed=true;
}else{
if((_95c.signed=="false")||(_95c.signed=="never")){
this.flags.signed=false;
this.flags.min=0;
}else{
this.flags.signed=[true,false];
}
}
if(_95c.separator){
this.flags.separator=_95c.separator;
}
if(_95c.min){
this.flags.min=parseInt(_95c.min);
}
if(_95c.max){
this.flags.max=parseInt(_95c.max);
}
},isValid:function(){
return dojo.validate.isInteger(this.textbox.value,this.flags);
},isInRange:function(){
return dojo.validate.isInRange(this.textbox.value,this.flags);
}});
dojo.provide("dojo.widget.RealNumberTextbox");
dojo.widget.defineWidget("dojo.widget.RealNumberTextbox",dojo.widget.IntegerTextbox,{mixInProperties:function(_95e,frag){
dojo.widget.RealNumberTextbox.superclass.mixInProperties.apply(this,arguments);
if(_95e.places){
this.flags.places=Number(_95e.places);
}
if((_95e.exponent=="true")||(_95e.exponent=="always")){
this.flags.exponent=true;
}else{
if((_95e.exponent=="false")||(_95e.exponent=="never")){
this.flags.exponent=false;
}else{
this.flags.exponent=[true,false];
}
}
if((_95e.esigned=="true")||(_95e.esigned=="always")){
this.flags.eSigned=true;
}else{
if((_95e.esigned=="false")||(_95e.esigned=="never")){
this.flags.eSigned=false;
}else{
this.flags.eSigned=[true,false];
}
}
if(_95e.min){
this.flags.min=parseFloat(_95e.min);
}
if(_95e.max){
this.flags.max=parseFloat(_95e.max);
}
},isValid:function(){
return dojo.validate.isRealNumber(this.textbox.value,this.flags);
},isInRange:function(){
return dojo.validate.isInRange(this.textbox.value,this.flags);
}});
dojo.provide("dojo.date.common");
dojo.date.setDayOfYear=function(_960,_961){
_960.setMonth(0);
_960.setDate(_961);
return _960;
};
dojo.date.getDayOfYear=function(_962){
var _963=_962.getFullYear();
var _964=new Date(_963-1,11,31);
return Math.floor((_962.getTime()-_964.getTime())/86400000);
};
dojo.date.setWeekOfYear=function(_965,week,_967){
if(arguments.length==1){
_967=0;
}
dojo.unimplemented("dojo.date.setWeekOfYear");
};
dojo.date.getWeekOfYear=function(_968,_969){
if(arguments.length==1){
_969=0;
}
var _96a=new Date(_968.getFullYear(),0,1);
var day=_96a.getDay();
_96a.setDate(_96a.getDate()-day+_969-(day>_969?7:0));
return Math.floor((_968.getTime()-_96a.getTime())/604800000);
};
dojo.date.setIsoWeekOfYear=function(_96c,week,_96e){
if(arguments.length==1){
_96e=1;
}
dojo.unimplemented("dojo.date.setIsoWeekOfYear");
};
dojo.date.getIsoWeekOfYear=function(_96f,_970){
if(arguments.length==1){
_970=1;
}
dojo.unimplemented("dojo.date.getIsoWeekOfYear");
};
dojo.date.shortTimezones=["IDLW","BET","HST","MART","AKST","PST","MST","CST","EST","AST","NFT","BST","FST","AT","GMT","CET","EET","MSK","IRT","GST","AFT","AGTT","IST","NPT","ALMT","MMT","JT","AWST","JST","ACST","AEST","LHST","VUT","NFT","NZT","CHAST","PHOT","LINT"];
dojo.date.timezoneOffsets=[-720,-660,-600,-570,-540,-480,-420,-360,-300,-240,-210,-180,-120,-60,0,60,120,180,210,240,270,300,330,345,360,390,420,480,540,570,600,630,660,690,720,765,780,840];
dojo.date.getDaysInMonth=function(_971){
var _972=_971.getMonth();
var days=[31,28,31,30,31,30,31,31,30,31,30,31];
if(_972==1&&dojo.date.isLeapYear(_971)){
return 29;
}else{
return days[_972];
}
};
dojo.date.isLeapYear=function(_974){
var year=_974.getFullYear();
return (year%400==0)?true:(year%100==0)?false:(year%4==0)?true:false;
};
dojo.date.getTimezoneName=function(_976){
var str=_976.toString();
var tz="";
var _979;
var pos=str.indexOf("(");
if(pos>-1){
pos++;
tz=str.substring(pos,str.indexOf(")"));
}else{
var pat=/([A-Z\/]+) \d{4}$/;
if((_979=str.match(pat))){
tz=_979[1];
}else{
str=_976.toLocaleString();
pat=/ ([A-Z\/]+)$/;
if((_979=str.match(pat))){
tz=_979[1];
}
}
}
return tz=="AM"||tz=="PM"?"":tz;
};
dojo.date.getOrdinal=function(_97c){
var date=_97c.getDate();
if(date%100!=11&&date%10==1){
return "st";
}else{
if(date%100!=12&&date%10==2){
return "nd";
}else{
if(date%100!=13&&date%10==3){
return "rd";
}else{
return "th";
}
}
}
};
dojo.date.compareTypes={DATE:1,TIME:2};
dojo.date.compare=function(_97e,_97f,_980){
var dA=_97e;
var dB=_97f||new Date();
var now=new Date();
with(dojo.date.compareTypes){
var opt=_980||(DATE|TIME);
var d1=new Date((opt&DATE)?dA.getFullYear():now.getFullYear(),(opt&DATE)?dA.getMonth():now.getMonth(),(opt&DATE)?dA.getDate():now.getDate(),(opt&TIME)?dA.getHours():0,(opt&TIME)?dA.getMinutes():0,(opt&TIME)?dA.getSeconds():0);
var d2=new Date((opt&DATE)?dB.getFullYear():now.getFullYear(),(opt&DATE)?dB.getMonth():now.getMonth(),(opt&DATE)?dB.getDate():now.getDate(),(opt&TIME)?dB.getHours():0,(opt&TIME)?dB.getMinutes():0,(opt&TIME)?dB.getSeconds():0);
}
if(d1.valueOf()>d2.valueOf()){
return 1;
}
if(d1.valueOf()<d2.valueOf()){
return -1;
}
return 0;
};
dojo.date.dateParts={YEAR:0,MONTH:1,DAY:2,HOUR:3,MINUTE:4,SECOND:5,MILLISECOND:6,QUARTER:7,WEEK:8,WEEKDAY:9};
dojo.date.add=function(dt,_988,incr){
if(typeof dt=="number"){
dt=new Date(dt);
}
function fixOvershoot(){
if(sum.getDate()<dt.getDate()){
sum.setDate(0);
}
}
var sum=new Date(dt);
with(dojo.date.dateParts){
switch(_988){
case YEAR:
sum.setFullYear(dt.getFullYear()+incr);
fixOvershoot();
break;
case QUARTER:
incr*=3;
case MONTH:
sum.setMonth(dt.getMonth()+incr);
fixOvershoot();
break;
case WEEK:
incr*=7;
case DAY:
sum.setDate(dt.getDate()+incr);
break;
case WEEKDAY:
var dat=dt.getDate();
var _98c=0;
var days=0;
var strt=0;
var trgt=0;
var adj=0;
var mod=incr%5;
if(mod==0){
days=(incr>0)?5:-5;
_98c=(incr>0)?((incr-5)/5):((incr+5)/5);
}else{
days=mod;
_98c=parseInt(incr/5);
}
strt=dt.getDay();
if(strt==6&&incr>0){
adj=1;
}else{
if(strt==0&&incr<0){
adj=-1;
}
}
trgt=(strt+days);
if(trgt==0||trgt==6){
adj=(incr>0)?2:-2;
}
sum.setDate(dat+(7*_98c)+days+adj);
break;
case HOUR:
sum.setHours(sum.getHours()+incr);
break;
case MINUTE:
sum.setMinutes(sum.getMinutes()+incr);
break;
case SECOND:
sum.setSeconds(sum.getSeconds()+incr);
break;
case MILLISECOND:
sum.setMilliseconds(sum.getMilliseconds()+incr);
break;
default:
break;
}
}
return sum;
};
dojo.date.diff=function(dtA,dtB,_994){
if(typeof dtA=="number"){
dtA=new Date(dtA);
}
if(typeof dtB=="number"){
dtB=new Date(dtB);
}
var _995=dtB.getFullYear()-dtA.getFullYear();
var _996=(dtB.getMonth()-dtA.getMonth())+(_995*12);
var _997=dtB.getTime()-dtA.getTime();
var _998=_997/1000;
var _999=_998/60;
var _99a=_999/60;
var _99b=_99a/24;
var _99c=_99b/7;
var _99d=0;
with(dojo.date.dateParts){
switch(_994){
case YEAR:
_99d=_995;
break;
case QUARTER:
var mA=dtA.getMonth();
var mB=dtB.getMonth();
var qA=Math.floor(mA/3)+1;
var qB=Math.floor(mB/3)+1;
qB+=(_995*4);
_99d=qB-qA;
break;
case MONTH:
_99d=_996;
break;
case WEEK:
_99d=parseInt(_99c);
break;
case DAY:
_99d=_99b;
break;
case WEEKDAY:
var days=Math.round(_99b);
var _9a3=parseInt(days/7);
var mod=days%7;
if(mod==0){
days=_9a3*5;
}else{
var adj=0;
var aDay=dtA.getDay();
var bDay=dtB.getDay();
_9a3=parseInt(days/7);
mod=days%7;
var _9a8=new Date(dtA);
_9a8.setDate(_9a8.getDate()+(_9a3*7));
var _9a9=_9a8.getDay();
if(_99b>0){
switch(true){
case aDay==6:
adj=-1;
break;
case aDay==0:
adj=0;
break;
case bDay==6:
adj=-1;
break;
case bDay==0:
adj=-2;
break;
case (_9a9+mod)>5:
adj=-2;
break;
default:
break;
}
}else{
if(_99b<0){
switch(true){
case aDay==6:
adj=0;
break;
case aDay==0:
adj=1;
break;
case bDay==6:
adj=2;
break;
case bDay==0:
adj=1;
break;
case (_9a9+mod)<0:
adj=2;
break;
default:
break;
}
}
}
days+=adj;
days-=(_9a3*2);
}
_99d=days;
break;
case HOUR:
_99d=_99a;
break;
case MINUTE:
_99d=_999;
break;
case SECOND:
_99d=_998;
break;
case MILLISECOND:
_99d=_997;
break;
default:
break;
}
}
return Math.round(_99d);
};
dojo.provide("dojo.date.supplemental");
dojo.date.getFirstDayOfWeek=function(_9aa){
var _9ab={mv:5,ae:6,af:6,bh:6,dj:6,dz:6,eg:6,er:6,et:6,iq:6,ir:6,jo:6,ke:6,kw:6,lb:6,ly:6,ma:6,om:6,qa:6,sa:6,sd:6,so:6,tn:6,ye:6,as:0,au:0,az:0,bw:0,ca:0,cn:0,fo:0,ge:0,gl:0,gu:0,hk:0,ie:0,il:0,is:0,jm:0,jp:0,kg:0,kr:0,la:0,mh:0,mo:0,mp:0,mt:0,nz:0,ph:0,pk:0,sg:0,th:0,tt:0,tw:0,um:0,us:0,uz:0,vi:0,za:0,zw:0,et:0,mw:0,ng:0,tj:0,gb:0,sy:4};
_9aa=dojo.hostenv.normalizeLocale(_9aa);
var _9ac=_9aa.split("-")[1];
var dow=_9ab[_9ac];
return (typeof dow=="undefined")?1:dow;
};
dojo.date.getWeekend=function(_9ae){
var _9af={eg:5,il:5,sy:5,"in":0,ae:4,bh:4,dz:4,iq:4,jo:4,kw:4,lb:4,ly:4,ma:4,om:4,qa:4,sa:4,sd:4,tn:4,ye:4};
var _9b0={ae:5,bh:5,dz:5,iq:5,jo:5,kw:5,lb:5,ly:5,ma:5,om:5,qa:5,sa:5,sd:5,tn:5,ye:5,af:5,ir:5,eg:6,il:6,sy:6};
_9ae=dojo.hostenv.normalizeLocale(_9ae);
var _9b1=_9ae.split("-")[1];
var _9b2=_9af[_9b1];
var end=_9b0[_9b1];
if(typeof _9b2=="undefined"){
_9b2=6;
}
if(typeof end=="undefined"){
end=0;
}
return {start:_9b2,end:end};
};
dojo.date.isWeekend=function(_9b4,_9b5){
var _9b6=dojo.date.getWeekend(_9b5);
var day=(_9b4||new Date()).getDay();
if(_9b6.end<_9b6.start){
_9b6.end+=7;
if(day<_9b6.start){
day+=7;
}
}
return day>=_9b6.start&&day<=_9b6.end;
};
dojo.provide("dojo.date.format");
(function(){
dojo.date.format=function(_9b8,_9b9){
if(typeof _9b9=="string"){
dojo.deprecated("dojo.date.format","To format dates with POSIX-style strings, please use dojo.date.strftime instead","0.5");
return dojo.date.strftime(_9b8,_9b9);
}
function formatPattern(_9ba,_9bb){
return _9bb.replace(/([a-z])\1*/ig,function(_9bc){
var s;
var c=_9bc.charAt(0);
var l=_9bc.length;
var pad;
var _9c1=["abbr","wide","narrow"];
switch(c){
case "G":
if(l>3){
dojo.unimplemented("Era format not implemented");
}
s=info.eras[_9ba.getFullYear()<0?1:0];
break;
case "y":
s=_9ba.getFullYear();
switch(l){
case 1:
break;
case 2:
s=String(s).substr(-2);
break;
default:
pad=true;
}
break;
case "Q":
case "q":
s=Math.ceil((_9ba.getMonth()+1)/3);
switch(l){
case 1:
case 2:
pad=true;
break;
case 3:
case 4:
dojo.unimplemented("Quarter format not implemented");
}
break;
case "M":
case "L":
var m=_9ba.getMonth();
var _9c4;
switch(l){
case 1:
case 2:
s=m+1;
pad=true;
break;
case 3:
case 4:
case 5:
_9c4=_9c1[l-3];
break;
}
if(_9c4){
var type=(c=="L")?"standalone":"format";
var prop=["months",type,_9c4].join("-");
s=info[prop][m];
}
break;
case "w":
var _9c7=0;
s=dojo.date.getWeekOfYear(_9ba,_9c7);
pad=true;
break;
case "d":
s=_9ba.getDate();
pad=true;
break;
case "D":
s=dojo.date.getDayOfYear(_9ba);
pad=true;
break;
case "E":
case "e":
case "c":
var d=_9ba.getDay();
var _9c4;
switch(l){
case 1:
case 2:
if(c=="e"){
var _9c9=dojo.date.getFirstDayOfWeek(_9b9.locale);
d=(d-_9c9+7)%7;
}
if(c!="c"){
s=d+1;
pad=true;
break;
}
case 3:
case 4:
case 5:
_9c4=_9c1[l-3];
break;
}
if(_9c4){
var type=(c=="c")?"standalone":"format";
var prop=["days",type,_9c4].join("-");
s=info[prop][d];
}
break;
case "a":
var _9ca=(_9ba.getHours()<12)?"am":"pm";
s=info[_9ca];
break;
case "h":
case "H":
case "K":
case "k":
var h=_9ba.getHours();
switch(c){
case "h":
s=(h%12)||12;
break;
case "H":
s=h;
break;
case "K":
s=(h%12);
break;
case "k":
s=h||24;
break;
}
pad=true;
break;
case "m":
s=_9ba.getMinutes();
pad=true;
break;
case "s":
s=_9ba.getSeconds();
pad=true;
break;
case "S":
s=Math.round(_9ba.getMilliseconds()*Math.pow(10,l-3));
break;
case "v":
case "z":
s=dojo.date.getTimezoneName(_9ba);
if(s){
break;
}
l=4;
case "Z":
var _9cc=_9ba.getTimezoneOffset();
var tz=[(_9cc<=0?"+":"-"),dojo.string.pad(Math.floor(Math.abs(_9cc)/60),2),dojo.string.pad(Math.abs(_9cc)%60,2)];
if(l==4){
tz.splice(0,0,"GMT");
tz.splice(3,0,":");
}
s=tz.join("");
break;
case "Y":
case "u":
case "W":
case "F":
case "g":
case "A":
dojo.debug(_9bc+" modifier not yet implemented");
s="?";
break;
default:
dojo.raise("dojo.date.format: invalid pattern char: "+_9bb);
}
if(pad){
s=dojo.string.pad(s,l);
}
return s;
});
}
_9b9=_9b9||{};
var _9ce=dojo.hostenv.normalizeLocale(_9b9.locale);
var _9cf=_9b9.formatLength||"full";
var info=dojo.date._getGregorianBundle(_9ce);
var str=[];
var _9d1=dojo.lang.curry(this,formatPattern,_9b8);
if(_9b9.selector!="timeOnly"){
var _9d2=_9b9.datePattern||info["dateFormat-"+_9cf];
if(_9d2){
str.push(_processPattern(_9d2,_9d1));
}
}
if(_9b9.selector!="dateOnly"){
var _9d3=_9b9.timePattern||info["timeFormat-"+_9cf];
if(_9d3){
str.push(_processPattern(_9d3,_9d1));
}
}
var _9d4=str.join(" ");
return _9d4;
};
dojo.date.parse=function(_9d5,_9d6){
_9d6=_9d6||{};
var _9d7=dojo.hostenv.normalizeLocale(_9d6.locale);
var info=dojo.date._getGregorianBundle(_9d7);
var _9d9=_9d6.formatLength||"full";
if(!_9d6.selector){
_9d6.selector="dateOnly";
}
var _9da=_9d6.datePattern||info["dateFormat-"+_9d9];
var _9db=_9d6.timePattern||info["timeFormat-"+_9d9];
var _9dc;
if(_9d6.selector=="dateOnly"){
_9dc=_9da;
}else{
if(_9d6.selector=="timeOnly"){
_9dc=_9db;
}else{
if(_9d6.selector=="dateTime"){
_9dc=_9da+" "+_9db;
}else{
var msg="dojo.date.parse: Unknown selector param passed: '"+_9d6.selector+"'.";
msg+=" Defaulting to date pattern.";
dojo.debug(msg);
_9dc=_9da;
}
}
}
var _9de=[];
var _9df=_processPattern(_9dc,dojo.lang.curry(this,_buildDateTimeRE,_9de,info,_9d6));
var _9e0=new RegExp("^"+_9df+"$");
var _9e1=_9e0.exec(_9d5);
if(!_9e1){
return null;
}
var _9e2=["abbr","wide","narrow"];
var _9e3=new Date(1972,0);
var _9e4={};
for(var i=1;i<_9e1.length;i++){
var grp=_9de[i-1];
var l=grp.length;
var v=_9e1[i];
switch(grp.charAt(0)){
case "y":
if(l!=2){
_9e3.setFullYear(v);
_9e4.year=v;
}else{
if(v<100){
v=Number(v);
var year=""+new Date().getFullYear();
var _9ea=year.substring(0,2)*100;
var _9eb=Number(year.substring(2,4));
var _9ec=Math.min(_9eb+20,99);
var num=(v<_9ec)?_9ea+v:_9ea-100+v;
_9e3.setFullYear(num);
_9e4.year=num;
}else{
if(_9d6.strict){
return null;
}
_9e3.setFullYear(v);
_9e4.year=v;
}
}
break;
case "M":
if(l>2){
if(!_9d6.strict){
v=v.replace(/\./g,"");
v=v.toLowerCase();
}
var _9ee=info["months-format-"+_9e2[l-3]].concat();
for(var j=0;j<_9ee.length;j++){
if(!_9d6.strict){
_9ee[j]=_9ee[j].toLowerCase();
}
if(v==_9ee[j]){
_9e3.setMonth(j);
_9e4.month=j;
break;
}
}
if(j==_9ee.length){
dojo.debug("dojo.date.parse: Could not parse month name: '"+v+"'.");
return null;
}
}else{
_9e3.setMonth(v-1);
_9e4.month=v-1;
}
break;
case "E":
case "e":
if(!_9d6.strict){
v=v.toLowerCase();
}
var days=info["days-format-"+_9e2[l-3]].concat();
for(var j=0;j<days.length;j++){
if(!_9d6.strict){
days[j]=days[j].toLowerCase();
}
if(v==days[j]){
break;
}
}
if(j==days.length){
dojo.debug("dojo.date.parse: Could not parse weekday name: '"+v+"'.");
return null;
}
break;
case "d":
_9e3.setDate(v);
_9e4.date=v;
break;
case "a":
var am=_9d6.am||info.am;
var pm=_9d6.pm||info.pm;
if(!_9d6.strict){
v=v.replace(/\./g,"").toLowerCase();
am=am.replace(/\./g,"").toLowerCase();
pm=pm.replace(/\./g,"").toLowerCase();
}
if(_9d6.strict&&v!=am&&v!=pm){
dojo.debug("dojo.date.parse: Could not parse am/pm part.");
return null;
}
var _9f3=_9e3.getHours();
if(v==pm&&_9f3<12){
_9e3.setHours(_9f3+12);
}else{
if(v==am&&_9f3==12){
_9e3.setHours(0);
}
}
break;
case "K":
if(v==24){
v=0;
}
case "h":
case "H":
case "k":
if(v>23){
dojo.debug("dojo.date.parse: Illegal hours value");
return null;
}
_9e3.setHours(v);
break;
case "m":
_9e3.setMinutes(v);
break;
case "s":
_9e3.setSeconds(v);
break;
case "S":
_9e3.setMilliseconds(v);
break;
default:
dojo.unimplemented("dojo.date.parse: unsupported pattern char="+grp.charAt(0));
}
}
if(_9e4.year&&_9e3.getFullYear()!=_9e4.year){
dojo.debug("Parsed year: '"+_9e3.getFullYear()+"' did not match input year: '"+_9e4.year+"'.");
return null;
}
if(_9e4.month&&_9e3.getMonth()!=_9e4.month){
dojo.debug("Parsed month: '"+_9e3.getMonth()+"' did not match input month: '"+_9e4.month+"'.");
return null;
}
if(_9e4.date&&_9e3.getDate()!=_9e4.date){
dojo.debug("Parsed day of month: '"+_9e3.getDate()+"' did not match input day of month: '"+_9e4.date+"'.");
return null;
}
return _9e3;
};
function _processPattern(_9f4,_9f5,_9f6,_9f7){
var _9f8=function(x){
return x;
};
_9f5=_9f5||_9f8;
_9f6=_9f6||_9f8;
_9f7=_9f7||_9f8;
var _9fa=_9f4.match(/(''|[^'])+/g);
var _9fb=false;
for(var i=0;i<_9fa.length;i++){
if(!_9fa[i]){
_9fa[i]="";
}else{
_9fa[i]=(_9fb?_9f6:_9f5)(_9fa[i]);
_9fb=!_9fb;
}
}
return _9f7(_9fa.join(""));
}
function _buildDateTimeRE(_9fd,info,_9ff,_a00){
return _a00.replace(/([a-z])\1*/ig,function(_a01){
var s;
var c=_a01.charAt(0);
var l=_a01.length;
switch(c){
case "y":
s="\\d"+((l==2)?"{2,4}":"+");
break;
case "M":
s=(l>2)?"\\S+":"\\d{1,2}";
break;
case "d":
s="\\d{1,2}";
break;
case "E":
s="\\S+";
break;
case "h":
case "H":
case "K":
case "k":
s="\\d{1,2}";
break;
case "m":
case "s":
s="[0-5]\\d";
break;
case "S":
s="\\d{1,3}";
break;
case "a":
var am=_9ff.am||info.am||"AM";
var pm=_9ff.pm||info.pm||"PM";
if(_9ff.strict){
s=am+"|"+pm;
}else{
s=am;
s+=(am!=am.toLowerCase())?"|"+am.toLowerCase():"";
s+="|";
s+=(pm!=pm.toLowerCase())?pm+"|"+pm.toLowerCase():pm;
}
break;
default:
dojo.unimplemented("parse of date format, pattern="+_a00);
}
if(_9fd){
_9fd.push(_a01);
}
return "\\s*("+s+")\\s*";
});
}
})();
dojo.date.strftime=function(_a07,_a08,_a09){
var _a0a=null;
function _(s,n){
return dojo.string.pad(s,n||2,_a0a||"0");
}
var info=dojo.date._getGregorianBundle(_a09);
function $(_a0e){
switch(_a0e){
case "a":
return dojo.date.getDayShortName(_a07,_a09);
case "A":
return dojo.date.getDayName(_a07,_a09);
case "b":
case "h":
return dojo.date.getMonthShortName(_a07,_a09);
case "B":
return dojo.date.getMonthName(_a07,_a09);
case "c":
return dojo.date.format(_a07,{locale:_a09});
case "C":
return _(Math.floor(_a07.getFullYear()/100));
case "d":
return _(_a07.getDate());
case "D":
return $("m")+"/"+$("d")+"/"+$("y");
case "e":
if(_a0a==null){
_a0a=" ";
}
return _(_a07.getDate());
case "f":
if(_a0a==null){
_a0a=" ";
}
return _(_a07.getMonth()+1);
case "g":
break;
case "G":
dojo.unimplemented("unimplemented modifier 'G'");
break;
case "F":
return $("Y")+"-"+$("m")+"-"+$("d");
case "H":
return _(_a07.getHours());
case "I":
return _(_a07.getHours()%12||12);
case "j":
return _(dojo.date.getDayOfYear(_a07),3);
case "k":
if(_a0a==null){
_a0a=" ";
}
return _(_a07.getHours());
case "l":
if(_a0a==null){
_a0a=" ";
}
return _(_a07.getHours()%12||12);
case "m":
return _(_a07.getMonth()+1);
case "M":
return _(_a07.getMinutes());
case "n":
return "\n";
case "p":
return info[_a07.getHours()<12?"am":"pm"];
case "r":
return $("I")+":"+$("M")+":"+$("S")+" "+$("p");
case "R":
return $("H")+":"+$("M");
case "S":
return _(_a07.getSeconds());
case "t":
return "\t";
case "T":
return $("H")+":"+$("M")+":"+$("S");
case "u":
return String(_a07.getDay()||7);
case "U":
return _(dojo.date.getWeekOfYear(_a07));
case "V":
return _(dojo.date.getIsoWeekOfYear(_a07));
case "W":
return _(dojo.date.getWeekOfYear(_a07,1));
case "w":
return String(_a07.getDay());
case "x":
return dojo.date.format(_a07,{selector:"dateOnly",locale:_a09});
case "X":
return dojo.date.format(_a07,{selector:"timeOnly",locale:_a09});
case "y":
return _(_a07.getFullYear()%100);
case "Y":
return String(_a07.getFullYear());
case "z":
var _a0f=_a07.getTimezoneOffset();
return (_a0f>0?"-":"+")+_(Math.floor(Math.abs(_a0f)/60))+":"+_(Math.abs(_a0f)%60);
case "Z":
return dojo.date.getTimezoneName(_a07);
case "%":
return "%";
}
}
var _a10="";
var i=0;
var _a12=0;
var _a13=null;
while((_a12=_a08.indexOf("%",i))!=-1){
_a10+=_a08.substring(i,_a12++);
switch(_a08.charAt(_a12++)){
case "_":
_a0a=" ";
break;
case "-":
_a0a="";
break;
case "0":
_a0a="0";
break;
case "^":
_a13="upper";
break;
case "*":
_a13="lower";
break;
case "#":
_a13="swap";
break;
default:
_a0a=null;
_a12--;
break;
}
var _a14=$(_a08.charAt(_a12++));
switch(_a13){
case "upper":
_a14=_a14.toUpperCase();
break;
case "lower":
_a14=_a14.toLowerCase();
break;
case "swap":
var _a15=_a14.toLowerCase();
var _a16="";
var j=0;
var ch="";
while(j<_a14.length){
ch=_a14.charAt(j);
_a16+=(ch==_a15.charAt(j))?ch.toUpperCase():ch.toLowerCase();
j++;
}
_a14=_a16;
break;
default:
break;
}
_a13=null;
_a10+=_a14;
i=_a12;
}
_a10+=_a08.substring(i);
return _a10;
};
(function(){
var _a19=[];
dojo.date.addCustomFormats=function(_a1a,_a1b){
_a19.push({pkg:_a1a,name:_a1b});
};
dojo.date._getGregorianBundle=function(_a1c){
var _a1d={};
dojo.lang.forEach(_a19,function(desc){
var _a1f=dojo.i18n.getLocalization(desc.pkg,desc.name,_a1c);
_a1d=dojo.lang.mixin(_a1d,_a1f);
},this);
return _a1d;
};
})();
dojo.date.addCustomFormats("dojo.i18n.calendar","gregorian");
dojo.date.addCustomFormats("dojo.i18n.calendar","gregorianExtras");
dojo.date.getNames=function(item,type,use,_a23){
var _a24;
var _a25=dojo.date._getGregorianBundle(_a23);
var _a26=[item,use,type];
if(use=="standAlone"){
_a24=_a25[_a26.join("-")];
}
_a26[1]="format";
return (_a24||_a25[_a26.join("-")]).concat();
};
dojo.date.getDayName=function(_a27,_a28){
return dojo.date.getNames("days","wide","format",_a28)[_a27.getDay()];
};
dojo.date.getDayShortName=function(_a29,_a2a){
return dojo.date.getNames("days","abbr","format",_a2a)[_a29.getDay()];
};
dojo.date.getMonthName=function(_a2b,_a2c){
return dojo.date.getNames("months","wide","format",_a2c)[_a2b.getMonth()];
};
dojo.date.getMonthShortName=function(_a2d,_a2e){
return dojo.date.getNames("months","abbr","format",_a2e)[_a2d.getMonth()];
};
dojo.date.toRelativeString=function(_a2f){
var now=new Date();
var diff=(now-_a2f)/1000;
var end=" ago";
var _a33=false;
if(diff<0){
_a33=true;
end=" from now";
diff=-diff;
}
if(diff<60){
diff=Math.round(diff);
return diff+" second"+(diff==1?"":"s")+end;
}
if(diff<60*60){
diff=Math.round(diff/60);
return diff+" minute"+(diff==1?"":"s")+end;
}
if(diff<60*60*24){
diff=Math.round(diff/3600);
return diff+" hour"+(diff==1?"":"s")+end;
}
if(diff<60*60*24*7){
diff=Math.round(diff/(3600*24));
if(diff==1){
return _a33?"Tomorrow":"Yesterday";
}else{
return diff+" days"+end;
}
}
return dojo.date.format(_a2f);
};
dojo.date.toSql=function(_a34,_a35){
return dojo.date.strftime(_a34,"%F"+!_a35?" %T":"");
};
dojo.date.fromSql=function(_a36){
var _a37=_a36.split(/[\- :]/g);
while(_a37.length<6){
_a37.push(0);
}
return new Date(_a37[0],(parseInt(_a37[1],10)-1),_a37[2],_a37[3],_a37[4],_a37[5]);
};
dojo.provide("dojo.validate.datetime");
dojo.validate.isValidTime=function(_a38,_a39){
dojo.deprecated("dojo.validate.datetime","use dojo.date.parse instead","0.5");
var re=new RegExp("^"+dojo.regexp.time(_a39)+"$","i");
return re.test(_a38);
};
dojo.validate.is12HourTime=function(_a3b){
dojo.deprecated("dojo.validate.datetime","use dojo.date.parse instead","0.5");
return dojo.validate.isValidTime(_a3b,{format:["h:mm:ss t","h:mm t"]});
};
dojo.validate.is24HourTime=function(_a3c){
dojo.deprecated("dojo.validate.datetime","use dojo.date.parse instead","0.5");
return dojo.validate.isValidTime(_a3c,{format:["HH:mm:ss","HH:mm"]});
};
dojo.validate.isValidDate=function(_a3d,_a3e){
dojo.deprecated("dojo.validate.datetime","use dojo.date.parse instead","0.5");
if(typeof _a3e=="object"&&typeof _a3e.format=="string"){
_a3e=_a3e.format;
}
if(typeof _a3e!="string"){
_a3e="MM/DD/YYYY";
}
var _a3f=_a3e.replace(/([$^.*+?=!:|\/\\\(\)\[\]\{\}])/g,"\\$1");
_a3f=_a3f.replace("YYYY","([0-9]{4})");
_a3f=_a3f.replace("MM","(0[1-9]|10|11|12)");
_a3f=_a3f.replace("M","([1-9]|10|11|12)");
_a3f=_a3f.replace("DDD","(00[1-9]|0[1-9][0-9]|[12][0-9][0-9]|3[0-5][0-9]|36[0-6])");
_a3f=_a3f.replace("DD","(0[1-9]|[12][0-9]|30|31)");
_a3f=_a3f.replace("D","([1-9]|[12][0-9]|30|31)");
_a3f=_a3f.replace("ww","(0[1-9]|[1-4][0-9]|5[0-3])");
_a3f=_a3f.replace("d","([1-7])");
_a3f="^"+_a3f+"$";
var re=new RegExp(_a3f);
if(!re.test(_a3d)){
return false;
}
var year=0,_a42=1,date=1,_a44=1,week=1,day=1;
var _a47=_a3e.match(/(YYYY|MM|M|DDD|DD|D|ww|d)/g);
var _a48=re.exec(_a3d);
for(var i=0;i<_a47.length;i++){
switch(_a47[i]){
case "YYYY":
year=Number(_a48[i+1]);
break;
case "M":
case "MM":
_a42=Number(_a48[i+1]);
break;
case "D":
case "DD":
date=Number(_a48[i+1]);
break;
case "DDD":
_a44=Number(_a48[i+1]);
break;
case "ww":
week=Number(_a48[i+1]);
break;
case "d":
day=Number(_a48[i+1]);
break;
}
}
var _a4a=(year%4==0&&(year%100!=0||year%400==0));
if(date==31&&(_a42==4||_a42==6||_a42==9||_a42==11)){
return false;
}
if(date>=30&&_a42==2){
return false;
}
if(date==29&&_a42==2&&!_a4a){
return false;
}
if(_a44==366&&!_a4a){
return false;
}
return true;
};
dojo.provide("dojo.widget.DateTextbox");
dojo.widget.defineWidget("dojo.widget.DateTextbox",dojo.widget.ValidationTextbox,{displayFormat:"",formatLength:"short",mixInProperties:function(_a4b){
dojo.widget.DateTextbox.superclass.mixInProperties.apply(this,arguments);
if(_a4b.format){
this.flags.format=_a4b.format;
}
},isValid:function(){
if(this.flags.format){
dojo.deprecated("dojo.widget.DateTextbox","format attribute is deprecated; use displayFormat or formatLength instead","0.5");
return dojo.validate.isValidDate(this.textbox.value,this.flags.format);
}
return dojo.date.parse(this.textbox.value,{formatLength:this.formatLength,selector:"dateOnly",locale:this.lang,datePattern:this.displayFormat});
}});
dojo.widget.defineWidget("dojo.widget.TimeTextbox",dojo.widget.ValidationTextbox,{displayFormat:"",formatLength:"short",mixInProperties:function(_a4c){
dojo.widget.TimeTextbox.superclass.mixInProperties.apply(this,arguments);
if(_a4c.format){
this.flags.format=_a4c.format;
}
if(_a4c.amsymbol){
this.flags.amSymbol=_a4c.amsymbol;
}
if(_a4c.pmsymbol){
this.flags.pmSymbol=_a4c.pmsymbol;
}
},isValid:function(){
if(this.flags.format){
dojo.deprecated("dojo.widget.TimeTextbox","format attribute is deprecated; use displayFormat or formatLength instead","0.5");
return dojo.validate.isValidTime(this.textbox.value,this.flags);
}
return dojo.date.parse(this.textbox.value,{formatLength:this.formatLength,selector:"timeOnly",locale:this.lang,timePattern:this.displayFormat});
}});
if(!this["dojo"]){
alert("\"dojo/__package__.js\" is now located at \"dojo/dojo.js\". Please update your includes accordingly");
}
dojo.provide("dojo.html.iframe");
dojo.html.iframeContentWindow=function(_a4d){
var win=dojo.html.getDocumentWindow(dojo.html.iframeContentDocument(_a4d))||dojo.html.iframeContentDocument(_a4d).__parent__||(_a4d.name&&document.frames[_a4d.name])||null;
return win;
};
dojo.html.iframeContentDocument=function(_a4f){
var doc=_a4f.contentDocument||((_a4f.contentWindow)&&(_a4f.contentWindow.document))||((_a4f.name)&&(document.frames[_a4f.name])&&(document.frames[_a4f.name].document))||null;
return doc;
};
dojo.html.BackgroundIframe=function(node){
if(dojo.render.html.ie55||dojo.render.html.ie60){
var html="<iframe src='javascript:false'"+" style='position: absolute; left: 0px; top: 0px; width: 100%; height: 100%;"+"z-index: -1; filter:Alpha(Opacity=\"0\");' "+">";
this.iframe=dojo.doc().createElement(html);
this.iframe.tabIndex=-1;
if(node){
node.appendChild(this.iframe);
this.domNode=node;
}else{
dojo.body().appendChild(this.iframe);
this.iframe.style.display="none";
}
}
};
dojo.lang.extend(dojo.html.BackgroundIframe,{iframe:null,onResized:function(){
if(this.iframe&&this.domNode&&this.domNode.parentNode){
var _a53=dojo.html.getMarginBox(this.domNode);
if(_a53.width==0||_a53.height==0){
dojo.lang.setTimeout(this,this.onResized,100);
return;
}
this.iframe.style.width=_a53.width+"px";
this.iframe.style.height=_a53.height+"px";
}
},size:function(node){
if(!this.iframe){
return;
}
var _a55=dojo.html.toCoordinateObject(node,true,dojo.html.boxSizing.BORDER_BOX);
with(this.iframe.style){
width=_a55.width+"px";
height=_a55.height+"px";
left=_a55.left+"px";
top=_a55.top+"px";
}
},setZIndex:function(node){
if(!this.iframe){
return;
}
if(dojo.dom.isNode(node)){
this.iframe.style.zIndex=dojo.html.getStyle(node,"z-index")-1;
}else{
if(!isNaN(node)){
this.iframe.style.zIndex=node;
}
}
},show:function(){
if(this.iframe){
this.iframe.style.display="block";
}
},hide:function(){
if(this.iframe){
this.iframe.style.display="none";
}
},remove:function(){
if(this.iframe){
dojo.html.removeNode(this.iframe,true);
delete this.iframe;
this.iframe=null;
}
}});
dojo.provide("dojo.widget.PopupContainer");
dojo.declare("dojo.widget.PopupContainerBase",null,function(){
this.queueOnAnimationFinish=[];
},{isShowingNow:false,currentSubpopup:null,beginZIndex:1000,parentPopup:null,parent:null,popupIndex:0,aroundBox:dojo.html.boxSizing.BORDER_BOX,openedForWindow:null,processKey:function(evt){
return false;
},applyPopupBasicStyle:function(){
with(this.domNode.style){
display="none";
position="absolute";
}
},aboutToShow:function(){
},open:function(x,y,_a5a,_a5b,_a5c,_a5d){
if(this.isShowingNow){
return;
}
if(this.animationInProgress){
this.queueOnAnimationFinish.push(this.open,arguments);
return;
}
this.aboutToShow();
var _a5e=false,node,_a60;
if(typeof x=="object"){
node=x;
_a60=_a5b;
_a5b=_a5a;
_a5a=y;
_a5e=true;
}
this.parent=_a5a;
dojo.body().appendChild(this.domNode);
_a5b=_a5b||_a5a["domNode"]||[];
var _a61=null;
this.isTopLevel=true;
while(_a5a){
if(_a5a!==this&&(_a5a.setOpenedSubpopup!=undefined&&_a5a.applyPopupBasicStyle!=undefined)){
_a61=_a5a;
this.isTopLevel=false;
_a61.setOpenedSubpopup(this);
break;
}
_a5a=_a5a.parent;
}
this.parentPopup=_a61;
this.popupIndex=_a61?_a61.popupIndex+1:1;
if(this.isTopLevel){
var _a62=dojo.html.isNode(_a5b)?_a5b:null;
dojo.widget.PopupManager.opened(this,_a62);
}
if(this.isTopLevel&&!dojo.withGlobal(this.openedForWindow||dojo.global(),dojo.html.selection.isCollapsed)){
this._bookmark=dojo.withGlobal(this.openedForWindow||dojo.global(),dojo.html.selection.getBookmark);
}else{
this._bookmark=null;
}
if(_a5b instanceof Array){
_a5b={left:_a5b[0],top:_a5b[1],width:0,height:0};
}
with(this.domNode.style){
display="";
zIndex=this.beginZIndex+this.popupIndex;
}
if(_a5e){
this.move(node,_a5d,_a60);
}else{
this.move(x,y,_a5d,_a5c);
}
this.domNode.style.display="none";
this.explodeSrc=_a5b;
this.show();
this.isShowingNow=true;
},move:function(x,y,_a65,_a66){
var _a67=(typeof x=="object");
if(_a67){
var _a68=_a65;
var node=x;
_a65=y;
if(!_a68){
_a68={"BL":"TL","TL":"BL"};
}
dojo.html.placeOnScreenAroundElement(this.domNode,node,_a65,this.aroundBox,_a68);
}else{
if(!_a66){
_a66="TL,TR,BL,BR";
}
dojo.html.placeOnScreen(this.domNode,x,y,_a65,true,_a66);
}
},close:function(_a6a){
if(_a6a){
this.domNode.style.display="none";
}
if(this.animationInProgress){
this.queueOnAnimationFinish.push(this.close,[]);
return;
}
this.closeSubpopup(_a6a);
this.hide();
if(this.bgIframe){
this.bgIframe.hide();
this.bgIframe.size({left:0,top:0,width:0,height:0});
}
if(this.isTopLevel){
dojo.widget.PopupManager.closed(this);
}
this.isShowingNow=false;
if(this.parent){
setTimeout(dojo.lang.hitch(this,function(){
try{
if(this.parent["focus"]){
this.parent.focus();
}else{
this.parent.domNode.focus();
}
}
catch(e){
dojo.debug("No idea how to focus to parent",e);
}
}),10);
}
if(this._bookmark&&dojo.withGlobal(this.openedForWindow||dojo.global(),dojo.html.selection.isCollapsed)){
if(this.openedForWindow){
this.openedForWindow.focus();
}
try{
dojo.withGlobal(this.openedForWindow||dojo.global(),"moveToBookmark",dojo.html.selection,[this._bookmark]);
}
catch(e){
}
}
this._bookmark=null;
},closeAll:function(_a6b){
if(this.parentPopup){
this.parentPopup.closeAll(_a6b);
}else{
this.close(_a6b);
}
},setOpenedSubpopup:function(_a6c){
this.currentSubpopup=_a6c;
},closeSubpopup:function(_a6d){
if(this.currentSubpopup==null){
return;
}
this.currentSubpopup.close(_a6d);
this.currentSubpopup=null;
},onShow:function(){
dojo.widget.PopupContainer.superclass.onShow.apply(this,arguments);
this.openedSize={w:this.domNode.style.width,h:this.domNode.style.height};
if(dojo.render.html.ie){
if(!this.bgIframe){
this.bgIframe=new dojo.html.BackgroundIframe();
this.bgIframe.setZIndex(this.domNode);
}
this.bgIframe.size(this.domNode);
this.bgIframe.show();
}
this.processQueue();
},processQueue:function(){
if(!this.queueOnAnimationFinish.length){
return;
}
var func=this.queueOnAnimationFinish.shift();
var args=this.queueOnAnimationFinish.shift();
func.apply(this,args);
},onHide:function(){
dojo.widget.HtmlWidget.prototype.onHide.call(this);
if(this.openedSize){
with(this.domNode.style){
width=this.openedSize.w;
height=this.openedSize.h;
}
}
this.processQueue();
}});
dojo.widget.defineWidget("dojo.widget.PopupContainer",[dojo.widget.HtmlWidget,dojo.widget.PopupContainerBase],{isContainer:true,fillInTemplate:function(){
this.applyPopupBasicStyle();
dojo.widget.PopupContainer.superclass.fillInTemplate.apply(this,arguments);
}});
dojo.widget.PopupManager=new function(){
this.currentMenu=null;
this.currentButton=null;
this.currentFocusMenu=null;
this.focusNode=null;
this.registeredWindows=[];
this.registerWin=function(win){
if(!win.__PopupManagerRegistered){
dojo.event.connect(win.document,"onmousedown",this,"onClick");
dojo.event.connect(win,"onscroll",this,"onClick");
dojo.event.connect(win.document,"onkey",this,"onKey");
win.__PopupManagerRegistered=true;
this.registeredWindows.push(win);
}
};
this.registerAllWindows=function(_a71){
if(!_a71){
_a71=dojo.html.getDocumentWindow(window.top&&window.top.document||window.document);
}
this.registerWin(_a71);
for(var i=0;i<_a71.frames.length;i++){
try{
var win=dojo.html.getDocumentWindow(_a71.frames[i].document);
if(win){
this.registerAllWindows(win);
}
}
catch(e){
}
}
};
this.unRegisterWin=function(win){
if(win.__PopupManagerRegistered){
dojo.event.disconnect(win.document,"onmousedown",this,"onClick");
dojo.event.disconnect(win,"onscroll",this,"onClick");
dojo.event.disconnect(win.document,"onkey",this,"onKey");
win.__PopupManagerRegistered=false;
}
};
this.unRegisterAllWindows=function(){
for(var i=0;i<this.registeredWindows.length;++i){
this.unRegisterWin(this.registeredWindows[i]);
}
this.registeredWindows=[];
};
dojo.addOnLoad(this,"registerAllWindows");
dojo.addOnUnload(this,"unRegisterAllWindows");
this.closed=function(menu){
if(this.currentMenu==menu){
this.currentMenu=null;
this.currentButton=null;
this.currentFocusMenu=null;
}
};
this.opened=function(menu,_a78){
if(menu==this.currentMenu){
return;
}
if(this.currentMenu){
this.currentMenu.close();
}
this.currentMenu=menu;
this.currentFocusMenu=menu;
this.currentButton=_a78;
};
this.setFocusedMenu=function(menu){
this.currentFocusMenu=menu;
};
this.onKey=function(e){
if(!e.key){
return;
}
if(!this.currentMenu||!this.currentMenu.isShowingNow){
return;
}
var m=this.currentFocusMenu;
while(m){
if(m.processKey(e)){
e.preventDefault();
e.stopPropagation();
break;
}
m=m.parentPopup||m.parentMenu;
}
},this.onClick=function(e){
if(!this.currentMenu){
return;
}
var _a7d=dojo.html.getScroll().offset;
var m=this.currentMenu;
while(m){
if(dojo.html.overElement(m.domNode,e)||dojo.html.isDescendantOf(e.target,m.domNode)){
return;
}
m=m.currentSubpopup;
}
if(this.currentButton&&dojo.html.overElement(this.currentButton,e)){
return;
}
this.currentMenu.closeAll(true);
};
};
dojo.provide("dojo.widget.DropdownContainer");
dojo.widget.defineWidget("dojo.widget.DropdownContainer",dojo.widget.HtmlWidget,{inputWidth:"7em",id:"",inputId:"",inputName:"",iconURL:dojo.uri.moduleUri("dojo.widget","templates/images/combo_box_arrow.png"),copyClasses:false,iconAlt:"",containerToggle:"plain",containerToggleDuration:150,templateString:"<span style=\"white-space:nowrap\"><input type=\"hidden\" name=\"\" value=\"\" dojoAttachPoint=\"valueNode\" /><input name=\"\" type=\"text\" value=\"\" style=\"vertical-align:middle;\" dojoAttachPoint=\"inputNode\" autocomplete=\"off\" /> <img src=\"${this.iconURL}\" alt=\"${this.iconAlt}\" dojoAttachEvent=\"onclick:onIconClick\" dojoAttachPoint=\"buttonNode\" style=\"vertical-align:middle; cursor:pointer; cursor:hand\" /></span>",templateCssPath:"",isContainer:true,attachTemplateNodes:function(){
dojo.widget.DropdownContainer.superclass.attachTemplateNodes.apply(this,arguments);
this.popup=dojo.widget.createWidget("PopupContainer",{toggle:this.containerToggle,toggleDuration:this.containerToggleDuration});
this.containerNode=this.popup.domNode;
},fillInTemplate:function(args,frag){
this.domNode.appendChild(this.popup.domNode);
if(this.id){
this.domNode.id=this.id;
}
if(this.inputId){
this.inputNode.id=this.inputId;
}
if(this.inputName){
this.inputNode.name=this.inputName;
}
this.inputNode.style.width=this.inputWidth;
this.inputNode.disabled=this.disabled;
if(this.copyClasses){
this.inputNode.style="";
this.inputNode.className=this.getFragNodeRef(frag).className;
}
dojo.event.connect(this.inputNode,"onchange",this,"onInputChange");
},onIconClick:function(evt){
if(this.disabled){
return;
}
if(!this.popup.isShowingNow){
this.popup.open(this.inputNode,this,this.buttonNode);
}else{
this.popup.close();
}
},hideContainer:function(){
if(this.popup.isShowingNow){
this.popup.close();
}
},onInputChange:function(){
},enable:function(){
this.inputNode.disabled=false;
dojo.widget.DropdownContainer.superclass.enable.apply(this,arguments);
},disable:function(){
this.inputNode.disabled=true;
dojo.widget.DropdownContainer.superclass.disable.apply(this,arguments);
}});
dojo.provide("dojo.date.serialize");
dojo.date.setIso8601=function(_a82,_a83){
var _a84=(_a83.indexOf("T")==-1)?_a83.split(" "):_a83.split("T");
_a82=dojo.date.setIso8601Date(_a82,_a84[0]);
if(_a84.length==2){
_a82=dojo.date.setIso8601Time(_a82,_a84[1]);
}
return _a82;
};
dojo.date.fromIso8601=function(_a85){
return dojo.date.setIso8601(new Date(0,0),_a85);
};
dojo.date.setIso8601Date=function(_a86,_a87){
var _a88="^([0-9]{4})((-?([0-9]{2})(-?([0-9]{2}))?)|"+"(-?([0-9]{3}))|(-?W([0-9]{2})(-?([1-7]))?))?$";
var d=_a87.match(new RegExp(_a88));
if(!d){
dojo.debug("invalid date string: "+_a87);
return null;
}
var year=d[1];
var _a8b=d[4];
var date=d[6];
var _a8d=d[8];
var week=d[10];
var _a8f=d[12]?d[12]:1;
_a86.setFullYear(year);
if(_a8d){
_a86.setMonth(0);
_a86.setDate(Number(_a8d));
}else{
if(week){
_a86.setMonth(0);
_a86.setDate(1);
var gd=_a86.getDay();
var day=gd?gd:7;
var _a92=Number(_a8f)+(7*Number(week));
if(day<=4){
_a86.setDate(_a92+1-day);
}else{
_a86.setDate(_a92+8-day);
}
}else{
if(_a8b){
_a86.setDate(1);
_a86.setMonth(_a8b-1);
}
if(date){
_a86.setDate(date);
}
}
}
return _a86;
};
dojo.date.fromIso8601Date=function(_a93){
return dojo.date.setIso8601Date(new Date(0,0),_a93);
};
dojo.date.setIso8601Time=function(_a94,_a95){
var _a96="Z|(([-+])([0-9]{2})(:?([0-9]{2}))?)$";
var d=_a95.match(new RegExp(_a96));
var _a98=0;
if(d){
if(d[0]!="Z"){
_a98=(Number(d[3])*60)+Number(d[5]);
_a98*=((d[2]=="-")?1:-1);
}
_a98-=_a94.getTimezoneOffset();
_a95=_a95.substr(0,_a95.length-d[0].length);
}
var _a99="^([0-9]{2})(:?([0-9]{2})(:?([0-9]{2})(.([0-9]+))?)?)?$";
d=_a95.match(new RegExp(_a99));
if(!d){
dojo.debug("invalid time string: "+_a95);
return null;
}
var _a9a=d[1];
var mins=Number((d[3])?d[3]:0);
var secs=(d[5])?d[5]:0;
var ms=d[7]?(Number("0."+d[7])*1000):0;
_a94.setHours(_a9a);
_a94.setMinutes(mins);
_a94.setSeconds(secs);
_a94.setMilliseconds(ms);
if(_a98!==0){
_a94.setTime(_a94.getTime()+_a98*60000);
}
return _a94;
};
dojo.date.fromIso8601Time=function(_a9e){
return dojo.date.setIso8601Time(new Date(0,0),_a9e);
};
dojo.date.toRfc3339=function(_a9f,_aa0){
if(!_a9f){
_a9f=new Date();
}
var _=dojo.string.pad;
var _aa2=[];
if(_aa0!="timeOnly"){
var date=[_(_a9f.getFullYear(),4),_(_a9f.getMonth()+1,2),_(_a9f.getDate(),2)].join("-");
_aa2.push(date);
}
if(_aa0!="dateOnly"){
var time=[_(_a9f.getHours(),2),_(_a9f.getMinutes(),2),_(_a9f.getSeconds(),2)].join(":");
var _aa5=_a9f.getTimezoneOffset();
time+=(_aa5>0?"-":"+")+_(Math.floor(Math.abs(_aa5)/60),2)+":"+_(Math.abs(_aa5)%60,2);
_aa2.push(time);
}
return _aa2.join("T");
};
dojo.date.fromRfc3339=function(_aa6){
if(_aa6.indexOf("Tany")!=-1){
_aa6=_aa6.replace("Tany","");
}
var _aa7=new Date();
return dojo.date.setIso8601(_aa7,_aa6);
};
dojo.provide("dojo.widget.DatePicker");
dojo.widget.defineWidget("dojo.widget.DatePicker",dojo.widget.HtmlWidget,{value:"",name:"",displayWeeks:6,adjustWeeks:false,startDate:"1492-10-12",endDate:"2941-10-12",weekStartsOn:"",staticDisplay:false,dayWidth:"narrow",classNames:{previous:"previousMonth",disabledPrevious:"previousMonthDisabled",current:"currentMonth",disabledCurrent:"currentMonthDisabled",next:"nextMonth",disabledNext:"nextMonthDisabled",currentDate:"currentDate",selectedDate:"selectedDate"},templatePath:dojo.uri.moduleUri("dojo.widget","templates/DatePicker.html"),templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/DatePicker.css"),postMixInProperties:function(){
dojo.widget.DatePicker.superclass.postMixInProperties.apply(this,arguments);
if(!this.weekStartsOn){
this.weekStartsOn=dojo.date.getFirstDayOfWeek(this.lang);
}
this.today=new Date();
this.today.setHours(0,0,0,0);
if(typeof (this.value)=="string"&&this.value.toLowerCase()=="today"){
this.value=new Date();
}else{
if(this.value&&(typeof this.value=="string")&&(this.value.split("-").length>2)){
this.value=dojo.date.fromRfc3339(this.value);
this.value.setHours(0,0,0,0);
}
}
},fillInTemplate:function(args,frag){
dojo.widget.DatePicker.superclass.fillInTemplate.apply(this,arguments);
var _aaa=this.getFragNodeRef(frag);
dojo.html.copyStyle(this.domNode,_aaa);
this.weekTemplate=dojo.dom.removeNode(this.calendarWeekTemplate);
this._preInitUI(this.value?this.value:this.today,false,true);
var _aab=dojo.lang.unnest(dojo.date.getNames("days",this.dayWidth,"standAlone",this.lang));
if(this.weekStartsOn>0){
for(var i=0;i<this.weekStartsOn;i++){
_aab.push(_aab.shift());
}
}
var _aad=this.dayLabelsRow.getElementsByTagName("td");
for(i=0;i<7;i++){
_aad.item(i).innerHTML=_aab[i];
}
if(this.value){
this.setValue(this.value);
}
},getValue:function(){
return dojo.date.toRfc3339(new Date(this.value),"dateOnly");
},getDate:function(){
return this.value;
},setValue:function(_aae){
this.setDate(_aae);
},setDate:function(_aaf){
if(_aaf==""){
this.value="";
this._preInitUI(this.curMonth,false,true);
}else{
if(typeof _aaf=="string"){
this.value=dojo.date.fromRfc3339(_aaf);
this.value.setHours(0,0,0,0);
}else{
this.value=new Date(_aaf);
this.value.setHours(0,0,0,0);
}
}
if(this.selectedNode!=null){
dojo.html.removeClass(this.selectedNode,this.classNames.selectedDate);
}
if(this.clickedNode!=null){
dojo.debug("adding selectedDate");
dojo.html.addClass(this.clickedNode,this.classNames.selectedDate);
this.selectedNode=this.clickedNode;
}else{
this._preInitUI(this.value,false,true);
}
this.clickedNode=null;
this.onValueChanged(this.value);
},_preInitUI:function(_ab0,_ab1,_ab2){
if(typeof (this.startDate)=="string"){
this.startDate=dojo.date.fromRfc3339(this.startDate);
}
if(typeof (this.endDate)=="string"){
this.endDate=dojo.date.fromRfc3339(this.endDate);
}
this.startDate.setHours(0,0,0,0);
this.endDate.setHours(24,0,0,-1);
if(_ab0<this.startDate||_ab0>this.endDate){
_ab0=new Date((_ab0<this.startDate)?this.startDate:this.endDate);
}
this.firstDay=this._initFirstDay(_ab0,_ab1);
this.selectedIsUsed=false;
this.currentIsUsed=false;
var _ab3=new Date(this.firstDay);
var _ab4=_ab3.getMonth();
this.curMonth=new Date(_ab3);
this.curMonth.setDate(_ab3.getDate()+6);
this.curMonth.setDate(1);
if(this.displayWeeks==""||this.adjustWeeks){
this.adjustWeeks=true;
this.displayWeeks=Math.ceil((dojo.date.getDaysInMonth(this.curMonth)+this._getAdjustedDay(this.curMonth))/7);
}
var days=this.displayWeeks*7;
if(dojo.date.diff(this.startDate,this.endDate,dojo.date.dateParts.DAY)<days){
this.staticDisplay=true;
if(dojo.date.diff(_ab3,this.endDate,dojo.date.dateParts.DAY)>days){
this._preInitUI(this.startDate,true,false);
_ab3=new Date(this.firstDay);
}
this.curMonth=new Date(_ab3);
this.curMonth.setDate(_ab3.getDate()+6);
this.curMonth.setDate(1);
var _ab6=(_ab3.getMonth()==this.curMonth.getMonth())?"current":"previous";
}
if(_ab2){
this._initUI(days);
}
},_initUI:function(days){
dojo.dom.removeChildren(this.calendarDatesContainerNode);
for(var i=0;i<this.displayWeeks;i++){
this.calendarDatesContainerNode.appendChild(this.weekTemplate.cloneNode(true));
}
var _ab9=new Date(this.firstDay);
this._setMonthLabel(this.curMonth.getMonth());
this._setYearLabels(this.curMonth.getFullYear());
var _aba=this.calendarDatesContainerNode.getElementsByTagName("td");
var _abb=this.calendarDatesContainerNode.getElementsByTagName("tr");
var _abc;
for(i=0;i<days;i++){
_abc=_aba.item(i);
_abc.innerHTML=_ab9.getDate();
_abc.setAttribute("djDateValue",_ab9.valueOf());
var _abd=(_ab9.getMonth()!=this.curMonth.getMonth()&&Number(_ab9)<Number(this.curMonth))?"previous":(_ab9.getMonth()==this.curMonth.getMonth())?"current":"next";
var _abe=_abd;
if(this._isDisabledDate(_ab9)){
var _abf={previous:"disabledPrevious",current:"disabledCurrent",next:"disabledNext"};
_abe=_abf[_abd];
}
dojo.html.setClass(_abc,this._getDateClassName(_ab9,_abe));
if(dojo.html.hasClass(_abc,this.classNames.selectedDate)){
this.selectedNode=_abc;
}
_ab9=dojo.date.add(_ab9,dojo.date.dateParts.DAY,1);
}
this.lastDay=dojo.date.add(_ab9,dojo.date.dateParts.DAY,-1);
this._initControls();
},_initControls:function(){
var d=this.firstDay;
var d2=this.lastDay;
var _ac2,_ac3,_ac4,_ac5,_ac6,_ac7;
_ac2=_ac3=_ac4=_ac5=_ac6=_ac7=!this.staticDisplay;
with(dojo.date.dateParts){
var add=dojo.date.add;
if(_ac2&&add(d,DAY,(-1*(this._getAdjustedDay(d)+1)))<this.startDate){
_ac2=_ac4=_ac6=false;
}
if(_ac3&&d2>this.endDate){
_ac3=_ac5=_ac7=false;
}
if(_ac4&&add(d,DAY,-1)<this.startDate){
_ac4=_ac6=false;
}
if(_ac5&&add(d2,DAY,1)>this.endDate){
_ac5=_ac7=false;
}
if(_ac6&&add(d2,YEAR,-1)<this.startDate){
_ac6=false;
}
if(_ac7&&add(d,YEAR,1)>this.endDate){
_ac7=false;
}
}
function enableControl(node,_aca){
dojo.html.setVisibility(node,_aca?"":"hidden");
}
enableControl(this.decreaseWeekNode,_ac2);
enableControl(this.increaseWeekNode,_ac3);
enableControl(this.decreaseMonthNode,_ac4);
enableControl(this.increaseMonthNode,_ac5);
enableControl(this.previousYearLabelNode,_ac6);
enableControl(this.nextYearLabelNode,_ac7);
},_incrementWeek:function(evt){
var d=new Date(this.firstDay);
switch(evt.target){
case this.increaseWeekNode.getElementsByTagName("img").item(0):
case this.increaseWeekNode:
var _acd=dojo.date.add(d,dojo.date.dateParts.WEEK,1);
if(_acd<this.endDate){
d=dojo.date.add(d,dojo.date.dateParts.WEEK,1);
}
break;
case this.decreaseWeekNode.getElementsByTagName("img").item(0):
case this.decreaseWeekNode:
if(d>=this.startDate){
d=dojo.date.add(d,dojo.date.dateParts.WEEK,-1);
}
break;
}
this._preInitUI(d,true,true);
},_incrementMonth:function(evt){
var d=new Date(this.curMonth);
var _ad0=new Date(this.firstDay);
switch(evt.currentTarget){
case this.increaseMonthNode.getElementsByTagName("img").item(0):
case this.increaseMonthNode:
_ad0=dojo.date.add(_ad0,dojo.date.dateParts.DAY,this.displayWeeks*7);
if(_ad0<this.endDate){
d=dojo.date.add(d,dojo.date.dateParts.MONTH,1);
}else{
var _ad1=true;
}
break;
case this.decreaseMonthNode.getElementsByTagName("img").item(0):
case this.decreaseMonthNode:
if(_ad0>this.startDate){
d=dojo.date.add(d,dojo.date.dateParts.MONTH,-1);
}else{
var _ad2=true;
}
break;
}
if(_ad2){
d=new Date(this.startDate);
}else{
if(_ad1){
d=new Date(this.endDate);
}
}
this._preInitUI(d,false,true);
},_incrementYear:function(evt){
var year=this.curMonth.getFullYear();
var _ad5=new Date(this.firstDay);
switch(evt.target){
case this.nextYearLabelNode:
_ad5=dojo.date.add(_ad5,dojo.date.dateParts.YEAR,1);
if(_ad5<this.endDate){
year++;
}else{
var _ad6=true;
}
break;
case this.previousYearLabelNode:
_ad5=dojo.date.add(_ad5,dojo.date.dateParts.YEAR,-1);
if(_ad5>this.startDate){
year--;
}else{
var _ad7=true;
}
break;
}
var d;
if(_ad7){
d=new Date(this.startDate);
}else{
if(_ad6){
d=new Date(this.endDate);
}else{
d=new Date(year,this.curMonth.getMonth(),1);
}
}
this._preInitUI(d,false,true);
},onIncrementWeek:function(evt){
evt.stopPropagation();
if(!this.staticDisplay){
this._incrementWeek(evt);
}
},onIncrementMonth:function(evt){
evt.stopPropagation();
if(!this.staticDisplay){
this._incrementMonth(evt);
}
},onIncrementYear:function(evt){
evt.stopPropagation();
if(!this.staticDisplay){
this._incrementYear(evt);
}
},_setMonthLabel:function(_adc){
this.monthLabelNode.innerHTML=dojo.date.getNames("months","wide","standAlone",this.lang)[_adc];
},_setYearLabels:function(year){
var y=year-1;
var that=this;
function f(n){
that[n+"YearLabelNode"].innerHTML=dojo.date.format(new Date(y++,0),{formatLength:"yearOnly",locale:that.lang});
}
f("previous");
f("current");
f("next");
},_getDateClassName:function(date,_ae2){
var _ae3=this.classNames[_ae2];
if((!this.selectedIsUsed&&this.value)&&(Number(date)==Number(this.value))){
_ae3=this.classNames.selectedDate+" "+_ae3;
this.selectedIsUsed=true;
}
if((!this.currentIsUsed)&&(Number(date)==Number(this.today))){
_ae3=_ae3+" "+this.classNames.currentDate;
this.currentIsUsed=true;
}
return _ae3;
},onClick:function(evt){
dojo.event.browser.stopEvent(evt);
},_handleUiClick:function(evt){
var _ae6=evt.target;
if(_ae6.nodeType!=dojo.dom.ELEMENT_NODE){
_ae6=_ae6.parentNode;
}
dojo.event.browser.stopEvent(evt);
this.selectedIsUsed=this.todayIsUsed=false;
if(dojo.html.hasClass(_ae6,this.classNames["disabledPrevious"])||dojo.html.hasClass(_ae6,this.classNames["disabledCurrent"])||dojo.html.hasClass(_ae6,this.classNames["disabledNext"])){
return;
}
this.clickedNode=_ae6;
this.setDate(new Date(Number(dojo.html.getAttribute(_ae6,"djDateValue"))));
},onValueChanged:function(date){
},_isDisabledDate:function(_ae8){
if(_ae8<this.startDate||_ae8>this.endDate){
return true;
}
return this.isDisabledDate(_ae8,this.lang);
},isDisabledDate:function(_ae9,_aea){
return false;
},_initFirstDay:function(_aeb,adj){
var d=new Date(_aeb);
if(!adj){
d.setDate(1);
}
d.setDate(d.getDate()-this._getAdjustedDay(d,this.weekStartsOn));
d.setHours(0,0,0,0);
return d;
},_getAdjustedDay:function(_aee){
var days=[0,1,2,3,4,5,6];
if(this.weekStartsOn>0){
for(var i=0;i<this.weekStartsOn;i++){
days.unshift(days.pop());
}
}
return days[_aee.getDay()];
},destroy:function(){
dojo.widget.DatePicker.superclass.destroy.apply(this,arguments);
dojo.html.destroyNode(this.weekTemplate);
}});
dojo.kwCompoundRequire({common:["dojo.html.common","dojo.html.style"]});
dojo.provide("dojo.html.*");
dojo.provide("dojo.widget.DropdownDatePicker");
dojo.widget.defineWidget("dojo.widget.DropdownDatePicker",dojo.widget.DropdownContainer,{iconURL:dojo.uri.moduleUri("dojo.widget","templates/images/dateIcon.gif"),formatLength:"short",displayFormat:"",saveFormat:"",value:"",name:"",displayWeeks:6,adjustWeeks:false,startDate:"1492-10-12",endDate:"2941-10-12",weekStartsOn:"",staticDisplay:false,postMixInProperties:function(_af1,frag){
dojo.widget.DropdownDatePicker.superclass.postMixInProperties.apply(this,arguments);
var _af3=dojo.i18n.getLocalization("dojo.widget","DropdownDatePicker",this.lang);
this.iconAlt=_af3.selectDate;
if(typeof (this.value)=="string"&&this.value.toLowerCase()=="today"){
this.value=new Date();
}
if(this.value&&isNaN(this.value)){
var orig=this.value;
this.value=dojo.date.fromRfc3339(this.value);
if(!this.value){
this.value=new Date(orig);
dojo.deprecated("dojo.widget.DropdownDatePicker","date attributes must be passed in Rfc3339 format","0.5");
}
}
if(this.value&&!isNaN(this.value)){
this.value=new Date(this.value);
}
},fillInTemplate:function(args,frag){
dojo.widget.DropdownDatePicker.superclass.fillInTemplate.call(this,args,frag);
var _af7={widgetContainerId:this.widgetId,lang:this.lang,value:this.value,startDate:this.startDate,endDate:this.endDate,displayWeeks:this.displayWeeks,weekStartsOn:this.weekStartsOn,adjustWeeks:this.adjustWeeks,staticDisplay:this.staticDisplay};
this.datePicker=dojo.widget.createWidget("DatePicker",_af7,this.containerNode,"child");
dojo.event.connect(this.datePicker,"onValueChanged",this,"_updateText");
dojo.event.connect(this.inputNode,"onChange",this,"_updateText");
if(this.value){
this._updateText();
}
this.containerNode.explodeClassName="calendarBodyContainer";
this.valueNode.name=this.name;
},getValue:function(){
return this.valueNode.value;
},getDate:function(){
return this.datePicker.value;
},setValue:function(_af8){
this.setDate(_af8);
},setDate:function(_af9){
this.datePicker.setDate(_af9);
this._syncValueNode();
},_updateText:function(){
this.inputNode.value=this.datePicker.value?dojo.date.format(this.datePicker.value,{formatLength:this.formatLength,datePattern:this.displayFormat,selector:"dateOnly",locale:this.lang}):"";
if(this.value<this.datePicker.startDate||this.value>this.datePicker.endDate){
this.inputNode.value="";
}
this._syncValueNode();
this.onValueChanged(this.getDate());
this.hideContainer();
},onValueChanged:function(_afa){
},onInputChange:function(){
var _afb=dojo.string.trim(this.inputNode.value);
if(_afb){
var _afc=dojo.date.parse(_afb,{formatLength:this.formatLength,datePattern:this.displayFormat,selector:"dateOnly",locale:this.lang});
if(!this.datePicker._isDisabledDate(_afc)){
this.setDate(_afc);
}
}else{
if(_afb==""){
this.datePicker.setDate("");
}
this.valueNode.value=_afb;
}
if(_afb){
this._updateText();
}
},_syncValueNode:function(){
var date=this.datePicker.value;
var _afe="";
switch(this.saveFormat.toLowerCase()){
case "rfc":
case "iso":
case "":
_afe=dojo.date.toRfc3339(date,"dateOnly");
break;
case "posix":
case "unix":
_afe=Number(date);
break;
default:
if(date){
_afe=dojo.date.format(date,{datePattern:this.saveFormat,selector:"dateOnly",locale:this.lang});
}
}
this.valueNode.value=_afe;
},destroy:function(_aff){
this.datePicker.destroy(_aff);
dojo.widget.DropdownDatePicker.superclass.destroy.apply(this,arguments);
}});
dojo.provide("openbravo.widget.ContentPane");
dojo.widget.defineWidget("openbravo.widget.ContentPane",dojo.widget.ContentPane,{fillInTemplate:function(){
openbravo.widget.ContentPane.superclass.fillInTemplate.apply(this,arguments);
}});
dojo.provide("openbravo.widget.LayoutContainer");
dojo.widget.defineWidget("openbravo.widget.LayoutContainer",dojo.widget.LayoutContainer,{fillInTemplate:function(){
openbravo.widget.LayoutContainer.superclass.fillInTemplate.apply(this,arguments);
}});
dojo.provide("openbravo.widget.Checkbox");
dojo.widget.defineWidget("openbravo.widget.Checkbox",dojo.widget.Checkbox,{templatePath:dojo.uri.dojoUri("../openbravo/widget/templates/Checkbox.html"),templateCssPath:null});
dojo.provide("openbravo.widget.ValidationTextbox");
dojo.require("openbravo.messages.common");
dojo.widget.defineWidget("openbravo.widget.ValidationTextbox",dojo.widget.ValidationTextbox,{readonly:false,disabled:false,listenOnKeyPress:false,templatePath:dojo.uri.dojoUri("../openbravo/widget/templates/ValidationTextbox.html"),templateCssPath:null,
postMixInProperties: function(localProperties, frag) {
openbravo.widget.ValidationTextbox.superclass.postMixInProperties.apply(this, arguments);
this.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
this.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
this.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
this.messages.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
this.messages.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
this.messages.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
},
fillInTemplate:function(args,frag){
openbravo.widget.ValidationTextbox.superclass.fillInTemplate.apply(this,arguments);
if(this.required){
dojo.html.addClass(this.textbox,"required");
}
if(this.readonly){
dojo.html.addClass(this.textbox,"readonly");
}
if(this.disabled){
dojo.html.addClass(this.textbox,"disabled");
}
if(this.disabled){
this.textbox.setAttribute("disabled","true");
}
if(this.readonly){
this.textbox.setAttribute("readonly","true");
}
this.invalidSpan.style.display="none";
this.missingSpan.style.display="none";
this.rangeSpan.style.display="none";
},onkeydown:function(){
},onclick:function(){
},onchange:function(){
}});
dojo.provide("openbravo.widget.ValidationTextArea");
dojo.require("openbravo.messages.common");
dojo.widget.defineWidget("openbravo.widget.ValidationTextArea",dojo.widget.ValidationTextbox,{readonly:false,disabled:false,listenOnKeyPress:false,cols:"",rows:"",templatePath:dojo.uri.dojoUri("../openbravo/widget/templates/ValidationTextArea.html"),templateCssPath:null,
postMixInProperties: function(localProperties, frag) {
openbravo.widget.ValidationTextArea.superclass.postMixInProperties.apply(this, arguments);
this.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
this.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
this.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
this.messages.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
this.messages.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
this.messages.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
}, 
fillInTemplate:function(){
openbravo.widget.ValidationTextArea.superclass.fillInTemplate.apply(this,arguments);
if(this.required){
dojo.html.addClass(this.textbox,"required");
}
if(this.readonly){
dojo.html.addClass(this.textbox,"readonly");
}
if(this.disabled){
dojo.html.addClass(this.textbox,"disabled");
}
if(this.disabled){
this.textbox.setAttribute("disabled","true");
}
if(this.readonly){
this.textbox.setAttribute("readonly","true");
}
this.invalidSpan.style.display="none";
this.missingSpan.style.display="none";
this.rangeSpan.style.display="none";
},onkeydown:function(){
},onclick:function(){
},onchange:function(){
}});
dojo.provide("openbravo.widget.RealNumberTextbox");
dojo.require("openbravo.messages.common");
dojo.widget.defineWidget("openbravo.widget.RealNumberTextbox",dojo.widget.RealNumberTextbox,{readonly:false,disabled:false,listenOnKeyPress:false,templatePath:dojo.uri.dojoUri("../openbravo/widget/templates/ValidationTextbox.html"),templateCssPath:null,
postMixInProperties: function(localProperties, frag) {
openbravo.widget.RealNumberTextbox.superclass.postMixInProperties.apply(this, arguments);
this.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
this.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
this.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
this.messages.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
this.messages.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
this.messages.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
},
fillInTemplate:function(args,frag){
openbravo.widget.RealNumberTextbox.superclass.fillInTemplate.apply(this,arguments);
if(this.required){
dojo.html.addClass(this.textbox,"required");
}
if(this.readonly){
dojo.html.addClass(this.textbox,"readonly");
}
if(this.disabled){
dojo.html.addClass(this.textbox,"disabled");
}
this.textbox.disabled=this.disabled;
this.textbox.readonly=this.readonly;
this.invalidSpan.style.display="none";
this.missingSpan.style.display="none";
this.rangeSpan.style.display="none";
},onkeydown:function(){
},onclick:function(){
},onchange:function(){
}});
dojo.provide("openbravo.widget.DateTextbox");
dojo.require("openbravo.messages.common");
dojo.widget.defineWidget("openbravo.widget.DateTextbox",dojo.widget.DateTextbox,{displayFormat:"",saveFormat:"",listenOnKeyPress:false,
// override css
templatePath: dojo.uri.dojoUri("../openbravo/widget/templates/DateTextbox.html"),
templateCssPath: null, // Defined in the skin --- ValidationTextbox.css
postMixInProperties:function(_b04,frag){
  openbravo.widget.DateTextbox.superclass.postMixInProperties.apply(this,arguments);
this.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
this.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
this.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
this.messages.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
this.messages.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
this.messages.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
},fillInTemplate:function(args,frag){
this.displayFormat=this.displayFormat.replace("mm","MM").replace("DD","dd").replace("YYYY","yyyy");
this.saveFormat=this.displayFormat;
openbravo.widget.DateTextbox.superclass.fillInTemplate.apply(this,arguments);
this.invalidSpan.style.display="none";
this.missingSpan.style.display="none";
this.rangeSpan.style.display="none";
},
isValid: function(){ 
  str_datetime = this.textbox.value;
  str_dateFormat = this.displayFormat;
  if (str_datetime.length == 0) return true;
  // datetime parsing and formatting routimes. modify them if you wish other datetime format
//function str2dt (str_datetime) {
	var re_date = /^(\d+)[\-|\/|\.](\d+)[\-|\/|\.](\d+)$/;
	if (!re_date.exec(str_datetime))
		return false;
	if (!str_dateFormat) str_dateFormat = defaultDateFormat;
	switch (str_dateFormat) {
		case "MM\/DD\/YYYY":
		case "MM\/DD\/YY":
		case "MM-DD-YYYY":
		case "MM-DD-YY":
		case "MM.DD.YYYY":
		case "MM.DD.YY":
		case "%m-%d-%Y":
			if (RegExp.$2 < 1 || RegExp.$2 > 31) return false;
			if (RegExp.$1 < 1 || RegExp.$1 > 12) return false;
			if (RegExp.$3 < 1 || RegExp.$3 > 9999) return false;
		case "YYYY\/MM\/DD":
		case "YY\/MM\/DD":
		case "YYYY-MM-DD":
		case "YY-MM-DD":
		case "YYYY.MM.DD":
		case "YY.MM.DD":
		case "%Y-%m-%d":
			if (RegExp.$3 < 1 || RegExp.$3 > 31) return false;
			if (RegExp.$2 < 1 || RegExp.$2 > 12) return false;
			if (RegExp.$1 < 1 || RegExp.$1 > 9999) return false;
		case "DD\/MM\/YYYY":
		case "DD\/MM\/YY":
		case "DD-MM-YYYY":
		case "DD-MM-YY":
		case "DD.MM.YYYY":
		case "DD.MM.YY":
		case "%d-%m-%Y":
		default:
			if (RegExp.$1 < 1 || RegExp.$1 > 31) return false;
			if (RegExp.$2 < 1 || RegExp.$2 > 12) return false;
			if (RegExp.$3 < 1 || RegExp.$3 > 9999) return false;
	}
	return (new Date (RegExp.$1, RegExp.$2, RegExp.$3, 0, 0, 0));
},
dateToRfc3339:function(_b08,_b09){
var year=null;
var _b0b=null;
var day=null;
switch(_b09.toUpperCase()){
case "DD-MM-YYYY":
case "DD/MM/YYYY":
year=_b08.substring(6,10);
_b0b=_b08.substring(3,5);
day=_b08.substring(0,2);
_b08=year+"-"+_b0b+"-"+day;
break;
case "MM-DD-YYYY":
case "MM/DD/YYYY":
year=_b08.substring(6,10);
_b0b=_b08.substring(0,2);
day=_b08.substring(3,5);
_b08=year+"-"+_b0b+"-"+day;
break;
}
return _b08;
}});
dojo.provide("openbravo.widget.TimeTextbox");
dojo.require("openbravo.messages.common");
dojo.widget.defineWidget("openbravo.widget.TimeTextbox",dojo.widget.TimeTextbox,{listenOnKeyPress:false,
postMixInProperties: function(localProperties, frag) {
openbravo.widget.TimeTextbox.superclass.postMixInProperties.apply(this, arguments);
this.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
this.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
this.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
this.messages.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
this.messages.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
this.messages.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
},
fillInTemplate:function(args,frag){
openbravo.widget.TimeTextbox.superclass.fillInTemplate.apply(this,arguments);
dojo.event.connect(this.textbox,"onchange",this,"onInputChange");
dojo.event.connect(this.textbox,"onclick",this,"onIconClick");
this.invalidSpan.style.display="none";
this.missingSpan.style.display="none";
this.rangeSpan.style.display="none";
},onclick:function(){
},onchange:function(){
},onIconClick:function(){
this.onclick();
},onInputChange:function(){
this.onchange();
}});
dojo.provide("openbravo.widget.DropdownDatePicker");
dojo.widget.defineWidget("openbravo.widget.DropdownDatePicker",dojo.widget.DropdownDatePicker,{iconURL:dojo.uri.dojoUri("../openbravo/widget/templates/dateIcon.gif"),displayFormat:"",saveFormat:"",templateString:"",templatePath:dojo.uri.dojoUri("../openbravo/widget/templates/DropdownDatePicker.html"),templateCssPath:null,postMixInProperties:function(_b0f,frag){
this.value=this.dateToRfc3339(this.value,this.displayFormat);
},fillInTemplate:function(args,frag){
this.displayFormat=this.displayFormat.replace("mm","MM").replace("DD","dd").replace("YYYY","yyyy");
this.saveFormat=this.displayFormat;
dojo.widget.DropdownDatePicker.superclass.fillInTemplate.call(this,args,frag);
var _b13={widgetContainerId:this.widgetId,lang:this.lang,value:this.value,startDate:this.startDate,endDate:this.endDate,displayWeeks:this.displayWeeks,weekStartsOn:this.weekStartsOn,adjustWeeks:this.adjustWeeks,staticDisplay:this.staticDisplay,templateCssPath:null};
this.datePicker=dojo.widget.createWidget("DatePicker",_b13,this.containerNode,"child");
dojo.event.connect(this.datePicker,"onValueChanged",this,"_updateText");
if(this.value){
this._updateText();
}
this.containerNode.explodeClassName="calendarBodyContainer";
this.valueNode.name=this.name;
},dateToRfc3339:function(_b14,_b15){
var year=null;
var _b17=null;
var day=null;
switch(_b15.toUpperCase()){
case "DD-MM-YYYY":
case "DD/MM/YYYY":
year=_b14.substring(6,10);
_b17=_b14.substring(3,5);
day=_b14.substring(0,2);
_b14=year+"-"+_b17+"-"+day;
break;
case "MM-DD-YYYY":
case "MM/DD/YYYY":
year=_b14.substring(6,10);
_b17=_b14.substring(0,2);
day=_b14.substring(3,5);
_b14=year+"-"+_b17+"-"+day;
break;
}
return _b14;
}});
dojo.provide("openbravo.widget.MessageBox");
dojo.widget.defineWidget("openbravo.widget.MessageBox",dojo.widget.HtmlWidget,{maxLevel:"Hidden",arrMessages:new Array(),title:"",type:"HIDDEN",isContainer:"true",templatePath:dojo.uri.dojoUri("../openbravo/widget/templates/MessageBox.html"),templateCssPath:null,fillInTemplate:function(args,frag){
if((this.type!=null&&this.type!="")&&((this.title!=null&&this.title!="")||(frag.value!=null&&frag.value!=""))){
this.setValues(this.type,this.title,frag.value);
}
this.render();
},setType:function(){
dojo.html.setClass(this.messageNode,"MessageBox"+this.maxLevel.toUpperCase());
},setContainer:function(_b1b){
this.containerNode.innerHTML=_b1b;
},setMessage:function(){
var _b1c="";
for(var i=0;i<this.arrMessages.length;i++){
if(this.arrMessages[i][0]==this.maxLevel){
_b1c+= "<DIV class=\"MessageBox_TextTitle\">"+ this.arrMessages[i][1] +"</DIV>\n<DIV class=\"MessageBox_TextDescription\">"+ this.arrMessages[i][2] +"</DIV>\n<DIV class=\"MessageBox_TextSeparator\"></DIV>";
}
}
_b1c+="";
this.setContainer(_b1c);
},setValues:function(type,_b1f,_b20){
var _b21=dojo.string.capitalize(type);
if(this.maxLevel=="ERROR"){
}else {
if(this.maxLevel=="WARNING"){
if(_b21=="ERROR"){
this.maxLevel=_b21;
}
}
else{
if(this.maxLevel=="INFO"){
if(_b21=="ERROR"||_b21=="WARNING"){
this.maxLevel=_b21;
}
}
else{
if(this.maxLevel=="SUCCESS"){
if(_b21=="ERROR"||_b21=="WARNING"||_b21=="INFO"){
this.maxLevel=_b21;
}
}
else{
if(this.maxLevel=="HIDDEN"){
if(_b21=="ERROR"||_b21=="WARNING"||_b21=="INFO"||_b21=="SUCCESS"){
this.maxLevel=_b21;
}
}
}
}
}
}
var _b22=this.arrMessages.length;
this.arrMessages[_b22]=new Array(3);
this.arrMessages[_b22][0]=_b21;
this.arrMessages[_b22][1]=_b1f;
this.arrMessages[_b22][2]=_b20;
this.render();
},initialize:function(){
this.maxLevel="HIDDEN";
this.arrMessages=new Array();
},render:function(){
this.setType();
this.setMessage();
},clear:function(){
this.initialize();
this.render();
}});
dojo.provide("dojo.validate.web");
dojo.validate.isIpAddress=function(_b23,_b24){
var re=new RegExp("^"+dojo.regexp.ipAddress(_b24)+"$","i");
return re.test(_b23);
};
dojo.validate.isUrl=function(_b26,_b27){
var re=new RegExp("^"+dojo.regexp.url(_b27)+"$","i");
return re.test(_b26);
};
dojo.validate.isEmailAddress=function(_b29,_b2a){
var re=new RegExp("^"+dojo.regexp.emailAddress(_b2a)+"$","i");
return re.test(_b29);
};
dojo.validate.isEmailAddressList=function(_b2c,_b2d){
var re=new RegExp("^"+dojo.regexp.emailAddressList(_b2d)+"$","i");
return re.test(_b2c);
};
dojo.validate.getEmailAddressList=function(_b2f,_b30){
if(!_b30){
_b30={};
}
if(!_b30.listSeparator){
_b30.listSeparator="\\s;,";
}
if(dojo.validate.isEmailAddressList(_b2f,_b30)){
return _b2f.split(new RegExp("\\s*["+_b30.listSeparator+"]\\s*"));
}
return [];
};
dojo.provide("dojo.widget.InternetTextbox");
dojo.widget.defineWidget("dojo.widget.IpAddressTextbox",dojo.widget.ValidationTextbox,{mixInProperties:function(_b31){
dojo.widget.IpAddressTextbox.superclass.mixInProperties.apply(this,arguments);
if(_b31.allowdotteddecimal){
this.flags.allowDottedDecimal=(_b31.allowdotteddecimal=="true");
}
if(_b31.allowdottedhex){
this.flags.allowDottedHex=(_b31.allowdottedhex=="true");
}
if(_b31.allowdottedoctal){
this.flags.allowDottedOctal=(_b31.allowdottedoctal=="true");
}
if(_b31.allowdecimal){
this.flags.allowDecimal=(_b31.allowdecimal=="true");
}
if(_b31.allowhex){
this.flags.allowHex=(_b31.allowhex=="true");
}
if(_b31.allowipv6){
this.flags.allowIPv6=(_b31.allowipv6=="true");
}
if(_b31.allowhybrid){
this.flags.allowHybrid=(_b31.allowhybrid=="true");
}
},isValid:function(){
return dojo.validate.isIpAddress(this.textbox.value,this.flags);
}});
dojo.widget.defineWidget("dojo.widget.UrlTextbox",dojo.widget.IpAddressTextbox,{mixInProperties:function(_b32){
dojo.widget.UrlTextbox.superclass.mixInProperties.apply(this,arguments);
if(_b32.scheme){
this.flags.scheme=(_b32.scheme=="true");
}
if(_b32.allowip){
this.flags.allowIP=(_b32.allowip=="true");
}
if(_b32.allowlocal){
this.flags.allowLocal=(_b32.allowlocal=="true");
}
if(_b32.allowcc){
this.flags.allowCC=(_b32.allowcc=="true");
}
if(_b32.allowgeneric){
this.flags.allowGeneric=(_b32.allowgeneric=="true");
}
},isValid:function(){
return dojo.validate.isUrl(this.textbox.value,this.flags);
}});
dojo.widget.defineWidget("dojo.widget.EmailTextbox",dojo.widget.UrlTextbox,{mixInProperties:function(_b33){
dojo.widget.EmailTextbox.superclass.mixInProperties.apply(this,arguments);
if(_b33.allowcruft){
this.flags.allowCruft=(_b33.allowcruft=="true");
}
},isValid:function(){
return dojo.validate.isEmailAddress(this.textbox.value,this.flags);
}});
dojo.widget.defineWidget("dojo.widget.EmailListTextbox",dojo.widget.EmailTextbox,{mixInProperties:function(_b34){
dojo.widget.EmailListTextbox.superclass.mixInProperties.apply(this,arguments);
if(_b34.listseparator){
this.flags.listSeparator=_b34.listseparator;
}
},isValid:function(){
return dojo.validate.isEmailAddressList(this.textbox.value,this.flags);
}});
dojo.provide("openbravo.widget.HyperLink");
dojo.widget.defineWidget("openbravo.widget.HyperLink",dojo.widget.HtmlWidget,{name:"",iconURL:dojo.uri.dojoUri("../openbravo/widget/templates/linkIcon.gif"),iconAlt:"Go to",value:"",containerToggle:"plain",containerToggleDuration:150,templatePath:dojo.uri.dojoUri("../openbravo/widget/templates/HyperLink.html"),templateCssPath:null,fillInTemplate:function(args,frag){
dojo.event.connect(this.inputHyperLinkNode,"onchange",this,"onInputChange");
dojo.event.connect(this.inputHyperLinkNode,"onkeyup",this,"onInputKeyUp");
},onClick:function(){
},onIconClick:function(){
window.open(this.value);
},onChange:function(){
},onInputChange:function(){
this.value=this.inputHyperLinkNode.value;
},onKeyUp:function(){
},onInputKeyUp:function(){
}});
dojo.provide("dojo.lfx.shadow");
dojo.lfx.shadow=function(node){
this.shadowPng=dojo.uri.moduleUri("dojo.html","images/shadow");
this.shadowThickness=8;
this.shadowOffset=15;
this.init(node);
};
dojo.extend(dojo.lfx.shadow,{init:function(node){
this.node=node;
this.pieces={};
var x1=-1*this.shadowThickness;
var y0=this.shadowOffset;
var y1=this.shadowOffset+this.shadowThickness;
this._makePiece("tl","top",y0,"left",x1);
this._makePiece("l","top",y1,"left",x1,"scale");
this._makePiece("tr","top",y0,"left",0);
this._makePiece("r","top",y1,"left",0,"scale");
this._makePiece("bl","top",0,"left",x1);
this._makePiece("b","top",0,"left",0,"crop");
this._makePiece("br","top",0,"left",0);
},_makePiece:function(name,_b3d,_b3e,_b3f,_b40,_b41){
var img;
var url=this.shadowPng+name.toUpperCase()+".png";
if(dojo.render.html.ie55||dojo.render.html.ie60){
img=dojo.doc().createElement("div");
img.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+url+"'"+(_b41?", sizingMethod='"+_b41+"'":"")+")";
}else{
img=dojo.doc().createElement("img");
img.src=url;
}
img.style.position="absolute";
img.style[_b3d]=_b3e+"px";
img.style[_b3f]=_b40+"px";
img.style.width=this.shadowThickness+"px";
img.style.height=this.shadowThickness+"px";
this.pieces[name]=img;
this.node.appendChild(img);
},size:function(_b44,_b45){
var _b46=_b45-(this.shadowOffset+this.shadowThickness+1);
if(_b46<0){
_b46=0;
}
if(_b45<1){
_b45=1;
}
if(_b44<1){
_b44=1;
}
with(this.pieces){
l.style.height=_b46+"px";
r.style.height=_b46+"px";
b.style.width=(_b44-1)+"px";
bl.style.top=(_b45-1)+"px";
b.style.top=(_b45-1)+"px";
br.style.top=(_b45-1)+"px";
tr.style.left=(_b44-1)+"px";
r.style.left=(_b44-1)+"px";
br.style.left=(_b44-1)+"px";
}
}});
dojo.provide("dojo.dnd.DragAndDrop");
dojo.declare("dojo.dnd.DragSource",null,{type:"",onDragEnd:function(evt){
},onDragStart:function(evt){
},onSelected:function(evt){
},unregister:function(){
dojo.dnd.dragManager.unregisterDragSource(this);
},reregister:function(){
dojo.dnd.dragManager.registerDragSource(this);
}});
dojo.declare("dojo.dnd.DragObject",null,{type:"",register:function(){
var dm=dojo.dnd.dragManager;
if(dm["registerDragObject"]){
dm.registerDragObject(this);
}
},onDragStart:function(evt){
},onDragMove:function(evt){
},onDragOver:function(evt){
},onDragOut:function(evt){
},onDragEnd:function(evt){
},onDragLeave:dojo.lang.forward("onDragOut"),onDragEnter:dojo.lang.forward("onDragOver"),ondragout:dojo.lang.forward("onDragOut"),ondragover:dojo.lang.forward("onDragOver")});
dojo.declare("dojo.dnd.DropTarget",null,{acceptsType:function(type){
if(!dojo.lang.inArray(this.acceptedTypes,"*")){
if(!dojo.lang.inArray(this.acceptedTypes,type)){
return false;
}
}
return true;
},accepts:function(_b51){
if(!dojo.lang.inArray(this.acceptedTypes,"*")){
for(var i=0;i<_b51.length;i++){
if(!dojo.lang.inArray(this.acceptedTypes,_b51[i].type)){
return false;
}
}
}
return true;
},unregister:function(){
dojo.dnd.dragManager.unregisterDropTarget(this);
},onDragOver:function(evt){
},onDragOut:function(evt){
},onDragMove:function(evt){
},onDropStart:function(evt){
},onDrop:function(evt){
},onDropEnd:function(){
}},function(){
this.acceptedTypes=[];
});
dojo.dnd.DragEvent=function(){
this.dragSource=null;
this.dragObject=null;
this.target=null;
this.eventStatus="success";
};
dojo.declare("dojo.dnd.DragManager",null,{selectedSources:[],dragObjects:[],dragSources:[],registerDragSource:function(_b58){
},dropTargets:[],registerDropTarget:function(_b59){
},lastDragTarget:null,currentDragTarget:null,onKeyDown:function(){
},onMouseOut:function(){
},onMouseMove:function(){
},onMouseUp:function(){
}});
dojo.provide("dojo.dnd.HtmlDragManager");
dojo.declare("dojo.dnd.HtmlDragManager",dojo.dnd.DragManager,{disabled:false,nestedTargets:false,mouseDownTimer:null,dsCounter:0,dsPrefix:"dojoDragSource",dropTargetDimensions:[],currentDropTarget:null,previousDropTarget:null,_dragTriggered:false,selectedSources:[],dragObjects:[],dragSources:[],dropTargets:[],currentX:null,currentY:null,lastX:null,lastY:null,mouseDownX:null,mouseDownY:null,threshold:7,dropAcceptable:false,cancelEvent:function(e){
e.stopPropagation();
e.preventDefault();
},registerDragSource:function(ds){
if(ds["domNode"]){
var dp=this.dsPrefix;
var _b5d=dp+"Idx_"+(this.dsCounter++);
ds.dragSourceId=_b5d;
this.dragSources[_b5d]=ds;
ds.domNode.setAttribute(dp,_b5d);
if(dojo.render.html.ie){
dojo.event.browser.addListener(ds.domNode,"ondragstart",this.cancelEvent);
}
}
},unregisterDragSource:function(ds){
if(ds["domNode"]){
var dp=this.dsPrefix;
var _b60=ds.dragSourceId;
delete ds.dragSourceId;
delete this.dragSources[_b60];
ds.domNode.setAttribute(dp,null);
if(dojo.render.html.ie){
dojo.event.browser.removeListener(ds.domNode,"ondragstart",this.cancelEvent);
}
}
},registerDropTarget:function(dt){
this.dropTargets.push(dt);
},unregisterDropTarget:function(dt){
var _b63=dojo.lang.find(this.dropTargets,dt,true);
if(_b63>=0){
this.dropTargets.splice(_b63,1);
}
},getDragSource:function(e){
var tn=e.target;
if(tn===dojo.body()){
return;
}
var ta=dojo.html.getAttribute(tn,this.dsPrefix);
while((!ta)&&(tn)){
tn=tn.parentNode;
if((!tn)||(tn===dojo.body())){
return;
}
ta=dojo.html.getAttribute(tn,this.dsPrefix);
}
return this.dragSources[ta];
},onKeyDown:function(e){
},onMouseDown:function(e){
if(this.disabled){
return;
}
if(dojo.render.html.ie){
if(e.button!=1){
return;
}
}else{
if(e.which!=1){
return;
}
}
var _b69=e.target.nodeType==dojo.html.TEXT_NODE?e.target.parentNode:e.target;
if(dojo.html.isTag(_b69,"button","textarea","input","select","option")){
return;
}
var ds=this.getDragSource(e);
if(!ds){
return;
}
if(!dojo.lang.inArray(this.selectedSources,ds)){
this.selectedSources.push(ds);
ds.onSelected();
}
this.mouseDownX=e.pageX;
this.mouseDownY=e.pageY;
e.preventDefault();
dojo.event.connect(document,"onmousemove",this,"onMouseMove");
},onMouseUp:function(e,_b6c){
if(this.selectedSources.length==0){
return;
}
this.mouseDownX=null;
this.mouseDownY=null;
this._dragTriggered=false;
e.dragSource=this.dragSource;
if((!e.shiftKey)&&(!e.ctrlKey)){
if(this.currentDropTarget){
this.currentDropTarget.onDropStart();
}
dojo.lang.forEach(this.dragObjects,function(_b6d){
var ret=null;
if(!_b6d){
return;
}
if(this.currentDropTarget){
e.dragObject=_b6d;
var ce=this.currentDropTarget.domNode.childNodes;
if(ce.length>0){
e.dropTarget=ce[0];
while(e.dropTarget==_b6d.domNode){
e.dropTarget=e.dropTarget.nextSibling;
}
}else{
e.dropTarget=this.currentDropTarget.domNode;
}
if(this.dropAcceptable){
ret=this.currentDropTarget.onDrop(e);
}else{
this.currentDropTarget.onDragOut(e);
}
}
e.dragStatus=this.dropAcceptable&&ret?"dropSuccess":"dropFailure";
dojo.lang.delayThese([function(){
try{
_b6d.dragSource.onDragEnd(e);
}
catch(err){
var _b70={};
for(var i in e){
if(i=="type"){
_b70.type="mouseup";
continue;
}
_b70[i]=e[i];
}
_b6d.dragSource.onDragEnd(_b70);
}
},function(){
_b6d.onDragEnd(e);
}]);
},this);
this.selectedSources=[];
this.dragObjects=[];
this.dragSource=null;
if(this.currentDropTarget){
this.currentDropTarget.onDropEnd();
}
}else{
}
dojo.event.disconnect(document,"onmousemove",this,"onMouseMove");
this.currentDropTarget=null;
},onScroll:function(){
for(var i=0;i<this.dragObjects.length;i++){
if(this.dragObjects[i].updateDragOffset){
this.dragObjects[i].updateDragOffset();
}
}
if(this.dragObjects.length){
this.cacheTargetLocations();
}
},_dragStartDistance:function(x,y){
if((!this.mouseDownX)||(!this.mouseDownX)){
return;
}
var dx=Math.abs(x-this.mouseDownX);
var dx2=dx*dx;
var dy=Math.abs(y-this.mouseDownY);
var dy2=dy*dy;
return parseInt(Math.sqrt(dx2+dy2),10);
},cacheTargetLocations:function(){
dojo.profile.start("cacheTargetLocations");
this.dropTargetDimensions=[];
dojo.lang.forEach(this.dropTargets,function(_b79){
var tn=_b79.domNode;
if(!tn||!_b79.accepts([this.dragSource])){
return;
}
var abs=dojo.html.getAbsolutePosition(tn,true);
var bb=dojo.html.getBorderBox(tn);
this.dropTargetDimensions.push([[abs.x,abs.y],[abs.x+bb.width,abs.y+bb.height],_b79]);
},this);
dojo.profile.end("cacheTargetLocations");
},onMouseMove:function(e){
if((dojo.render.html.ie)&&(e.button!=1)){
this.currentDropTarget=null;
this.onMouseUp(e,true);
return;
}
if((this.selectedSources.length)&&(!this.dragObjects.length)){
var dx;
var dy;
if(!this._dragTriggered){
this._dragTriggered=(this._dragStartDistance(e.pageX,e.pageY)>this.threshold);
if(!this._dragTriggered){
return;
}
dx=e.pageX-this.mouseDownX;
dy=e.pageY-this.mouseDownY;
}
this.dragSource=this.selectedSources[0];
dojo.lang.forEach(this.selectedSources,function(_b80){
if(!_b80){
return;
}
var tdo=_b80.onDragStart(e);
if(tdo){
tdo.onDragStart(e);
tdo.dragOffset.y+=dy;
tdo.dragOffset.x+=dx;
tdo.dragSource=_b80;
this.dragObjects.push(tdo);
}
},this);
this.previousDropTarget=null;
this.cacheTargetLocations();
}
dojo.lang.forEach(this.dragObjects,function(_b82){
if(_b82){
_b82.onDragMove(e);
}
});
if(this.currentDropTarget){
var c=dojo.html.toCoordinateObject(this.currentDropTarget.domNode,true);
var dtp=[[c.x,c.y],[c.x+c.width,c.y+c.height]];
}
if((!this.nestedTargets)&&(dtp)&&(this.isInsideBox(e,dtp))){
if(this.dropAcceptable){
this.currentDropTarget.onDragMove(e,this.dragObjects);
}
}else{
var _b85=this.findBestTarget(e);
if(_b85.target===null){
if(this.currentDropTarget){
this.currentDropTarget.onDragOut(e);
this.previousDropTarget=this.currentDropTarget;
this.currentDropTarget=null;
}
this.dropAcceptable=false;
return;
}
if(this.currentDropTarget!==_b85.target){
if(this.currentDropTarget){
this.previousDropTarget=this.currentDropTarget;
this.currentDropTarget.onDragOut(e);
}
this.currentDropTarget=_b85.target;
e.dragObjects=this.dragObjects;
this.dropAcceptable=this.currentDropTarget.onDragOver(e);
}else{
if(this.dropAcceptable){
this.currentDropTarget.onDragMove(e,this.dragObjects);
}
}
}
},findBestTarget:function(e){
var _b87=this;
var _b88=new Object();
_b88.target=null;
_b88.points=null;
dojo.lang.every(this.dropTargetDimensions,function(_b89){
if(!_b87.isInsideBox(e,_b89)){
return true;
}
_b88.target=_b89[2];
_b88.points=_b89;
return Boolean(_b87.nestedTargets);
});
return _b88;
},isInsideBox:function(e,_b8b){
if((e.pageX>_b8b[0][0])&&(e.pageX<_b8b[1][0])&&(e.pageY>_b8b[0][1])&&(e.pageY<_b8b[1][1])){
return true;
}
return false;
},onMouseOver:function(e){
},onMouseOut:function(e){
}});
dojo.dnd.dragManager=new dojo.dnd.HtmlDragManager();
(function(){
var d=document;
var dm=dojo.dnd.dragManager;
dojo.event.connect(d,"onkeydown",dm,"onKeyDown");
dojo.event.connect(d,"onmouseover",dm,"onMouseOver");
dojo.event.connect(d,"onmouseout",dm,"onMouseOut");
dojo.event.connect(d,"onmousedown",dm,"onMouseDown");
dojo.event.connect(d,"onmouseup",dm,"onMouseUp");
dojo.event.connect(window,"onscroll",dm,"onScroll");
})();
dojo.provide("dojo.dnd.HtmlDragAndDrop");
dojo.declare("dojo.dnd.HtmlDragSource",dojo.dnd.DragSource,{dragClass:"",onDragStart:function(){
var _b90=new dojo.dnd.HtmlDragObject(this.dragObject,this.type);
if(this.dragClass){
_b90.dragClass=this.dragClass;
}
if(this.constrainToContainer){
_b90.constrainTo(this.constrainingContainer||this.domNode.parentNode);
}
return _b90;
},setDragHandle:function(node){
node=dojo.byId(node);
dojo.dnd.dragManager.unregisterDragSource(this);
this.domNode=node;
dojo.dnd.dragManager.registerDragSource(this);
},setDragTarget:function(node){
this.dragObject=node;
},constrainTo:function(_b93){
this.constrainToContainer=true;
if(_b93){
this.constrainingContainer=_b93;
}
},onSelected:function(){
for(var i=0;i<this.dragObjects.length;i++){
dojo.dnd.dragManager.selectedSources.push(new dojo.dnd.HtmlDragSource(this.dragObjects[i]));
}
},addDragObjects:function(el){
for(var i=0;i<arguments.length;i++){
this.dragObjects.push(dojo.byId(arguments[i]));
}
}},function(node,type){
node=dojo.byId(node);
this.dragObjects=[];
this.constrainToContainer=false;
if(node){
this.domNode=node;
this.dragObject=node;
this.type=(type)||(this.domNode.nodeName.toLowerCase());
dojo.dnd.DragSource.prototype.reregister.call(this);
}
});
dojo.declare("dojo.dnd.HtmlDragObject",dojo.dnd.DragObject,{dragClass:"",opacity:0.5,createIframe:true,disableX:false,disableY:false,createDragNode:function(){
var node=this.domNode.cloneNode(true);
if(this.dragClass){
dojo.html.addClass(node,this.dragClass);
}
if(this.opacity<1){
dojo.html.setOpacity(node,this.opacity);
}
var ltn=node.tagName.toLowerCase();
var isTr=(ltn=="tr");
if((isTr)||(ltn=="tbody")){
var doc=this.domNode.ownerDocument;
var _b9d=doc.createElement("table");
if(isTr){
var _b9e=doc.createElement("tbody");
_b9d.appendChild(_b9e);
_b9e.appendChild(node);
}else{
_b9d.appendChild(node);
}
var _b9f=((isTr)?this.domNode:this.domNode.firstChild);
var _ba0=((isTr)?node:node.firstChild);
var _ba1=_b9f.childNodes;
var _ba2=_ba0.childNodes;
for(var i=0;i<_ba1.length;i++){
if((_ba2[i])&&(_ba2[i].style)){
_ba2[i].style.width=dojo.html.getContentBox(_ba1[i]).width+"px";
}
}
node=_b9d;
}
if((dojo.render.html.ie55||dojo.render.html.ie60)&&this.createIframe){
with(node.style){
top="0px";
left="0px";
}
var _ba4=document.createElement("div");
_ba4.appendChild(node);
this.bgIframe=new dojo.html.BackgroundIframe(_ba4);
_ba4.appendChild(this.bgIframe.iframe);
node=_ba4;
}
node.style.zIndex=999;
return node;
},onDragStart:function(e){
dojo.html.clearSelection();
this.scrollOffset=dojo.html.getScroll().offset;
this.dragStartPosition=dojo.html.getAbsolutePosition(this.domNode,true);
this.dragOffset={y:this.dragStartPosition.y-e.pageY,x:this.dragStartPosition.x-e.pageX};
this.dragClone=this.createDragNode();
this.containingBlockPosition=this.domNode.offsetParent?dojo.html.getAbsolutePosition(this.domNode.offsetParent,true):{x:0,y:0};
if(this.constrainToContainer){
this.constraints=this.getConstraints();
}
with(this.dragClone.style){
position="absolute";
top=this.dragOffset.y+e.pageY+"px";
left=this.dragOffset.x+e.pageX+"px";
}
dojo.body().appendChild(this.dragClone);
dojo.event.topic.publish("dragStart",{source:this});
},getConstraints:function(){
if(this.constrainingContainer.nodeName.toLowerCase()=="body"){
var _ba6=dojo.html.getViewport();
var _ba7=_ba6.width;
var _ba8=_ba6.height;
var _ba9=dojo.html.getScroll().offset;
var x=_ba9.x;
var y=_ba9.y;
}else{
var _bac=dojo.html.getContentBox(this.constrainingContainer);
_ba7=_bac.width;
_ba8=_bac.height;
x=this.containingBlockPosition.x+dojo.html.getPixelValue(this.constrainingContainer,"padding-left",true)+dojo.html.getBorderExtent(this.constrainingContainer,"left");
y=this.containingBlockPosition.y+dojo.html.getPixelValue(this.constrainingContainer,"padding-top",true)+dojo.html.getBorderExtent(this.constrainingContainer,"top");
}
var mb=dojo.html.getMarginBox(this.domNode);
return {minX:x,minY:y,maxX:x+_ba7-mb.width,maxY:y+_ba8-mb.height};
},updateDragOffset:function(){
var _bae=dojo.html.getScroll().offset;
if(_bae.y!=this.scrollOffset.y){
var diff=_bae.y-this.scrollOffset.y;
this.dragOffset.y+=diff;
this.scrollOffset.y=_bae.y;
}
if(_bae.x!=this.scrollOffset.x){
var diff=_bae.x-this.scrollOffset.x;
this.dragOffset.x+=diff;
this.scrollOffset.x=_bae.x;
}
},onDragMove:function(e){
this.updateDragOffset();
var x=this.dragOffset.x+e.pageX;
var y=this.dragOffset.y+e.pageY;
if(this.constrainToContainer){
if(x<this.constraints.minX){
x=this.constraints.minX;
}
if(y<this.constraints.minY){
y=this.constraints.minY;
}
if(x>this.constraints.maxX){
x=this.constraints.maxX;
}
if(y>this.constraints.maxY){
y=this.constraints.maxY;
}
}
this.setAbsolutePosition(x,y);
dojo.event.topic.publish("dragMove",{source:this});
},setAbsolutePosition:function(x,y){
if(!this.disableY){
this.dragClone.style.top=y+"px";
}
if(!this.disableX){
this.dragClone.style.left=x+"px";
}
},onDragEnd:function(e){
switch(e.dragStatus){
case "dropSuccess":
dojo.html.removeNode(this.dragClone);
this.dragClone=null;
break;
case "dropFailure":
var _bb6=dojo.html.getAbsolutePosition(this.dragClone,true);
var _bb7={left:this.dragStartPosition.x+1,top:this.dragStartPosition.y+1};
var anim=dojo.lfx.slideTo(this.dragClone,_bb7,300);
var _bb9=this;
dojo.event.connect(anim,"onEnd",function(e){
dojo.html.removeNode(_bb9.dragClone);
_bb9.dragClone=null;
});
anim.play();
break;
}
dojo.event.topic.publish("dragEnd",{source:this});
},constrainTo:function(_bbb){
this.constrainToContainer=true;
if(_bbb){
this.constrainingContainer=_bbb;
}else{
this.constrainingContainer=this.domNode.parentNode;
}
}},function(node,type){
this.domNode=dojo.byId(node);
this.type=type;
this.constrainToContainer=false;
this.dragSource=null;
dojo.dnd.DragObject.prototype.register.call(this);
});
dojo.declare("dojo.dnd.HtmlDropTarget",dojo.dnd.DropTarget,{vertical:false,onDragOver:function(e){
if(!this.accepts(e.dragObjects)){
return false;
}
this.childBoxes=[];
for(var i=0,_bc0;i<this.domNode.childNodes.length;i++){
_bc0=this.domNode.childNodes[i];
if(_bc0.nodeType!=dojo.html.ELEMENT_NODE){
continue;
}
var pos=dojo.html.getAbsolutePosition(_bc0,true);
var _bc2=dojo.html.getBorderBox(_bc0);
this.childBoxes.push({top:pos.y,bottom:pos.y+_bc2.height,left:pos.x,right:pos.x+_bc2.width,height:_bc2.height,width:_bc2.width,node:_bc0});
}
return true;
},_getNodeUnderMouse:function(e){
for(var i=0,_bc5;i<this.childBoxes.length;i++){
with(this.childBoxes[i]){
if(e.pageX>=left&&e.pageX<=right&&e.pageY>=top&&e.pageY<=bottom){
return i;
}
}
}
return -1;
},createDropIndicator:function(){
this.dropIndicator=document.createElement("div");
with(this.dropIndicator.style){
position="absolute";
zIndex=999;
if(this.vertical){
borderLeftWidth="1px";
borderLeftColor="black";
borderLeftStyle="solid";
height=dojo.html.getBorderBox(this.domNode).height+"px";
top=dojo.html.getAbsolutePosition(this.domNode,true).y+"px";
}else{
borderTopWidth="1px";
borderTopColor="black";
borderTopStyle="solid";
width=dojo.html.getBorderBox(this.domNode).width+"px";
left=dojo.html.getAbsolutePosition(this.domNode,true).x+"px";
}
}
},onDragMove:function(e,_bc7){
var i=this._getNodeUnderMouse(e);
if(!this.dropIndicator){
this.createDropIndicator();
}
var _bc9=this.vertical?dojo.html.gravity.WEST:dojo.html.gravity.NORTH;
var hide=false;
if(i<0){
if(this.childBoxes.length){
var _bcb=(dojo.html.gravity(this.childBoxes[0].node,e)&_bc9);
if(_bcb){
hide=true;
}
}else{
var _bcb=true;
}
}else{
var _bcc=this.childBoxes[i];
var _bcb=(dojo.html.gravity(_bcc.node,e)&_bc9);
if(_bcc.node===_bc7[0].dragSource.domNode){
hide=true;
}else{
var _bcd=_bcb?(i>0?this.childBoxes[i-1]:_bcc):(i<this.childBoxes.length-1?this.childBoxes[i+1]:_bcc);
if(_bcd.node===_bc7[0].dragSource.domNode){
hide=true;
}
}
}
if(hide){
this.dropIndicator.style.display="none";
return;
}else{
this.dropIndicator.style.display="";
}
this.placeIndicator(e,_bc7,i,_bcb);
if(!dojo.html.hasParent(this.dropIndicator)){
dojo.body().appendChild(this.dropIndicator);
}
},placeIndicator:function(e,_bcf,_bd0,_bd1){
var _bd2=this.vertical?"left":"top";
var _bd3;
if(_bd0<0){
if(this.childBoxes.length){
_bd3=_bd1?this.childBoxes[0]:this.childBoxes[this.childBoxes.length-1];
}else{
this.dropIndicator.style[_bd2]=dojo.html.getAbsolutePosition(this.domNode,true)[this.vertical?"x":"y"]+"px";
}
}else{
_bd3=this.childBoxes[_bd0];
}
if(_bd3){
this.dropIndicator.style[_bd2]=(_bd1?_bd3[_bd2]:_bd3[this.vertical?"right":"bottom"])+"px";
if(this.vertical){
this.dropIndicator.style.height=_bd3.height+"px";
this.dropIndicator.style.top=_bd3.top+"px";
}else{
this.dropIndicator.style.width=_bd3.width+"px";
this.dropIndicator.style.left=_bd3.left+"px";
}
}
},onDragOut:function(e){
if(this.dropIndicator){
dojo.html.removeNode(this.dropIndicator);
delete this.dropIndicator;
}
},onDrop:function(e){
this.onDragOut(e);
var i=this._getNodeUnderMouse(e);
var _bd7=this.vertical?dojo.html.gravity.WEST:dojo.html.gravity.NORTH;
if(i<0){
if(this.childBoxes.length){
if(dojo.html.gravity(this.childBoxes[0].node,e)&_bd7){
return this.insert(e,this.childBoxes[0].node,"before");
}else{
return this.insert(e,this.childBoxes[this.childBoxes.length-1].node,"after");
}
}
return this.insert(e,this.domNode,"append");
}
var _bd8=this.childBoxes[i];
if(dojo.html.gravity(_bd8.node,e)&_bd7){
return this.insert(e,_bd8.node,"before");
}else{
return this.insert(e,_bd8.node,"after");
}
},insert:function(e,_bda,_bdb){
var node=e.dragObject.domNode;
if(_bdb=="before"){
return dojo.html.insertBefore(node,_bda);
}else{
if(_bdb=="after"){
return dojo.html.insertAfter(node,_bda);
}else{
if(_bdb=="append"){
_bda.appendChild(node);
return true;
}
}
}
return false;
}},function(node,_bde){
if(arguments.length==0){
return;
}
this.domNode=dojo.byId(node);
dojo.dnd.DropTarget.call(this);
if(_bde&&dojo.lang.isString(_bde)){
_bde=[_bde];
}
this.acceptedTypes=_bde||[];
dojo.dnd.dragManager.registerDropTarget(this);
});
dojo.kwCompoundRequire({common:["dojo.dnd.DragAndDrop"],browser:["dojo.dnd.HtmlDragAndDrop"],dashboard:["dojo.dnd.HtmlDragAndDrop"]});
dojo.provide("dojo.dnd.*");
dojo.provide("dojo.dnd.HtmlDragMove");
dojo.declare("dojo.dnd.HtmlDragMoveSource",dojo.dnd.HtmlDragSource,{onDragStart:function(){
var _bdf=new dojo.dnd.HtmlDragMoveObject(this.dragObject,this.type);
if(this.constrainToContainer){
_bdf.constrainTo(this.constrainingContainer);
}
return _bdf;
},onSelected:function(){
for(var i=0;i<this.dragObjects.length;i++){
dojo.dnd.dragManager.selectedSources.push(new dojo.dnd.HtmlDragMoveSource(this.dragObjects[i]));
}
}});
dojo.declare("dojo.dnd.HtmlDragMoveObject",dojo.dnd.HtmlDragObject,{onDragStart:function(e){
dojo.html.clearSelection();
this.dragClone=this.domNode;
if(dojo.html.getComputedStyle(this.domNode,"position")!="absolute"){
this.domNode.style.position="relative";
}
var left=parseInt(dojo.html.getComputedStyle(this.domNode,"left"));
var top=parseInt(dojo.html.getComputedStyle(this.domNode,"top"));
this.dragStartPosition={x:isNaN(left)?0:left,y:isNaN(top)?0:top};
this.scrollOffset=dojo.html.getScroll().offset;
this.dragOffset={y:this.dragStartPosition.y-e.pageY,x:this.dragStartPosition.x-e.pageX};
this.containingBlockPosition={x:0,y:0};
if(this.constrainToContainer){
this.constraints=this.getConstraints();
}
dojo.event.connect(this.domNode,"onclick",this,"_squelchOnClick");
},onDragEnd:function(e){
},setAbsolutePosition:function(x,y){
if(!this.disableY){
this.domNode.style.top=y+"px";
}
if(!this.disableX){
this.domNode.style.left=x+"px";
}
},_squelchOnClick:function(e){
dojo.event.browser.stopEvent(e);
dojo.event.disconnect(this.domNode,"onclick",this,"_squelchOnClick");
}});
dojo.provide("dojo.widget.Dialog");
dojo.declare("dojo.widget.ModalDialogBase",null,{isContainer:true,focusElement:"",bgColor:"black",bgOpacity:0.4,followScroll:true,closeOnBackgroundClick:false,trapTabs:function(e){
if(e.target==this.tabStartOuter){
if(this._fromTrap){
this.tabStart.focus();
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabEnd.focus();
}
}else{
if(e.target==this.tabStart){
if(this._fromTrap){
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabEnd.focus();
}
}else{
if(e.target==this.tabEndOuter){
if(this._fromTrap){
this.tabEnd.focus();
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabStart.focus();
}
}else{
if(e.target==this.tabEnd){
if(this._fromTrap){
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabStart.focus();
}
}
}
}
}
},clearTrap:function(e){
var _bea=this;
setTimeout(function(){
_bea._fromTrap=false;
},100);
},postCreate:function(){
with(this.domNode.style){
position="absolute";
zIndex=999;
display="none";
overflow="visible";
}
var b=dojo.body();
b.appendChild(this.domNode);
this.bg=document.createElement("div");
this.bg.className="dialogUnderlay";
with(this.bg.style){
position="absolute";
left=top="0px";
zIndex=998;
display="none";
}
b.appendChild(this.bg);
this.setBackgroundColor(this.bgColor);
this.bgIframe=new dojo.html.BackgroundIframe();
if(this.bgIframe.iframe){
with(this.bgIframe.iframe.style){
position="absolute";
left=top="0px";
zIndex=90;
display="none";
}
}
if(this.closeOnBackgroundClick){
dojo.event.kwConnect({srcObj:this.bg,srcFunc:"onclick",adviceObj:this,adviceFunc:"onBackgroundClick",once:true});
}
},uninitialize:function(){
this.bgIframe.remove();
dojo.html.removeNode(this.bg,true);
},setBackgroundColor:function(_bec){
if(arguments.length>=3){
_bec=new dojo.gfx.color.Color(arguments[0],arguments[1],arguments[2]);
}else{
_bec=new dojo.gfx.color.Color(_bec);
}
this.bg.style.backgroundColor=_bec.toString();
return this.bgColor=_bec;
},setBackgroundOpacity:function(op){
if(arguments.length==0){
op=this.bgOpacity;
}
dojo.html.setOpacity(this.bg,op);
try{
this.bgOpacity=dojo.html.getOpacity(this.bg);
}
catch(e){
this.bgOpacity=op;
}
return this.bgOpacity;
},_sizeBackground:function(){
if(this.bgOpacity>0){
var _bee=dojo.html.getViewport();
var h=_bee.height;
var w=_bee.width;
with(this.bg.style){
width=w+"px";
height=h+"px";
}
var _bf1=dojo.html.getScroll().offset;
this.bg.style.top=_bf1.y+"px";
this.bg.style.left=_bf1.x+"px";
var _bee=dojo.html.getViewport();
if(_bee.width!=w){
this.bg.style.width=_bee.width+"px";
}
if(_bee.height!=h){
this.bg.style.height=_bee.height+"px";
}
}
this.bgIframe.size(this.bg);
},_showBackground:function(){
if(this.bgOpacity>0){
this.bg.style.display="block";
}
if(this.bgIframe.iframe){
this.bgIframe.iframe.style.display="block";
}
},placeModalDialog:function(){
var _bf2=dojo.html.getScroll().offset;
var _bf3=dojo.html.getViewport();
var mb;
if(this.isShowing()){
mb=dojo.html.getMarginBox(this.domNode);
}else{
dojo.html.setVisibility(this.domNode,false);
dojo.html.show(this.domNode);
mb=dojo.html.getMarginBox(this.domNode);
dojo.html.hide(this.domNode);
dojo.html.setVisibility(this.domNode,true);
}
var x=_bf2.x+(_bf3.width-mb.width)/2;
var y=_bf2.y+(_bf3.height-mb.height)/2;
with(this.domNode.style){
left=x+"px";
top=y+"px";
}
},_onKey:function(evt){
if(evt.key){
var node=evt.target;
while(node!=null){
if(node==this.domNode){
return;
}
node=node.parentNode;
}
if(evt.key!=evt.KEY_TAB){
dojo.event.browser.stopEvent(evt);
}else{
if(!dojo.render.html.opera){
try{
this.tabStart.focus();
}
catch(e){
}
}
}
}
},showModalDialog:function(){
if(this.followScroll&&!this._scrollConnected){
this._scrollConnected=true;
dojo.event.connect(window,"onscroll",this,"_onScroll");
}
dojo.event.connect(document.documentElement,"onkey",this,"_onKey");
this.placeModalDialog();
this.setBackgroundOpacity();
this._sizeBackground();
this._showBackground();
this._fromTrap=true;
setTimeout(dojo.lang.hitch(this,function(){
try{
this.tabStart.focus();
}
catch(e){
}
}),50);
},hideModalDialog:function(){
if(this.focusElement){
dojo.byId(this.focusElement).focus();
dojo.byId(this.focusElement).blur();
}
this.bg.style.display="none";
this.bg.style.width=this.bg.style.height="1px";
if(this.bgIframe.iframe){
this.bgIframe.iframe.style.display="none";
}
dojo.event.disconnect(document.documentElement,"onkey",this,"_onKey");
if(this._scrollConnected){
this._scrollConnected=false;
dojo.event.disconnect(window,"onscroll",this,"_onScroll");
}
},_onScroll:function(){
var _bf9=dojo.html.getScroll().offset;
this.bg.style.top=_bf9.y+"px";
this.bg.style.left=_bf9.x+"px";
this.placeModalDialog();
},checkSize:function(){
if(this.isShowing()){
this._sizeBackground();
this.placeModalDialog();
this.onResized();
}
},onBackgroundClick:function(){
if(this.lifetime-this.timeRemaining>=this.blockDuration){
return;
}
this.hide();
}});
dojo.widget.defineWidget("dojo.widget.Dialog",[dojo.widget.ContentPane,dojo.widget.ModalDialogBase],{templatePath:dojo.uri.moduleUri("dojo.widget","templates/Dialog.html"),blockDuration:0,lifetime:0,closeNode:"",postMixInProperties:function(){
dojo.widget.Dialog.superclass.postMixInProperties.apply(this,arguments);
if(this.closeNode){
this.setCloseControl(this.closeNode);
}
},postCreate:function(){
dojo.widget.Dialog.superclass.postCreate.apply(this,arguments);
dojo.widget.ModalDialogBase.prototype.postCreate.apply(this,arguments);
},show:function(){
if(this.lifetime){
this.timeRemaining=this.lifetime;
if(this.timerNode){
this.timerNode.innerHTML=Math.ceil(this.timeRemaining/1000);
}
if(this.blockDuration&&this.closeNode){
if(this.lifetime>this.blockDuration){
this.closeNode.style.visibility="hidden";
}else{
this.closeNode.style.display="none";
}
}
if(this.timer){
clearInterval(this.timer);
}
this.timer=setInterval(dojo.lang.hitch(this,"_onTick"),100);
}
this.showModalDialog();
dojo.widget.Dialog.superclass.show.call(this);
},onLoad:function(){
this.placeModalDialog();
dojo.widget.Dialog.superclass.onLoad.call(this);
},fillInTemplate:function(){
},hide:function(){
this.hideModalDialog();
dojo.widget.Dialog.superclass.hide.call(this);
if(this.timer){
clearInterval(this.timer);
}
},setTimerNode:function(node){
this.timerNode=node;
},setCloseControl:function(node){
this.closeNode=dojo.byId(node);
dojo.event.connect(this.closeNode,"onclick",this,"hide");
},setShowControl:function(node){
node=dojo.byId(node);
dojo.event.connect(node,"onclick",this,"show");
},_onTick:function(){
if(this.timer){
this.timeRemaining-=100;
if(this.lifetime-this.timeRemaining>=this.blockDuration){
if(this.closeNode){
this.closeNode.style.visibility="visible";
}
}
if(!this.timeRemaining){
clearInterval(this.timer);
this.hide();
}else{
if(this.timerNode){
this.timerNode.innerHTML=Math.ceil(this.timeRemaining/1000);
}
}
}
}});
dojo.provide("dojo.widget.ResizeHandle");
dojo.widget.defineWidget("dojo.widget.ResizeHandle",dojo.widget.HtmlWidget,{targetElmId:"",templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/ResizeHandle.css"),templateString:"<div class=\"dojoHtmlResizeHandle\"><div></div></div>",postCreate:function(){
dojo.event.connect(this.domNode,"onmousedown",this,"_beginSizing");
},_beginSizing:function(e){
if(this._isSizing){
return false;
}
this.targetWidget=dojo.widget.byId(this.targetElmId);
this.targetDomNode=this.targetWidget?this.targetWidget.domNode:dojo.byId(this.targetElmId);
if(!this.targetDomNode){
return;
}
this._isSizing=true;
this.startPoint={"x":e.clientX,"y":e.clientY};
var mb=dojo.html.getMarginBox(this.targetDomNode);
this.startSize={"w":mb.width,"h":mb.height};
dojo.event.kwConnect({srcObj:dojo.body(),srcFunc:"onmousemove",targetObj:this,targetFunc:"_changeSizing",rate:25});
dojo.event.connect(dojo.body(),"onmouseup",this,"_endSizing");
e.preventDefault();
},_changeSizing:function(e){
try{
if(!e.clientX||!e.clientY){
return;
}
}
catch(e){
return;
}
var dx=this.startPoint.x-e.clientX;
var dy=this.startPoint.y-e.clientY;
var newW=this.startSize.w-dx;
var newH=this.startSize.h-dy;
if(this.minSize){
var mb=dojo.html.getMarginBox(this.targetDomNode);
if(newW<this.minSize.w){
newW=mb.width;
}
if(newH<this.minSize.h){
newH=mb.height;
}
}
if(this.targetWidget){
this.targetWidget.resizeTo(newW,newH);
}else{
dojo.html.setMarginBox(this.targetDomNode,{width:newW,height:newH});
}
e.preventDefault();
},_endSizing:function(e){
dojo.event.disconnect(dojo.body(),"onmousemove",this,"_changeSizing");
dojo.event.disconnect(dojo.body(),"onmouseup",this,"_endSizing");
this._isSizing=false;
}});
dojo.provide("dojo.widget.FloatingPane");
dojo.declare("dojo.widget.FloatingPaneBase",null,{title:"",iconSrc:"",hasShadow:false,constrainToContainer:false,taskBarId:"",resizable:true,titleBarDisplay:true,windowState:"normal",displayCloseAction:false,displayMinimizeAction:false,displayMaximizeAction:false,_max_taskBarConnectAttempts:5,_taskBarConnectAttempts:0,templatePath:dojo.uri.moduleUri("dojo.widget","templates/FloatingPane.html"),templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/FloatingPane.css"),fillInFloatingPaneTemplate:function(args,frag){
var _c08=this.getFragNodeRef(frag);
dojo.html.copyStyle(this.domNode,_c08);
dojo.body().appendChild(this.domNode);
if(!this.isShowing()){
this.windowState="minimized";
}
if(this.iconSrc==""){
dojo.html.removeNode(this.titleBarIcon);
}else{
this.titleBarIcon.src=this.iconSrc.toString();
}
if(this.titleBarDisplay){
this.titleBar.style.display="";
dojo.html.disableSelection(this.titleBar);
this.titleBarIcon.style.display=(this.iconSrc==""?"none":"");
this.minimizeAction.style.display=(this.displayMinimizeAction?"":"none");
this.maximizeAction.style.display=(this.displayMaximizeAction&&this.windowState!="maximized"?"":"none");
this.restoreAction.style.display=(this.displayMaximizeAction&&this.windowState=="maximized"?"":"none");
this.closeAction.style.display=(this.displayCloseAction?"":"none");
this.drag=new dojo.dnd.HtmlDragMoveSource(this.domNode);
if(this.constrainToContainer){
this.drag.constrainTo();
}
this.drag.setDragHandle(this.titleBar);
var self=this;
dojo.event.topic.subscribe("dragMove",function(info){
if(info.source.domNode==self.domNode){
dojo.event.topic.publish("floatingPaneMove",{source:self});
}
});
}
if(this.resizable){
this.resizeBar.style.display="";
this.resizeHandle=dojo.widget.createWidget("ResizeHandle",{targetElmId:this.widgetId,id:this.widgetId+"_resize"});
this.resizeBar.appendChild(this.resizeHandle.domNode);
}
if(this.hasShadow){
this.shadow=new dojo.lfx.shadow(this.domNode);
}
this.bgIframe=new dojo.html.BackgroundIframe(this.domNode);
if(this.taskBarId){
this._taskBarSetup();
}
dojo.body().removeChild(this.domNode);
},postCreate:function(){
if(dojo.hostenv.post_load_){
this._setInitialWindowState();
}else{
dojo.addOnLoad(this,"_setInitialWindowState");
}
},maximizeWindow:function(evt){
var mb=dojo.html.getMarginBox(this.domNode);
this.previous={width:mb.width||this.width,height:mb.height||this.height,left:this.domNode.style.left,top:this.domNode.style.top,bottom:this.domNode.style.bottom,right:this.domNode.style.right};
if(this.domNode.parentNode.style.overflow.toLowerCase()!="hidden"){
this.parentPrevious={overflow:this.domNode.parentNode.style.overflow};
dojo.debug(this.domNode.parentNode.style.overflow);
this.domNode.parentNode.style.overflow="hidden";
}
this.domNode.style.left=dojo.html.getPixelValue(this.domNode.parentNode,"padding-left",true)+"px";
this.domNode.style.top=dojo.html.getPixelValue(this.domNode.parentNode,"padding-top",true)+"px";
if((this.domNode.parentNode.nodeName.toLowerCase()=="body")){
var _c0d=dojo.html.getViewport();
var _c0e=dojo.html.getPadding(dojo.body());
this.resizeTo(_c0d.width-_c0e.width,_c0d.height-_c0e.height);
}else{
var _c0f=dojo.html.getContentBox(this.domNode.parentNode);
this.resizeTo(_c0f.width,_c0f.height);
}
this.maximizeAction.style.display="none";
this.restoreAction.style.display="";
if(this.resizeHandle){
this.resizeHandle.domNode.style.display="none";
}
this.drag.setDragHandle(null);
this.windowState="maximized";
},minimizeWindow:function(evt){
this.hide();
for(var attr in this.parentPrevious){
this.domNode.parentNode.style[attr]=this.parentPrevious[attr];
}
this.lastWindowState=this.windowState;
this.windowState="minimized";
},restoreWindow:function(evt){
if(this.windowState=="minimized"){
this.show();
if(this.lastWindowState=="maximized"){
this.domNode.parentNode.style.overflow="hidden";
this.windowState="maximized";
}else{
this.windowState="normal";
}
}else{
if(this.windowState=="maximized"){
for(var attr in this.previous){
this.domNode.style[attr]=this.previous[attr];
}
for(var attr in this.parentPrevious){
this.domNode.parentNode.style[attr]=this.parentPrevious[attr];
}
this.resizeTo(this.previous.width,this.previous.height);
this.previous=null;
this.parentPrevious=null;
this.restoreAction.style.display="none";
this.maximizeAction.style.display=this.displayMaximizeAction?"":"none";
if(this.resizeHandle){
this.resizeHandle.domNode.style.display="";
}
this.drag.setDragHandle(this.titleBar);
this.windowState="normal";
}else{
}
}
},toggleDisplay:function(){
if(this.windowState=="minimized"){
this.restoreWindow();
}else{
this.minimizeWindow();
}
},closeWindow:function(evt){
dojo.html.removeNode(this.domNode);
this.destroy();
},onMouseDown:function(evt){
this.bringToTop();
},bringToTop:function(){
var _c16=dojo.widget.manager.getWidgetsByType(this.widgetType);
var _c17=[];
for(var x=0;x<_c16.length;x++){
if(this.widgetId!=_c16[x].widgetId){
_c17.push(_c16[x]);
}
}
_c17.sort(function(a,b){
return a.domNode.style.zIndex-b.domNode.style.zIndex;
});
_c17.push(this);
var _c1b=100;
for(x=0;x<_c17.length;x++){
_c17[x].domNode.style.zIndex=_c1b+x*2;
}
},_setInitialWindowState:function(){
if(this.isShowing()){
this.width=-1;
var mb=dojo.html.getMarginBox(this.domNode);
this.resizeTo(mb.width,mb.height);
}
if(this.windowState=="maximized"){
this.maximizeWindow();
this.show();
return;
}
if(this.windowState=="normal"){
this.show();
return;
}
if(this.windowState=="minimized"){
this.hide();
return;
}
this.windowState="minimized";
},_taskBarSetup:function(){
var _c1d=dojo.widget.getWidgetById(this.taskBarId);
if(!_c1d){
if(this._taskBarConnectAttempts<this._max_taskBarConnectAttempts){
dojo.lang.setTimeout(this,this._taskBarSetup,50);
this._taskBarConnectAttempts++;
}else{
dojo.debug("Unable to connect to the taskBar");
}
return;
}
_c1d.addChild(this);
},showFloatingPane:function(){
this.bringToTop();
},onFloatingPaneShow:function(){
var mb=dojo.html.getMarginBox(this.domNode);
this.resizeTo(mb.width,mb.height);
},resizeTo:function(_c1f,_c20){
dojo.html.setMarginBox(this.domNode,{width:_c1f,height:_c20});
dojo.widget.html.layout(this.domNode,[{domNode:this.titleBar,layoutAlign:"top"},{domNode:this.resizeBar,layoutAlign:"bottom"},{domNode:this.containerNode,layoutAlign:"client"}]);
dojo.widget.html.layout(this.containerNode,this.children,"top-bottom");
this.bgIframe.onResized();
if(this.shadow){
this.shadow.size(_c1f,_c20);
}
this.onResized();
},checkSize:function(){
},destroyFloatingPane:function(){
if(this.resizeHandle){
this.resizeHandle.destroy();
this.resizeHandle=null;
}
}});
dojo.widget.defineWidget("dojo.widget.FloatingPane",[dojo.widget.ContentPane,dojo.widget.FloatingPaneBase],{fillInTemplate:function(args,frag){
this.fillInFloatingPaneTemplate(args,frag);
dojo.widget.FloatingPane.superclass.fillInTemplate.call(this,args,frag);
},postCreate:function(){
dojo.widget.FloatingPaneBase.prototype.postCreate.apply(this,arguments);
dojo.widget.FloatingPane.superclass.postCreate.apply(this,arguments);
},show:function(){
dojo.widget.FloatingPane.superclass.show.apply(this,arguments);
this.showFloatingPane();
},onShow:function(){
dojo.widget.FloatingPane.superclass.onShow.call(this);
this.onFloatingPaneShow();
},destroy:function(){
this.destroyFloatingPane();
dojo.widget.FloatingPane.superclass.destroy.apply(this,arguments);
}});
dojo.widget.defineWidget("dojo.widget.ModalFloatingPane",[dojo.widget.FloatingPane,dojo.widget.ModalDialogBase],{windowState:"minimized",displayCloseAction:true,postCreate:function(){
dojo.widget.ModalDialogBase.prototype.postCreate.call(this);
dojo.widget.ModalFloatingPane.superclass.postCreate.call(this);
},show:function(){
this.showModalDialog();
dojo.widget.ModalFloatingPane.superclass.show.apply(this,arguments);
this.bg.style.zIndex=this.domNode.style.zIndex-1;
},hide:function(){
this.hideModalDialog();
dojo.widget.ModalFloatingPane.superclass.hide.apply(this,arguments);
},closeWindow:function(){
this.hide();
dojo.widget.ModalFloatingPane.superclass.closeWindow.apply(this,arguments);
}});

