'use strict';


// Declare app level module which depends on filters, and services
angular.module("musikjunker-admin", []);

angular.module("musikjunker-admin")
	.controller("MainController", function($http, $scope){
	
		$scope.getState = function() {
			$http.get("services/admin/reindex")
				.success(function(data){
					if (data == '') {
						data = null;
					}
					$scope.currentState = data;
				});
		};
		$scope.getState();
		
		
		$scope.launch = function() {
			$http.post("services/admin/reindex")
			.success(function(data){
				if (data.error) {
					alert("Error: " + data.message);
				} else {
					$scope.currentState = data;
				}
			});
		};
		
		$scope.stop = function() {
			$http.delete("services/admin/reindex")
				.success(function(data){
					alert("Stopping requested");
				});
		};
		
});