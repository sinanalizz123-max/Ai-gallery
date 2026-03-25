package com.smartgallery.data.model

data class SearchQuery(
    val text: String = "",
    val minPeople: Int? = null,
    val maxPeople: Int? = null
)
