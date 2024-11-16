package com.example.gameserver

import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors

class Game(private var players: MutableList<Socket>, private val gameSessionId: String, private val gameSessionQueue: BlockingQueue<String>) {

    val worldTile = 10 * 10
    private var player1x = 0
    private var player1y = 0

    private var player2x = 100
    private var player2y = 100

    fun addNewPlayer(psocket: Socket) {
        players.add(psocket)
        val executor = Executors.newVirtualThreadPerTaskExecutor()
        executor.submit { processPlayerActions(psocket) }
    }

    fun startNewGame() {
        println("Starting new game")
        val executor = Executors.newVirtualThreadPerTaskExecutor()

        players.forEach { v ->
            executor.submit { processPlayerActions(v) }
        }

    }

    private fun processPlayerActions(socket: Socket) {
        val inputStream = socket.getInputStream()
        val outputStream = socket.getOutputStream()
        while(true) {

            val buffer = ByteArray(2)

            var size = inputStream.read(buffer)
            if (size == -1) {
                println("Client ${socket.inetAddress.address} disconnected")
                gameSessionQueue.put(gameSessionId)
                break
            }

            val commandByte = buffer[0].toInt()
            val byteLength = buffer[1]
            size = byteLength.toInt()

            val bufferPayload = ByteArray(size)

            inputStream.read(bufferPayload)

            val command = Commands.fromValue(commandByte)
            processCommand(command, bufferPayload, outputStream)

            sendGameInfo(outputStream)
        }
        println("The game is over")
    }

    private fun processCommand(
        command: Commands?,
        bufferPayload: ByteArray,
        outputStream: OutputStream
    ) {
        when (command) {
            Commands.MOVE -> {
                val bufferMsg = move(bufferPayload)
                outputStream.write(bufferMsg)
            }

            Commands.ATTACK -> TODO()
            null -> TODO()
        }
    }

    fun move(payload: ByteArray) : ByteArray {
        val value = payload[0].toInt()
        when(Movement.fromValue(value)) {
            Movement.UP -> player1x++
            Movement.DOWN -> player1x--
            Movement.LEFT -> player1y--
            Movement.RIGHT -> player1y++
            null -> TODO()
        }
        val byteArray = ByteArray(3)
        byteArray[0] = Commands.MOVE.value.toByte()
        byteArray[1] = 1
        byteArray[2] = payload[0]

        return byteArray
    }

    fun sendGameInfo(outputStream: OutputStream) {
        val bufferResponse = ByteArray(18)
        bufferResponse[0] = ServerCommands.GAME_INFO.value.toByte()
        bufferResponse[1] = 16.toByte()

        System.arraycopy(ByteBuffer.allocate(4).putInt(player1x).array(), 0, bufferResponse, 2, 4)
        System.arraycopy(ByteBuffer.allocate(4).putInt(player1y).array(), 0, bufferResponse, 6, 4)
        System.arraycopy(ByteBuffer.allocate(4).putInt(player2x).array(), 0, bufferResponse, 10, 4)
        System.arraycopy(ByteBuffer.allocate(4).putInt(player2y).array(), 0, bufferResponse, 14, 4)

        outputStream.write(bufferResponse)
    }

    fun ackCommand(outputStream: OutputStream) {
        outputStream.write(1)
    }
}