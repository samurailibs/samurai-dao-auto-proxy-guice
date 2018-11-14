package jp.dodododo.dao.function;

import static jp.dodododo.dao.commons.Bool.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import java.util.Optional;
import javax.sql.DataSource;
import jp.dodododo.dao.annotation.Bean;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.annotation.Property;
import jp.dodododo.dao.id.Sequence;
import jp.dodododo.dao.impl.Dept;
import jp.dodododo.dao.impl.RdbDao;
import jp.dodododo.dao.lazyloading.AutoLazyLoadingProxy;
import jp.dodododo.dao.log.SqlLogRegistry;

import jp.dodododo.dao.unit.DbTestRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProxyTest {

	@Rule
	public DbTestRule dbTestRule = new DbTestRule();

	private RdbDao dao;

	@Test
	public void testInsertAndSelect() {
		dao = new RdbDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();
		DeptProxy.dao = dao;

		Emp emp = new Emp();
		emp.dept = new Dept();
		emp.dept.setDEPTNO("10");
		emp.dept.setDNAME("dept__name");
		emp.COMM = "2";
		// emp.EMPNO = "1";
		emp.TSTAMP = null;
		emp.TSTAMP = new Date();
		emp.NAME = "ename";
		dao.insert("emp", emp);
		// dao.insert(emp.dept);
		String empNo = emp.EMPNO;

		List<Emp> select = dao.select("select * from emp, dept where emp.deptno = dept.deptno and empno = " + empNo, Emp.class);
		System.out.println(select);
		assertEquals(empNo, select.get(0).EMPNO);
		assertEquals("2.00", select.get(0).COMM);
		assertEquals("ename", select.get(0).NAME);
		assertNotNull(select.get(0).TSTAMP);

		assertEquals("select * from emp, dept where emp.deptno = dept.deptno and empno = " + empNo, logRegistry.getLast()
				.getCompleteSql());
		assertEquals("10", select.get(0).dept.getDEPTNO());
		assertEquals("select * from dept where deptno =10", logRegistry.getLast().getCompleteSql());
	}

	public static class Emp {
		@Id(value = @IdDefSet(type = Sequence.class, name = "sequence"), targetTables = { "emp" })
		public String EMPNO;

		@Column("ename")
		public String NAME;

		@Column(table = "emp", value = "Tstamp")
		public Date TSTAMP;

		public String JOB;

		public String MGR;

		public String HIREDATE;

		public String SAL;

		public String COMM;

		@Property(readable = TRUE)
		private Dept dept;

		public Emp() {
		}

		public Emp(@Bean(DeptProxy.class) Dept dept) {
			this.dept = dept;
		}

	}

	public static class DeptProxy extends Dept implements AutoLazyLoadingProxy<Dept> {
		private static RdbDao dao;

		@Column("deptNO")
		public String DEPTNO;

		private Dept real;

		public DeptProxy() {
		}

		public DeptProxy(@Column("deptNO") String DEPTNO) {
			this.DEPTNO = DEPTNO;
		}

		public Dept lazyLoad() {
			Optional<Dept> dept = DeptProxy.dao.selectOne("select * from dept where deptno =" + DEPTNO, Dept.class);
			if(dept.isPresent()) {
				return dept.get();
			} else {
				return null;
			}
		}

		public Dept real() {
			return real;
		}

		public void setReal(Dept real) {
			this.real = real;
		}
	}

	private DataSource getDataSource() {
		return dbTestRule.getDataSource();
	}

	private Connection getConnection() throws SQLException {
		return dbTestRule.getConnection();
	}
}
