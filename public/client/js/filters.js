angular.module('hnotes.filters', ['ionic'])

.factory('AuthFilter', function($rootScope, $window) { 
	return {
		request: function(config) {
            config.headers = config.headers || {};
            if ($window.localStorage.getItem('token')) {
                config.headers.authorization = $window.localStorage.getItem('token');
            }
            return config;
		}
	}
})

.factory('LoadingFilter', function($rootScope) { 
	return {
		request: function(config) {
			$rootScope.loading = true;
            return config ;
		},
        requestError: function (request) {
            $rootScope.loading = false;
            return request;
        },
        response: function (response) {
            $rootScope.loading = false;
            return response;
        },
        responseError: function (response) {
            $rootScope.loading = false;
            return response;
        }
	}
})

	.factory('ErrorFilter', function($rootScope ) { 
	return {
		requestError: function (request) {
			console.log('request error');
            return request;
        },
        responseError: function (response) {
        	console.log('response error: ' + response.status);
        	$rootScope.$broadcast('responseError', response)
			
            return response;
        }
	}
})

.config(function ($httpProvider) {
	$httpProvider.interceptors.push('LoadingFilter');
    $httpProvider.interceptors.push('AuthFilter');
    $httpProvider.interceptors.push('ErrorFilter');
})
;