/*
 * The MIT License
 *
 * Copyright (c) 2014- High-Mobility GmbH (https://high-mobility.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.highmobility.hmkit

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Initialize the NavigationManager with a FragmentManager, which will be used at the
 * fragment transactions.
 *
 *
 * Taken from https://www.toptal.com/android/android-fragment-navigation-pattern
 */
internal class OAuthNavigationManager(private val fragmentManager: FragmentManager) {

    /**
     * Listener interface for navigation events.
     */
    interface NavigationListener {

        /**
         * Callback on backstack changed.
         */
        fun onBackstackChanged()
    }

    private var mNavigationListener: NavigationListener? = null

    fun init() {
        fragmentManager.addOnBackStackChangedListener {
            if (mNavigationListener != null) {
                mNavigationListener!!.onBackstackChanged()
            }
        }
    }

    /**
     * Displays the next fragment
     *
     * @param fragment
     */
    private fun open(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.main_container, fragment)
                .setCustomAnimations(R.animator.slide_in_left,
                        R.animator.slide_out_right,
                        R.animator.slide_in_right,
                        R.animator.slide_out_left)
                // dont add to back stack, otherwise goes from info to empty webView
                /*.addToBackStack(fragment.toString())*/
                .commit()
    }

    /**
     * pops every fragment and starts the given fragment as a new one.
     *
     * @param fragment
     */
    private fun openAsRoot(fragment: Fragment) {
        popEveryFragment()
        open(fragment)
    }


    /**
     * Pops all the queued fragments
     */
    private fun popEveryFragment() {
        // Clear all back stack.
        val backStackCount = fragmentManager.backStackEntryCount
        for (i in 0 until backStackCount) {

            // Get the back stack fragment id.
            val backStackId = fragmentManager.getBackStackEntryAt(i).id

            fragmentManager.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        }
    }

    /**
     * Navigates back by popping the back stack. If there is no more items left we finish the current activity.
     *
     * @param baseActivity
     */
    fun navigateBack(baseActivity: Activity) {
        if (fragmentManager.backStackEntryCount == 0) {
            // we can finish the base activity since we have no other fragments
            baseActivity.finish()
        }
        else {
            fragmentManager.popBackStackImmediate()
        }
    }

    fun startWebView(iWebView: IWebView, url: String): WebViewFragment {
        val fragment = WebViewFragment.newInstance(iWebView, url)
        openAsRoot(fragment)
        return fragment
    }

    fun startInfo(infoView: IInfoView, text: String, error: Boolean): OAuthInfoFragment {
        val fragment = OAuthInfoFragment.newInstance(infoView, text, error)
        openAsRoot(fragment)
        return fragment
    }

    /**
     * @return true if the current fragment displayed is a root fragment
     */
    fun isRootFragmentVisible(): Boolean {
        return fragmentManager.backStackEntryCount <= 1
    }

    fun getNavigationListener(): NavigationListener? {
        return mNavigationListener
    }

    fun setNavigationListener(navigationListener: NavigationListener) {
        mNavigationListener = navigationListener
    }
}