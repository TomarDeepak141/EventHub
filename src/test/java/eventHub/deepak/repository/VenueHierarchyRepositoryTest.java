package eventHub.deepak.repository;

import eventHub.deepak.entity.*;
import eventHub.deepak.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class VenueHierarchyRepositoryTest {
    @Autowired VenueRepository venues;
    @Autowired ScreenRepository screens;
    @Autowired SeatCategoryRepository categories;
    @Autowired SeatRepository seats;
    @Autowired PlatformTransactionManager transactionManager;

    @Test void enforcesUniqueScreenNamesPerVenueAndSeatIdentifiersPerScreen() {
        TransactionTemplate tx=new TransactionTemplate(transactionManager);
        long[] ids=tx.execute(s->{Venue v=venues.saveAndFlush(Venue.builder().name("Hall").city("Delhi").build());Screen sc=screens.saveAndFlush(Screen.builder().name("Screen 1").venue(v).build());SeatCategory c=categories.saveAndFlush(SeatCategory.builder().name("Standard").price(new java.math.BigDecimal("250.00")).build());return new long[]{v.getId(),sc.getId(),c.getId()};});
        assertThrows(DataIntegrityViolationException.class,()->tx.execute(s->{Venue v=venues.getReferenceById(ids[0]);screens.saveAndFlush(Screen.builder().name("Screen 1").venue(v).build());return null;}));
        tx.executeWithoutResult(s->{Screen sc=screens.getReferenceById(ids[1]);SeatCategory c=categories.getReferenceById(ids[2]);seats.saveAndFlush(Seat.builder().identifier("A1").screen(sc).category(c).build());});
        assertThrows(DataIntegrityViolationException.class,()->tx.execute(s->{Screen sc=screens.getReferenceById(ids[1]);SeatCategory c=categories.getReferenceById(ids[2]);seats.saveAndFlush(Seat.builder().identifier("A1").screen(sc).category(c).build());return null;}));
    }
}
