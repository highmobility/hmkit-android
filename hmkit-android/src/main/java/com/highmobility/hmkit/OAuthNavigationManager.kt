package com.highmobility.hmkit

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Initialize the NavigationManager with a FragmentManager, which will be used at the
 * fragment transactions.
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
                .addToBackStack(fragment.toString())
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
     * Navigates back by popping teh back stack. If there is no more items left we finish the current activity.
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