package net.wuerl.wormhole.client.protocol

open class Outbound(val type: String)

open class Bind(
    val appid: String = "lothar.com/wormhole/text-or-file-xfer",
    val side: String,
    val clientVersion: Array<String> = arrayOf("kotlin", "0.1.0"),
) : Outbound("bind")
