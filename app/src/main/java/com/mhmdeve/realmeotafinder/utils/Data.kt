package com.mhmdeve.realmeotafinder.utils

class Data {
    companion object {
        // Headers configuration
        val defaultHeaders = mapOf(
            "language" to "en-EN",            // lang-LANG
            "romVersion" to "unknown",        // ro.build.version.ota
            "otaVersion" to "unknown",        // ro.build.version.ota
            "androidVersion" to "unknown",    // Android{Version}
            "colorOSVersion" to "unknown",    // ColorOS{Version}
            "model" to "unknown",             // ro.product.name
            "infVersion" to "1",              // N/A
            "operator" to "unknown",          // ro.product.name
            "nvCarrier" to "unknown",         // ro.build.oplus_nv_id
            "uRegion" to "unknown",           // persist.sys.oppo.region (RUI1)
            "trackRegion" to "unknown",       // ro.oppo.regionmark (RUI1)
            "imei" to "000000000000000",      // IMEI
            "imei1" to "000000000000000",     // IMEI
            "deviceId" to "0",                // N/A
            "mode" to "client_auto",          // Known values: "manual", "client_auto", "server_auto"
            "channel" to "pc",                // Update channel
            "version" to "1",                 // Request version
            "Accept" to "application/json",   // N/A
            "Content-Type" to "application/json", // N/A
            "User-Agent" to "NULL"            // N/A
        )

        // Request body configuration
        val defaultBody = mapOf(
            "language" to "en-EN",            // lang-LANG
            "romVersion" to "unknown",        // ro.build.version.ota
            "otaVersion" to "unknown",        // ro.build.version.ota
            "androidVersion" to "unknown",    // Android{Version}
            "colorOSVersion" to "unknown",    // ColorOS{Version}
            "model" to "unknown",             // ro.product.name
            "productName" to "unknown",       // ro.product.name
            "operator" to "unknown",          // ro.product.name
            "uRegion" to "unknown",           // persist.sys.oppo.region (RUI1)
            "trackRegion" to "unknown",       // ro.oppo.regionmark (RUI1)
            "imei" to "000000000000000",      // IMEI
            "imei1" to "000000000000000",     // IMEI
            "mode" to "0",                    // 0 for normal, 1 for beta
            "registrationId" to "unknown",    // N/A
            "deviceId" to "0",                // N/A
            "version" to "3",                 // N/A
            "type" to "1",                    // N/A
            "otaPrefix" to "unknown",         // ro.build.version.ota
            "isRealme" to "unknown",          // N/A
            "time" to "0",                    // N/A
            "canCheckSelf" to "0"             // N/A
        )

        // URLs configuration
        val urls = mapOf(
            1 to mapOf(
                "GL" to "https://ifota.realmemobile.com/post/Query_Update",    // GL
                "CN" to "https://iota.coloros.com/post/Query_Update",          // CN
                "IN" to "https://ifota-in.realmemobile.com/post/Query_Update", // IN
                "EU" to "https://ifota-eu.realmemobile.com/post/Query_Update"  // EU
            ),
            2 to mapOf(
                "GL" to "https://component-ota-f.coloros.com/update/v3",       // GL
                "CN" to "https://component-ota.coloros.com/update/v3",         // CN
                "IN" to "https://component-ota-in.coloros.com/update/v3",      // IN
                "EU" to "https://component-ota-eu.coloros.com/update/v3"       // EU
            )
        )

        // Server parameters configuration
        val serverParams: Map<String, ServerConfig> = mapOf(
            "GL" to ServerConfig(
                serverURL = "https://component-otapc-sg.allawnos.com/update/v3",
                pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkA980wxi+eTGcFDiw2I6RrUeO4jL/Aj3Yw4dNuW7tYt+O1sRTHgrzxPD9SrOqzz7G0KgoSfdFHe3JVLPN+U1waK+T0HfLusVJshDaMrMiQFDUiKajb+QKr+bXQhVofH74fjat+oRJ8vjXARSpFk4/41x5j1Bt/2bHoqtdGPcUizZ4whMwzap+hzVlZgs7BNfepo24PWPRujsN3uopl+8u4HFpQDlQl7GdqDYDj2zNOHdFQI2UpSf0aIeKCKOpSKF72KDEESpJVQsqO4nxMwEi2jMujQeCHyTCjBZ+W35RzwT9+0pyZv8FB3c7FYY9FdF/+lvfax5mvFEBd9jO+dpMQIDAQAB",
                negotiationVersion = "1615895993238"
            ),
            "CN" to ServerConfig(
                serverURL = "https://component-otapc-cn.allawntech.com/update/v3",
                pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApXYGXQpNL7gmMzzvajHaoZIHQQvBc2cOEhJc7/tsaO4sT0unoQnwQKfNQCuv7qC1Nu32eCLuewe9LSYhDXr9KSBWjOcCFXVXteLO9WCaAh5hwnUoP/5/Wz0jJwBA+yqs3AaGLA9wJ0+B2lB1vLE4FZNE7exUfwUc03fJxHG9nCLKjIZlrnAAHjRCd8mpnADwfkCEIPIGhnwq7pdkbamZcoZfZud1+fPsELviB9u447C6bKnTU4AaMcR9Y2/uI6TJUTcgyCp+ilgU0JxemrSIPFk3jbCbzamQ6Shkw/jDRzYoXpBRg/2QDkbq+j3ljInu0RHDfOeXf3VBfHSnQ66HCwIDAQAB",
                negotiationVersion = "1615879139745"
            ),
            "IN" to ServerConfig(
                serverURL = "https://component-otapc-in.allawnos.com/update/v3",
                pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwYtghkzeStC9YvAwOQmWylbp74Tj8hhi3f9IlK7A/CWrGbLgzz/BeKxNb45zBN8pgaaEOwAJ1qZQV5G4nProWCPOP1ro1PkemFJvw/vzOOT5uN0ADnHDzZkZXCU/knxqUSfLcwQlHXsYhNsAm7uOKjY9YXF4zWzYN0eFPkML3Pj/zg7hl/ov9clB2VeyI1/blMHFfcNA/fvqDTENXcNBIhgJvXiCpLcZqp+aLZPC5AwY/sCb3j5jTWer0Rk0ZjQBZE1AncwYvUx4mA65U59cWpTyl4c47J29MsQ66hqWv6eBHlDNZSEsQpHePUqgsf7lmO5Wd7teB8ugQki2oz1Y5QIDAQAB",
                negotiationVersion = "1615896309308"
            ),
            "EU" to ServerConfig(
                serverURL = "https://component-otapc-eu.allawnos.com/update/v3",
                pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh8/EThsK3f0WyyPgrtXb/D0Xni6UZNppaQHUqHWo976cybl92VxmehE0ISObnxERaOtrlYmTPIxkVC9MMueDvTwZ1l0KxevZVKU0sJRxNR9AFcw6D7k9fPzzpNJmhSlhpNbt3BEepdgibdRZbacF3NWy3ejOYWHgxC+I/Vj1v7QU5gD+1OhgWeRDcwuV4nGY1ln2lvkRj8EiJYXfkSq/wUI5AvPdNXdEqwou4FBcf6mD84G8pKDyNTQwwuk9lvFlcq4mRqgYaFg9DAgpDgqVK4NTJWM7tQS1GZuRA6PhupfDqnQExyBFhzCefHkEhcFywNyxlPe953NWLFWwbGvFKwIDAQAB",
                negotiationVersion = "1615897067573"
            )
        )

        data class ServerConfig(
            val serverURL: String,
            val pubKey: String,
            val negotiationVersion: String
        )
    }
}