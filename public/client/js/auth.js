angular.module('hnotes.auth', ['hnotes.config', 'auth0.lock', 'angular-jwt'])

	.config(function ($stateProvider, lockProvider) {
		lockProvider.init({
	        clientID: 'RqwHCef6ycF9tjPCUxFIDuujpzmHjcfE',
	        domain: 'anhminh.eu.auth0.com',
	        options: {
	        	container: 'lock-container',
	        	language: 'fr',
	        	auth: {
	        		scope: 'openid email'
	        			}
	        		}
	      });
		
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
	
	.run(function(lock) {
		
		// Intercept the hash that comes back from authentication
	    // to ensure the `authenticated` event fires
	    lock.interceptHash()
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
			},
			auth0Login: function(idToken) {
				console.log("auth0 login")
				return $http.post(SERVER_URL + '/auth0Login', {token: idToken}).then(loggedIn, errLogin)
			}
		} 
	})
	.controller('AuthCtrl', function($scope, $state, Auth, authService, $rootScope) {
		// Put the authService on $scope to access
        // the login method in the view
        $scope.authService = authService;
        
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
	.controller('LogoutCtrl', function($scope, $state, Auth, authService) {
		
		
		$scope.$on('$stateChangeSuccess', function(event, toState) {
			if(toState.name == 'logout') {
				console.log("logging out")
				Auth.logout()
				authService.logout()
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
	})
	.service('authService', authService)
	.run(function($rootScope, authService, Auth) {
	
	  // Put the authService on $rootScope so its methods
	  // can be accessed from the nav bar
		$rootScope.authService = authService
	  authService.registerAuthenticationListener();
		$rootScope.$on('idTokenSet', function(event, token) {
			console.log("id token set received")
			Auth.auth0Login(token)
		})
	});;


authService.$inject = ['$rootScope', 'lock', 'authManager'];

function authService($rootScope, lock, authManager) {

  var userProfile = JSON.parse(localStorage.getItem('profile')) || {};

  function login() {
	  console.log("authservice login")
    lock.show();
  }

  // Logging out just requires removing the user's
  // id_token and profile
  function logout() {
    localStorage.removeItem('id_token');
    localStorage.removeItem('profile');
    authManager.unauthenticate();
    userProfile = {};
  }

  // Set up the logic for when a user authenticates
  // This method is called from app.run.js
  function registerAuthenticationListener() {
    lock.on('authenticated', function(authResult) {
      localStorage.setItem('id_token', authResult.idToken);
      console.log(authResult.idToken)
      $rootScope.$broadcast('idTokenSet', authResult.idToken)
      console.log("broadcast id token set")
      /*
      authManager.authenticate();

      lock.getProfile(authResult.idToken, function(error, profile) {
        if (error) {
          console.log(error);
        }
        

        localStorage.setItem('profile', JSON.stringify(profile));
        $rootScope.$broadcast('userProfileSet', profile);
      });
      */
    });
  }

  return {
    userProfile: userProfile,
    login: login,
    logout: logout,
    registerAuthenticationListener: registerAuthenticationListener
  }
}
