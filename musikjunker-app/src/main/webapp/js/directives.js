'use strict';

/* Directives */

angular.module("musikjunker").directive("junkerAudio", function($parse) {
    return {
        restrict: "E",
        template: '<audio id="audio" controls style="width: 100%; padding: 0px"></audio>',
        replace: true,
        scope: {
            playlist: "=playlist",
            onSongChange: "&"
        },
        transclude: true,
        link: function(scope, elt, attrs) {
            var audio = elt[0];
            var currentPlayingFile = null;

            function processPlayList() {
                var pl = scope.playlist;
                if (pl.length > 0) {
                    var song = pl[0];
                    if (currentPlayingFile == null || currentPlayingFile.id != song.id){
                        var songPath = "services/file?f=" + song.path + '/' + song.fileName;
                        currentPlayingFile = song;
                        audio.setAttribute("src", songPath);
                        audio.play();
                    }
                } else{
                    currentPlayingFile = null;
                    audio.setAttribute("src", null);
                }
                scope.onSongChange({$song: currentPlayingFile});
            }
            elt.bind("ended", function() {
                scope.$apply(function(){
                    if (currentPlayingFile) {
                        var idx = scope.playlist.indexOf(currentPlayingFile);
                        if (idx != -1) {
                            scope.playlist.splice(idx, 1);
                        }
                    }
                });
                processPlayList();
            });
            scope.$watch("playlist", function(t) {
                processPlayList();
            });
        }
    };
});

angular.module("musikjunker").directive("stars", function($http) {
    return {
        restrict: "E",
        template: '<span ng-show="id != null" ng-mouseleave="onMouseLeave()" class="star-selector">' +
            '<span class="star" ng-repeat="n in levels" data-starlevel="{{ n }}" ng-click="doStar(n)" ng-mouseover="onMouseOver(n)"></span>' +
            '<i class="icon icon-trash reset-icon" ng-click="resetStars()" ng-mouseover="onMouseOver(0)"></i> ' +
            '</span>',
        replace: true,
        scope: {
            stars: "=",
            id: "="
        },
        transclude: true,
        link: function(scope, elt, attrs) {
            scope.levels = [1,2,3,4,5];

            scope.resetStars = function(){
                scope.doStar(0);
            };
            scope.doStar = function(x) {
                $http.post('services/song/' + scope.id + '/star?n=' + x).
                    success(function(){
                        scope.stars = x;
                    });
            };

            function updateClasses(nb) {
                var stars = elt.find('.star');
                $.each(stars, function(i, elt){
                    var level = i+1;
                    $(elt).toggleClass('starred', nb >= level);
                });
            }
            scope.$watch("stars", function(nval){
                updateClasses(nval);
            });
            scope.onMouseOver = function(n) {
                updateClasses(n);
            };
            scope.onMouseLeave = function() {
                updateClasses(scope.stars);
            };

        }
    };
});

angular.module("musikjunker").directive("starsDisplay", function($http) {
    return {
        restrict: "E",
        template: '<span>' +
            '<span class="star-small" ng-show="isStarred(1)"></span> ' +
            '<span class="star-small" ng-show="isStarred(2)"></span> ' +
            '<span class="star-small" ng-show="isStarred(3)"></span> ' +
            '<span class="star-small" ng-show="isStarred(4)"></span> ' +
            '<span class="star-small" ng-show="isStarred(5)"></span> ' +
            '</span>',
        replace: true,
        scope: {
            stars: "="
        },
        transclude: true,
        link: function(scope, elt, attrs) {
            scope.isStarred = function(x){
                return scope.stars >= x;
            };
        }
    };
});
