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
					templateUrl: 'templates/editor.html',
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
			},
			update: function(note) {
				return $http.post(SERVER_URL + '/notes', note)
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

	.controller('NoteEditCtrl', function($scope, Notes, $stateParams, $interval ) {
		config = {sharedSpaces : {top: 'toolbar'}};
		$scope.editor = CKEDITOR.replace( 'editor', config );
		var autosave = undefined;
		
		$scope.saveState = 'saved';
			
		$scope.editor.on('change', function() {
			console.log('change');
			if($scope.editor.checkDirty()) {
				$scope.saveState = 'unsaved';
				$scope.$apply();
			}
		});
			
		$scope.doRefresh = function() {
			Notes.detail($stateParams.id).then(function(result) {
				$scope.editor.setData(result.data.text)
				$scope.title = result.data.title
				if(autosave == undefined) {
					autosave = $interval($scope.save, 30 * 1000)
				}
			},
			function() {
				
			})
		}
	
		$scope.save = function() {
			// TODO add dirty checking of title and text
			var id = Number($stateParams.id)
			var note = {id: id, title: $scope.title, text: $scope.editor.getData()}
			console.log("save: " + JSON.stringify(note))
			// TODO reset dirty for title and text
			Notes.update(note).then(function(response) {
				$scope.editor.resetDirty();
				$scope.saveState = 'saved';
				console.log(JSON.stringify(response));
				$scope.$apply();
			});
			return;
		}
		
		$scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
			if(toState.name == 'app.note') {
				$scope.doRefresh()
			} else if(fromState.name == 'app.note' && autosave != undefined) {
				$interval.cancel(autosave)
				autosave = undefined
			}
		})

})