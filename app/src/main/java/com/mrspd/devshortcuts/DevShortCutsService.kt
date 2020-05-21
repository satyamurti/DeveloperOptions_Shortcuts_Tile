package com.mrspd.devshortcuts

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.util.Log.d
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class DevShortCutsService : TileService() {

    private val developSettingObserver by lazy {
        DevelopSettingsObserver(android.os.Handler())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }


    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateAppTile() {
        try {
            qsTile?.let {
                it.state = tileState()
                it.updateTile()
            }
        } catch (e: Settings.SettingNotFoundException) {
            Log.w(TAG, "Not supported", e)
        }
    }


    override fun onClick() {
        d("MrSPD", "onClick has been called")
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).also { intent ->
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivityAndCollapse(intent)
            d("MrSPD", "Started Acrtivity And collapse")

        } catch (e: ActivityNotFoundException) {
            d("MrSPD", "Activity was not Found")
        }

    }

    override fun onTileAdded() {
        super.onTileAdded()
        d("MrSPD", "Tile has been added")

    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        d("MrSPD", "Tile has been removed")

    }

    override fun onStartListening() {
        super.onStartListening()
        d("MrSPD", "Started Listening (onStartListening)")


        contentResolver.let {

            it.notifyChange(Settings.Global.getUriFor(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED), developSettingObserver)
            it.notifyChange(Settings.Global.getUriFor(Settings.Global.ADB_ENABLED), developSettingObserver)
        }

        uplateAppTile()
    }

    private fun uplateAppTile() {
        try {
            qsTile?.let {
                it.state = tileState()
                it.updateTile()
            }
        } catch (e: Settings.SettingNotFoundException) {
            d("MrSPD", "Settings mila nahi bro")


        }
    }

    private fun tileState(): Int {
        val enabled = Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)
        d("MrSPD", "DEVELOMENT SETTINGS ENABLED = $enabled")

        val adb = Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0)
        d("MrSPD", "ADB ENABLED = $adb")

        return when {

            enabled == 1 && adb == 1 -> Tile.STATE_ACTIVE

            else -> Tile.STATE_INACTIVE
        }


    }

    inner class DevelopSettingsObserver(handler: android.os.Handler) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            updateAppTile()
        }
    }


    companion object {

        private const val TAG = "DeveloperOptionsTileService"

    }

}
