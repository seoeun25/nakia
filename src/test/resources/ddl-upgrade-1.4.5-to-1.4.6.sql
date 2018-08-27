CREATE TABLE `coupon_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bonus_coin` int(11) DEFAULT NULL,
  `coin` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `expire_type` varchar(255) NOT NULL,
  `expire_value` bigint(20) NOT NULL,
  `group_id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `pg_company` varchar(255) NOT NULL,
  `point` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
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

