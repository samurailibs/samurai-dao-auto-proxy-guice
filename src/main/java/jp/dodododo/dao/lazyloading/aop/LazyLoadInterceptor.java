package jp.dodododo.dao.lazyloading.aop;

import java.lang.reflect.Method;

import jp.dodododo.dao.annotation.LazyLoading;
import jp.dodododo.dao.annotation.Proxy;
import jp.dodododo.dao.exception.FailLazyLoadException;
import jp.dodododo.dao.exception.InvocationTargetRuntimeException;
import jp.dodododo.dao.lazyloading.AutoLazyLoadingProxy;
import jp.dodododo.dao.util.MethodUtil;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class LazyLoadInterceptor<T> implements MethodInterceptor {

	@SuppressWarnings("unused")
	private Class<T> targetClass;

	public LazyLoadInterceptor(Class<T> targetClass) {
		this.targetClass = targetClass;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		@SuppressWarnings("unchecked")
		AutoLazyLoadingProxy<T> proxy = (AutoLazyLoadingProxy<T>) invocation
				.getThis();

		Method method = invocation.getMethod();
		int paramLength = method.getParameterTypes().length;
		if (method.getAnnotation(Proxy.class) != null) {
			return invocation.proceed();
		}
		LazyLoading lazyLoading = method.getAnnotation(LazyLoading.class);
		if (lazyLoading != null && lazyLoading.enable() == false) {
			return invocation.proceed();
		}
		if ("lazyLoad".equals(method.getName()) == true && paramLength == 0) {
			return invocation.proceed();
		}
		if ("real".equals(method.getName()) == true && paramLength == 0) {
			return invocation.proceed();
		}
		if ("setReal".equals(method.getName()) == true && paramLength == 1) {
			return invocation.proceed();
		}
		if ("hashCode".equals(method.getName()) == true && paramLength == 0) {
			return invocation.proceed();
		}

		T real = proxy.real();

		if ("finalize".equals(method.getName()) == true && paramLength == 0) {
			if (real == null) {
				return invocation.proceed();
			}
		}

		synchronized (proxy) {
			if (real == null) {
				real = proxy.lazyLoad();
				if (real == null) {
					throw new FailLazyLoadException(proxy);
				}
				proxy.setReal(real);
			}
		}
		try {
			return MethodUtil.invoke(method, real, invocation.getArguments());
		} catch (InvocationTargetRuntimeException e) {
			Throwable cause = e.getCause().getCause();
			if (canThrow(cause, method)) {
				throw cause;
			} else {
				throw e;
			}

		}
	}

	private boolean canThrow(Throwable cause, Method method) {
		Class<? extends Throwable> causeClass = cause.getClass();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		for (Class<?> exceptionType : exceptionTypes) {
			if (exceptionType.isAssignableFrom(causeClass) == true) {
				return true;
			}
		}
		return false;
	}
}
