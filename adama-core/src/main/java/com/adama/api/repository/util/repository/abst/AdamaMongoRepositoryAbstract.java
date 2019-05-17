package com.adama.api.repository.util.repository.abst;

import com.adama.api.domain.util.domain.abst.delete.DeleteEntityAbstract;
import com.adama.api.repository.util.repository.AdamaMongoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.bson.Document;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Abstract Repository base implementation for Mongo.
 * 
 */
@Slf4j
public abstract class AdamaMongoRepositoryAbstract<T extends DeleteEntityAbstract, ID extends Serializable> implements AdamaMongoRepository<T, ID> {
	public final MongoOperations mongoOperations;
	public final MongoEntityInformation<T, ID> entityInformation;
	@Inject
	public MongoTemplate mongoTemplate;

	/**
	 * Creates a new {@link AdamaMongoRepositoryAbstract} for the given
	 * {@link MongoEntityInformation} and {@link MongoTemplate}.
	 * 
	 * @param metadata
	 *            must not be {@literal null}.
	 * @param mongoOperations
	 *            must not be {@literal null}.
	 */
	public AdamaMongoRepositoryAbstract(MongoEntityInformation<T, ID> metadata, MongoOperations mongoOperations) {
		Assert.notNull(mongoOperations);
		Assert.notNull(metadata);
		this.entityInformation = metadata;
		this.mongoOperations = mongoOperations;
	}

	@Override
	public <S extends T> S save(S entity) {
		Assert.notNull(entity, "Entity must not be null!");
		if (entityInformation.isNew(entity)) {
			entity.setActive(true);
			mongoOperations.insert(entity, entityInformation.getCollectionName());
		} else {
			mongoOperations.save(entity, entityInformation.getCollectionName());
		}
		return entity;
	}

	/**
	 * @deprecated Retrocompatibility
	 *
	 * @param iterable
	 * @param <S>
	 * @return
	 */
	public <S extends T> List<S> save(Iterable<S> iterable) {
		return this.saveAll(iterable);
	}

	@Override
	public <S extends T> List<S> saveAll(Iterable<S> entities) {
		Assert.notNull(entities, "The given Iterable of entities not be null!");
		List<S> result = convertIterableToList(entities);
		boolean allNew = result.parallelStream().allMatch(entity -> !entityInformation.isNew(entity));
		if (allNew) {
			Stream<S> stream = result.parallelStream().peek(entity -> entity.setActive(true));
			mongoOperations.insertAll(stream.collect(Collectors.toList()));
		} else {
			result.parallelStream().forEach(entity -> save(entity));
		}
		return result;
	}

	@Override
	public boolean existsById(ID id) {
		Assert.notNull(id, "The given id must not be null!");
		Query query = new Query(getIdCriteria(id));
		Class<T> entityClass = entityInformation.getJavaType();
		String collectionName = entityInformation.getCollectionName();
		return mongoOperations.exists(query, entityClass, collectionName);
	}

	@Override
	public long count() {
		return count(new Query());
	}

	@Override
	public void deleteById(ID id) {
		Assert.notNull(id, "The given id must not be null!");
		T entity = findById(id).get();
		Assert.notNull(entity, "Cannot find the entity with this id!");
		entity.setActive(false);
		save(entity);
	}

	@Override
	public void delete(T entity) {
		Assert.notNull(entity, "The given entity must not be null!");
		deleteById(entityInformation.getId(entity));
	}

	@Override
	public void deleteAll(Iterable<? extends T> entities) {
		Assert.notNull(entities, "The given Iterable of entities not be null!");
		entities.forEach(entity -> delete(entity));
	}

	@Override
	public void deleteAll() {
		mongoOperations.findAll(entityInformation.getJavaType()).parallelStream().forEach(entity -> delete(entity));
	}

	@Override
	public <S extends T> S insert(S entity) {
		Assert.notNull(entity, "Entity must not be null!");
		entity.setActive(true);
		mongoOperations.insert(entity, entityInformation.getCollectionName());
		return entity;
	}

