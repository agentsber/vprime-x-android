package com.vprimex.messenger.wallpaper

import com.vprimex.messenger.R
import com.vprimex.messenger.components.AvatarImageView
import com.vprimex.messenger.conversation.colors.AvatarColor
import com.vprimex.messenger.recipients.Recipient

sealed class WallpaperPreviewPortrait {
  class ContactPhoto(private val recipient: Recipient) : WallpaperPreviewPortrait() {
    override fun applyToAvatarImageView(avatarImageView: AvatarImageView) {
      avatarImageView.setAvatar(recipient)
      avatarImageView.colorFilter = null
    }
  }

  class SolidColor(private val avatarColor: AvatarColor) : WallpaperPreviewPortrait() {
    override fun applyToAvatarImageView(avatarImageView: AvatarImageView) {
      avatarImageView.setImageResource(R.drawable.circle_tintable)
      avatarImageView.setColorFilter(avatarColor.colorInt())
    }
  }

  abstract fun applyToAvatarImageView(avatarImageView: AvatarImageView)
}
