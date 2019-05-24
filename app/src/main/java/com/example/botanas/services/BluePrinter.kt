package com.example.botanas.services

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.widget.Toast
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.example.botanas.R
import java.io.File
import java.io.OutputStream
import java.util.*
import kotlin.experimental.or
import android.R.attr.bitmap





class BluePrinter(context: Context) {
    companion object {
        var device: BluetoothDevice? = null
        private lateinit var mBTSocket: BluetoothSocket
        private lateinit var os: OutputStream
        private var connected = false
        private lateinit var uuid: BluetoothSocket
    }
    private val context = context
    private var mWidth = 0
    private var mHeight = 0
    private var mStatus = ""
    private lateinit var dots: BitSet

    init {

    }

    fun connected(): Boolean {
        return connected
    }

    fun setPrinter(device: BluetoothDevice): Boolean {
        try {
            if (!connected()) {
                uuid = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
                mBTSocket = uuid
                mBTSocket.connect()
                os = mBTSocket.outputStream
                os.flush()
                connected = true
                return true

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun unsetPrinter() {
        if  (connected()) {
            mBTSocket.close()
            connected = false
        }

    }

    fun printSale(idRequisition: Int) {
        //os.write(PrinterCommands.FONT_SIZE_SMALL)
        //os.write(PrinterCommands.ESC_ALIGN_CENTER)
        //os.write("Hola mundo!\n".toByteArray())

        val bitMap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_botanas_50x50)
        val fileName ="logo_botanas_50x50.jpg"
        try {
            val out1 = context.openFileOutput(fileName, Context.MODE_PRIVATE)

            bitMap.compress(Bitmap.CompressFormat.JPEG, 100, out1)

            out1.flush()

            out1.close()

            val f = context.getFileStreamPath(fileName)

            val mPath = f.absolutePath
            Log.d("path", mPath)
            print_image(mPath)
        } catch (e: java.lang.Exception){
            e.printStackTrace()
        }


        /*try {
            val mySqlHelper = MySqlHelper(context)
            val currency = NumberFormat.getCurrencyInstance()
            //os.write(PrinterCommands.CHARCODE_PC850)
            val title = "Botanas Anita".toByteArray()
            os.apply {
                write(PrinterCommands.FONT_SIZE_LARGE)
                write(PrinterCommands.ESC_ALIGN_CENTER)
                write(title)
                write(PrinterCommands.SET_LINE)
                write(PrinterCommands.SET_LINE)


            }
            var clientName: ByteArray = byteArrayOf()
            var subTotal = 0.00
            var total = byteArrayOf()
            var discount = byteArrayOf()
            mySqlHelper.use {
                select("requisition")
                    .whereArgs("id_requisition == {id_requisition}", "id_requisition" to idRequisition)
                    .exec {
                        this.moveToNext()
                        val idClient = this.getInt(this.getColumnIndex("id_client"))
                        discount = this.getString(this.getColumnIndex("discount")).toByteArray()
                        total = currency.format(this.getDouble(this.getColumnIndex("total"))).toByteArray()
                        select("client")
                            .whereArgs("id_client == {id_client}", "id_client" to idClient)
                            .exec {
                                this.moveToNext()
                                clientName = ("Cliente:\n" + this.getString(this.getColumnIndex("name"))).toByteArray()
                            }
                    }
            }
            os.apply {
                write(PrinterCommands.FONT_SIZE_MEDIUM)
                write(PrinterCommands.ESC_ALIGN_LEFT)
                write(PrinterCommands.ESC_CANCEL_BOLD)
                write(clientName)
                write(PrinterCommands.SET_LINE)
                write(PrinterCommands.SET_LINE)
            }
            mySqlHelper.use {
                select("requisition_description")
                    .whereArgs("id_requisition == {id_requisition}", "id_requisition" to idRequisition)
                    .exec {
                        while (this.moveToNext()) {

                            val quantity = this.getString(this.getColumnIndex("quantity"))+"x"
                            val name = this.getString(this.getColumnIndex("description"))
                            val totalProduct =  currency.format(this.getDouble(this.getColumnIndex("total")))
                            subTotal += this.getDouble(this.getColumnIndex("total"))
                            os.apply {

                                write(PrinterCommands.FONT_SIZE_SMALL)
                                write(PrinterCommands.ESC_CANCEL_BOLD)
                                write(PrinterCommands.ESC_ALIGN_LEFT)

                                write(quantity.toByteArray())
                                write(PrinterCommands.BLANK_SPACE)
                                write(PrinterCommands.BLANK_SPACE)

                                write(name.toByteArray())
                                write(PrinterCommands.SET_LINE)

                                write(PrinterCommands.ESC_ALIGN_RIGHT)
                                write(totalProduct.toByteArray())
                                write(PrinterCommands.SET_LINE)
                            }

                        }
                    }
            }
            val subTotalString = currency.format(subTotal).toByteArray()
            os.apply {
                write(PrinterCommands.ESC_ALIGN_LEFT)
                write(PrinterCommands.SET_LINE)
                write("Subtotal:".toByteArray())
                write(PrinterCommands.SET_LINE)
                write(PrinterCommands.ESC_ALIGN_RIGHT)
                write(subTotalString)
                write(PrinterCommands.SET_LINE)

                write(PrinterCommands.ESC_ALIGN_LEFT)
                write("Descuento:".toByteArray())
                write(PrinterCommands.SET_LINE)
                write(PrinterCommands.ESC_ALIGN_RIGHT)
                write(discount)
                write(PrinterCommands.SET_LINE)

                write(PrinterCommands.ESC_ALIGN_LEFT)
                write("Total:".toByteArray())
                write(PrinterCommands.SET_LINE)
                write(PrinterCommands.ESC_ALIGN_RIGHT)
                write(total)
                write(PrinterCommands.SET_LINE)
                write(PrinterCommands.SET_LINE)
                write(PrinterCommands.SET_LINE)
            }



        } catch (e: Exception) {
            e.printStackTrace()
        }*/

    }


    private fun print_image(file: String) {
        val fl = File(file)
        if (fl.exists()) {
            val bmp = BitmapFactory.decodeFile(file)
            convertBitmap(bmp)
            os.write(PrinterCommands.SET_LINE_SPACING_24)

            var offset = 0
            while (offset < bmp.height) {
                os.write(PrinterCommands.SELECT_BIT_IMAGE_MODE)
                for (x in 0 until bmp.width) {

                    for (k in 0..2) {

                        var slice: Byte = 0
                        for (b in 0..7) {
                            val y = (offset / 8 + k) * 8 + b
                            val i = y * bmp.width + x
                            var v = false
                            if (i < dots.length()) {
                                v = dots.get(i)
                            }
                            slice = slice or ((if (v) 1 else 0) shl 7 - b).toByte()
                        }
                        os.write(byteArrayOf(slice))
                    }
                }
                offset += 24
                os.write(PrinterCommands.FEED_LINE)
            }
            os.write(PrinterCommands.SET_LINE_SPACING_30)


        } else {
            Toast.makeText(context, "file doesn't exists", Toast.LENGTH_SHORT)
                .show()
        }
    }


    fun convertBitmap(inputBitmap: Bitmap): String {

        mWidth = inputBitmap.width
        mHeight = inputBitmap.height

        convertArgbToGrayscale(inputBitmap, mWidth, mHeight)
        mStatus = "ok"
        return mStatus

    }

    private fun convertArgbToGrayscale(
        bmpOriginal: Bitmap, width: Int,
        height: Int
    ) {
        var pixel: Int
        var k = 0
        var B = 0
        var G = 0
        var R = 0
        dots = BitSet()
        try {

            for (x in 0 until height) {
                for (y in 0 until width) {
                    // get one pixel color
                    pixel = bmpOriginal.getPixel(y, x)

                    // retrieve color of all channels
                    R = Color.red(pixel)
                    G = Color.green(pixel)
                    B = Color.blue(pixel)
                    // take conversion up to one single value by calculating
                    // pixel intensity.
                    B = (0.299 * R + 0.587 * G + 0.114 * B).toInt()
                    G = B
                    R = G
                    // set bit into bitset, by calculating the pixel's luma
                    if (R < 55) {
                        dots.set(k)//this is the bitset that i'm printing
                    }
                    k++

                }


            }


        } catch (e: Exception) {
            // TODO: handle exception
            Log.e(TAG, e.toString())
        }

    }

}

object PrinterCommands {
    val HT: Byte = 0x9
    val LF: Byte = 0x0A
    val CR: Byte = 0x0D
    val ESC: Byte = 0x1B
    val DLE: Byte = 0x10
    val GS: Byte = 0x1D
    val FS: Byte = 0x1C
    val STX: Byte = 0x02
    val US: Byte = 0x1F
    val CAN: Byte = 0x18
    val CLR: Byte = 0x0C
    val EOT: Byte = 0x04
    val CHARCODE_PC850 = byteArrayOf(27, 116, 0x02)

    //val FONT_SIZE_SMALL = byteArrayOf(27, 33, 13)
    val FONT_SIZE_SMALL = byteArrayOf(27, 33, 13)
    val FONT_SIZE_MEDIUM = byteArrayOf(27, 33, 6)
    val FONT_SIZE_LARGE = byteArrayOf(27, 33, 127)
    //val FONT_SIZE_LARGE = byteArrayOf(29, 33, 0x10)
    val SET_LINE = "\n".toByteArray()
    val BLANK_SPACE = " ".toByteArray()


    val INIT = byteArrayOf(27, 64)
    var FEED_LINE = byteArrayOf(10)

    var SELECT_FONT_A = byteArrayOf(20, 33, 0)

    var SET_BAR_CODE_HEIGHT = byteArrayOf(29, 104, 100)
    var PRINT_BAR_CODE_1 = byteArrayOf(29, 107, 2)
    var SEND_NULL_BYTE = byteArrayOf(0x00)

    var SELECT_PRINT_SHEET = byteArrayOf(0x1B, 0x63, 0x30, 0x02)
    var FEED_PAPER_AND_CUT = byteArrayOf(0x1D, 0x56, 66, 0x00)

    var SELECT_CYRILLIC_CHARACTER_CODE_TABLE = byteArrayOf(0x1B, 0x74, 0x11)

    var SELECT_BIT_IMAGE_MODE = byteArrayOf(0x1B, 0x2A, 33, 255.toByte(), 3)
    var SET_LINE_SPACING_24 = byteArrayOf(0x1B, 0x33, 24)
    var SET_LINE_SPACING_30 = byteArrayOf(0x1B, 0x33, 30)

    var TRANSMIT_DLE_PRINTER_STATUS = byteArrayOf(0x10, 0x04, 0x01)
    var TRANSMIT_DLE_OFFLINE_PRINTER_STATUS = byteArrayOf(0x10, 0x04, 0x02)
    var TRANSMIT_DLE_ERROR_STATUS = byteArrayOf(0x10, 0x04, 0x03)
    var TRANSMIT_DLE_ROLL_PAPER_SENSOR_STATUS = byteArrayOf(0x10, 0x04, 0x04)

    val ESC_FONT_COLOR_DEFAULT = byteArrayOf(0x1B, 'r'.toByte(), 0x00)
    val FS_FONT_ALIGN = byteArrayOf(0x1C, 0x21, 1, 0x1B, 0x21, 1)
    val ESC_ALIGN_LEFT = byteArrayOf(0x1b, 'a'.toByte(), 0x00)
    val ESC_ALIGN_RIGHT = byteArrayOf(0x1b, 'a'.toByte(), 0x02)
    val ESC_ALIGN_CENTER = byteArrayOf(0x1b, 'a'.toByte(), 0x01)
    val ESC_CANCEL_BOLD = byteArrayOf(0x1B, 0x45, 0)


    /** */
    val ESC_HORIZONTAL_CENTERS = byteArrayOf(0x1B, 0x44, 20, 28, 0)
    val ESC_CANCLE_HORIZONTAL_CENTERS = byteArrayOf(0x1B, 0x44, 0)
    /** */

    val ESC_ENTER = byteArrayOf(0x1B, 0x4A, 0x40)
    val PRINTE_TEST = byteArrayOf(0x1D, 0x28, 0x41)

}