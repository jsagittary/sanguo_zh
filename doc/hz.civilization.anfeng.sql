/*
Navicat MySQL Data Transfer

Source Server         : 本机
Source Server Version : 50710
Source Host           : localhost:3306
Source Database       : civilization_1

Target Server Type    : MYSQL
Target Server Version : 50710
File Encoding         : 65001

Date: 2020-06-30 22:30:39
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for p_account
-- ----------------------------
DROP TABLE IF EXISTS `p_account`;
CREATE TABLE `p_account` (
  `keyId` int(10) NOT NULL AUTO_INCREMENT,
  `accountKey` bigint(20) NOT NULL,
  `serverId` int(10) NOT NULL,
  `platNo` int(10) NOT NULL,
  `platId` char(40) COLLATE utf8_bin NOT NULL,
  `childNo` int(10) NOT NULL DEFAULT '0',
  `forbid` int(10) NOT NULL DEFAULT '0' COMMENT '是否封号 1开启封号',
  `whiteName` int(10) NOT NULL DEFAULT '0' COMMENT '是否白名单玩家 1.属于白名单玩家',
  `lordId` bigint(20) NOT NULL,
  `created` int(10) NOT NULL DEFAULT '0' COMMENT '是否创建了角色',
  `deviceNo` char(80) CHARACTER SET ascii DEFAULT NULL,
  `createDate` datetime DEFAULT NULL COMMENT '角色创建日期',
  `loginDays` int(10) NOT NULL DEFAULT '1' COMMENT '登陆天数（非连续登陆天数，登陆一天累加一次）',
  `loginDate` datetime DEFAULT NULL COMMENT '最后登录日期',
  `isGm` int(10) NOT NULL DEFAULT '0' COMMENT '是否gm',
  `isGuider` int(10) NOT NULL DEFAULT '0' COMMENT '是否新手引导员',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `only_index` (`accountKey`,`serverId`) USING BTREE,
  UNIQUE KEY `lord_index` (`lordId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of p_account
-- ----------------------------

-- ----------------------------
-- Table structure for p_building
-- ----------------------------
DROP TABLE IF EXISTS `p_building`;
CREATE TABLE `p_building` (
  `lordId` bigint(20) NOT NULL,
  `command` int(10) NOT NULL COMMENT '司令部等级',
  `wall` int(10) NOT NULL COMMENT '围墙等级',
  `tech` int(10) NOT NULL COMMENT '科研所等级',
  `ware` int(10) NOT NULL COMMENT '仓库等级',
  `club` int(10) NOT NULL COMMENT '俱乐部等级',
  `refit` int(10) NOT NULL COMMENT '改造中心等级',
  `factory1` int(10) NOT NULL COMMENT '兵营等级',
  `factory2` int(10) NOT NULL COMMENT '坦克工厂等级',
  `factory3` int(10) NOT NULL COMMENT '装甲基地等级',
  `chemical` int(10) NOT NULL COMMENT '化工厂等级',
  `munition` int(10) NOT NULL COMMENT '军工厂等级',
  `college` int(10) NOT NULL COMMENT '军事学院\r\n等级',
  `trade` int(10) NOT NULL,
  `war` int(10) NOT NULL,
  `train` int(10) NOT NULL,
  `air` int(10) NOT NULL COMMENT '空军基地',
  `train2` int(10) NOT NULL COMMENT '2号超级工厂',
  PRIMARY KEY (`lordId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of p_building
-- ----------------------------

-- ----------------------------
-- Table structure for p_common
-- ----------------------------
DROP TABLE IF EXISTS `p_common`;
CREATE TABLE `p_common` (
  `lordId` bigint(20) NOT NULL,
  `baptizeCnt` int(10) NOT NULL COMMENT '洗练次数',
  `baptizeTime` int(10) NOT NULL COMMENT '洗练刷新时间',
  `reBuild` int(10) NOT NULL COMMENT '重建次数',
  `scoutCdTime` int(10) NOT NULL DEFAULT '0' COMMENT '侦查CD结束时间',
  `bagCnt` int(10) NOT NULL DEFAULT '0' COMMENT '装备容量',
  `bagBuy` int(10) NOT NULL DEFAULT '0' COMMENT '装备容量购买次数',
  `washCount` int(10) NOT NULL DEFAULT '0' COMMENT '将领洗髓免费次数',
  `washTime` int(10) NOT NULL DEFAULT '0' COMMENT '免费洗髓次数下次更新时间',
  `autoArmy` int(10) NOT NULL COMMENT '自动补兵',
  `heroCdTime` int(10) NOT NULL DEFAULT '0' COMMENT '良将寻访功能，下次免费寻访CD结束时间',
  `normalHero` int(10) NOT NULL DEFAULT '0' COMMENT '良将已寻访次数',
  `superProcess` int(10) NOT NULL DEFAULT '0' COMMENT '神将寻访解锁进度，百分比（0-100）',
  `superHero` int(10) NOT NULL DEFAULT '0' COMMENT '神将已寻访次数',
  `superTime` int(10) NOT NULL DEFAULT '0' COMMENT '如果已激活神将，本次结束时间',
  `superOpenNum` int(10) NOT NULL DEFAULT '0' COMMENT '神将已激活次数，用于做首次激活特殊处理',
  `superFreeNum` int(11) NOT NULL COMMENT '神将寻访免费次数',
  `buyAct` int(11) NOT NULL,
  `retreat` int(11) NOT NULL,
  `killNum` int(11) NOT NULL DEFAULT '0' COMMENT '玩家杀敌数',
  `renameCnt` int(11) NOT NULL DEFAULT '0' COMMENT '已改名的次数',
  `autoBuildCnt` int(11) NOT NULL DEFAULT '0' COMMENT '自动建筑剩余次数',
  `autoBuildOnOff` int(11) NOT NULL DEFAULT '0' COMMENT '自动建造开关,0表示关闭,1表示开启',
  PRIMARY KEY (`lordId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of p_common
-- ----------------------------

-- ----------------------------
-- Table structure for p_cross_map
-- ----------------------------
DROP TABLE IF EXISTS `p_cross_map`;
CREATE TABLE `p_cross_map` (
  `mapId` int(10) NOT NULL,
  `city` mediumblob COMMENT '城池情况',
  `bandit` mediumblob COMMENT '流寇数据',
  `mine` mediumblob COMMENT '矿点信息',
  `battle` mediumblob COMMENT '战斗信息',
  `playerArmy` mediumblob COMMENT '玩家在该地图的部队',
  `mapInfo` mediumblob COMMENT '该地图的信息',
  `mapExt1` mediumblob COMMENT '扩展信息1',
  PRIMARY KEY (`mapId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='本地跨服地图数据保存';

-- ----------------------------
-- Records of p_cross_map
-- ----------------------------

-- ----------------------------
-- Table structure for p_data
-- ----------------------------
DROP TABLE IF EXISTS `p_data`;
CREATE TABLE `p_data` (
  `lordId` bigint(20) NOT NULL,
  `maxKey` int(10) NOT NULL DEFAULT '0' COMMENT '最大key',
  `roleData` blob NOT NULL COMMENT '玩家数据公用字段',
  `heros` blob NOT NULL COMMENT '武将数据',
  `equips` blob NOT NULL COMMENT '装备数据',
  `props` blob NOT NULL COMMENT '道具数据',
  `buildQue` blob NOT NULL COMMENT '建造或生产队列',
  `tasks` blob NOT NULL COMMENT '任务相关',
  `mill` blob NOT NULL COMMENT '资源建筑',
  `mails` mediumblob COMMENT '邮件',
  `gains` blob NOT NULL COMMENT '官员效果加成',
  `factory` blob NOT NULL COMMENT '兵营造兵队列',
  `army` blob NOT NULL COMMENT '行军队列',
  `combats` blob NOT NULL COMMENT '关卡信息',
  `equipQue` blob NOT NULL COMMENT '打造',
  `typeInfo` blob NOT NULL COMMENT '历史记录 已招募id',
  `tech` blob NOT NULL COMMENT '科技',
  `shop` blob NOT NULL COMMENT '商店',
  `combatFb` blob NOT NULL COMMENT '高级副本',
  `acquisition` blob NOT NULL COMMENT '个人资源点',
  `awards` blob NOT NULL,
  `supEquips` blob NOT NULL,
  `supEquipQue` blob NOT NULL,
  `opts` blob NOT NULL,
  `wallNpc` blob NOT NULL,
  `effects` blob NOT NULL,
  `chemical` blob NOT NULL,
  `treasure` blob NOT NULL,
  `friends` blob NOT NULL COMMENT '好友列表',
  `masterApprentice` blob NOT NULL COMMENT '师徒信息',
  `cabinet` blob NOT NULL COMMENT '内阁相关的信息',
  `trophy` blob NOT NULL,
  `playerExt` blob NOT NULL COMMENT '玩家的附加信息',
  `day7Act` blob NOT NULL COMMENT '七日活动',
  `activity` blob NOT NULL,
  `medals` mediumblob NOT NULL COMMENT '勋章数据',
  `signin` blob NOT NULL COMMENT '签到数据',
  `signInExt` blob NOT NULL COMMENT '签到数据扩展',
  `crossData` blob COMMENT '跨服数据',
  PRIMARY KEY (`lordId`),
  KEY `lord_index` (`lordId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of p_data
-- ----------------------------

-- ----------------------------
-- Table structure for p_global
-- ----------------------------
DROP TABLE IF EXISTS `p_global`;
CREATE TABLE `p_global` (
  `globalId` int(10) NOT NULL AUTO_INCREMENT,
  `mapArea` blob NOT NULL COMMENT '记录世界地图中的区域开启情况',
  `city` blob NOT NULL COMMENT '城池状态记录',
  `bandit` mediumblob NOT NULL COMMENT '流寇数据',
  `mine` mediumblob NOT NULL COMMENT '矿点信息',
  `battle` mediumblob NOT NULL COMMENT '记录城战、阵营战等战斗信息',
  `worldTask` blob NOT NULL COMMENT '世界任务',
  `cabinetLead` blob NOT NULL COMMENT '点兵统领数据',
  `privateChat` longblob COMMENT '所有玩家私聊信息',
  `trophy` mediumblob COMMENT '全服成就相关',
  `gestapo` mediumblob COMMENT '盖世太保数据',
  `globalExt` mediumblob COMMENT '公用数据扩展',
  `worldSchedule` mediumblob COMMENT '世界进程信息',
  PRIMARY KEY (`globalId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='公用数据保存表';

-- ----------------------------
-- Records of p_global
-- ----------------------------

-- ----------------------------
-- Table structure for p_global_activity
-- ----------------------------
DROP TABLE IF EXISTS `p_global_activity`;
CREATE TABLE `p_global_activity` (
  `activityType` int(11) NOT NULL,
  `goal` int(11) NOT NULL,
  `sortord` int(11) NOT NULL,
  `topupa` bigint(20) NOT NULL,
  `topupb` bigint(20) NOT NULL,
  `topupc` bigint(20) NOT NULL,
  `params` blob,
  `activityTime` int(11) NOT NULL,
  `recordTime` int(11) NOT NULL,
  `royalArena` blob COMMENT '阵营对拼活动数据',
  `auction` blob COMMENT '秋季拍卖',
  PRIMARY KEY (`activityType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of p_global_activity
-- ----------------------------

-- ----------------------------
-- Table structure for p_lord
-- ----------------------------
DROP TABLE IF EXISTS `p_lord`;
CREATE TABLE `p_lord` (
  `lordId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '玩家id',
  `nick` char(30) COLLATE utf8_bin DEFAULT NULL COMMENT '主公名字',
  `portrait` int(10) NOT NULL DEFAULT '1' COMMENT '头像',
  `sex` int(10) NOT NULL DEFAULT '0',
  `camp` int(10) NOT NULL DEFAULT '1' COMMENT '所属阵营',
  `level` int(10) NOT NULL DEFAULT '1' COMMENT '当前等级',
  `exp` bigint(20) NOT NULL DEFAULT '0' COMMENT '当前经验值',
  `vip` int(10) NOT NULL DEFAULT '0' COMMENT 'vip等级',
  `vipExp` int(11) NOT NULL,
  `topup` int(10) NOT NULL DEFAULT '0' COMMENT '总充值金额',
  `area` int(10) NOT NULL DEFAULT '0' COMMENT '玩家所属分区',
  `pos` int(10) NOT NULL DEFAULT '-1' COMMENT '坐标',
  `gold` int(10) NOT NULL DEFAULT '0' COMMENT '金币',
  `goldCost` int(10) NOT NULL DEFAULT '0' COMMENT '金币总消耗',
  `goldGive` int(10) NOT NULL DEFAULT '0' COMMENT '总共赠予的金币',
  `power` int(10) NOT NULL DEFAULT '0' COMMENT '体力',
  `ranks` int(10) NOT NULL DEFAULT '0' COMMENT '军阶',
  `exploit` bigint(20) NOT NULL DEFAULT '0' COMMENT '军功',
  `job` int(10) NOT NULL DEFAULT '0' COMMENT '玩家在军团中的职位',
  `fight` bigint(20) NOT NULL DEFAULT '0' COMMENT '战斗力',
  `newState` int(10) NOT NULL DEFAULT '0' COMMENT '新手引导步骤',
  `newerGift` int(10) NOT NULL DEFAULT '0' COMMENT '0未领取新手礼包 1已领取',
  `onTime` int(10) NOT NULL DEFAULT '0' COMMENT '最近一次上线时间',
  `olTime` int(10) NOT NULL DEFAULT '0' COMMENT '当日在线时长',
  `offTime` int(10) NOT NULL DEFAULT '0' COMMENT '最近一次离线时间',
  `ctTime` int(10) NOT NULL DEFAULT '0' COMMENT '在线奖励倒计时开始时间',
  `olAward` int(10) NOT NULL DEFAULT '0' COMMENT '领取了第几个在线奖励',
  `olMonth` int(10) NOT NULL DEFAULT '0' COMMENT '每月登录天数，值=月份*10000+登录时间*100+天数',
  `silence` int(10) NOT NULL DEFAULT '0' COMMENT '禁言',
  `combatId` int(11) NOT NULL,
  `heroToken` int(10) NOT NULL DEFAULT '0' COMMENT '将令',
  `mouthCardDay` int(11) NOT NULL DEFAULT '0' COMMENT '月卡剩余天数',
  `mouthCLastTime` int(10) NOT NULL DEFAULT '0' COMMENT ' 最近一次发放月卡时间',
  `credit` int(11) NOT NULL DEFAULT '0' COMMENT '师徒积分',
  `refreshTime` int(11) NOT NULL COMMENT '每日刷新',
  `signature` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '个性签名',
  `honor` int(11) NOT NULL COMMENT '荣誉点数',
  `goldBar` int(11) NOT NULL COMMENT '金条数',
  PRIMARY KEY (`lordId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of p_lord
-- ----------------------------

-- ----------------------------
-- Table structure for p_mail
-- ----------------------------
DROP TABLE IF EXISTS `p_mail`;
CREATE TABLE `p_mail` (
  `lordId` bigint(20) NOT NULL,
  `mails` mediumblob NOT NULL,
  `reports` mediumblob NOT NULL,
  `mails1` mediumblob,
  `mails2` mediumblob,
  `mails3` mediumblob,
  `mails4` mediumblob,
  `mails5` mediumblob,
  `mails6` mediumblob,
  `mails7` mediumblob,
  `mails8` mediumblob,
  `mails9` mediumblob,
  `mails10` mediumblob,
  `mails11` mediumblob,
  `mails12` mediumblob,
  `mails13` mediumblob,
  `mails14` mediumblob,
  `mails15` mediumblob,
  PRIMARY KEY (`lordId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of p_mail
-- ----------------------------

-- ----------------------------
-- Table structure for p_party
-- ----------------------------
DROP TABLE IF EXISTS `p_party`;
CREATE TABLE `p_party` (
  `camp` int(10) NOT NULL COMMENT '军团阵营',
  `partyLv` int(10) NOT NULL COMMENT '军团等级',
  `partyExp` int(10) NOT NULL COMMENT '军团本级经验',
  `status` int(3) NOT NULL COMMENT '军团当前状态（官员相关），0 未开启官员功能，1 官员投票中，2 已投票结束',
  `endTime` int(10) NOT NULL COMMENT '当前状态结束时间',
  `slogan` varchar(1024) COLLATE utf8_bin NOT NULL COMMENT '军团公告',
  `author` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '最后修改军团公告的玩家名称',
  `build` int(10) NOT NULL COMMENT '军团建设次数',
  `cityBattle` int(10) NOT NULL COMMENT '城战次数',
  `campBattle` int(10) NOT NULL COMMENT '阵营战次数',
  `cityRank` mediumblob NOT NULL COMMENT '每周城战次数排行榜',
  `campRank` mediumblob NOT NULL COMMENT '每周阵营战次数排行榜',
  `buildRank` mediumblob NOT NULL COMMENT '每周建设次数排行榜',
  `officials` blob NOT NULL COMMENT '军团现任官员列表',
  `log` mediumblob NOT NULL COMMENT '军团日志记录',
  `refreshTime` int(11) NOT NULL COMMENT '刷新时间',
  `ext` mediumblob NOT NULL COMMENT '军团附加信息',
  `qq` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '军团留言板qq',
  `wx` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '军团留言板wx',
  PRIMARY KEY (`camp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='军团表';

-- ----------------------------
-- Records of p_party
-- ----------------------------

-- ----------------------------
-- Table structure for p_party_member
-- ----------------------------
DROP TABLE IF EXISTS `p_party_member`;
CREATE TABLE `p_party_member` (
  `roleId` bigint(20) NOT NULL,
  `buildDate` int(10) NOT NULL COMMENT '玩家最后一次记录军团建设的日期',
  `build` int(10) NOT NULL COMMENT '玩家当天在军团中的建设次数',
  `cityDate` int(10) NOT NULL COMMENT '最后一次记录城战的日期',
  `cityBattle` int(10) NOT NULL COMMENT '记录玩家参与城战次数',
  `campDate` int(10) NOT NULL COMMENT '最后一次记录玩家参加阵营战的日期',
  `campBattle` int(10) NOT NULL COMMENT '记录玩家参与阵营战次数',
  `honorDate` int(10) NOT NULL COMMENT '记录玩家最后一次领取军团荣誉礼包的日期',
  `honorGift` int(10) NOT NULL COMMENT '记录玩家已领取的军团荣誉礼包，格式：101，表示1和3礼包已领取，2礼包未领取',
  `jobVote` int(10) NOT NULL COMMENT '玩家拥有的官员选举选票数',
  `canvass` int(10) NOT NULL DEFAULT '0' COMMENT '玩家拉票次数',
  `taskTime` int(10) NOT NULL,
  `taskAwardCnt` int(10) NOT NULL,
  PRIMARY KEY (`roleId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='军团成员信息记录';

-- ----------------------------
-- Records of p_party_member
-- ----------------------------

-- ----------------------------
-- Table structure for p_pay
-- ----------------------------
DROP TABLE IF EXISTS `p_pay`;
CREATE TABLE `p_pay` (
  `keyId` int(11) NOT NULL AUTO_INCREMENT,
  `serverId` int(11) DEFAULT NULL COMMENT '服务器id',
  `roleId` bigint(20) NOT NULL COMMENT '角色Id',
  `platNo` int(11) DEFAULT NULL COMMENT '渠道号',
  `platId` char(40) COLLATE utf8_bin DEFAULT NULL COMMENT '渠道平台账号',
  `orderId` char(64) COLLATE utf8_bin DEFAULT NULL COMMENT '渠道订单号',
  `serialId` char(64) COLLATE utf8_bin NOT NULL COMMENT '游戏内充值流水号(自己生成订单号)',
  `amount` int(11) NOT NULL DEFAULT '0' COMMENT '充值金额',
  `payTime` datetime DEFAULT NULL COMMENT '支付时间',
  `orderTime` datetime NOT NULL COMMENT '下单时间',
  `state` int(11) NOT NULL DEFAULT '0' COMMENT '订单状态,0 未支付,1 已支付',
  `payType` int(11) NOT NULL DEFAULT '0' COMMENT '充值类型',
  `usd` float(11,2) DEFAULT NULL COMMENT '海外平台使用美元(如果有美元字段则显示美元金额, 否则显示price)',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `serialId` (`serialId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of p_pay
-- ----------------------------

-- ----------------------------
-- Table structure for p_resource
-- ----------------------------
DROP TABLE IF EXISTS `p_resource`;
CREATE TABLE `p_resource` (
  `lordId` bigint(20) NOT NULL,
  `food` bigint(20) NOT NULL DEFAULT '0' COMMENT '粮食',
  `elec` bigint(20) NOT NULL DEFAULT '0' COMMENT '电',
  `oil` bigint(20) NOT NULL DEFAULT '0' COMMENT '油',
  `ore` bigint(20) NOT NULL DEFAULT '0' COMMENT '矿石',
  `arm1` bigint(20) NOT NULL COMMENT '兵营兵',
  `arm2` bigint(20) NOT NULL COMMENT '坦克兵',
  `arm3` bigint(20) NOT NULL COMMENT '装甲兵',
  `foodOut` bigint(20) NOT NULL COMMENT '当前粮食产出数量',
  `elecOut` bigint(20) NOT NULL,
  `oilOut` bigint(20) NOT NULL,
  `oreOut` bigint(20) NOT NULL,
  `foodOutF` int(10) NOT NULL DEFAULT '0' COMMENT '当前粮食产出增加百分比',
  `elecOutF` int(10) NOT NULL DEFAULT '0',
  `oilOutF` int(10) NOT NULL DEFAULT '0',
  `oreOutF` int(10) NOT NULL DEFAULT '0',
  `foodMax` bigint(20) NOT NULL COMMENT '当前最大可容纳粮食数量',
  `elecMax` bigint(20) NOT NULL,
  `oilMax` bigint(20) NOT NULL,
  `oreMax` bigint(20) NOT NULL,
  `storeF` int(10) NOT NULL DEFAULT '0' COMMENT '容量额外百分比',
  `tFood` bigint(20) NOT NULL COMMENT '玩家获取的粮食总量记录，只增不减',
  `tElec` bigint(20) NOT NULL,
  `tOil` bigint(20) NOT NULL,
  `tOre` bigint(20) NOT NULL,
  `storeTime` int(10) NOT NULL COMMENT '上次刷新资源分钟数',
  `human` bigint(20) NOT NULL COMMENT '人口',
  `humanTime` int(10) NOT NULL COMMENT '人口更新时间',
  `uranium` bigint(20) NOT NULL DEFAULT '0' COMMENT '铀',
  PRIMARY KEY (`lordId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of p_resource
-- ----------------------------

-- ----------------------------
-- Table structure for p_robot
-- ----------------------------
DROP TABLE IF EXISTS `p_robot`;
CREATE TABLE `p_robot` (
  `roleId` bigint(20) NOT NULL COMMENT '机器人对应的角色id',
  `treeId` int(10) NOT NULL COMMENT '机器人对应的行为树id',
  `robotState` int(3) NOT NULL DEFAULT '1' COMMENT '机器人状态，0 正常，1 失效',
  `guideIndex` int(10) NOT NULL DEFAULT '0' COMMENT '当前已执行的新手指引奖励index最大纪录',
  `posArea` int(10) NOT NULL COMMENT '机器人分配的区域',
  `actionType` int(10) NOT NULL COMMENT '机器人开启的行为, 1 活跃 , 0 不活跃 (地图上是否能看到机器人的行为)',
  PRIMARY KEY (`roleId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of p_robot
-- ----------------------------

-- ----------------------------
-- Table structure for p_smallid
-- ----------------------------
DROP TABLE IF EXISTS `p_smallid`;
CREATE TABLE `p_smallid` (
  `keyId` bigint(20) NOT NULL DEFAULT '0',
  `lordId` bigint(20) NOT NULL,
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`lordId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='小号表';

-- ----------------------------
-- Records of p_smallid
-- ----------------------------

-- ----------------------------
-- Table structure for s_server_setting
-- ----------------------------
DROP TABLE IF EXISTS `s_server_setting`;
CREATE TABLE `s_server_setting` (
  `paramId` int(11) NOT NULL,
  `title` char(20) COLLATE utf8_bin NOT NULL,
  `paramName` char(30) CHARACTER SET utf8 NOT NULL,
  `paramValue` varchar(255) COLLATE utf8_bin NOT NULL,
  `descs` varchar(255) CHARACTER SET utf8 NOT NULL,
  PRIMARY KEY (`paramId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of s_server_setting
-- ----------------------------
INSERT INTO `s_server_setting` VALUES ('1', '账号服务器地址', 'accountServerUrl', 'http://172.16.2.201:9200/civilization_account/account/inner.do', '账号服务器的url，账号服务器用来验证玩家身份信息');
INSERT INTO `s_server_setting` VALUES ('2', '测试模式', 'testMode', 'no', '(yes / no)是否开启测试模式，测试模式下不进行身份验证');
INSERT INTO `s_server_setting` VALUES ('3', '配置方式', 'configMode', 'db', '服务器配置方式(db/file), db方式下s_server_setting生效，file方式下gameServer.properties文件里的配置生效  ');
INSERT INTO `s_server_setting` VALUES ('4', '白名单模式', 'openWhiteName', 'no', '(yes / no)是否开启白名单模式，白名单模式下只有白名单玩家能进入游戏');
INSERT INTO `s_server_setting` VALUES ('5', '开启通信加密', 'cryptMsg', 'no', '(yes / no)是否对客户端的通信协议加密');
INSERT INTO `s_server_setting` VALUES ('6', '通信密码', 'msgCryptCode', '', '通信协议加密时使用的密码');
INSERT INTO `s_server_setting` VALUES ('7', '兑换码', 'convertUrl', '', '统计后台地址');
INSERT INTO `s_server_setting` VALUES ('8', '开充值入口', 'pay', 'yes', '客户端是否开充值');
INSERT INTO `s_server_setting` VALUES ('9', 'tcp端口', 'clientPort', '', '对外tcp连接端口');
INSERT INTO `s_server_setting` VALUES ('10', 'http端口', 'httpPort', '', '对外http连接端口');
INSERT INTO `s_server_setting` VALUES ('11', '区号', 'serverId', '', '区号');
INSERT INTO `s_server_setting` VALUES ('12', '开服时间', 'openTime', '', '本区开服时间');
INSERT INTO `s_server_setting` VALUES ('13', '服务器名', 'serverName', '', '');
INSERT INTO `s_server_setting` VALUES ('14', '活动模板ID', 'actMold', '1', '活动模板ID');
INSERT INTO `s_server_setting` VALUES ('15', '支付服务器', 'payServerUrl', 'http://172.16.2.201:9200/civilization_account/account/inner.do', '支付服务器url,用于支付使用');
INSERT INTO `s_server_setting` VALUES ('16', '运行环境', 'environment', 'release', '运行环境;test 表示测试环境, release 表示线上环境');
