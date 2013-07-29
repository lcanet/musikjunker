package org.tekila.musikjunker.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Component;

@Component
public class HibernateRepository {

	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	/* ********************* pagination ********************** */
	

	/* ********************* critera utils ********************** */

	
	public long findCount(DetachedCriteria criteria) {
		return findNumber(criteria);
	}

	@SuppressWarnings("unchecked")
	public long findNumber(DetachedCriteria criteria) {
		criteria.setProjection(Projections.rowCount());
		List<Number> ln = hibernateTemplate.findByCriteria(criteria);
		return ln.isEmpty() ? 0 : ln.get(0).longValue();
	}

	public boolean countGreaterThanZero(DetachedCriteria criteria) {
		return findCount(criteria) > 0;
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> List<T> findByCriteria(DetachedCriteria crit) {
		return hibernateTemplate.findByCriteria(crit);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> findByCriteria(final DetachedCriteria criteria, final int firstResult, final int maxResults) {
		return hibernateTemplate.findByCriteria(criteria, firstResult, maxResults);
	}	
	
	public <T> T findUnique(DetachedCriteria criteria) {
		List<T> lt = findByCriteria(criteria);
		return lt.isEmpty() ? null : lt.get(0);
	}

	
	/* ********************* access sesson  ********************** */
	
	public Session getSession() {
		if (hibernateTemplate.isAlwaysUseNewSession()) {
			return SessionFactoryUtils.getNewSession(hibernateTemplate.getSessionFactory(), 
					hibernateTemplate.getEntityInterceptor());
		}
		else if (hibernateTemplate.isAllowCreate()) {
			return SessionFactoryUtils.getSession(
					hibernateTemplate.getSessionFactory(), 
					hibernateTemplate.getEntityInterceptor(), 
					hibernateTemplate.getJdbcExceptionTranslator());
		}
		else if (SessionFactoryUtils.hasTransactionalSession(hibernateTemplate.getSessionFactory())) {
			return SessionFactoryUtils.getSession(hibernateTemplate.getSessionFactory(), false);
		}
		else {
			try {
				return hibernateTemplate.getSessionFactory().getCurrentSession();
			}
			catch (HibernateException ex) {
				throw new DataAccessResourceFailureException("Could not obtain current Hibernate Session", ex);
			}
		}
	}
	
	/* ********************* basic  ********************** */

	public <T> T get(Class<T> clazz, Serializable id) {
		return hibernateTemplate.get(clazz, id);
	}
	
	public <T> T merge(T entity) {
		return hibernateTemplate.merge(entity);
	}

	public <T> void persistTransient(T entity) {
		hibernateTemplate.saveOrUpdate(entity);
		hibernateTemplate.flush();
		hibernateTemplate.evict(entity);
	}


	public <T> void delete(T entity) {
		hibernateTemplate.delete(entity);
	}
	
	public <T> void delete(Class<T> clazz, Serializable id) {
		T entity = get(clazz, id);
		if (entity != null) {
			delete(entity);
		}
	}

	public <T> void deleteAll(Collection<T> lt) {
		hibernateTemplate.deleteAll(lt);
	}

	/**
	 * @param queryString
	 * @param values
	 * @return
	 * @throws DataAccessException
	 * @see org.springframework.orm.hibernate3.HibernateTemplate#find(java.lang.String, java.lang.Object[])
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> find(String queryString, Object... values)
			throws DataAccessException {
		return hibernateTemplate.find(queryString, values);
	}

	public <T> List<T> loadAll(Class<T> class1) {
		return hibernateTemplate.loadAll(class1);
	}

	public <T> T findUnique(String query, Object ... args) {
		List<T> lt = find(query, args);
		return lt.isEmpty() ? null : lt.get(0);
	}

	public <T> List<T> find(Class<T> class1, String prop, Object val) {
		DetachedCriteria crit = DetachedCriteria.forClass(class1);
		crit.add(Restrictions.eq(prop, val));
		return findByCriteria(crit);
	}

	public void evict(Object o) {
		hibernateTemplate.evict(o);
	}
	
	public <T> void evictAll(List<T> lo) {
		for (T obj : lo) {
			evict(obj);
		}
	}

	public <T> void save(T t) {
		hibernateTemplate.save(t);
		
	}
	
	public <T> void saveOrUpdate(T o) {
		hibernateTemplate.saveOrUpdate(o);
		
	}

	public <T> void saveOrUpdateAll(List<T> entities) {
		hibernateTemplate.saveOrUpdateAll(entities);
		
	}


}
