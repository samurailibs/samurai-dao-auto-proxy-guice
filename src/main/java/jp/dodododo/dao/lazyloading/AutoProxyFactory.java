package jp.dodododo.dao.lazyloading;

import com.google.inject.matcher.AbstractMatcher;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewConstructor;
import jp.dodododo.dao.lazyloading.aop.LazyLoadInterceptor;
import jp.dodododo.dao.message.Message;
import jp.dodododo.dao.util.CacheUtil;
import jp.dodododo.dao.util.ClassUtil;
import jp.dodododo.dao.util.ConstructorUtil;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;

public class AutoProxyFactory extends ProxyFactory {

	private final static Map<Class<?>, Injector> GUICES = CacheUtil.cacheMap();
	private final static Map<Class<?>, Class<?>> TARGET_CLASSES = CacheUtil.cacheMap();

	@Override
	public <T> T create(Class<T> clazz) {

		validate(clazz);

		Class<T> targetClass = getTargetClass(clazz);
		Injector injector = getGuice(targetClass);
		return injector.getInstance(targetClass);
	}

	protected <T> Class<T> getTargetClass(Class<T> clazz) {
		Class<?> targetClass = TARGET_CLASSES.get(clazz);
		if (targetClass != null) {
			return (Class<T>) targetClass;
		}
		if (Modifier.isAbstract(clazz.getModifiers())) {
			ClassPool classPool = ClassPool.getDefault();
			CtClass cc = classPool.makeClass(clazz.getName() + "$$CreateBySamuraiDao");
			try {
				cc.setSuperclass(classPool.makeClass(clazz.getName()));
				cc.addConstructor(CtNewConstructor.defaultConstructor(cc));
				targetClass = (Class<T>) cc.toClass();
			} catch (CannotCompileException e) {
				throw new RuntimeException(e);
			}
		} else {
			targetClass = clazz;
		}
		TARGET_CLASSES.put(clazz, targetClass);
		return (Class<T>) targetClass;
	}

	private static synchronized <T> Injector getGuice(Class<T> clazz) {
		Injector ret = GUICES.get(clazz);
		if (ret != null) {
			return ret;
		}
		ret = Guice.createInjector(new ProxyModule<>(clazz));
		GUICES.put(clazz, ret);
		return ret;
	}

	@Override
	public <T> T create(Class<T> clazz, Constructor<T> constructor, Object... args) {

		validate(clazz);

		Injector injector = Guice.createInjector(new ProxyModule<>(clazz));
		T tmpInstance = injector.getInstance(clazz);
		@SuppressWarnings("unchecked")
		Class<T> targetClass = (Class<T>) tmpInstance.getClass();
		T ret = ConstructorUtil.newInstance(ClassUtil.getConstructor(targetClass, constructor.getParameterTypes()), args);
		ProxyObjectInitializer.init(clazz, ret);
		return ret;
	}

	private static <T> void validate(Class<T> clazz) {

		try {
			clazz.getConstructor((Class<?>[]) null);
			return;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException ignore) {
		}
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor((Class<?>[]) null);
			int modifiers = constructor.getModifiers();
			if (Modifier.isProtected(modifiers) == false) {
				throw new RuntimeException(Message.getMessage("00025"));
			}
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(Message.getMessage("00025"), e);
		}
	}

	public static class ProxyModule<T> extends AbstractModule {

		private Class<T> targetClass;

		public ProxyModule(Class<T> targetClass) {
			this.targetClass = targetClass;
		}

		@Override
		protected void configure() {
			bind(targetClass);
			bindInterceptor(Matchers.any(), MethodMatcher.INSTANCE, new LazyLoadInterceptor<>(targetClass));
		}
	}

	public static class MethodMatcher extends AbstractMatcher<Method> {
		public static MethodMatcher INSTANCE = new MethodMatcher();

		@Override
		public boolean matches(Method method) {
			if (method.isSynthetic() || method.isBridge()) {
				return false;
			} else {
				return true;
			}
		}
	}
}
