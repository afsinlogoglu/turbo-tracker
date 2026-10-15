package com.turbo.tracker.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.turbo.tracker.entity.Task;
import com.turbo.tracker.entity.TaskPriority;
import com.turbo.tracker.entity.TaskStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class TaskSearch {

	private final EntityManager em;

	public TaskSearch(EntityManager em) {
		this.em = em;
	}

	public List<Task> find(Criteria criteria, int page, int size) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Task> query = cb.createQuery(Task.class);
		Root<Task> root = query.from(Task.class);
		query.where(predicates(cb, root, criteria));
		query.orderBy(order(cb, root, criteria.sort()));
		TypedQuery<Task> typed = em.createQuery(query);
		typed.setFirstResult(page * size);
		typed.setMaxResults(size);
		return typed.getResultList();
	}

	public long count(Criteria criteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Task> root = query.from(Task.class);
		query.select(cb.count(root)).where(predicates(cb, root, criteria));
		return em.createQuery(query).getSingleResult();
	}

	private Predicate[] predicates(CriteriaBuilder cb, Root<Task> root, Criteria criteria) {
		List<Predicate> predicates = new ArrayList<>();
		if (criteria.projectId() != null) {
			predicates.add(cb.equal(root.get("project").get("id"), criteria.projectId()));
		}
		if (criteria.projectIds() != null) {
			if (criteria.projectIds().isEmpty()) {
				predicates.add(cb.disjunction());
			}
			else {
				predicates.add(root.get("project").get("id").in(criteria.projectIds()));
			}
		}
		if (criteria.status() != null) {
			predicates.add(cb.equal(root.get("status"), criteria.status()));
		}
		if (criteria.priority() != null) {
			predicates.add(cb.equal(root.get("priority"), criteria.priority()));
		}
		if (criteria.unassigned()) {
			predicates.add(cb.isNull(root.get("assignee")));
		}
		else if (criteria.assigneeId() != null) {
			predicates.add(cb.equal(root.get("assignee").get("id"), criteria.assigneeId()));
		}
		if (criteria.query() != null && !criteria.query().isBlank()) {
			String like = "%" + criteria.query().trim().toLowerCase() + "%";
			predicates.add(cb.or(
					cb.like(cb.lower(root.get("title")), like),
					cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like)));
		}
		return predicates.toArray(Predicate[]::new);
	}

	private jakarta.persistence.criteria.Order order(CriteriaBuilder cb, Root<Task> root, String sort) {
		if ("title".equals(sort)) {
			return cb.asc(root.get("title"));
		}
		if ("priority".equals(sort)) {
			return cb.asc(cb.selectCase()
					.when(cb.equal(root.get("priority"), TaskPriority.HIGH), 0)
					.when(cb.equal(root.get("priority"), TaskPriority.MEDIUM), 1)
					.otherwise(2));
		}
		return cb.desc(root.get("createdAt"));
	}

	public record Criteria(
			Long projectId,
			List<Long> projectIds,
			String query,
			TaskStatus status,
			TaskPriority priority,
			Long assigneeId,
			boolean unassigned,
			String sort) {
	}

}
