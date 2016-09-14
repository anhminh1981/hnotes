angular.module('hnotes.notes', ['hnotes.config'])
	.config(function ($stateProvider) {
		$stateProvider
			.state('app.notes', {
				url: '/notes',
				views: {
					'menuContent': {
						templateUrl: 'templates/notes.html',
						controller: 'NotesListCtrl'
							
					}
				}
			})
	})
	.factory('Notes', function($http, SERVER_URL) {
		return {
			list: function() {
				return $http.get(SERVER_URL + '/notes')
			}
		}
	})
	
	.controller('NotesListCtrl', function($scope, Notes) {
		$scope.notes = []
		$scope.doRefresh = function() {
			console.log("doRefresh")
			Notes.list().then(function(result) {
				console.log(JSON.stringify(result))
				$scope.notes = result.data.notes
			} ).finally(function() {
			       // Stop the ion-refresher from spinning
			       $scope.$broadcast('scroll.refreshComplete');
			     })
		}
		
		$scope.doRefresh()
		
		
	})