function createElement(target, options) {
	function asFunc(obj) {
		return typeof obj === "function" ? obj : function() {
			return obj;
		};
	}
	function doCreate(name) {
		var name = name.replace(/\s+/g, "_");
		var instanceTemplate = asFunc(options.instanceTemplate)();
		var renderTemplate = asFunc(options.renderTemplate);

		var uri = options.valueToUri(name);
		var d = new $.Deferred;
		function render() {
			enilink.render(renderTemplate(uri), {
				model : enilink.param("model")
			}, function(html) {
				d.resolve(html);
			});
		}
		function createAndRender() {
			var rdf = instanceTemplate.attr("about", uri).rdf().databank.dump();
			enilink.rdf.updateTriples(rdf, null, function(success) {
				if (!success) {
					d.reject("Anlegen des Elements ist fehlgeschlagen.");
				} else {
					render();
				}
				return false;
			});

		}
		enilink.render($("<div>").append(instanceTemplate).html(), {
			model : enilink.param("model"),
			resource : uri
		}, function(html) {
			if (html) {
				d.reject("Das Element '" + name + "' existiert bereits.");
			} else {
				createAndRender();
			}
		});
		return d.promise();
	}

	var self = $(target);
	var editableOptions = {
		type : "text",
		onblur : "ignore",
		inputclass : "input-xxlarge",
		mode : "inline",
		toggle : "manual"
	};
	editableOptions = $.extend(editableOptions, {
		display : false,
		success : function(html) {
			if (html) {
				// insert element
			}
		},
		value : "",
		title : "Neues Element",
		url : function(params) {
			return doCreate(params.value);
		}
	}, options.editableOptions || {});
	self.editable(editableOptions);
	self.editable("toggle");
}

function toposort(edges) {
	var nodes = {}, // hash: stringified id of the node => { id: id, successors:
	// list of ids }
	sorted = [], // sorted list of IDs ( returned value )
	visited = {}; // hash: id of already visited node => true

	var Node = function(id) {
		this.id = id;
		this.successors = [];
	}

	// 1. build data structures
	edges.forEach(function(v) {
		var from = v[0], to = v[1];
		if (!nodes[from])
			nodes[from] = new Node(from);
		if (!nodes[to])
			nodes[to] = new Node(to);
		nodes[from].successors.push(to);
	});

	// 2. topological sort
	Object.keys(nodes).forEach(function visit(idstr, ancestors) {
		var node = nodes[idstr], id = node.id;

		// if already exists, do nothing
		if (visited[idstr])
			return;

		if (!Array.isArray(ancestors))
			ancestors = [];

		ancestors.push(id);

		visited[idstr] = true;

		node.successors.forEach(function(afterID) {
			if (ancestors.indexOf(afterID) >= 0) // if already in ancestors,
				// a closed chain exists.
				if (window.console) {
					console.log('[toposort] closed chain: ' + afterID + ' is already predecessor of ' + id);
					return;
				}
			visit(afterID.toString(), ancestors.map(function(v) {
				return v;
			})); // recursive call
		});

		sorted.unshift(id);
	});

	return sorted;
}