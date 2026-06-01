package com.vprimex.messenger.video.interfaces

fun interface TranscoderCancelationSignal {
  fun isCanceled(): Boolean
}
