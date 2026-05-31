package com.vprimex.messenger.mediasend.v2

import android.view.animation.Interpolator
import com.vprimex.messenger.util.createDefaultCubicBezierInterpolator

object MediaAnimations {
  /**
   * Fast-In-Extra-Slow-Out Interpolator
   */
  @JvmStatic
  val interpolator: Interpolator = createDefaultCubicBezierInterpolator()
}
