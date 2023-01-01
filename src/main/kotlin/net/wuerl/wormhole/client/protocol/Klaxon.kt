package net.wuerl.wormhole.client.protocol

import com.beust.klaxon.FieldRenamer
import com.beust.klaxon.Klaxon

private val renamer = object : FieldRenamer {
    override fun toJson(fieldName: String) = FieldRenamer.camelToUnderscores(fieldName)
    override fun fromJson(fieldName: String) = FieldRenamer.underscoreToCamel(fieldName)
}

val klaxon = Klaxon().fieldRenamer(renamer)