package com.musicianhelper.mergequestion

import io.reactivex.rxjava3.observers.DisposableObserver

class StateObserver() : DisposableObserver<State>() {

  override fun onNext(t: State) {
    //do nothing
    //here we propagate new values to UI, don't need for test purposes
  }

  override fun onError(e: Throwable?) {
    println(e?.message ?: "No error")
  }

  override fun onComplete() {
    // do nothing
  }
}