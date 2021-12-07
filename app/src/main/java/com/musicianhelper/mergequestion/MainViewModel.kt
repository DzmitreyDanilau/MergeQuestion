package com.musicianhelper.mergequestion

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.core.Scheduler
import timber.log.Timber

class MainViewModel(
    private val scheduler: Scheduler = AndroidSchedulers.mainThread(),
    private val initialUseCase: UseCase<InitialAction, MainResult.InitialResult> = MainUseCase(),
    private val mergeUseCase: UseCase<ClickAction, MainResult.RequiredResult> = MergeUseCase()
) : ViewModel(), StatePresenter<MainState> {

    private val relay = PublishRelay.create<Action>()

    private val submit: ObservableTransformer<Action, Result> =
        ObservableTransformer<Action, Result> { actions ->
            actions.publish { sharedAction ->
                sharedAction.ofType(InitialAction::class.java).compose(initialUseCase)
                sharedAction.ofType(ClickAction::class.java).compose(mergeUseCase)
            }
        }

    override fun state(eventObservable: Observable<Event>): Observable<MainState> {
        return result(eventObservable)
            .map {
                when (it) {
                    is MainResult.InitialResult -> {
                        MainState.InitialState
                    }
                    is MainResult.RequiredResult -> {
                        MainState.RequiredState
                    }
                    else -> {
                        MainState.InitialState
                    }
                }
            }
            .observeOn(scheduler)
    }

    private fun result(eventObservable: Observable<Event>): Observable<Result> {
        /**
         * We merging (subscribe to) event observable twice, to be able to multicasting events
         */
        return Observable.merge(
            Observable.merge(
                relay,
                eventObservable.map(::toAction)
            ).compose(submit),
            eventObservable.map(::toResult),
        )
    }

    private fun toAction(event: Event): Action {
        Timber.d("toAction()")
        return when (event) {
            is ClickEvent -> ClickAction
            else -> object : Action {}
        }
    }

    private fun toResult(event: Event): Result {
        Timber.d("toResult()")
        return object : Result {}
    }
}

sealed class MainState : State {
    object InitialState : MainState()
    object RequiredState : MainState()
}

sealed class MainResult : Result {
    object InitialResult : MainResult()
    object RequiredResult : MainResult()
}

interface UseCase<A : Action, R : Result> : ObservableTransformer<A, R>

object InitialAction : Action
object ClickAction : Action

class MainUseCase : UseCase<InitialAction, MainResult.InitialResult> {
    override fun apply(upstream: Observable<InitialAction>): ObservableSource<MainResult.InitialResult> {
        return upstream
            .map { MainResult.InitialResult }
            .doOnNext {
                Timber.d("MainUseCase: ${it.javaClass.name}, Thread: ${Thread.currentThread().name}")
            }
    }
}

class MergeUseCase : UseCase<ClickAction, MainResult.RequiredResult> {
    override fun apply(upstream: Observable<ClickAction>): ObservableSource<MainResult.RequiredResult> {
        return upstream
            .map { MainResult.RequiredResult }
            .doOnNext {
                Timber.d("MergeUseCase: ${it.javaClass.name}, Thread: ${Thread.currentThread().name}")
            }
    }
}
