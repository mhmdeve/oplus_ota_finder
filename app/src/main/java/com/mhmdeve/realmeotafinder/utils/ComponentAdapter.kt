package com.mhmdeve.realmeotafinder.utils

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mhmdeve.realmeotafinder.R

class ComponentAdapter(private val components: List<Component>, private val context: Context) :
    RecyclerView.Adapter<ComponentAdapter.ComponentViewHolder>() {

    class ComponentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvComponentName)
        val version: TextView = view.findViewById(R.id.tvComponentVersion)
        val size: TextView = view.findViewById(R.id.tvSize)
        val md5: TextView = view.findViewById(R.id.tvMd5)
        val androidVersion: TextView = view.findViewById(R.id.tvAndroidVersion)
        val securityPatch: TextView = view.findViewById(R.id.tvSecurityPatch)
        val realOsVersion: TextView = view.findViewById(R.id.tvRealOsVersion)
        val downloadButton: Button = view.findViewById(R.id.btnDownload)
        val copyLinkButton: Button = view.findViewById(R.id.btnCopyLink)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_component, parent, false)
        return ComponentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComponentViewHolder, position: Int) {
        val component = components[position]
        holder.name.text = "OTA Details"
        holder.version.text = "Version: ${component.componentVersion}"
        holder.size.text = "Size: ${component.size}"
        holder.md5.text = "MD5: ${component.md5}"
        holder.androidVersion.text = "Android Version: ${component.androidVersion}"
        holder.securityPatch.text = "Security Patch: ${component.securityPatch}"
        holder.realOsVersion.text = "OS Version: ${component.realOsVersion}"

        holder.downloadButton.setOnClickListener {
            val url = component.url

            // Extract filename from URL
            val fileName = url.substring(url.lastIndexOf('/') + 1)

            // Initialize DownloadManager
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading OTA Update")
                .setDescription("Downloading $fileName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            // Enqueue the request
            val downloadManager = ContextCompat.getSystemService(context, DownloadManager::class.java)
            downloadManager?.enqueue(request)

            Toast.makeText(context, "Download Started: $fileName", Toast.LENGTH_SHORT).show()
        }

        holder.copyLinkButton.setOnClickListener {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Download Link", component.url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = components.size
}
