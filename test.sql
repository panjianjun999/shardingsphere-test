CREATE TABLE `t_order_0` (
  `id` bigint(20) NOT NULL COMMENT '唯一标识',
  `playerId` int(11) NOT NULL COMMENT '玩家id',
  `order_id` int(11) NOT NULL COMMENT '订单id',
  `name` char(32) DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`,`playerId`),
  KEY `indPlayerId` (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='测试订单'
partition by hash (playerId)
partitions 10;

CREATE TABLE `t_order_1` (
  `id` bigint(20) NOT NULL COMMENT '唯一标识',
  `playerId` int(11) NOT NULL COMMENT '玩家id',
  `order_id` int(11) NOT NULL COMMENT '订单id',
  `name` char(32) DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`,`playerId`),
  KEY `indPlayerId` (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='测试订单'
partition by hash (playerId)
partitions 10;