package com.adama.api.security;

import com.adama.api.util.security.SecurityUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of AuditorAware based on Spring Security.
 */
@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
	@Override
	public Optional<String> getCurrentAuditor() {
		String userName = SecurityUtils.getCurrentUserLogin().orElse("Unknow");
		return Optional.of(userName);
	}
}
