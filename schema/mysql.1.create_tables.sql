SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

CREATE TABLE IF NOT EXISTS `Resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addedDate` datetime DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `fileDate` datetime DEFAULT NULL,
  `fileName` varchar(255) DEFAULT NULL,
  `album` varchar(255) DEFAULT NULL,
  `artist` varchar(255) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `genre` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `year` varchar(32) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_res_filename` (`fileName`),
  KEY `idx_res_path` (`path`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

