package com.myprt.app.data

import com.myprt.app.data.model.Story

object DummyData {
    fun getListStory(): List<Story> {
        val listStory = arrayListOf<Story>()

        val story = Story(
            id = "story-lvGI8K4u5HBz6qkT",
            name = "farhan kebab",
            description = "hai",
            photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1718097423540_0bc73788ea6387801c89.jpg",
            createdAt = "2024-06-11T09:17:03.548Z",
            lat = null,
            lon = null
        )

        for (i in 0..5) {
            listStory.add(story)
        }

        return listStory
    }
}