package eventHub.deepak.demo;

import eventHub.deepak.entity.User;
import eventHub.deepak.enums.Role;
import eventHub.deepak.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminBootstrapServiceTest {
    @Autowired AdminBootstrapService bootstrap;
    @Autowired UserRepository users;

    @Test
    void promotesOnlyTheConfiguredExistingAccountAndDoesNotChangeItsCredentials() {
        User target = users.saveAndFlush(User.builder().name("Portfolio Owner")
                .email("bootstrap-target@example.test").password("already-hashed-value")
                .role(Role.CUSTOMER).isActive(true).build());
        User other = users.saveAndFlush(User.builder().name("Other Account")
                .email("bootstrap-other@example.test").password("other-hash")
                .role(Role.CUSTOMER).isActive(true).build());
        long beforeCount = users.count();

        assertTrue(bootstrap.promoteExistingAccount(" BOOTSTRAP-TARGET@EXAMPLE.TEST "));

        assertEquals(Role.ADMIN, users.findById(target.getId()).orElseThrow().getRole());
        assertEquals("already-hashed-value", users.findById(target.getId()).orElseThrow().getPassword());
        assertEquals(Role.CUSTOMER, users.findById(other.getId()).orElseThrow().getRole());
        assertEquals(beforeCount, users.count());
    }

    @Test
    void missingOrBlankAddressDoesNotCreateOrPromoteAnAccount() {
        long beforeCount = users.count();

        assertFalse(bootstrap.promoteExistingAccount("missing@example.test"));
        assertFalse(bootstrap.promoteExistingAccount("  "));

        assertEquals(beforeCount, users.count());
        assertTrue(users.findByEmailIgnoreCase("missing@example.test").isEmpty());
    }
}
