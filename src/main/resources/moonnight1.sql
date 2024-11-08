CREATE TABLE `user` (
	`user_seq`	INT AUTO_INCREMENT PRIMARY KEY,
	`email`	VARCHAR(100)	NOT NULL,
	`password`	VARCHAR(100)	NOT NULL,
	`name`	VARCHAR(100)	NOT NULL,
	`birth`	VARCHAR(100)	NOT NULL,
	`mobile_carrier`	ENUM("SKT","KT","LG")	,
	`phone`	VARCHAR(20)	NOT NULL,
    `address_seq` INT NOT NULL,
    `status`	ENUM("NORMAL","STAY","STOP")	NOT NULL,
    `marketing_received_status`	BOOLEAN	NOT NULL,
	`created_at`	DATETIME NOT NULL,
	`updated_at`	DATETIME NOT NULL
);
SELECT * FROM `user`;
SELECT COUNT(*) FROM `user` WHERE `email` = 'st2035@naver.com' AND `phone` = '010-9611-1382';

DELETE FROM `user` where user_seq = "";
DROP TABLE IF EXISTS `user`;
ALTER TABLE `user` AUTO_INCREMENT = 1;

update user set address_seq = '2' where user_seq=1;
INSERT INTO user (email, password, name, birth, phone, status, marketing_received_status, created_at) VALUES ('chamman@chamman.net','1234','마스터', '12345678', '010-1234-5678', 'NORMAL', '0', CURRENT_TIMESTAMP);

CREATE TABLE `address` (
	`address_seq`	INT	AUTO_INCREMENT PRIMARY KEY,
	`user_seq`	INT	NOT NULL,
    `name` VARCHAR(50),
	`postcode`	VARCHAR(50)	NOT NULL,
	`main_address`	VARCHAR(255)	NOT NULL,
	`detail_address`	VARCHAR(255)	NOT NULL,
    `created_at`	DATETIME NOT NULL,
	`updated_at`	DATETIME NOT NuLL
);

ALTER TABLE `address` ADD CONSTRAINT `FK_user_TO_address_1` FOREIGN KEY (
	`user_seq`
)
REFERENCES `user` (
	`user_seq`
);
update address set `updated_at` = CURRENT_TIMESTAMP where address_seq=1;
update address set `detail_address` = '805동502호 가나다라마바사아자차카타파하가나다라마바사아자차카타파하' where address_seq=1;
INSERT INTO address (user_seq, name, postcode, main_address, detail_address,updated_at) VALUES (1, "이호진", "36481", "마포구 12", "805-502",CURRENT_TIMESTAMP);

SELECT * FROM `address`;

DELETE FROM `address`;
DROP TABLE IF EXISTS `address`;
ALTER TABLE `address` AUTO_INCREMENT = 1;


create table `verification`(
`verification_seq` INT AUTO_INCREMENT PRIMARY KEY ,
`to` VARCHAR(50)	NOT NULL,
`verification_code` VARCHAR(20)	NOT NULL,
`status` VARCHAR(20)	NOT NULL,
`create_at` DATETIME	NOT NULL
);
SELECT * FROM `verification`;

DELETE FROM `verification`;
DROP TABLE IF EXISTS `verification`;
ALTER TABLE `verification` AUTO_INCREMENT = 1;

CREATE TABLE `estimate` (
    `estimate_seq` INT AUTO_INCREMENT PRIMARY KEY,
    `user_seq` INT,
    `name` VARCHAR(100),
    `phone` VARCHAR(20) NOT NULL,
    `email` VARCHAR(100),
    `emailAgree` BOOLEAN DEFAULT FALSE,
    `smsAgree` BOOLEAN DEFAULT FALSE,
    `callAgree` BOOLEAN DEFAULT FALSE,
    `postcode` VARCHAR(10),
    `mainAddress` VARCHAR(255) NOT NULL,
    `detailAddress` VARCHAR(255),
    `content` TEXT,
    `imagesPath` TEXT,
    `status` ENUM("RECEIVED", "IN_PROGRESS", "COMPLETED", "DELETE") NOT NULL,
    `created_at` DATETIME NOT NULL, 
    `updated_at` DATETIME NOT NULL
);
ALTER TABLE `estimate` ADD CONSTRAINT `FK_user_TO_estimate_1` FOREIGN KEY (
	`user_seq`
)
REFERENCES `user` (
	`user_seq`
);
SELECT * FROM `estimate`;
SELECT * FROM estimate ORDER BY created_at DESC LIMIT 50 OFFSET 0;
select * from estimate where user_seq = 1 ORDER BY created_at DESC;


