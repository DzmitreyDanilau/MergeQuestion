package com.musicianhelper.mergequestion

import io.reactivex.rxjava3.core.Observable

interface View {

  fun events(): Observable<Event>
}
