package io.github.mirancz.gtfs.export.writer

import io.github.mirancz.gtfs.ParsedGtfs
import io.github.mirancz.gtfs.export.SingleWriter
import io.github.mirancz.libreinfo.codec.ApiCodec
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.ApiEntry

class ApiWriter : SingleWriter("api") {

    override fun write(gtfs: ParsedGtfs, os: CheckedOutputStream) {
        val api = gtfs.api

        ApiCodec.write(os, api.map { e ->
            ApiEntry(
                e.trip_id,
                e.line_id,
                e.route_id
            )
        })
    }

}