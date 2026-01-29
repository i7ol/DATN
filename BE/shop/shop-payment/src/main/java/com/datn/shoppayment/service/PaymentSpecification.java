package com.datn.shoppayment.service;

import com.datn.shopdatabase.entity.PaymentEntity;
import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class PaymentSpecification {

    public static Specification<PaymentEntity> filter(
            PaymentStatus status,
            PaymentMethod method,
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));

            if (method != null)
                predicates.add(cb.equal(root.get("method"), method));

            if (fromDate != null)
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        fromDate.atStartOfDay()
                ));

            if (toDate != null)
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        toDate.atTime(23, 59, 59)
                ));

            if (minAmount != null)
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("amount"), minAmount
                ));

            if (maxAmount != null)
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("amount"), maxAmount
                ));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
