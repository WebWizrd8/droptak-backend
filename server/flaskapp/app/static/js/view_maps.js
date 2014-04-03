ko.validation.rules['unique'] = {
    validator: function (val, otherVal) {
    	/*eventually use to check if item exists
			unique: true,
    	*/
        return val === otherVal;
    },
    message: 'The field must equal {0}'
};
ko.validation.registerExtenders();
/* setup ko validation */
ko.validation.init( {
	decorateInputElement: true,
	errorElementClass: 'invalid',
	insertMessages: true

});

/**
* generate uids for data-binding in dom
*/
ko.bindingHandlers.uniqueId = {
    init: function(element, valueAccessor) {
        var value = valueAccessor();
        value.cuid = value.cuid || ko.bindingHandlers.uniqueId.prefix + (++ko.bindingHandlers.uniqueId.counter);

        element.id = value.cuid;
    },
    counter: 0,
    prefix: "unique"
};

ko.bindingHandlers.uniqueFor = {
    init: function(element, valueAccessor) {
        var value = valueAccessor();
        value.cuid = value.cuid || ko.bindingHandlers.uniqueId.prefix + (++ko.bindingHandlers.uniqueId.counter);

        element.setAttribute("for", value.cuid);
    } 
};
ko.bindingHandlers.uniquePopover = {
    init: function(element, valueAccessor) {
        var value = valueAccessor();
        value.cuid = value.cuid || ko.bindingHandlers.uniqueId.prefix + (++ko.bindingHandlers.uniqueId.counter);
        value.popcuid = "popover" + value.cuid;
        element.id = "popover" + value.cuid;
    } 
};
ko.bindingHandlers.urlFor = {
    update: function(element, valueAccessor) {
        // This will be called once when the binding is first applied to an element,
        // and again whenever the associated observable changes value.
        // Update the DOM element based on the supplied values here.
        var value = valueAccessor();
        element.setAttribute("href", "/maps/" + value.id());

    }
};
ko.bindingHandlers.qrcodify = {
    init: function(element, valueAccessor, allBindingsAccessor, viewModel) {
    		$(element).empty();
        $(element).qrcode({
					text: "mapitapps.appspot.com/maps/"+ (valueAccessor().id() || "") 
				});        
    },
    update: function(element, valueAccessor, allBindingsAccessor, viewModel) {
       	$(element).empty();
        $(element).qrcode({
					text: "mapitapps.appspot.com/maps/"+ (valueAccessor().id() || "") 
				});        
    }
};

ko.bindingHandlers.qrcodePopover = {
    init: function(element, valueAccessor, allBindingsAccessor, viewModel) {
        var mapcuid = valueAccessor().popcuid;
        console.log(mapcuid);
        var options = {};
        options.content = $("#" + mapcuid).html();
        options.html = true;
        options.placement = "auto top";
        var defaultOptions = {};   
        options = $.extend(true, {}, defaultOptions, options);
        $(element).popover(options);
    },
     update: function(element, valueAccessor, allBindingsAccessor, viewModel) {
        var mapcuid = valueAccessor().popcuid;
        console.log(mapcuid);
        var options = {};
        options.content = $(element).qrcode({
					text: "mapitapps.appspot.com/maps/"+ (valueAccessor().id() || "") 
				}).html();
        options.html = true;
        options.placement = "auto top";
        var defaultOptions = {};   
        options = $.extend(true, {}, defaultOptions, options);
        $(element).popover(options);
    }
};
/**
*	Knockout models for data binding
*/

function Tak(){
	var self 				= this;
	self.title      = ko.observable();
	self.lat        = ko.observable();
	self.lng  			= ko.observable();

}
function FormMap(){
	var self = this;
	self.name = ko.observable().extend({
		required: true,
		minLength: 1
	});
}
function Map(){
	var self        = this;
	self.name      = ko.observable();
	self.id 				= ko.observable();
	self.taks  			= ko.observableArray();

	self.addTak = function(){
			var tak = new Tak();
			self.taks.push( tak);
			return tak;
		}
	self.removeTak = function(tak){

			self.taks.remove(tak);
		}

}
/**
*	Highest level model for entire app
*/
function MapTakModel() {
	var self      	= this;
	self.maps 	  	= ko.observableArray();
	self.selected		= ko.observable();
	self.form 			= new FormMap();

	self.select = function(element, mapid){
		self.selected(element);
		console.log(element.id());
		$.getJSON("/api/maps/" + element.id(), function(data) { 
	    element.taks().length = 0;
	    for (var i = 0; i < data.length; i++) { 
	    	var tak = element.addTak();
	    	tak.title(data[i].title);
	    	tak.lat(data[i].lat);
	    	tak.lng(data[i].lng);
			}
			console.log(element.taks());
			setMarkers(ko.toJS(element.taks ));
		});
		
	}

	/**
	* add a new map
	*/
	self.addMap= function() { 
			var map = new Map();
			self.maps.push(map);
			return map;
		};

	self.submit = function(modal){
			// check if form is valid
			console.log("submit");
			console.log(self.form.name());
			if(self.form.name.isValid()){
				console.log("valid");
				// if it is, then submit it to server
				$.post('/maps/new',{name: self.form.name()}) 
					.done(function(response) {
				    	// parse JSON text response
				    	var obj = jQuery.parseJSON(response);;
				    	//and display it locally
				    	var map = self.addMap();
				    	map.name(obj.name);
				    	map.id(obj.id);
				    	// reset form and hide it
				    	self.form.name(undefined);
							self.form.name.isModified(false);
							$(modal).modal("hide");
					})
					.fail(function() {
						alert( "Error submitting" );
					});
			}
		};

	/**
	* remove a map from model
	*/
	self.removeMap = function(map) {
			//send delete to server
			if(!confirm("This cannot be undone (yet). Delete?")) return;
			$.ajax({
		    url: '/api/maps/' + map.id(),
		    type: 'DELETE',
		    success: function(result) {
		        self.maps.remove(map);
		        console.log(result);
		    }
			});
		};

	$.getJSON("/maps", function(data) { 
	    // Now use this data to update your view models, 
	    // and Knockout will update your UI automatically 
	    for (var i = 0; i < data.length; i++) { 
	    	var local = self.addMap();
	    	local.name(data[i].name);
	    	local.id(data[i].id)
			}
		});
}

/**
*	bind data after page is loaded
*/
$(document).ready(function() {
	ko.applyBindings( new MapTakModel());
});

