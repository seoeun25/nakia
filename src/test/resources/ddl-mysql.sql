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

CREATE TABLE `tapjoyevent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `request_id` varchar(255) NOT NULL,
  `locale` varchar(10) NOT NULL,
  `platform` varchar(10) NOT NULL,
  `snuid` bigint(20) NOT NULL,
  `currency` int(11) NOT NULL,
  `display_multiplier` double DEFAULT NULL,
  `verifier` varchar(255) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `postback_at` datetime DEFAULT NULL,
  `reward_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `tapjoyevent_snuid` (`snuid`),
  KEY `tapjoyevent_request_id` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
