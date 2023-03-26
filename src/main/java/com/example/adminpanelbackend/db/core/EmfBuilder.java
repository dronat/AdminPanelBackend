package com.example.adminpanelbackend.db.core;

import javax.persistence.EntityManagerFactory;

public class EmfBuilder {
    private ConnectionConfig ConnectionConfig;

    public EmfBuilder() {
    }

    ConnectionConfig config() {
        if (this.ConnectionConfig == null) {
            this.ConnectionConfig = new ConnectionConfig();
        }

        return this.ConnectionConfig;
    }

    public EmfBuilder postgres() {
        this.config().jdbcClass = "org.postgresql.ds.PGSimpleDataSource";
        this.config().jdbcPrefix = "jdbc:postgresql";
        this.config().dialect = "org.hibernate.dialect.PostgreSQL94Dialect";
        return this;
    }

    public EmfBuilder mySql() {
        this.config().jdbcDriver = "com.mysql.cj.jdbc.Driver";
        this.config().jdbcClass = "com.mysql.cj.jdbc.MysqlDataSource";
        this.config().jdbcPrefix = "jdbc:mysql";
        this.config().dialect = "org.hibernate.dialect.MySQL8Dialect";
        return this;
    }

    public EmfBuilder h2() {
        this.config().jdbcClass = "org.h2.jdbcx.JdbcDataSource";
        this.config().jdbcPrefix = "jdbc:h2";
        this.config().dialect = "org.hibernate.dialect.H2Dialect";
        return this;
    }

    public EmfBuilder withJdbcUrl(String jdbcUrl) {
        this.config().jdbcUrl = jdbcUrl;
        return this;
    }

    public EmfBuilder withDbHost(String dbHost) {
        this.config().dbHost = dbHost;
        return this;
    }

    public EmfBuilder withDbName(String dbName) {
        this.config().dbName = dbName;
        return this;
    }

    public EmfBuilder withUsername(String username) {
        this.config().username = username;
        return this;
    }

    public EmfBuilder withPassword(String password) {
        this.config().password = password;
        return this;
    }

    public EmfBuilder withPersistenceUnitName(String persistenceUnitName) {
        this.config().persistenceUnitName = persistenceUnitName;
        return this;
    }

    public EmfBuilder withDbPort(int dbPort) {
        this.config().dbPort = dbPort;
        return this;
    }

    public EmfBuilder withHibernateDialect(String dialect) {
        this.config().dialect = dialect;
        return this;
    }

    public EntityManagerFactory build() {
        return EmfContext.INSTANCE.get(this.config().validate());
    }

    public EmfThreadLocal buildThreadLocal() {
        return new EmfThreadLocal(EmfContext.INSTANCE.get(this.config().validate()));
    }

    public EmfThreadLocal buildThreadLocalWithReload() {
        EmfContext.INSTANCE.reloadConnectionConfigContainer();
        return new EmfThreadLocal(EmfContext.INSTANCE.get(this.config().validate()));
    }
}
