package com.example.moneymanager.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val moneyDao: MoneyDao
) : ViewModel() {

    val allCategories: StateFlow<List<Category>> = moneyDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesMap: StateFlow<Map<Long, Category>> = allCategories.map { list ->
        list.associateBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val recentTransactions: StateFlow<List<Transaction>> = moneyDao.getAllTransactions()
        .map { list -> list.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}