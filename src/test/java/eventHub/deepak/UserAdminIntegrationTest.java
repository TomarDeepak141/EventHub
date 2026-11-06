package eventHub.deepak;

import eventHub.deepak.entity.SeatCategory;
import eventHub.deepak.entity.User;
import eventHub.deepak.enums.Role;
import eventHub.deepak.repository.SeatCategoryRepository;
import eventHub.deepak.repository.UserRepository;
import eventHub.deepak.service.interfaces.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserAdminIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired SeatCategoryRepository categories;
    @Autowired JwtService jwt;

    @Test
    void adminMayPromoteAndDemoteOnlyCustomerAndOrganizerAccounts() throws Exception {
        User admin = user("role-admin@example.test", Role.ADMIN);
        User customer = user("role-customer@example.test", Role.CUSTOMER);
        String token = token(admin);

        mvc.perform(put("/user/admin/users/{id}", customer.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ORGANIZER\",\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ORGANIZER"));

        mvc.perform(put("/user/admin/users/{id}", customer.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"CUSTOMER\",\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void adminCannotGrantAdminChangeAdminRoleOrDemoteSelf() throws Exception {
        User admin = user("role-protected-admin@example.test", Role.ADMIN);
        User customer = user("role-protected-customer@example.test", Role.CUSTOMER);
        String token = token(admin);

        mvc.perform(put("/user/admin/users/{id}", customer.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\",\"active\":true}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/user/admin/users/{id}", admin.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ORGANIZER\",\"active\":true}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/user/admin/users/{id}", admin.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\",\"active\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminsMayListAccountsAndOrganizersMayReadExistingSeatCategories() throws Exception {
        User customer = user("role-list-customer@example.test", Role.CUSTOMER);
        User organizer = user("role-list-organizer@example.test", Role.ORGANIZER);
        categories.save(SeatCategory.builder().name("Organizer visible category").price(new BigDecimal("12.00")).build());

        mvc.perform(get("/user/admin/users").header("Authorization", "Bearer " + token(customer)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/seat-categories").header("Authorization", "Bearer " + token(organizer)))
                .andExpect(status().isOk());
        mvc.perform(get("/seat-categories").header("Authorization", "Bearer " + token(customer)))
                .andExpect(status().isForbidden());
    }

    private User user(String email, Role role) {
        return users.saveAndFlush(User.builder().name("Role Test").email(email).password("already-encoded")
                .role(role).isActive(true).build());
    }

    private String token(User user) {
        return jwt.generateToken(user);
    }
}
