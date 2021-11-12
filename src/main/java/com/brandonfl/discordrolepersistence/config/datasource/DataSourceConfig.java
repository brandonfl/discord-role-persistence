package com.brandonfl.discordrolepersistence.config.datasource;

import java.text.MessageFormat;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

  @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${bot-datasource.host:}')")
  @Bean("mysqlDataSource")
  @Autowired
  public DataSource mysqlDataSource(DataSourceProperties dataSourceProperties) {
    DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
    dataSourceBuilder.url(MessageFormat
            .format("jdbc:mysql://{0}/{1}?serverTimezone={2}",
                dataSourceProperties.getHost(),
                dataSourceProperties.getName(),
                dataSourceProperties.getTimezone()));
    dataSourceBuilder.username(dataSourceProperties.getUsername());
    dataSourceBuilder.password(dataSourceProperties.getPassword());
    return dataSourceBuilder.build();
  }

  @ConditionalOnMissingBean(name = "mysqlDataSource")
  @Bean("h2DataSource")
  @Autowired
  public DataSource h2DataSource(DataSourceProperties dataSourceProperties) {
    DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName("org.h2.Driver");

    if (dataSourceProperties.getPath() != null && !Objects.equals(dataSourceProperties.getPath(),"")) {
      dataSourceBuilder.url("jdbc:h2:file:" + dataSourceProperties.getPath() + ";MODE=MYSQL");
    } else {
      dataSourceBuilder.url("jdbc:h2:mem:" + dataSourceProperties.getName() + ";MODE=MYSQL");
    }

    dataSourceBuilder.username(dataSourceProperties.getUsername());
    dataSourceBuilder.password(dataSourceProperties.getPassword());
    return dataSourceBuilder.build();
  }
}
