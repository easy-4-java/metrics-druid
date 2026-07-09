package io.github.hiwepy.metrics.druid;

import com.alibaba.druid.stat.DruidDataSourceStatManager;
import com.alibaba.druid.stat.DruidStatService;
import com.codahale.metrics.health.HealthCheck;

public class DatabaseHealthCheck extends HealthCheck {

	private DruidStatService statService = DruidStatService.getInstance();

	public DatabaseHealthCheck() {
	}

	@Override
    public HealthCheck.Result check() throws Exception {
    	
		statService.getWallStatMap(null);
		
		DruidDataSourceStatManager.getInstances();
		
		
		DruidStatService.registerMBean();
    	
    	/*
        if (database.isConnected()) {
            return HealthCheck.Result.healthy();
        } else {
            return HealthCheck.Result.unhealthy("Cannot connect to " + database.getUrl());
        }*/
        
        return HealthCheck.Result.healthy();
        
    }
}