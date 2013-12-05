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
            $scope.setPlayList(data);
        });
    };


}

function BrowseController($scope, $http, $location, $filter) {

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

        $location.path("/" + $scope.dirStack.join('/'));
        refreshCurrentDir();
    };

    $scope.playAllFiles = function(){
        // on est obligé de filtrer
        var fn = $filter('filter');
        var arr = [].concat($scope.files);
        var newplaylist = fn(arr, $scope.filterText);

        $scope.setPlayList($scope.playlist.concat(newplaylist));
    };

    $scope.randomFromDir = function(dirStack) {
        var dir = dirStack.join('/');
        $http.get("services/dir/random?n=20&dir=" + encodeURIComponent(dir)).success(function(data) {
            $scope.setPlayList(data);
        });
    };

    // fin: init selon le path de l'url
    var path = $location.path();
    if (path && path != '/') {
        $scope.dirStack = path.substring(1).split('/');
    }

    refreshCurrentDir();
}


function MainController($timeout, $scope, $http, $filter, titleUpdater, desktopNotification, $q) {

    $scope.playlist = [];
    $scope.currentlyPlaying = null;
    $scope.covers = [];
    $scope.currentCover = null;
    $scope.wikiInfo = null;

    $scope.viewState = {
        browseMode: true,
        searchMode: false,
        tagCloudMode: false
    };

    $scope.viewState.change = function(mode) {
        $scope.viewState.searchMode = (mode == 'search');
        $scope.viewState.tagCloudMode = (mode == 'tagcloud');
        $scope.viewState.browseMode = (mode == 'browse');
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

    $scope.setPlayList = function(newarr) {
        if (!newarr) {
            newarr = $scope.playlist;
        }
        newarr = [].concat(newarr);
        $scope.playlist = newarr;
    };


    $scope.playFile = function(f) {
        $scope.setPlayList([f]);
    };

    $scope.queueFile = function(f) {
        $scope.playlist.push(f);
        $scope.setPlayList();
    };

    $scope.dequeueFile = function(idx) {
        $scope.playlist.splice(idx, 1);
        $scope.setPlayList();
    };
    $scope.moveToTop = function(idx) {
        var pl = $scope.playlist;
        var a = pl[idx];
        pl.splice(idx, 1);
        pl.unshift(a);
        $scope.setPlayList();
    };

    $scope.clearPlaylist = function() {
        $scope.setPlayList([]);
    };

    $scope.shufflePlaylist = function() {
        $scope.playlist.shuffle();
        $scope.setPlayList();
    };
    $scope.skipFirst = function() {
        $scope.playlist.shift();
        $scope.setPlayList();
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
            }, 3000);
        }
        $scope.wikiInfo = null;
    };

    function incrementPlay($song) {
        $http.post("services/song/" + $song.id + "/incrementPlayStats");
    }

    $scope.setCurrentCover = function(c) {
        $scope.currentCover = c;
    };

    $scope.loadRandomPlayList = function(){
        $http.get("services/random?n=20").success(function(data) {
            $scope.playlist = data;
        });
    };
    $scope.loadStarred = function() {
        $http.get("services/stars/random?n=20").success(function(data) {
            $scope.playlist = data;
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
                        $scope.wikiInfo = foundPage.extract;
                    } else{
                        $scope.wikiInfo = '<p class="error">Not found</p>'
                    }
                }, function(){
                    $scope.wikiInfo = '<p class="error">Error getting page</p>'
                });
            }
        }
    };


}
