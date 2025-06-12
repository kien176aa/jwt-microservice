package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Slf4j
public class SearchRequest<T, E> {
    private T condition;
    private Integer pageSize;
    private Integer pageIndex;
    private Boolean orderBy;
    private String sortBy;

    @JsonIgnore
    private Pageable pageable;

    @JsonCreator
    public SearchRequest(
            @JsonProperty("condition") T condition,
            @JsonProperty("pageSize") Integer pageSize,
            @JsonProperty("pageIndex") Integer pageIndex,
            @JsonProperty("orderBy") Boolean orderBy,
            @JsonProperty("sortBy") String sortBy) {
        log.info("search req start: {}", this);
        this.condition = condition;
        this.pageSize = (pageSize != null && pageSize > 0) ? pageSize : 10;
        this.pageIndex = (pageIndex != null && pageIndex > 0) ? pageIndex : 1;
        this.orderBy = (orderBy == null) ? Boolean.FALSE : orderBy;
        this.sortBy = sortBy;

        log.info("search req end: {}", this);
    }

    public Pageable getPageable(Class<E> entityClass) {
        validateSortBy(entityClass);
        if (sortBy != null && !sortBy.isEmpty()) {
            this.pageable = PageRequest.of(
                    this.pageIndex - 1,
                    this.pageSize,
                    this.orderBy ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        } else {
            this.pageable = PageRequest.of(this.pageIndex - 1, this.pageSize);
        }
        return pageable;
    }

    public void validateSortBy(Class<E> entityClass) {
        log.info("validate sort by: {}", entityClass);
        Set<String> validFields = new HashSet<>();
        for (Field field : entityClass.getDeclaredFields()) {
            validFields.add(field.getName());
        }

        if (sortBy == null || !validFields.contains(sortBy)) {
            sortBy = entityClass.getDeclaredFields()[0].getName();
        }
    }
}
