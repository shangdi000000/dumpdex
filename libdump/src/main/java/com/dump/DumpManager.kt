package com.dump

import android.Manifest
import android.content.Context
import android.util.Log
import com.dump.read.ReadFile
import com.dump.utils.*
import kotlinx.coroutines.experimental.*
import java.util.regex.Pattern
import java.io.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import android.R.attr.versionCode
import android.R.attr.versionName
import android.content.pm.PackageManager
import com.dump.bean.ApkResult
import com.dump.bean.HashBean
import com.dump.http.ApiResponse
import com.dump.http.HttpServiceManager
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.SafeSubscriber
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest


/**
 * Created by huan on 2018/5/11.
 */
class DumpManager private constructor(var context: Context) {

    private var dicGroupList = ArrayList<String>()
    private val unzipFilePath = "sdcard/SecurityEngine"
    private var startRead: Long = 0
    private var charDic: MutableMap<String, String> = HashMap()
    private var protectDic: MutableMap<String, String> = HashMap()
    private lateinit var dumpCmdFilePath: String
    private var result = ArrayList<ApkResult>()

    companion object {
        private var instance: DumpManager? = null

        fun getInstance(context: Context): DumpManager {
            if (instance == null)
                instance = DumpManager(context)
            return instance!!
        }
    }


    /**
     * 外部调用
     */
    fun dumpApk(hashList: List<HashBean>) {
        result.clear()

        HttpServiceManager.getInstance().scanTclhash(hashList, context.packageName)
                .flatMap(Function<ApiResponse<List<ApkResult>>, ObservableSource<List<ApkResult>>> {
                    var firstResult = it.scanRes
                    var needDumpList = firstResult.filter { it.needDump() }

                    if (needDumpList.isEmpty()) {
                        return@Function ObservableSource<List<ApkResult>> {
                            it.onNext(firstResult)
                            it.onComplete()
                        }
                    } else {
                        needDumpList.forEach {
                            var needDumpApk = it
                            hashList.filter {
                                it.hash == needDumpApk.tCLHash
                            }
                        }
                        hashList.forEach {
                            Log.e("DumpManager", "need dump apk : $it")
                        }
                        var apkListArr = JSONArray()
                        hashList.forEach {
                            val apkPath = PackageUtils.getPackageInfoByPkg(context, it.packageName)!!.applicationInfo.publicSourceDir
                            Log.e("DumpManager", "dump apkpath : $apkPath")
                            var feature = doDump(apkPath)
                            Log.e("DumpManager", "doDump ret $feature")
                            if (feature.contains(",")) {
                                var apkInfo = JSONObject()
                                apkInfo.put("TCLHash", it.hash)
                                apkInfo.put("PkgName", it.packageName)
                                apkInfo.put("Feature", feature)
                                apkInfo.put("SigMd5", getSigHash(it.packageName))
                                apkListArr.put(apkInfo)

                            } else {
                                result.add(ApkResult(it.hash, "-1", "-1", feature))
                            }
                        }
                        return@Function HttpServiceManager.getInstance().scanFeature(apkListArr, context.packageName)
                    }

                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    result.addAll(it)

                    result.forEach {
                        Log.e("DumpManager", "result : ${it.toString()}")
                    }

                }) {
                    it.printStackTrace()
                }

    }


    /**
     * dump
     */
    private fun doDump(apkPath: String): String {
        Log.e("DumpManager", "start doDump")
        if (!PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return StatusConst.NO_SD_PERMISSION
        }
        //将dump命令行工具copy到sd卡
        dumpCmdFilePath = context.filesDir.parent + "/CodeDump_pie"
        FileUtils.copyOneFileFromAssetsToSD(context, "CodeDump_pie", dumpCmdFilePath)

        //解压
        var start = System.currentTimeMillis()
        var apkFile = File(apkPath)
        if (!apkFile.exists()) {
            return StatusConst.FILE_NOT_EXIT
        }
        var unZipFile = File(unzipFilePath)
        if (unZipFile.exists()) {
            FileUtils.deleteDir(unzipFilePath)
        }
        var allFiles = ZipUtils.unzipApk(apkPath, unzipFilePath)

        var upzip = System.currentTimeMillis()
        Log.e("----", "upzip time  ${upzip - start}")

        //判断是否加固
        var libNameStr = ZipUtils.getLibNames(apkPath)
        Log.e("----", "libs $libNameStr")
        protectDic.values.forEach {
            if (libNameStr.contains(it)) {
                return StatusConst.APK_IS_PROTECT
            }
        }

        //判断dex文件是否小于3个
        var dexFiles = allFiles.takeIf {
            it.size < 3
        } ?: return StatusConst.DEX_FILES_TOO_MANY

        //判断dex是否大于16M
        dexFiles.sumByDouble {
            (it.length() / 1024f).toDouble()
        }.takeIf {
            Log.e("----", "dex length $it")
            it < 16 * 1024
        } ?: return StatusConst.DEX_FILES_TOO_LARGE

        //正常开始dump
        startRead = System.currentTimeMillis()
        var charSb = StringBuilder()
        dexFiles.sortedBy {
            it.absolutePath
        }.forEach {
            Log.e("----", "absolutePath ${it.absolutePath}")
            var ret = caculateFile1(it)
            charSb.append(ret)

            Log.e("----", "result str $charSb")
        }

        var result = StringBuilder()
        dicGroupList.forEachIndexed { index, it ->
            var appearNum = appearNumber(charSb.toString(), it)
            if (index != 0) {
                result.append(",")
            }
            result.append(appearNum)
        }

        FileUtils.deleteDir(unzipFilePath)
        Log.e("----", "all read time  ${System.currentTimeMillis() - startRead}")
        return result.toString()
    }


