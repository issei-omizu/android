package com.example.isseiomizu.stopwatch.presentation.viewmodel.basics

import com.example.isseiomizu.stopwatch.presentation.viewmodel.ViewModelBase
import jp.keita.kagurazaka.rxproperty.Nothing
import jp.keita.kagurazaka.rxproperty.ReadOnlyRxProperty
import jp.keita.kagurazaka.rxproperty.RxCommand
import jp.keita.kagurazaka.rxproperty.RxProperty

/**
 * Created by isseiomizu on 2017/04/01.
 */

abstract class BasicsViewModel : ViewModelBase() {
    abstract val input: RxProperty<String>
    abstract val output: ReadOnlyRxProperty<String>
    abstract val command: RxCommand<Nothing>
}
