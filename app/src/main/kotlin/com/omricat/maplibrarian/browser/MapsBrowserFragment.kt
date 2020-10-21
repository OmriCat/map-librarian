package com.omricat.maplibrarian.browser

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.omricat.maplibrarian.R

class MapsBrowserFragment : Fragment(R.layout.maps_browser_fragment) {

    companion object {
        fun newInstance() = MapsBrowserFragment()
    }

    private lateinit var viewModel: MapsBrowserViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MapsBrowserViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
