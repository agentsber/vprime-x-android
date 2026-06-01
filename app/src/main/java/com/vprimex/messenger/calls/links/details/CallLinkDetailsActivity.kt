/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.vprimex.messenger.calls.links.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import org.signal.core.ui.compose.theme.SignalTheme
import org.signal.core.util.getParcelableExtraCompat
import com.vprimex.messenger.calls.links.EditCallLinkNameDialogFragment
import com.vprimex.messenger.main.MainNavigationCallDetailRouter
import com.vprimex.messenger.main.MainNavigationDetailLocation
import com.vprimex.messenger.service.webrtc.links.CallLinkRoomId
import com.vprimex.messenger.util.viewModel

class CallLinkDetailsActivity : FragmentActivity() {

  companion object {
    private const val ARG_ROOM_ID = "room.id"

    fun createIntent(context: Context, callLinkRoomId: CallLinkRoomId): Intent {
      return Intent(context, CallLinkDetailsActivity::class.java)
        .putExtra(ARG_ROOM_ID, callLinkRoomId)
    }
  }

  private val roomId: CallLinkRoomId
    get() = intent.getParcelableExtraCompat(ARG_ROOM_ID, CallLinkRoomId::class.java)!!

  private val viewModel: CallLinkDetailsViewModel by viewModel {
    CallLinkDetailsViewModel(roomId)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()

    super.onCreate(savedInstanceState)

    setContent {
      SignalTheme {
        CallLinkDetailsScreen(
          roomId = roomId,
          viewModel = viewModel,
          router = remember { Router() }
        )
      }
    }
  }

  private inner class Router : MainNavigationCallDetailRouter {
    override fun goToCallDetail(location: MainNavigationDetailLocation.Calls) {
      when (location) {
        is MainNavigationDetailLocation.Calls.CallLinks.EditCallLinkName -> {
          EditCallLinkNameDialogFragment().apply {
            arguments = bundleOf(EditCallLinkNameDialogFragment.ARG_NAME to viewModel.nameSnapshot)
          }.show(supportFragmentManager, null)
        }
      }
    }

    override fun exitDetailLocation() = finishAfterTransition()
  }
}
