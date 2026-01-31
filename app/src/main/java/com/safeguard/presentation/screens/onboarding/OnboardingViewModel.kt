package com.safeguard.presentation.screens.onboarding

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.data.preferences.SettingsDataStore
import com.safeguard.util.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel
@Inject
constructor(
        private val permissionManager: PermissionManager,
        private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        // Initial check to see if we can skip steps
        // For example, if role is already held, jump to permissions
        checkRoleStatus()
    }

    fun nextStep() {
        val current = _uiState.value.currentStep
        val next =
                when (current) {
                    OnboardingStep.WELCOME -> OnboardingStep.DEFAULT_ROLE
                    OnboardingStep.DEFAULT_ROLE -> OnboardingStep.PERMISSIONS
                    OnboardingStep.PERMISSIONS -> OnboardingStep.LOGIN
                    OnboardingStep.LOGIN -> return completeOnboarding()
                }
        _uiState.value = _uiState.value.copy(currentStep = next)
    }

    fun requestRole(launcher: ActivityResultLauncher<Intent>) {
        if (permissionManager.hasCallScreeningRole()) {
            // Already has role, move next
            nextStep()
        } else {
            val intent = permissionManager.createRequestRoleIntent()
            if (intent != null) {
                launcher.launch(intent)
            } else {
                // If intent is null (old Android version), skip
                nextStep()
            }
        }
    }

    fun checkRoleStatus() {
        if (permissionManager.hasCallScreeningRole()) {
            // If we are currently on the role step, move forward automatically
            if (_uiState.value.currentStep == OnboardingStep.DEFAULT_ROLE) {
                nextStep()
            }
        }
    }

    fun requestPermissions(launcher: ActivityResultLauncher<Array<String>>) {
        if (permissionManager.hasRuntimePermissions()) {
            nextStep()
        } else {
            launcher.launch(permissionManager.getRequiredPermissions())
        }
    }

    fun checkPermissionStatus() {
        if (permissionManager.hasRuntimePermissions()) {
            if (_uiState.value.currentStep == OnboardingStep.PERMISSIONS) {
                nextStep()
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsDataStore.setFirstLaunchComplete()
            _uiState.value = _uiState.value.copy(isOnboardingComplete = true)
        }
    }
}

data class OnboardingUiState(
        val currentStep: OnboardingStep = OnboardingStep.WELCOME,
        val isOnboardingComplete: Boolean = false
)

enum class OnboardingStep {
    WELCOME,
    DEFAULT_ROLE,
    PERMISSIONS,
    LOGIN
}
