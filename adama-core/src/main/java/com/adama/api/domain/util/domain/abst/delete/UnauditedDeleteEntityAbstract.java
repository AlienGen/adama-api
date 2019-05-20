package com.adama.api.domain.util.domain.abst.delete;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class UnauditedDeleteEntityAbstract implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String ACTIVE_FIELD_NAME = "active";
	public static final String ID_FIELD_NAME = "id";
	@Id
	@Field(ID_FIELD_NAME)
	private String id;
	@Field(ACTIVE_FIELD_NAME)
	private Boolean active = true;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
