package com.flo.readinglog.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flo.readinglog.domain.model.CharacterStats
import com.flo.readinglog.domain.model.ShopItem
import com.flo.readinglog.domain.model.ShopPurchase
import com.flo.readinglog.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopUiState(
    val stats: CharacterStats = CharacterStats.Empty,
    val purchases: List<ShopPurchase> = emptyList(),
    val message: String? = null,
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ShopUiState> =
        combine(
            characterRepository.observeStats(),
            characterRepository.observePurchases(),
            _message,
        ) { stats, purchases, message ->
            ShopUiState(stats = stats, purchases = purchases, message = message)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ShopUiState(),
        )

    fun purchase(item: ShopItem) {
        viewModelScope.launch {
            val success = characterRepository.requestReward(item)
            _message.value = if (success) "🎉 ${item.name} redeemed!" else "⚔️ Not enough gold!"
        }
    }

    fun onMessageShown() {
        _message.value = null
    }
}
