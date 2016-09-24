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
			.state('app.note', {
			url: '/notes/:id',
			views: {
				'menuContent': {
					templateUrl: 'templates/note-edit.html',
					controller: 'NoteEditCtrl'

				}
			}
		})
	})
	.factory('Notes', function($http, SERVER_URL) {
		return {
			list: function() {
				return $http.get(SERVER_URL + '/notes')
			},
			detail: function(id) {
				return $http.get(SERVER_URL + '/notes/' + id)
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
		
		$scope.$on('$stateChangeSuccess', function(event, toState) {
			if(toState.name == 'app.notes') {
				$scope.doRefresh()
			}
		})
		
	})

	.controller('NoteEditCtrl', function($scope, Notes, $stateParams ) {
		$scope.doRefresh() = function() {
			Notes.
		}
	
	
		$scope.$on('$stateChangeSuccess', function(event, toState) {
			if(toState.name == 'app.note') {
				$scope.doRefresh()
			}
		})

})