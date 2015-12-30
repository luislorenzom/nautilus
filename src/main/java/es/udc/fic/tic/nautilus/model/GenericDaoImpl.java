package es.udc.fic.tic.nautilus.model;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import javax.management.InstanceNotFoundException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of GenericDao Interface
 * 
 * @author Luis Lorenzo
 */
public class GenericDaoImpl<E, PK extends Serializable> implements
GenericDao<E, PK> {
	
	private SessionFactory sessionFactory;

	private Class<E> entityClass;

	@SuppressWarnings("unchecked")
	public GenericDaoImpl() {
		this.entityClass = (Class<E>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	public void save(E entity) {
		getSession().saveOrUpdate(entity);
	}

	@SuppressWarnings("unchecked")
	public E findById(PK id) throws InstanceNotFoundException {
		E entity = (E) getSession().get(entityClass, id);
		if (entity == null) {
			throw new InstanceNotFoundException();
		}
		return entity;
	}

	public void delete(E entity) throws InstanceNotFoundException {
		getSession().delete(entity);
	}

}
