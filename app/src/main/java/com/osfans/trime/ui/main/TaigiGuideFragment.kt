/*
 * SPDX-FileCopyrightText: 2026 Phah Tai-bun contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.osfans.trime.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import com.osfans.trime.R
import com.osfans.trime.ui.common.PaddingPreferenceFragment

/** 拍台文使用說明: 線上指南 + Android 專屬功能說明 (特化頁, 上游無此檔). */
class TaigiGuideFragment : PaddingPreferenceFragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).apply {
            addPreference(
                Preference(context).apply {
                    isIconSpaceReserved = false
                    setTitle(R.string.taigi_guide_online_title)
                    setSummary(R.string.taigi_guide_url)
                    setOnPreferenceClickListener {
                        startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.taigi_guide_url))),
                        )
                        true
                    }
                },
            )
            val sections =
                listOf(
                    R.string.taigi_guide_keyboard_title to R.string.taigi_guide_keyboard_summary,
                    R.string.taigi_guide_panel_title to R.string.taigi_guide_panel_summary,
                    R.string.taigi_guide_commit_title to R.string.taigi_guide_commit_summary,
                )
            for ((titleRes, summaryRes) in sections) {
                addPreference(
                    Preference(context).apply {
                        isIconSpaceReserved = false
                        isSelectable = false
                        setTitle(titleRes)
                        setSummary(summaryRes)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.disableTopOptionsMenu()
    }
}
