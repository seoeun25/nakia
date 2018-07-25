-- DDL for mysql
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
