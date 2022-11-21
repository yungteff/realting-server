package com.realting.world.clip.stream

class ByteStreamExt(var buffer: ByteArray) {
    fun skip(length: Int) {
        currentOffset += length
    }

    fun readUnsignedByte(): Int {
        return buffer[currentOffset++].toInt() and 0xff
    }

    fun readSignedByte(): Byte {
        return buffer[currentOffset++]
    }

    fun readUnsignedShort(): Int {
        currentOffset += 2
        return (buffer[currentOffset - 2].toInt() and 0xff shl 8) + (buffer[currentOffset - 1].toInt() and 0xff)
    }

    fun readSignedShort(): Int {
        currentOffset += 2
        var i: Int = (buffer[currentOffset - 2].toInt() and 0xff shl 8) + (buffer[currentOffset - 1].toInt() and 0xff)
        if (i > 32767) i -= 0x10000
        return i
    }

    fun read3Bytes(): Int {
        currentOffset += 3
        return (buffer[currentOffset - 3].toInt() and 0xff shl 16) + (buffer[currentOffset - 2].toInt() and 0xff shl 8) + (buffer[currentOffset - 1].toInt() and 0xff)
    }

    fun readR3Bytes(): Int {
        currentOffset += 3
        return (buffer[currentOffset - 1].toInt() and 0xff shl 16) + (buffer[currentOffset - 2].toInt() and 0xff shl 8) + (buffer[currentOffset - 3].toInt() and 0xff)
    }

    fun get24BitInt(): Int {
        return (readUnsignedByte() shl 16) + (readUnsignedByte() shl 8) + readUnsignedByte()
    }

    fun readDWord(): Int {
        currentOffset += 4
        return (buffer[currentOffset - 4].toInt() and 0xff shl 24) + (buffer[currentOffset - 3].toInt() and 0xff shl 16) + (buffer[currentOffset - 2].toInt() and 0xff shl 8) + (buffer[currentOffset - 1].toInt() and 0xff)
    }

    fun readQWord(): Long {
        val l = readDWord().toLong() and 0xffffffffL
        val l1 = readDWord().toLong() and 0xffffffffL
        return (l shl 32) + l1
    }

    fun readString(): String {
        val i = currentOffset
        while (buffer[currentOffset++].toInt() != 10);
        return String(buffer, i, currentOffset - i - 1)
    }

    fun readNewString(): String {
        val i = currentOffset
        while (buffer[currentOffset++].toInt() != 0);
        return String(buffer, i, currentOffset - i - 1)
    }

    fun readBytes(): ByteArray {
        val i = currentOffset
        while (buffer[currentOffset++].toInt() != 10);
        val abyte0 = ByteArray(currentOffset - i - 1)
        System.arraycopy(buffer, i, abyte0, i - i, currentOffset - 1 - i)
        return abyte0
    }

    fun readBytes(i: Int, j: Int, abyte0: ByteArray) {
        for (l in j until j + i) abyte0[l] = buffer[currentOffset++]
    }

    @JvmField
    var currentOffset = 0 //removed useless static initializer
}