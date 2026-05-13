package com.flo.readinglog.ui.screens.digests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flo.readinglog.domain.model.Digest
import com.flo.readinglog.domain.repository.DigestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DigestsViewModel @Inject constructor(
    digestRepository: DigestRepository,
) : ViewModel() {

    val digests: StateFlow<List<Digest>> = digestRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
