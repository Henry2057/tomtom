package com.example.tomtom.util

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

object FragmentNavigationUtil {
    fun replaceFragment(
        fragmentManager: FragmentManager,
        @IdRes containerViewId: Int,
        fragment: Fragment,
        tag: String? = null
    ) {
        fragmentManager.beginTransaction().apply {
            replace(containerViewId, fragment, tag)
            commitNow()
        }
    }
}