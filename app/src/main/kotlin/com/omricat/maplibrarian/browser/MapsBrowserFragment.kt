package com.omricat.maplibrarian.browser

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.omricat.maplibrarian.R

class MapsBrowserFragment : Fragment(R.layout.maps_browser_fragment) {

    companion object {
        fun newInstance() = MapsBrowserFragment()
    }

    private lateinit var viewModel: MapsBrowserViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MapsBrowserViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
