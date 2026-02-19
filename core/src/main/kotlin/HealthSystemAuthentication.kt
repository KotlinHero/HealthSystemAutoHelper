package tech.kotlinhero.autohelper.core

interface HealthSystemAuthentication {
    val username: String
    val password: String
}

data class SimpleHealthSystemAuthentication(
    override val username: String,
    override val password: String
) : HealthSystemAuthentication

fun healthSystemAuthentication(username: String, password: String): HealthSystemAuthentication =
    SimpleHealthSystemAuthentication(username, password)