package com.gramaurja.presentation.screens.home

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.gramaurja.data.local.PreferencesManager
import com.gramaurja.data.model.PowerStatus
import com.gramaurja.data.model.StatusUpdate
import com.gramaurja.domain.usecase.GetPowerHistoryUseCase
import com.gramaurja.domain.usecase.GetZoneStatusUseCase
import com.gramaurja.domain.usecase.PredictPowerReturnUseCase
import com.gramaurja.domain.usecase.UpdatePowerStatusUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: HomeViewModel
    private val getZoneStatus: GetZoneStatusUseCase = mockk()
    private val updatePowerStatus: UpdatePowerStatusUseCase = mockk()
    private val getPowerHistory: GetPowerHistoryUseCase = mockk()
    private val predictPowerReturn: PredictPowerReturnUseCase = mockk()
    private val prefs: PreferencesManager = mockk(relaxed = true)
    private val auth: FirebaseAuth = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default flows
        every { prefs.selectedZoneId } returns flowOf("zone1")
        every { prefs.selectedZoneName } returns flowOf("Zone 1")
        every { prefs.cachedStatus } returns flowOf(PowerStatus.UNKNOWN)
        every { prefs.cachedTimestamp } returns flowOf(0L)
        
        val user: FirebaseUser = mockk()
        every { user.uid } returns "user123"
        every { auth.currentUser } returns user

        every { getZoneStatus(any()) } returns flowOf(
            StatusUpdate("zone1", "Zone 1", PowerStatus.ON, 1000L, "user123", 5)
        )
        every { getPowerHistory(any()) } returns flowOf(emptyList())

        viewModel = HomeViewModel(
            getZoneStatus,
            updatePowerStatus,
            getPowerHistory,
            predictPowerReturn,
            prefs,
            auth
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should load zone and observe status`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("zone1", state.zoneId)
        assertEquals(PowerStatus.ON, state.currentStatus)
        assertEquals(5, state.confirmCount)
    }
}
