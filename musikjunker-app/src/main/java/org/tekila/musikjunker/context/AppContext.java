package org.tekila.musikjunker.context;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@PropertySource({"file:musikjunker.properties"})
@ComponentScan(basePackages = {"org.tekila.musikjunker"})
@Import({DataSourceContext.class, HibernateContext.class})
@EnableAsync
@EnableTransactionManagement(mode = AdviceMode.PROXY, order = 0)
@EnableAspectJAutoProxy
public class AppContext {


	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private DataSource dataSource;
	
	/* 
	 * **********************************
	 * TRANSACTIONS
	 * **********************************
	 */


	@Bean
	public PlatformTransactionManager transactionManager() {
		HibernateTransactionManager htm = new HibernateTransactionManager();
		htm.setSessionFactory(sessionFactory);
		htm.setDataSource(dataSource);
		htm.setDefaultTimeout(600);
		return htm;
	}

	@Bean
	public TransactionTemplate transactionTemplate() {
		TransactionTemplate template = new TransactionTemplate();
		template.setTransactionManager(transactionManager());
		template.setTimeout(600);
		return template;
	}

	/* 
	 * **********************************
	 * SERVICE
	 * **********************************
	 */


}
