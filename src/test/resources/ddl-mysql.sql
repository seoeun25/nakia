-- DDL for mysql
CREATE TABLE `adevent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `usrkey` bigint(20) DEFAULT NULL,
  `appkey` int(11) DEFAULT NULL,
  `transid` varchar(255) DEFAULT NULL,
  `osFlag` varchar(255) DEFAULT NULL,
  `customUrl` varchar(255) DEFAULT NULL,
  `appName` varchar(255) DEFAULT NULL,
  `appTitle` varchar(255) DEFAULT NULL,
  `coin` int(11) DEFAULT NULL,
  `cointInt` int(11) DEFAULT NULL,
  `attpAt` datetime DEFAULT NULL,
  `postbackAt` datetime DEFAULT NULL,
  `rewardAt` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `appkey` (`appkey`),
  KEY `usrkey` (`usrkey`),
  KEY `transid` (`transid`),
  KEY `osFlag` (`osFlag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
