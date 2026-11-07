package io.github.mirancz.gtfs.export.writer

import io.github.mirancz.gtfs.ParsedGtfs
import io.github.mirancz.gtfs.export.SingleWriter
import io.github.mirancz.gtfs.remap.IdType
import io.github.mirancz.gtfs.remap.Mapper
import io.github.mirancz.libreinfo.codec.PostsCodec
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.Post
import io.github.mirancz.libreinfo.model.types.Location
import io.github.mirancz.request.ids.ApiPost

class PostWriter(val posts: List<ApiPost>) : SingleWriter("posts") {

    override fun write(gtfs: ParsedGtfs, os: CheckedOutputStream) {
        PostsCodec.write(os, posts.map { p ->
            // VERY careful here, we need to use the mapped stop id
            val stopId: Int = Mapper.map(IdType.STOP, p.stopId)

            Post(
                stopId,
                p.postId,
                p.name,
                Location(p.latitude, p.longitude)
            )
        })
    }

}