package com.good.hie.shardingsphere_test.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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

        // 配置Order 分库 + 分表策略
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("t_order","database${0..1}.t_order_${0..1}");
        tableRuleConfiguration.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("playerId","database${playerId % 2}"));
        tableRuleConfiguration.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id","t_order_${order_id % 2}"));

     // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);

        DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfiguration,new Properties());
        
        //测试插入
//        testInsert(dataSource);
        
//        //测试删除
//        testDelete(dataSource);
        
        //测试更新
//        testUpdate(dataSource);
        
        //测试查询:全部
        testSelect(dataSource);
        
        //测试查询:部分
//        testSelectByProp(dataSource);
    }
    
    private static void testUpdate(DataSource dataSource) throws SQLException {
		String sql = "update t_order set name = ? where id = ?";
		Connection connection = dataSource.getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setString(1, "修改了哦-" + System.currentTimeMillis());
		preparedStatement.setLong(2, 1571817671464L);
		int r = preparedStatement.executeUpdate();
		System.out.println("update=" + r);
		
		connection.close();
	}
    
    private static void testDelete(DataSource dataSource) throws SQLException {
		String sql = "delete from t_order where id = ?";
		Connection connection = dataSource.getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setLong(1, 1571817671466L);
		int r = preparedStatement.executeUpdate();
		System.out.println("delete=" + r);
		
		connection.close();
	}

	private static void testSelectByProp(DataSource dataSource) throws SQLException {
		String sql = "select * from t_order where order_id = ?";
		Connection connection = dataSource.getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setInt(1, 0);//部分查询
		ResultSet r = preparedStatement.executeQuery();
		int i = 0;
		while(r.next()) {
			long id = r.getLong(1);
			int playerId = r.getInt(2);
			int order_id = r.getInt(3);
			String name = r.getString(4);
			System.out.println((++i) + ":" + id + "/" + playerId + "/" + order_id + "/" + name);
		}
		System.out.println("总数=" + i);
		
		connection.close();
	}
	
	private static void testSelect(DataSource dataSource) throws SQLException {
		String sql = "select * from t_order";
		Connection connection = dataSource.getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		ResultSet r = preparedStatement.executeQuery();
		int i = 0;
		while(r.next()) {
			long id = r.getLong(1);
			int playerId = r.getInt(2);
			int order_id = r.getInt(3);
			String name = r.getString(4);
			System.out.println((++i) + ":" + id + "/" + playerId + "/" + order_id + "/" + name);
		}
		System.out.println("总数=" + i);
		
		connection.close();
	}

	private static void testInsert(DataSource dataSource) throws SQLException {
		String sql = "insert into t_order (id,playerId,order_id,name) values (?, ?, ?, ?)";
		long id = System.currentTimeMillis();
        for (int playerId = 0; playerId < 10; playerId++) {
			for (int order_id = 0; order_id < 5; order_id++) {
				Connection connection = dataSource.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setLong(1, id++);//id
				preparedStatement.setInt(2, playerId);//playerId
				preparedStatement.setInt(3, order_id);//order_id
				preparedStatement.setString(4, "测试名称");//name
				boolean r = preparedStatement.execute();
				
				connection.close();
				System.out.println(playerId + "/" + order_id + "=" + r);
			}
		}
	}
}
