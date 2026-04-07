package ru.sandr.users.core.validation;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.sandr.users.core.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PageableValidator {

    public static Pageable validateAndMap(Pageable pageable, Map<String, String> allowedToSortFields) {
        List<Sort.Order> safeOrders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String frontedField = order.getProperty();

            if(!allowedToSortFields.containsKey(frontedField)) {
                throw new BadRequestException("BAD_SORT_FIELD", "Сортировка по полю " + frontedField + " запрещена");
            }

            safeOrders.add(new Sort.Order(order.getDirection(), allowedToSortFields.get(frontedField)));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(safeOrders));
    }
}
