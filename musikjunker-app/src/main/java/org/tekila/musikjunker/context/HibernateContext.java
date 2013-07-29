package org.tekila.musikjunker.context;

import java.util.Properties;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.MySQLInnoDBDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

@Slf4j
@Configuration
public class HibernateContext {

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private org.springframework.core.env.Environment environment;
	
	@Bean
	public AnnotationSessionFactoryBean annotationSessionFactory() {
		Properties props = new Properties();
		props.put(Environment.DIALECT, lookupDialect());
		props.put(Environment.SHOW_SQL, "false");
		props.put(Environment.FORMAT_SQL, "false");
		props.put(Environment.HBM2DDL_AUTO, environment.getProperty("musikjunker.db.schemaupdate", "create"));
		
		AnnotationSessionFactoryBean bean = new AnnotationSessionFactoryBean();
		bean.setDataSource(dataSource);
		bean.setHibernateProperties(props);
		bean.setPackagesToScan(new String[] { "org.tekila.musikjunker.domain"  });
		return bean;
	}
	
	private String lookupDialect() {
		String val = environment.getRequiredProperty("musikjunker.db.dialect");
		log.info("Looking up dialect for parameter '{}'", val);
		if ("mysql".equalsIgnoreCase(val)) {
			return MySQLInnoDBDialect.class.getName();
		} else if ("postgres".equals(val)) {
			return PostgreSQLDialect.class.getName();
		} else {
			try {
				return Class.forName(val).getName();
			} catch (ClassNotFoundException cnfe) {
				log.error("Cannot find suitable hibernate driver for {}", val);
				throw new IllegalArgumentException("Invalid parameter db.dialect: " + val);
			}
		}
	}

	@Bean
	public SessionFactory sessionFactory() {
		return (SessionFactory) annotationSessionFactory().getObject();
	}
	
	@Bean
	public HibernateTemplate hibernateTemplate() {
		return new HibernateTemplate(sessionFactory());
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource);
	}

}
