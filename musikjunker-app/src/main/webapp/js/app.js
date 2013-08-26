'use strict';


// Declare app level module which depends on filters, and services
angular.module("musikjunker", ['ngCookies']);

angular.module('musikjunker').run(function(desktopNotification){

    desktopNotification.checkNotifications();
});