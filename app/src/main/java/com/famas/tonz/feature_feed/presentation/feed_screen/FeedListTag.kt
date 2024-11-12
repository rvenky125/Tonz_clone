package com.famas.tonz.feature_feed.presentation.feed_screen

enum class FeedListTag(val label: String, val value: Int) {
    RecentUploads("Recent Uploads", 0),
    LatestSongs("Recent Releases", 1),
    Popular("Most Liked", 2),
}