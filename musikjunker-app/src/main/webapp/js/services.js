'use strict';

angular.module('musikjunker').service('titleUpdater',
    function(){
       this.setTitle = function(t) {
           document.title = t;
       } ;
    });


angular.module('musikjunker').service('desktopNotification',
    function($cookieStore, $document, $timeout){
        var DESKTOP_NOTIFICATIONS_DELAY = 5000;

        var service = {};
        service.enabled = false;

        service.checkNotifications = function(){
            if (window.Notification) {
                var perm = window.Notification.permission;
                if (perm == 'granted') {
                    service.enabled = true;
                } else if (perm == 'default') {
                    $document.one("click", function(){
                        Notification.requestPermission();
                        service.enabled = true;
                    });
                }
            }
        };

        service.notify = function(title, content, link) {
            if (service.enabled) {
                var n = new Notification(title, {
                    body: content,
                    icon: link
                });
                $timeout(function(){
                    n.close();

                }, DESKTOP_NOTIFICATIONS_DELAY);
            }
        };


        return service;
    });

angular.module('musikjunker').service('faviconChanger',
    function($http){
        this.setFavicon = function(song) {
            var favicon
            if (song) {
                $("#favicon").attr("href", 'services/song/' + song.id + '/favicon');
            } else {
                $("#favicon").attr("href", 'lib/workless/img/favicon.png');
            }

        } ;
    });

