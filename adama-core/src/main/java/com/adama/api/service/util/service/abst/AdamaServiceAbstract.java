package com.adama.api.service.util.service.abst;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.adama.api.domain.util.domain.abst.delete.DeleteEntityAbstract;
import com.adama.api.repository.util.repository.AdamaMongoRepository;
import com.adama.api.service.util.service.AdamaServiceInterface;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

@Slf4j
public abstract class AdamaServiceAbstract<D extends DeleteEntityAbstract, R extends AdamaMongoRepository<D, String>> implements AdamaServiceInterface<D> {
	private R repo;

	@PostConstruct
	public abstract void init();

	@Override
	public D save(D adamaEntity) {
		log.debug("Request to save adamaEntity : {}", adamaEntity);
		return repo.save(adamaEntity);
	}

	@Override
	public List<D> findAll() {
		log.debug("Request to get all Entities");
		List<D> result = repo.findAll();
		return result;
	}

	@Override
	public Page<D> findAll(Pageable pageable) {
		log.debug("Request to get all Entities");
		Page<D> result = repo.findAll(pageable);
		return result;
	}

	@Override
	public D findOne(String id) {
		log.debug("Request to get Entity : {}", id);
		D entity = repo.findOne(id);
		return entity;
	}

	@Override
	public Iterable<D> findAll(List<String> idList) {
		return repo.findAll(idList);
	}

	@Override
	public Iterable<D> findAll(List<String> idList, Sort sort) {
		return repo.findAll(idList, Optional.of(sort));
	}

	@Override
	public void delete(String id) {
		log.debug("Request to delete Client : {}", id);
		repo.delete(id);
	}

	@Override
	public Page<D> searchAll(String key, Pageable pageable) {
		log.debug("Request to search Entity with key : {}", key);
		Page<D> result = repo.search(key, pageable);
		return result;
	}

	@Override
	public Long count() {
		log.debug("Request to count all");
		Long result = repo.count();
		return result;
	}

	/**
	 * Set the repository to use for this service
	 * 
	 * @param repo
	 */
	public void setRepo(R repo) {
		this.repo = repo;
	}
}