update estimate set emailAgree =1 where estimate_seq=1;
DELETE FROM `estimate`;
DROP TABLE IF EXISTS `estimate`;
ALTER TABLE `estimate` AUTO_INCREMENT = 1;

create table `comment`(
	`comment_seq` int auto_increment primary key,
    `user_seq` int not null,
    `estimate_seq` int not null,
    `commentText` varchar(255) not null,
    `created_at` datetime not null,
    `updated_at` datetime not null
);
ALTER TABLE `comment` ADD CONSTRAINT `FK_estimate_TO_comment_1` FOREIGN KEY (
	`estimate_seq`
)
REFERENCES `estimate` (
	`estimate_seq`
);
ALTER TABLE `comment` ADD CONSTRAINT `FK_user_TO_comment_1` FOREIGN KEY (
	`user_seq`
)
REFERENCES `user` (
	`user_seq`
);
insert into comment (user_seq,estimate_seq,commentText,created_at,updated_at) values('1','1','완료된 견적 문의 입니다.',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
SELECT * FROM `comment`;
DELETE FROM `comment`;
DROP TABLE IF EXISTS `comment`;
ALTER TABLE `comment` AUTO_INCREMENT = 1;

CREATE TABLE `oauth` (
	`oauth_seq`	INT	AUTO_INCREMENT PRIMARY KEY,
	`user_seq`	INT,
	`provider`	ENUM ("NAVER", "KAKAO")	NOT NULL,
	`id`	VARCHAR(255)	NOT NULL,
	`email`	VARCHAR(255)	NOT NULL,
	`name`	VARCHAR(50)	,
	`birth`	VARCHAR(50)	,
	`phone`	VARCHAR(50)	,
    `status`	ENUM("NORMAL","STAY","STOP")	NOT NULL,
	`created_at`	DATETIME	NOT NULL
);

ALTER TABLE `oauth` ADD CONSTRAINT `FK_user_TO_oauth_1` FOREIGN KEY (
	`user_seq`
)
REFERENCES `user` (
	`user_seq`
);

SELECT * FROM `oauth`;

DELETE FROM `oauth`;
DROP TABLE IF EXISTS `oauth`;
ALTER TABLE `oauth` AUTO_INCREMENT = 1;


create table `login_success_log`(
	`login_success_seq` int auto_increment primary key,
    `provider` varchar(20) not null,
    `id` VARCHAR(255) not null,
    `ip` VARCHAR(45),
    `create_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
SELECT * FROM `login_success_log`;

DELETE FROM `login_success_log`;
DROP TABLE IF EXISTS `login_success_log`;	
ALTER TABLE `login_success_log` AUTO_INCREMENT = 1;

create table `login_fail_log`(
	`login_fail_seq` int auto_increment primary key,
    `provider` varchar(20) not null,
    `id` VARCHAR(255) not null,
    `ip` VARCHAR(45) NOT NULL,
    `reason` VARCHAR(255) NOT NULL,
    `success_seq` int,
    `create_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
SELECT * FROM `login_fail_log`;

DELETE FROM `login_fail_log`;
DROP TABLE IF EXISTS `login_fail_log`;
ALTER TABLE `login_fail_log` AUTO_INCREMENT = 1;

update `login_fail_log` set `success_seq`= "-1" where `id` = "st2035@naver.com" AND `success_seq` is null;
