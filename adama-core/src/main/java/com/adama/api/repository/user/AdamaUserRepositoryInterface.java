package com.adama.api.repository.user;

import com.adama.api.domain.user.AdamaUser;
import com.adama.api.domain.util.domain.abst.delete.DeleteEntityAbstract;
import com.adama.api.repository.util.repository.AdamaMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB interface for the AdamaUser entity.
 */
@Repository
public interface AdamaUserRepositoryInterface<D extends DeleteEntityAbstract, A extends AdamaUser<D>> extends AdamaMongoRepository<A, String> {
	Optional<A> findByResetKey(String resetKey);

	Optional<A> findByEmail(String email);

	Optional<A> findByLogin(String login);

	long count();
}
