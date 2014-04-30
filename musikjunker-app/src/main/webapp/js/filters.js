'use strict';

/* Filters */
angular.module('musikjunker')
    .filter('join', function() {
        return function(t) {
            return t.join(' / ');
        }
    })
    .filter('last', function() {
        return function(t) {
            return t[t.length - 1];
        }
    })
    .filter('songlabel', function(){
        return function(t) {
            if (t.metadata && t.metadata.artist && t.metadata.title) {
                return t.metadata.artist + ' - ' + t.metadata.title;
            } else {
                return t.fileName;
            }
        }
    })
    .filter('hl', function($sce){
      return function (t, hl) {
          if (t == null) {
              return "";
          }
          return $sce.trustAsHtml(t.replace(new RegExp('(' + hl + ')', 'gi'), '<span class="hl">$1</span>'));
      }
    })
    .filter('trunc', function(){
        return function (text, len) {
            if (text == null || text.length < len) {
                return text;
            } else {
                return text.substring(0, len-3) + '...';
            }
        }

    })
    .filter('songduration', function(){
        return function (x) {
            var nbh = Math.floor(x / 3600);
            x -= nbh * 3600;
            var nbm = Math.floor(x / 60);
            x -= nbm * 60;
            var nbs = x;

            if (nbs < 10) {
                nbs = "0" + nbs;
            }
            if (nbm < 10) {
                nbm = "0" + nbm;
            }

            if (nbh != 0) {
                return nbh + ":" + nbm + ":" + nbs;
            } else {
                return nbm + ":" + nbs;
            }
        }

    })
    .filter('coverurl', function(){
        return function (c) {
            if (c == null) {
                return "img/music.jpg";
            }
            return "services/file?f=" + c.path + "/" + c.fileName;
        }
    })

;
