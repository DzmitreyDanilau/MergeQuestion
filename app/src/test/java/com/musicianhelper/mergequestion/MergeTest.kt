package com.musicianhelper.mergequestion

import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.observables.ConnectableObservable
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.Before
import org.junit.Test

class MergeTest {

    private val mainScheduler = TestScheduler()
    private val eventRelay = PublishRelay.create<Event>()

    private lateinit var viewModel: MainViewModel

    private val stateObserver = TestObserver<MainState>()

    private val initialUseCase = object : FakeUseCase<InitialAction, MainResult.InitialResult>() {
        override var result: MainResult.InitialResult = MainResult.InitialResult
    }

    private val mainUseCase = object : FakeUseCase<ClickAction, MainResult.RequiredResult>() {
        override var result: MainResult.RequiredResult = MainResult.RequiredResult
    }

    @Before
    fun setup() {
        viewModel = MainViewModel(
            scheduler = mainScheduler,
            initialUseCase = initialUseCase,
            mainUseCase = mainUseCase
        )
    }

    @Test
    fun `click event should change state to RequiredState`() {

        val toggleObservable: ConnectableObservable<ClickEvent> = Observable.just(ClickEvent).publish()

        val eventObservable = Observable.merge(
            eventRelay,
            toggleObservable
        )

        viewModel.state(eventObservable).subscribe(stateObserver)
        mainScheduler.triggerActions()
        toggleObservable.connect()
        mainScheduler.triggerActions()

        val latestState = stateObserver.values().last() as MainState

        assertThat(latestState, instanceOf(MainState.RequiredState::class.java))
    }

}

class Test : Observable<Event>() {
    override fun subscribeActual(observer: Observer<in Event>) {
        observer.onNext(ClickEvent)
    }
}
