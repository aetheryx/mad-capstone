package nl.hva.chatstone.util

import android.content.Context
import android.net.Uri

fun resourceToURI(context: Context, resource: Int): Uri =
  Uri.parse("android.resource://${context.packageName}/$resource")
