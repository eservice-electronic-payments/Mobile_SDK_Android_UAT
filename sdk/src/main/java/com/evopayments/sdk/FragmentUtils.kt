package com.evopayments.sdk

import androidx.fragment.app.Fragment

internal inline fun <reified T> Fragment.getListenerOrThrowException(): T {
    return getListener<T>()
        ?: throw IllegalStateException("Calling class must implement:  " + T::class.java.simpleName)
}

internal inline fun <reified T> Fragment.getListener(): T? {
    var listener = getListenerFromTargetFragment<T>()
    if (listener != null) {
        return listener
    }

    listener = getListenerFromParentFragment<T>()
    if (listener != null) {
        return listener
    }

    return getListenerFromActivity()
}

internal inline fun <reified T> getListener(target: Any?): T? {
    return if (T::class.java.isInstance(target)) {
        T::class.java.cast(target)
    } else {
        null
    }
}

internal inline fun <reified T> Fragment.getListenerFromTargetFragment(): T? {
    return getListener(targetFragment)
}

internal inline fun <reified T> Fragment.getListenerFromParentFragment(): T? {
    return getListener<T>(parentFragment)
}

internal inline fun <reified T> Fragment.getListenerFromActivity(): T? {
    return getListener<T>(activity)
}