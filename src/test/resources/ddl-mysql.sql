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

CREATE TABLE `coupon_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pg_company` varchar(255) NOT NULL,
  `group_id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `point` int(11) DEFAULT NULL,
  `bonus_coin` int(11) DEFAULT NULL,
  `coin` int(11) DEFAULT NULL,
  `expire_type` varchar(255) NOT NULL,
  `expire_value` bigint(20) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `coupon` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pg_company` varchar(50) NOT NULL,
  `request_id` varchar(255) NOT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `coupon_id` varchar(100) DEFAULT NULL,
  `expired_at` bigint(20) DEFAULT NULL,
  `meta` varchar(1000) DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL,
  `issued_at` datetime DEFAULT NULL,
  `discarded_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `coupon_pg_request_id` (`pg_company`,`request_id`),
  KEY `coupon_lz_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

