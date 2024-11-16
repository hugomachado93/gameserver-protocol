package com.example.gameserver

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SpringBootApplication
class GameserverApplication : CommandLineRunner {

    override fun run(vararg args: String?) {
         println("Start server")
        ServerProtocol().startGameServer()
    }

}

fun main(args: Array<String>) {
    runApplication<GameserverApplication>(*args)
}
