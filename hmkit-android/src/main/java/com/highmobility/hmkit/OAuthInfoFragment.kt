package com.highmobility.hmkit


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_oauth_info.*

internal class OAuthInfoFragment(val view: IInfoView, val text: String, val error: Boolean) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_oauth_info, container, false)
        return view!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView.text = this.text

        if (error) {
            closeButton.visibility = VISIBLE
            progressBar.visibility = GONE
        }

        closeButton.setOnClickListener {
            this.view.onCloseButtonClicked()
        }
    }

    fun showText(text: String, error: Boolean) {
        textView.text = text
        closeButton.visibility = if (error) VISIBLE else GONE
    }

    companion object {
        @JvmStatic
        fun newInstance(view: IInfoView, text: String, error: Boolean): OAuthInfoFragment {
            return OAuthInfoFragment(view, text, error)
        }
    }
}

interface IInfoView {
    fun onCloseButtonClicked()
}
