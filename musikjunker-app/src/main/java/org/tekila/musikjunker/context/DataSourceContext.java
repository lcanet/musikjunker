package org.tekila.musikjunker.context;


import javax.naming.NamingException;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiObjectFactoryBean;

/**
 * @author lc
 *
 */
@Slf4j
@Configuration
public class DataSourceContext {
	
	@Autowired
	private Environment environment;
	
	@Bean
	public DataSource dataSource() throws NamingException {
		String dbAccesMode = environment.getRequiredProperty("musikjunker.db");
		
		log.info("Building database from type " + dbAccesMode);
		
		if ("jndi".equalsIgnoreCase(dbAccesMode)) {
			return buildDataSourceJndi();
		} else if ("direct".equalsIgnoreCase(dbAccesMode)) {
			return buildDataSourceEmbedded();
		} else {
			throw new IllegalArgumentException("Unknown DB Access Mode " + dbAccesMode);
		}
	}

	private DataSource buildDataSourceEmbedded() {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(environment.getRequiredProperty("musikjunker.db.driver"));

		ds.setUrl(environment.getRequiredProperty("musikjunker.db.url"));
		ds.setUsername(environment.getProperty("musikjunker.db.user"));
		ds.setPassword(environment.getProperty("musikjunker.db.pass"));
		ds.setInitialSize(2);
		ds.setMaxActive(10);
		return ds;
	}

	private DataSource buildDataSourceJndi() throws NamingException {
		JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
		factory.setJndiName(environment.getProperty("musikjunker.db.jndiName", "jdbc/musikjunkerDataSource"));
		factory.afterPropertiesSet();
		
		return (DataSource) factory.getObject();
	}


}
