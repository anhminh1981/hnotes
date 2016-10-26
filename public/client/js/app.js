angular.module('hnotes', ['ionic', 'hnotes.controllers', 'hnotes.config', 'hnotes.filters', 'hnotes.auth', 'hnotes.notes'])

.run(function($ionicPlatform, $ionicPopup, $rootScope, $window, $state, $location, SERVER_URL) {
	
	
	$ionicPlatform.ready(function() {
		// Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
		// for form inputs)
		if (window.cordova && window.cordova.plugins.Keyboard) {
			cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
			cordova.plugins.Keyboard.disableScroll(true);

		}
		if (window.StatusBar) {
			// org.apache.cordova.statusbar required
			StatusBar.styleDefault();
		}
	});


	// Re-route to welcome street if we don't have an authenticated token
	$rootScope.$on('$stateChangeStart', function(event, toState) {
		var emptyToken
		console.log("going to " + toState.name)
		if($window.localStorage.getItem('token')) {
			emptyToken = false
		} else {
			emptyToken = true
		}
		if (toState.name !== 'auth' && toState.name !== 'app.logout' && !$window.localStorage.token) {
			console.log('Aborting state ' + toState.name + ': No token');
			event.preventDefault();
			$state.go('auth')
		}
	});

	$rootScope.$on('responseError', function(event, response) {
		var alertPopup = $ionicPopup.alert({
			title: 'Error',
			template: 'There was a problem contacting the server. Please retry later.'
		});

		alertPopup.then(function(res) {
			console.log('Error: ' + JSON.stringify(response));
		});
	})
	
	$state.go('app.notes');

})

.config(function($stateProvider, $urlRouterProvider) {
	
	
	$stateProvider

	.state('app', {
		url: '/app',
		abstract: true,
		templateUrl: 'templates/menu.html',
		controller: 'AppCtrl'
	})

	.state('app.search', {
		url: '/search',
		views: {
			'menuContent': {
				templateUrl: 'templates/search.html'
			}
		}
	})

	;



	// if none of the above states are matched, use this as the fallback
	$urlRouterProvider.otherwise('/auth');
} )

;
