package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.CategoryService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.RecordSearchService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LedgerCategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private RecordSearchService recordSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LedgerCategoryController controller = new LedgerCategoryController();
        ReflectionTestUtils.setField(controller, "categoryService", categoryService);
        ReflectionTestUtils.setField(controller, "ledgerService", ledgerService);
        ReflectionTestUtils.setField(controller, "recordSearchService", recordSearchService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listCategoriesShouldAllowLedgerMembers() throws Exception {
        Category category = new Category();
        category.setId(5L);
        category.setLedgerId(31L);
        category.setUserId(1L);
        category.setName("Food");
        category.setType("expense");

        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(categoryService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<Category>>any())).thenReturn(List.of(category));

        mockMvc.perform(get("/api/ledgers/31/categories")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].ledgerId").value(31));
    }

    @Test
    void createCategoryShouldRequireLedgerManagerPermission() throws Exception {
        when(ledgerService.hasManagementPermission(31L, 2L)).thenReturn(false);

        mockMvc.perform(post("/api/ledgers/31/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "name": "Food",
                                  "icon": "utensils",
                                  "color": "#FF6B6B",
                                  "type": "expense"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to manage this ledger's categories"));
    }

    @Test
    void createCategoryShouldPersistLedgerScopedCategory() throws Exception {
        when(ledgerService.hasManagementPermission(31L, 1L)).thenReturn(true);
        when(categoryService.insertCategory(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(6L);
            return true;
        });

        mockMvc.perform(post("/api/ledgers/31/categories")
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
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.ledgerId").value(31))
                .andExpect(jsonPath("$.userId").value(1));

        verify(categoryService).insertCategory(any(Category.class));
    }
}