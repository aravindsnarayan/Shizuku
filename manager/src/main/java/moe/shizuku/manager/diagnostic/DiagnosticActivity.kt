package moe.shizuku.manager.diagnostic

import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.TextView
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.databinding.DiagnosticActivityBinding
import moe.shizuku.manager.utils.EnvironmentUtils
import moe.shizuku.manager.utils.ShizukuStateMachine
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuApiConstants

class DiagnosticActivity : AppBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DiagnosticActivityBinding.inflate(layoutInflater, rootView, true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        runDiagnostics(binding)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun runDiagnostics(binding: DiagnosticActivityBinding) {
        // Service checks
        checkShizukuStatus(binding.checkShizukuStatus)
        checkShizukuVersion(binding.checkShizukuVersion)

        // ADB checks
        checkUsbDebugging(binding.checkUsbDebugging)
        checkWirelessDebugging(binding.checkWirelessDebugging)
        checkAdbTcpPort(binding.checkAdbTcpPort)

        // System checks
        checkAndroidVersion(binding.checkAndroidVersion)
        checkRootStatus(binding.checkRootStatus)
        checkBatteryOptimization(binding.checkBatteryOptimization)
        checkDeveloperOptions(binding.checkDeveloperOptions)
    }

    private fun checkShizukuStatus(textView: TextView) {
        val isRunning = ShizukuStateMachine.isRunning()
        setCheckResult(
            textView,
            if (isRunning) getString(R.string.diagnostic_shizuku_running)
            else getString(R.string.diagnostic_shizuku_not_running),
            isRunning
        )
    }

    private fun checkShizukuVersion(textView: TextView) {
        val isRunning = ShizukuStateMachine.isRunning()
        if (isRunning) {
            try {
                val version = "${Shizuku.getVersion()}.${ShizukuApiConstants.SERVER_PATCH_VERSION}"
                setCheckResult(
                    textView,
                    getString(R.string.diagnostic_shizuku_version, version),
                    true
                )
            } catch (e: Exception) {
                setCheckResult(
                    textView,
                    getString(R.string.diagnostic_shizuku_version_unknown),
                    null
                )
            }
        } else {
            setCheckResult(
                textView,
                getString(R.string.diagnostic_shizuku_version_unknown),
                null
            )
        }
    }

    private fun checkUsbDebugging(textView: TextView) {
        val adbEnabled = try {
            Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (e: Exception) {
            false
        }
        setCheckResult(
            textView,
            if (adbEnabled) getString(R.string.diagnostic_usb_debugging_on)
            else getString(R.string.diagnostic_usb_debugging_off),
            adbEnabled
        )
    }

    private fun checkWirelessDebugging(textView: TextView) {
        val supported = EnvironmentUtils.isTlsSupported()
        setCheckResult(
            textView,
            if (supported) getString(R.string.diagnostic_wireless_debugging_on)
            else getString(R.string.diagnostic_wireless_debugging_off),
            supported
        )
    }

    private fun checkAdbTcpPort(textView: TextView) {
        val port = EnvironmentUtils.getAdbTcpPort()
        val active = port > 0
        setCheckResult(
            textView,
            if (active) getString(R.string.diagnostic_adb_tcp_active, port)
            else getString(R.string.diagnostic_adb_tcp_inactive),
            if (active) true else null
        )
    }

    private fun checkAndroidVersion(textView: TextView) {
        setCheckResult(
            textView,
            getString(R.string.diagnostic_android_version, Build.VERSION.RELEASE, Build.VERSION.SDK_INT),
            true
        )
    }

    private fun checkRootStatus(textView: TextView) {
        val rooted = EnvironmentUtils.isRooted()
        setCheckResult(
            textView,
            if (rooted) getString(R.string.diagnostic_root_available)
            else getString(R.string.diagnostic_root_unavailable),
            if (rooted) true else null
        )
    }

    private fun checkBatteryOptimization(textView: TextView) {
        val pm = getSystemService(PowerManager::class.java)
        val isIgnoring = pm.isIgnoringBatteryOptimizations(packageName)
        setCheckResult(
            textView,
            if (isIgnoring) getString(R.string.diagnostic_battery_not_optimized)
            else getString(R.string.diagnostic_battery_optimized),
            isIgnoring
        )
    }

    private fun checkDeveloperOptions(textView: TextView) {
        val devEnabled = try {
            Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        } catch (e: Exception) {
            false
        }
        setCheckResult(
            textView,
            if (devEnabled) getString(R.string.diagnostic_developer_options_on)
            else getString(R.string.diagnostic_developer_options_off),
            devEnabled
        )
    }

    /**
     * Sets the text and drawable indicator for a check result.
     * @param pass true = pass (green check), false = fail (red X), null = info (neutral)
     */
    private fun setCheckResult(textView: TextView, text: String, pass: Boolean?) {
        textView.text = text
        val iconRes = when (pass) {
            true -> R.drawable.ic_server_ok_24dp
            false -> R.drawable.ic_server_error_24dp
            null -> R.drawable.ic_outline_info_24
        }
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(iconRes, 0, 0, 0)
    }
}
