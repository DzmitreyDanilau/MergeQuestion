package com.musicianhelper.mergequestion

import io.reactivex.rxjava3.core.Observable

interface StatePresenter<S : State> {

  fun state(eventObservable: Observable<Event>): Observable<S>
}