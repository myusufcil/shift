package com.cil.shift.update

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages in-app updates using Google Play Core library.
 * Checks for available updates and prompts users to update when necessary.
 */
class InAppUpdateManager(context: Context) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    // Days since update was available before showing immediate update
    private val DAYS_FOR_FLEXIBLE_UPDATE = 3
    private val DAYS_FOR_IMMEDIATE_UPDATE = 7

    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                _updateState.value = UpdateState.Downloaded
            }
            InstallStatus.FAILED -> {
                _updateState.value = UpdateState.Error("Update failed")
            }
            InstallStatus.INSTALLING -> {
                _updateState.value = UpdateState.Installing
            }
            else -> {}
        }
    }

    init {
        appUpdateManager.registerListener(installStateListener)
    }

    /**
     * Check if an update is available.
     * Should be called when the app starts or resumes.
     */
    fun checkForUpdate(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            handleUpdateInfo(activity, updateInfo)
        }.addOnFailureListener { exception ->
            android.util.Log.e("InAppUpdateManager", "Failed to check for update", exception)
            _updateState.value = UpdateState.Idle
        }
    }

    private fun handleUpdateInfo(activity: Activity, updateInfo: AppUpdateInfo) {
        when (updateInfo.updateAvailability()) {
            UpdateAvailability.UPDATE_AVAILABLE -> {
                val staleDays = updateInfo.clientVersionStalenessDays() ?: 0

                when {
                    // Critical update - force immediate
                    updateInfo.updatePriority() >= 4 -> {
                        startImmediateUpdate(activity, updateInfo)
                    }
                    // Update has been available for a week - force immediate
                    staleDays >= DAYS_FOR_IMMEDIATE_UPDATE &&
                        updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                        startImmediateUpdate(activity, updateInfo)
                    }
                    // Update available for a few days - flexible
                    staleDays >= DAYS_FOR_FLEXIBLE_UPDATE &&
                        updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                        startFlexibleUpdate(activity, updateInfo)
                    }
                    // New update - flexible if available
                    updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                        _updateState.value = UpdateState.Available(
                            versionCode = updateInfo.availableVersionCode(),
                            isImmediate = false
                        )
                    }
                    else -> {
                        _updateState.value = UpdateState.Idle
                    }
                }
            }
            UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                _updateState.value = UpdateState.Idle
            }
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                // Resume the update if it was in progress
                if (updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    startImmediateUpdate(activity, updateInfo)
                }
            }
            else -> {
                _updateState.value = UpdateState.Idle
            }
        }
    }

    private fun startImmediateUpdate(activity: Activity, updateInfo: AppUpdateInfo) {
        _updateState.value = UpdateState.Updating(isImmediate = true)
        appUpdateManager.startUpdateFlowForResult(
            updateInfo,
            activity,
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
            REQUEST_CODE_UPDATE
        )
    }

    private fun startFlexibleUpdate(activity: Activity, updateInfo: AppUpdateInfo) {
        _updateState.value = UpdateState.Updating(isImmediate = false)
        appUpdateManager.startUpdateFlowForResult(
            updateInfo,
            activity,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
            REQUEST_CODE_UPDATE
        )
    }

    /**
     * Start update flow manually (when user clicks update button).
     */
    fun startUpdate(activity: Activity, immediate: Boolean = false) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val updateType = if (immediate && updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    AppUpdateType.IMMEDIATE
                } else if (updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    AppUpdateType.FLEXIBLE
                } else {
                    return@addOnSuccessListener
                }

                _updateState.value = UpdateState.Updating(isImmediate = updateType == AppUpdateType.IMMEDIATE)
                appUpdateManager.startUpdateFlowForResult(
                    updateInfo,
                    activity,
                    AppUpdateOptions.newBuilder(updateType).build(),
                    REQUEST_CODE_UPDATE
                )
            }
        }
    }

    /**
     * Complete the update installation (for flexible updates).
     * Should be called after update is downloaded.
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    /**
     * Check for pending updates when app resumes.
     * This handles the case where an immediate update was interrupted.
     */
    fun checkPendingUpdate(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            // If an immediate update is in progress, resume it
            if (updateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                if (updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    startImmediateUpdate(activity, updateInfo)
                }
            }

            // If a flexible update is downloaded, prompt to install
            if (updateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _updateState.value = UpdateState.Downloaded
            }
        }
    }

    /**
     * Dismiss the update notification.
     */
    fun dismissUpdate() {
        _updateState.value = UpdateState.Idle
    }

    fun cleanup() {
        appUpdateManager.unregisterListener(installStateListener)
    }

    companion object {
        const val REQUEST_CODE_UPDATE = 1234
    }
}

/**
 * Represents the current state of in-app update.
 */
sealed class UpdateState {
    data object Idle : UpdateState()
    data class Available(val versionCode: Int, val isImmediate: Boolean) : UpdateState()
    data class Updating(val isImmediate: Boolean) : UpdateState()
    data object Downloaded : UpdateState()
    data object Installing : UpdateState()
    data class Error(val message: String) : UpdateState()
}
