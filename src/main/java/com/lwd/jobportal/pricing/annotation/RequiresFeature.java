package com.lwd.jobportal.pricing.annotation;

import java.lang.annotation.*;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.pricing.enums.PlanName;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresFeature {

    String value();

    // 🔥 Allowed roles
    Role[] roles() default {};

    // 🔥 Optional: restrict to specific plans
    PlanName[] plans() default {};
}
