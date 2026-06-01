package com.vprimex.messenger.avatar.text

import com.vprimex.messenger.avatar.Avatar
import com.vprimex.messenger.avatar.AvatarColorItem
import com.vprimex.messenger.avatar.Avatars

data class TextAvatarCreationState(
  val currentAvatar: Avatar.Text
) {
  fun colors(): List<AvatarColorItem> = Avatars.colors.map { AvatarColorItem(it, currentAvatar.color == it) }
}
