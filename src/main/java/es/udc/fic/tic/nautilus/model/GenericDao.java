package es.udc.fic.tic.nautilus.model;

import java.io.Serializable;

import javax.management.InstanceNotFoundException;

/**
 * Interface to GenericDao
 * 
 * @author Luis Lorenzo
 */
public interface GenericDao<E, PK extends Serializable> {
	
	/**
	 * Save or update the entity
	 * 
	 * @param E entity
	 */
	void save(E entity);
	
	/**
	 * Find the entity by the id
	 * 
	 * @param int id
	 * @return E entity
	 * @throws InstanceNotFoundException
	 */
	E findById(PK id) throws InstanceNotFoundException;
	
	/**
	 * Delete the entity
	 * 
	 * @param E entity
	 * @throws InstanceNotFoundException
	 */
	void delete(E entity) throws InstanceNotFoundException;
}
