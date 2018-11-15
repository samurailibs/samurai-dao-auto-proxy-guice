package jp.dodododo.dao.lazyloading;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import jp.dodododo.dao.annotation.LazyLoading;

import org.junit.Test;

public class AutoProxyFactoryTest {

	@Test
	public void testCreate() throws Exception {
		TestTarget target = new AutoProxyFactory().create(TestTarget.class);

		assertNull(target.real());
		target.echo();
		assertNotNull(target.real());
	}

	@Test
	public void testCreate2() throws Exception {
		TestTarget target = new AutoProxyFactory().create(TestTarget.class, TestTarget.class.getConstructor(Integer.TYPE), 999);

		assertNull(target.real());
		target.echo();
		assertNotNull(target.real());
	}

	@Test
	public void testException() throws Exception {
		TestTarget target = new AutoProxyFactory().create(TestTarget.class,
				TestTarget.class.getConstructor(Integer.TYPE), 999);

		try {
			target.exception();
			fail();
		} catch (IOException e) {
			assertEquals("test", e.getMessage());
		}
	}

	@Test
	public void testException2() throws Exception {
		TestTarget target = new AutoProxyFactory().create(TestTarget.class,
				TestTarget.class.getConstructor(Integer.TYPE), 999);

		try {
			target.exception2();
			fail();
		} catch (FileNotFoundException e) {
			assertEquals("test2", e.getMessage());
		}
	}

	@Test
	public void testNoLazyLoading() throws Exception {
		TestTarget target = new AutoProxyFactory().create(TestTarget.class,
				TestTarget.class.getConstructor(Integer.TYPE), 999);

		assertNull(target.real());
		target.noLazyLoading();
		assertNull(target.real());
	}

	public static class TestTarget implements AutoLazyLoadingProxy<TestTarget> {

		private TestTarget real;

		private int i;

		protected TestTarget() {
		}

		public TestTarget(int i) {
			this.i = i;
		}

		@Override
		public TestTarget lazyLoad() {
			System.out.println("lazyLoad");
			return new TestTarget();
		}

		@Override
		public TestTarget real() {
			System.out.println("real");
			return real;
		}

		@Override
		public void setReal(TestTarget real) {
			System.out.println("setReal");
			this.real = real;
		}

		public void echo() {
			System.out.println("Echo xxx.... " + i);
		}

		public void exception() throws IOException {
			throw new IOException("test");
		}

		public void exception2() throws IOException {
			throw new FileNotFoundException("test2");
		}

		@LazyLoading(enable = false)
		public void noLazyLoading() {
			System.out.println("noLazyLoading");
		}
	}

}
