angular.module('hnotes.auth', [])
	.config(function ($stateProvider) {
		$stateProvider
			.state('auth', {
				url: '/auth',
				templateUrl: 'templates/auth.html',
				controller: 'AuthCtrl'
			}) ;
	})
	.factory('Auth', function($http, $window, $rootScope) {
		return {
			login: function(loginData) { 
				return $http.post($rootScope.server + '/login', loginData)
			},
			signup: function(signupData) { 
				return $http.post($rootScope.server + '/signup', signupData)
			}
		} 
	})
	.controller('AuthCtrl', function($scope, $rootScope, $state, Auth) {
		$scope.doLogin = function(loginData) { 
			console.log(JSON.stringify(loginData));
			Auth.login(loginData).success(function(result) { 
				if(result.status == 'KO') { 
					$scope.signupError = result.cause;
				} else { 
					loggedIn(result);
				}
			}).catch(function() { 
				$scope.signupError = "couldn't connect to server";
			}
			);
		}
		
		$scope.doSignup = function(signupData) { 
			console.log(JSON.stringify(signupData));
			Auth.signup({email: signupData.email, password: signupData.password}).success(function(result) { 
				if(result.status == 'KO') { 
					$scope.signupError = result.cause;
				} else { 
					loggedIn(result);
				}
			}).catch(function() { 
				$scope.signupError = "couldn't connect to server";
			}
			);
		}
		
		var loggedIn = function(result) {
			$window.localStorage.token =  result.token ;
			$rootScope.user = result.user;
			$window.localStorage.user = JSON.stringify(result.user) ;
			$state.go('app.playlists')
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