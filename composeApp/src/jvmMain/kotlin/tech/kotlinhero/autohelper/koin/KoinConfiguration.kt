package tech.kotlinhero.autohelper.koin

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tech.kotlinhero.autohelper.ui.viewmodel.TaskExecuteViewModel

fun initKoin() {
    startKoin {
        modules(viewModelModule)
    }
}

private val viewModelModule = module {
    viewModel { TaskExecuteViewModel() }
}