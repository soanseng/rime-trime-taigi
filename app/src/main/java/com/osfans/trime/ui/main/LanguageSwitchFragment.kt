/*
 * SPDX-FileCopyrightText: 2026 Phah Tai-bun contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.osfans.trime.ui.main

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import com.osfans.trime.R
import com.osfans.trime.ui.common.PaddingPreferenceFragment

/** 拍台文 UI 語言切換: 繁中 ↔ 台文(漢羅) — 特化頁, 上游無此檔. */
class LanguageSwitchFragment : PaddingPreferenceFragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).apply {
            addPreference(
                Preference(context).apply {
                    isIconSpaceReserved = false
                    setTitle(R.string.taigi_language_switch)
                    setSummary(R.string.taigi_language_summary)
                    setOnPreferenceClickListener {
                        showLanguagePicker()
                        true
                    }
                },
            )
        }
    }

    private fun showLanguagePicker() {
        val items = arrayOf("繁體中文", "台文（漢羅）")
        val values = arrayOf("zh-TW", "nan-TW")
        val current =
            AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { "zh-TW" }
        val checked = values.indexOfFirst { it == current }.coerceAtLeast(0)
        AlertDialog
            .Builder(requireContext())
            .setTitle(R.string.taigi_language_switch)
            .setSingleChoiceItems(items, checked) { dialog, which ->
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(values[which]),
                )
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.disableTopOptionsMenu()
    }
}
