package com.vprimex.messenger.components.settings.models

import android.view.View
import com.vprimex.messenger.R
import com.vprimex.messenger.components.settings.PreferenceModel
import com.vprimex.messenger.util.adapter.mapping.LayoutFactory
import com.vprimex.messenger.util.adapter.mapping.MappingAdapter
import com.vprimex.messenger.util.adapter.mapping.MappingViewHolder

object IndeterminateLoadingCircle : PreferenceModel<IndeterminateLoadingCircle>() {
  override fun areItemsTheSame(newItem: IndeterminateLoadingCircle): Boolean = true

  private class ViewHolder(itemView: View) : MappingViewHolder<IndeterminateLoadingCircle>(itemView) {
    override fun bind(model: IndeterminateLoadingCircle) = Unit
  }

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(IndeterminateLoadingCircle::class.java, LayoutFactory({ ViewHolder(it) }, R.layout.indeterminate_loading_circle_pref))
  }
}
