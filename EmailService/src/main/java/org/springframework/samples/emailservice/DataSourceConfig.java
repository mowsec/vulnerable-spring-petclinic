package org.springframework.samples.emailservice;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.samples.emailservice.model.Owner;
import org.springframework.samples.emailservice.model.Pet;
import org.springframework.samples.emailservice.model.PetType;
import org.springframework.samples.emailservice.model.Visit;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	basePackageClasses = { Owner.class,  Pet.class, Visit.class, PetType.class,  },
	entityManagerFactoryRef = "petclinicEntityManagerFactory",
	transactionManagerRef = "petclinicTransactionManager")
public class DataSourceConfig {

	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource.petclinic")
	public DataSourceProperties petclinicDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	public DataSource petclinicDataSource() {
		return petclinicDataSourceProperties().initializeDataSourceBuilder().build();
	}

	@Bean
	@Primary
	public JdbcTemplate petclinicJdbcTemplate(@Qualifier("petclinicDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	@Primary
	public LocalContainerEntityManagerFactoryBean petclinicEntityManagerFactory(
		@Qualifier("petclinicDataSource") DataSource dataSource, EntityManagerFactoryBuilder builder) {
		return builder.dataSource(dataSource)
			.packages(Owner.class, Pet.class, Visit.class, PetType.class)
			.build();
	}

	@Bean
	@Primary
	public PlatformTransactionManager petclinicTransactionManager(
		@Qualifier("petclinicEntityManagerFactory") LocalContainerEntityManagerFactoryBean petclinicEntityManagerFactory) {
		return new JpaTransactionManager(Objects.requireNonNull(petclinicEntityManagerFactory.getObject()));
	}

	@Bean
	@Primary
	public ResourceDatabasePopulator petclinicPopulator(
		@Value("${spring.datasource.petclinic.schema-locations}") String schema,
		@Value("${spring.datasource.petclinic.data-locations}") String data) {
		ResourceDatabasePopulator r = new ResourceDatabasePopulator(new ClassPathResource(schema), new ClassPathResource(data));
		r.setSeparator(";");
		return r;
	}

	@Bean
	@Primary
	public DataSourceInitializer petclinicInitializer(
		@Qualifier("petclinicPopulator") ResourceDatabasePopulator petclinicPopulator,
		@Qualifier("petclinicDataSource") DataSource dataSource) {

		DataSourceInitializer initializer = new DataSourceInitializer();
		initializer.setDataSource(dataSource);
		initializer.setDatabasePopulator(petclinicPopulator);

		return initializer;
	}

}
