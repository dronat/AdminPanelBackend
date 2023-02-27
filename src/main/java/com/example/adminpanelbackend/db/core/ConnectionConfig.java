package com.example.adminpanelbackend.db.core;

import java.util.Objects;

final class ConnectionConfig {
    public String dbHost;
    public Integer dbPort;
    public String dbName;
    public String username;
    public String password;
    public String persistenceUnitName;
    public String jdbcClass;
    public String jdbcDriver;
    public String jdbcPrefix;
    public String dialect;
    public String jdbcUrl;

    ConnectionConfig() {
    }

    public ConnectionConfig validate() {
        if (this.jdbcUrl == null) {
            if (this.dbHost == null) {
                throw new IllegalStateException("db host must not be null.");
            }

            if (this.dbName == null) {
                throw new IllegalStateException("db name must not be null.");
            }

            if (this.dbPort == null) {
                throw new IllegalStateException("db port must not be null.");
            }

            if (this.jdbcPrefix == null) {
                throw new IllegalStateException("JDBC prefix must not be null. For example: jdbc:postgresql or jdbc:mysql");
            }
        }

        if (this.username == null) {
            throw new IllegalStateException("db username must not be null.");
        } else if (this.password == null) {
            throw new IllegalStateException("db password must not be null.");
        } else if (this.persistenceUnitName == null) {
            throw new IllegalStateException("persistence unit name must not be null.");
        } else if (this.jdbcClass == null) {
            throw new IllegalStateException("JDBC class name must not be null.");
        } else if (this.dialect == null) {
            throw new IllegalStateException("Hibernate dialect must not be null.");
        } else if (this.jdbcDriver == null) {
            throw new IllegalStateException("Hibernate driver must not be null.");
        } else {
            return this;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ConnectionConfig that = (ConnectionConfig) o;
            return Objects.equals(this.dbHost, that.dbHost) && Objects.equals(this.dbName, that.dbName) && Objects.equals(this.username, that.username) && Objects.equals(this.password, that.password) && Objects.equals(this.persistenceUnitName, that.persistenceUnitName) && Objects.equals(this.dbPort, that.dbPort) && Objects.equals(this.jdbcClass, that.jdbcClass) && Objects.equals(this.jdbcPrefix, that.jdbcPrefix) && Objects.equals(this.dialect, that.dialect) && Objects.equals(this.jdbcUrl, that.jdbcUrl);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.dbHost, this.dbName, this.username, this.password, this.persistenceUnitName, this.dbPort, this.jdbcClass, this.jdbcPrefix, this.dialect, this.jdbcUrl});
    }
}
