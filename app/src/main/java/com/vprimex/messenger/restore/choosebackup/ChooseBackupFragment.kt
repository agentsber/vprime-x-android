/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.restore.choosebackup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import org.signal.core.ui.logging.LoggingFragment
import org.signal.core.util.logging.Log
import com.vprimex.messenger.R
import com.vprimex.messenger.components.ViewBinderDelegate
import com.vprimex.messenger.databinding.FragmentChooseBackupBinding
import com.vprimex.messenger.keyvalue.SignalStore
import com.vprimex.messenger.registration.fragments.RegistrationViewDelegate
import com.vprimex.messenger.restore.RestoreViewModel
import com.vprimex.messenger.util.navigation.safeNavigate

/**
 * This fragment presents a button to the user to browse their local file system for a legacy backup file.
 */
class ChooseBackupFragment : LoggingFragment(R.layout.fragment_choose_backup) {
  private val sharedViewModel by activityViewModels<RestoreViewModel>()
  private val binding: FragmentChooseBackupBinding by ViewBinderDelegate(FragmentChooseBackupBinding::bind)

  private val pickMedia = registerForActivityResult(BackupFileContract()) {
    if (it != null) {
      onUserChoseBackupFile(it)
    } else {
      Log.i(TAG, "Null URI returned for backup file selection.")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    RegistrationViewDelegate.setDebugLogSubmitMultiTapView(binding.chooseBackupFragmentTitle)
    binding.chooseBackupFragmentButton.setOnClickListener { onChooseBackupSelected() }

    binding.chooseBackupFragmentLearnMore.text = HtmlCompat.fromHtml(String.format("<a href=\"%s\">%s</a>", getString(R.string.backup_support_url), getString(R.string.ChooseBackupFragment__learn_more)), 0)
    binding.chooseBackupFragmentLearnMore.movementMethod = LinkMovementMethod.getInstance()
  }

  private fun onChooseBackupSelected() {
    pickMedia.launch("application/octet-stream")
  }

  private fun onUserChoseBackupFile(backupFileUri: Uri) {
    sharedViewModel.setBackupFileUri(backupFileUri)
    NavHostFragment.findNavController(this).safeNavigate(ChooseBackupFragmentDirections.actionChooseLocalBackupFragmentToRestoreLocalBackupFragment())
  }

  private class BackupFileContract : ActivityResultContracts.GetContent() {
    override fun createIntent(context: Context, input: String): Intent {
      return super.createIntent(context, input).apply {
        putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        if (Build.VERSION.SDK_INT >= 26) {
          putExtra(DocumentsContract.EXTRA_INITIAL_URI, SignalStore.settings.latestSignalBackupDirectory)
        }
      }
    }
  }

  companion object {
    private val TAG = Log.tag(ChooseBackupFragment::class.java)
  }
}
