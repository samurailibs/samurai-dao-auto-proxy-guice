package jp.dodododo.dao.lazyloading;

import java.lang.reflect.Field;

public class ProxyObjectInitializer {

	private static final AutoProxyFactory AUTO_PROXY_FACTORY = new AutoProxyFactory();

	public static <T> void init(Class<T> clazz, T target) {
		T t = AUTO_PROXY_FACTORY.create(clazz);

		@SuppressWarnings("unchecked")
		Class<T> enhancedClass = (Class<T>) target.getClass();
		Field[] declaredFields = enhancedClass.getDeclaredFields();
		try {
			for (Field field : declaredFields) {
				if (field.getName().startsWith("CGLIB$BOUND")
						&& field.getType().equals(Boolean.TYPE)) {
					field.setAccessible(true);
					field.set(target, Boolean.TRUE);
				}
				if (field.getName().startsWith("CGLIB$CALLBACK_")
						&& field.getType().getName()
								.startsWith("com.google.inject.internal")) {
					field.setAccessible(true);
					if (field.get(target) != null) {
						continue;
					}
					Field field2 = enhancedClass.getDeclaredField(field
							.getName());
					field2.setAccessible(true);
					field.set(target, field2.get(t));
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

}
