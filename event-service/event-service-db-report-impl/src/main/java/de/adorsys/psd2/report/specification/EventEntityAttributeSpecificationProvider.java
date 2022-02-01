/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.report.specification;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import java.util.List;

/**
 * This is a class for providing Spring Data Jpa Specification for different entities attributes
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventEntityAttributeSpecificationProvider {
    private EventEntityAttributeSpecificationProvider() {
    }

    /**
     * Provides specification for the attribute in some entity.
     *
     * @param attribute name of the attribute in entity
     * @param value     optional value of the attribute
     * @param <T>       type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if the attribute's value was omitted
     */
    public static <T> Specification<T> provideSpecificationForEntityAttribute(String attribute, String value) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.isBlank(value)) {
                return null;
            }
            return criteriaBuilder.and(criteriaBuilder.equal(root.get(attribute), value));
        };
    }

    public static <T> Specification<T> provideSpecificationForEntityAttributeInList(String attribute, List<String> values) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (CollectionUtils.isEmpty(values)) {
                return null;
            }
            return criteriaBuilder.and(criteriaBuilder.in(root.get(attribute)).value(values));
        };
    }

    /**
     * Provides specification for the attribute in a joined entity.
     *
     * @param join      join to an entity
     * @param attribute name of the attribute in joined entity
     * @param value     optional value of the attribute
     * @param <T>       type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if the attribute's value was omitted
     */
    public static <T> Specification<T> provideSpecificationForJoinedEntityAttribute(@NotNull Join<T, ?> join,
                                                                                    @NotNull String
                                                                                        attribute,
                                                                                    @Nullable String value) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.isBlank(value)) {
                return null;
            }
            return criteriaBuilder.and(criteriaBuilder.equal(join.get(attribute), value));
        };
    }

    /**
     * Provides specification for the attribute in a joined entity.
     *
     * @param join      join to an entity
     * @param attribute name of the attribute in joined entity
     * @param values    optional values of the attribute
     * @param <T>       type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if the attribute's value was omitted
     */
    public static <T> Specification<T> provideSpecificationForJoinedEntityAttributeIn(@NotNull Join<T, ?> join,
                                                                                      @NotNull String attribute,
                                                                                      @Nullable List<String> values) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (CollectionUtils.isEmpty(values)) {
                return null;
            }
            return criteriaBuilder.and(join.get(attribute).in(values));
        };
    }

    /**
     * Provides specification for the attribute in a joined entity.
     *
     * @param join      join to an entity
     * @param attribute name of the attribute in joined entity
     * @param value     optional value of the attribute as Object
     * @param <T>       type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if the attribute's value was omitted
     */
    public static <T> Specification<T> provideSpecificationForJoinedEntityAttribute(@NotNull Join<T, ?> join,
                                                                                    @NotNull String
                                                                                        attribute,
                                                                                    @Nullable Object value) {
        return (root, criteriaQuery, criteriaBuilder) -> value == null
                                                             ? criteriaBuilder.and(criteriaBuilder.isNull(join.get(attribute)))
                                                             : criteriaBuilder.and(criteriaBuilder.equal(join.get(attribute), value));
    }

    /**
     * Provides specification for the attribute in some entity.
     *
     * @param attribute name of the attribute in entity
     * @param value     optional value of the attribute
     * @param <T>       type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if the attribute's value was omitted
     */
    public static <T> Specification<T> provideSpecificationForEntityObjectAttribute(String attribute, @NotNull Object value) {
        return (root, criteriaQuery, criteriaBuilder) ->  criteriaBuilder.and(criteriaBuilder.equal(root.get(attribute), value));
    }
}
