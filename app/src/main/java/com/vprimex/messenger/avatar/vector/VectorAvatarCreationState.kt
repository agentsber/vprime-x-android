package com.vprimex.messenger.avatar.vector

import com.vprimex.messenger.avatar.Avatar
import com.vprimex.messenger.avatar.AvatarColorItem
import com.vprimex.messenger.avatar.Avatars

data class VectorAvatarCreationState(
  val currentAvatar: Avatar.Vector
) {
  fun colors(): List<AvatarColorItem> = Avatars.colors.map { AvatarColorItem(it, currentAvatar.color == it) }
}
