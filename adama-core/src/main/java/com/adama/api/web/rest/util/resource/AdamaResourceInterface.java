package com.adama.api.web.rest.util.resource;

import com.adama.api.domain.util.domain.abst.delete.DeleteEntityAbstract;
import com.adama.api.service.excel.exception.ExcelException;
import com.adama.api.web.rest.util.dto.abst.AdamaDtoAbstract;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;

public interface AdamaResourceInterface<T extends DeleteEntityAbstract, D extends AdamaDtoAbstract> {
	/**
	 * POST /entities : Create a new Entity.
	 *
	 * @param dto
	 *            the entityDTO to create
	 * @param request
	 * @return the ResponseEntity with status 201 (Created) and with body the
	 *         new entityDTO, or with status 400 (Bad Request) if the entity has
	 *         already an ID, or have duplicate
	 * @throws URISyntaxException
	 *             if the Location URI syntax is incorrect
	 */
	@ApiOperation(value = "Create a new Entity")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Entity created return in body"), @ApiResponse(code = 400, message = "Entity has already an ID, or have duplicate") })
	ResponseEntity<D> createEntity(D dto, HttpServletRequest request) throws URISyntaxException;

	/**
	 * PUT /entities : Updates an existing Entity.
	 *
	 * @param dto
	 *            the entityDTO to update
	 * @param request
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         entityDTO, or with status 400 (Bad Request) if the entityDTO is
	 *         not valid, or with status 500 (Internal Server Error) if the
	 *         entityDTO couldn't be updated
	 * @throws URISyntaxException
	 *             if the Location URI syntax is incorrect
	 */
	@ApiOperation(value = "Updates an existing Entity.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Entity updated return in body"), @ApiResponse(code = 400, message = "The Entity is not valid"),
			@ApiResponse(code = 500, message = " The Entity couldnt be updated") })
	ResponseEntity<D> updateEntity(D dto, HttpServletRequest request) throws URISyntaxException;

	/**
	 * GET /entities : get all the Entities. Can paginate with page=1&size=20.
	 * Sorting with sort='field'.ASC. Get Excel by Header Accept as
	 * 'application/vnd.ms-excel'
	 * 
	 * @param pageable
	 *            the pagination information
	 * @param search
	 *            the value for searching
	 * @param all
	 *            if you want get all the object
	 * @param request
	 * @return the ResponseEntity with status 200 (OK) and the list of Entities
	 *         in body
	 * @throws URISyntaxException
	 *             if there is an error to generate the pagination HTTP headers
	 * @throws ExcelException
	 *             if problem during excel generation
	 */
	@ApiOperation(value = "Get all the Entities.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "The list of Entities return in body") })
	ResponseEntity<?> getAllEntities(String search, Boolean all, Pageable pageable, HttpServletRequest request) throws URISyntaxException, ExcelException;;

	/**
	 * GET /entities/:id : get the "id" Entity.
	 *
	 * @param id
	 *            the id of the EntityDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         EntityDTO, or with status 404 (Not Found)
	 */
	@ApiOperation(value = "Get the 'id' Entity.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "The Entity return in body"), @ApiResponse(code = 404, message = "Entity not found") })
	ResponseEntity<D> getEntity(String id);

	/**
	 * DELETE /entities/:id : delete the "id" Entity.
	 *
	 * @param id
	 *            the id of the EntityDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@ApiOperation(value = "Delete the 'id' Entity.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Entity have been deleted") })
	ResponseEntity<Void> deleteEntity(String id);

	/**
	 * POST /entities/excel : Updates a list of Entities with excel file.
	 *
	 * If the entity exist it's updated, if not it's created. Entity exists if
	 * id exists.
	 *
	 * @param file
	 *            the excel file with entity to upload
	 * @return the ResponseEntity with status 200 (OK), with status 500
	 *         (Internal Server Error)
	 * @throws URISyntaxException
	 *             if the Location URI syntax is incorrect
	 * @throws Exception
	 */
	@ApiOperation(value = "Updates a list of Entities with excel file.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "the entities have been updated or created"), @ApiResponse(code = 500, message = " The Entity couldnt be updated") })
	ResponseEntity<?> updateEntityExcel(MultipartFile file) throws URISyntaxException, Exception;
}