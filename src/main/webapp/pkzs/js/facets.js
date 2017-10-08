$(function() {
	function updateUrl(params) {
		window.location.search = "?" + enilink.encodeParams(params);
	}

	$(".filter-add").click(function(e) {
		var target = $(e.currentTarget);
		var value = target.closest("[about]").attr("about");
		value = $.rdf.resource(value, {
			namespaces : target.xmlns()
		}).value;
		var name = target.attr("data-param");
		var params = enilink.params();
		delete params["offset"];
		params[name] = value;
	});
	$(".filter-remove").click(function(e) {
		var target = $(e.currentTarget);
		var name = target.attr("data-param");
		var params = enilink.params();
		delete params[name];
		updateUrl(params);
	});
});