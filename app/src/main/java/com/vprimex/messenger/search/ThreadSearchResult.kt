package com.vprimex.messenger.search

import com.vprimex.messenger.database.model.ThreadWithRecipient

data class ThreadSearchResult(val results: List<ThreadWithRecipient>, val query: String)
