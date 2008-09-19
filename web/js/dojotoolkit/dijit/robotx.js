if(!dojo._hasResource["dijit.robotx"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.robotx"] = true;
dojo.provide("dijit.robotx");
dojo.require("dijit.robot");
dojo.require("dojo.robotx");
dojo.experimental("dijit.robotx");
(function(){
var __updateDocument = doh.robot._updateDocument;

dojo.mixin(doh.robot,{
	_updateDocument: function(){
		__updateDocument();
		var win = (dojo.doc.parentWindow || dojo.doc.defaultView);
		if(win["dijit"]){
			dijit.registry = win.dijit.registry;
		}
	}
});

})();

}
