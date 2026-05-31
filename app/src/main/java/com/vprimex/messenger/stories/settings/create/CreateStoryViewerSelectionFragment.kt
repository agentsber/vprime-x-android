package com.vprimex.messenger.stories.settings.create

import androidx.navigation.fragment.findNavController
import com.vprimex.messenger.R
import com.vprimex.messenger.database.model.DistributionListId
import com.vprimex.messenger.recipients.RecipientId
import com.vprimex.messenger.stories.settings.select.BaseStoryRecipientSelectionFragment
import com.vprimex.messenger.util.navigation.safeNavigate

/**
 * Allows user to select who will see the story they are creating
 */
class CreateStoryViewerSelectionFragment : BaseStoryRecipientSelectionFragment() {
  override val actionButtonLabel: Int = R.string.CreateStoryViewerSelectionFragment__next
  override val distributionListId: DistributionListId? = null

  override fun goToNextScreen(recipients: Set<RecipientId>) {
    findNavController().safeNavigate(CreateStoryViewerSelectionFragmentDirections.actionCreateStoryViewerSelectionToCreateStoryWithViewers(recipients.toTypedArray()))
  }
}
