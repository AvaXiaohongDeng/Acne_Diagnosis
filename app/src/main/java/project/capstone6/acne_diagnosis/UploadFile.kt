package project.capstone6.acne_diagnosis

import android.annotation.SuppressLint
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object UploadFile {
    private const val TAG = "uploadFile"
    private const val TIME_OUT = 10 * 10000000 //超时时间
    private const val CHARSET = "utf-8" //设置编码
    private const val BOUNDARY =
        "FlPm4LpSXsE" //UUID.randomUUID().toString(); //边界标识 随机生成 String PREFIX = "--" , LINE_END = "\r\n";
    private const val PREFIX = "--"
    private const val LINE_END = "\r\n"
    private const val CONTENT_TYPE = "multipart/form-data" //内容类型

    /** * android上传文件到服务器
     * @param file 需要上传的文件
     * @param requestURL 请求的rul
     * @return 返回响应的内容
     */
    fun uploadFile(file: File?, requestURL: String?): String {
        return try {
            val url = URL(requestURL)
            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = TIME_OUT
            conn.connectTimeout = TIME_OUT
            conn.doInput = true //允许输入流
            conn.doOutput = true //允许输出流
            conn.useCaches = false //不允许使用缓存
            conn.requestMethod = "POST" //请求方式
            conn.setRequestProperty("Charset", CHARSET) //设置编码
            //头信息
            conn.setRequestProperty("Connection", "keep-alive")
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY)
            if (file != null) {
                /** * 当文件不为空，把文件包装并且上传  */
                val outputSteam = conn.outputStream
                val dos = DataOutputStream(outputSteam)
                val params = arrayOf(
                    "\"ownerId\"",
                    "\"docName\"",
                    "\"docType\"",
                    "\"sessionKey\"",
                    "\"sig\""
                )
                val values = arrayOf(
                    "1410065922",
                    file.name,
                    "jpg",
                    "dfbe0e1686656d5a0c8de11347f93bb6",
                    "e70cff74f433ded54b014e7402cf094a"
                )
                //添加docName,docType,sessionKey,sig参数
                for (i in params.indices) {
                    //添加分割边界
                    val sb = StringBuffer()
                    sb.append(PREFIX)
                    sb.append(BOUNDARY)
                    sb.append(LINE_END)
                    sb.append("Content-Disposition: form-data; name=" + params[i] + LINE_END)
                    sb.append(LINE_END)
                    sb.append(values[i])
                    sb.append(LINE_END)
                    dos.write(sb.toString().toByteArray())
                }

                //file内容
                val sb = StringBuffer()
                sb.append(PREFIX)
                sb.append(BOUNDARY)
                sb.append(LINE_END)
                sb.append("Content-Disposition: form-data; name=\"data\";filename=" + "\"" + file.name + "\"" + LINE_END)
                sb.append("Content-Type: image/jpg" + LINE_END)
                sb.append(LINE_END)
                dos.write(sb.toString().toByteArray())
                //读取文件的内容
                val `is`: InputStream = FileInputStream(file)
                val bytes = ByteArray(1024)
                var len = 0
                while (`is`.read(bytes).also { len = it } != -1) {
                    dos.write(bytes, 0, len)
                }
                `is`.close()
                //写入文件二进制内容
                dos.write(LINE_END.toByteArray())
                //写入end data
                val end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).toByteArray()
                dos.write(end_data)
                dos.flush()
                /**
                 * 获取响应码 200=成功
                 * 当响应成功，获取响应的流
                 */
                val res = conn.responseCode
                Log.e(TAG, "response code:$res")
                if (res == 200) {
                    var oneLine: String?
                    val response = StringBuffer()
                    val input = BufferedReader(InputStreamReader(conn.inputStream))
                    while (input.readLine().also { oneLine = it } != null) {
                        response.append(oneLine)
                    }
                    response.toString()
                } else {
                    res.toString() + ""
                }
            } else {
                "file not found"
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            "failed"
        } catch (e: IOException) {
            e.printStackTrace()
            "failed"
        }
    }

    /**
     * Enables https connections
     */
    @SuppressLint("TrulyRandom")
    fun handleSSLHandshake() {
        try {
            val trustAllCerts: Array<TrustManager> =
                arrayOf<TrustManager>(object : X509TrustManager {
                    val acceptedIssuers: Array<Any?>?
                        get() = arrayOfNulls(0)

                    override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
                    override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        TODO("Not yet implemented")
                    }
                })
            val sc: SSLContext = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
            HttpsURLConnection.setDefaultHostnameVerifier(object : HostnameVerifier {
                override fun verify(arg0: String?, arg1: SSLSession?): Boolean {
                    return true
                }
            })
        } catch (ignored: java.lang.Exception) {
        }
    }

}