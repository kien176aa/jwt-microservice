package com.javatechie.repository;

import com.javatechie.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> searchWithOperators(String name, String nameOp,
                                             Double price, String priceOp,
                                             Integer quantity, String quantityOp,
                                             Boolean status,
                                             String sortBy, String sortDir) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        // Name filter with operators
        if (name != null && !name.trim().isEmpty() && nameOp != null) {
            switch (nameOp) {
                case "=":
                    predicates.add(cb.equal(cb.lower(root.get("name")), name.toLowerCase()));
                    break;
                case "<>":
                    predicates.add(cb.notEqual(cb.lower(root.get("name")), name.toLowerCase()));
                    break;
                case "like":
                default:
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
                    break;
            }
        }

        // Price filter with operators
        if (price != null && priceOp != null) {
            switch (priceOp) {
                case "=":
                    predicates.add(cb.equal(root.get("price"), price));
                    break;
                case "<>":
                    predicates.add(cb.notEqual(root.get("price"), price));
                    break;
                case ">":
                    predicates.add(cb.greaterThan(root.get("price"), price));
                    break;
                case "<":
                    predicates.add(cb.lessThan(root.get("price"), price));
                    break;
                case ">=":
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), price));
                    break;
                case "<=":
                    predicates.add(cb.lessThanOrEqualTo(root.get("price"), price));
                    break;
            }
        }

        // Quantity filter with operators
        if (quantity != null && quantityOp != null) {
            switch (quantityOp) {
                case "=":
                    predicates.add(cb.equal(root.get("quantity"), quantity));
                    break;
                case "<>":
                    predicates.add(cb.notEqual(root.get("quantity"), quantity));
                    break;
                case ">":
                    predicates.add(cb.greaterThan(root.get("quantity"), quantity));
                    break;
                case "<":
                    predicates.add(cb.lessThan(root.get("quantity"), quantity));
                    break;
                case ">=":
                    predicates.add(cb.greaterThanOrEqualTo(root.get("quantity"), quantity));
                    break;
                case "<=":
                    predicates.add(cb.lessThanOrEqualTo(root.get("quantity"), quantity));
                    break;
            }
        }

        // Status filter (boolean)
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        // Apply WHERE clause
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Apply SORTING
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            String field = sortBy.trim().toLowerCase();
            String direction = (sortDir != null && sortDir.trim().equalsIgnoreCase("desc")) ? "desc" : "asc";

            // Validate sortBy field
            if (field.equals("name") || field.equals("price") || field.equals("quantity")) {
                if (direction.equals("desc")) {
                    query.orderBy(cb.desc(root.get(field)));
                } else {
                    query.orderBy(cb.asc(root.get(field)));
                }
            }
        }

        return entityManager.createQuery(query).getResultList();
    }
}