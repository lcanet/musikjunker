package org.tekila.musikjunker.context;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.MySQLInnoDBDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

@Configuration
public class HibernateContext {

	@Autowired
	private DataSource dataSource;
	
	@Bean
	public AnnotationSessionFactoryBean annotationSessionFactory() {
		Properties props = new Properties();
		props.put(Environment.DIALECT, MySQLInnoDBDialect.class.getName());
		props.put(Environment.SHOW_SQL, "false");
		props.put(Environment.FORMAT_SQL, "false");
		props.put(Environment.GENERATE_STATISTICS, "true");
		
		AnnotationSessionFactoryBean bean = new AnnotationSessionFactoryBean();
		bean.setDataSource(dataSource);
		bean.setHibernateProperties(props);
		bean.setPackagesToScan(new String[] { "org.tekila.musikjunker.domain"  });
		return bean;
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
