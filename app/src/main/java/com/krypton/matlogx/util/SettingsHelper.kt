/*
 * Copyright (C) 2021 AOSP-Krypton Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.krypton.matlogx.util

import android.content.Context
import android.content.SharedPreferences

import androidx.core.content.edit

import dagger.hilt.android.qualifiers.ApplicationContext

import javax.inject.Inject

/**
 * Class that manages user preferences and
 * exposes helper APIs for clients to use.
 */
class SettingsHelper @Inject constructor(
    @ApplicationContext context: Context
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val sharedPreferences by lazy {
        context.getSharedPreferences(
            PREF_FILE_NAME,
            Context.MODE_PRIVATE
        )
    }

    private var registered = false
    private val listeners = mutableListOf<() -> Unit>()

    fun getLogcatBuffers(): Set<String> =
        sharedPreferences.getStringSet(PREF_KEY_LOGCAT_BUFFER, PREF_KEY_LOGCAT_BUFFER_DEFAULT)!!

    fun setLogcatBuffers(buffers: Set<String>) {
        sharedPreferences.edit(commit = true) {
            putStringSet(PREF_KEY_LOGCAT_BUFFER, buffers)
        }
    }

    /**
     * Get an [Int] type value of log size limit stored in [SharedPreferences]
     *
     * @return size limit stored in [SharedPreferences]
     */
    fun getLogSizeLimit() = sharedPreferences.getInt(PREF_KEY_LOG_SIZE_LIMIT, LOG_SIZE_DEFAULT)

    /**
     * Saves the limit for number of log lines to keep inside [SharedPreferences].
     *
     * @param limit the value to save.
     */
    fun setLogSizeLimit(limit: Int) {
        sharedPreferences.edit(commit = true) {
            putInt(PREF_KEY_LOG_SIZE_LIMIT, limit)
        }
    }

    /**
     * Get the log level saved in [SharedPreferences].
     *
     * @return the saved log level or default [LOG_LEVEL_DEFAULT].
     */
    fun getLogLevel(): String =
        sharedPreferences.getString(PREF_KEY_LOG_LEVEL, LOG_LEVEL_DEFAULT)!!

    /**
     * Saves the log level to [SharedPreferences]
     *
     * @param value the log level to save
     */
    fun setLogLevel(value: String) {
        sharedPreferences.edit(commit = true) {
            putString(PREF_KEY_LOG_LEVEL, value)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREF_KEY_LOGCAT_BUFFER ||
            key == PREF_KEY_LOG_SIZE_LIMIT ||
            key == PREF_KEY_LOG_LEVEL
        ) {
            listeners.forEach { it() }
        }
    }

    /**
     * Registers a listener to get notified when [PREF_KEY_LOGCAT_BUFFER],
     * [PREF_KEY_LOG_SIZE_LIMIT], [PREF_KEY_LOG_LEVEL] change.
     *
     * @param listener the callback that will be invoked when settings change.
     */
    fun registerConfigurationChangeListener(listener: () -> Unit) {
        if (!registered) sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        listeners.add(listener)
    }

    /**
     * Whether to include device information in logs.
     *
     * @return the saved value.
     */
    fun getIncludeDeviceInfo(): Boolean =
        sharedPreferences.getBoolean(PREF_KEY_INCLUDE_DEVICE_INFO, INCLUDE_DEVICE_INFO_DEFAULT)

    /**
     * Save include device information preference.
     *
     * @param include the value to save.
     */
    fun setIncludeDeviceInfo(include: Boolean) {
        sharedPreferences.edit(commit = true) {
            putBoolean(PREF_KEY_INCLUDE_DEVICE_INFO, include)
        }
    }

    companion object {
        private const val PREF_FILE_NAME = "matlogx_shared_preferences"

        private const val PREF_KEY_LOGCAT_ARG_PREFIX = "key_logcat_arg_"
        private const val PREF_KEY_LOGCAT_BUFFER = "${PREF_KEY_LOGCAT_ARG_PREFIX}buffer"
        private val PREF_KEY_LOGCAT_BUFFER_DEFAULT = setOf("main", "system", "crash")

        private const val PREF_KEY_LOG_SIZE_LIMIT = "key_log_size_limit"
        const val LOG_SIZE_DEFAULT = 10000

        private const val PREF_KEY_LOG_LEVEL = "key_log_level"
        private const val LOG_LEVEL_DEFAULT = "V"

        private const val PREF_KEY_INCLUDE_DEVICE_INFO = "key_include_device_info"
        private const val INCLUDE_DEVICE_INFO_DEFAULT = false
    }
}