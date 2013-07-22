'use strict';

/* Services */
// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('musikjunker').service('titleUpdater',
    function(){
       this.setTitle = function(t) {
           document.title = t;
       } ;
    });
