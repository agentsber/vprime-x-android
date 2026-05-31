package com.vprimex.messenger.mediasend.v2.gallery

import com.vprimex.messenger.util.adapter.mapping.MappingModel

data class MediaGalleryState(
  val bucketId: String?,
  val bucketTitle: String?,
  val items: List<MappingModel<*>> = listOf()
)
