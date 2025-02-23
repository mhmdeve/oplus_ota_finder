package com.mhmdeve.realmeotafinder.utils

data class Component(
    val componentId: String,
    val componentName: String,
    val componentVersion: String,
    val size: String,
    val manualUrl: String,
    val url: String,
    val md5: String,
    val otaStreamingProperty: String?,
    val vabPackageHash: String?,
    val extraParams: String?,
    val fileHash: String?,
    val fileSize: String?,
    val metadataHash: String?,
    val metadataSize: String?,
    val androidVersion: String?,
    val oplusRomVersion: String?,
    val securityPatch: String?,
    val securityPatchVendor: String?,
    val mainlineVersion: String?,
    val versionTypeId: String?,
    val versionName: String?,
    val realAndroidVersion: String?,
    val realOsVersion: String?,
    val osVersion: String?,
    val colorOSVersion: String?
)
