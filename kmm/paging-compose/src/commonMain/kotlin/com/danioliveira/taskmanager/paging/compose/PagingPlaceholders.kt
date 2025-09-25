package com.danioliveira.taskmanager.paging.compose

/**
 * Gets a paging placeholder key for the given index.
 */
internal fun getPagingPlaceholderKey(index: Int) = PagingPlaceholderKey(index)

/**
 * A key for paging placeholders.
 * This is a simple data class that holds the index value.
 */
@PagingParcelize
internal data class PagingPlaceholderKey(val index: Int) : PagingParcelable

/**
 * Content type for paging placeholders.
 */
internal object PagingPlaceholderContentType
