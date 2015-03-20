'use strict';

/* Controllers */

function SearchController($scope, $http) {
    $scope.searchQuery = "";
    $scope.searchResults = null;

    $scope.page = {
        total: 0,
        index: 0,
        order: "artist"
    };

    function doSearchInternal() {
        $scope.searchQueryHl = $scope.searchQuery;
        $http.get("services/search",
            {
                params: {
                    q: $scope.searchQuery,
                    start: $scope.page.index,
                    o: $scope.page.order
                }
            }).success(function(data) {

            $scope.searchResults = data.results;
            $scope.page.total = data.size;
            $scope.page.index = data.index;
        });
    }

    $scope.doSearch = function() {
        $scope.page.index = 0;
        doSearchInternal();
    };
    $scope.doSort = function(order) {
        $scope.page.index = 0;
        $scope.page.order = order;
        doSearchInternal();
    };

    $scope.getNextPage = function() {
        $scope.page.index += 20;
        doSearchInternal();
    };
    $scope.getPrevPage = function() {
        $scope.page.index -= 20;
        doSearchInternal();
    };

    $scope.paginationAvailable = function() {
        return $scope.searchResults != null  && $scope.page.total > 20;
    };

    $scope.addAlbum = function(s) {
        $http.get("services/album?q=" + s.metadata.album).success(function(data) {
            $scope.setPlayQueue(data);
        });
    };

    $scope.$on('SearchTrack', function($ev, q){
        $scope.searchQuery = q;
        $scope.doSearch();
    });

}

function BrowseController($scope, $http, $filter) {

    $scope.dirs = [];
    $scope.dirStack = [];
    $scope.files = [];

    function refreshCurrentDir() {
        var dir = $scope.dirStack.join('/');

        $http.get("services/dirs?q=" + dir).success(function(data) {
            if ($scope.dirStack.length > 0) {
                data.unshift("..");
            }
            $scope.dirs = data;

        });

        $scope.files = [];
        if ($scope.dirStack.length > 0) {
            $http.get("services/audios?dir=" + dir).success(function(data) {
                $scope.files = data;
            });
        }
    }

    $scope.selectDir = function(dir) {
        if (dir == "..") {
            $scope.dirStack.pop();
        }  else {
            $scope.dirStack.push(dir);
        }

        refreshCurrentDir();
    };

    $scope.playAllFiles = function(){
        // on est obligé de filtrer
        var fn = $filter('filter');
        var arr = [].concat($scope.files);
        var newplaylist = fn(arr, $scope.filterText);

        $scope.setPlayQueue($scope.playqueue.concat(newplaylist));
    };

    $scope.randomFromDir = function(dirStack) {
        var dir = dirStack.join('/');
        $http.get("services/dir/random?n=20&dir=" + encodeURIComponent(dir)).success(function(data) {
            $scope.setPlayQueue(data);
        });
    };

    function markFilesIgnored(status) {
        if ($scope.files) {
            var nb = $scope.files.length;
            for (var i = 0; i < nb; i++) {
                $scope.files[i].ignoreShuffle = status;
            }
        }
    }

    $scope.unignore = function(dirStack) {
        var dir = dirStack.join('/');
        $http.post('services/dir/unignore?dir=' + encodeURIComponent(dir)).success(function(){
            markFilesIgnored(false);
        });
    };

    $scope.ignore = function(dirStack) {
        var dir = dirStack.join('/');
        $http.post('services/dir/ignore?dir=' + encodeURIComponent(dir)).success(function(){
            markFilesIgnored(true);
        });
    };


    // fin: init selon le path de l'url

    refreshCurrentDir();
}

function PlaylistController($scope, $http) {

    $scope.newPlaylist = {
        name: ''
    };

    $scope.addPlaylist = function(){
        if ($scope.addPlaylistForm.$valid) {
            $http.post('services/playlist', $scope.newPlaylist).success(function(res){
                $scope.playlists.push(res);
                $scope.newPlaylist.name = '';
            });
        }
    };

    $scope.deletePlaylist = function(pl) {
        $http.delete('services/playlist/' + pl.id).success(function(res){
            var ip = $scope.playlists.indexOf(pl);
            $scope.playlists.splice(ip, 1);
        });
    };

    $scope.queuePlaylist = function(pl) {
        $http.get('services/playlist/' + pl.id).success(function(res){
            $scope.setPlayQueue(res.songs);
        });
    };

    $scope.openPlaylist = function(pl) {
        $http.get('services/playlist/' + pl.id).success(function(res){
            $scope.currentPlaylist = res;
        });
    };

    $scope.removeFromPlaylist = function(s) {
        $http.put('services/playlist/' + $scope.currentPlaylist.id, {
            deleteResources: [ s.id ]
        }).success(function(){
            var idx = $scope.currentPlaylist.songs.indexOf(s);
            if (idx != -1) {
                $scope.currentPlaylist.songs.splice(idx, 1);
            }
        });
    };

    $scope.$on('playlistchanged', function($evt, pl){
        if ($scope.currentPlaylist && pl.id === $scope.currentPlaylist.id){
            $scope.openPlaylist(pl);
        }
    });

}

