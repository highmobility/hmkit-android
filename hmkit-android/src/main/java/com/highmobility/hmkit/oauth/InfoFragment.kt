package com.highmobility.hmkit.oauth

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.highmobility.hmkit.R

internal class InfoFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_oauth_info, container, false)
        return view!!
    }

    fun showText(text: String) {
        // there is no info text. activity will just finish
    }

    companion object {
        @JvmStatic
        fun newInstance(): InfoFragment {
            return InfoFragment()
        }
    }

}
