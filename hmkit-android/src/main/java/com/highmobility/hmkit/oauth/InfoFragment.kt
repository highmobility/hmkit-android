package com.highmobility.hmkit.oauth


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.highmobility.hmkit.R

internal class InfoFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_oauth_info, container, false)
        return view!!
    }

    fun showText(text: String, error: Boolean) {
        // there is no info text. activity will just finish
        // TODO: 2018-12-12 if error, show error and add close button
    }

    companion object {
        @JvmStatic
        fun newInstance(): InfoFragment {
            return InfoFragment()
        }
    }

}
