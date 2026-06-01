package com.vprimex.messenger.conversationlist;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.RequestManager;

import com.vprimex.messenger.BindableConversationListItem;
import com.vprimex.messenger.R;
import com.vprimex.messenger.conversationlist.model.ConversationSet;
import com.vprimex.messenger.database.model.ThreadWithRecipient;

import java.util.Locale;
import java.util.Set;

public class ConversationListItemAction extends FrameLayout implements BindableConversationListItem {

  private TextView description;

  public ConversationListItemAction(Context context) {
    super(context);
  }

  public ConversationListItemAction(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ConversationListItemAction(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();
    this.description = findViewById(R.id.description);
  }

  @Override
  public void bind(@NonNull LifecycleOwner lifecycleOwner,
                   @NonNull ThreadWithRecipient thread,
                   @NonNull RequestManager requestManager,
                   @NonNull Locale locale,
                   @NonNull Set<Long> typingThreads,
                   @NonNull ConversationSet selectedConversations,
                   long activeThreadId)
  {
    this.description.setText(getContext().getString(R.string.ConversationListItemAction_archived_conversations_d, thread.getUnreadCount()));
  }

  @Override
  public void unbind() {

  }

  @Override
  public void setActiveThreadId(long activeThreadId) {

  }

  @Override
  public void setSelectedConversations(@NonNull ConversationSet conversations) {

  }

  @Override
  public void updateTypingIndicator(@NonNull Set<Long> typingThreads) {

  }

  @Override
  public void updateTimestamp() {
    // Intentionally left blank.
  }
}
