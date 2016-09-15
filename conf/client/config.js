angular.module('hnotes.config', [])
	.constant('SERVER_URL', '${env.SERVER_URL?http://localhost:9000}');