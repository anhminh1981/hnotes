angular.module('hnotes.auth', [])
	.config(function ($stateProvider) {
		$stateProvider
			.state('auth', {
				url: '/auth',
				templateUrl: 'templates/auth.html',
				controller: 'AuthCtrl'
			}) 
	})
	.factory('Auth', function($http, $window, $rootScope) {
		var loggedIn = function(response) {
			if(response.data.status == 'OK') {
				$window.localStorage.token =  response.token 
				$rootScope.user = response.user
				$window.localStorage.user = JSON.stringify(response.user) 
			}
			return response.data
			
		}
		
		var errLogin = function(response) {
			return {status: 'KO', cause: response.data.cause || "couldn't connect to server"}
		}
		return {
			login: function(loginData) { 
				return $http.post($rootScope.server + '/login', loginData).then(loggedIn, errLogin)
			},
			signup: function(signupData) { 
				return $http.post($rootScope.server + '/signup', signupData).then(loggedIn, errLogin)
			}
		} 
	})
	.controller('AuthCtrl', function($scope, $rootScope, $state, Auth) {
		$scope.doLogin = function(loginData) { 
			Auth.login(loginData).then(function(result) { 
				if(result.status == 'KO') { 
					$scope.signupError = result.cause;
				} else { 
					$state.go('app.playlists');
				}
			})
		}
		
		$scope.doSignup = function(signupData) { 
			Auth.signup({email: signupData.email, password: signupData.password}).then(function(result) { 
				if(result.status == 'KO') { 
					$scope.signupError = result.cause;
				} else { 
					$state.go('app.playlists');
				}
			})
		}
		
		
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