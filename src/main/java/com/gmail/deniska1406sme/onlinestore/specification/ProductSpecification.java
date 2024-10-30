package com.gmail.deniska1406sme.onlinestore.specification;

import com.gmail.deniska1406sme.onlinestore.model.Product;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ProductSpecification {

    public static Specification<Product> filterByAttributes(Map<String, List<String>> filters, Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
                String attributeName = entry.getKey();
                List<String> attributeValues = entry.getValue();

                if (!attributeValues.isEmpty()) {
                    MapJoin<Product, String, String> attributesJoin = root.joinMap("attributes");
                    predicates.add(criteriaBuilder.and(
                            criteriaBuilder.equal(attributesJoin.key(), attributeName),
                            attributesJoin.value().in(attributeValues)
                    ));
                }
            }

            predicates.add(criteriaBuilder.between(root.get("price"), minPrice, maxPrice));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
