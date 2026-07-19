package com.ecommerce.service;

import com.ecommerce.dto.CategoryRequest;
import com.ecommerce.dto.CategoryResponse;
import com.ecommerce.dto.PagedResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        categoryRequest = CategoryRequest.builder()
                .name("Electronics")
                .description("Electronic items")
                .build();

        category = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic items")
                .isActive(true)
                .build();
    }

    @Test
    void createCategory_Success() {
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(categoryRequest);

        assertNotNull(response);
        assertEquals("Electronics", response.getName());
        assertEquals("Electronic items", response.getDescription());
        assertTrue(response.isActive());
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.createCategory(categoryRequest));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_Success() {
        CategoryRequest updateRequest = CategoryRequest.builder()
                .name("Updated Electronics")
                .description("Updated description")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Updated Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse response = categoryService.updateCategory(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Electronics", response.getName());
    }

    @Test
    void updateCategory_NotFound_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(999L, categoryRequest));
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_NotFound_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(999L));
    }

    @Test
    void getCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getCategoryById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Electronics", response.getName());
    }

    @Test
    void getCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(999L));
    }

    @Test
    void getAllCategories_Success() {
        Page<Category> page = new PageImpl<>(List.of(category));
        when(categoryRepository.findByIsActiveTrue(any(Pageable.class))).thenReturn(page);

        PagedResponse<CategoryResponse> response =
                categoryService.getAllCategories(0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Electronics", response.getContent().get(0).getName());
    }

    @Test
    void getAllCategoriesForAdmin_Success() {
        Page<Category> page = new PageImpl<>(List.of(category));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<CategoryResponse> response =
                categoryService.getAllCategoriesForAdmin(0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

}
