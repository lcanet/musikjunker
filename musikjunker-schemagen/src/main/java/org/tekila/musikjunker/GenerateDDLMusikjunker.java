package org.tekila.musikjunker;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.MySQLInnoDBDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

public class GenerateDDLMusikjunker {

	public static void main(String[] args) throws Exception {
		
		DateFormat df = new SimpleDateFormat("yyyy_MM_dd");
		
		Properties props = new Properties();
		props.put(Environment.DIALECT, MySQLInnoDBDialect.class.getName());
		props.put(Environment.SHOW_SQL, "false");
		props.put(Environment.FORMAT_SQL, "false");
		props.put(Environment.GENERATE_STATISTICS, "true");

		AnnotationSessionFactoryBean bean = new AnnotationSessionFactoryBean();
		bean.setHibernateProperties(props);
		bean.setPackagesToScan(new String[] { "org.tekila.musikjunker.domain"  });
		bean.afterPropertiesSet();
		
		SchemaExport se = new SchemaExport(bean.getConfiguration());
		se.setOutputFile("sql/schema_" + df.format(new Date()) + ".sql");
		se.setDelimiter(";");
		se.create(true, false);
		System.exit(0);
	}
}
