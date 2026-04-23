package com.lwd.jobportal.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageAuditRepository extends JpaRepository<MessageAudit, Long> {
}
