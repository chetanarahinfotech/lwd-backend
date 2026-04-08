package com.lwd.jobportal.messaging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lwd.jobportal.messaging.entity.MessageAudit;

public interface MessageAuditRepository extends JpaRepository<MessageAudit, Long> {
}
