package global.config;

import java.util.Properties;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import jakarta.persistence.EntityManagerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableTransactionManagement // Spring이 트랜잭션 AOP 프록시를 생성하도록 설정하는 역할
@EnableJpaRepositories(basePackages = {"domain","auth","infra"}) 
@ComponentScan(basePackages={"domain","auth","infra"})
@PropertySource("classpath:application.properties")
public class DataSourceConfig {

  @Value("${aws.rds.endpoint}")
  private String url;
  @Value("${aws.rds.username}")
  private String username;
  @Value("${aws.rds.password}")
  private String password;

  @Value("${aws.credentials.access-key}")
  private String awsAccessKey;
  @Value("${aws.credentials.secret-key}")
  private String awsSecretKey;
  @Value("${aws.region.static}")
  private String awsRegion;
  
  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
        .build();
  }
  
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean factoryBean =
        new LocalContainerEntityManagerFactoryBean();
    factoryBean.setDataSource(dataSource());
    factoryBean.setPackagesToScan("domain","auth");
    factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

    Properties jpaProperties = new Properties();
    jpaProperties.put("hibernate.hbm2ddl.auto", "update");
    jpaProperties.put("hibernate.show_sql", "true");
    jpaProperties.put("hibernate.format_sql", "true");
    jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
    factoryBean.setJpaProperties(jpaProperties);

    return factoryBean;
  }

  @Bean(destroyMethod = "close")
  public DataSource dataSource() {
    DataSource ds = new DataSource();
    ds.setDriverClassName("com.mysql.cj.jdbc.Driver"); // JDBC 드라이버 클래스를 MySQL 드라이버 클래스로 지정.
    ds.setUrl(url);
    ds.setUsername(username);
    ds.setPassword(password);
    ds.setInitialSize(10);
    ds.setMaxActive(10);
    ds.setTestWhileIdle(true);
    ds.setMinEvictableIdleTimeMillis(1000 * 60 * 3);
    ds.setTimeBetweenEvictionRunsMillis(1000 * 10);
    return ds;
  }

  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }
}
