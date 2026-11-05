package eventHub.deepak.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Set;

public final class PageableSupport {
    private static final int MAX_PAGE_SIZE = 100;

    private PageableSupport() { }

    public static Pageable validate(Pageable pageable, Set<String> allowedSorts, Sort defaultSort) {
        if (pageable.getPageNumber() < 0) throw new IllegalArgumentException("page must be zero or greater");
        if (pageable.getPageSize() < 1 || pageable.getPageSize() > MAX_PAGE_SIZE)
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        pageable.getSort().forEach(order -> {
            if (!allowedSorts.contains(order.getProperty()))
                throw new IllegalArgumentException("Unsupported sort field: " + order.getProperty());
        });
        Sort sort = pageable.getSort().isUnsorted() ? defaultSort : pageable.getSort();
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
