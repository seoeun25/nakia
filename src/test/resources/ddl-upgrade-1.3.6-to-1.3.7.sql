-- DDL for mysql
ALTER TABLE `adevent`
  CHANGE `transid` `trans_id` VARCHAR(255) DEFAULT NULL,
  CHANGE `osFlag` `osflag` INT(11) DEFAULT NULL,
  CHANGE `cointInt` `coinint` INT(11) DEFAULT NULL,
  CHANGE `attpAt` `attp_at` DATETIME DEFAULT NULL,
  CHANGE `postbackAt` `postback_at` DATETIME DEFAULT NULL,
  CHANGE `rewardAt` `reward_at` DATETIME DEFAULT NULL;

