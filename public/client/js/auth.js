angular.module('hnotes.auth', ['hnotes.config'])

	.config(function ($stateProvider) {
		$stateProvider
			.state('auth', {
				url: '/auth',
				templateUrl: 'templates/auth.html',
				controller: 'AuthCtrl'
			}) 
			.state('logout', {
				url: '/logout',
				controller: 'LogoutCtrl'
			}) 
	})
	
	.factory('Auth', function($http, $window, $rootScope, SERVER_URL) {
		var loggedIn = function(response) {
			console.log(JSON.stringify(response))
			if(response.data.status == 'OK') {
				$window.localStorage.token =  response.data.token 
				$rootScope.user = response.data.user
				$window.localStorage.user = JSON.stringify(response.data.user) 
			}
			return response.data
			
		}
		
		var errLogin = function(response) {
			return {status: 'KO', cause: response.data.cause || "couldn't connect to server"}
		}
		return {
			login: function(loginData) { 
				return $http.post(SERVER_URL + '/login', loginData).then(loggedIn, errLogin)
			},
			signup: function(signupData) { 
				return $http.post(SERVER_URL + '/signup', signupData).then(loggedIn, errLogin)
			},
			logout: function() {
				$window.localStorage.removeItem('token')
				$window.localStorage.removeItem('user')
			}
		} 
	})
	.controller('AuthCtrl', function($scope, $state, Auth) {
		
		$scope.doLogin = function(loginData) { 
			Auth.login(loginData).then(function(result) { 
				if(result.status == 'KO') { 
					$scope.loginError = result.cause;
				} else { 
					$state.go('app.notes');
				}
			})
		}
		
		$scope.doSignup = function(signupData) { 
			Auth.signup({email: signupData.email, password: signupData.password}).then(function(result) { 
				if(result.status == 'KO') { 
					$scope.signupError = result.cause;
				} else { 
					$state.go('app.notes');
				}
			})
		}
		
		
		
		
	})
	.controller('LogoutCtrl', function($scope, $state, Auth) {
		
		$scope.$on('$stateChangeSuccess', function(event, toState) {
			if(toState.name == 'logout') {
				console.log("logging out")
				Auth.logout()
				$state.go('auth')
			}
		})
		
		
	})
	
	.directive("compareTo", function() { 
		return {
	        require: "ngModel",
	        scope: {
	            otherModelValue: "=compareTo"
	        },
	        link: function(scope, element, attributes, ngModel) {
	             
	            ngModel.$validators.compareTo = function(modelValue, viewValue) {
	                return modelValue == scope.otherModelValue;
	            };
	 
	            scope.$watch("otherModelValue", function() {
	                ngModel.$validate();
	            });
	        }
	    };
	});