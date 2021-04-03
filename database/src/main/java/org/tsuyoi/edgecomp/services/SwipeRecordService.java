package org.tsuyoi.edgecomp.services;

import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.tsuyoi.edgecomp.models.SwipeRecord;
import org.tsuyoi.edgecomp.utilities.SessionFactoryManager;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class SwipeRecordService {
    private static CLogger logger;

    public static void setPluginBuilder(PluginBuilder pluginBuilder) {
        logger = pluginBuilder.getLogger(SwipeRecordService.class.getName(), CLogger.Level.Info);
        SessionFactoryManager.setPluginBuilder(pluginBuilder);
    }

    public static synchronized SwipeRecord create(SwipeRecord object) {
        Session session = SessionFactoryManager.getSession();
        if (session == null)
            return null;
        try {
            session.getTransaction().begin();
            session.save( object );
            session.getTransaction().commit();
            return object;
        } catch (Exception e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            return null;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<SwipeRecord> all() {
        Session session = SessionFactoryManager.getSession();
        if (session == null)
            return new ArrayList<>();
        try {
            session.getTransaction().begin();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<SwipeRecord> query = builder.createQuery(SwipeRecord.class);
            Root<SwipeRecord> root = query.from(SwipeRecord.class);
            query.select(root);
            Query<SwipeRecord> typedQuery = session.createQuery(query);
            List<SwipeRecord> list = typedQuery.getResultList();
            session.getTransaction().commit();
            return list;
        } catch (Exception e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            return new ArrayList<>();
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static SwipeRecord getByID(String id) {
        Session session = SessionFactoryManager.getSession();
        if (session == null) {
            return null;
        }
        try {
            session.getTransaction().begin();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<SwipeRecord> query = builder.createQuery(SwipeRecord.class);
            Root<SwipeRecord> root = query.from(SwipeRecord.class);
            query.select(root).where(builder.equal(root.get("id"), id));
            Query<SwipeRecord> typedQuery = session.createQuery(query);
            SwipeRecord object = typedQuery.getSingleResult();
            session.getTransaction().commit();
            return object;
        } catch (Exception e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            e.printStackTrace();
            return null;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static long getCount(String site, String from, String to) {
        Session session = SessionFactoryManager.getSession();
        if (session == null) {
            return -1L;
        }
        try {
            session.getTransaction().begin();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<SwipeRecord> root = query.from(SwipeRecord.class);
            query.select(builder.count(root));
            Query<Long> typedQuery = session.createQuery(query);
            Long count = typedQuery.getSingleResult();
            session.getTransaction().commit();
            return count;
        } catch (RuntimeException e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            logger.error("getCount() Exception:\n" + ExceptionUtils.getStackTrace(e));
            return -1L;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static long getFilteredCount(String site, String from, String to, String filter) {
        Session session = SessionFactoryManager.getSession();
        if (session == null) {
            return -1L;
        }
        try {
            session.getTransaction().begin();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<SwipeRecord> root = query.from(SwipeRecord.class);
            query.select(
                    builder.count(root)
            );
            if (site != null) {
                query.where(
                        builder.and(
                                builder.le(root.get("ts"), Long.valueOf(from)),
                                builder.ge(root.get("ts"), Long.valueOf(to)),
                                builder.or(
                                        builder.like(
                                                builder.lower(root.get("site")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_id")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_first_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_last_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_email")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("error_msg")),
                                                builder.lower(builder.literal("%" + filter + "%")))
                                ),
                                builder.equal(root.get("site"), site)
                        )
                );
            } else {
                query.where(
                        builder.and(
                                builder.le(root.get("ts"), Long.valueOf(from)),
                                builder.ge(root.get("ts"), Long.valueOf(to)),
                                builder.or(
                                        builder.like(
                                                builder.lower(root.get("site")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_id")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_first_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_last_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_email")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("error_msg")),
                                                builder.lower(builder.literal("%" + filter + "%")))
                                )
                        )
                );
            }
            Query<Long> typedQuery = session.createQuery(query);
            Long filteredCount = typedQuery.getSingleResult();
            session.getTransaction().commit();
            return filteredCount;
        } catch (RuntimeException e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            logger.error("getFilteredCount() Exception:\n" + ExceptionUtils.getStackTrace(e));
            return -1L;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Object[]> list(String site, String from, String to, String sortBy, String dir, String filter, int start, int length) {
        Session session = SessionFactoryManager.getSession();
        if (session == null)
            return new ArrayList<>();
        try {
            session.getTransaction().begin();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
            Root<SwipeRecord> root = query.from(SwipeRecord.class);
            query.multiselect(
                    root.get("id"),
                    root.get("ts"),
                    root.get("site"),
                    root.get("swipe"),
                    root.get("user_id"),
                    root.get("user_name"),
                    root.get("user_first_name"),
                    root.get("user_last_name"),
                    root.get("user_email"),
                    root.get("error_msg")
            );
            if (site != null) {
                query.where(
                        builder.and(
                                builder.le(root.get("ts"), Long.valueOf(from)),
                                builder.ge(root.get("ts"), Long.valueOf(to)),
                                builder.or(
                                        builder.like(
                                                builder.lower(root.get("site")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_id")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_first_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_last_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_email")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("error_msg")),
                                                builder.lower(builder.literal("%" + filter + "%")))
                                ),
                                builder.equal(root.get("site"), site)
                        )
                );
            } else {
                query.where(
                        builder.and(
                                builder.le(root.get("ts"), Long.valueOf(from)),
                                builder.ge(root.get("ts"), Long.valueOf(to)),
                                builder.or(
                                        builder.like(
                                                builder.lower(root.get("site")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_id")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_first_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_last_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_email")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("error_msg")),
                                                builder.lower(builder.literal("%" + filter + "%")))
                                )
                        )
                );
            }
            if (dir.equals("asc"))
                query.orderBy(builder.asc(root.get(sortBy)));
            else
                query.orderBy(builder.desc(root.get(sortBy)));
            Query<Object[]> typedQuery = session.createQuery(query);
            typedQuery.setFirstResult(start);
            typedQuery.setMaxResults(length);
            List<Object[]> list = typedQuery.getResultList();
            session.getTransaction().commit();
            return list;
        } catch (Exception e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            if (logger != null)
                logger.error("list({},{},{},{},{},{},{},{})\n" + ExceptionUtils.getStackTrace(e), site, from, to,
                        sortBy, dir, filter, start, length);
            return new ArrayList<>();
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Object[]> download(String site, String from, String to, String filter) {
        Session session = SessionFactoryManager.getSession();
        if (session == null)
            return new ArrayList<>();
        try {
            session.getTransaction().begin();

            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
            Root<SwipeRecord> root = query.from(SwipeRecord.class);
            query.multiselect(
                    root.get("ts"),
                    root.get("site"),
                    root.get("user_id"),
                    root.get("user_name"),
                    root.get("user_first_name"),
                    root.get("user_last_name"),
                    root.get("user_email")
            );
            if (site != null) {
                query.where(
                        builder.and(
                                builder.le(root.get("ts"), Long.valueOf(from)),
                                builder.ge(root.get("ts"), Long.valueOf(to)),
                                builder.or(
                                        builder.like(
                                                builder.lower(root.get("site")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_id")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_first_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_last_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_email")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("error_msg")),
                                                builder.lower(builder.literal("%" + filter + "%")))
                                ),
                                builder.equal(root.get("site"), site)
                        )
                );
            } else {
                query.where(
                        builder.and(
                                builder.le(root.get("ts"), Long.valueOf(from)),
                                builder.ge(root.get("ts"), Long.valueOf(to)),
                                builder.or(
                                        builder.like(
                                                builder.lower(root.get("site")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_id")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_first_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_last_name")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("user_email")),
                                                builder.lower(builder.literal("%" + filter + "%"))),
                                        builder.like(
                                                builder.lower(root.get("error_msg")),
                                                builder.lower(builder.literal("%" + filter + "%")))
                                )
                        )
                );
            }
            Query<Object[]> typedQuery = session.createQuery(query);
            logger.trace("QueryString: {}", typedQuery.getQueryString());
            List<Object[]> list = typedQuery.getResultList();
            session.getTransaction().commit();
            return list;
        } catch (Exception e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            if (logger != null)
                logger.error("download({},{},{})\n" + ExceptionUtils.getStackTrace(e), site, from, to);
            return new ArrayList<>();
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> getSites() {
        Session session = SessionFactoryManager.getSession();
        if (session == null)
            return new ArrayList<>();
        try {
            session.getTransaction().begin();
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
            Root<SwipeRecord> root = criteriaQuery.from(SwipeRecord.class);
            criteriaQuery.select(root.get("site")).distinct(true);
            Query<String> typedQuery = session.createQuery(criteriaQuery);
            logger.trace("QueryString: {}", typedQuery.getQueryString());
            List<String> list = typedQuery.getResultList();
            session.getTransaction().commit();
            return list;
        } catch (Exception e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            return new ArrayList<>();
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }
}
