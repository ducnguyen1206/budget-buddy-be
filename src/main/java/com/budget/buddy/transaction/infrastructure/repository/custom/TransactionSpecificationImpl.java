package com.budget.buddy.transaction.infrastructure.repository.custom;

import com.budget.buddy.transaction.application.dto.transaction.TransactionFilterCriteria;
import com.budget.buddy.transaction.domain.model.transaction.Transaction;
import com.budget.buddy.transaction.infrastructure.repository.TransactionSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class TransactionSpecificationImpl implements TransactionSpecification {
    // Field names to avoid magic strings
    private static final String FIELD_SOURCE_ACCOUNT = "sourceAccount";
    private static final String FIELD_ID = "id";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_REMARKS = "remarks";
    private static final String FIELD_CURRENCY = "currency";
    private static final String FIELD_IS_NOT = "is not";
    private static final String FIELD_IS = "is";

    private static final Set<String> VALID_SORT_FIELDS = Set.of(FIELD_DATE, FIELD_AMOUNT, FIELD_NAME);

    @Override
    public Specification<Transaction> buildSpecification(TransactionFilterCriteria criteria, String sort) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria != null) {
                addAccountFilter(criteria, root, predicates);
                addCategoryFilter(criteria, root, predicates);
                addNameFilter(criteria, root, builder, predicates);
                addDateFilter(criteria, root, builder, predicates);
                addAmountFilter(criteria, root, builder, predicates);
                addCurrenciesFilter(criteria, root, predicates);
                addTypesFilter(criteria, root, predicates);
                addRemarksFilter(criteria, root, builder, predicates);
            }

            // Apply sorting (default: date desc)
            if (query != null) {
                applySorting(root, query, builder, sort);
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addAccountFilter(TransactionFilterCriteria criteria, Root<Transaction> root, List<Predicate> predicates) {
        TransactionFilterCriteria.IdsFilter accounts = criteria.getAccounts();
        filterIDs(root, predicates, accounts, FIELD_SOURCE_ACCOUNT);
    }

    private void addCategoryFilter(TransactionFilterCriteria criteria, Root<Transaction> root, List<Predicate> predicates) {
        TransactionFilterCriteria.IdsFilter categories = criteria.getCategories();
        filterIDs(root, predicates, categories, FIELD_CATEGORY);
    }

    private void filterIDs(Root<Transaction> root, List<Predicate> predicates, TransactionFilterCriteria.IdsFilter idsFilter, String fieldCategory) {
        if (idsFilter != null && !CollectionUtils.isEmpty(idsFilter.getIds())) {
            String op = normalize(idsFilter.getOperator());
            Expression<Long> path = root.get(fieldCategory).get(FIELD_ID).as(Long.class);
            if (FIELD_IS.equals(op)) {
                predicates.add(path.in(idsFilter.getIds()));
            } else if (FIELD_IS_NOT.equals(op)) {
                predicates.add(path.in(idsFilter.getIds()).not());
            }
        }
    }

    private void addNameFilter(TransactionFilterCriteria criteria, Root<Transaction> root, CriteriaBuilder builder, List<Predicate> predicates) {
        if (criteria.getName() != null && criteria.getName().getValue() != null) {
            String value = safeTrim(criteria.getName().getValue());
            String operator = normalize(criteria.getName().getOperator());
            addTextFilter(root, builder, predicates, FIELD_NAME, value, operator);
        }
    }

    private void addDateFilter(TransactionFilterCriteria criteria, Root<Transaction> root, CriteriaBuilder builder, List<Predicate> predicates) {
        if (criteria.getDate() != null) {
            TransactionFilterCriteria.DateFilter dateRange = criteria.getDate();
            String operator = normalize(dateRange.getOperator());
            if (FIELD_IS.equals(operator) && dateRange.getStartDate() != null) {
                predicates.add(builder.equal(root.get(FIELD_DATE), dateRange.getStartDate()));
            } else if ("is between".equals(operator) && dateRange.getStartDate() != null && dateRange.getEndDate() != null) {
                predicates.add(builder.between(root.get(FIELD_DATE), dateRange.getStartDate(), dateRange.getEndDate()));
            }
        }
    }

    private void addAmountFilter(TransactionFilterCriteria criteria, Root<Transaction> root, CriteriaBuilder builder, List<Predicate> predicates) {
        if (criteria.getAmount() != null && criteria.getAmount().getValue() != null) {
            String operator = normalize(criteria.getAmount().getOperator());
            BigDecimal value = criteria.getAmount().getValue();
            Expression<BigDecimal> amountPath = root.get(FIELD_AMOUNT).as(BigDecimal.class);
            switch (operator) {
                case "=":
                    predicates.add(builder.equal(amountPath, value));
                    break;
                case "!=":
                    predicates.add(builder.notEqual(amountPath, value));
                    break;
                case ">":
                    predicates.add(builder.greaterThan(amountPath, value));
                    break;
                case "<":
                    predicates.add(builder.lessThan(amountPath, value));
                    break;
                case ">=":
                    predicates.add(builder.greaterThanOrEqualTo(amountPath, value));
                    break;
                case "<=":
                    predicates.add(builder.lessThanOrEqualTo(amountPath, value));
                    break;
                default:
                    // ignore invalid operator to avoid leaking details; DTO validation should prevent this
                    break;
            }
        }
    }

    private void addCurrenciesFilter(TransactionFilterCriteria criteria, Root<Transaction> root, List<Predicate> predicates) {
        TransactionFilterCriteria.CurrenciesFilter currencies = criteria.getCurrencies();
        if (currencies != null && !CollectionUtils.isEmpty(currencies.getCurrencies())) {
            String op = normalize(currencies.getOperator());
            Expression<?> path = root.get(FIELD_SOURCE_ACCOUNT).get("money").get(FIELD_CURRENCY);
            if (FIELD_IS.equals(op)) {
                predicates.add(path.in(currencies.getCurrencies()));
            } else if (FIELD_IS_NOT.equals(op)) {
                predicates.add(path.in(currencies.getCurrencies()).not());
            }
        }
    }

    private void addTypesFilter(TransactionFilterCriteria criteria, Root<Transaction> root, List<Predicate> predicates) {
        TransactionFilterCriteria.TypesFilter types = criteria.getTypes();
        if (types != null && !CollectionUtils.isEmpty(types.getTypes())) {
            String op = normalize(types.getOperator());
            if (FIELD_IS.equals(op)) {
                predicates.add(root.get(FIELD_TYPE).in(types.getTypes()));
            } else if (FIELD_IS_NOT.equals(op)) {
                predicates.add(root.get(FIELD_TYPE).in(types.getTypes()).not());
            }
        }
    }

    private void addRemarksFilter(TransactionFilterCriteria criteria, Root<Transaction> root, CriteriaBuilder builder, List<Predicate> predicates) {
        if (criteria.getRemarks() != null && criteria.getRemarks().getValue() != null) {
            String value = safeTrim(criteria.getRemarks().getValue());
            String operator = normalize(criteria.getRemarks().getOperator());
            addTextFilter(root, builder, predicates, FIELD_REMARKS, value, operator);
        }
    }

    private void addTextFilter(Root<Transaction> root,
                               CriteriaBuilder builder,
                               List<Predicate> predicates,
                               String field,
                               String value,
                               String operator) {
        if (value == null) return;
        Expression<String> path = builder.lower(root.get(field));
        String lowered = value.toLowerCase(Locale.ROOT);
        String pattern;
        switch (operator) {
            case FIELD_IS:
                predicates.add(builder.equal(path, lowered));
                break;
            case FIELD_IS_NOT:
                predicates.add(builder.notEqual(path, lowered));
                break;
            case "contains":
                pattern = "%" + lowered + "%";
                predicates.add(builder.like(path, pattern));
                break;
            case "does not contain":
                pattern = "%" + lowered + "%";
                predicates.add(builder.notLike(path, pattern));
                break;
            case "starts with":
                pattern = lowered + "%";
                predicates.add(builder.like(path, pattern));
                break;
            case "ends with":
                pattern = "%" + lowered;
                predicates.add(builder.like(path, pattern));
                break;
            default:
                // ignore invalid operator
                break;
        }
    }

    private void applySorting(Root<Transaction> root, jakarta.persistence.criteria.CriteriaQuery<?> query, CriteriaBuilder builder, String sort) {
        String field = FIELD_DATE;
        boolean desc = true; // default desc

        if (sort != null && !sort.isBlank()) {
            String s = sort.trim();
            String dir = null;
            if (s.contains(",")) {
                String[] parts = s.split(",", 2);
                field = parts[0].trim();
                dir = parts[1].trim();
            } else if (s.contains(":")) {
                String[] parts = s.split(":", 2);
                field = parts[0].trim();
                dir = parts[1].trim();
            } else if (s.contains(" ")) {
                String[] parts = s.split(" ", 2);
                field = parts[0].trim();
                dir = parts[1].trim();
            } else {
                field = s;
            }
            field = field.toLowerCase(Locale.ROOT);
            if (!VALID_SORT_FIELDS.contains(field)) {
                field = FIELD_DATE;
            }
            if (dir != null) {
                desc = !dir.equalsIgnoreCase("asc");
            }
        }

        if (desc) {
            query.orderBy(builder.desc(root.get(field)));
        } else {
            query.orderBy(builder.asc(root.get(field)));
        }
    }

    private String normalize(String operator) {
        return operator.trim().toLowerCase(Locale.ROOT);
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
