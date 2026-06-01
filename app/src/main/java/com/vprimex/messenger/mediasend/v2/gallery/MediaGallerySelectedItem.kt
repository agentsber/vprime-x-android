package com.vprimex.messenger.mediasend.v2.gallery

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import org.signal.core.models.media.Media
import org.signal.glide.decryptableuri.DecryptableUri
import com.vprimex.messenger.R
import com.vprimex.messenger.util.MediaUtil
import com.vprimex.messenger.util.adapter.mapping.LayoutFactory
import com.vprimex.messenger.util.adapter.mapping.MappingAdapter
import com.vprimex.messenger.util.adapter.mapping.MappingModel
import com.vprimex.messenger.util.adapter.mapping.MappingViewHolder
import com.vprimex.messenger.util.visible

typealias OnSelectedMediaClicked = (Media) -> Unit

object MediaGallerySelectedItem {

  fun register(mappingAdapter: MappingAdapter, onSelectedMediaClicked: OnSelectedMediaClicked) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory({ ViewHolder(it, onSelectedMediaClicked) }, R.layout.v2_media_selection_item))
  }

  class Model(val media: Media) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean {
      return media.uri == newItem.media.uri
    }

    override fun areContentsTheSame(newItem: Model): Boolean {
      return media.uri == newItem.media.uri
    }
  }

  class ViewHolder(itemView: View, private val onSelectedMediaClicked: OnSelectedMediaClicked) : MappingViewHolder<Model>(itemView) {

    private val imageView: ImageView = itemView.findViewById(R.id.media_selection_image)
    private val videoOverlay: ImageView = itemView.findViewById(R.id.media_selection_play_overlay)

    override fun bind(model: Model) {
      Glide.with(imageView)
        .load(DecryptableUri(model.media.uri))
        .centerCrop()
        .into(imageView)

      videoOverlay.visible = MediaUtil.isVideo(model.media.contentType) && !model.media.isVideoGif
      itemView.setOnClickListener { onSelectedMediaClicked(model.media) }
    }
  }
}
