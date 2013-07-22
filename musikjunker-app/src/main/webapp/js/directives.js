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
