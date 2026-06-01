package com.vprimex.messenger.sharing.interstitial;

import com.vprimex.messenger.R;
import com.vprimex.messenger.util.adapter.mapping.MappingAdapter;
import com.vprimex.messenger.util.viewholders.RecipientViewHolder;

class ShareInterstitialSelectionAdapter extends MappingAdapter {
  ShareInterstitialSelectionAdapter() {
    registerFactory(ShareInterstitialMappingModel.class, RecipientViewHolder.createFactory(R.layout.share_contact_selection_item, null));
  }
}
