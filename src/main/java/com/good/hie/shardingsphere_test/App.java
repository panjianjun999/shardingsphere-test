package com.good.hie.shardingsphere_test;

import java.sql.SQLException;

import com.good.hie.shardingsphere_test.jdbc.Manager;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "shardingsphere_test : Hello World!---20200401" );
        
        try {
			Manager.test();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
}