    /**
     * 线程池计算
     */
    private fun caculateFileMix(dexFile: File): String {
        var start = System.currentTimeMillis()
        val cmdStr = "dexdump -d ${dexFile.absolutePath} > $unzipFilePath/${dexFile.name}.txt"
        var cmdRet = ShellUtils.execCmd(cmdStr, false)
        Log.e("----", "cmd ret success -> ${cmdRet.successMsg}, failure -> ${cmdRet.errorMsg}")
        var dump = System.currentTimeMillis()
        Log.e("----", "dump time  ${dump - start}")
        var txtFile = File("$unzipFilePath/${dexFile.name}.txt")

        var fis: FileInputStream? = null
        try {
            val readFile = ReadFile()
            fis = FileInputStream(txtFile)
            val available = fis.available()
            val maxThreadNum = 10
            // 线程粗略开始位置
            val i = available / maxThreadNum


            val fixedThreadPool = Executors.newFixedThreadPool(maxThreadNum)
            var map = HashMap<Int, String>()
            for (j in 0 until maxThreadNum) {
                val startNum = if (j == 0) 0 else readFile.getStartNum(txtFile, (i * j).toLong())
                val endNum = if (j + 1 < maxThreadNum) readFile.getStartNum(txtFile, (i * (j + 1)).toLong()) else -2

                fixedThreadPool.execute {
                    val readFile = ReadFile()
                    var charSb = StringBuilder()
                    try {
                        var outStrList = readFile.readFileByLine(txtFile.absolutePath, startNum, endNum + 1)
                        outStrList.forEach {
                            if (charDic[it] != null) {
                                charSb.append(charDic[it])
                            }
                        }
                        Log.e("DumpManager", "out str ${charSb.toString()}")
                        map[j] = charSb.toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }
            fixedThreadPool.shutdown()
            try {//等待直到所有任务完成
                fixedThreadPool.awaitTermination(java.lang.Long.MAX_VALUE, TimeUnit.MINUTES)
                var retSb = StringBuilder();
                map.forEach {
                    retSb.append(it.value)
                }
                return retSb.toString()

            } catch (e: InterruptedException) {
                e.printStackTrace()
            }


        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""

    }

    /**
     * 单线程计算
     */
    private fun caculateFile(dexFile: File, sb: StringBuilder) {
        var start = System.currentTimeMillis()
        val cmdStr = "dexdump -d ${dexFile.absolutePath} > $unzipFilePath/${dexFile.name}.txt"
        ShellUtils.execCmd(cmdStr, false)
        var dump = System.currentTimeMillis()
        Log.e("----", "dump time  ${dump - start}")

        var txtFile = File("$unzipFilePath/${dexFile.name}.txt")
        startRead = System.currentTimeMillis()
        txtFile.forEachLine {
            if (it.contains("\\|".toRegex())) {
                var strArr = it.split("\\|".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                if (strArr.size > 1) {
                    var indexArr = strArr[1].split("\\s".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    if (indexArr.size > 1) {
                        if (charDic.containsKey(indexArr[1])) {
                            Log.e("----", "file path ${indexArr[1]}")
                            sb.append(charDic[indexArr[1]])
                        }
                    }
                }
            }
        }
    }

    /**
     * newest
     */

    private fun caculateFile1(dexFile: File): String {

        val ret = ShellUtils.execCmd("chmod 766 $dumpCmdFilePath", false)

        Log.e("----", "chmod cmd error : " + ret.errorMsg + ", succss : " + ret.successMsg)

        val cmdStr = "$dumpCmdFilePath -d ${dexFile.absolutePath} > $unzipFilePath/${dexFile.name}.txt"
        val ret1 = ShellUtils.execCmd(cmdStr, false)
        Log.e("----", "dump cmd error : " + ret1.errorMsg + ", succss : " + ret1.successMsg)

        var txtFile = File("$unzipFilePath/${dexFile.name}.txt")
        var txtContent = txtFile.readText()
        return if (txtContent.contains("verified")) {
            txtContent.substringAfterLast("verified")
        } else {
            txtContent
        }
    }


    /**
     * 拼接排列组合
     */
    private fun per(buf: CharArray, chs: CharArray, len: Int) {
        if (len == -1) {
            var sb = StringBuilder()
            for (i in buf.indices.reversed())
                sb.append(buf[i])
            dicGroupList.add(sb.toString())
            return
        }
        for (i in chs.indices) {
            buf[len] = chs[i]
            per(buf, chs, len - 1)
        }
    }

    /**
     * 计算出现次数
     */
    private fun appearNumber(srcText: String, findText: String): Int {
        var count = 0
        val p = Pattern.compile(findText)
        val m = p.matcher(srcText)
        while (m.find()) {
            count++
        }
        return count
    }

    init {
        val chs = charArrayOf('M', 'R', 'G', 'I', 'T', 'P', 'V')
        per(CharArray(3), chs, 3 - 1)
        charDic = MapUtils.getHashMapResource(context, R.xml.core_char_dic)
        protectDic = MapUtils.getHashMapResource(context, R.xml.protect_dic)

    }

    fun getSigHash(pkgName: String): String {
        try {
            var pkg = context.packageManager.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES)
            var digest: MessageDigest? = null
            try {
                digest = MessageDigest.getInstance("MD5")
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
            val bytes = pkg.signatures[0].toByteArray()
            digest.update(bytes)
            return digestToString(digest.digest())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    private fun digestToString(dig: ByteArray): String {
        var builder = StringBuilder()
        for (i in dig.indices) {
            var k = dig[i].toInt()
            if (k < 0) {
                k += 256
            }
            builder.append(String.format("%02X", k))
        }
        return builder.toString()
    }


}