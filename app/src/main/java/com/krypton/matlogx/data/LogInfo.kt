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

package com.krypton.matlogx.data

/**
 * Data class representing a line from logcat.
 *
 * @property pid process id of the program that logged this entry.
 * @property timestamp time (format MM-dd HH:MM:ss.SSS) at which this entry was logged.
 * @property tag the log tag of this entry.
 * @property level the log level of this entry.
 * @property message the message that was logged.
 */
data class LogInfo(
    val pid: Short = -1,
    val timestamp: String = "",
    val tag: String = "",
    val level: Char = ' ',
    val message: String = "",
) {
    /**
     * Check whether this object represents only a message.
     * This is the case when a [LogInfo] object is used to
     * contain event separator logs.
     *
     * @return true if this represents only a message.
     */
    fun hasOnlyMessage() = level.isWhitespace()

    companion object {
        private val timestampRegex =
            Regex("^[0-9]{2}-[0-9]{2}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{6}")
        private val pidRegex = Regex("\\(\\s*[0-9]+\\)")

        /**
         * Convert a line of log to a [LogInfo] object
         *
         * @param logLine the line to convert
         * @return the converted object
         */
        fun fromLine(logLine: String): LogInfo {
            // Filter event separators
            if (logLine.startsWith("-")) {
                return LogInfo(message = logLine)
            }
            // Log format:
            // DD-MM HH:MM:SS.ssssss D/TAG( PID): message
            val metadata = logLine.substringBefore("/")
            val pid = try {
                pidRegex.find(logLine)?.value?.substringAfter("(")?.substringBefore(")")
                    ?.trimStart()?.toShort()
                    ?: -1
            } catch (e: NumberFormatException) {
                -1
            }
            return LogInfo(
                pid = pid,
                timestamp = timestampRegex.find(metadata)?.value ?: "",
                // Assuming that no one insane used ( in their tag
                tag = logLine.substringAfter("/").substringBefore("("),
                level = metadata.last(),
                message = logLine.substringAfter("):").trim(),
            )
        }
    }
}