package com.smartgallery.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.smartgallery.data.repository.AlbumRepository

class AlbumsViewModel(
    val albumRepository: AlbumRepository
) : ViewModel()
