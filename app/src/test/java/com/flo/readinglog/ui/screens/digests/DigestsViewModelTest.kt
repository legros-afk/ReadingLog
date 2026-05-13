package com.flo.readinglog.ui.screens.digests

import app.cash.turbine.test
import com.flo.readinglog.domain.model.Digest
import com.flo.readinglog.domain.repository.DigestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DigestsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `digests emits empty list when repository is empty`() = runTest {
        val viewModel = DigestsViewModel(FakeDigestRepository(emptyList()))

        viewModel.digests.test {
            assertEquals(emptyList<Digest>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `digests emits repository contents`() = runTest {
        val digest = Digest(
            id = 1L,
            weekStart = LocalDate.of(2025, 1, 6),
            message = "Test digest message",
            sentAt = 1_000_000L,
        )
        val viewModel = DigestsViewModel(FakeDigestRepository(listOf(digest)))

        viewModel.digests.test {
            // stateIn emits its initial emptyList() before the upstream value arrives
            val first = awaitItem()
            val result = if (first.isEmpty()) awaitItem() else first
            assertEquals(listOf(digest), result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `digests reflects repository updates`() = runTest {
        val fakeRepo = FakeDigestRepository(emptyList())
        val viewModel = DigestsViewModel(fakeRepo)
        val newDigest = Digest(
            id = 2L,
            weekStart = LocalDate.of(2025, 1, 13),
            message = "Week 2",
            sentAt = 2_000_000L,
        )

        viewModel.digests.test {
            assertEquals(emptyList<Digest>(), awaitItem()) // initial empty

            fakeRepo.emit(listOf(newDigest))
            assertEquals(listOf(newDigest), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeDigestRepository(initial: List<Digest> = emptyList()) : DigestRepository {

    private val flow = MutableStateFlow(initial)

    fun emit(digests: List<Digest>) {
        flow.value = digests
    }

    override fun observeAll(): Flow<List<Digest>> = flow

    override suspend fun getById(id: Long): Digest? = flow.value.find { it.id == id }

    override suspend fun getByWeekStart(weekStart: LocalDate): Digest? =
        flow.value.find { it.weekStart == weekStart }

    override suspend fun upsert(digest: Digest): Long {
        flow.value = flow.value.filter { it.id != digest.id } + digest
        return digest.id
    }

    override suspend fun getUpdatedSince(since: Long): List<Digest> =
        flow.value.filter { it.sentAt > since }
}
