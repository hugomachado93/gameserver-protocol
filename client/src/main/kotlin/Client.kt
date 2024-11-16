import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


class Client {

    val worldTile = 10 * 10
    var player1x = 0
    var player1y = 0

    var player2x = 100
    var player2y = 100

    private val running = AtomicBoolean(true)

    fun startClient() {
        val socket = Socket("localhost", 1234)
        val executor = Executors.newVirtualThreadPerTaskExecutor()
        executor.submit { listen(socket) }
        handleInputs(socket)
    }

    fun listen(socket: Socket) {
        println(socket.isConnected)
        while (running.get()) {
            val buffer = ByteArray(2)
            val inputStream = socket.getInputStream()
            inputStream.read(buffer)

            println("Reading from server")

            val byteLength = buffer[1]
            val size = byteLength.toInt()

            val bufferPayload = ByteArray(size)
            inputStream.read(bufferPayload)
            val commandInt = buffer[0].toInt()
            println(commandInt)
            val command = ServerCommands.fromValue(commandInt)

            if(command == ServerCommands.GAME_INFO) {
                val payloadBuff = ByteBuffer.wrap(bufferPayload)

                this.player1x = payloadBuff.getInt()
                this.player1y = payloadBuff.getInt()
                this.player2x = payloadBuff.getInt()
                this.player2y = payloadBuff.getInt()

                println(player1x)
                println(player1y)
                println(player2x)
                println(player2y)
            }

        }
    }

    fun handleInputs(socket: Socket) {
        val reader: LineReader = LineReaderBuilder.builder().build()

        while (running.get()) {
            try {
                val line = reader.readLine()
                when(line) {
                    "w" -> {
                        val byteArray = ByteArray(3)
                        byteArray[0] = Commands.MOVE.value.toByte()
                        byteArray[1] = 1
                        byteArray[2] = Movement.UP.value.toByte()
                        socket.getOutputStream().write(byteArray)
                    }
                    "s" -> {
                        val byteArray = ByteArray(3)
                        byteArray[0] = Commands.MOVE.value.toByte()
                        byteArray[1] = 1
                        byteArray[2] = Movement.DOWN.value.toByte()
                        socket.getOutputStream().write(byteArray)
                    }
                    "a" -> {
                        val byteArray = ByteArray(3)
                        byteArray[0] = Commands.MOVE.value.toByte()
                        byteArray[1] = 1
                        byteArray[2] = Movement.LEFT.value.toByte()
                        socket.getOutputStream().write(byteArray)
                    }
                    "d" -> {
                        val byteArray = ByteArray(3)
                        byteArray[0] = Commands.MOVE.value.toByte()
                        byteArray[1] = 1
                        byteArray[2] = Movement.RIGHT.value.toByte()
                        socket.getOutputStream().write(byteArray)
                    }
                    "q" -> running.set(false)
                }
            }catch (_: Exception){}
        }
    }

    fun move(payload: ByteArray) {
        val value = payload[0].toInt()
        when(Movement.fromValue(value)) {
            Movement.UP -> player1x++
            Movement.DOWN -> player1x--
            Movement.LEFT -> player1y--
            Movement.RIGHT -> player1y++
            null -> TODO()
        }
        println("Moved player to position $player1x $player1y")
    }
}