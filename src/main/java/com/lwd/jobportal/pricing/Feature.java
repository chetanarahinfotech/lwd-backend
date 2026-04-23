package com.lwd.jobportal.pricing;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "features",
    uniqueConstraints = {
    		 @UniqueConstraint(
    		            name = "uk_feature_code_plan_type",
    		            columnNames = {"code", "plan_type"}
    		        )
    		 },
    indexes = {
        @Index(name = "idx_feature_code", columnList = "code")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 Use ENUM instead of String (FIXES YOUR ISSUE)
    @Column(nullable = false, length = 100)
    private String code; // 🔥 dynamic now

    
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType; // 🔥 IMPORTANT

    @Column(length = 255)
    private String description;
}