	@Override
	public <S extends T> List<S> insert(Iterable<S> entities) {
		Assert.notNull(entities, "The given Iterable of entities must not be null!");
		List<S> list = convertIterableToList(entities);
		if (!list.isEmpty()) {
			Stream<S> stream = list.parallelStream().peek(entity -> entity.setActive(true));
			mongoOperations.insertAll(stream.collect(Collectors.toList()));
		}
		return list;
	}

	@Override
	public Page<T> search(String key, final Pageable pageable) {
		Field[] allFields = entityInformation.getJavaType().getDeclaredFields();
		List<Criteria> criterias = new ArrayList<>();
		Page<T> result;
		if (key != null && !key.isEmpty()) {
			Assert.notNull(pageable, "pageable must not be null!");
			Arrays.asList(allFields).stream().filter(field -> !ClassUtils.isPrimitiveOrWrapper(field.getType()) && Modifier.isPrivate(field.getModifiers()))
					.forEach(field -> criterias.add(Criteria.where(field.getName()).regex(key, "i")));
			Query query = new Query();
			query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()]))).with(pageable);
			List<T> list = findAll(query, pageable.getSort(), pageable);
			Long count = count(query);
			result = new PageImpl<>(list, pageable, count);
		} else {
			result = findAllQueryPageable(null, pageable);
		}
		return result;
	}

	@Override
	public Optional<T> findById(ID id) {
		return Optional.ofNullable(mongoOperations.findById(id, entityInformation.getJavaType(), entityInformation.getCollectionName()));
	}

	public T findById(Query query) {
		return mongoOperations.findOne(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
	}

	@Override
	public <S extends T> Optional<S> findOne(Example<S> example) {
		return null; // TODO
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example) {
		return null; // TODO
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
		return null; // TODO
	}

	@Override
	public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
		return null; // TODO
	}

	@Override
	public <S extends T> long count(Example<S> example) {
		return 0; // TODO
	}

	@Override
	public <S extends T> boolean exists(Example<S> example) {
		return false; // TODO
	}

	@Override
	public List<T> findAll(Query query) {
		return mongoOperations.find(query, entityInformation.getJavaType());
		// return findAll(query, sortOptional, pageableOptional);
	}

	@Override
	public List<T> findAll() {
		return findAll(null, null, null);
	}

	@Override
	public Iterable<T> findAllById(Iterable<ID> ids) {
		List<ID> parameters = convertIterableToList(ids);
		return findAll(new Query(new Criteria(entityInformation.getIdAttribute()).in(parameters)), null, null);
	}

	@Override
	public List<T> findAll(Iterable<ID> ids, Sort sort) {
		List<ID> parameters = convertIterableToList(ids);
		return findAll(new Query(new Criteria(entityInformation.getIdAttribute()).in(parameters)), sort, null);
	}

	@Override
	public Page<T> findAll(final Pageable pageable) {
		// query
		Query query = new Query();
		List<T> list = findAll(query.with(pageable), pageable.getSort(), pageable);
		// count
		Query queryCount = new Query(getFilterCriteria());
		Long count = count(queryCount);
		// result
		return new PageImpl<>(list, pageable, count);
	}

	@Override
	public List<T> findAll(Sort sort) {
		Query query = new Query().with(sort);
		return findAll(query, sort, null);
	}

	@Override
	public Page<T> findAllQueryPageable(Query query, Pageable pageable) {
		Sort sort = pageable != null ? pageable.getSort() : null;
		// query
		List<T> list = findAll(query, sort, pageable);
		// count
		Query countQuery = query != null ? query : new Query().addCriteria(getFilterCriteria());
		Long count = count(countQuery);
		// result
		return new PageImpl<>(list, pageable, count);
	}

	public Page<T> findAllQueryPageable(Query query, Sort sort, Pageable pageable) {
		// query
		List<T> list = findAll(query, sort, pageable);
		// count
		Query countQuery = query != null ? query : new Query().addCriteria(getFilterCriteria());
		Long count = count(countQuery);
		// result
		return new PageImpl<>(list, pageable, count);
	}

	public List<T> findAll(Query query, Sort sort, Pageable pageable) {
		// Set default query.
		if (query == null) {
			query = new Query();
		}
		query.addCriteria(getFilterCriteria());
		// get the list of sorting with primitive field
		List<Order> orderPrimitiveList = new ArrayList<>();
		List<Order> orderDBRefList = new ArrayList<>();
		if (sort != null) {
			for (Order order : sort) {
				if (isTheFieldExistAndIsPrimitive(order.getProperty())) {
					orderPrimitiveList.add(order);
				} else if (isTheFieldExistAndIsDBRef(order.getProperty())) {
					orderDBRefList.add(order);
				}
			}
		}
		// get the list of sorting with DBRef field
		if (orderDBRefList.isEmpty() && orderPrimitiveList.isEmpty()) {
			if (sort != null) {
				query.with(sort);
			}
			if (pageable != null) {
				query.with(pageable);
			}
			return mongoOperations.find(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
		}
		// FIXME sort: works for only one criteria
		// FIXME sort: for multi-criteria the sort is not specialized
		// (citeria 1, than 2 if entities both have same criteria 1)
		ListOrderedSet<T> result = new ListOrderedSet<>();
		if (!orderDBRefList.isEmpty()) {
			final Query subquery = query;
			orderDBRefList.forEach(order -> result.addAll(findAllWithDBRef(subquery, order)));
		}
		if (!orderPrimitiveList.isEmpty()) {
			return sortPrimitiveWithCaseInsensitive(query, orderPrimitiveList, pageable);
		}
		return result.asList();
	}

	private List<T> sortPrimitiveWithCaseInsensitive(Query query, List<Order> orderPrimitiveList, Pageable pageable) {
		MongoCollection collection = mongoOperations.getCollection(entityInformation.getCollectionName());
		List<AggregationOperation> operations = new ArrayList<>();
		if (query != null) {
			DBObject match = new BasicDBObject();
			match.put("$match", query.getQueryObject());
			operations.add(new DBObjectToAggregationOperation(match));
		}
		DBObject prjflds = new BasicDBObject();
		prjflds.put("doc", "$$ROOT");
		orderPrimitiveList.stream().forEach(order -> prjflds.put("insensitive" + order.getProperty(), new BasicDBObject("$toLower", "$" + order.getProperty())));
		DBObject project = new BasicDBObject();
		project.put("$project", prjflds);
		operations.add(new DBObjectToAggregationOperation(project));
		DBObject sortflds = new BasicDBObject();
		orderPrimitiveList.stream().forEach(order -> sortflds.put("insensitive" + order.getProperty(), Direction.ASC.equals(order.getDirection()) ? 1 : -1));
		DBObject sort = new BasicDBObject();
		sort.put("$sort", sortflds);
		operations.add(new DBObjectToAggregationOperation(sort));
		if (pageable != null) {
			DBObject skip = new BasicDBObject();
			skip.put("$skip", pageable.getOffset());
			operations.add(new DBObjectToAggregationOperation(skip));
			DBObject limit = new BasicDBObject();
			limit.put("$limit", pageable.getPageSize());
			operations.add(new DBObjectToAggregationOperation(limit));
		}
		AggregationOptions options = AggregationOptions.builder().allowDiskUse(true).build();
		Aggregation aggregation = Aggregation.newAggregation(operations).withOptions(options);
		AggregationResults<Document> results = mongoOperations.aggregate(aggregation, entityInformation.getCollectionName(), Document.class);
		List<T> objects = new ArrayList<>();
		log.warn("Aggregation query done! Convertion of DBObject to entities...");
		for (Document result : results) {
			Document bson = (Document) result.get("doc");
			objects.add(mongoOperations.getConverter().read(entityInformation.getJavaType(), bson));
		}
		log.warn("DBObject converted to entities!");
		return objects;
	}

	private List<T> findAllWithDBRef(Query query, Order order) {
		Query queryFull = new Query();
		if (query != null) {
			for (String key : query.getQueryObject().keySet()) {
				Object object = query.getQueryObject().get(key);
				Criteria criteria = Criteria.where(key).is(object);
				queryFull.addCriteria(criteria);
			}
		}
		List<T> fullEntityList = mongoOperations.find(queryFull, entityInformation.getJavaType(), entityInformation.getCollectionName());
		int index = order.getProperty().indexOf(".");
		String dbRefFieldName = order.getProperty().substring(0, index);
		String fieldToSortInDBRef = order.getProperty().substring(index + 1);
		Field dbRefField = ReflectionUtils.findField(entityInformation.getJavaType(), dbRefFieldName);
		ReflectionUtils.makeAccessible(dbRefField);
		List<Object> entityDBRefObjectList = fullEntityList.parallelStream().map(entity -> {
			try {
				return dbRefField.get(entity);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				return null;
			}
		}).filter(myObject -> myObject != null).collect(Collectors.toList());
		if (entityDBRefObjectList != null && entityDBRefObjectList.size() != 0) {
			Class<? extends Object> myClass = entityDBRefObjectList.get(0).getClass();
			List<String> entityDBRefIdList = entityDBRefObjectList.stream().map(object -> (DeleteEntityAbstract.class.cast(object)).getId()).collect(Collectors.toList());
			// TODO add the sort to this function
			List<? extends Object> dbRefList = mongoOperations.find(
					queryFull.addCriteria(Criteria.where(DeleteEntityAbstract.ID_FIELD_NAME).in(entityDBRefIdList)).with(new Sort(order.getDirection(), fieldToSortInDBRef)), myClass);
			Collections.sort(fullEntityList, new Comparator<T>() {
				@Override
				public int compare(T left, T right) {
					try {
						return Integer.compare(dbRefList.indexOf(dbRefField.get(left)), dbRefList.indexOf(dbRefField.get(right)));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						return 0;
					}
				}
			});
		}
		return fullEntityList;
	}

	public long count(Query query) {
		return mongoOperations.getCollection(entityInformation.getCollectionName()).count(query.getQueryObject());
	}

	protected static <T> List<T> convertIterableToList(Iterable<T> entities) {
		if (entities instanceof List) {
			return (List<T>) entities;
		}
		return StreamSupport.stream(entities.spliterator(), false).collect(Collectors.toList());
	}

	private boolean isTheFieldExistAndIsPrimitive(String fieldName) {
		try {
			Field orderField = entityInformation.getJavaType().getDeclaredField(fieldName);
			return !ClassUtils.isPrimitiveOrWrapper(orderField.getType());
		} catch (NoSuchFieldException | SecurityException e) {
			// nothing to do
		}
		return false;
	}

	private boolean isTheFieldExistAndIsDBRef(String fieldName) {
		try {
			if (fieldName.contains(".")) {
				Field orderField = entityInformation.getJavaType().getDeclaredField(fieldName.substring(0, fieldName.indexOf(".")));
				return orderField.getAnnotation(DBRef.class) != null;
			}
		} catch (NoSuchFieldException | SecurityException e) {
			// nothing to do
		}
		return false;
	}

	protected abstract Criteria getIdCriteria(Object id);

	protected abstract Criteria getFilterCriteria();

	public class DBObjectToAggregationOperation implements AggregationOperation {
		private String jsonOperation;

		public DBObjectToAggregationOperation(DBObject operation) {
			this.jsonOperation = ((BasicDBObject) operation).toJson();
		}

		@Override
		public Document toDocument(AggregationOperationContext aggregationOperationContext) {
			return aggregationOperationContext.getMappedObject(Document.parse(jsonOperation));
		}
	}
}
