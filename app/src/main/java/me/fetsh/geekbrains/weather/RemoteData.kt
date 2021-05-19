package me.fetsh.geekbrains.weather

sealed class RemoteData<out V : Any, out E : Any> {
    object NotAsked : RemoteData<Nothing, Nothing>()
    object Loading : RemoteData<Nothing, Nothing>()
    data class Success<out V : Any>(val value : V) : RemoteData<V, Nothing>()
    data class Failure<out E : Any>(val error : E) : RemoteData<Nothing, E>()

    val isNotAsked
        get() = this is NotAsked

    val isLoading
        get() = this is Loading

    val isSuccess
        get() = this is Success

    val isFailure
        get() = this is Failure
}