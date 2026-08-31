package com.expense.tracker.category.service;

import com.expense.tracker.category.dto.CategoryRequest;
import com.expense.tracker.category.entity.Category;
import com.expense.tracker.category.repository.CategoryRepository;
import com.expense.tracker.common.exception.DuplicateResourceException;
import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.user.entity.Role;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * CONCEPT: Unit test with Mockito
 * This test never starts a Spring context or touches a real database - we
 * hand-construct CategoryService with MOCK repositories (@Mock) so we can
 * control exactly what they return and verify exactly how the service
 * calls them. This is the payoff of constructor injection: the class under
 * test can be built with `new CategoryService(...)` in a plain unit test,
 * no Spring container required, so tests like this run in milliseconds.
 *
 * @ExtendWith(MockitoExtension.class) wires up @Mock/@InjectMocks for JUnit 5.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@example.com").fullName("Test User").role(Role.USER).build();
    }

    @Test
    void create_savesCategory_whenNameIsUnique() {
        CategoryRequest request = new CategoryRequest("Groceries", "#6366f1");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(1L, "Groceries")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        var response = categoryService.create("test@example.com", request);

        assertThat(response.name()).isEqualTo("Groceries");
        assertThat(response.id()).isEqualTo(10L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_throwsDuplicateResourceException_whenNameAlreadyExists() {
        CategoryRequest request = new CategoryRequest("Groceries", "#6366f1");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(1L, "Groceries")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create("test@example.com", request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void delete_throwsResourceNotFoundException_whenCategoryBelongsToAnotherUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        // findByIdAndUserId scopes the lookup to THIS user's id - if the category
        // belongs to someone else, the repository (correctly) returns empty,
        // and the service must surface that as "not found", not a raw null.
        when(categoryRepository.findByIdAndUserId(eq(99L), eq(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete("test@example.com", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
