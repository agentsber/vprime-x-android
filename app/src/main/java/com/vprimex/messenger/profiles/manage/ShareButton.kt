package com.vprimex.messenger.profiles.manage

import com.vprimex.messenger.databinding.ShareButtonBinding
import com.vprimex.messenger.util.adapter.mapping.BindingFactory
import com.vprimex.messenger.util.adapter.mapping.BindingViewHolder
import com.vprimex.messenger.util.adapter.mapping.MappingAdapter
import com.vprimex.messenger.util.adapter.mapping.MappingModel

object ShareButton {
  fun register(adapter: MappingAdapter) {
    adapter.registerFactory(Model::class.java, BindingFactory(::ViewHolder, ShareButtonBinding::inflate))
  }

  class Model(
    val text: CharSequence,
    val onClick: (Model) -> Unit
  ) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean = true

    override fun areContentsTheSame(newItem: Model): Boolean = text == newItem.text
  }

  private class ViewHolder(binding: ShareButtonBinding) : BindingViewHolder<Model, ShareButtonBinding>(binding) {
    override fun bind(model: Model) {
      binding.shareButton.setOnClickListener { model.onClick(model) }
    }
  }
}
