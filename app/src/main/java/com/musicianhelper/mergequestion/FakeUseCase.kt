package com.musicianhelper.mergequestion

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource

abstract class FakeUseCase<A : Action, R : com.musicianhelper.mergequestion.Result> :
    UseCase<A, R> {

    abstract var result: R

    override fun apply(upstream: Observable<A>): ObservableSource<R> {
        return upstream.map { result }
    }
}
