package com.realting.world.clip.stream

class ByteStream(private val buffer: ByteArray) {
    var offset = 0
    fun skip(length: Int) {
        offset += length
    }

    fun setOffset(position: Long) {
        offset = position.toInt()
    }

    fun length(): Int {
        return buffer.size
    }

    val byte: Byte
        get() = buffer[offset++]
    val uByte: Int
        get() = buffer[offset++].toInt() and 0xff
    val short: Int
        get() {
            var `val`: Int = (byte.toInt()  shl 8) + byte
            if (`val` > 32767) `val` -= 0x10000
            return `val`
        }
    val uShort: Int
        get() = (uByte shl 8) + uByte
    val int: Int
        get() = (uByte shl 24) + (uByte shl 16) + (uByte shl 8) + uByte
    val long: Long
        get() = ((uByte shl 56) + (uByte shl 48) + (uByte shl 40) + (uByte shl 32) + (uByte shl 24) + (uByte shl 16) + (uByte shl 8) + uByte).toLong()
    val uSmart: Int
        get() {
            val i: Int = buffer[offset].toInt()  and 0xff
            return if (i < 128) {
                uByte
            } else {
                uShort - 32768
            }
        }
    val medium: Int
        get() {
            offset += 3
            return buffer[offset - 3].toInt()  and 0xff shl 16 or (buffer[offset - 2].toInt()  and 0xff shl 8) or (buffer[offset - 1].toInt()  and 0xff)
        }
    val nString: String
        get() {
            val i = offset
            while (buffer[offset++].toInt() != 0);
            return String(buffer, i, offset - i - 1)
        }
    val bytes: ByteArray
        get() {
            val i = offset
            while (buffer[offset++].toInt() != 10);
            val abyte0 = ByteArray(offset - i - 1)
            System.arraycopy(buffer, i, abyte0, i - i, offset - 1 - i)
            return abyte0
        }

    fun read(length: Int): ByteArray {
        val b = ByteArray(length)
        for (i in 0 until length) b[i] = buffer[offset++]
        return b
    }
}