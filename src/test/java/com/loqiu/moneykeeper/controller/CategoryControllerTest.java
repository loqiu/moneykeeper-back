package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.entity.Ledger;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.CategoryService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.RecordSearchService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private RecordSearchService recordSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController();
        ReflectionTestUtils.setField(controller, "categoryService", categoryService);
        ReflectionTestUtils.setField(controller, "ledgerService", ledgerService);
        ReflectionTestUtils.setField(controller, "recordSearchService", recordSearchService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createCategoryShouldAssignDefaultLedger() throws Exception {
        when(ledgerService.getOrCreatePersonalLedger(1L)).thenReturn(Ledger.builder().id(21L).build());
        when(categoryService.insertCategory(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(5L);
            return true;
        });

        mockMvc.perform(post("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "name": "Food",
                                  "icon": "utensils",
                                  "color": "#FF6B6B",
                                  "type": "expense"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.ledgerId").value(21));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryService).insertCategory(captor.capture());
        assertEquals(21L, captor.getValue().getLedgerId());
    }

    @Test
    void createCategoryShouldNormalizeChineseType() throws Exception {
        when(ledgerService.getOrCreatePersonalLedger(1L)).thenReturn(Ledger.builder().id(21L).build());
        when(categoryService.insertCategory(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(6L);
            return true;
        });

        mockMvc.perform(post("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "name": "Food",
                                  "icon": "utensils",
                                  "color": "#FF6B6B",
                                  "type": "支出"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("expense"));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryService).insertCategory(captor.capture());
        assertEquals("expense", captor.getValue().getType());
    }

    @Test
    void updateCategoryShouldRefreshOnlyThatCategoryRecords() throws Exception {
        Category existingCategory = new Category();
        existingCategory.setId(5L);
        existingCategory.setUserId(1L);
        existingCategory.setLedgerId(21L);
        existingCategory.setName("Food");
        existingCategory.setIcon("utensils");
        existingCategory.setColor("#FF6B6B");
        existingCategory.setType("expense");

        Category updatedCategory = new Category();
        updatedCategory.setId(5L);
        updatedCategory.setUserId(1L);
        updatedCategory.setLedgerId(21L);
        updatedCategory.setName("Dining");
        updatedCategory.setIcon("utensils");
        updatedCategory.setColor("#FF6B6B");
        updatedCategory.setType("expense");

        when(categoryService.getById(5L)).thenReturn(existingCategory, updatedCategory);
        when(categoryService.updateById(any(Category.class))).thenReturn(true);

        mockMvc.perform(put("/api/categories/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "name": "Dining"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dining"))
                .andExpect(jsonPath("$.icon").value("utensils"))
                .andExpect(jsonPath("$.color").value("#FF6B6B"))
                .andExpect(jsonPath("$.type").value("expense"));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryService).updateById(captor.capture());
        assertEquals("Dining", captor.getValue().getName());
        assertEquals("utensils", captor.getValue().getIcon());
        assertEquals("#FF6B6B", captor.getValue().getColor());
        assertEquals("expense", captor.getValue().getType());
        assertEquals(21L, captor.getValue().getLedgerId());
        verify(recordSearchService).refreshCategoryRecordsIfEnabled(5L);
    }

    @Test
    void deleteCategoryShouldRefreshOnlyThatCategoryRecords() throws Exception {
        Category existingCategory = new Category();
        existingCategory.setId(5L);
        existingCategory.setUserId(1L);
        existingCategory.setLedgerId(21L);
        existingCategory.setName("Food");
        existingCategory.setIcon("utensils");
        existingCategory.setColor("#FF6B6B");
        existingCategory.setType("expense");
        existingCategory.setDeletedAt(0);

        when(categoryService.getById(5L)).thenReturn(existingCategory);
        when(categoryService.update(any())).thenReturn(true);

        mockMvc.perform(delete("/api/categories/5")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk());

        verify(recordSearchService).refreshCategoryRecordsIfEnabled(5L);
    }
}
