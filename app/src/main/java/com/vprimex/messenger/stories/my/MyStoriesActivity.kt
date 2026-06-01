package com.vprimex.messenger.stories.my

import androidx.fragment.app.Fragment
import com.vprimex.messenger.components.FragmentWrapperActivity

class MyStoriesActivity : FragmentWrapperActivity() {
  override fun getFragment(): Fragment {
    return MyStoriesFragment()
  }
}
