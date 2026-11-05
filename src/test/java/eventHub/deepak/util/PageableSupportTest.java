package eventHub.deepak.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PageableSupportTest {
    @Test void rejectsOutOfRangeSizeAndUnknownSort() {
        assertThrows(IllegalArgumentException.class, () -> PageableSupport.validate(PageRequest.of(0, 101), Set.of("name"), Sort.by("name")));
        assertThrows(IllegalArgumentException.class, () -> PageableSupport.validate(PageRequest.of(0, 10, Sort.by("password")), Set.of("name"), Sort.by("name")));
    }

    @Test void appliesDeterministicDefaultSortWhenUnsorted() {
        var result = PageableSupport.validate(PageRequest.of(2, 20), Set.of("name"), Sort.by(Sort.Order.asc("name")));
        assertEquals(2, result.getPageNumber());
        assertEquals(20, result.getPageSize());
        assertEquals("name", result.getSort().getOrderFor("name").getProperty());
    }
}
