-- DDL for mysql
CREATE TABLE `adevent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `usrkey` bigint(20) DEFAULT NULL,
  `appkey` int(11) DEFAULT NULL,
  `trans_id` varchar(255) DEFAULT NULL,
  `osflag` varchar(255) DEFAULT NULL,
  `customurl` varchar(500) DEFAULT NULL,
  `appname` varchar(255) DEFAULT NULL,
  `apptitle` varchar(255) DEFAULT NULL,
  `coin` int(11) DEFAULT NULL,
  `coinint` int(11) DEFAULT NULL,
  `attp_at` datetime DEFAULT NULL,
  `postback_at` datetime DEFAULT NULL,
  `reward_at` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `appkey` (`appkey`),
  KEY `usrkey` (`usrkey`),
  KEY `transid` (`trans_id`),
  KEY `osFlag` (`osflag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
