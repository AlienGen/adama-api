package com.adama.api.repository.util.repository;

import com.adama.api.domain.util.domain.abst.delete.DeleteEntityAbstract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Adama Mongo specific {@link org.springframework.data.repository.Repository}
 * interface.
 * 
 */
@NoRepositoryBean
public interface AdamaMongoRepository<T extends DeleteEntityAbstract, ID extends Serializable> extends MongoRepository<T, ID> {
	/**
	 * Search on the entity with the given key
	 * 
	 * @param key
	 *            the key for the search
	 * @param pageable
	 * @return
	 */
	Page<T> search(String key, Pageable pageable);

	/**
	 * find all with the query and the pageable
	 * 
	 * @param query
	 * @param pageable
	 * @return
	 */
	Page<T> findAllQueryPageable(Optional<Query> query, Optional<Pageable> pageable);

	/**
	 * find all with the query, the sortable and the pageable
	 * 
	 * @param query
	 * @param sort
	 * @param pageable
	 * @return
	 */
	Page<T> findAllQueryPageable(Optional<Query> queryOptional, Optional<Sort> sortOptional, Optional<Pageable> pageableOptional);

	/**
	 * find all with the query
	 * 
	 * @param query
	 * 
	 * @return
	 */
	List<T> findAll(Optional<Query> query);

	/**
	 * find all with the id list
	 * 
	 * @param idList
	 * @param sort
	 * 
	 * @return
	 */
	List<T> findAll(Iterable<ID> idList, Optional<Sort> sort);
	/**
	 * find one with the query
	 * 
	 * @param query
	 * 
	 * @return
	 */
	// T findOne(Optional<Query> query);
}