package com.vprimex.messenger.components.settings.app.changenumber

import androidx.fragment.app.Fragment
import com.vprimex.messenger.components.settings.app.AppSettingsActivity

/**
 * Helpers for various aspects of the change number flow.
 */
object ChangeNumberUtil {

  fun Fragment.changeNumberSuccess() {
    requireActivity().finish()
    requireActivity().startActivity(AppSettingsActivity.home(requireContext(), AppSettingsActivity.ACTION_CHANGE_NUMBER_SUCCESS))
  }
}
