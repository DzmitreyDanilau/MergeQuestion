package com.musicianhelper.mergequestion

import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.TestScheduler
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
            mergeUseCase = mainUseCase
        )
    }

    @Test
    fun `click event should change state to RequiredState`() {
        val emitter = Emitter()

        val toggleObservable = TestObservable(emitter)

        val eventObservable = Observable.merge(
            eventRelay,
            toggleObservable
        )

        viewModel.state(eventObservable).subscribe(stateObserver)
        mainScheduler.triggerActions()
        emitter.emit()
        mainScheduler.triggerActions()

        //We take first, because we don't have the mechanism deu to simplicity that returns us
        //Previous State value for the cases when we return Result from the toResult(event: Event)
        //Func
        val latestState = stateObserver.values().first() as MainState

        assertThat(latestState, instanceOf(MainState.RequiredState::class.java))
    }

}

class TestObservable(private val emitter: Emitter) : Observable<Event>() {
    override fun subscribeActual(observer: Observer<in Event>) {
        val eventConsumer = EventConsumer(observer = observer, emitter = emitter)
        observer.onSubscribe(eventConsumer)
        emitter.addListener(eventConsumer)
    }

    private class EventConsumer(
        private val observer: Observer<in Event>,
        private val emitter: Emitter
    ) : EventListener, Disposable {

        override fun performEvent(e: Event) {
            observer.onNext(e)
        }

        override fun dispose() {
            emitter.removeListener()
        }

        override fun isDisposed(): Boolean {
            return false
        }
    }
}

interface EventListener {
    fun performEvent(e: Event)
}


class Emitter {

    private var listener: EventListener? = null

    fun addListener(listener: EventListener) {
        this.listener = listener
    }

    fun removeListener() {
        listener = null
    }

    fun emit() {
        listener?.performEvent(ClickEvent)
    }
}

