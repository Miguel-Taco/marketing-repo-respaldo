package pe.unmsm.crm.marketing.shared.utils;

import lombok.experimental.UtilityClass;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.lang.NonNull;

@UtilityClass
public class PaginationUtils {
    @NonNull
    public Pageable buildPageable(
            Integer page,
            Integer size,
            String sortBy,
            String direction) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = (size == null || size <= 0 || size > 100) ? 20 : size;

        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isBlank()) {
            sort = "desc".equalsIgnoreCase(direction)
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
        }

        return PageRequest.of(safePage, safeSize, sort);
    }

}
