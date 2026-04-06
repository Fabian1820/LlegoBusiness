package com.llego.shared.data.model

data class MediaUrls(
    val avatarUrl: String? = null,
    val avatarUrlBaja: String? = null,
    val avatarUrlAlta: String? = null,
    val coverUrl: String? = null,
    val coverUrlBaja: String? = null,
    val coverUrlAlta: String? = null
)

fun MediaUrls.avatarSmall(): String? = avatarUrlBaja ?: avatarUrl ?: avatarUrlAlta

fun MediaUrls.avatarLarge(): String? = avatarUrlAlta ?: avatarUrl ?: avatarUrlBaja

fun MediaUrls.coverFast(): String? = coverUrlBaja ?: coverUrl ?: coverUrlAlta

fun MediaUrls.coverBest(): String? = coverUrlAlta ?: coverUrl ?: coverUrlBaja

fun Branch.mediaUrls(): MediaUrls = MediaUrls(
    avatarUrl = avatarUrl,
    avatarUrlBaja = avatarUrlBaja,
    avatarUrlAlta = avatarUrlAlta,
    coverUrl = coverUrl,
    coverUrlBaja = coverUrlBaja,
    coverUrlAlta = coverUrlAlta
)

fun Business.mediaUrls(): MediaUrls = MediaUrls(
    avatarUrl = avatarUrl,
    avatarUrlBaja = avatarUrlBaja,
    avatarUrlAlta = avatarUrlAlta
)

fun BusinessWithBranches.mediaUrls(): MediaUrls = MediaUrls(
    avatarUrl = avatarUrl,
    avatarUrlBaja = avatarUrlBaja,
    avatarUrlAlta = avatarUrlAlta
)

fun Branch.avatarSmallUrl(): String? = mediaUrls().avatarSmall()

fun Branch.avatarLargeUrl(): String? = mediaUrls().avatarLarge()

fun Branch.coverFastUrl(): String? = mediaUrls().coverFast()

fun Branch.coverBestUrl(): String? = mediaUrls().coverBest()

fun Business.avatarSmallUrl(): String? = mediaUrls().avatarSmall()

fun Business.avatarLargeUrl(): String? = mediaUrls().avatarLarge()

fun BusinessWithBranches.avatarSmallUrl(): String? = mediaUrls().avatarSmall()

fun BusinessWithBranches.avatarLargeUrl(): String? = mediaUrls().avatarLarge()
