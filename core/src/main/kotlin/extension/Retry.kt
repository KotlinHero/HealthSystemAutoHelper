package tech.kotlinhero.autohelper.core.extension


fun <T> retry(
    times: Int = 3,
    block: () -> T
): Result<T> {
    repeat(times) {
        runCatching { block() }.onSuccess { return Result.success(it) }
    }
    return Result.failure(Exception("Retry $times times failed"))
}