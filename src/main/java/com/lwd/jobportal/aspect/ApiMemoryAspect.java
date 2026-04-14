package com.lwd.jobportal.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ApiMemoryAspect {
	private static final ThreadLocal<Boolean> isControllerCall = ThreadLocal.withInitial(() -> false);
	
	
	@Around("execution(* com.lwd.jobportal..controller..*(..))")
	public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
	    return logExecution(joinPoint, "CONTROLLER");
	}

	@Around("execution(* com.lwd.jobportal..service..*(..))")
	public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
	    return logExecution(joinPoint, "SERVICE");
	}

	private Object logExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {

	    Runtime runtime = Runtime.getRuntime();

	    boolean isTopLevel = false;

	    // Detect top-level controller call
	    if (layer.equals("CONTROLLER")) {
	        isControllerCall.set(true);
	        isTopLevel = true;
	    }

	    long before = runtime.totalMemory() - runtime.freeMemory();
	    long start = System.currentTimeMillis();

	    Object result = joinPoint.proceed();

	    long after = runtime.totalMemory() - runtime.freeMemory();
	    long end = System.currentTimeMillis();

	    // ✅ Print logs
	    if (isTopLevel) {
	        log.info("🌐 API: {} | Memory: {} KB | Time: {} ms",
	                joinPoint.getSignature().toShortString(),
	                (after - before) / 1024,
	                (end - start));

	        isControllerCall.remove();
	    } else if (isControllerCall.get()) {
	        log.info("   ↳ {}: {} | {} KB | {} ms",
	                layer,
	                joinPoint.getSignature().toShortString(),
	                (after - before) / 1024,
	                (end - start));
	    }

	    return result;
	}
}