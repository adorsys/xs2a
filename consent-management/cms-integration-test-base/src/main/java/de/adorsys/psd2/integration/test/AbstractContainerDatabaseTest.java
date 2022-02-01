/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
