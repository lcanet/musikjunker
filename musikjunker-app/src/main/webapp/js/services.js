'use strict';

angular.module('musikjunker').service('titleUpdater',
    function(){
       this.setTitle = function(t) {
           document.title = t;
       } ;
    });


angular.module('musikjunker').service('desktopNotification',
    function($cookieStore, $document, $timeout){
        var service = {};
        service.enabled = false;

        service.checkNotifications = function(){
            if (window.webkitNotifications) {
                var perm = window.webkitNotifications.checkPermission();
                if (perm === 0) { // 0 is PERMISSION_ALLOWED
                    service.enabled = true;
                } else if (perm == 1) {
                    $document.one("click", function(){
                        window.webkitNotifications.requestPermission();
                        service.enabled = true;
                    });
                }
            }
        };

        service.notify = function(title, content, link) {
            if (service.enabled) {
                var n = window.webkitNotifications.createNotification(link,
                    title,
                    content);
                n.show();
                $timeout(function(){
                    n.close();

                }, 5000);
            }
        };


        return service;
    });
