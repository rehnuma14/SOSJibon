package com.example.sosjibon.data.content

data class CommunityStory(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val title: String = "",
    val excerpt: String = "",
    val category: String = "Healthcare Experience",
    val status: String = "published", // "pending", "published", "rejected"
    val timestamp: Long = System.currentTimeMillis()
)

val sampleCommunityStories = listOf(
    CommunityStory(
        id = "1",
        authorUid = "sample_1",
        authorName = "Rafiq H.",
        title = "How a stranger saved my father",
        excerpt = "A passerby recognized the signs of a heart attack and started CPR before the ambulance arrived.",
        category = "Critical Care",
        status = "published"
    ),
    CommunityStory(
        id = "2",
        authorUid = "sample_2",
        authorName = "Nusrat J.",
        title = "First aid training changed how I react",
        excerpt = "After taking a first-aid course, I finally knew what to do when my neighbor collapsed from heatstroke.",
        category = "First Aid",
        status = "published"
    ),
    CommunityStory(
        id = "3",
        authorUid = "sample_3",
        authorName = "Kamal U.",
        title = "Recovering after a road accident",
        excerpt = "Six months ago I was in a serious accident. Here's what I learned about quick first response.",
        category = "Accident Emergency",
        status = "published"
    )
)
