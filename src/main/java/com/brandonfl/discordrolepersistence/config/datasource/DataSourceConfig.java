/*
 * MIT License
 *
 * Copyright (c) 2021 Fontany--Legall Brandon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
