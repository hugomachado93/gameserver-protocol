package com.example.gameserver

import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

//PROTOCOL
//1 byte - COMAND
// 2 byte - PAYLOAD SIZE
// 3/* - PAYLOAD

class ServerProtocol {

    fun startGameServer() {
        Executors.newVirtualThreadPerTaskExecutor().use {

            val server = ServerSocket(1234)
            val games = mutableMapOf<String, MutableList<Socket>>()
            val gameSessionQueue = LinkedBlockingQueue<String>()

            it.submit {
                while (true) {
                    val gameSessionId = gameSessionQueue.take()
                    games[gameSessionId]?.forEach { s ->
                        //close the socket and send command to client
                        val buffer = ByteArray(3)
                        buffer[0] = ServerCommands.GAME_INFO.value.toByte()
                        buffer[1] = 0x00.toByte()
                        s.getOutputStream().write(buffer)
                        s.close()
                    }
                    games.remove(gameSessionId)
                    println("Match $gameSessionId ended")
                }
            }

            while (true) {
                val socket = server.accept()
                findGame(games)?.let { g ->
                    println("Initiating game")
                    games[g]?.add(socket)
                    it.submit { Game(games[g]!!, g, gameSessionQueue).startNewGame() }
                } ?: run {
                    println("Initianting game room")
                    val sessionId = UUID.randomUUID().toString()
                    games[sessionId] = mutableListOf(socket)
                }
            }
        }
    }

    private fun findGame(matches: Map<String, MutableList<Socket>>) : String? {
        matches.forEach { (sessionId, sockets) ->
            if (sockets.size == 1) {
                return sessionId
            }
        }
        return null
    }

}