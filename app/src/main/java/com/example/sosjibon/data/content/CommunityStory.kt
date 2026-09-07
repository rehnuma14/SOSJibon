package com.example.sosjibon.data.content

data class CommunityStory(
    val id: String,
    val authorName: String,
    val title: String,
    val excerpt: String
)

val sampleCommunityStories = listOf(
    CommunityStory(
        id = "1",
        authorName = "Rafiq H.",
        title = "How a stranger saved my father",
        excerpt = "A passerby recognized the signs of a heart attack and started CPR before the ambulance arrived."
    ),
    CommunityStory(
        id = "2",
        authorName = "Nusrat J.",
        title = "First aid training changed how I react",
        excerpt = "After taking a first-aid course, I finally knew what to do when my neighbor collapsed from heatstroke."
    ),
    CommunityStory(
        id = "3",
        authorName = "Kamal U.",
        title = "Recovering after a road accident",
        excerpt = "Six months ago I was in a serious accident. Here's what I learned about quick first response."
    )
)
