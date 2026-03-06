package moe.shizuku.manager.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.R
import moe.shizuku.manager.utils.ShizukuStateMachine
import rikka.shizuku.Shizuku

class ShizukuTileService : TileService() {

    private val stateListener: (ShizukuStateMachine.State) -> Unit = {
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        ShizukuStateMachine.addListener(stateListener)
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        ShizukuStateMachine.removeListener(stateListener)
    }

    override fun onClick() {
        super.onClick()
        val state = ShizukuStateMachine.get()

        when (state) {
            ShizukuStateMachine.State.RUNNING -> {
                // Stop Shizuku
                ShizukuStateMachine.set(ShizukuStateMachine.State.STOPPING)
                runCatching { Shizuku.exit() }
                updateTile()
            }
            ShizukuStateMachine.State.STOPPED, ShizukuStateMachine.State.CRASHED -> {
                // Open the app to start - starting requires UI interaction (ADB pairing, root dialog, etc.)
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val pendingIntent = PendingIntent.getActivity(
                        this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    startActivityAndCollapse(pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(intent)
                }
            }
            else -> {
                // STARTING or STOPPING - do nothing
            }
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val state = ShizukuStateMachine.get()

        tile.label = getString(R.string.qs_tile_label)

        when (state) {
            ShizukuStateMachine.State.RUNNING -> {
                tile.state = Tile.STATE_ACTIVE
                tile.subtitle = getString(R.string.home_status_service_is_running, getString(R.string.app_name))
            }
            ShizukuStateMachine.State.STARTING -> {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.subtitle = getString(R.string.notification_service_starting)
            }
            ShizukuStateMachine.State.STOPPING -> {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.subtitle = getString(R.string.stop)
            }
            ShizukuStateMachine.State.STOPPED -> {
                tile.state = Tile.STATE_INACTIVE
                tile.subtitle = getString(R.string.home_status_service_not_running, getString(R.string.app_name))
            }
            ShizukuStateMachine.State.CRASHED -> {
                tile.state = Tile.STATE_INACTIVE
                tile.subtitle = getString(R.string.home_status_service_not_running, getString(R.string.app_name))
            }
        }

        tile.updateTile()
    }
}
