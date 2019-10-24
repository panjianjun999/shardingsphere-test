package com.good.hie.shardingsphere_test.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Manager {
	
    public static void test() throws SQLException {
    	 // 配置真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        
        String dbUrl = "jdbc:mysql://10.6.8.169/dbName?useUnicode=true&characterEncoding=UTF-8&useLocalSessionState=true&useSSL=false&serverTimezone=Asia/Shanghai";
        String dbUser = "root";
        String dbPw = "hzdbserver";

     // 配置第一个数据源
        BasicDataSource dataSource1 = new BasicDataSource();
        dataSource1.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource1.setUrl(dbUrl.replace("dbName", "test_shardingsphere_0"));
        dataSource1.setUsername(dbUser);
        dataSource1.setPassword(dbPw);
        dataSourceMap.put("database0", dataSource1);

        // 配置第二个数据源
        BasicDataSource dataSource2 = new BasicDataSource();
        dataSource2.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource2.setUrl(dbUrl.replace("dbName", "test_shardingsphere_1"));
        dataSource2.setUsername(dbUser);
        dataSource2.setPassword(dbPw);
        dataSourceMap.put("database1", dataSource2);

        // 配置表的分库 + 分表策略:分库分表
//        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("t_order","database${0..1}.t_order_${0..1}");
//        tableRuleConfiguration.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("playerId","database${playerId % 2}"));
//        tableRuleConfiguration.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id","t_order_${order_id % 2}"));
        
        // 配置表的分库 + 分表策略:只分库,不分表
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("tbHero","database${0..1}.tbHero");
        tableRuleConfiguration.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("playerId","database${playerId % 2}"));
//        tableRuleConfiguration.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("defineId","tbHero"));

     // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);
        
        //默认数据库
        shardingRuleConfiguration.setDefaultDataSourceName("database0");
        
        // 其他参数
     	Properties properties = new Properties();
     	properties.setProperty("sql.show", "true");//打印分库路由sql

        DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfiguration,properties);
        
        //测试插入
//        testInsert(dataSource);
        
        //测试查询:全部
        List<Long> playerIds = testSelect(dataSource,"tbPlayer");
        
        //测试查询:部分
        for (Long long1 : playerIds) {
        	System.out.println("玩家-" + long1 + "-的hero:");
        	testSelectByProp(dataSource,"tbHero",long1);
		}
        
//      //测试删除
//      testDelete(dataSource,"tbHero",1571912886468L);
      
      //测试更新
      testUpdate(dataSource,"tbPlayer",1571912886465L);
      testUpdate(dataSource,"tbHero",1571912886479L);
    }
    
    private static void testUpdate(DataSource dataSource,String tableName,long key) throws SQLException {
		String sql = "update " + tableName + " set name = ? where id = ?";
		Connection connection = dataSource.getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setString(1, "修改了哦-" + System.currentTimeMillis());
		preparedStatement.setLong(2, key);
		int r = preparedStatement.executeUpdate();
		System.out.println("update=" + r);
		
		connection.close();
	}
    
    private static void testDelete(DataSource dataSource,String tableName,long key) throws SQLException {
		String sql = "delete from " + tableName + " where id = ?";
		Connection connection = dataSource.getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setLong(1, key);
		int r = preparedStatement.executeUpdate();
		System.out.println("delete=" + r);
		
		connection.close();
	}

	private static List<Long> testSelectByProp(DataSource dataSource,String tableName,long key) throws SQLException {
		String sql = "select * from " + tableName + " where playerId = ?";
		Connection connection = dataSource.getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setLong(1, key);//部分查询
		ResultSet r = preparedStatement.executeQuery();

		int i = 0;
		List<Long> rs = new ArrayList<>();
		while(r.next()) {
			long id = r.getLong(1);
//			int playerId = r.getInt(2);
//			int order_id = r.getInt(3);
//			String name = r.getString(4);
			System.out.println("\t" + tableName + ":" + (++i) + "/" + id);
			
			rs.add(id);
		}
		System.out.println("\t" + tableName + ":总数=" + i);
		
		connection.close();
		
		return rs;
	}
	
	private static List<Long> testSelect(DataSource dataSource,String tableName) throws SQLException {
		String sql = "select * from " + tableName;
		Connection connection = dataSource.getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		ResultSet r = preparedStatement.executeQuery();
		
		int i = 0;
		List<Long> rs = new ArrayList<>();
		while(r.next()) {
			long id = r.getLong(1);
//			int playerId = r.getInt(2);
//			int order_id = r.getInt(3);
//			String name = r.getString(4);
			System.out.println(tableName + ":" + (++i) + "/" + id);
			
			rs.add(id);
		}
		System.out.println(tableName + ":总数=" + i);
		
		connection.close();
		
		return rs;
	}

	private static void testInsert(DataSource dataSource) throws SQLException {
		//插入tbPlayer
		String sql_1 = "insert into tbPlayer (id,accountId,name) values (?, ?, ?)";
		//插入tbHero
		String sql_2 = "insert into tbHero (id,playerId,defineId,name) values (?, ?, ?, ?)";
		
		long id_start = System.currentTimeMillis();
		long id_player = id_start;
		long id_hero = id_start;
		Connection connection = dataSource.getConnection();
        for (int i = 0; i < 4; i++) {
        	PreparedStatement preparedStatement1 = connection.prepareStatement(sql_1);
        	
        	long playerId = id_player++;
        	preparedStatement1.setLong(1, playerId);//playerId
        	preparedStatement1.setInt(2, i);//accountId
        	preparedStatement1.setString(3, "玩家-" + i);//name
        	boolean r1 = preparedStatement1.execute();
        	System.out.println("插入tbPlayer:" + playerId + "/" + r1);
        	
        	//插入tbHero
    		for (int j = 0; j < 4; j++) {
    			PreparedStatement preparedStatement2 = connection.prepareStatement(sql_2);
    			
    			long heroId = id_hero++;
    			preparedStatement2.setLong(1, heroId);//heroId
    			preparedStatement2.setLong(2, playerId);//playerId
    			preparedStatement2.setInt(3, j);//defineId
    			preparedStatement2.setString(4, "英雄-" + j);//name
    			boolean r2 = preparedStatement2.execute();
    			
    			System.out.println("\t 插入tbHero:" + playerId + "/" + heroId + "=" + r2);
    		}
		}
        
        connection.close();
	}
}
