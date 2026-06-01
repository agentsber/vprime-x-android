package com.vprimex.messenger.stories.settings.connections

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.vprimex.messenger.R
import com.vprimex.messenger.components.ViewBinderDelegate
import com.vprimex.messenger.components.WrapperDialogFragment
import com.vprimex.messenger.contacts.paged.ContactSearchAdapter
import com.vprimex.messenger.contacts.paged.ContactSearchConfiguration
import com.vprimex.messenger.contacts.paged.ContactSearchPagedDataSourceRepository
import com.vprimex.messenger.contacts.paged.ContactSearchRepository
import com.vprimex.messenger.contacts.paged.ContactSearchViewModel
import com.vprimex.messenger.database.RecipientTable
import com.vprimex.messenger.databinding.ViewAllSignalConnectionsFragmentBinding
import com.vprimex.messenger.groups.SelectionLimits
import com.vprimex.messenger.search.SearchRepository

class ViewAllSignalConnectionsFragment : Fragment(R.layout.view_all_signal_connections_fragment) {

  private val binding by ViewBinderDelegate(ViewAllSignalConnectionsFragmentBinding::bind)

  private val contactSearchViewModel: ContactSearchViewModel by viewModels {
    ContactSearchViewModel.Factory(
      selectionLimits = SelectionLimits(0, 0),
      isMultiSelect = false,
      repository = ContactSearchRepository(),
      performSafetyNumberChecks = false,
      arbitraryRepository = null,
      searchRepository = SearchRepository(requireContext().getString(R.string.note_to_self)),
      contactSearchPagedDataSourceRepository = ContactSearchPagedDataSourceRepository(requireContext())
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.toolbar.setNavigationOnClickListener {
      requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    binding.recycler.bind(
      viewModel = contactSearchViewModel,
      fragmentManager = childFragmentManager,
      displayOptions = ContactSearchAdapter.DisplayOptions(
        displayCheckBox = false,
        displaySecondaryInformation = ContactSearchAdapter.DisplaySecondaryInformation.NEVER
      ),
      mapStateToConfiguration = { getConfiguration() }
    )
  }

  private fun getConfiguration(): ContactSearchConfiguration {
    return ContactSearchConfiguration.build {
      addSection(
        ContactSearchConfiguration.Section.Individuals(
          includeHeader = false,
          includeSelfMode = RecipientTable.IncludeSelfMode.Exclude,
          includeLetterHeaders = true,
          transportType = ContactSearchConfiguration.TransportType.PUSH
        )
      )
    }
  }

  class Dialog : WrapperDialogFragment() {
    override fun getWrappedFragment(): Fragment {
      return ViewAllSignalConnectionsFragment()
    }

    companion object {
      fun show(fragmentManager: FragmentManager) {
        Dialog().show(fragmentManager, null)
      }
    }
  }
}
