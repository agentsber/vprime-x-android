package com.vprimex.messenger.components.settings.conversation.permissions

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.Log
import org.signal.core.util.orNull
import com.vprimex.messenger.database.GroupTable
import com.vprimex.messenger.database.SignalDatabase
import com.vprimex.messenger.groups.GroupAccessControl
import com.vprimex.messenger.groups.GroupChangeException
import com.vprimex.messenger.groups.GroupId
import com.vprimex.messenger.groups.GroupManager
import com.vprimex.messenger.groups.ui.GroupChangeErrorCallback
import com.vprimex.messenger.groups.ui.GroupChangeFailureReason
import java.io.IOException

private val TAG = Log.tag(PermissionsSettingsRepository::class.java)

class PermissionsSettingsRepository(
  private val context: Context,
  private val groupTable: GroupTable = SignalDatabase.groups
) {

  fun applyMembershipRightsChange(groupId: GroupId, newRights: GroupAccessControl, error: GroupChangeErrorCallback) {
    SignalExecutors.UNBOUNDED.execute {
      try {
        GroupManager.applyMembershipAdditionRightsChange(context, groupId.requireV2(), newRights)
      } catch (e: GroupChangeException) {
        Log.w(TAG, e)
        error.onError(GroupChangeFailureReason.fromException(e))
      } catch (e: IOException) {
        Log.w(TAG, e)
        error.onError(GroupChangeFailureReason.fromException(e))
      }
    }
  }

  fun applyAttributesRightsChange(groupId: GroupId, newRights: GroupAccessControl, error: GroupChangeErrorCallback) {
    SignalExecutors.UNBOUNDED.execute {
      try {
        GroupManager.applyAttributesRightsChange(context, groupId.requireV2(), newRights)
      } catch (e: GroupChangeException) {
        Log.w(TAG, e)
        error.onError(GroupChangeFailureReason.fromException(e))
      } catch (e: IOException) {
        Log.w(TAG, e)
        error.onError(GroupChangeFailureReason.fromException(e))
      }
    }
  }

  fun applyAnnouncementGroupChange(groupId: GroupId, isAnnouncementGroup: Boolean, error: GroupChangeErrorCallback) {
    SignalExecutors.UNBOUNDED.execute {
      try {
        GroupManager.applyAnnouncementGroupChange(context, groupId.requireV2(), isAnnouncementGroup)
      } catch (e: GroupChangeException) {
        Log.w(TAG, e)
        error.onError(GroupChangeFailureReason.fromException(e))
      } catch (e: IOException) {
        Log.w(TAG, e)
        error.onError(GroupChangeFailureReason.fromException(e))
      }
    }
  }

  fun hasNonAdminMembersWithLabels(groupId: GroupId): Boolean {
    val v2GroupId = groupId.v2OrNull() ?: return false
    val group = groupTable.getGroup(v2GroupId).orNull() ?: return false
    return group.requireV2GroupProperties().nonAdminMembersWithLabels().isNotEmpty()
  }

  fun applyMemberLabelRightsChange(groupId: GroupId, newRights: GroupAccessControl, errorCallback: GroupChangeErrorCallback) {
    SignalExecutors.UNBOUNDED.execute {
      try {
        GroupManager.applyMemberLabelRightsChange(context, groupId.requireV2(), newRights)
      } catch (e: GroupChangeException) {
        Log.w(TAG, e)
        errorCallback.onError(GroupChangeFailureReason.fromException(e))
      } catch (e: IOException) {
        Log.w(TAG, e)
        errorCallback.onError(GroupChangeFailureReason.fromException(e))
      }
    }
  }
}
