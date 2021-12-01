package com.musicianhelper.mergequestion

import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding4.widget.checkedChanges
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class MainActivity : AppCompatActivity(), View, EventListener {

    private val viewModel: StatePresenter<MainState> =
        MainViewModel(AndroidSchedulers.mainThread())

    lateinit var check: Switch

    private val eventRelay = PublishRelay.create<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        check = findViewById<Switch>(R.id.view)
    }

    override fun events(): Observable<Event> {
        return Observable.merge(
            eventRelay,
            check.checkedChanges()
                .map {
                    ClickEvent
                }
        )
    }

    override fun event(event: Event) {
        eventRelay.accept(event)
    }

    override fun onResume() {
        super.onResume()
        viewModel.state(events()).subscribeWith(StateObserver())
    }
}

object ClickEvent : Event
object InitialEvent : Event
