package com.danioliveira.taskmanager.paging.compose

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class PagingParcelize()

expect interface PagingParcelable
