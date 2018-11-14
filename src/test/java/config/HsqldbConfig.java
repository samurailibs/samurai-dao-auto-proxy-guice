package config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import jp.dodododo.dao.unit.DBConfig;

public class HsqldbConfig implements DBConfig {

	@Override
	public String driverClassName() {
		return "org.hsqldb.jdbcDriver";
	}

	@Override
	public String URL() throws IOException, URISyntaxException {
		return "jdbc:hsqldb:file:"//
				+ new File(DBConfig.class.getResource("/log4j.properties").toURI()).getParentFile().getCanonicalPath()//
				+ "/data-hsqldb/test;shutdown=true";
	}

	@Override
	public String user() {
		return "sa";
	}

	@Override
	public String password() {
		return "";
	}
}