function MainController($timeout, $scope, $http, $log, $rootScope, $filter, titleUpdater, desktopNotification, $q, $sce, $location, faviconChanger) {

    $scope.playqueue = [];
    $scope.currentlyPlaying = null;
    $scope.covers = [];
    $scope.currentCover = null;
    $scope.wikiInfo = null;

    $scope.viewState = {
        browseMode: true,
        searchMode: false,
        playlistMode: false
    };

    $scope.viewState.change = function(mode) {
        $scope.viewState.searchMode = (mode == 'search');
        $scope.viewState.browseMode = (mode == 'browse');
        $scope.viewState.playlistMode = (mode == 'playlist');
    };

    function refreshCovers(path) {
        $scope.covers = [];
        $scope.currentCover = null;

        var defer = $q.defer();

        if (path) {
            $http.get("services/covers?dir=" + path).success(function(data) {
                $scope.covers = data;
                if ($scope.covers.length > 0){
                    $scope.currentCover = $scope.covers[0];
                    defer.resolve($scope.currentCover);
                } else {
                    defer.resolve(null);
                }
            });
        } else {
            defer.reject(null);
        }
        return defer.promise;
    }
    function refreshPlaylists() {
        $http.get('services/playlists').success(function(res){
            $scope.playlists =  res;
        });
    }

    $scope.setPlayQueue = function(newarr) {
        if (!newarr) {
            newarr = $scope.playqueue;
        }
        newarr = [].concat(newarr);
        $scope.playqueue = newarr;
    };


    $scope.playFile = function(f) {
        $scope.setPlayQueue([f]);
    };

    $scope.queueFile = function(f) {
        $scope.playqueue.push(f);
        $scope.setPlayQueue();
    };

    $scope.dequeueFile = function(idx) {
        $scope.playqueue.splice(idx, 1);
        $scope.setPlayQueue();
    };
    $scope.moveToTop = function(idx) {
        var pl = $scope.playqueue;
        var a = pl[idx];
        pl.splice(idx, 1);
        pl.unshift(a);
        $scope.setPlayQueue();
    };

    $scope.clearPlayQueue = function() {
        $scope.setPlayQueue([]);
    };

    $scope.shufflePlayQueue = function() {
        $scope.playqueue.shuffle();
        $scope.setPlayQueue();
    };
    $scope.skipFirst = function() {
        $scope.playqueue.shift();
        $scope.setPlayQueue();
    };

    var fnFormatSong = $filter('songlabel');
    var fnCoverUrl = $filter('coverurl');

    var timer;

    $scope.doOnPlayChange = function($song) {
        var p = refreshCovers($song ? $song.path : null)
        $scope.currentlyPlaying = $song;

        // title
        titleUpdater.setTitle($song != null ?
                fnFormatSong($song) :
                "Musikjunker");

        faviconChanger.setFavicon($song);

        if ($song != null && p){
            p.then(function(cover){
                var coverUrl = null;
                if (cover) {
                    coverUrl = fnCoverUrl(cover);
                }
                desktopNotification.notify('Musikjunker',
                    fnFormatSong($song),
                    coverUrl);

            });
        }
        if (timer) {
            $timeout.cancel(timer);
            timer = null;
        }
        if ($song) {
            timer = $timeout(function(){
                incrementPlay($song);
            }, 10000);

            var urlLabel = fnFormatSong($song).replace(/ /g, '_');
            $location.path('/' + $song.id + '/' + urlLabel);
        }
        $scope.wikiInfo = null;
    };

    function incrementPlay($song) {
        $http.post("services/song/" + $song.id + "/incrementPlayStats");
    }

    $scope.setCurrentCover = function(c) {
        $scope.currentCover = c;
    };

    $scope.loadRandomPlayQueue = function(){
        $http.get("services/random?n=20").success(function(data) {
            $scope.playqueue = data;
        });
    };
    $scope.loadStarred = function() {
        $http.get("services/stars/random?n=20").success(function(data) {
            $scope.playqueue = data;
        });
    };

    $scope.loadNewFiles = function() {
        $http.get("services/new/random?n=20&d=300").success(function(data) {
            $scope.playqueue = data;
        });

    };

    $scope.loadWikiInfos = function() {
        if ($scope.wikiInfo) {
            $scope.wikiInfo = null;
        } else {

            if ($scope.currentlyPlaying && $scope.currentlyPlaying.metadata.artist) {
                $http.get("services/wiki?q=" + $scope.currentlyPlaying.metadata.artist).success(function(data){
                    var pages = data.query.pages;
                    var foundPage = null;
                    for (var p in pages) {
                        if (p != "-1") {
                            foundPage = pages[p];
                            break;
                        }
                    }
                    if (foundPage) {
                        $scope.wikiInfo = $sce.trustAsHtml(foundPage.extract);
                    } else{
                        $scope.wikiInfo = $sce.trustAsHtml('<p class="error">Not found</p>');
                    }
                }, function(){
                    $scope.wikiInfo = $sce.trustAsHtml('<p class="error">Error getting page</p>');
                });
            }
        }
    };

    var LASTFM_API_KEY = '128dc9c7cd594d65a187ebf400ea44fd';

    $scope.loadSimilar = function() {
        if ($scope.similarTracks != null) {
            $scope.similarTracks = null;
        } else if ($scope.currentlyPlaying && $scope.currentlyPlaying.metadata.artist && $scope.currentlyPlaying.metadata.title) {

            $scope.noTracksFound = false;

            var url = 'http://ws.audioscrobbler.com/2.0/?method=track.getsimilar' +
                '&artist=' +  encodeURIComponent($scope.currentlyPlaying.metadata.artist) +
                '&track=' + encodeURIComponent($scope.currentlyPlaying.metadata.title) +
                '&api_key=' + LASTFM_API_KEY +
                '&format=json';
            $http.get(url).then(function(res){
                var similar = res.data.similartracks;
                $log.info("Similar tracks", similar);
                if (similar && similar.track && angular.isArray(similar.track)) {
                    $scope.similarTracks = similar.track.slice(0, Math.min(20, similar.track.length));
                } else {
                    $scope.noTracksFound = true;
                }
            }, function(err){
                $log.error('Error requesting last.fm', err);
                alert('Cannot get data!');
            });
        }
    };

    $scope.unignore = function(f) {
        $http.post('services/song/' + f.id + '/unignore').success(function(){
            f.ignoreShuffle = false;
        });

    };
    $scope.ignore = function(f) {
        $http.post('services/song/' + f.id + '/ignore').success(function(){
            f.ignoreShuffle = true;
        });

    };

    $scope.togglePlaylistWorkflow = function() {
        $scope.addToPlaylistWorkflow = !$scope.addToPlaylistWorkflow;
    };
    var COLOR_CLASSES = ['pink', 'green', 'green', 'blue', 'red', 'magenta', 'orange', 'yellow'];

    $scope.getLabelColorClass = function(pl) {
        return COLOR_CLASSES[pl.id % COLOR_CLASSES.length];
    };

    $scope.addCurrentToPlaylist = function(pl) {
        $http.put('/services/playlist/'+ pl.id, {
            addResources: [
                $scope.currentlyPlaying.id
            ]
        }).success(function(){
            $scope.togglePlaylistWorkflow();
            $rootScope.$broadcast('playlistchanged', pl);
        });
    };

    // load unique file if provided in url
    var path = $location.path();
    var idx = path.indexOf('/', 1);
    if (idx != -1) {
        var songId = path.substring(1, idx);
        $log.info("Loading song #" + songId);
        $http.get('services/song/' + songId).success(function(song){
            if (song) {
                $scope.setPlayQueue([song]);
            }
        });
    }
    refreshPlaylists();


}


function MoreTracksLikeThisController($scope, $rootScope) {

    $scope.getImageOfTrack = function(tr) {
        if (!tr) return null;
        var goodLink = null;
        angular.forEach(tr.image, function(img){
            if (img.size === 'small') {
                goodLink = img['#text'];
            }
        });
        return goodLink;
    };

    $scope.searchSimilar = function(track) {
        var q = track.artist.name + ' ' + track.name;
        $rootScope.$broadcast('SearchTrack', q);
        $scope.viewState.change('search');

        $("#workZoneTopSeparator")[0].scrollIntoView( true );
    }
}