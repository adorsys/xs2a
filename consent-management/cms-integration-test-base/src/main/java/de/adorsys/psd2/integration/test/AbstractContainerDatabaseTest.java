/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.integration.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("rawtypes")
public class AbstractContainerDatabaseTest {
    private static Connection connection;

    private HikariDataSource dataSource;

    void performQuery(JdbcDatabaseContainer container, String sql) {
        Connection con;
        try {
            con = getConnection(container);
        } catch (SQLException e) {
            fail("Connection creation failed: " + e.getMessage());
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            fail("Execution of sql statement failed: " + e.getMessage());
        }
    }

    DataSource getDataSource(JdbcDatabaseContainer container) {
        if (this.dataSource != null) {
            return this.dataSource;
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(container.getJdbcUrl());
        hikariConfig.setUsername(container.getUsername());
        hikariConfig.setPassword(container.getPassword());
        hikariConfig.setDriverClassName(container.getDriverClassName());

        this.dataSource = new HikariDataSource(hikariConfig);
        return this.dataSource;
    }

    private Connection getConnection(JdbcDatabaseContainer container) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        connection = getDataSource(container).getConnection();    //NOSONAR
        return connection;
    }

    @AfterAll
    static void afterAll() throws SQLException {
        if (connection != null && connection.isClosed()) {
            connection.close();
        }
    }
}
