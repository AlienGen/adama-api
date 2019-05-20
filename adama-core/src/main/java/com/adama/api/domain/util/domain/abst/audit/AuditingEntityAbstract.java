package com.adama.api.domain.util.domain.abst.audit;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Base abstract class for entities which will hold definitions for created,
 * last modified by and created, last modified by date.
 */
@Data
public abstract class AuditingEntityAbstract implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String CREATEBY_FIELD_NAME = "created_by";
	public static final String CREATEDATE_FIELD_NAME = "created_date";
	public static final String LASTMODIFIEDBY_FIELD_NAME = "last_modified_by";
	public static final String LASTMODIFIEDDATE_FIELD_NAME = "last_modified_date";
	public static final String VERSION_FIELD_NAME = "version";
	@CreatedBy
	@Field(CREATEBY_FIELD_NAME)
	protected String createdBy;
	@CreatedDate
	@Field(CREATEDATE_FIELD_NAME)
	protected ZonedDateTime createdDate = ZonedDateTime.now();
	@LastModifiedBy
	@Field(LASTMODIFIEDBY_FIELD_NAME)
	protected String lastModifiedBy;
	// FIXME: Not working with nested object.
	// @LastModifiedDate
	@Field(LASTMODIFIEDDATE_FIELD_NAME)
	protected ZonedDateTime lastModifiedDate = ZonedDateTime.now();
}
